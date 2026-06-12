package com.civicconnect.server.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 2048, message = "Avatar URL must not exceed 2048 characters")
    private String avatarUrl;

    @Size(min = 2, max = 2, message = "Language code must be exactly 2 characters (e.g. en, hi, gu)")
    private String language;
}
