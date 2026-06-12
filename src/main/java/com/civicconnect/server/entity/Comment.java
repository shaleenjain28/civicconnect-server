package com.civicconnect.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Comment — JPA Entity
 *
 * Maps to the "comments" table in PostgreSQL.
 * Comments can be left by citizens, authority (municipal officer), or supervisor.
 * The authorType field tells the UI which style to render (e.g. authority badge).
 *
 * ── authorType as String (not enum) ──────────────────────────────────────────
 * The Prisma schema stores this as a plain String: "citizen | authority | supervisor"
 * We keep it as a String here because the set of author types is stable and small,
 * and creating a 4th enum+converter for 3 values would be over-engineering.
 * If this grows (e.g. adding "ngo"), just add it to the valid values in the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The issue this comment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    /** The user who wrote this comment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The comment text content.
     * TEXT type — comments can be lengthy updates/explanations.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Role context of the comment author at time of posting.
     * Values: "citizen", "authority", "supervisor"
     * Stored as a plain String — not an enum (see class-level Javadoc above).
     */
    @Column(name = "author_type", nullable = false)
    private String authorType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment that = (Comment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
