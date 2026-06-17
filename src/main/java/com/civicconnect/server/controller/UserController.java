package com.civicconnect.server.controller;

import com.civicconnect.server.dto.request.UserUpdateRequest;
import com.civicconnect.server.dto.response.UserResponse;
import com.civicconnect.server.dto.response.UserStatsResponse;
import com.civicconnect.server.security.CustomUserDetails;
import com.civicconnect.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Auth logic implemented!
     * We use @AuthenticationPrincipal to extract the parsed JWT CustomUserDetails.
     */

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserStatsResponse response = userService.getUserStats(userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
