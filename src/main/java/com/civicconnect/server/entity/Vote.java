package com.civicconnect.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Vote — JPA Entity
 *
 * Maps to the "votes" table in PostgreSQL.
 * Represents a citizen upvoting an issue to signal importance.
 *
 * ── Unique constraint: one vote per user per issue ────────────────────────────
 * @Table(uniqueConstraints = ...) adds a UNIQUE index at the DB level on
 * (user_id, issue_id) — a user can only vote once on any given issue.
 * If a second vote is attempted, the DB throws a unique constraint violation.
 * We catch that in the service and return a 409 Conflict response.
 *
 * This is the same as Prisma's @@unique([userId, issueId]).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "votes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "issue_id"})
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The user who cast this vote.
     * FetchType.LAZY — we rarely need the full User object when loading votes.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The issue being upvoted.
     * FetchType.LAZY — same reasoning.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vote)) return false;
        Vote that = (Vote) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
