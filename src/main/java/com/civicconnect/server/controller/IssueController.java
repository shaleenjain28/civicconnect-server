package com.civicconnect.server.controller;

import com.civicconnect.server.dto.request.IssueCreateRequest;
import com.civicconnect.server.dto.response.IssueResponse;
import com.civicconnect.server.security.CustomUserDetails;
import com.civicconnect.server.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable Integer id) {
        IssueResponse response = issueService.getIssue(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody IssueCreateRequest request) {
            
        IssueResponse response = issueService.createIssue(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @GetMapping("/nearby")
    public ResponseEntity<java.util.List<IssueResponse>> getNearbyIssues(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5000") double radius,
            @RequestParam(defaultValue = "50") int limit) {
            
        java.util.List<IssueResponse> response = issueService.getNearbyIssues(lat, lon, radius, limit);
        return ResponseEntity.ok(response);
    }

    // ── Accountability State Machine Endpoints ────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueStatusUpdateRequest request) {
            
        IssueResponse response = issueService.updateStatus(userDetails.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<IssueResponse> submitResolution(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueResolveRequest request) {
            
        IssueResponse response = issueService.submitResolution(userDetails.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<IssueResponse> verifyResolution(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueVerifyRequest request) {
            
        IssueResponse response = issueService.verifyResolution(userDetails.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<IssueResponse> confirmResolution(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueVerifyRequest request) {
            
        IssueResponse response = issueService.confirmResolution(userDetails.getId(), id, request);
        return ResponseEntity.ok(response);
    }
}
