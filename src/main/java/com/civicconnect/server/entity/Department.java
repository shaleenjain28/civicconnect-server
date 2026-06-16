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
 * Lombok's @Data generates equals() and hashCode() using ALL fields.
 * For JPA entities, this is dangerous because:
 * 1. Hibernate loads entities lazily — accessing a field to compute hashCode
 * can trigger unexpected database queries.
 * 2. Two Department objects with the same DB row but loaded in different
 * sessions can have different hashCodes if any transient field differs.
 * 3. This breaks HashSet / HashMap behaviour in the persistence context.
 * Solution: use @Getter + @Setter only, and write equals/hashCode manually
 * using ONLY the primary key (id). Two entities with the same id = same row.
 *
 * ── Why @NoArgsConstructor? ─────────────────────────────────────────────────
 * Hibernate creates entity instances via reflection when reading from DB.
 * It needs a no-argument constructor to instantiate the object before
 * setting each field. Without it, Hibernate throws an InstantiationException.
 *
 * ── Why @AllArgsConstructor? ────────────────────────────────────────────────
 * Convenience for creating Department objects in tests and service layer.
 * e.g. new Department(null, "Roads", "roads", "🛣️", "#FF5733", ...)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
// @Table maps this class to the "departments" table in PostgreSQL.
// Without this, Hibernate would look for a table called "Department" (class
// name).
// Prisma equivalent: @@map("departments")
@Table(name = "departments")
public class Department {
    /**
     * Primary key — auto-incremented by PostgreSQL.
     *
     * @Id marks this as the primary key column.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY):
     *                          Tells Hibernate to let the DATABASE generate the id
     *                          on INSERT.
     *                          PostgreSQL uses its native SERIAL / GENERATED ALWAYS
     *                          AS IDENTITY mechanism.
     *                          The DB assigns the id AFTER the row is inserted,
     *                          then Hibernate reads it back.
     *
     *                          Why IDENTITY and not SEQUENCE?
     *                          - SEQUENCE requires a separate DB sequence object +
     *                          an extra SELECT nextval()
     *                          before every INSERT (2 queries per insert).
     *                          - IDENTITY uses the column's built-in auto-increment
     *                          (1 query per insert).
     *                          - Tradeoff: IDENTITY cannot batch inserts (Hibernate
     *                          needs the id back immediately).
     *                          For CivicConnect, this is acceptable — we never
     *                          bulk-insert departments.
     *
     *                          Integer vs Long:
     *                          Prisma uses "Int" (32-bit). Java Integer maps
     *                          exactly to that.
     *                          Long (64-bit) is also fine for future-proofing, but
     *                          Integer is precise here.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * Department name — e.g., "Roads & Infrastructure", "Water Supply"
     *
     * nullable = false → adds NOT NULL constraint in the DB schema.
     * unique = true → adds a UNIQUE index in the DB schema.
     *
     * No @Column(name = "name") needed — Hibernate maps the Java field "name"
     * to the DB column "name" automatically when names match exactly.
     * Prisma equivalent: name String @unique
     */
    @Column(nullable = false, unique = true)
    private String name;
    /**
     * URL-safe identifier — e.g., "roads", "water-supply"
     * Used in API paths and frontend routing.
     *
     * unique = true ensures no two departments share the same slug.
     * Prisma equivalent: slug String @unique
     */
    @Column(nullable = false, unique = true)
    private String slug;
    /**
     * Emoji or icon identifier for UI display — e.g., "🛣️"
     * Required field — every department must have a visual identifier.
     * Prisma equivalent: icon String
     */
    @Column(nullable = false)
    private String icon;
    /**
     * Hex color code for UI theming — e.g., "#FF5733"
     * Used to color-code issues by department on the map and dashboard.
     * Prisma equivalent: color String
     */
    @Column(nullable = false)
    private String color;
    /**
     * Optional human-readable description of the department's responsibilities.
     * nullable = true by default — no @Column annotation needed for optional
     * fields.
     * Prisma equivalent: description String?
     */
    private String description;
    /**
     * HOD = Head of Department
     *
     * The next four fields store contact info for the department's senior official.
     * These are ALL optional (the HOD may not be set when a department is created).
     *
     * WHY @Column(name = "hod_name") HERE but NOT for "name", "slug" etc.?
     * Hibernate's default column naming maps Java camelCase → DB camelCase (exact
     * match).
     * So Java field "name" → DB column "name" ✅ (no annotation needed)
     * Java field "hodName" → DB column "hodName" ❌ (DB has "hod_name")
     * We use @Column(name = "...") ONLY when the Java name differs from the DB
     * column name.
     * Prisma equivalent: hodName String? @map("hod_name")
     */
    @Column(name = "hod_name")
    private String hodName;
    /**
     * HOD email — used to send escalation notifications. Prisma:
     * hodEmail @map("hod_email")
     */
    @Column(name = "hod_email")
    private String hodEmail;
    /** HOD phone number. Prisma: hodPhone @map("hod_phone") */
    @Column(name = "hod_phone")
    private String hodPhone;
    /**
     * HOD title/designation — e.g., "Commissioner", "Chief Engineer"
     * Displayed on the dashboard's departments page.
     * Prisma: hodTitle @map("hod_title")
     */
    @Column(name = "hod_title")
    private String hodTitle;

    // ── equals() and hashCode() ─────────────────────────────────────────────
    //
    // WHY WE WRITE THESE MANUALLY (not using @Data or @EqualsAndHashCode):
    //
    // Rule: Two JPA entities are EQUAL if and only if they have the SAME primary
    // key.
    // A Department with id=5 loaded from session A == Department with id=5 from
    // session B.
    // We don't compare name, slug, or any other field — the id is the source of
    // truth.
    //
    // hashCode() returns the same value for ALL Department instances
    // (getClass().hashCode()).
    // This seems wrong but is actually CORRECT for JPA entities because:
    // - A new entity (id=null) and a persisted entity (id=5) must be usable in
    // the same HashSet without the hash changing when the id is assigned on save.
    // - Consistent hashCode + id-based equals is the standard JPA pattern.
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // same object reference
        if (!(o instanceof Department))
            return false; // type check
        Department that = (Department) o;
        return id != null && id.equals(that.id); // null-safe id comparison
    }

    @Override
    public int hashCode() {
        // All Department instances share the same hashCode (class-level).
        // This ensures a Department's position in a HashSet doesn't change
        // when Hibernate assigns its id after the first save.
        return getClass().hashCode();
    }
}