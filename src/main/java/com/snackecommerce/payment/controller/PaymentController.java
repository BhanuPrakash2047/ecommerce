package com.snackecommerce.payment.controller;

import com.snackecommerce.common.annotation.RateLimit;
import com.snackecommerce.payment.dto.CreatePaymentRequest;
import com.snackecommerce.payment.dto.PaymentResponse;
import com.snackecommerce.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment Controller for Razorpay integration
 * 
 * Production Level Features:
 * - Payment creation endpoint
 * - Webhook success endpoint
 * - Webhook failure endpoint
 * - Comprehensive error handling
 * - Detailed logging
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * POST /api/payments/create - Create Razorpay order for checkout
     * 
     * Request: {
     *   "orderId": 1,
     *   "amount": 5000,
     *   "email": "user@example.com",
     *   "phone": "9876543210"
     * }
     * 
     * Response: {
     *   "razorpayOrderId": "order_ABC123",
     *   "amount": 5000,
     *   "email": "user@example.com",
     *   "phone": "9876543210"
     * }
     * 
     * Frontend uses razorpayOrderId to initialize Razorpay checkout modal
     */
    @PostMapping("/create")
     @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
     @RateLimit(value = "payment", useAuthenticatedUser = true)
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            logger.info("Payment creation request for order ID: {}, amount: {}", request.getOrderId(), request.getAmount());
            
            PaymentResponse response = paymentService.createPayment(request);
            
            logger.info("Payment created successfully. Razorpay Order ID: {}", response.getRazorpayOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Business logic error during payment creation", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during payment creation", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Payment gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/payments/webhook/success - Razorpay webhook for successful payment
     * 
     * Called by Razorpay after successful payment
     * Signature verification ensures authenticity
     * 
     * Query Params:
     *   - razorpayOrderId: Razorpay Order ID
     *   - razorpayPaymentId: Razorpay Payment ID
     *   - signature: X-Razorpay-Signature value for verification
     * 
     * Response: {
     *   "message": "Payment successful - Order confirmed"
     * }
     */
    @PostMapping("/webhook/success")
    public ResponseEntity<?> handlePaymentSuccess(
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String signature) {
        try {
            logger.info("Payment success webhook received for order: {}", razorpayOrderId);
            
            paymentService.handlePaymentSuccess(razorpayOrderId, razorpayPaymentId, signature);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment successful - Order confirmed");
            logger.info("Payment success processed successfully for order: {}", razorpayOrderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Business logic error during payment success webhook", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during payment success webhook", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/payments/webhook/failure - Razorpay webhook for failed payment
     * 
     * Called by Razorpay after failed payment
     * Stock is released and order is cancelled
     * Cart items are preserved for user to retry
     * 
     * Query Params:
     *   - razorpayOrderId: Razorpay Order ID
     *   - razorpayPaymentId: Razorpay Payment ID (if available)
     * 
     * Response: {
     *   "message": "Payment failed - Order cancelled and stock released"
     * }
     */
    @PostMapping("/webhook/failure")
    public ResponseEntity<?> handlePaymentFailure(
            @RequestParam String razorpayOrderId,
            @RequestParam(required = false) String razorpayPaymentId) {
        try {
            logger.info("Payment failure webhook received for order: {}", razorpayOrderId);
            
            if (razorpayPaymentId == null) {
                razorpayPaymentId = "unknown";
            }
            
            paymentService.handlePaymentFailure(razorpayOrderId, razorpayPaymentId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment failed - Order cancelled and stock released");
            logger.info("Payment failure processed successfully for order: {}", razorpayOrderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Business logic error during payment failure webhook", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during payment failure webhook", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
