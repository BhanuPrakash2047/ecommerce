package com.snackecommerce.user.repository;

import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by email and auth provider
     */
    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
    
    /**
     * Check if user exists with given email and active status
     */
    boolean existsByEmailAndActive(String email, Boolean active);
}
