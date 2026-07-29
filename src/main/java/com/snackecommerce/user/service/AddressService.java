package com.snackecommerce.user.service;

import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.user.entity.Address;
import com.snackecommerce.user.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
@Transactional
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    private static final long CACHE_VALIDITY_DAYS = 7;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Check if pincode is reachable via Delhivery
     * If Address has cached result and it's < 7 days old, return cached value
     * Otherwise, hit Delhivery API and update cache
     * 
     * @param address Address entity with pincode
     * @return PincodeAvailabilityResponse with reachability status
     */
    public PincodeAvailabilityResponse checkPincodeReachability(Address address) {
        if (address == null || address.getZipCode() == null || address.getZipCode().isEmpty()) {
            return PincodeAvailabilityResponse.builder()
                    .isAvailable(false)
                    .status("INVALID_PINCODE")
                    .pincode(address != null ? address.getZipCode() : "")
                    .build();
        }

        String pincode = address.getZipCode();
        
        // Check if cache is still valid (< 7 days)
        if (address.getPincodeReachable() != null && address.getLastCheckedAt() != null) {
            LocalDateTime cacheExpiry = address.getLastCheckedAt().plusDays(CACHE_VALIDITY_DAYS);
            if (LocalDateTime.now().isBefore(cacheExpiry)) {
                logger.info("Using cached pincode reachability for pincode: {} (checked at: {})", 
                           pincode, address.getLastCheckedAt());
                return PincodeAvailabilityResponse.builder()
                    .pincode(pincode)
                    .isAvailable(address.getPincodeReachable())
                    .status(address.getPincodeReachable() ? "SERVICEABLE (cached)" : "NOT_SERVICEABLE (cached)")
                    .build();
            }
        }

        // Cache expired or not set - hit Delhivery API
        logger.info("Checking pincode reachability with Delhivery API for pincode: {}", pincode);
        try {
            PincodeAvailabilityResponse delhiveryResponse = deliveryService.checkPincodeAvailability(pincode);
            
            // Update address with fresh data
            address.setPincodeReachable(delhiveryResponse.getIsAvailable());
            address.setLastCheckedAt(LocalDateTime.now());
            addressRepository.save(address);
            
            logger.info("Pincode {} reachability: {}", pincode, delhiveryResponse.getIsAvailable());
            return delhiveryResponse;
        } catch (Exception e) {
            logger.error("Error checking pincode reachability for pincode: {}", pincode, e);
            // Return not available if API fails (safer approach)
            return PincodeAvailabilityResponse.builder()
                    .pincode(pincode)
                    .isAvailable(false)
                    .status("API_ERROR")
                    .build();
        }
    }

    /**
     * Check pincode directly without address entity
     * Useful for pre-checkout validation
     * 
     * @param pincode Pincode to check
     * @return PincodeAvailabilityResponse
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public PincodeAvailabilityResponse checkPincodeByValue(String pincode) {
        if (pincode == null || pincode.isEmpty()) {
            return PincodeAvailabilityResponse.builder()
                    .isAvailable(false)
                    .status("INVALID_PINCODE")
                    .pincode("")
                    .build();
        }

        logger.info("Checking pincode availability directly: {}", pincode);
        try {
            return deliveryService.checkPincodeAvailability(pincode);
        } catch (Exception e) {
            logger.error("Error checking pincode: {}", pincode, e);
            return PincodeAvailabilityResponse.builder()
                    .pincode(pincode)
                    .isAvailable(false)
                    .status("NOT_SERVICEABLE")
                    .build();
        }
    }

    /**
     * Load an address only if it belongs to the authenticated user.
     */
    public Address requireOwnedAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));

        if (userId == null || !address.getUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to access this address");
        }

        return address;
    }
}
