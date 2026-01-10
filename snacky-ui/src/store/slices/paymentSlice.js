/* eslint-disable no-unused-vars */
import { createSlice } from '@reduxjs/toolkit';
import {
  createRazorpayOrder,
  verifyPayment,
  handlePaymentFailure
} from '../thunks/paymentThunks';

const initialState = {
  loading: false,
  error: null,
  orderId: null,
  razorpayOrderId: null,
  amount: 0,
  email: '',
  phone: '',
  paymentStatus: 'PENDING',
  paymentDetails: {
    razorpayPaymentId: null,
    razorpaySignature: null,
    paymentMethod: null,
    processedAt: null
  }
};

const paymentSlice = createSlice({
  name: 'payment',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    resetPayment: (state) => {
      return initialState;
    }
  },
  extraReducers: (builder) => {
    // CREATE RAZORPAY ORDER
    builder
      .addCase(createRazorpayOrder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createRazorpayOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.razorpayOrderId = action.payload.razorpayOrderId;
        state.orderId = action.payload.orderId;
        state.amount = action.payload.amount;
        state.email = action.payload.email;
        state.phone = action.payload.phone;
        state.paymentStatus = 'PENDING';
      })
      .addCase(createRazorpayOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.paymentStatus = 'FAILED';
      });

    // VERIFY PAYMENT
    builder
      .addCase(verifyPayment.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyPayment.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentStatus = 'SUCCESS';
        state.paymentDetails.razorpayPaymentId = action.payload.razorpayPaymentId;
        state.paymentDetails.razorpaySignature = action.payload.razorpaySignature;
        state.paymentDetails.processedAt = action.payload.processedAt;
      })
      .addCase(verifyPayment.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.paymentStatus = 'FAILED';
      });

    // HANDLE PAYMENT FAILURE
    builder
      .addCase(handlePaymentFailure.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(handlePaymentFailure.fulfilled, (state) => {
        state.loading = false;
        state.paymentStatus = 'FAILED';
      })
      .addCase(handlePaymentFailure.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.paymentStatus = 'FAILED';
      });
  }
});

export const { clearError, resetPayment } = paymentSlice.actions;
export default paymentSlice.reducer;
