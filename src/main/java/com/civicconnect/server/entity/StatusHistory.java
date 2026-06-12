package com.civicconnect.server.entity;

import com.civicconnect.server.entity.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * StatusHistory — JPA Entity
 *
 * Maps to the "status_history" table in PostgreSQL.
 * Records every status transition of an Issue — the accountability audit trail.
 *
 * WHY this table exists (system design reasoning):
 * Without StatusHistory, you can only see WHERE an issue is now (current status).
 * With it, you can see the full journey:
 *   PENDING → IN_PROGRESS (assigned by supervisor on day 1)
 *   IN_PROGRESS → PENDING_VERIFICATION (officer uploaded photo on day 5)
 *   PENDING_VERIFICATION → RESOLVED (citizen verified on day 6)
 *
 * This is a standard audit log pattern used in any system where
 * state transitions need to be traceable for accountability.
 *
 * ── IssueStatus enum fields ───────────────────────────────────────────────────
 * oldStatus and newStatus use our IssueStatus enum.
 * IssueStatusConverter (autoApply = true) handles the DB ↔ Java mapping.
 * The DB stores "in_progress", Java holds IssueStatus.IN_PROGRESS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The issue whose status changed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    /**
     * The user who triggered this status change.
     * Could be: citizen (reporting), officer (updating), supervisor (escalating).
     * FK column is "changed_by" in the DB (matches Prisma @map("changed_by")).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    /**
     * The status BEFORE this transition.
     * IssueStatusConverter (autoApply=true) maps to/from DB lowercase string.
     */
    @Column(name = "old_status", nullable = false)
    private IssueStatus oldStatus;

    /**
     * The status AFTER this transition.
     */
    @Column(name = "new_status", nullable = false)
    private IssueStatus newStatus;

    /**
     * Optional note explaining WHY the status changed.
     * e.g. "Escalated due to missed deadline" or "Verified resolution is complete"
     */
    private String note;

    /**
     * Exact timestamp of the status change.
     * Auto-set by Hibernate on INSERT, never changed.
     */
    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusHistory)) return false;
        StatusHistory that = (StatusHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
