package com.civicconnect.server.controller;

import com.civicconnect.server.dto.request.UserUpdateRequest;
import com.civicconnect.server.dto.response.UserResponse;
import com.civicconnect.server.dto.response.UserStatsResponse;
import com.civicconnect.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * MOCK AUTHENTICATION:
     * Until Spring Security is configured with JWT, we read the user ID
     * from a custom header "X-User-Id". In production, this will be replaced
     * by @AuthenticationPrincipal or SecurityContextHolder.
     */

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") String userId) {
        UserResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@RequestHeader("X-User-Id") String userId) {
        UserStatsResponse response = userService.getUserStats(userId);
        return ResponseEntity.ok(response);
    }
}
