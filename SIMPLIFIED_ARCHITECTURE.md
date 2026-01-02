# Simplified E-Commerce Payment System - Architecture

## Overview
Simplified payment system removing stock tracking and complex coupon validation. Focus on Razorpay integration with basic product availability and flat discount coupons.

---

## Key Changes from Complex to Simplified

### 1. Product Availability (Instead of Stock Tracking)
**Before:** `stockQuantity`, `reservedQuantity`, @Version optimistic locking, concurrent conflicts
**After:** Single boolean `isAvailable` (true/false)
- Admin can enable/disable product
- No stock limits
- No concurrent issues
- Eliminates all stock overselling scenarios

### 2. Coupon System (Instead of Complex Validation)
**Before:** Coupon-Product eligibility matrix, usedCount, maxUsageLimit, @Retryable retry logic
**After:** Simple flat discount
- `discountAmount` field (e.g., 100 rupees)
- All products automatically eligible for all coupons
- No usage limits
- No concurrent conflicts
- No ProductCoupon mapping table needed

### 3. No Reservation System
**Before:** Reserve stock before payment, deduct after success, release on failure
**After:** No reservation at all
- Just check if product `isAvailable` at checkout
- No state tracking
- No cleanup needed

---

## Simplified Flow

### Checkout Flow
```
1. User adds to cart → Create CartItem (no stock check)
2. User proceeds to checkout:
   ├─ Validate cart not empty
   ├─ Check each product isAvailable = true
   ├─ Check product price hasn't changed
   ├─ Calculate total (with coupon discount if applied)
   ├─ Apply coupon (no limit checks needed)
   └─ Create Order with PAYMENT_PENDING status

3. Create Razorpay order and send to frontend
```

### Payment Success (Webhook)
```
1. Receive webhook: payment.completed
2. Verify signature
3. Check if already processed (idempotency)
4. Update order status → CONFIRMED
5. Clear user's cart
6. Response: Success
```

### Payment Failure (Webhook)
```
1. Receive webhook: payment.failed
2. Update order status → CANCELLED
3. Keep cart items (user can retry)
4. Response: Failure acknowledged
```

### Webhook Failure Recovery (Scheduler)
```
Every 1 hour:
1. Find all PAYMENT_PENDING orders > 1 hour old
2. For each order:
   ├─ Query Razorpay API: Get order status
   ├─ If payment captured (not received webhook):
   │  ├─ Update order → CONFIRMED
   │  ├─ Clear user's cart
   │  └─ Log: "Webhook failed, fixed by scheduler"
   └─ If payment not captured:
      ├─ Update order → CANCELLED
      └─ Log: "Payment not made, order cancelled"
```

---

## Removed Components

1. ✅ **Stock Reservation Model** - No longer needed
2. ✅ **@Version Optimistic Locking** - No concurrent stock updates
3. ✅ **PaymentCompensationService** - No complex compensation needed
4. ✅ **ProductCoupon Entity** - All products eligible for all coupons
5. ✅ **Coupon Limit Validation** - No usage tracking
6. ✅ **@Retryable** - No concurrent conflicts to retry
7. ✅ **Nested Try-Catch Logic** - Simplified error handling
8. ✅ **Stock Deduction/Release Logic** - Just check isAvailable

---

## Kept Components

1. ✅ **Razorpay Integration** - Core payment processing
2. ✅ **Webhook Handling** - Success/failure callbacks
3. ✅ **ReservationCleanupScheduler** - Renamed to PaymentReconciliationScheduler
   - Runs every 1 hour (was 10 min)
   - Only checks payment status, no stock cleanup
   - Confirms orders with captured payments but missing webhooks
4. ✅ **Basic Validation** - Cart not empty, product available, price check
5. ✅ **Coupon Discount** - Simple flat amount deduction

---

## Database Changes

### Product Entity
```java
@Entity
public class Product {
    Long id;
    String name;
    Double price;
    Double originalPrice;
    Boolean isAvailable;  // Only this field for availability
    Boolean isEligibleForCoupon = true;  // Can be removed entirely
    // Removed: stockQuantity, reservedQuantity, @Version
}
```

### Order Entity
```java
@Entity
public class Order {
    Long id;
    Long userId;
    OrderStatus status;  // PAYMENT_PENDING, CONFIRMED, CANCELLED
    Long appliedCouponId;
    // Removed: @Version
}
```

### Coupon Entity
```java
@Entity
public class Coupon {
    Long id;
    String couponCode;
    Double discountAmount;  // Flat discount (e.g., 100)
    // Removed: usedCount, maxUsageLimit, @Version, CouponType
}
```

### Deleted Tables
- `ProductCoupon` - No longer needed

---

## Error Handling (Simplified)

Instead of complex nested try-catch with compensation:

```
Checkout fails:
└─ Return error message, keep cart

Payment webhook fails:
└─ Order stays PAYMENT_PENDING, scheduler will fix

Scheduler detects payment captured:
└─ Confirms order, clears cart

Scheduler detects no payment:
└─ Cancels order, keeps cart
```

---

## Benefits of Simplification

1. **No Stock Conflicts** - No concurrent updates to handle
2. **No Coupon Conflicts** - No usage tracking or limits
3. **Reduced Code** - ~40% less code (from 5430 to ~3200 lines)
4. **Easier Testing** - No complex transaction scenarios
5. **Faster Development** - Focus on core features
6. **Production Ready** - Still handles webhook failures via scheduler

---

## Edge Cases Handled

1. ✅ Webhook success received and processed
2. ✅ Webhook success lost, scheduler detects and confirms
3. ✅ Webhook failure received and processed
4. ✅ Product disabled during checkout (isAvailable = false)
5. ✅ Price changed during checkout (price validation)
6. ✅ Cart modified during payment (existing behavior)
7. ✅ Idempotency - duplicate webhooks ignored
8. ✅ Scheduler recovery for missing webhooks (every 1 hour)

---

## Migration Notes

When deploying:
1. Backup database
2. Remove stock from product_inventory
3. Add isAvailable boolean column
4. Delete product_coupon table
5. Remove coupon_used_count, coupon_max_usage from coupons table
6. Update scheduler interval from 10 min to 60 min
7. Deploy new code
