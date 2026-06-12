package com.civicconnect.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Department — JPA Entity
 *
 * Maps to the "departments" PostgreSQL table.
 * Represents a government department (Roads, Water, Sanitation, etc.)
 * that owns and handles civic issues.
 *
 * ── Why NOT @Data? ──────────────────────────────────────────────────────────
 * @Data generates equals() and hashCode() using ALL fields. For JPA entities
 * this is dangerous:
 *   1. Lazy relationships can be triggered unexpectedly by hashCode/toString.
 *   2. Two entities loaded in different sessions can have inconsistent hashCodes.
 *   3. This breaks HashSet / HashMap behaviour in the persistence context.
 * Solution: use @Getter + @Setter only, write equals/hashCode on id only.
 *
 * ── Why @NoArgsConstructor? ─────────────────────────────────────────────────
 * Hibernate creates entity instances via reflection. It needs a no-arg
 * constructor to instantiate the object before setting each field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "departments")
public class Department {

    /**
     * Primary key — auto-incremented by PostgreSQL.
     * Integer (32-bit) maps exactly to Prisma's "Int" type.
     * GenerationType.IDENTITY = let the DB assign the id on INSERT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Department name — e.g., "Roads & Infrastructure". Unique, required. */
    @Column(nullable = false, unique = true)
    private String name;

    /** URL-safe slug — e.g., "roads". Used in API paths and frontend routing. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Emoji/icon identifier for UI — e.g., "🛣️". Required. */
    @Column(nullable = false)
    private String icon;

    /** Hex color code for UI theming — e.g., "#FF5733". Required. */
    @Column(nullable = false)
    private String color;

    /** Optional description of the department's responsibilities. */
    private String description;

    /**
     * HOD contact fields — all optional.
     * @Column(name = "hod_name") needed because Java camelCase → DB snake_case
     * mismatch: Java "hodName" ≠ DB "hod_name".
     */
    @Column(name = "hod_name")
    private String hodName;

    @Column(name = "hod_email")
    private String hodEmail;

    @Column(name = "hod_phone")
    private String hodPhone;

    /** HOD designation — e.g., "Commissioner", "Chief Engineer". */
    @Column(name = "hod_title")
    private String hodTitle;


    // ── equals() and hashCode() ─────────────────────────────────────────────
    // Two entities are equal if and only if their ids match.
    // hashCode is class-level so a new entity (id=null) and a persisted
    // entity (id=5) can coexist in a HashSet without the hash changing.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;
        Department that = (Department) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
