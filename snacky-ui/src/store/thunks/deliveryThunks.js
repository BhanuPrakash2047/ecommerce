import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// TRACK ORDER
export const trackOrder = createAsyncThunk(
  'delivery/trackOrder',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/orders/${orderId}/track`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CHECK PINCODE AVAILABILITY
export const checkPincodeAvailability = createAsyncThunk(
  'delivery/checkPincode',
  async (pincode, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/delivery/pincode', {
        params: { pincode }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// VERIFY PINCODE FOR ADDRESS
export const verifyPincode = createAsyncThunk(
  'delivery/verifyPincode',
  async (pincode, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/delivery/pincode', {
        params: { pincode }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
