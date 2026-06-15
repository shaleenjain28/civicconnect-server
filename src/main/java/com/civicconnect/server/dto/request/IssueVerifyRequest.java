package com.civicconnect.server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueVerifyRequest {

    @NotNull(message = "Approved boolean is required")
    private Boolean approved;

    private String note;
}
