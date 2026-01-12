import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// LOGIN
export const loginUser = createAsyncThunk(
  'auth/login',
  async ({ email, password }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/auth/login', { email, password });
      const { token, user } = response.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      return { token, user };
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// REGISTER
export const registerUser = createAsyncThunk(
  'auth/register',
  async ({ email, password, fullName, phone, role = 'USER' }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/auth/register', { 
        email, 
        password,
        fullName,
        phone,
        role 
      });
      const { token, user } = response.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      return { token, user };
    } catch (error) {
      return rejectWithValue(error.message || error.toString());
    }
  }
);

// GET PROFILE
export const getProfile = createAsyncThunk(
  'auth/getProfile',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/auth/profile');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE PROFILE
export const updateProfile = createAsyncThunk(
  'auth/updateProfile',
  async (userUpdate, { rejectWithValue }) => {
    try {
      const response = await apiClient.put('/auth/profile', userUpdate);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CHANGE PASSWORD
export const changePassword = createAsyncThunk(
  'auth/changePassword',
  async ({ oldPassword, newPassword, confirmPassword }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/auth/change-password', {
        oldPassword,
        newPassword,
        confirmPassword,
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// LOGOUT
export const logoutUser = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return null;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
