package com.snackecommerce.user.repository;

import com.snackecommerce.user.entity.Address;

import java.util.List;

public interface AddressRepository extends org.springframework.data.jpa.repository.JpaRepository<com.snackecommerce.user.entity.Address, Long> {
    List<Address> findByUserId(Long userId);
}
