package com.snackecommerce.delivery.service;

import com.snackecommerce.common.exception.OrderNotFoundException;
import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.delivery.dto.TrackingResponse;
import com.snackecommerce.delivery.util.DelhiveryUtil;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.enums.TrackingAgent;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.user.entity.Address;
import com.snackecommerce.user.repository.AddressRepository;
import com.snackecommerce.notification.service.NotificationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Delivery Service for managing Delhivery shipments and tracking
 */
@Service
@Transactional
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private DelhiveryUtil delhiveryUtil;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create shipment on Delhivery for an order
     * 
     * @param orderId Order ID
     * @return Waybill number
     * @throws Exception if shipment creation fails
     */
    public String createShipment(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        // Get address details from Address entity
        Address address = addressRepository.findById(order.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found for order: " + orderId));

        // Check if pincode is serviceable
        if (address.getZipCode() != null) {
            PincodeAvailabilityResponse pincodeCheck = checkPincodeAvailability(address.getZipCode());
            if (!pincodeCheck.getIsAvailable()) {
                throw new RuntimeException("Pincode " + address.getZipCode() + " is not serviceable");
            }
        }

        // Build proper shipment payload with address from Address entity
        String fullAddress = address.getAddressLine1();
        if (address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()) {
            fullAddress += ", " + address.getAddressLine2();
        }
        
        JSONObject shipmentRequest = delhiveryUtil.buildShipmentPayload(
                order.getOrderNumber(),
                fullAddress,
                address.getCity(),
                address.getZipCode(),
                address.getState(),
                address.getCountry(),
                null,  // landmark (not in Address entity)
                address.getPhoneNumber(),
                address.getFullName()
        );

        try {
            JSONObject response = delhiveryUtil.createShipment(shipmentRequest);
            
            // Extract waybill number from response
            String waybill = delhiveryUtil.extractWaybillNumber(response);

            if (waybill == null || waybill.isEmpty()) {
                throw new RuntimeException("No waybill received from Delhivery");
            }

            // Get label URL for on-the-fly download
            String labelUrl = delhiveryUtil.getShippingLabelUrl(waybill);

            // Update order with waybill, tracking agent, and label URL
            order.setTrackingNumber(waybill);
            order.setTrackingAgent(TrackingAgent.DELHIVERY);
            order.setShippingLabelUrl(labelUrl);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            logger.info("Shipment created for order {} with waybill: {} and label URL: {}", 
                       orderId, waybill, labelUrl);

            // Send notification to user: Shipment created with tracking
            try {
                notificationService.notifyShipmentCreated(
                    order.getUserId(),
                    order.getId(),
                    order.getOrderNumber(),
                    waybill,
                    labelUrl
                );
                logger.info("Notification sent to user {} for shipment created", order.getUserId());
            } catch (Exception e) {
                logger.error("Failed to send shipment notification: {}", e.getMessage());
            }

            return waybill;
        } catch (Exception e) {
            logger.error("Failed to create shipment for order {}", orderId, e);
            throw e;
        }
    }

    /**
     * Get tracking information for an order
     * 
     * @param orderId Order ID
     * @return Tracking details
     * @throws Exception if tracking fails
     */
    public TrackingResponse trackOrder(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getTrackingNumber() == null || order.getTrackingNumber().isEmpty()) {
            throw new RuntimeException("No tracking number available for this order");
        }

        try {
            JSONObject trackingData = delhiveryUtil.trackShipment(order.getTrackingNumber());

            TrackingResponse response = TrackingResponse.builder()
                    .orderId(orderId)
                    .waybillNumber(order.getTrackingNumber())
                    .currentStatus(trackingData.optString("status", "UNKNOWN"))
                    .location(trackingData.optString("current_location", ""))
                    .lastUpdate(trackingData.optString("last_update", ""))
                    .isDelivered("DELIVERED".equals(trackingData.optString("status", "")))
                    .estimatedDeliveryDate(trackingData.optString("estimated_delivery_date", ""))
                    .build();

            // Update order status if delivered
            if (response.getIsDelivered() && !order.getStatus().equals(OrderStatus.DELIVERED)) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setDeliveredAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }

            logger.info("Order {} tracking status: {}", orderId, response.getCurrentStatus());
            return response;
        } catch (Exception e) {
            logger.error("Failed to track order {}", orderId, e);
            throw e;
        }
    }

    /**
     * Check if pincode is serviceable by Delhivery
     * 
     * @param pincode Delivery pincode
     * @return Pincode availability details
     * @throws Exception if check fails
     */
    public PincodeAvailabilityResponse checkPincodeAvailability(String pincode) throws Exception {
        try {
            JSONObject pincodeData = delhiveryUtil.checkPincodeAvailability(pincode);

            PincodeAvailabilityResponse response = PincodeAvailabilityResponse.builder()
                    .pincode(pincode)
                    .isAvailable("SERVICEABLE".equals(pincodeData.optString("status", "")))
                    .status(pincodeData.optString("status", "UNKNOWN"))
                    .estimatedDeliveryDays((double) pincodeData.optInt("days", 0))
                    .region(pincodeData.optString("region", ""))
                    .state(pincodeData.optString("state", ""))
                    .build();

            logger.info("Pincode {} availability: {}", pincode, response.getStatus());
            return response;
        } catch (Exception e) {
            logger.error("Failed to check pincode availability: {}", pincode, e);
            throw e;
        }
    }

    /**
     * Handle Delhivery webhook updates (called by webhook endpoint)
     * 
     * @param waybillNumber Waybill number
     * @param status New status from Delhivery
     * @param lastLocation Current location
     */
    public void handleDeliveryUpdate(String waybillNumber, String status, String lastLocation) {
        try {
            // Find order by tracking number
            Optional<Order> orderOpt = orderRepository.findByTrackingNumber(waybillNumber);
            if (orderOpt.isEmpty()) {
                logger.warn("No order found for waybill: {}", waybillNumber);
                return;
            }

            Order order = orderOpt.get();

            // Update order status based on delivery status
            if ("DELIVERED".equals(status)) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setDeliveredAt(LocalDateTime.now());
            } else if ("PENDING".equals(status) || "IN_TRANSIT".equals(status)) {
                if (!order.getStatus().equals(OrderStatus.SHIPPED)) {
                    order.setStatus(OrderStatus.SHIPPED);
                }
            } else if ("FAILED".equals(status)) {
                order.setStatus(OrderStatus.CANCELLED);
            }

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            logger.info("Updated order {} status to {} - Location: {}", order.getId(), status, lastLocation);
        } catch (Exception e) {
            logger.error("Error handling delivery update for waybill: {}", waybillNumber, e);
        }
    }

    /**
     * Download shipping label for an order
     * Downloads on-the-fly from Delhivery API based on stored URL
     * 
     * @param orderId Order ID
     * @return PDF bytes of the shipping label
     * @throws Exception if download fails
     */
    public byte[] downloadShippingLabel(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getTrackingNumber() == null || order.getTrackingNumber().isEmpty()) {
            throw new RuntimeException("No tracking number available for this order. Shipment not created yet.");
        }

        try {
            logger.info("Downloading shipping label for order {} with waybill: {}", orderId, order.getTrackingNumber());
            byte[] labelData = delhiveryUtil.downloadShippingLabel(order.getTrackingNumber());
            logger.info("Successfully downloaded label for order: {}", orderId);
            return labelData;
        } catch (Exception e) {
            logger.error("Failed to download shipping label for order: {}", orderId, e);
            throw new RuntimeException("Failed to download shipping label: " + e.getMessage());
        }
    }
}
