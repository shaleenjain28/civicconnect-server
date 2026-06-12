package com.civicconnect.server.entity;

import com.civicconnect.server.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User — JPA Entity
 *
 * Maps to the "users" table in PostgreSQL.
 * Represents a platform user whose identity is managed by Supabase Auth.
 *
 * ── KEY DESIGN DECISION: Supabase owns the identity ──────────────────────────
 * Users authenticate via Supabase (Google OAuth, email/password etc.).
 * Supabase generates a UUID for each user in its own auth.users table.
 * Our "users" table is a PROFILE table — it stores app-specific data:
 * display name, role, department assignment, preferences.
 *
 * This means:
 *   1. We do NOT store passwords — Supabase handles that entirely.
 *   2. The id is a UUID String assigned by Supabase — NOT auto-incremented.
 *   3. There is NO @GeneratedValue — we receive the UUID from the JWT token
 *      and use it as-is.
 *
 * ── Why NOT @Data? ────────────────────────────────────────────────────────────
 * User has a @ManyToOne Department relationship (lazy-loaded).
 * @Data generates toString() and hashCode() using ALL fields.
 * Calling toString() would trigger a SQL query to load the Department.
 * Use @Getter + @Setter + manual equals/hashCode on id only.
 *
 * ── Why NO List<Issue>, List<Vote> etc.? ──────────────────────────────────────
 * Unidirectional design — Issue holds the FK to User (@ManyToOne User).
 * User does NOT hold the collection. Keeping the collection here would:
 *   - Load hundreds/thousands of rows silently on every User fetch
 *   - Violate SRP — User entity shouldn't carry issue/vote/comment baggage
 * When you need "issues by user X", query IssueRepository explicitly.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    /**
     * Primary key — UUID String from Supabase Auth.
     *
     * @Id marks this as the primary key.
     *
     * WHY NO @GeneratedValue?
     * Supabase generates the UUID when the user registers.
     * We receive this UUID via the JWT token in every authenticated request.
     * We store it as-is. Our DB does NOT generate user IDs.
     *
     * This is different from Department where PostgreSQL generates the id
     * via SERIAL (GenerationType.IDENTITY).
     *
     * If we added @GeneratedValue(IDENTITY), Hibernate would try to
     * auto-increment an integer — which would fail because the column
     * is a VARCHAR/UUID type, not a SERIAL.
     */
    @Id
    private String id;

    /**
     * User's full display name — e.g. "Shaleen Jain"
     * Required. Comes from Supabase user metadata or registration form.
     */
    @Column(nullable = false)
    private String name;

    /**
     * User's email address — unique across the platform.
     * Sourced from Supabase Auth — the email the user registered with.
     * Required, unique.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Optional phone number — unique when provided.
     * Used for SMS notifications (future feature).
     * Nullable — not all users provide phone numbers.
     * No @Column needed — nullable is true by default.
     */
    @Column(unique = true)
    private String phone;

    /**
     * URL to the user's profile avatar image.
     * Sourced from Google/GitHub OAuth profile photo, or manually uploaded.
     * Optional — users without avatars get a generated initial avatar in the UI.
     *
     * @Column(name = "avatar_url") — maps Java "avatarUrl" → DB "avatar_url"
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * User's role in the platform — controls what actions they can perform.
     *
     * Uses our Role enum from entity/enums/Role.java.
     * RoleConverter (@Converter autoApply=true) handles DB ↔ Java mapping:
     *   DB stores "citizen" (lowercase) ↔ Java holds Role.CITIZEN
     *
     * NO @Enumerated annotation needed — the converter handles everything.
     * Adding @Enumerated(EnumType.STRING) would OVERRIDE our converter
     * and store "CITIZEN" (uppercase), breaking the existing data.
     */
    @Column(nullable = false)
    private Role role;

    /**
     * User's preferred UI language.
     * Supported values: "en" (English), "hi" (Hindi), "gu" (Gujarati)
     * Defaults to "en" — default is set at DB level via Flyway migration.
     */
    @Column(nullable = false)
    private String language;

    /**
     * Optional FK to the department this user works in.
     * Only relevant for Role.MUNICIPAL users.
     * Citizens (Role.CITIZEN) and NGO users have null here.
     *
     * @ManyToOne: many users can belong to one department.
     * @JoinColumn(name = "department_id"): the FK column in the users table.
     * FetchType.LAZY: don't load Department unless explicitly accessed.
     *   Without LAZY, every User fetch would join the departments table.
     *
     * This is the OWNING SIDE of the relationship — users table holds the FK.
     * Department entity does NOT have a List<User> (unidirectional).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Timestamp set automatically when the row is first created.
     *
     * @CreationTimestamp — Hibernate sets this to the current timestamp
     * on the first INSERT. It is never updated after that.
     *
     * @Column(updatable = false) — prevents Hibernate from changing this
     * field on any UPDATE statement. It is write-once.
     *
     * LocalDateTime vs Instant:
     *   Instant  → UTC timestamp (no timezone info, just a point in time)
     *   LocalDateTime → date+time without timezone (ambiguous across timezones)
     *   For a civic app serving one country, LocalDateTime is fine.
     *   For global apps, use Instant or ZonedDateTime.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // ── equals() and hashCode() ───────────────────────────────────────────────
    // Same pattern as Department — id-based equality only.
    // The id is a String UUID — never null for a persisted user,
    // but could be null for a new User object before persistence (rare case here
    // since we receive the id from Supabase rather than generating it).

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
