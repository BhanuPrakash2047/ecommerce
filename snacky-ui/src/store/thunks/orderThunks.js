import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH USER ORDERS
export const fetchUserOrders = createAsyncThunk(
  'orders/fetchUser',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/orders');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FETCH ORDER DETAILS
export const fetchOrderDetails = createAsyncThunk(
  'orders/fetchDetails',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/orders/${orderId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CREATE ORDER (CHECKOUT)
export const createOrder = createAsyncThunk(
  'orders/create',
  async (addressId, { rejectWithValue }) => {
    try {
      const response = await apiClient.post(`/cart/checkout/confirm?id=${addressId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.error || 
        error.response?.data?.message ||
        error.message || 
        'Failed to create order'
      );
    }
  }
);

// CANCEL ORDER
export const cancelOrder = createAsyncThunk(
  'orders/cancel',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/orders/${orderId}/cancel`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// RETURN ORDER
export const returnOrder = createAsyncThunk(
  'orders/return',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await apiClient.post(`/orders/${orderId}/return`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DOWNLOAD SHIPPING LABEL
export const downloadShippingLabel = createAsyncThunk(
  'orders/downloadLabel',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/orders/${orderId}/shipping-label`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
