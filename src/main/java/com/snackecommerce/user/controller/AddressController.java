package com.snackecommerce.user.controller;

import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.user.entity.Address;
import com.snackecommerce.user.repository.AddressRepository;
import com.snackecommerce.user.repository.UserRepository;
import com.snackecommerce.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated user ID from SecurityContext
     */
    private Long getCurrentUserId() {
        // Extract email from security context (set by JwtAuthenticationFilter)
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Look up user by email and return their ID
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
                .getId();
    }
    private String getCurrentUserEmail() {
        // Extract email from security context (set by JwtAuthenticationFilter)
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * GET /api/addresses
     * Fetch all addresses for the authenticated user
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> getAllAddresses() {
        try {
            Long userId = getCurrentUserId();
            List<Address> addresses = addressRepository.findByUserId(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to fetch addresses: " + e.getMessage()));
        }
    }

    /**
     * GET /api/addresses/{addressId}
     * Fetch a single address by ID
     */
    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> getAddressById(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();
            
            Address address = addressRepository.findById(addressId)
                    .orElse(null);
            
            if (address == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Address not found"));
            }
            
            // Verify ownership - ensure user can only fetch their own addresses
            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to access this address"));
            }
            
            return ResponseEntity.ok(Map.of("data", address));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch address: " + e.getMessage()));
        }
    }

    /**
     * POST /api/addresses
     * Add a new address for the authenticated user
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> addAddress(
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId();

            // Validate required fields
            if (!request.containsKey("fullName") || request.get("fullName") == null || 
                request.get("fullName").toString().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Full name is required"));
            }

            if (!request.containsKey("phoneNumber") || request.get("phoneNumber") == null || 
                request.get("phoneNumber").toString().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Phone number is required"));
            }

            if (!request.containsKey("addressLine1") || request.get("addressLine1") == null || 
                request.get("addressLine1").toString().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Address line 1 is required"));
            }

            if (!request.containsKey("zipCode") || request.get("zipCode") == null || 
                request.get("zipCode").toString().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Zip code is required"));
            }

            Address address = Address.builder()
                    .userId(userId)
                    .fullName(request.get("fullName").toString())
                    .phoneNumber(request.get("phoneNumber").toString())
                    .addressLine1(request.get("addressLine1").toString())
                    .addressLine2(request.containsKey("addressLine2") ? request.get("addressLine2").toString() : null)
                    .city(request.containsKey("city") ? request.get("city").toString() : null)
                    .state(request.containsKey("state") ? request.get("state").toString() : null)
                    .zipCode(request.get("zipCode").toString())
                    .country(request.containsKey("country") ? request.get("country").toString() : "India")
                    .isDefault(request.containsKey("isDefault") ? (Boolean) request.get("isDefault") : false)
                    .build();

            Address savedAddress = addressRepository.save(address);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add address: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/addresses/{addressId}
     * Update an existing address
     */
    @PutMapping("/{addressId}")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId();

            Address address = addressRepository.findById(addressId)
                    .orElse(null);

            if (address == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Address not found"));
            }

            // Verify ownership
            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to update this address"));
            }

            // Validate required fields
            if (request.containsKey("fullName") && (request.get("fullName") == null || 
                request.get("fullName").toString().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Full name cannot be empty"));
            }

            if (request.containsKey("phoneNumber") && (request.get("phoneNumber") == null || 
                request.get("phoneNumber").toString().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Phone number cannot be empty"));
            }

            if (request.containsKey("zipCode") && (request.get("zipCode") == null || 
                request.get("zipCode").toString().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Zip code cannot be empty"));
            }

            // Update fields
            if (request.containsKey("fullName")) {
                address.setFullName(request.get("fullName").toString());
            }
            if (request.containsKey("phoneNumber")) {
                address.setPhoneNumber(request.get("phoneNumber").toString());
            }
            if (request.containsKey("addressLine1")) {
                address.setAddressLine1(request.get("addressLine1").toString());
            }
            if (request.containsKey("addressLine2")) {
                address.setAddressLine2(request.get("addressLine2").toString());
            }
            if (request.containsKey("city")) {
                address.setCity(request.get("city").toString());
            }
            if (request.containsKey("state")) {
                address.setState(request.get("state").toString());
            }
            if (request.containsKey("zipCode")) {
                address.setZipCode(request.get("zipCode").toString());
            }
            if (request.containsKey("country")) {
                address.setCountry(request.get("country").toString());
            }
            if (request.containsKey("isDefault")) {
                address.setIsDefault((Boolean) request.get("isDefault"));
            }

            Address updatedAddress = addressRepository.save(address);
            return ResponseEntity.ok(updatedAddress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update address: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/addresses/{addressId}
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();

            Address address = addressRepository.findById(addressId)
                    .orElse(null);

            if (address == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Address not found"));
            }

            // Verify ownership
            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to delete this address"));
            }

            addressRepository.delete(address);
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete address: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/addresses/{addressId}/default
     * Set an address as default
     */
    @PutMapping("/{addressId}/default")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long addressId) {
        try {
            Long userId = getCurrentUserId();

            Address address = addressRepository.findById(addressId)
                    .orElse(null);

            if (address == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Address not found"));
            }

            // Verify ownership
            if (!address.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to modify this address"));
            }

            // Remove default from all other addresses
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            for (Address addr : userAddresses) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }

            // Set this address as default
            address.setIsDefault(true);
            Address updatedAddress = addressRepository.save(address);

            return ResponseEntity.ok(updatedAddress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to set default address: " + e.getMessage()));
        }
    }

    /**
     * GET /api/addresses/check-pincode?pincode=110001
     * Check if a pincode is reachable via Delhivery
     * 
     * Uses 7-day cache: if Address or pincode was checked < 7 days ago, returns cached result
     * Otherwise, hits Delhivery API for fresh data
     * 
     * Response: { "isAvailable": true, "message": "Serviceable" }
     */
    @GetMapping("/check-pincode")
    public ResponseEntity<?> checkPincodeReachability(
            @RequestParam(value = "pincode", required = false) String pincode,
            @RequestParam(value = "addressId", required = false) Long addressId) {
        
        try {
            PincodeAvailabilityResponse response;
            
            if (addressId != null) {
                // If addressId provided, update the specific Address entity
                Address address = addressRepository.findById(addressId).orElse(null);
                if (address == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Address not found"));
                }
                
                response = addressService.checkPincodeReachability(address);
            } else {
                // Just check pincode without updating an address
                response = addressService.checkPincodeByValue(pincode);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Pincode check failed: " + e.getMessage()));
        }
    }
}
