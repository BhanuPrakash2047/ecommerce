/**
 * API ENDPOINTS SUMMARY
 * All endpoints integrated with Redux store
 */

export const API_ENDPOINTS = {
  // ===== AUTH =====
  AUTH: {
    LOGIN: 'POST /auth/login',
    REGISTER: 'POST /auth/register',
    PROFILE: 'GET /auth/profile',
    UPDATE_PROFILE: 'PUT /auth/profile',
  },

  // ===== PRODUCTS =====
  PRODUCTS: {
    GET_ALL: 'GET /products',
    GET_BY_ID: 'GET /products/{productId}',
    FILTER_BY_PRICE: 'GET /products/filter/price',
    SEARCH: 'GET /products/search',
    GET_REVIEWS: 'GET /products/{productId}/reviews',
    ADD_REVIEW: 'POST /products/{productId}/reviews',
    GET_FAQS: 'GET /products/{productId}/faqs',
    GET_IMAGES: 'GET /products/{productId}/images', // Returns blob data
    GET_VIDEOS: 'GET /products/{productId}/videos', // Returns blob data
  },

  // ===== CART =====
  CART: {
    GET: 'GET /cart',
    ADD_ITEM: 'POST /cart/items',
    UPDATE_QUANTITY: 'PUT /cart/items/{cartItemId}',
    REMOVE_ITEM: 'DELETE /cart/items/{cartItemId}',
    APPLY_COUPON: 'POST /cart/coupons',
    REMOVE_COUPON: 'DELETE /cart/coupons',
    VALIDATE: 'POST /cart/validate',
    CLEAR: 'DELETE /cart',
    GET_ELIGIBLE_COUPONS: 'GET /cart/coupons/eligible',
    CHECKOUT: 'POST /cart/checkout',
  },

  // ===== ORDERS =====
  ORDERS: {
    GET_USER_ORDERS: 'GET /orders',
    GET_ORDER_DETAILS: 'GET /orders/{orderId}',
    CANCEL: 'PUT /orders/{orderId}/cancel',
    RETURN: 'POST /orders/{orderId}/return',
    TRACK: 'GET /orders/{orderId}/track',
    DOWNLOAD_LABEL: 'GET /orders/{orderId}/shipping-label', // Returns PDF blob
  },

  // ===== PAYMENTS =====
  PAYMENTS: {
    CREATE: 'POST /payments/create',
    WEBHOOK_SUCCESS: 'POST /payments/webhook/success',
    WEBHOOK_FAILURE: 'POST /payments/webhook/failure',
  },

  // ===== DELIVERY =====
  DELIVERY: {
    CHECK_PINCODE: 'GET /delivery/pincode',
  },

  // ===== COUPONS =====
  COUPONS: {
    GET_AVAILABLE: 'GET /coupons/available',
    VALIDATE: 'POST /coupons/validate',
    CREATE: 'POST /coupons', // Admin only
    UPDATE: 'PUT /coupons/{couponId}', // Admin only
    DEACTIVATE: 'DELETE /coupons/{couponId}', // Admin only
    DELETE: 'DELETE /coupons/{couponId}/permanent', // Admin only
  },

  // ===== NOTIFICATIONS =====
  NOTIFICATIONS: {
    GET_ALL: 'GET /notifications',
    MARK_READ: 'PUT /notifications/{notificationId}/read',
    MARK_ALL_READ: 'PUT /notifications/read-all',
    GET_UNREAD_COUNT: 'GET /notifications/unread-count',
    DELETE: 'DELETE /notifications/{notificationId}',
  },

  // ===== ADDRESSES =====
  ADDRESSES: {
    GET_ALL: 'GET /addresses',
    ADD: 'POST /addresses',
    UPDATE: 'PUT /addresses/{addressId}',
    DELETE: 'DELETE /addresses/{addressId}',
    SET_DEFAULT: 'PUT /addresses/{addressId}/default',
  }
};

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503
};

export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection.',
  UNAUTHORIZED: 'Unauthorized. Please login again.',
  FORBIDDEN: 'You do not have permission to access this resource.',
  NOT_FOUND: 'Resource not found.',
  VALIDATION_ERROR: 'Validation error. Please check your input.',
  SERVER_ERROR: 'Server error. Please try again later.',
  PAYMENT_FAILED: 'Payment failed. Please try again.',
  PRODUCT_NOT_AVAILABLE: 'Product is not available.',
  CART_EMPTY: 'Your cart is empty.',
  ORDER_NOT_FOUND: 'Order not found.',
  COUPON_INVALID: 'Coupon is invalid or expired.',
  ADDRESS_NOT_FOUND: 'Address not found.',
  PINCODE_NOT_SERVICEABLE: 'Pincode is not serviceable.',
};

/**
 * Request body structures for reference
 */
export const REQUEST_BODIES = {
  // Auth
  LOGIN: {
    email: 'string',
    password: 'string'
  },
  REGISTER: {
    email: 'string',
    password: 'string',
    role: 'USER|ADMIN'
  },

  // Cart
  ADD_TO_CART: {
    productId: 'number',
    quantity: 'number (1-999)'
  },
  UPDATE_QUANTITY: {
    quantity: 'number (1-999)'
  },
  APPLY_COUPON: {
    couponId: 'number'
  },

  // Orders
  CREATE_ORDER: {
    addressId: 'number',
    appliedCouponId: 'number (optional)'
  },

  // Payment
  CREATE_PAYMENT: {
    orderId: 'number',
    amount: 'number (in rupees)',
    email: 'string',
    phone: 'string'
  },

  // Addresses
  ADD_ADDRESS: {
    fullName: 'string',
    phoneNumber: 'string',
    addressLine1: 'string',
    addressLine2: 'string (optional)',
    city: 'string',
    state: 'string',
    zipCode: 'string',
    country: 'string',
    isDefault: 'boolean'
  },

  // Reviews
  ADD_REVIEW: {
    rating: 'number (1-5)',
    title: 'string',
    text: 'string'
  },

  // Coupons (Admin)
  CREATE_COUPON: {
    code: 'string',
    type: 'PERCENTAGE|FLAT',
    discountValue: 'string (BigDecimal)',
    minOrderAmount: 'string (BigDecimal)',
    validFrom: 'LocalDateTime string',
    validTill: 'LocalDateTime string'
  }
};

/**
 * Response structures for reference
 */
export const RESPONSE_STRUCTURES = {
  PRODUCT: {
    id: 'number',
    name: 'string',
    price: 'string (BigDecimal)',
    originalPrice: 'string (BigDecimal)',
    isAvailable: 'boolean',
    isEligibleForCoupon: 'boolean',
    createdAt: 'ISO DateTime string'
  },

  CART: {
    cartId: 'number',
    userId: 'number',
    items: 'CartItemResponse[]',
    subtotal: 'string (BigDecimal)',
    discountAmount: 'string (BigDecimal)',
    total: 'string (BigDecimal)',
    appliedCouponId: 'number|null',
    appliedCouponCode: 'string|null',
    alerts: 'string[]'
  },

  ORDER: {
    id: 'number',
    orderNumber: 'string',
    status: 'CREATED|PAYMENT_PENDING|PAID|CONFIRMED|SHIPPED|DELIVERED|RETURNED|CANCELLED',
    items: 'OrderItemResponse[]',
    subtotal: 'string (BigDecimal)',
    discountAmount: 'string (BigDecimal)',
    totalAmount: 'string (BigDecimal)',
    appliedCouponId: 'number|null',
    couponCode: 'string|null',
    discountType: 'PERCENTAGE|FLAT|null',
    discountValue: 'string (BigDecimal)|null',
    addressId: 'number|null',
    receiverName: 'string',
    receiverPhone: 'string',
    receiverEmail: 'string',
    trackingNumber: 'string|null',
    trackingAgent: 'string|null',
    createdAt: 'ISO DateTime string',
    updatedAt: 'ISO DateTime string',
    deliveredAt: 'ISO DateTime string|null'
  },

  TRACKING: {
    orderId: 'number',
    waybillNumber: 'string|null',
    currentStatus: 'IN_TRANSIT|DELIVERED|FAILED|PENDING|null',
    location: 'string|null',
    lastUpdate: 'string|null',
    isDelivered: 'boolean',
    estimatedDeliveryDate: 'string|null'
  },

  ADDRESS: {
    id: 'number',
    userId: 'number',
    fullName: 'string',
    phoneNumber: 'string',
    addressLine1: 'string',
    addressLine2: 'string|null',
    city: 'string',
    state: 'string',
    zipCode: 'string',
    country: 'string',
    isDefault: 'boolean',
    pincodeReachable: 'boolean|null',
    lastCheckedAt: 'ISO DateTime string|null',
    createdAt: 'ISO DateTime string'
  },

  NOTIFICATION: {
    id: 'number',
    userId: 'number',
    title: 'string',
    message: 'string',
    type: 'PAYMENT_RECEIVED|SHIPMENT_CREATED|ORDER_DELIVERED|ADMIN_SHIPMENT_FAILED',
    relatedEntityType: 'ORDER|SHIPMENT_JOB|null',
    relatedEntityId: 'number|null',
    isRead: 'boolean',
    metadata: 'object (JSON)',
    createdAt: 'ISO DateTime string',
    readAt: 'ISO DateTime string|null'
  }
};
