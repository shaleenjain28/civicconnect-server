package com.civicconnect.server.repository;

import com.civicconnect.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // The id is a String UUID, so it's JpaRepository<User, String>
    
    // We need findByEmail in case we ever need to look up a user by email
    Optional<User> findByEmail(String email);
    
    // phone is unique in the DB, so this might be useful later
    Optional<User> findByPhone(String phone);
}
