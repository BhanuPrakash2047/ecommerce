import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// CREATE RAZORPAY ORDER
export const createRazorpayOrder = createAsyncThunk(
  'payment/createOrder',
  async ({ orderId, amount, email, phone }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/payments/create', {
        orderId,
        amount,
        email,
        phone
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// VERIFY PAYMENT
export const verifyPayment = createAsyncThunk(
  'payment/verify',
  async ({ razorpayOrderId, razorpayPaymentId, razorpaySignature }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/payments/webhook/success', {
        razorpayOrderId,
        razorpayPaymentId,
        razorpaySignature
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// HANDLE PAYMENT FAILURE
export const handlePaymentFailure = createAsyncThunk(
  'payment/failure',
  async ({ orderId, error }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/payments/webhook/failure', {
        orderId,
        error
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);
