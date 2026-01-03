package com.snackecommerce.user.controller;

import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.user.entity.Address;
import com.snackecommerce.user.repository.AddressRepository;
import com.snackecommerce.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    /**
     * GET /api/address/check-pincode?pincode=110001
     * Check if a pincode is reachable via Delhivery
     * 
     * Uses 7-day cache: if Address or pincode was checked < 7 days ago, returns cached result
     * Otherwise, hits Delhivery API for fresh data
     * 
     * Response: { "isAvailable": true, "message": "Serviceable" }
     */
    @GetMapping("/check-pincode")
    public ResponseEntity<?> checkPincodeReachability(
            @RequestParam(value = "pincode", required = true) String pincode,
            @RequestParam(value = "addressId", required = false) Long addressId) {
        
        try {
            PincodeAvailabilityResponse response;
            
            if (addressId != null) {
                // If addressId provided, update the specific Address entity
                Address address = addressRepository.findById(addressId).orElse(null);
                if (address == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new HashMap<String, String>() {{ put("error", "Address not found"); }});
                }
                
                response = addressService.checkPincodeReachability(address);
            } else {
                // Just check pincode without updating an address
                response = addressService.checkPincodeByValue(pincode);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Pincode check failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
