package com.civicconnect.server.service;

import com.civicconnect.server.dto.request.UserUpdateRequest;
import com.civicconnect.server.dto.response.UserResponse;
import com.civicconnect.server.dto.response.UserStatsResponse;
import com.civicconnect.server.entity.User;
import com.civicconnect.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        // Phase 2 Note: IssueRepository will be needed here later for stats.
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Access department INSIDE the transaction (session is still open here)
        // Passing the id directly to the DTO avoids lazy load after session closes
        Integer departmentId = (user.getDepartment() != null) ? user.getDepartment().getId() : null;
        return UserResponse.from(user, departmentId);
    }

    @Transactional
    public UserResponse updateProfile(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Partial update pattern
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getLanguage() != null) user.setLanguage(request.getLanguage());

        User saved = userRepository.save(user);
        Integer departmentId = (saved.getDepartment() != null) ? saved.getDepartment().getId() : null;
        return UserResponse.from(saved, departmentId);
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(String userId) {
        // We verify the user exists first
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserStatsResponse stats = new UserStatsResponse();
        
        // TODO (Phase 3): Query IssueRepository for these stats.
        // For now, we return 0s until Issue domain is built.
        stats.setTotalReports(0);
        stats.setTotalUpvotesReceived(0);
        stats.setIssuesResolved(0);
        stats.setIssuesPending(0);
        stats.setIssuesInProgress(0);

        return stats;
    }
}
