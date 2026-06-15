package com.civicconnect.server.service;

import com.civicconnect.server.dto.request.IssueCreateRequest;
import com.civicconnect.server.dto.response.IssueResponse;
import com.civicconnect.server.entity.Department;
import com.civicconnect.server.entity.Issue;
import com.civicconnect.server.entity.User;
import com.civicconnect.server.entity.enums.Criticality;
import com.civicconnect.server.entity.enums.IssueScope;
import com.civicconnect.server.entity.enums.IssueStatus;
import com.civicconnect.server.entity.enums.Role;
import com.civicconnect.server.repository.DepartmentRepository;
import com.civicconnect.server.repository.IssueRepository;
import com.civicconnect.server.repository.NotificationRepository;
import com.civicconnect.server.repository.StatusHistoryRepository;
import com.civicconnect.server.repository.UserRepository;
import com.civicconnect.server.repository.VoteRepository;
import com.civicconnect.server.dto.request.IssueStatusUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final VoteRepository voteRepository;

    public IssueService(IssueRepository issueRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            StatusHistoryRepository statusHistoryRepository,
            NotificationRepository notificationRepository,
            VoteRepository voteRepository) {
        this.issueRepository = issueRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.notificationRepository = notificationRepository;
        this.voteRepository = voteRepository;
    }

    /**
     * Get a single issue by ID with all relationships eagerly loaded.
     */
    @Transactional(readOnly = true)
    public IssueResponse getIssue(Integer id) {
        // Because we added @EntityGraph in IssueRepository, this single query
        // fetches the Issue AND the User AND the Department without N+1 queries.
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));

        return IssueResponse.from(issue);
    }

    /**
     * Create a new issue reported by a citizen.
     */
    @Transactional
    public IssueResponse createIssue(String userId, IssueCreateRequest request) {
        // 1. Verify foreign keys exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 2. Map DTO to Entity
        Issue issue = new Issue();
        issue.setUser(user);
        issue.setDepartment(department);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setLocationText(request.getLocationText());
        issue.setImageUrl(request.getImageUrl());

        // 3. Set defaults
        issue.setStatus(IssueStatus.PENDING);
        issue.setScope(request.getScope() != null ? request.getScope() : IssueScope.LOCAL);
        issue.setCriticality(request.getCriticality() != null ? request.getCriticality() : Criticality.MEDIUM);
        issue.setUpvoteCount(0);
        issue.setEscalated(false);

        // 4. Calculate Deadline & Urgency Score (Phase 3 logic to be refined)
        issue.setDeadline(calculateDeadline(issue.getCriticality()));
        issue.setUrgencyScore(
                calculateUrgencyScore(0, issue.getCriticality(), department.getSlug(), issue.getDeadline()));

        // 5. Save and return
        Issue saved = issueRepository.save(issue);
        return IssueResponse.from(saved);
    }

    // ── Accountability State Machine ──────────────────────────────────────────

    /**
     * Updates an issue's status (e.g. PENDING -> IN_PROGRESS).
     * Only MUNICIPAL and SUPERVISOR roles can do this.
     */
    @Transactional
    public IssueResponse updateStatus(String actingUserId, Integer issueId, IssueStatusUpdateRequest request) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actingUser.getRole() != Role.MUNICIPAL && actingUser.getRole() != Role.SUPERVISOR) {
            throw new SecurityException("Only municipal staff or supervisors can change issue status");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus = request.getStatus();

        issue.setStatus(newStatus);

        // If moving to IN_PROGRESS, assign to the officer who moved it
        if (newStatus == IssueStatus.IN_PROGRESS) {
            issue.setAssignedTo(actingUser);
        }

        // If directly resolving (though usually they should use submitResolution)
        if (newStatus == IssueStatus.RESOLVED) {
            issue.setResolvedAt(LocalDateTime.now());
        }

        Issue saved = issueRepository.save(issue);

        // Record the audit trail
        logStatusChange(issue, actingUser, oldStatus, newStatus, request.getNote());

        return IssueResponse.from(saved);
    }

    /**
     * MUNICIPAL officer uploads proof of resolution.
     * Status moves to PENDING_VERIFICATION.
     */
    @Transactional
    public IssueResponse submitResolution(String actingUserId, Integer issueId,
            com.civicconnect.server.dto.request.IssueResolveRequest request) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actingUser.getRole() != Role.MUNICIPAL && actingUser.getRole() != Role.SUPERVISOR) {
            throw new SecurityException("Only municipal staff or supervisors can submit resolutions");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus = IssueStatus.PENDING_VERIFICATION;

        issue.setStatus(newStatus);
        issue.setResolutionPhoto(request.getResolutionPhoto());
        issue.setResolutionNote(request.getResolutionNote());
        issue.setAssignedTo(actingUser);

        Issue saved = issueRepository.save(issue);

        String logNote = request.getResolutionNote() != null ? request.getResolutionNote()
                : "Resolution submitted for verification";
        logStatusChange(issue, actingUser, oldStatus, newStatus, logNote);

        return IssueResponse.from(saved);
    }

    private void logStatusChange(Issue issue, User changedBy, IssueStatus oldStatus, IssueStatus newStatus,
            String note) {
        com.civicconnect.server.entity.StatusHistory history = new com.civicconnect.server.entity.StatusHistory();
        history.setIssue(issue);
        history.setChangedBy(changedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        statusHistoryRepository.save(history);
    }

    /**
     * SUPERVISOR verifies the resolution submitted by MUNICIPAL officer.
     * Stage 1 of verification.
     */
    @Transactional
    public IssueResponse verifyResolution(String actingUserId, Integer issueId,
            com.civicconnect.server.dto.request.IssueVerifyRequest request) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (actingUser.getRole() != Role.SUPERVISOR) {
            throw new SecurityException("Only supervisors can verify a submitted resolution");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (issue.getStatus() != IssueStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Issue is not pending verification");
        }

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus;

        if (request.getApproved()) {
            newStatus = IssueStatus.PENDING_USER_VERIFICATION;
            issue.setStatus(newStatus);
            issue.setVerifiedBy(actingUser);
            issue.setVerifiedAt(LocalDateTime.now());

            // Notify reporter and upvoters
            notifyUsers(issue, "verification_required", "Issue resolution needs confirmation",
                    "A supervisor verified the resolution for “" + issue.getTitle()
                            + "”. Please confirm if it’s resolved.");
        } else {
            // Rejected -> reopen
            newStatus = IssueStatus.IN_PROGRESS;
            issue.setStatus(newStatus);
            issue.setResolutionPhoto(null);
            issue.setResolutionNote(null);
            issue.setVerifiedBy(null);
            issue.setVerifiedAt(null);
        }

        Issue saved = issueRepository.save(issue);

        String note = request.getNote() != null ? request.getNote()
                : (request.getApproved() ? "Resolution verified by supervisor — awaiting citizen confirmation"
                        : "Resolution rejected — issue reopened");
        logStatusChange(issue, actingUser, oldStatus, newStatus, note);

        return IssueResponse.from(saved);
    }

    /**
     * CITIZEN (reporter or upvoter) confirms the resolution.
     * Stage 2 of verification.
     */
    @Transactional
    public IssueResponse confirmResolution(String actingUserId, Integer issueId,
            com.civicconnect.server.dto.request.IssueVerifyRequest request) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (issue.getStatus() != IssueStatus.PENDING_USER_VERIFICATION) {
            throw new IllegalStateException("Issue is not pending user verification");
        }

        boolean isReporter = issue.getUser().getId().equals(actingUser.getId());
        boolean isUpvoter = voteRepository.existsByUserIdAndIssueId(actingUser.getId(), issue.getId());

        if (!isReporter && !isUpvoter) {
            throw new SecurityException("Only the reporter or an upvoter can confirm resolution");
        }

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus newStatus;

        if (request.getApproved()) {
            newStatus = IssueStatus.RESOLVED;
            issue.setStatus(newStatus);
            issue.setResolvedAt(LocalDateTime.now());

            notifyUsers(issue, "issue_update", "Issue closed",
                    "“" + issue.getTitle() + "” has been confirmed resolved and closed.");
        } else {
            newStatus = IssueStatus.IN_PROGRESS;
            issue.setStatus(newStatus);
            issue.setResolutionPhoto(null);
            issue.setResolutionNote(null);
            issue.setVerifiedBy(null);
            issue.setVerifiedAt(null);

            // Best effort notify assignee
            if (issue.getAssignedTo() != null) {
                sendNotification(issue.getAssignedTo(), issue, "issue_update", "Resolution rejected",
                        "“" + issue.getTitle() + "” was rejected during confirmation and has been reopened.");
            }
        }

        Issue saved = issueRepository.save(issue);

        String note = request.getNote() != null ? request.getNote()
                : (request.getApproved() ? "Resolution confirmed by citizen ✅"
                        : "Citizen rejected resolution — issue reopened");
        logStatusChange(issue, actingUser, oldStatus, newStatus, note);

        return IssueResponse.from(saved);
    }

    private void notifyUsers(Issue issue, String type, String title, String body) {
        // Notify reporter
        sendNotification(issue.getUser(), issue, type, title, body);

        // Notify upvoters
        java.util.List<com.civicconnect.server.entity.Vote> votes = voteRepository.findByIssueId(issue.getId());
        for (com.civicconnect.server.entity.Vote vote : votes) {
            if (!vote.getUser().getId().equals(issue.getUser().getId())) {
                sendNotification(vote.getUser(), issue, type, title, body);
            }
        }
    }

    private void sendNotification(User user, Issue issue, String type, String title, String body) {
        com.civicconnect.server.entity.Notification notification = new com.civicconnect.server.entity.Notification();
        notification.setUser(user);
        notification.setIssue(issue);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notificationRepository.save(notification);
    }

    // ── Helper Methods (Will be moved to a dedicated service later) ───────────

    private LocalDateTime calculateDeadline(Criticality criticality) {
        LocalDateTime now = LocalDateTime.now();
        return switch (criticality) {
            case CRITICAL -> now.plusDays(1);
            case HIGH -> now.plusDays(3);
            case MEDIUM -> now.plusDays(7);
            case LOW -> now.plusDays(14);
        };
    }

    private int calculateUrgencyScore(int upvotes, Criticality criticality, String deptSlug, LocalDateTime deadline) {
        int baseScore = switch (criticality) {
            case CRITICAL -> 80;
            case HIGH -> 50;
            case MEDIUM -> 20;
            case LOW -> 10;
        };
        // Simple formula: base score + (upvotes * 2)
        return baseScore + (upvotes * 2);
    }
}
