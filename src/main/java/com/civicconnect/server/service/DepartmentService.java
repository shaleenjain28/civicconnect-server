package com.civicconnect.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.civicconnect.server.dto.request.HodUpdateRequest;
import com.civicconnect.server.dto.response.DepartmentResponse;
import com.civicconnect.server.dto.response.HodResponse;
import com.civicconnect.server.entity.Department;
import com.civicconnect.server.repository.DepartmentRepository;

/**
 * DepartmentService — Business Logic Layer
 *
 * ── What belongs in a Service? ───────────────────────────────────────────────
 * ALL business logic lives here. The Service is the brain of the application.
 * Rules:
 * ✅ Orchestrate repository calls
 * ✅ Map Entities → DTOs (and DTOs → Entities)
 * ✅ Apply business rules (who can do what)
 * ✅ Throw domain-specific exceptions
 * ❌ Never handle HTTP (no Request/Response objects here)
 * ❌ Never write SQL or call the DB directly
 * ❌ Never return Entity objects — always return DTOs
 *
 * ── Dependency Injection via Constructor ─────────────────────────────────────
 * We inject DepartmentRepository through the constructor (not @Autowired on
 * field).
 * WHY constructor injection?
 * 1. Makes dependencies explicit and visible.
 * 2. The class is immutable — fields are final.
 * 3. Easier to test — you can pass a mock repository in unit tests without
 * needing Spring context at all.
 * 4. Spring automatically detects and uses constructor injection when there
 * is only one constructor (no @Autowired annotation needed).
 *
 * ── @Service ─────────────────────────────────────────────────────────────────
 * Marks this as a Spring bean (stereotype annotation).
 * Spring creates one instance of this class at startup and injects it
 * wherever it's needed (e.g. into DepartmentController's constructor).
 *
 * ── @Transactional ───────────────────────────────────────────────────────────
 * Methods marked @Transactional run inside a database transaction.
 * If any exception is thrown, the entire transaction is rolled back.
 * readOnly = true on GET methods:
 * - Tells Hibernate: "don't track changes to entities fetched here"
 * - Hibernate skips dirty checking → better performance on reads
 * - Some DB drivers route read-only transactions to read replicas
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Constructor injection — Spring automatically wires DepartmentRepository here
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL DEPARTMENTS
    // Maps to: GET /api/departments
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all departments with their issue statistics.
     *
     * ── The N+1 Problem (current approach vs optimized) ──────────────────────
     * NAIVE approach (what the Express backend does — 31 queries for 5
     * departments):
     * - 1 query: SELECT all departments
     * - For each department (5): 6 separate COUNT queries = 30 queries
     * - Total: 31 queries per page load ← THIS IS BAD
     *
     * CURRENT approach here (2 queries — acceptable for now):
     * - 1 query: SELECT all departments
     * - 1 query per department for total count (via IssueRepository — TODO)
     * - We'll optimize to 2 total when IssueRepository is built using
     * GROUP BY conditional aggregation (see IssueRepository TODO below)
     *
     * ── Java Streams ─────────────────────────────────────────────────────────
     * departments.stream() → creates a Stream from the list
     * .map(this::toResponse) → transforms each Department → DepartmentResponse
     * .collect(Collectors.toList()) → collects results back into a List
     * 
     * [Dept1, Dept2, Dept3] ← List<Department> from DB
     * ↓ .stream()
     * conveyor belt starts
     * ↓ .map(this::toResponse)
     * toResponse(Dept1) → Resp1
     * toResponse(Dept2) → Resp2
     * toResponse(Dept3) → Resp3
     * ↓ .collect(Collectors.toList())
     * [Resp1, Resp2, Resp3] ← List<DepartmentResponse> returned
     *
     * 
     * This is the modern Java way to transform collections without for-loops.
     *
     * @Transactional(readOnly = true) → read-only transaction, no dirty checking
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream().map(dept -> this.toResponse(dept)).collect(Collectors.toList());

        // TODO (Phase 3 optimization): Replace individual count queries with a single
        // GROUP BY query from IssueRepository to reduce N+1 to exactly 2 queries total:
        // SELECT department_id, status, COUNT(*) FROM issues GROUP BY department_id,
        // status
        // Then map results in Java instead of firing a query per department.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET SINGLE DEPARTMENT BY ID
    // Maps to: GET /api/departments/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a single department by its primary key.
     *
     * findById() returns Optional<Department> — we must handle the "not found"
     * case.
     * .orElseThrow() unwraps the Optional:
     * - If present → returns the Department
     * - If empty → throws RuntimeException → GlobalExceptionHandler catches it →
     * 404
     *
     * We'll replace RuntimeException with a custom NotFoundException
     * once we build the exception layer.
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return toResponse(dept);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET SINGLE DEPARTMENT BY SLUG
    // Used by frontend when filtering issues by department slug
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentBySlug(String slug) {
        Department dept = departmentRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Department not found with slug: " + slug));
        return toResponse(dept);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET HOD INFO
    // Maps to: GET /api/departments/{id}/hod
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns only the HOD contact info for a department.
     * Used by the citizen app to show who is responsible for their issue.
     *
     * Returns HodResponse (not DepartmentResponse) — a focused DTO
     * with only the 6 fields this endpoint exists to provide.
     * The null fallback ("Not assigned") is handled inside HodResponse.from().
     */
    @Transactional(readOnly = true)
    public HodResponse getHodInfo(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return HodResponse.from(dept);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE HOD INFO
    // Maps to: PATCH /api/departments/{id}/hod (supervisor only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the Head of Department contact info for a department.
     *
     * ── Partial Update Pattern ────────────────────────────────────────────────
     * Only update a field if the request provided a non-null value.
     * If the client sends { "hodEmail": "new@email.com" } only,
     * hodName/hodPhone/hodTitle remain unchanged.
     *
     * This matches the Express "hodName || undefined" behaviour.
     *
     * ── Why @Transactional (without readOnly) here? ───────────────────────────
     * This method writes to the DB — we need a writable transaction.
     * If save() throws an exception, the entire transaction rolls back.
     * The department row either gets fully updated or not at all.
     *
     * Note: Role-based authorization (supervisor only) will be enforced
     * in the Controller via Spring Security — not here in the Service.
     * The Service assumes the caller is already authorized.
     */
    @Transactional
    public DepartmentResponse updateHod(Integer id, HodUpdateRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Only update fields that were explicitly provided (non-null)
        // null means "don't change this field" — not "set this field to null"
        if (request.getHodName() != null)
            dept.setHodName(request.getHodName());
        if (request.getHodEmail() != null)
            dept.setHodEmail(request.getHodEmail());
        if (request.getHodPhone() != null)
            dept.setHodPhone(request.getHodPhone());
        if (request.getHodTitle() != null)
            dept.setHodTitle(request.getHodTitle());

        // save() → Hibernate detects the entity is managed (already in DB),
        // generates UPDATE SQL with only the changed fields, executes it.
        Department saved = departmentRepository.save(dept);
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPER — Entity → DTO mapping
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps a Department entity to a DepartmentResponse DTO.
     *
     * WHY private? This is an internal implementation detail of the service.
     * No other class needs to call this directly.
     *
     * WHY in the Service and not in the DTO or Entity?
     * - Entity should not know about DTOs (it's a DB concern)
     * - DTO should not know about business rules (it's a transport concern)
     * - Service is the right place to bridge the two worlds
     *
     * The DepartmentResponse.from() static method handles the core field mapping.
     * Issue counts are set to 0 here for now — to be filled in Phase 3
     * optimization.
     */
    private DepartmentResponse toResponse(Department dept) {
        return DepartmentResponse.from(dept);
        // TODO: set issue counts from IssueRepository once it's built
    }
}
