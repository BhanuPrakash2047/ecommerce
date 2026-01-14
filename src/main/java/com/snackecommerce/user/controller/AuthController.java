package com.snackecommerce.user.controller;

import com.snackecommerce.common.annotation.RateLimit;
import com.snackecommerce.common.util.JwtUtil;
import com.snackecommerce.user.dto.ChangePasswordRequest;
import com.snackecommerce.user.dto.JwtResponse;
import com.snackecommerce.user.dto.LoginRequest;
import com.snackecommerce.user.dto.RegisterRequest;
import com.snackecommerce.user.dto.UserResponse;
import com.snackecommerce.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Traditional JWT login with username/email and password
     */
    @PostMapping("/login")
    @RateLimit(value = "login", useAuthenticatedUser = false)
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * User registration with JWT token
     */
    @PostMapping("/register")
    @RateLimit(value = "register", useAuthenticatedUser = false)
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        JwtResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile (requires authentication)
     * Works with both JWT and OAuth2
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = authService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile (requires authentication)
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserResponse updateRequest) {
        String email = authentication.getName();
        UserResponse response = authService.updateProfile(email, updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Change user password (requires authentication)
     * Validates old password before changing to new password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        String email = authentication.getName();
        authService.changePassword(email, changePasswordRequest);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * OAuth2 Success Callback - Frontend receives token and user info
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String email) {
        if (token == null) {
            return ResponseEntity.badRequest().body("Token not found in OAuth2 response");
        }

        UserResponse userResponse = authService.getProfile(email);
        JwtResponse jwtResponse = new JwtResponse(token, userResponse);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * OAuth2 Failure Callback
     */
    @GetMapping("/oauth2/error")
    public ResponseEntity<?> oauth2Error(@RequestParam(defaultValue = "Authentication failed") String message) {
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /**
     * Validate token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid authorization header"));
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getUsernameFromToken(token);
            return ResponseEntity.ok(new TokenValidationResponse(true, email));
        }

        return ResponseEntity.badRequest().body(new TokenValidationResponse(false, null));
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        String email = authentication.getName();
        String newToken = jwtUtil.generateToken(email);
        return ResponseEntity.ok(new RefreshTokenResponse(newToken));
    }

    // Inner classes for response DTOs
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String email;

        public TokenValidationResponse(boolean valid, String email) {
            this.valid = valid;
            this.email = email;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class RefreshTokenResponse {
        private String token;

        public RefreshTokenResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
