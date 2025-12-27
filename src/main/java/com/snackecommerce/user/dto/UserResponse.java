package com.snackecommerce.user.dto;

import com.snackecommerce.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
