package com.civicconnect.server.dto.response;

import com.civicconnect.server.entity.User;
import com.civicconnect.server.entity.enums.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String name;
    private String phone;
    private String avatarUrl;
    private Role role;
    private String language;
    private LocalDateTime createdAt;
    
    // Only included if user is MUNICIPAL
    private Integer departmentId;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setLanguage(user.getLanguage());
        response.setCreatedAt(user.getCreatedAt());

        // ── WHY we DON'T call user.getDepartment() here ───────────────────────
        // Department is FetchType.LAZY. If we access it outside an active
        // Hibernate session (after the @Transactional method returns),
        // it throws LazyInitializationException.
        // Instead, the Service passes the departmentId directly if it needs it.
        // This from() method is the safe baseline — no lazy access.
        
        return response;
    }

    /**
     * Alternative factory used when the service has already accessed
     * the department WITHIN the transaction boundary.
     */
    public static UserResponse from(User user, Integer departmentId) {
        UserResponse response = from(user);
        response.setDepartmentId(departmentId);
        return response;
    }
}
