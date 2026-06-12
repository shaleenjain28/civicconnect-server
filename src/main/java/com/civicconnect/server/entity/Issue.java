package com.civicconnect.server.entity;

import com.civicconnect.server.entity.enums.Criticality;
import com.civicconnect.server.entity.enums.IssueScope;
import com.civicconnect.server.entity.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Issue — JPA Entity
 *
 * Maps to the "issues" table in PostgreSQL.
 * The CORE entity of CivicConnect — every citizen report lives here.
 *
 * ── Three @ManyToOne relationships to User ────────────────────────────────────
 * Issue references User THREE times with different semantic meanings:
 *
 *   1. user        → Who REPORTED the issue (always set, not null)
 *   2. assignedTo  → Which municipal officer is WORKING on it (nullable)
 *   3. verifiedBy  → Which user VERIFIED the resolution (nullable)
 *
 * In Prisma, this is done with named relations:
 *   @relation("ReportedIssues"), @relation("AssignedIssues"), @relation("VerifiedIssues")
 *
 * In JPA, when a single entity references another entity more than once,
 * we must set @JoinColumn(name = "...") explicitly on EACH to tell Hibernate
 * which FK column in the "issues" table belongs to which relationship.
 * Otherwise Hibernate gets confused and generates wrong SQL.
 *
 * ── Why NO List<Vote>, List<Comment>, List<StatusHistory>? ────────────────────
 * Same unidirectional rule as before.
 * Vote, Comment, StatusHistory each hold a FK to Issue.
 * We query them via their own repositories when needed.
 * Collecting them in Issue would load all votes/comments silently.
 *
 * ── Enum fields ────────────────────────────────────────────────────────────────
 * Three enums: IssueStatus, IssueScope, Criticality.
 * All three have @Converter(autoApply = true) on their converters.
 * No @Enumerated annotation needed — converters handle everything.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "issues")
public class Issue {

    /**
     * Primary key — auto-incremented by PostgreSQL (Integer, same as Department).
     * Issues are created by our system, so IDENTITY generation is correct here.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ── Relationships to User ─────────────────────────────────────────────────

    /**
     * The citizen who reported this issue. Always required.
     *
     * @JoinColumn(name = "user_id") — the FK column in the "issues" table.
     * nullable = false — every issue must have a reporter.
     *
     * Why FetchType.LAZY?
     * When listing 50 issues, we don't need to load 50 User objects.
     * The service fetches user info separately only when it's needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The municipal officer assigned to work on this issue. Optional.
     *
     * Same FK column name as Prisma: @map("assigned_to")
     * nullable = true (default) — newly reported issues are unassigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /**
     * The user who verified that the resolution photo/note is legitimate.
     * Part of the accountability flow — citizens or supervisors verify resolutions.
     * Optional — only set when status reaches PENDING_USER_VERIFICATION → RESOLVED.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    /**
     * The government department responsible for resolving this issue.
     * Always required — issues are filed under a specific department.
     *
     * @JoinColumn(name = "department_id") — FK column in "issues" table.
     * This is the OWNING side — Issue holds the FK, Department has no collection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // ── Core Issue Fields ─────────────────────────────────────────────────────

    /** Short human-readable title — e.g. "Pothole near Navrangpura flyover" */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of the problem.
     * @Column(columnDefinition = "TEXT") — uses PostgreSQL TEXT type (unlimited length)
     * instead of VARCHAR(255) which is the default for String.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // ── Enum Fields ───────────────────────────────────────────────────────────

    /**
     * Current status of the issue — drives the accountability state machine.
     * IssueStatusConverter (autoApply = true) stores as lowercase string in DB.
     * e.g. IssueStatus.IN_PROGRESS ↔ DB "in_progress"
     */
    @Column(nullable = false)
    private IssueStatus status;

    /**
     * Geographic scope of the problem.
     * IssueScope: LOCAL, CITY, STATE, COUNTRY
     * IssueScopeConverter (autoApply = true) stores as lowercase.
     */
    @Column(nullable = false)
    private IssueScope scope;

    /**
     * Urgency level of the issue.
     * Criticality: LOW, MEDIUM, HIGH, CRITICAL
     * CriticalityConverter (autoApply = true) stores as lowercase.
     */
    @Column(nullable = false)
    private Criticality criticality;

    // ── Location Fields ───────────────────────────────────────────────────────

    /**
     * GPS latitude of the reported issue.
     * Double precision — required for map pin placement.
     * In Java: primitive double (not Double wrapper) to prevent null.
     */
    @Column(nullable = false)
    private double latitude;

    /**
     * GPS longitude of the reported issue.
     */
    @Column(nullable = false)
    private double longitude;

    /**
     * Human-readable location description — e.g. "Near Navrangpura flyover, Ahmedabad"
     * Optional — derived from reverse geocoding on the frontend.
     */
    @Column(name = "location_text")
    private String locationText;

    // ── Media ─────────────────────────────────────────────────────────────────

    /** URL to the photo uploaded with this issue report. Optional. */
    @Column(name = "image_url")
    private String imageUrl;

    // ── Engagement & Scoring ─────────────────────────────────────────────────

    /**
     * Number of citizens who upvoted this issue.
     * Incremented when a Vote is cast via VoteService.
     * Default 0 set by DB (Flyway migration), not by Java.
     */
    @Column(name = "upvote_count", nullable = false)
    private int upvoteCount;

    /**
     * Computed priority score used to sort issues for municipal officers.
     * Higher score = shown first in the officer's dashboard.
     * Calculated by: upvotes + criticality weight + days overdue.
     * Default 0, recalculated by a scheduled job (Phase 6).
     */
    @Column(name = "urgency_score", nullable = false)
    private int urgencyScore;

    // ── Deadline & Escalation ─────────────────────────────────────────────────

    /**
     * Optional deadline by which the issue should be resolved.
     * Set by supervisor when assigning. Triggers escalation if missed.
     */
    private LocalDateTime deadline;

    /**
     * Whether this issue has been escalated to the supervisor.
     * Set to true automatically when deadline is missed (Phase 6 scheduler).
     * Default false.
     */
    @Column(nullable = false)
    private boolean escalated;

    // ── Resolution Fields ─────────────────────────────────────────────────────

    /**
     * URL to the photo that municipal officer uploads as proof of resolution.
     * Required when status transitions to PENDING_VERIFICATION.
     */
    @Column(name = "resolution_photo")
    private String resolutionPhoto;

    /**
     * Text note explaining how the issue was resolved.
     * e.g. "Pothole filled and road resurfaced on 10 June 2025"
     */
    @Column(name = "resolution_note")
    private String resolutionNote;

    /**
     * Timestamp when the resolution was verified.
     * Set when the issue moves from PENDING_USER_VERIFICATION → RESOLVED.
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // ── Timestamps ────────────────────────────────────────────────────────────

    /**
     * Auto-set by Hibernate on INSERT. Never changes after creation.
     * @Column(updatable = false) — Hibernate excludes this from UPDATE statements.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Set when the issue is resolved and verified.
     * Null until the issue reaches RESOLVED status.
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;


    // ── equals() and hashCode() ───────────────────────────────────────────────
    // Id-based equality. Same pattern as Department and User.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Issue)) return false;
        Issue that = (Issue) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
