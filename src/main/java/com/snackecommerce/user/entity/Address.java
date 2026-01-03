package com.snackecommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String fullName;
    private String phoneNumber;

    private String addressLine1;
    private String addressLine2;

    private String city;
    private String state;
    private String zipCode;
    private String country;

    private Boolean isDefault = false;

    private Boolean pincodeReachable;

    private LocalDateTime lastCheckedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
