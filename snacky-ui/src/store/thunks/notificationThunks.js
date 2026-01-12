import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH NOTIFICATIONS
export const fetchNotifications = createAsyncThunk(
  'notifications/fetch',
  async ({ page = 0, pageSize = 20 } = {}, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/notifications', {
        params: { limit: pageSize }
      });
      // Backend returns { notifications: [...], count: X, status: 'success' }
      // Extract just the notifications array
      return response.data?.notifications || [];
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// MARK AS READ
export const markNotificationAsRead = createAsyncThunk(
  'notifications/markRead',
  async (notificationId, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/notifications/${notificationId}/read`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// MARK ALL AS READ
export const markAllNotificationsAsRead = createAsyncThunk(
  'notifications/markAllRead',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.put('/notifications/read-all');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// GET UNREAD COUNT
export const getUnreadNotificationCount = createAsyncThunk(
  'notifications/getUnreadCount',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/notifications/unread-count');
      // Backend returns { unreadCount: X, status: 'success' }
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE NOTIFICATION
export const deleteNotification = createAsyncThunk(
  'notifications/delete',
  async (notificationId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/notifications/${notificationId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
