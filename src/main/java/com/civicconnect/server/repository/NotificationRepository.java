package com.civicconnect.server.repository;

import com.civicconnect.server.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Fetch notifications for a user, usually ordered by newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}
