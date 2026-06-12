package com.civicconnect.server.dto.response;

import com.civicconnect.server.entity.Department;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DepartmentResponse — Output DTO for department data
 *
 * ── What is a DTO? ───────────────────────────────────────────────────────────
 * DTO = Data Transfer Object.
 * It shapes the data that leaves your API in the HTTP response.
 *
 * WHY NOT return the Department entity directly from the controller?
 *   1. Entities mirror DB tables — exposing them leaks internal structure.
 *   2. This response needs COMPUTED fields (pendingCount, resolvedCount)
 *      that don't exist in the DB at all.
 *   3. Decoupling: rename a DB column → only entity changes, API contract stays.
 *
 * RULE: Entities never leave the Service layer. Controllers return DTOs only.
 *
 * ── Why @Data is SAFE on DTOs (unlike entities) ──────────────────────────────
 * @Data generates equals(), hashCode(), toString() using ALL fields.
 * On JPA entities this is dangerous (triggers lazy loading, breaks HashSets).
 * On DTOs it is perfectly fine — no Hibernate, no lazy loading, no proxy.
 * DTOs are plain Java objects (POJOs) with no persistence lifecycle.
 *
 * @Data = @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
 * @NoArgsConstructor is added separately because Jackson (JSON deserializer)
 * needs a no-arg constructor to create the object before setting fields.
 */
@Data
@NoArgsConstructor
public class DepartmentResponse {

    // ── Core department fields (mapped from Department entity) ────────────────

    private Integer id;
    private String name;
    private String slug;
    private String icon;
    private String color;
    private String description;

    // ── HOD fields ────────────────────────────────────────────────────────────
    // Included so the citizen app can show HOD contact after reporting an issue

    private String hodName;
    private String hodEmail;
    private String hodPhone;
    private String hodTitle;

    // ── Issue statistics (COMPUTED — not stored in DB) ────────────────────────
    //
    // These counts are calculated at runtime by querying the issues table.
    // They are NOT fields in the Department entity or the departments DB table.
    // The Service layer computes them via IssueRepository and sets them here.
    //
    // This is a key reason DTOs exist — to carry computed/aggregated data
    // that doesn't map 1:1 to any single database table.

    private long totalIssues;
    private long pendingCount;
    private long inProgressCount;
    private long pendingVerificationCount;
    private long pendingUserVerificationCount;
    private long resolvedCount;
    private long escalatedCount;

    /**
     * Static factory method — maps a Department entity → DepartmentResponse DTO.
     *
     * WHY a static factory method?
     * Because counts cannot be set here — they require separate IssueRepository queries.
     * The Service calls this first, then sets counts individually:
     *
     *   DepartmentResponse response = DepartmentResponse.from(dept);
     *   response.setPendingCount(issueRepo.countByDepartmentIdAndStatus(dept.getId(), PENDING));
     *   response.setResolvedCount(issueRepo.countByDepartmentIdAndStatus(dept.getId(), RESOLVED));
     *   // etc.
     *
     * Entity → DTO mapping ALWAYS happens in the Service layer, never in Controller or Repository.
     */
    public static DepartmentResponse from(Department dept) {
        DepartmentResponse res = new DepartmentResponse();
        res.setId(dept.getId());
        res.setName(dept.getName());
        res.setSlug(dept.getSlug());
        res.setIcon(dept.getIcon());
        res.setColor(dept.getColor());
        res.setDescription(dept.getDescription());
        res.setHodName(dept.getHodName());
        res.setHodEmail(dept.getHodEmail());
        res.setHodPhone(dept.getHodPhone());
        res.setHodTitle(dept.getHodTitle());
        // Counts default to 0 (long default) — Service will set them after
        return res;
    }
}
