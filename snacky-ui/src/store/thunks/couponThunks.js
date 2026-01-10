import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH AVAILABLE COUPONS
export const fetchAvailableCoupons = createAsyncThunk(
  'coupons/fetchAvailable',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/coupons/available');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// GET ELIGIBLE COUPONS
export const getEligibleCoupons = createAsyncThunk(
  'coupons/getEligible',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/cart/coupons/eligible');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// VALIDATE COUPON
export const validateCoupon = createAsyncThunk(
  'coupons/validate',
  async (code, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/coupons/validate', {
        code
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CREATE COUPON (ADMIN)
export const createCoupon = createAsyncThunk(
  'coupons/create',
  async ({ code, type, discountValue, minOrderAmount, validFrom, validTill }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/coupons', {
        code,
        type,
        discountValue,
        minOrderAmount,
        validFrom,
        validTill
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE COUPON (ADMIN)
export const updateCoupon = createAsyncThunk(
  'coupons/update',
  async ({ couponId, code, type, discountValue, minOrderAmount, validFrom, validTill }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/coupons/${couponId}`, {
        code,
        type,
        discountValue,
        minOrderAmount,
        validFrom,
        validTill
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DEACTIVATE COUPON (ADMIN)
export const deactivateCoupon = createAsyncThunk(
  'coupons/deactivate',
  async (couponId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/coupons/${couponId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE COUPON PERMANENTLY (ADMIN)
export const deleteCoupon = createAsyncThunk(
  'coupons/delete',
  async (couponId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/coupons/${couponId}/permanent`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
