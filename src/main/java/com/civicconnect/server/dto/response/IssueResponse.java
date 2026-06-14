package com.civicconnect.server.dto.response;

import com.civicconnect.server.entity.Issue;
import com.civicconnect.server.entity.User;
import com.civicconnect.server.entity.enums.Criticality;
import com.civicconnect.server.entity.enums.IssueScope;
import com.civicconnect.server.entity.enums.IssueStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IssueResponse — Output DTO for Issue data
 *
 * Notice how we flatten the relationships to prevent deep nesting and
 * circular references. Instead of returning the full User or Department
 * objects, we only return the specific fields the frontend needs.
 */
@Data
@NoArgsConstructor
public class IssueResponse {

    private Integer id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssueScope scope;
    private Criticality criticality;
    
    private double latitude;
    private double longitude;
    private String locationText;
    private String imageUrl;
    
    private int upvoteCount;
    private int urgencyScore;
    
    private LocalDateTime deadline;
    private boolean escalated;
    
    // Resolution fields
    private String resolutionPhoto;
    private String resolutionNote;
    private LocalDateTime verifiedAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // ── Flattened Relationships ───────────────────────────────────────────────
    
    // Department info
    private Integer departmentId;
    private String departmentName;
    private String departmentColor; // Useful for UI tags
    
    // User info (Reporter)
    private String reporterId;
    private String reporterName;
    
    // User info (Assignee - nullable)
    private String assignedToId;
    private String assignedToName;
    
    // User info (Verifier - nullable)
    private String verifiedById;
    private String verifiedByName;

    /**
     * Static factory method to map an Issue entity to an IssueResponse.
     *
     * IMPORTANT: This method assumes that the relationships (user, department,
     * assignedTo, verifiedBy) have been fetched safely within the transaction.
     * We will use EntityGraphs or JOIN FETCH in the repository to ensure this
     * doesn't trigger N+1 queries.
     */
    public static IssueResponse from(Issue issue) {
        IssueResponse res = new IssueResponse();
        
        res.setId(issue.getId());
        res.setTitle(issue.getTitle());
        res.setDescription(issue.getDescription());
        res.setStatus(issue.getStatus());
        res.setScope(issue.getScope());
        res.setCriticality(issue.getCriticality());
        res.setLatitude(issue.getLatitude());
        res.setLongitude(issue.getLongitude());
        res.setLocationText(issue.getLocationText());
        res.setImageUrl(issue.getImageUrl());
        res.setUpvoteCount(issue.getUpvoteCount());
        res.setUrgencyScore(issue.getUrgencyScore());
        res.setDeadline(issue.getDeadline());
        res.setEscalated(issue.isEscalated());
        res.setResolutionPhoto(issue.getResolutionPhoto());
        res.setResolutionNote(issue.getResolutionNote());
        res.setVerifiedAt(issue.getVerifiedAt());
        res.setCreatedAt(issue.getCreatedAt());
        res.setResolvedAt(issue.getResolvedAt());

        // Map Department (Always present)
        if (issue.getDepartment() != null) {
            res.setDepartmentId(issue.getDepartment().getId());
            res.setDepartmentName(issue.getDepartment().getName());
            res.setDepartmentColor(issue.getDepartment().getColor());
        }

        // Map Reporter (Always present)
        if (issue.getUser() != null) {
            res.setReporterId(issue.getUser().getId());
            res.setReporterName(issue.getUser().getName());
        }

        // Map Assignee (Optional)
        if (issue.getAssignedTo() != null) {
            res.setAssignedToId(issue.getAssignedTo().getId());
            res.setAssignedToName(issue.getAssignedTo().getName());
        }

        // Map Verifier (Optional)
        if (issue.getVerifiedBy() != null) {
            res.setVerifiedById(issue.getVerifiedBy().getId());
            res.setVerifiedByName(issue.getVerifiedBy().getName());
        }

        return res;
    }
}
