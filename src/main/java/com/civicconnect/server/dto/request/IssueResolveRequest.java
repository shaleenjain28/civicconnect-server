package com.civicconnect.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueResolveRequest {

    @NotBlank(message = "Resolution photo is required for verification")
    private String resolutionPhoto;

    private String resolutionNote;
}
