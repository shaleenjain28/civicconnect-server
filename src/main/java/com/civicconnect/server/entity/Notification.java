package com.civicconnect.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification — JPA Entity
 *
 * Maps to the "notifications" table in PostgreSQL.
 * Lightweight in-app notifications polled by the frontend.
 *
 * ── Design: polling vs push ───────────────────────────────────────────────────
 * This is a "polling" notification model — the client periodically calls
 * GET /api/notifications to check for unread notifications.
 * A more advanced approach would be WebSockets or Server-Sent Events (Phase 7).
 *
 * ── readAt pattern ────────────────────────────────────────────────────────────
 * Notifications are never deleted — they're marked as read by setting readAt.
 * This preserves the notification history for audit purposes.
 * "Unread" = WHERE read_at IS NULL.
 * "Read" = WHERE read_at IS NOT NULL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The user this notification is for. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The issue this notification relates to. Optional.
     * Some notifications are system-wide and not tied to a specific issue.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    /**
     * Notification type — controls which icon/colour the UI uses.
     * Values: "issue_update", "verification_required", "system"
     * Kept as String (like Comment.authorType) — small stable set of values.
     */
    @Column(nullable = false)
    private String type;

    /** Short heading shown in the notification list. */
    @Column(nullable = false)
    private String title;

    /** Optional body text with more detail. */
    private String body;

    /**
     * Timestamp set when the user reads/dismisses this notification.
     * NULL = unread. NOT NULL = read.
     * The frontend sets this via PATCH /api/notifications/{id}/read.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
