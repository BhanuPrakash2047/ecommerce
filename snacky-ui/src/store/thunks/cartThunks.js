import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH CART
export const fetchCart = createAsyncThunk(
  'cart/fetch',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/cart');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// ADD TO CART
export const addToCart = createAsyncThunk(
  'cart/addItem',
  async ({ productId, quantity }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/cart/items', {
        productId,
        quantity
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE CART ITEM QUANTITY
export const updateCartItemQuantity = createAsyncThunk(
  'cart/updateQuantity',
  async ({ cartItemId, quantity }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/cart/items/${cartItemId}`, {
        quantity
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// REMOVE FROM CART
export const removeFromCart = createAsyncThunk(
  'cart/removeItem',
  async (cartItemId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/cart/items/${cartItemId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// APPLY COUPON
export const applyCoupon = createAsyncThunk(
  'cart/applyCoupon',
  async (couponId, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/cart/coupons', {
        couponId
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// REMOVE COUPON
export const removeCoupon = createAsyncThunk(
  'cart/removeCoupon',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete('/cart/coupons');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// VALIDATE CART
export const validateCart = createAsyncThunk(
  'cart/validate',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/cart/validate');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CLEAR CART
export const clearCart = createAsyncThunk(
  'cart/clear',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete('/cart');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// GET ELIGIBLE COUPONS FOR CART
export const getEligibleCoupons = createAsyncThunk(
  'cart/getEligibleCoupons',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/cart/coupons/eligible');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
