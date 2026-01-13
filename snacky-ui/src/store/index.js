import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import productReducer from './slices/productSlice';
import cartReducer, { resetCart } from './slices/cartSlice';
import orderReducer, { resetOrders } from './slices/orderSlice';
import paymentReducer, { resetPayment } from './slices/paymentSlice';
import deliveryReducer, { resetDelivery } from './slices/deliverySlice';
import couponReducer, { resetCoupon } from './slices/couponSlice';
import notificationReducer, { resetNotifications } from './slices/notificationSlice';
import addressReducer, { resetAddress } from './slices/addressSlice';
import { logoutUser } from './thunks/authThunks';

// Middleware to reset all user-specific data on logout
const logoutMiddleware = store => next => action => {
  const result = next(action);
  
  if (action.type === logoutUser.fulfilled.type) {
    // Dispatch all reset actions for user-specific data
    store.dispatch(resetCart());
    store.dispatch(resetOrders());
    store.dispatch(resetAddress());
    store.dispatch(resetPayment());
    store.dispatch(resetDelivery());
    store.dispatch(resetNotifications());
    store.dispatch(resetCoupon());
  }
  
  return result;
};

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
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(logoutMiddleware)
});

export default store;
