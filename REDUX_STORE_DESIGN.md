# Redux Store Design Architecture

## Overview
This document outlines the complete Redux store structure for the Snacky E-commerce application based on the backend API.

---

## 1. REDUX STORE STRUCTURE

```
store/
├── slices/
│   ├── authSlice.js
│   ├── productSlice.js
│   ├── cartSlice.js
│   ├── orderSlice.js
│   ├── paymentSlice.js
│   ├── deliverySlice.js
│   ├── couponSlice.js
│   ├── notificationSlice.js
│   └── addressSlice.js
├── middleware/
│   ├── authThunks.js
│   ├── productThunks.js
│   ├── cartThunks.js
│   ├── orderThunks.js
│   ├── paymentThunks.js
│   ├── deliveryThunks.js
│   ├── couponThunks.js
│   ├── notificationThunks.js
│   └── addressThunks.js
├── index.js (store configuration)
└── api.js (API client setup)
```

---

## 2. STATE SHAPE

### 2.1 Auth Slice State
```typescript
{
  auth: {
    isAuthenticated: boolean,
    loading: boolean,
    error: string | null,
    token: string | null,
    user: {
      id: number | null,
      email: string | null,
      role: "USER" | "ADMIN" | null,
      active: boolean,
      createdAt: string | null
    }
  }
}
```

**Fields:**
- `isAuthenticated`: Tracks if user is logged in
- `loading`: Loading state during auth operations
- `error`: Error message if auth fails
- `token`: JWT token from backend
- `user`: Current user information

**Data Types Match Backend:**
- `id`: Long → number
- `email`: String → string
- `role`: UserRole enum → "USER" | "ADMIN"
- `active`: Boolean → boolean
- `createdAt`: LocalDateTime → ISO string

---

### 2.2 Product Slice State
```typescript
{
  products: {
    loading: boolean,
    error: string | null,
    items: [
      {
        id: number,
        name: string,
        price: string,           // BigDecimal → string to avoid floating point issues
        originalPrice: string,
        isAvailable: boolean,
        isEligibleForCoupon: boolean,
        createdAt: string
      }
    ],
    selectedProduct: {
      id: number,
      name: string,
      price: string,
      originalPrice: string,
      isAvailable: boolean,
      isEligibleForCoupon: boolean,
      createdAt: string,
      reviews: [
        {
          id: number,
          productId: number,
          userId: number,
          userEmail: string,
          rating: number,        // 1-5
          title: string,
          text: string,
          verified: boolean,
          createdAt: string,
          updatedAt: string
        }
      ],
      faqs: [
        {
          id: number,
          productId: number,
          question: string,
          answer: string,
          upvotes: number,
          verified: boolean,
          createdAt: string
        }
      ],
      images: [
        {
          id: number,
          productId: number,
          imageUrl: string,
          displayOrder: number,
          createdAt: string
        }
      ],
      videos: [
        {
          id: number,
          productId: number,
          videoUrl: string,
          displayOrder: number,
          createdAt: string
        }
      ]
    },
    filters: {
      minPrice: number | null,
      maxPrice: number | null,
      searchQuery: string | null
    },
    pagination: {
      page: number,
      pageSize: number,
      totalProducts: number
    }
  }
}
```

**Data Types Match Backend:**
- `price`: BigDecimal → string (to prevent precision loss)
- `originalPrice`: BigDecimal → string
- `isAvailable`: Boolean → boolean
- `rating`: Integer → number (1-5)
- `createdAt`: LocalDateTime → ISO string

---

### 2.3 Cart Slice State
```typescript
{
  cart: {
    loading: boolean,
    error: string | null,
    cartId: number | null,
    userId: number | null,
    items: [
      {
        cartItemId: number,
        productId: number,
        productName: string,
        quantity: number,        // 1-999
        snapshotPrice: string,   // Price when added (BigDecimal)
        currentPrice: string,    // Current price from DB (BigDecimal)
        itemTotal: string,       // quantity × currentPrice (BigDecimal)
        priceChangeAlert: string | null
      }
    ],
    subtotal: string,           // Sum of all items (BigDecimal)
    discountAmount: string,     // Applied coupon discount (BigDecimal)
    total: string,              // subtotal - discount (BigDecimal)
    appliedCouponId: number | null,
    appliedCouponCode: string | null,
    alerts: string[],           // Price changes, deleted products, etc.
    validationErrors: {
      stockErrors: string[],
      unavailableProducts: number[],
      deletedProducts: number[]
    }
  }
}
```

**Data Types Match Backend:**
- `quantity`: Integer → number (1-999)
- All prices: BigDecimal → string
- `createdAt`: LocalDateTime → ISO string

---

### 2.4 Order Slice State
```typescript
{
  orders: {
    loading: boolean,
    error: string | null,
    items: [
      {
        id: number,
        orderNumber: string,
        status: "CREATED" | "PAYMENT_PENDING" | "PAID" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "RETURNED" | "CANCELLED",
        subtotal: string,
        discountAmount: string,
        totalAmount: string,
        appliedCouponId: number | null,
        couponCode: string | null,
        discountType: "PERCENTAGE" | "FLAT" | null,
        discountValue: string | null,
        addressId: number | null,
        receiverName: string,
        receiverPhone: string,
        receiverEmail: string,
        trackingNumber: string | null,
        trackingAgent: string | null,
        items: [
          {
            itemId: number,
            productId: number,
            productName: string,
            unitPrice: string,
            quantity: number,
            subtotal: string
          }
        ],
        createdAt: string,
        updatedAt: string,
        deliveredAt: string | null
      }
    ],
    selectedOrder: {
      // Same structure as above
    },
    pagination: {
      page: number,
      pageSize: number,
      totalOrders: number
    }
  }
}
```

**Data Types Match Backend:**
- `status`: OrderStatus enum → "CREATED" | "PAYMENT_PENDING" | etc
- `discountType`: DiscountType enum → "PERCENTAGE" | "FLAT"
- All prices: BigDecimal → string
- `createdAt`, `updatedAt`, `deliveredAt`: LocalDateTime → ISO string

---

### 2.5 Payment Slice State
```typescript
{
  payment: {
    loading: boolean,
    error: string | null,
    orderId: number | null,
    razorpayOrderId: string | null,
    amount: number,             // In rupees (paise will be in backend)
    email: string,
    phone: string,
    paymentStatus: "PENDING" | "SUCCESS" | "FAILED" | "CANCELLED",
    paymentDetails: {
      razorpayPaymentId: string | null,
      razorpaySignature: string | null,
      paymentMethod: string | null,
      processedAt: string | null
    }
  }
}
```

**Data Types Match Backend:**
- `amount`: Long → number (in rupees)
- `processedAt`: LocalDateTime → ISO string

---

### 2.6 Delivery Slice State
```typescript
{
  delivery: {
    loading: boolean,
    error: string | null,
    tracking: {
      orderId: number | null,
      waybillNumber: string | null,
      currentStatus: "IN_TRANSIT" | "DELIVERED" | "FAILED" | "PENDING" | null,
      location: string | null,
      lastUpdate: string | null,
      isDelivered: boolean,
      estimatedDeliveryDate: string | null
    },
    pincodeAvailability: {
      pincode: string,
      isAvailable: boolean,
      status: "SERVICEABLE" | "NON_SERVICEABLE" | "PARTIALLY_SERVICEABLE" | null,
      estimatedDeliveryDays: number | null,
      region: string | null,
      state: string | null
    }
  }
}
```

**Data Types Match Backend:**
- `currentStatus`: String (from backend) → enum string
- `estimatedDeliveryDate`: String (LocalDateTime) → ISO string
- `lastUpdate`: String (LocalDateTime) → ISO string

---

### 2.7 Coupon Slice State
```typescript
{
  coupons: {
    loading: boolean,
    error: string | null,
    availableCoupons: [
      {
        id: number,
        code: string,
        type: "PERCENTAGE" | "FLAT",
        discountValue: string,  // BigDecimal
        minOrderAmount: string, // BigDecimal
        active: boolean,
        validFrom: string,      // LocalDateTime → ISO string
        validTill: string,      // LocalDateTime → ISO string
      }
    ],
    eligibleCoupons: [
      // Same structure as availableCoupons
    ],
    appliedCoupon: {
      id: number,
      code: string,
      type: "PERCENTAGE" | "FLAT",
      discountValue: string,
      minOrderAmount: string,
      active: boolean,
      validFrom: string,
      validTill: string,
      calculatedDiscount: string // For display
    }
  }
}
```

**Data Types Match Backend:**
- `type`: CouponType enum → "PERCENTAGE" | "FLAT"
- `discountValue`: BigDecimal → string
- `minOrderAmount`: BigDecimal → string
- `validFrom`, `validTill`: LocalDateTime → ISO string

---

### 2.8 Notification Slice State
```typescript
{
  notifications: {
    loading: boolean,
    error: string | null,
    items: [
      {
        id: number,
        userId: number,
        title: string,
        message: string,
        type: "PAYMENT_RECEIVED" | "SHIPMENT_CREATED" | "ORDER_DELIVERED" | "ADMIN_SHIPMENT_FAILED",
        relatedEntityType: "ORDER" | "SHIPMENT_JOB" | null,
        relatedEntityId: number | null,
        isRead: boolean,
        metadata: any,          // JSON object
        createdAt: string,
        readAt: string | null
      }
    ],
    unreadCount: number,
    pagination: {
      page: number,
      pageSize: number,
      totalNotifications: number
    }
  }
}
```

**Data Types Match Backend:**
- `type`: NotificationType enum → "PAYMENT_RECEIVED" | "SHIPMENT_CREATED" | etc
- `relatedEntityType`: String → enum string
- `createdAt`, `readAt`: LocalDateTime → ISO string
- `metadata`: JSON string from backend → parsed JS object

---

### 2.9 Address Slice State
```typescript
{
  addresses: {
    loading: boolean,
    error: string | null,
    items: [
      {
        id: number,
        userId: number,
        fullName: string,
        phoneNumber: string,
        addressLine1: string,
        addressLine2: string | null,
        city: string,
        state: string,
        zipCode: string,
        country: string,
        isDefault: boolean,
        pincodeReachable: boolean | null,
        lastCheckedAt: string | null,
        createdAt: string
      }
    ],
    selectedAddressId: number | null,
    defaultAddress: {
      // Same structure as address item
    }
  }
}
```

**Data Types Match Backend:**
- `pincodeReachable`: Boolean → boolean
- `lastCheckedAt`: LocalDateTime → ISO string
- `createdAt`: LocalDateTime → ISO string

---

## 3. ASYNC THUNKS (Middleware)

### 3.1 Auth Thunks

```typescript
// authThunks.js

// Types matching LoginRequest
export const loginUser = createAsyncThunk(
  'auth/login',
  async ({ email, password }, { rejectWithValue }) => {
    // POST /api/auth/login
    // Request: { email: string, password: string }
    // Response: JwtResponse { token, user }
    return response; // Returns { token, user }
  }
);

// Types matching RegisterRequest
export const registerUser = createAsyncThunk(
  'auth/register',
  async ({ email, password, role }, { rejectWithValue }) => {
    // POST /api/auth/register
    // Request: { email: string, password: string, role?: "USER" | "ADMIN" }
    // Response: JwtResponse { token, user }
    return response;
  }
);

export const getProfile = createAsyncThunk(
  'auth/getProfile',
  async (_, { rejectWithValue }) => {
    // GET /api/auth/profile
    // Response: UserResponse
    return response;
  }
);

export const updateProfile = createAsyncThunk(
  'auth/updateProfile',
  async (userUpdate, { rejectWithValue }) => {
    // PUT /api/auth/profile
    // Request: UserResponse updates
    // Response: UserResponse
    return response;
  }
);

export const logout = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    // Optional backend logout endpoint
    // Clear local token here
    return null;
  }
);
```

**Request/Response Types:**
- `LoginRequest`: { email: string, password: string }
- `RegisterRequest`: { email: string, password: string, role?: UserRole }
- `JwtResponse`: { token: string, user: UserResponse }
- `UserResponse`: { id: number, email: string, role: UserRole, active: boolean, createdAt: string }

---

### 3.2 Product Thunks

```typescript
// productThunks.js

export const fetchAllProducts = createAsyncThunk(
  'product/fetchAll',
  async ({ page = 0, pageSize = 10 }, { rejectWithValue }) => {
    // GET /api/products
    // Response: List<ProductResponse>
    return response;
  }
);

export const fetchProductById = createAsyncThunk(
  'product/fetchById',
  async (productId, { rejectWithValue }) => {
    // GET /api/products/{productId}
    // Response: ProductResponse with reviews, FAQs, images, videos
    return response;
  }
);

export const filterProductsByPrice = createAsyncThunk(
  'product/filterByPrice',
  async ({ minPrice, maxPrice, page = 0, pageSize = 10 }, { rejectWithValue }) => {
    // GET /api/products/filter/price?minPrice=X&maxPrice=Y
    // Response: List<ProductResponse>
    return response;
  }
);

export const searchProducts = createAsyncThunk(
  'product/search',
  async (searchQuery, { rejectWithValue }) => {
    // GET /api/products/search?query=X
    // Response: List<ProductResponse>
    return response;
  }
);

export const fetchProductReviews = createAsyncThunk(
  'product/fetchReviews',
  async (productId, { rejectWithValue }) => {
    // GET /api/products/{productId}/reviews
    // Response: List<ReviewResponse>
    return response;
  }
);

export const addReview = createAsyncThunk(
  'product/addReview',
  async ({ productId, reviewData }, { rejectWithValue }) => {
    // POST /api/products/{productId}/reviews
    // Request: ReviewRequest { rating, title, text }
    // Response: ReviewResponse
    return response;
  }
);
```

**Request/Response Types:**
- `ProductResponse`: { id, name, price, originalPrice, isAvailable, isEligibleForCoupon, createdAt }
- `ReviewRequest`: { rating: number, title: string, text: string }
- `ReviewResponse`: { id, productId, userId, userEmail, rating, title, text, verified, createdAt, updatedAt }

---

### 3.3 Cart Thunks

```typescript
// cartThunks.js

export const fetchCart = createAsyncThunk(
  'cart/fetch',
  async (_, { rejectWithValue }) => {
    // GET /api/cart
    // Response: CartResponse
    return response;
  }
);

// Types matching AddToCartRequest
export const addToCart = createAsyncThunk(
  'cart/addItem',
  async ({ productId, quantity }, { rejectWithValue }) => {
    // POST /api/cart/items
    // Request: { productId: number, quantity: number }
    // Response: CartResponse
    return response;
  }
);

export const updateCartItemQuantity = createAsyncThunk(
  'cart/updateQuantity',
  async ({ cartItemId, quantity }, { rejectWithValue }) => {
    // PUT /api/cart/items/{cartItemId}
    // Request: { quantity: number }
    // Response: CartResponse
    return response;
  }
);

export const removeFromCart = createAsyncThunk(
  'cart/removeItem',
  async (cartItemId, { rejectWithValue }) => {
    // DELETE /api/cart/items/{cartItemId}
    // Response: CartResponse
    return response;
  }
);

export const applyCoupon = createAsyncThunk(
  'cart/applyCoupon',
  async (couponId, { rejectWithValue }) => {
    // POST /api/cart/coupons
    // Request: { couponId: number }
    // Response: CartResponse with applied coupon info
    return response;
  }
);

export const removeCoupon = createAsyncThunk(
  'cart/removeCoupon',
  async (_, { rejectWithValue }) => {
    // DELETE /api/cart/coupons
    // Response: CartResponse without coupon
    return response;
  }
);

export const validateCart = createAsyncThunk(
  'cart/validate',
  async (_, { rejectWithValue }) => {
    // POST /api/cart/validate
    // Response: CheckoutValidationResponse
    return response;
  }
);

export const clearCart = createAsyncThunk(
  'cart/clear',
  async (_, { rejectWithValue }) => {
    // DELETE /api/cart
    // Response: { message: "Cart cleared" }
    return response;
  }
);
```

**Request/Response Types:**
- `AddToCartRequest`: { productId: number, quantity: number }
- `CartResponse`: { cartId, userId, items[], subtotal, discountAmount, total, appliedCouponId, appliedCouponCode, alerts[] }
- `CartItemResponse`: { cartItemId, productId, productName, quantity, snapshotPrice, currentPrice, itemTotal, priceChangeAlert }

---

### 3.4 Order Thunks

```typescript
// orderThunks.js

export const fetchUserOrders = createAsyncThunk(
  'order/fetchUser',
  async (_, { rejectWithValue }) => {
    // GET /api/orders
    // Response: List<OrderListResponse>
    return response;
  }
);

export const fetchOrderDetails = createAsyncThunk(
  'order/fetchDetails',
  async (orderId, { rejectWithValue }) => {
    // GET /api/orders/{orderId}
    // Response: OrderResponse
    return response;
  }
);

export const createOrder = createAsyncThunk(
  'order/create',
  async (checkoutData, { rejectWithValue }) => {
    // POST /api/cart/checkout
    // Request: { addressId: number, appliedCouponId?: number }
    // Response: OrderResponse
    return response;
  }
);

export const cancelOrder = createAsyncThunk(
  'order/cancel',
  async (orderId, { rejectWithValue }) => {
    // PUT /api/orders/{orderId}/cancel
    // Response: OrderResponse with CANCELLED status
    return response;
  }
);

export const returnOrder = createAsyncThunk(
  'order/return',
  async (orderId, { rejectWithValue }) => {
    // POST /api/orders/{orderId}/return
    // Response: OrderResponse with RETURNED status
    return response;
  }
);
```

**Request/Response Types:**
- `OrderResponse`: { id, orderNumber, status, items[], subtotal, discountAmount, totalAmount, appliedCouponId, couponCode, discountType, discountValue, addressId, receiverName, receiverPhone, receiverEmail, trackingNumber, trackingAgent, createdAt, updatedAt, deliveredAt }
- `OrderItemResponse`: { itemId, productId, productName, unitPrice, quantity, subtotal }

---

### 3.5 Payment Thunks

```typescript
// paymentThunks.js

// Types matching CreatePaymentRequest
export const createRazorpayOrder = createAsyncThunk(
  'payment/createOrder',
  async ({ orderId, amount, email, phone }, { rejectWithValue }) => {
    // POST /api/payments/create
    // Request: { orderId: number, amount: number, email: string, phone: string }
    // Response: PaymentResponse
    return response;
  }
);

export const verifyPayment = createAsyncThunk(
  'payment/verify',
  async ({ razorpayOrderId, razorpayPaymentId, razorpaySignature }, { rejectWithValue }) => {
    // POST /api/payments/webhook/success
    // Request: Razorpay webhook data
    // Response: { message: "Payment verified" }
    return response;
  }
);

export const handlePaymentFailure = createAsyncThunk(
  'payment/failure',
  async ({ orderId, error }, { rejectWithValue }) => {
    // POST /api/payments/webhook/failure
    // Response: { message: "Payment failed" }
    return response;
  }
);
```

**Request/Response Types:**
- `CreatePaymentRequest`: { orderId: number, amount: number, email: string, phone: string }
- `PaymentResponse`: { razorpayOrderId: string, amount: number, email: string, phone: string, orderId: string }

---

### 3.6 Delivery Thunks

```typescript
// deliveryThunks.js

export const trackOrder = createAsyncThunk(
  'delivery/track',
  async (orderId, { rejectWithValue }) => {
    // GET /api/orders/{orderId}/track
    // Response: TrackingResponse
    return response;
  }
);

export const checkPincodeAvailability = createAsyncThunk(
  'delivery/checkPincode',
  async (pincode, { rejectWithValue }) => {
    // GET /api/delivery/pincode?pincode=X
    // Response: PincodeAvailabilityResponse
    return response;
  }
);

export const downloadShippingLabel = createAsyncThunk(
  'delivery/downloadLabel',
  async (orderId, { rejectWithValue }) => {
    // GET /api/orders/{orderId}/shipping-label
    // Response: PDF byte array
    return response; // blob
  }
);
```

**Request/Response Types:**
- `TrackingResponse`: { orderId, waybillNumber, currentStatus, location, lastUpdate, isDelivered, estimatedDeliveryDate }
- `PincodeAvailabilityResponse`: { pincode, isAvailable, status, estimatedDeliveryDays, region, state }

---

### 3.7 Coupon Thunks

```typescript
// couponThunks.js

export const fetchAvailableCoupons = createAsyncThunk(
  'coupon/fetchAvailable',
  async (_, { rejectWithValue }) => {
    // GET /api/coupons/available
    // Response: List<CouponResponse>
    return response;
  }
);

export const getEligibleCoupons = createAsyncThunk(
  'coupon/getEligible',
  async (_, { rejectWithValue }) => {
    // GET /api/cart/coupons/eligible
    // Response: EligibleCouponsResponse { eligibleCoupons[] }
    return response;
  }
);

export const validateCoupon = createAsyncThunk(
  'coupon/validate',
  async (couponCode, { rejectWithValue }) => {
    // POST /api/coupons/validate
    // Request: { code: string }
    // Response: CouponValidationResponse
    return response;
  }
);
```

**Request/Response Types:**
- `CouponResponse`: { id, code, type, discountValue, minOrderAmount, active, validFrom, validTill }
- `CouponValidationResponse`: { isValid: boolean, message: string, coupon?: CouponResponse }

---

### 3.8 Notification Thunks

```typescript
// notificationThunks.js

export const fetchNotifications = createAsyncThunk(
  'notification/fetchAll',
  async ({ page = 0, pageSize = 20 }, { rejectWithValue }) => {
    // GET /api/notifications?page=X&pageSize=Y
    // Response: List<Notification>
    return response;
  }
);

export const markAsRead = createAsyncThunk(
  'notification/markRead',
  async (notificationId, { rejectWithValue }) => {
    // PUT /api/notifications/{notificationId}/read
    // Response: Notification
    return response;
  }
);

export const markAllAsRead = createAsyncThunk(
  'notification/markAllRead',
  async (_, { rejectWithValue }) => {
    // PUT /api/notifications/read-all
    // Response: { message: "All notifications marked as read" }
    return response;
  }
);

export const getUnreadCount = createAsyncThunk(
  'notification/getUnreadCount',
  async (_, { rejectWithValue }) => {
    // GET /api/notifications/unread-count
    // Response: { count: number }
    return response;
  }
);

export const deleteNotification = createAsyncThunk(
  'notification/delete',
  async (notificationId, { rejectWithValue }) => {
    // DELETE /api/notifications/{notificationId}
    // Response: { message: "Notification deleted" }
    return response;
  }
);
```

**Request/Response Types:**
- `Notification`: { id, userId, title, message, type, relatedEntityType, relatedEntityId, isRead, metadata, createdAt, readAt }

---

### 3.9 Address Thunks

```typescript
// addressThunks.js

export const fetchAddresses = createAsyncThunk(
  'address/fetchAll',
  async (_, { rejectWithValue }) => {
    // GET /api/addresses
    // Response: List<AddressResponse>
    return response;
  }
);

export const addAddress = createAsyncThunk(
  'address/add',
  async (addressData, { rejectWithValue }) => {
    // POST /api/addresses
    // Request: { fullName, phoneNumber, addressLine1, addressLine2?, city, state, zipCode, country, isDefault }
    // Response: AddressResponse
    return response;
  }
);

export const updateAddress = createAsyncThunk(
  'address/update',
  async ({ addressId, addressData }, { rejectWithValue }) => {
    // PUT /api/addresses/{addressId}
    // Response: AddressResponse
    return response;
  }
);

export const deleteAddress = createAsyncThunk(
  'address/delete',
  async (addressId, { rejectWithValue }) => {
    // DELETE /api/addresses/{addressId}
    // Response: { message: "Address deleted" }
    return response;
  }
);

export const setDefaultAddress = createAsyncThunk(
  'address/setDefault',
  async (addressId, { rejectWithValue }) => {
    // PUT /api/addresses/{addressId}/default
    // Response: AddressResponse
    return response;
  }
);

export const verifyPincode = createAsyncThunk(
  'address/verifyPincode',
  async (pincode, { rejectWithValue }) => {
    // GET /api/delivery/pincode?pincode=X
    // Response: PincodeAvailabilityResponse
    return response;
  }
);
```

**Request/Response Types:**
- `AddressRequest`: { fullName, phoneNumber, addressLine1, addressLine2?, city, state, zipCode, country, isDefault }
- `AddressResponse`: { id, userId, fullName, phoneNumber, addressLine1, addressLine2, city, state, zipCode, country, isDefault, pincodeReachable, lastCheckedAt, createdAt }

---

## 4. API CLIENT SETUP (api.js)

```typescript
// api.js

import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
);

// Add token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle responses
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## 5. STORE CONFIGURATION (index.js)

```typescript
// store/index.js

import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import productReducer from './slices/productSlice';
import cartReducer from './slices/cartSlice';
import orderReducer from './slices/orderSlice';
import paymentReducer from './slices/paymentSlice';
import deliveryReducer from './slices/deliverySlice';
import couponReducer from './slices/couponSlice';
import notificationReducer from './slices/notificationSlice';
import addressReducer from './slices/addressSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    products: productReducer,
    cart: cartReducer,
    orders: orderReducer,
    payment: paymentReducer,
    delivery: deliveryReducer,
    coupons: couponReducer,
    notifications: notificationReducer,
    addresses: addressReducer
  }
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

---

## 6. KEY CONSIDERATIONS

### 6.1 Data Type Conversions
- **BigDecimal** → Store as **string** to avoid floating-point precision loss
- **LocalDateTime** → Convert to **ISO 8601 string** (e.g., "2024-01-10T10:30:00Z")
- **Enum** → Convert to **string** literals for union types
- **Boolean** → Keep as **boolean**
- **Integer/Long** → Convert to **number**

### 6.2 Error Handling
Each thunk should handle:
- Network errors
- Validation errors from backend
- Business logic errors
- 401/403 unauthorized errors

Example:
```typescript
try {
  const response = await apiClient.get('/endpoint');
  return response.data;
} catch (error) {
  return rejectWithValue(error.response?.data?.message || error.message);
}
```

### 6.3 Loading States
Each slice needs:
- `loading: false` → Initial state
- `loading: true` → During thunk execution
- `loading: false` → After thunk completes
- `error: null` → For error messages

### 6.4 Caching Strategy
- Cart: Refetch on every action (most dynamic)
- Products: Cache with manual refresh
- Orders: Refetch on open orders page
- Addresses: Cache and refetch on address page
- Notifications: Real-time with polling or WebSocket (optional)

### 6.5 Local Storage
Store in localStorage:
- JWT token
- User ID (for quick reference)
- Cart ID (optional, for recovery)

---

## 7. SLICE STRUCTURE TEMPLATE

Each slice should follow this template:

```typescript
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Define thunks here (exported)

const initialState = {
  loading: false,
  error: null,
  data: []
};

const slice = createSlice({
  name: 'sliceName',
  initialState,
  reducers: {
    // Synchronous actions
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(thunkName.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(thunkName.fulfilled, (state, action) => {
        state.loading = false;
        state.data = action.payload;
      })
      .addCase(thunkName.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export default slice.reducer;
```

---

## 8. COMPONENT USAGE EXAMPLE

```typescript
// In a component
import { useDispatch, useSelector } from 'react-redux';
import { fetchAllProducts } from './store/middleware/productThunks';

function ProductList() {
  const dispatch = useDispatch();
  const { products, loading, error } = useSelector(state => state.products);

  useEffect(() => {
    dispatch(fetchAllProducts({ page: 0, pageSize: 10 }));
  }, [dispatch]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      {products.items.map(product => (
        <div key={product.id}>{product.name}</div>
      ))}
    </div>
  );
}
```

---

This structure ensures:
✅ Type safety and consistency with backend
✅ Proper separation of concerns
✅ Scalability for future features
✅ Easy to test and maintain
✅ Follows Redux best practices

