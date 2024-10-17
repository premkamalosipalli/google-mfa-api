package com.okta.app.repository;

import com.okta.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Method to find a user by username
    User findByUsername(String username);

    boolean existsByUsername(String username);
}
