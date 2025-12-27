package com.snackecommerce.user.service;

import com.snackecommerce.common.exception.OAuth2AuthenticationException;
import com.snackecommerce.user.dto.JwtResponse;
import com.snackecommerce.user.dto.LoginRequest;
import com.snackecommerce.user.dto.RegisterRequest;
import com.snackecommerce.user.dto.UserResponse;
import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.enums.AuthProvider;
import com.snackecommerce.user.enums.UserRole;
import com.snackecommerce.user.repository.UserRepository;
import com.snackecommerce.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login with email and password
     */
    public JwtResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new OAuth2AuthenticationException("Invalid email or password"));

        if (!user.getActive()) {
            throw new OAuth2AuthenticationException("User account is disabled");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new OAuth2AuthenticationException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = convertToUserResponse(user);

        return new JwtResponse(token, userResponse);
    }

    /**
     * Register new user with email and password
     */
    public JwtResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new OAuth2AuthenticationException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .role(registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());
        UserResponse userResponse = convertToUserResponse(savedUser);

        return new JwtResponse(token, userResponse);
    }

    /**
     * Get user profile by email
     */
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException("User not found"));

        if (!user.getActive()) {
            throw new OAuth2AuthenticationException("User account is disabled");
        }

        return convertToUserResponse(user);
    }

    /**
     * Update user profile
     */
    public UserResponse updateProfile(String email, UserResponse updateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException("User not found"));

        if (!user.getActive()) {
            throw new OAuth2AuthenticationException("User account is disabled");
        }

        // Update only allowed fields
        if (updateRequest.getRole() != null && !updateRequest.getRole().equals(user.getRole())) {
            // Only admins should be able to change roles
            user.setRole(updateRequest.getRole());
        }

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
