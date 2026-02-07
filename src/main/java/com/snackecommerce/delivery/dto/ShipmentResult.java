package com.snackecommerce.delivery.dto;

/**
 * DTO for shipment creation result
 * Contains success/failure status and the actual error message from delivery provider
 */
public class ShipmentResult {
    
    private boolean success;
    private String waybill;
    private String errorMessage;
    private String labelUrl;
    
    // Private constructor - use factory methods
    private ShipmentResult() {}
    
    /**
     * Create a successful shipment result
     */
    public static ShipmentResult success(String waybill, String labelUrl) {
        ShipmentResult result = new ShipmentResult();
        result.success = true;
        result.waybill = waybill;
        result.labelUrl = labelUrl;
        result.errorMessage = null;
        return result;
    }
    
    /**
     * Create a failed shipment result with the actual provider error message
     */
    public static ShipmentResult failure(String errorMessage) {
        ShipmentResult result = new ShipmentResult();
        result.success = false;
        result.waybill = null;
        result.labelUrl = null;
        result.errorMessage = errorMessage;
        return result;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getWaybill() {
        return waybill;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getLabelUrl() {
        return labelUrl;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "ShipmentResult{success=true, waybill='" + waybill + "', labelUrl='" + labelUrl + "'}";
        } else {
            return "ShipmentResult{success=false, errorMessage='" + errorMessage + "'}";
        }
    }
}
