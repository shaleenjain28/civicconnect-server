package com.civicconnect.server.service;

import com.civicconnect.server.dto.request.IssueCreateRequest;
import com.civicconnect.server.dto.response.IssueResponse;
import com.civicconnect.server.entity.Department;
import com.civicconnect.server.entity.Issue;
import com.civicconnect.server.entity.User;
import com.civicconnect.server.entity.enums.Criticality;
import com.civicconnect.server.entity.enums.IssueScope;
import com.civicconnect.server.entity.enums.IssueStatus;
import com.civicconnect.server.repository.DepartmentRepository;
import com.civicconnect.server.repository.IssueRepository;
import com.civicconnect.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public IssueService(IssueRepository issueRepository, 
                        DepartmentRepository departmentRepository, 
                        UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new civic issue reported by a citizen.
     * 
     * @param userId The ID of the authenticated citizen reporting the issue
     * @param request The validated payload from the frontend
     * @return The created issue formatted as a response DTO
     */
    @Transactional
    public IssueResponse createIssue(String userId, IssueCreateRequest request) {
        // 1. Fetch related entities (User and Department)
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));

        // 2. Instantiate the new Issue
        Issue issue = new Issue();
        issue.setUser(reporter);
        issue.setDepartment(department);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setLocationText(request.getLocationText());
        issue.setImageUrl(request.getImageUrl());
        
        // Default Enums if not provided
        Criticality criticality = request.getCriticality() != null ? request.getCriticality() : Criticality.MEDIUM;
        IssueScope scope = request.getScope() != null ? request.getScope() : IssueScope.LOCAL;
        
        issue.setCriticality(criticality);
        issue.setScope(scope);
        issue.setStatus(IssueStatus.PENDING);
        issue.setUpvoteCount(0);
        issue.setEscalated(false);

        // 3. Calculate computed fields
        issue.setDeadline(calculateDeadline(criticality));
        issue.setUrgencyScore(calculateUrgencyScore(0, criticality, department.getSlug(), issue.getDeadline()));

        // 4. Save and return
        Issue savedIssue = issueRepository.save(issue);
        return IssueResponse.from(savedIssue);
    }

    // ── Private Helper Methods ───────────────────────────────────────────────

    /**
     * Calculates the SLA (Service Level Agreement) deadline based on criticality.
     */
    private LocalDateTime calculateDeadline(Criticality criticality) {
        LocalDateTime now = LocalDateTime.now();
        switch (criticality) {
            case CRITICAL: return now.plusDays(1); // 24 hours
            case HIGH:     return now.plusDays(3);
            case LOW:      return now.plusDays(14);
            case MEDIUM:   
            default:       return now.plusDays(7);
        }
    }

    /**
     * Calculates priority score so officers see most urgent issues first.
     * Base algorithm ported from the Node.js implementation.
     */
    private int calculateUrgencyScore(int upvotes, Criticality criticality, String deptSlug, LocalDateTime deadline) {
        int score = upvotes * 2; // Upvotes are a baseline metric

        // Criticality weighting
        switch (criticality) {
            case CRITICAL: score += 50; break;
            case HIGH:     score += 30; break;
            case MEDIUM:   score += 10; break;
            case LOW:      score += 0;  break;
        }

        // Department-specific multipliers (e.g., water leaks are immediately destructive)
        if ("water".equals(deptSlug) || "electricity".equals(deptSlug)) {
            score += 15;
        }

        // Deadline proximity (if deadline is within 48 hours, boost score)
        if (deadline != null && deadline.isBefore(LocalDateTime.now().plusDays(2))) {
            score += 20;
        }

        return score;
    }
}
