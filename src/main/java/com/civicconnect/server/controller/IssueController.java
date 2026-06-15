package com.civicconnect.server.controller;

import com.civicconnect.server.dto.request.IssueCreateRequest;
import com.civicconnect.server.dto.response.IssueResponse;
import com.civicconnect.server.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody IssueCreateRequest request) {
            
        // We temporarily use X-User-Id until Spring Security JWT is set up
        IssueResponse response = issueService.createIssue(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Accountability State Machine Endpoints ────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueStatusUpdateRequest request) {
            
        IssueResponse response = issueService.updateStatus(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<IssueResponse> submitResolution(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueResolveRequest request) {
            
        IssueResponse response = issueService.submitResolution(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<IssueResponse> verifyResolution(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueVerifyRequest request) {
            
        IssueResponse response = issueService.verifyResolution(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<IssueResponse> confirmResolution(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer id,
            @Valid @RequestBody com.civicconnect.server.dto.request.IssueVerifyRequest request) {
            
        IssueResponse response = issueService.confirmResolution(userId, id, request);
        return ResponseEntity.ok(response);
    }
}
