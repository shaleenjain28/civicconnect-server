package com.civicconnect.server.controller;

import com.civicconnect.server.dto.request.HodUpdateRequest;
import com.civicconnect.server.dto.response.DepartmentResponse;
import com.civicconnect.server.dto.response.HodResponse;
import com.civicconnect.server.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DepartmentController — HTTP Layer
 *
 * ── What belongs in a Controller? ────────────────────────────────────────────
 * The Controller is the entry point for HTTP requests. It does THREE things only:
 *   1. Receive the HTTP request and extract parameters/body
 *   2. Call the Service with those parameters
 *   3. Return an HTTP response with the appropriate status code
 *
 * What does NOT belong here:
 *   ❌ Business logic (goes in Service)
 *   ❌ Database queries (goes in Repository)
 *   ❌ Entity objects (never leave the Service)
 *
 * ── @RestController ──────────────────────────────────────────────────────────
 * Combines two annotations:
 *   @Controller  → marks this as a Spring MVC controller (Spring scans for it)
 *   @ResponseBody → automatically serializes return values to JSON
 * Without @ResponseBody, you'd have to manually write JSON in every method.
 *
 * ── @RequestMapping("/api/departments") ──────────────────────────────────────
 * Sets the base URL path for ALL methods in this class.
 * Every @GetMapping, @PatchMapping etc. inside is relative to this base path.
 * So @GetMapping("/{id}") maps to GET /api/departments/{id}
 *
 * ── ResponseEntity<T> ────────────────────────────────────────────────────────
 * A wrapper that lets you control both the body AND the HTTP status code.
 *   ResponseEntity.ok(body)          → 200 OK  + body
 *   ResponseEntity.noContent().build()→ 204 No Content (no body)
 *   ResponseEntity.created(uri).body() → 201 Created + body
 * Without ResponseEntity, Spring defaults to 200 OK — which is wrong for
 * some operations (e.g. DELETE should return 204, POST should return 201).
 *
 * ── Dependency Injection ──────────────────────────────────────────────────────
 * DepartmentService is injected via constructor (same reasoning as in Service).
 * Controller depends on Service. Controller NEVER depends on Repository directly.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/departments
    // Public — no auth required
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all departments with issue statistics.
     *
     * @GetMapping — handles HTTP GET requests at the base path "/api/departments"
     *
     * ResponseEntity<List<DepartmentResponse>> — the response body is a JSON array
     * of department objects. Spring serializes this automatically using Jackson.
     *
     * No @RequestParam, no @PathVariable needed — this returns everything.
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
        // HTTP 200 OK + JSON array body
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/departments/{id}
    // Public — no auth required
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a single department by its numeric ID.
     *
     * @PathVariable — binds the {id} part of the URL to the method parameter.
     *   Request: GET /api/departments/3
     *   Spring extracts "3" from the URL and passes it as Integer id = 3
     *
     * Spring automatically converts the String "3" from the URL to Integer.
     * If someone sends GET /api/departments/abc, Spring returns 400 Bad Request
     * before your method is even called (type mismatch).
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Integer id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/departments/{id}/hod
    // Public — citizens see this after reporting an issue
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns HOD contact info for a department.
     *
     * Note: even though the path is /{id}/hod, the {id} still maps
     * to @PathVariable Integer id — Spring matches by parameter name.
     *
     * Returns HodResponse (not DepartmentResponse) — a focused 6-field DTO.
     * This is why we created a separate DTO — the API contract is explicit.
     */
    @GetMapping("/{id}/hod")
    public ResponseEntity<HodResponse> getHodInfo(@PathVariable Integer id) {
        HodResponse response = departmentService.getHodInfo(id);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/departments/{id}/hod
    // Protected — supervisor role required (enforced by Spring Security later)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates HOD contact information for a department.
     *
     * @PatchMapping — handles HTTP PATCH requests (partial update, not full replace).
     *   PATCH vs PUT:
     *   PUT   → replace the ENTIRE resource (all fields required)
     *   PATCH → update PARTIAL fields (only send what's changing)
     *   We use PATCH because the supervisor may only update the email,
     *   not all four HOD fields.
     *
     * @RequestBody — tells Spring to deserialize the JSON request body
     *   into a HodUpdateRequest object.
     *   Spring reads the Content-Type header (must be "application/json")
     *   and uses Jackson to map JSON keys → Java fields via setter methods.
     *
     * @Valid — triggers Jakarta Bean Validation on the HodUpdateRequest.
     *   Before your service is called, Spring validates all annotations:
     *   @Email, @Size, etc. If validation fails → 400 Bad Request automatically.
     *   ALWAYS use @Valid on @RequestBody parameters to protect your service layer.
     *
     * TODO: Add @PreAuthorize("hasRole('SUPERVISOR')") once Spring Security is configured.
     * For now, role check will be added when the security layer is built.
     */
    @PatchMapping("/{id}/hod")
    public ResponseEntity<DepartmentResponse> updateHod(
            @PathVariable Integer id,
            @Valid @RequestBody HodUpdateRequest request) {
        DepartmentResponse response = departmentService.updateHod(id, request);
        return ResponseEntity.ok(response);
    }
}
