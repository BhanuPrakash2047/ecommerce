package com.snackecommerce.delivery.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Delhivery API Integration Utility
 * Handles shipment creation, tracking, and pincode availability checks
 */
@Component
public class DelhiveryUtil {

    private static final Logger logger = LoggerFactory.getLogger(DelhiveryUtil.class);

    @Value("${delhivery.api-token}")
    private String apiToken;

    @Value("${delhivery.api-url:https://track.delhivery.com/api}")
    private String apiUrl;

    private static final String SHIPMENT_CREATE_ENDPOINT = "/cmu/create/json/";
    private static final String SHIPMENT_TRACK_ENDPOINT = "/cmu/track/json/";
    private static final String PINCODE_CHECK_ENDPOINT = "/pin/query/";

    /**
     * Build proper Delhivery shipment JSON payload
     * 
     * @param orderNumber Order number
     * @param addressLine Address line
     * @param city City
     * @param pincode Pincode
     * @param state State
     * @param country Country
     * @param landmark Landmark
     * @param phone Phone number
     * @param customerName Customer name
     * @return JSONObject with properly formatted shipment payload
     */
    public JSONObject buildShipmentPayload(
            String orderNumber,
            String addressLine,
            String city,
            String pincode,
            String state,
            String country,
            String landmark,
            String phone,
            String customerName) {
        
        JSONObject shipment = new JSONObject();
        
        // Order information
        shipment.put("order", orderNumber);
        
        // Payment mode: Prepaid or COD
        shipment.put("payment_mode", "Prepaid");
        
        // Package weight in kg
        shipment.put("weight", "0.5");
        
        // Pickup location (warehouse ID)
        shipment.put("pickup_location", "WH_01");
        
        // Shipping address (receiver/delivery details) - Complete address
        shipment.put("shipping_address", addressLine);
        if (landmark != null && !landmark.isEmpty()) {
            shipment.put("shipping_address", addressLine + ", " + landmark);
        }
        shipment.put("shipping_city", city);
        shipment.put("shipping_pincode", pincode);
        shipment.put("shipping_state", state);
        shipment.put("shipping_country", country);
        shipment.put("shipping_phone", phone);
        shipment.put("shipping_customer_name", customerName);
        
        logger.info("Built Delhivery shipment payload for order: {} to {}, {}, {}", 
                   orderNumber, addressLine, city, pincode);
        
        return shipment;
    }

    /**
     * Create shipment on Delhivery and get waybill number
     * 
     * @param shipmentRequest Shipment details
     * @return JSONObject containing waybill number and label URL
     * @throws Exception if API call fails
     */
    public JSONObject createShipment(JSONObject shipmentRequest) throws Exception {
        try {
            logger.info("Creating Delhivery shipment");
            
            String endpoint = apiUrl + SHIPMENT_CREATE_ENDPOINT;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = shipmentRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200 || responseCode == 201) {
                JSONObject responseJson = new JSONObject(response);
                logger.info("Shipment created successfully with waybill: {}", responseJson.optString("waybill"));
                return responseJson;
            } else {
                logger.error("Failed to create shipment. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Delhivery API error: " + response);
            }
        } catch (Exception e) {
            logger.error("Error creating Delhivery shipment", e);
            throw e;
        }
    }

    /**
     * Extract waybill number from Delhivery API response
     * 
     * @param shipmentResponse API response from shipment creation
     * @return Waybill number
     */
    public String extractWaybillNumber(JSONObject shipmentResponse) {
        String waybill = shipmentResponse.optString("waybill");
        if (waybill == null || waybill.isEmpty()) {
            waybill = shipmentResponse.optString("waybill_no");
        }
        logger.info("Extracted waybill number: {}", waybill);
        return waybill;
    }

    /**
     * Get shipping label URL from Delhivery for on-the-fly download
     * 
     * @param waybillNumber Waybill number
     * @return URL to download label PDF
     */
    public String getShippingLabelUrl(String waybillNumber) {
        // Delhivery label URL format
        String labelUrl = apiUrl + "/services/waybills/" + waybillNumber + "/label";
        logger.info("Generated label URL for waybill: {}", waybillNumber);
        return labelUrl;
    }

    /**
     * Download shipping label from Delhivery API
     * 
     * @param waybillNumber Waybill number
     * @return PDF bytes for the label
     * @throws Exception if download fails
     */
    public byte[] downloadShippingLabel(String waybillNumber) throws Exception {
        try {
            logger.info("Downloading shipping label for waybill: {}", waybillNumber);
            
            String labelUrl = getShippingLabelUrl(waybillNumber);
            URL url = new URL(labelUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Accept", "application/pdf");

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                byte[] labelData = conn.getInputStream().readAllBytes();
                logger.info("Successfully downloaded label for waybill: {}", waybillNumber);
                return labelData;
            } else {
                logger.error("Failed to download label. Response code: {}", responseCode);
                throw new RuntimeException("Label download failed with code: " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Error downloading shipping label for waybill: {}", waybillNumber, e);
            throw e;
        }
    }

    /**
     * Track shipment using waybill number
     * 
     * @param waybillNumber Delhivery waybill number
     * @return JSONObject containing tracking details
     * @throws Exception if API call fails
     */
    public JSONObject trackShipment(String waybillNumber) throws Exception {
        try {
            logger.info("Tracking Delhivery shipment: {}", waybillNumber);
            
            String endpoint = apiUrl + SHIPMENT_TRACK_ENDPOINT + "?waybill=" + waybillNumber;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                logger.info("Shipment tracking status: {}", responseJson.optString("status"));
                return responseJson;
            } else {
                logger.error("Failed to track shipment. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Delhivery tracking error: " + response);
            }
        } catch (Exception e) {
            logger.error("Error tracking Delhivery shipment: {}", waybillNumber, e);
            throw e;
        }
    }

    /**
     * Check pincode availability for Delhivery delivery
     * 
     * @param pincode Destination pincode
     * @return JSONObject containing availability info
     * @throws Exception if API call fails
     */
    public JSONObject checkPincodeAvailability(String pincode) throws Exception {
        try {
            logger.info("Checking Delhivery availability for pincode: {}", pincode);
            
            String endpoint = apiUrl + PINCODE_CHECK_ENDPOINT + "?pincode=" + pincode;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200) {
                JSONArray responseArray = new JSONArray(response);
                if (responseArray.length() > 0) {
                    JSONObject pincodeData = responseArray.getJSONObject(0);
                    logger.info("Pincode {} is available: {}", pincode, pincodeData.optString("status"));
                    return pincodeData;
                } else {
                    throw new RuntimeException("Pincode not serviceable");
                }
            } else {
                logger.error("Failed to check pincode. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Pincode check failed: " + response);
            }
        } catch (Exception e) {
            logger.error("Error checking pincode availability: {}", pincode, e);
            throw e;
        }
    }

    /**
     * Get shipment status by waybill
     * 
     * @param waybillNumber Waybill number
     * @return Current status of shipment
     * @throws Exception if API call fails
     */
    public String getShipmentStatus(String waybillNumber) throws Exception {
        JSONObject trackingData = trackShipment(waybillNumber);
        return trackingData.optString("status", "UNKNOWN");
    }

    /**
     * Helper method to read HTTP response
     */
    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader br;
        if (conn.getResponseCode() >= 400) {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        return response.toString();
    }
}
