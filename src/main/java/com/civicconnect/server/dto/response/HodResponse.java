package com.civicconnect.server.dto.response;

import com.civicconnect.server.entity.Department;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HodResponse — Output DTO for GET /api/departments/{id}/hod
 *
 * ── Why a separate DTO and not reuse DepartmentResponse? ─────────────────────
 * The HOD endpoint exists for ONE purpose: show citizens the contact info
 * of the department official responsible for their issue.
 * It needs only 6 fields. DepartmentResponse has 15+ fields.
 *
 * Sending all 15 fields when only 6 are needed:
 *   1. Wastes bandwidth (matters on mobile connections)
 *   2. Confuses API consumers — which fields are relevant here?
 *   3. Leaks data — sending issue counts in a HOD contact response is meaningless
 *
 * A focused DTO = a clear API contract.
 * The frontend knows exactly what this endpoint returns and nothing else.
 *
 * ── API Design Principle: Principle of Least Information ─────────────────────
 * Return only what the consumer needs for THAT specific use case.
 * This also makes future changes safer — you can change DepartmentResponse
 * without worrying about breaking the HOD endpoint, and vice versa.
 */
@Data
@NoArgsConstructor
public class HodResponse {

    // Department context — so the citizen knows which department this HOD belongs to
    private Integer departmentId;
    private String departmentName;
    private String departmentIcon;

    // HOD contact details — the actual data this endpoint exists to provide
    // All are nullable because HOD info may not yet be assigned to a department
    private String hodName;
    private String hodEmail;
    private String hodPhone;
    private String hodTitle;

    /**
     * Static factory method — maps a Department entity → HodResponse DTO.
     *
     * The null-safe fallback "Not assigned" matches the Express behaviour:
     *   hodName: dept.hodName || 'Not assigned'
     *
     * We handle the null here in the mapping layer (DTO factory),
     * keeping the Service clean and readable.
     */
    public static HodResponse from(Department dept) {
        HodResponse response = new HodResponse();
        response.setDepartmentId(dept.getId());
        response.setDepartmentName(dept.getName());
        response.setDepartmentIcon(dept.getIcon());

        // Fallback to "Not assigned" if HOD info has not been set yet
        response.setHodName(dept.getHodName() != null ? dept.getHodName() : "Not assigned");
        response.setHodEmail(dept.getHodEmail());
        response.setHodPhone(dept.getHodPhone());
        response.setHodTitle(dept.getHodTitle());
        return response;
    }
}
