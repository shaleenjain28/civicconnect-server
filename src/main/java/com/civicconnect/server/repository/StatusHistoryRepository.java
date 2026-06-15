package com.civicconnect.server.repository;

import com.civicconnect.server.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Integer> {

    // Fetch the history for a specific issue, ordered by newest first
    List<StatusHistory> findByIssueIdOrderByChangedAtDesc(Integer issueId);
}
