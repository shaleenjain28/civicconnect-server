package com.civicconnect.server.repository;

import com.civicconnect.server.entity.Issue;
import com.civicconnect.server.entity.enums.IssueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * IssueRepository — Data Access Layer for Issues
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {

    // ── Pre-fetch relationships to prevent N+1 queries in IssueResponse.from() ──
    @EntityGraph(attributePaths = {"department", "user", "assignedTo", "verifiedBy"})
    Optional<Issue> findById(Integer id);

    // ── The answer to my question ─────────────────────────────────────────────
    // Spring Data JPA reads "countBy", then looks at the fields "UserId" and "Status".
    // It automatically generates: SELECT COUNT(*) FROM issues WHERE user_id = ? AND status = ?
    long countByUserIdAndStatus(String userId, IssueStatus status);

    // Used for the user's total reports count
    long countByUserId(String userId);

    // ── Used by DepartmentService (to be optimized later) ─────────────────────
    long countByDepartmentIdAndStatus(Integer departmentId, IssueStatus status);

    // ── Used by DepartmentService for GET /api/departments/:id ────────────────
    // The frontend requests the top 50 most urgent issues for a single department.
    // In Express: include: { issues: { take: 50, orderBy: urgencyScore } }
    // In Spring, we translate this to a derived query:
    // findTop50          → LIMIT 50
    // ByDepartmentId     → WHERE department_id = ?
    // OrderByUrgencyScoreDesc → ORDER BY urgency_score DESC
    // ── Phase 3: Bounding Box Query for Geolocation ───────────────────────────
    // We use a bounding box (Between) to pre-filter rows in the DB fast,
    // then we will apply the exact Haversine mathematical filter in Java.
    @EntityGraph(attributePaths = {"department", "user"})
    java.util.List<Issue> findByLatitudeBetweenAndLongitudeBetweenAndStatusNot(
            Double minLat, Double maxLat, 
            Double minLon, Double maxLon, 
            IssueStatus excludedStatus
    );
}
