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

    /**
     * POST /api/issues
     * 
     * MOCK AUTHENTICATION:
     * Using "X-User-Id" header temporarily until Spring Security is set up.
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody IssueCreateRequest request) {
            
        IssueResponse response = issueService.createIssue(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
