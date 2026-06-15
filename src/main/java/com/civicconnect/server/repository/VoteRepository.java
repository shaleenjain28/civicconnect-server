package com.civicconnect.server.repository;

import com.civicconnect.server.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Integer> {

    // Fetch all votes for a specific issue
    List<Vote> findByIssueId(Integer issueId);
    
    // Check if a specific user voted on a specific issue
    boolean existsByUserIdAndIssueId(String userId, Integer issueId);
}
