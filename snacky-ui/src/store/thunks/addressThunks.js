import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH ADDRESSES
export const fetchAddresses = createAsyncThunk(
  'addresses/fetch',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/addresses');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// ADD ADDRESS
export const addAddress = createAsyncThunk(
  'addresses/add',
  async ({ fullName, phoneNumber, addressLine1, addressLine2, city, state, zipCode, country, isDefault }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/addresses', {
        fullName,
        phoneNumber,
        addressLine1,
        addressLine2,
        city,
        state,
        zipCode,
        country,
        isDefault
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE ADDRESS
export const updateAddress = createAsyncThunk(
  'addresses/update',
  async ({ addressId, fullName, phoneNumber, addressLine1, addressLine2, city, state, zipCode, country, isDefault }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/addresses/${addressId}`, {
        fullName,
        phoneNumber,
        addressLine1,
        addressLine2,
        city,
        state,
        zipCode,
        country,
        isDefault
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE ADDRESS
export const deleteAddress = createAsyncThunk(
  'addresses/delete',
  async (addressId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/addresses/${addressId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// SET DEFAULT ADDRESS
export const setDefaultAddress = createAsyncThunk(
  'addresses/setDefault',
  async (addressId, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/addresses/${addressId}/default`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
