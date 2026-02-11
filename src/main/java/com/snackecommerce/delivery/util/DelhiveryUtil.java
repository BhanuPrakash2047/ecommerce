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
 * Handles shipment creation, tracking, pincode serviceability, and label generation
 * 
 * Production API Base: https://track.delhivery.com
 * Staging API Base: https://staging-express.delhivery.com
 */
@Component
public class DelhiveryUtil {

    private static final Logger logger = LoggerFactory.getLogger(DelhiveryUtil.class);

    @Value("${delhivery.api-token}")
    private String apiToken;

    @Value("${delhivery.api-url:https://track.delhivery.com}")
    private String apiUrl;

    @Value("${delhivery.pickup-location}")
    private String defaultPickupLocation;

    // Production Delhivery API Endpoints
    private static final String SHIPMENT_CREATE_ENDPOINT = "/api/cmu/create.json";
    private static final String SHIPMENT_TRACK_ENDPOINT = "/api/v1/packages/json/";
    private static final String PINCODE_CHECK_ENDPOINT = "/c/api/pin-codes/json/";
    private static final String LABEL_GENERATION_ENDPOINT = "/api/p/packing_slip";

    /**
     * Build proper Delhivery shipment JSON payload as per production API specs.
     * 
     * Payment Modes:
     * - "Prepaid" or "COD" for forward packages
     * - "Pickup" for reverse packages (RVP)
     * - "REPL" for replacement shipments
     * 
     * @param orderNumber Unique Order ID
     * @param addressLine Consignee address
     * @param city Consignee city
     * @param pincode Consignee pincode
     * @param state Consignee state
     * @param country Consignee country
     * @param landmark Landmark (optional)
     * @param phone Consignee phone number
     * @param customerName Consignee name
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
        
        return buildShipmentPayload(orderNumber, addressLine, city, pincode, state, 
                country, landmark, phone, customerName, "Prepaid", null, 0, null);
    }

    /**
     * Build Delhivery shipment JSON payload with extended options.
     * 
     * @param orderNumber Unique Order ID
     * @param addressLine Consignee address
     * @param city Consignee city
     * @param pincode Consignee pincode
     * @param state Consignee state
     * @param country Consignee country
     * @param landmark Landmark (optional)
     * @param phone Consignee phone number
     * @param customerName Consignee name
     * @param paymentMode Payment mode: Prepaid, COD, Pickup, or REPL
     * @param shippingMode Shipping mode: Surface or Express (optional)
     * @param codAmount COD amount if payment_mode is COD
     * @param productDesc Product description (optional)
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
            String customerName,
            String paymentMode,
            String shippingMode,
            double codAmount,
            String productDesc) {
        
        // Build the shipment object with Delhivery's expected field names
        JSONObject shipment = new JSONObject();
        
        // Mandatory fields
        shipment.put("name", customerName);                          // Consignee name
        shipment.put("order", orderNumber);                          // Unique Order ID
        shipment.put("phone", phone);                                // Consignee phone
        shipment.put("add", buildFullAddress(addressLine, landmark)); // Consignee address
        shipment.put("pin", pincode);                                // Consignee pincode
        
        // Payment mode: Prepaid, COD, Pickup (RVP), or REPL
        shipment.put("payment_mode", "Prepaid");
        
        // Optional but recommended fields
        shipment.put("city", city);
        shipment.put("state", state);
        shipment.put("country", country != null ? country : "India");
        
        // Shipping mode: Surface or Express
//        if (shippingMode != null && !shippingMode.isEmpty()) {
            shipment.put("shipping_mode","Surface");
//        }
        
        // // COD amount if applicable
        // if ("COD".equalsIgnoreCase(paymentMode) && codAmount > 0) {
        //     shipment.put("cod_amount", String.valueOf(codAmount));
        // }
        
        // Product description
        if (productDesc != null && !productDesc.isEmpty()) {
            shipment.put("products_desc", productDesc);
        }
        
        // Optional shipment dimensions (can be extended)
        shipment.put("shipment_width", "");
        shipment.put("shipment_height", "");
        shipment.put("weight", "");
        
        // Return address fields (empty for forward shipments)
        shipment.put("return_pin", "");
        shipment.put("return_city", "");
        shipment.put("return_phone", "");
        shipment.put("return_add", "");
        shipment.put("return_state", "");
        shipment.put("return_country", "");
        
        // Additional optional fields
        shipment.put("waybill", "");            // Auto-generated by Delhivery for SPS
        shipment.put("seller_name", "");
        shipment.put("seller_add", "");
        shipment.put("seller_inv", "");
        shipment.put("quantity", "");
        shipment.put("total_amount", "");
        shipment.put("hsn_code", "");
        shipment.put("order_date", JSONObject.NULL);
        shipment.put("address_type", "");       // home or office
        
        logger.info("Built Delhivery shipment payload for order: {} to {}, {}, {} [mode: {}]", 
                   orderNumber, customerName, city, pincode, paymentMode);
        
        return shipment;
    }

    /**
     * Build the complete request payload with shipments array and pickup location.
     * 
     * @param shipment Single shipment JSONObject
     * @param pickupLocationName Warehouse/pickup location name
     * @return Complete payload ready for API call
     */
    public JSONObject buildCompletePayload(JSONObject shipment, String pickupLocationName) {
        JSONObject payload = new JSONObject();
        
        // Shipments array (can contain multiple shipments)
        JSONArray shipmentsArray = new JSONArray();
        shipmentsArray.put(shipment);
        payload.put("shipments", shipmentsArray);
        
        // Pickup location object
        JSONObject pickupLocation = new JSONObject();
        pickupLocation.put("name", pickupLocationName != null ? pickupLocationName : defaultPickupLocation);
        payload.put("pickup_location", pickupLocation);
        
        return payload;
    }

    /**
     * Build full address with landmark
     */
    private String buildFullAddress(String addressLine, String landmark) {
        if (landmark != null && !landmark.isEmpty()) {
            return addressLine + ", " + landmark;
        }
        return addressLine;
    }

    /**
     * Create shipment on Delhivery and get waybill number.
     * Uses format=json&data={payload} as required by Delhivery API.
     * 
     * @param shipmentRequest Single shipment details
     * @return JSONObject containing waybill number and response details
     * @throws Exception if API call fails
     */
    public JSONObject createShipment(JSONObject shipmentRequest) throws Exception {
        return createShipment(shipmentRequest, defaultPickupLocation);
    }

    /**
     * Create shipment on Delhivery with specified pickup location.
     * 
     * @param shipmentRequest Single shipment details
     * @param pickupLocationName Warehouse/pickup location name (case/space sensitive)
     * @return JSONObject containing waybill number and response details
     * @throws Exception if API call fails
     */
    public JSONObject createShipment(JSONObject shipmentRequest, String pickupLocationName) throws Exception {
        try {
            logger.info("Creating Delhivery shipment for order: {}", shipmentRequest.optString("order"));
            
            // Build complete payload with shipments array and pickup location
            JSONObject completePayload = buildCompletePayload(shipmentRequest, pickupLocationName);
            
            // Log the request payload
            logger.info("========== DELHIVERY SHIPMENT CREATE REQUEST ==========");
            logger.info("Request Payload: {}", completePayload.toString(2));
            
            String endpoint = apiUrl + SHIPMENT_CREATE_ENDPOINT;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // Handle token prefix
            String authHeader = apiToken.trim().startsWith("Token ") ? apiToken.trim() : "Token " + apiToken.trim();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Delhivery expects: format=json&data={json_payload}
            String requestBody = "format=json&data=" + URLEncoder.encode(completePayload.toString(), StandardCharsets.UTF_8);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            // Log the full API response
            logger.info("========== DELHIVERY SHIPMENT CREATE RESPONSE ==========");
            logger.info("Response Code: {}", responseCode);
            logger.info("Raw Response: {}", response);
            
            try {
                JSONObject prettyResponse = new JSONObject(response);
                logger.info("Parsed Response (JSON): {}", prettyResponse.toString(2));
            } catch (Exception e) {
                logger.info("Response is not valid JSON: {}", response);
            }
            logger.info("========================================================");

            if (responseCode == 200 || responseCode == 201) {
                JSONObject responseJson = new JSONObject(response);
                
                // Validate waybill was actually assigned (API may return 200 with error)
                String waybill = extractWaybillNumber(responseJson);
                if (waybill == null || waybill.isEmpty()) {
                    String errorMsg = responseJson.optString("rmk", 
                            responseJson.optString("remarks", 
                            responseJson.optString("error", "Waybill not assigned")));
                    logger.error("Delhivery returned 200 but no waybill assigned. Error: {}, Full Response: {}", 
                                errorMsg, response);
                    throw new RuntimeException("Shipment creation failed - No waybill assigned: " + errorMsg);
                }
                
                logger.info("Shipment created successfully with waybill: {}", waybill);
                return responseJson;
            } else {
                logger.error("Failed to create shipment. Response code: {}, Full Response: {}", responseCode, response);
                throw new RuntimeException("Delhivery API error: " + response);
            }
        } catch (Exception e) {
            logger.error("Error creating Delhivery shipment", e);
            throw e;
        }
    }

    /**
     * Create multiple shipments in a single API call.
     * 
     * @param shipments Array of shipment JSONObjects
     * @param pickupLocationName Warehouse/pickup location name
     * @return JSONObject containing waybill numbers for all shipments
     * @throws Exception if API call fails
     */
    public JSONObject createMultipleShipments(JSONArray shipments, String pickupLocationName) throws Exception {
        try {
            logger.info("Creating {} Delhivery shipments", shipments.length());
            
            JSONObject payload = new JSONObject();
            payload.put("shipments", shipments);
            
            JSONObject pickupLocation = new JSONObject();
            pickupLocation.put("name", pickupLocationName != null ? pickupLocationName : defaultPickupLocation);
            payload.put("pickup_location", pickupLocation);
            
            String endpoint = apiUrl + SHIPMENT_CREATE_ENDPOINT;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String requestBody = "format=json&data=" + URLEncoder.encode(payload.toString(), StandardCharsets.UTF_8);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200 || responseCode == 201) {
                JSONObject responseJson = new JSONObject(response);
                logger.info("Multiple shipments created successfully");
                return responseJson;
            } else {
                logger.error("Failed to create shipments. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Delhivery API error: " + response);
            }
        } catch (Exception e) {
            logger.error("Error creating multiple Delhivery shipments", e);
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
        String waybill = null;
        
        // Check in packages array (common response format)
        if (shipmentResponse.has("packages")) {
            JSONArray packages = shipmentResponse.optJSONArray("packages");
            if (packages != null && packages.length() > 0) {
                JSONObject firstPackage = packages.optJSONObject(0);
                if (firstPackage != null) {
                    waybill = firstPackage.optString("waybill");
                }
            }
        }
        
        // Fallback to direct waybill field
        if (waybill == null || waybill.isEmpty()) {
            waybill = shipmentResponse.optString("waybill");
        }
        if (waybill == null || waybill.isEmpty()) {
            waybill = shipmentResponse.optString("waybill_no");
        }
        
        logger.info("Extracted waybill number: {}", waybill);
        return waybill;
    }

    /**
     * Generate shipping label URL for Delhivery.
     * Production endpoint: /api/p/packing_slip
     * 
     * @param waybillNumber Waybill number
     * @param generatePdf If true, returns S3 PDF link; if false, returns JSON for custom rendering
     * @param pdfSize Label size: "A4" (8x11) or "4R" (4x6). Defaults to A4.
     * @return URL to download/generate label
     */
    public String getShippingLabelUrl(String waybillNumber, boolean generatePdf, String pdfSize) {
        StringBuilder labelUrl = new StringBuilder(apiUrl);
        labelUrl.append(LABEL_GENERATION_ENDPOINT);
        labelUrl.append("?wbns=").append(waybillNumber);
        labelUrl.append("&pdf=").append(generatePdf);
        
        if (pdfSize != null && !pdfSize.isEmpty()) {
            labelUrl.append("&pdf_size=").append(pdfSize);
        }
        
        logger.info("Generated label URL for waybill: {} [pdf={}, size={}]", waybillNumber, generatePdf, pdfSize);
        return labelUrl.toString();
    }

    /**
     * Get shipping label URL with default PDF generation (A4 size)
     * 
     * @param waybillNumber Waybill number
     * @return URL to download label PDF
     */
    public String getShippingLabelUrl(String waybillNumber) {
        return getShippingLabelUrl(waybillNumber, true, "A4");
    }

    /**
     * Download shipping label from Delhivery API.
     * Uses the packing_slip endpoint with pdf=true.
     * 
     * @param waybillNumber Waybill number
     * @return PDF bytes for the label (when pdf=true, returns S3 link in response)
     * @throws Exception if download fails
     */
    public byte[] downloadShippingLabel(String waybillNumber) throws Exception {
        return downloadShippingLabel(waybillNumber, "A4");
    }

    /**
     * Download shipping label from Delhivery API with specified size.
     * 
     * @param waybillNumber Waybill number
     * @param pdfSize Label size: "A4" (8x11) or "4R" (4x6)
     * @return PDF bytes for the label
     * @throws Exception if download fails
     */
    public byte[] downloadShippingLabel(String waybillNumber, String pdfSize) throws Exception {
        try {
            logger.info("Downloading shipping label for waybill: {} [size: {}]", waybillNumber, pdfSize);
            
            String labelUrl = getShippingLabelUrl(waybillNumber, true, pdfSize);
            URL url = new URL(labelUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");  // FIXED: API returns JSON with S3 link, not raw PDF

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                // Response is JSON containing S3 PDF link
                String response = readResponse(conn);
                logger.info("Delhivery label response: {}", response);
                
                try {
                    JSONObject responseJson = new JSONObject(response);
                    
                    // Extract S3 PDF link from response (multiple field name possibilities)
                    String s3Url = responseJson.optString("pdf_link");
                    if (s3Url == null || s3Url.isEmpty()) {
                        s3Url = responseJson.optString("url");
                    }
                    if (s3Url == null || s3Url.isEmpty()) {
                        s3Url = responseJson.optString("link");
                    }
                    
                    if (s3Url != null && !s3Url.isEmpty()) {
                        logger.info("Found S3 PDF URL, downloading from: {}...", s3Url.substring(0, Math.min(50, s3Url.length())));
                        byte[] pdfData = downloadFromUrl(s3Url);
                        logger.info("Successfully downloaded label from S3 for waybill: {}", waybillNumber);
                        return pdfData;
                    } else {
                        logger.error("No PDF link found in Delhivery response: {}", responseJson.toString(2));
                        throw new RuntimeException("No PDF link in Delhivery response");
                    }
                } catch (org.json.JSONException e) {
                    logger.error("Failed to parse JSON response as label data. Response: {}", response, e);
                    throw new RuntimeException("Delhivery returned invalid JSON: " + e.getMessage());
                }
            } else {
                String errorResponse = readResponse(conn);
                logger.error("Failed to download label. Response code: {}, Response: {}", responseCode, errorResponse);
                throw new RuntimeException("Label download failed with code: " + responseCode + " - " + errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error downloading shipping label for waybill: {}", waybillNumber, e);
            throw e;
        }
    }

    /**
     * Get shipping label as JSON for custom rendering.
     * The JSON response should be rendered into HTML using encoding 128.
     * 
     * @param waybillNumber Waybill number
     * @return JSONObject containing label data for custom rendering
     * @throws Exception if API call fails
     */
    public JSONObject getShippingLabelJson(String waybillNumber) throws Exception {
        try {
            logger.info("Fetching shipping label JSON for waybill: {}", waybillNumber);
            
            String labelUrl = getShippingLabelUrl(waybillNumber, false, null);
            URL url = new URL(labelUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200) {
                logger.info("Successfully retrieved label JSON for waybill: {}", waybillNumber);
                return new JSONObject(response);
            } else {
                logger.error("Failed to get label JSON. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Label JSON fetch failed with code: " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Error fetching label JSON for waybill: {}", waybillNumber, e);
            throw e;
        }
    }

    /**
     * Helper method to download content from a URL (used for S3 links)
     */
    private byte[] downloadFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return conn.getInputStream().readAllBytes();
        } else {
            throw new RuntimeException("Download failed from URL: " + urlString);
        }
    }

    /**
     * Track shipment using waybill number.
     * Production endpoint: /api/v1/packages/json/
     * Can track up to 50 waybills (comma-separated) in a single request.
     * 
     * @param waybillNumber Delhivery waybill number
     * @return JSONObject containing tracking details and scan history
     * @throws Exception if API call fails
     */
    public JSONObject trackShipment(String waybillNumber) throws Exception {
        return trackShipment(waybillNumber, null);
    }

    /**
     * Track shipment using waybill number and/or order ID.
     * 
     * @param waybillNumber Delhivery waybill number (can be comma-separated for multiple)
     * @param orderId Optional order ID (ref_ids)
     * @return JSONObject containing tracking details and scan history
     * @throws Exception if API call fails
     */
    public JSONObject trackShipment(String waybillNumber, String orderId) throws Exception {
        try {
            logger.info("Tracking Delhivery shipment: {} (orderId: {})", waybillNumber, orderId);
            
            StringBuilder endpoint = new StringBuilder(apiUrl);
            endpoint.append(SHIPMENT_TRACK_ENDPOINT);
            endpoint.append("?waybill=").append(URLEncoder.encode(waybillNumber, StandardCharsets.UTF_8));
            
            if (orderId != null && !orderId.isEmpty()) {
                endpoint.append("&ref_ids=").append(URLEncoder.encode(orderId, StandardCharsets.UTF_8));
            }
            
            URL url = new URL(endpoint.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            logger.info("========== DELHIVERY SHIPMENT CREATE RESPONSE ==========");
            logger.info("Response Code: {}", responseCode);
            logger.info("Raw Response: {}", response);

            if (responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                logger.info("Shipment tracking retrieved for: {}", waybillNumber);
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
     * Track multiple shipments at once (up to 50).
     * 
     * @param waybillNumbers List of waybill numbers
     * @return JSONObject containing tracking details for all shipments
     * @throws Exception if API call fails
     */
    public JSONObject trackMultipleShipments(java.util.List<String> waybillNumbers) throws Exception {
        if (waybillNumbers == null || waybillNumbers.isEmpty()) {
            throw new IllegalArgumentException("Waybill numbers list cannot be empty");
        }
        if (waybillNumbers.size() > 50) {
            throw new IllegalArgumentException("Cannot track more than 50 waybills at once");
        }
        
        String waybillsCsv = String.join(",", waybillNumbers);
        return trackShipment(waybillsCsv, null);
    }

    /**
     * Check pincode serviceability for Delhivery delivery.
     * Production endpoint: /c/api/pin-codes/json/
     * 
     * If response is empty list, pincode is non-serviceable (NSZ).
     * If remark is "Embargo", pincode is temporarily non-serviceable.
     * Blank remark means pincode is serviceable.
     * 
     * @param pincode Destination pincode
     * @return JSONObject containing serviceability info
     * @throws Exception if API call fails or pincode not serviceable
     */
    public JSONObject checkPincodeAvailability(String pincode) throws Exception {
        try {
            logger.info("Checking Delhivery serviceability for pincode: {}", pincode);
            
            // Debug: Check if token is loaded
            if (apiToken == null || apiToken.isEmpty() || apiToken.startsWith("${")) {
                logger.error("Delhivery API token is not configured! Token value: {}", 
                            apiToken == null ? "null" : (apiToken.startsWith("${") ? apiToken : "***"));
                throw new RuntimeException("Delhivery API token is not configured. Check DELHIVERY_API_TOKEN environment variable.");
            }
            
            // Handle case where token already has "Token " prefix
            String authHeader = apiToken.trim().startsWith("Token ") ? apiToken.trim() : "Token " + apiToken.trim();
            
            String endpoint = apiUrl + PINCODE_CHECK_ENDPOINT + "?filter_codes=" + pincode;
            logger.info("Pincode check URL: {}", endpoint);
            logger.debug("Auth header: {}", authHeader.substring(0, Math.min(15, authHeader.length())) + "...");
            
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10000); // 10 second timeout
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            
            logger.info("Delhivery pincode API response code: {}, response: {}", responseCode, response);
            
            // Check if we got HTML instead of JSON (indicates wrong endpoint or auth issue)
            if (response != null && response.trim().startsWith("<")) {
                logger.error("Received HTML response instead of JSON for pincode {}. URL: {}", pincode, endpoint);
                throw new RuntimeException("Delhivery API returned HTML instead of JSON - check API URL and token configuration");
            }

            if (responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                logger.info("Parsed JSON response: {}", responseJson.toString());
                
                // Check if delivery_codes array exists and has data
                JSONArray deliveryCodes = responseJson.optJSONArray("delivery_codes");
                int deliveryCodesLength = deliveryCodes != null ? deliveryCodes.length() : 0;
                logger.info("delivery_codes array exists: {}, length: {}", deliveryCodes != null, deliveryCodesLength);
                
                // CRITICAL: If delivery_codes is empty or null, pincode is NOT serviceable
                if (deliveryCodes == null || deliveryCodesLength == 0) {
                    logger.warn("Pincode {} is non-serviceable (NSZ) - delivery_codes is empty or missing", pincode);
                    throw new RuntimeException("Pincode " + pincode + " is not serviceable by Delhivery");
                }
                
                JSONObject firstEntry = deliveryCodes.getJSONObject(0);
                logger.info("First entry in delivery_codes: {}", firstEntry.toString());
                
                JSONObject pincodeData = firstEntry.optJSONObject("postal_code");
                logger.info("postal_code object: {}", pincodeData);
                
                if (pincodeData == null) {
                    logger.warn("Pincode {} - postal_code object is missing in response", pincode);
                    throw new RuntimeException("Pincode " + pincode + " is not serviceable - invalid response structure");
                }
                
                // Note: Delhivery uses "remarks" (plural), not "remark"
                String remark = pincodeData.optString("remarks", "");
                logger.info("Pincode {} remark: '{}'", pincode, remark);
                
                if ("Embargo".equalsIgnoreCase(remark)) {
                    logger.warn("Pincode {} is temporarily non-serviceable (Embargo)", pincode);
                    throw new RuntimeException("Pincode " + pincode + " is temporarily non-serviceable (Embargo)");
                }
                
                logger.info("Pincode {} is serviceable", pincode);
                return pincodeData;
                
            } else {
                logger.error("Failed to check pincode. Response code: {}, Response: {}", responseCode, response);
                throw new RuntimeException("Pincode check failed: " + response);
            }
        } catch (Exception e) {
            logger.error("Error checking pincode serviceability: {}", pincode, e);
            throw e;
        }
    }

    /**
     * Check if a pincode is serviceable (returns boolean instead of throwing exception).
     * 
     * @param pincode Destination pincode
     * @return true if serviceable, false otherwise
     */
    public boolean isPincodeServiceable(String pincode) {
        try {
            checkPincodeAvailability(pincode);
            return true;
        } catch (Exception e) {
            logger.debug("Pincode {} is not serviceable: {}", pincode, e.getMessage());
            return false;
        }
    }

    /**
     * Get all serviceable pincodes (without filter, returns both serviceable and embargoed).
     * Note: This can return a large dataset.
     * 
     * @return JSONObject containing all pincode data
     * @throws Exception if API call fails
     */
    public JSONObject getAllPincodes() throws Exception {
        try {
            logger.info("Fetching all Delhivery pincodes");
            
            String endpoint = apiUrl + PINCODE_CHECK_ENDPOINT;
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);

            if (responseCode == 200) {
                return new JSONObject(response);
            } else {
                logger.error("Failed to fetch pincodes. Response code: {}", responseCode);
                throw new RuntimeException("Failed to fetch pincodes: " + response);
            }
        } catch (Exception e) {
            logger.error("Error fetching all pincodes", e);
            throw e;
        }
    }

    /**
     * Get shipment status by waybill.
     * Extracts the current status from tracking data.
     * 
     * @param waybillNumber Waybill number
     * @return Current status of shipment
     * @throws Exception if API call fails
     */
    public String getShipmentStatus(String waybillNumber) throws Exception {
        JSONObject trackingData = trackShipment(waybillNumber);
        
        // Navigate through the response structure
        if (trackingData.has("ShipmentData")) {
            JSONArray shipmentData = trackingData.optJSONArray("ShipmentData");
            if (shipmentData != null && shipmentData.length() > 0) {
                JSONObject shipment = shipmentData.getJSONObject(0).optJSONObject("Shipment");
                if (shipment != null) {
                    return shipment.optString("Status", "UNKNOWN");
                }
            }
        }
        
        return trackingData.optString("status", "UNKNOWN");
    }

    /**
     * Get detailed scan history for a shipment.
     * 
     * @param waybillNumber Waybill number
     * @return JSONArray containing all scan events
     * @throws Exception if API call fails
     */
    public JSONArray getShipmentScans(String waybillNumber) throws Exception {
        JSONObject trackingData = trackShipment(waybillNumber);
        
        if (trackingData.has("ShipmentData")) {
            JSONArray shipmentData = trackingData.optJSONArray("ShipmentData");
            if (shipmentData != null && shipmentData.length() > 0) {
                JSONObject shipment = shipmentData.getJSONObject(0).optJSONObject("Shipment");
                if (shipment != null) {
                    return shipment.optJSONArray("Scans");
                }
            }
        }
        
        return new JSONArray();
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
