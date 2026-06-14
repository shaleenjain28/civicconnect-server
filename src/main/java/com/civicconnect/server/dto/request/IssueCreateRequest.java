package com.civicconnect.server.dto.request;

import com.civicconnect.server.entity.enums.Criticality;
import com.civicconnect.server.entity.enums.IssueScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IssueCreateRequest — DTO for POST /api/issues
 *
 * This acts as our "whitelist" validation layer. We NEVER accept the
 * raw Entity object from the frontend because a malicious user could
 * send {"status": "RESOLVED", "upvoteCount": 9999} and instantly
 * bypass the workflow if we mapped it directly.
 */
@Data
@NoArgsConstructor
public class IssueCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Department ID is required")
    private Integer departmentId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    // Optional fields
    private String locationText;
    private String imageUrl;

    // Defaults handled by the Service if not provided
    private Criticality criticality;
    private IssueScope scope;
}
