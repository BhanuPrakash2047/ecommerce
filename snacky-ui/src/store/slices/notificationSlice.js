import { createSlice } from '@reduxjs/toolkit';
import {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  getUnreadNotificationCount,
  deleteNotification
} from '../thunks/notificationThunks';

const initialState = {
  loading: false,
  error: null,
  items: [],
  unreadCount: 0,
  pagination: {
    page: 0,
    pageSize: 20,
    totalNotifications: 0
  }
};

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    // FETCH NOTIFICATIONS
    builder
      .addCase(fetchNotifications.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchNotifications.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // MARK AS READ
    builder
      .addCase(markNotificationAsRead.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(markNotificationAsRead.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.items.findIndex(n => n.id === action.payload.id);
        if (index !== -1) {
          state.items[index].isRead = true;
          state.items[index].readAt = action.payload.readAt;
          if (state.unreadCount > 0) {
            state.unreadCount--;
          }
        }
      })
      .addCase(markNotificationAsRead.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // MARK ALL AS READ
    builder
      .addCase(markAllNotificationsAsRead.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(markAllNotificationsAsRead.fulfilled, (state) => {
        state.loading = false;
        state.items.forEach(notification => {
          notification.isRead = true;
        });
        state.unreadCount = 0;
      })
      .addCase(markAllNotificationsAsRead.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // GET UNREAD COUNT
    builder
      .addCase(getUnreadNotificationCount.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getUnreadNotificationCount.fulfilled, (state, action) => {
        state.loading = false;
        state.unreadCount = action.payload.count;
      })
      .addCase(getUnreadNotificationCount.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE NOTIFICATION
    builder
      .addCase(deleteNotification.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteNotification.fulfilled, (state, action) => {
        state.loading = false;
        const notificationId = action.meta.arg;
        state.items = state.items.filter(n => n.id !== notificationId);
      })
      .addCase(deleteNotification.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError } = notificationSlice.actions;
export default notificationSlice.reducer;
