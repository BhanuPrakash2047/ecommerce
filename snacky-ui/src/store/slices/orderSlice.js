import { createSlice } from '@reduxjs/toolkit';
import {
  fetchUserOrders,
  fetchOrderDetails,
  createOrder,
  cancelOrder,
  returnOrder,
  downloadShippingLabel
} from '../thunks/orderThunks';

const initialState = {
  loading: false,
  error: null,
  items: [],
  selectedOrder: null,
  pagination: {
    page: 0,
    pageSize: 10,
    totalOrders: 0
  }
};

const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearSelectedOrder: (state) => {
      state.selectedOrder = null;
    },
    resetOrders: () => {
      return initialState;
    }
  },
  extraReducers: (builder) => {
    // FETCH USER ORDERS
    builder
      .addCase(fetchUserOrders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserOrders.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchUserOrders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH ORDER DETAILS
    builder
      .addCase(fetchOrderDetails.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchOrderDetails.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedOrder = action.payload;
      })
      .addCase(fetchOrderDetails.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CREATE ORDER
    builder
      .addCase(createOrder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedOrder = action.payload;
        state.items.push(action.payload);
      })
      .addCase(createOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CANCEL ORDER
    builder
      .addCase(cancelOrder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(cancelOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedOrder = action.payload;
        const index = state.items.findIndex(order => order.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      })
      .addCase(cancelOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // RETURN ORDER
    builder
      .addCase(returnOrder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(returnOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedOrder = action.payload;
        const index = state.items.findIndex(order => order.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      })
      .addCase(returnOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DOWNLOAD SHIPPING LABEL
    builder
      .addCase(downloadShippingLabel.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(downloadShippingLabel.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(downloadShippingLabel.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, clearSelectedOrder, resetOrders } = orderSlice.actions;
export default orderSlice.reducer;
