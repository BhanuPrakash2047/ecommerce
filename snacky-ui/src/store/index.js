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

export default store;
