import { createSlice } from '@reduxjs/toolkit';
import {
  trackOrder,
  checkPincodeAvailability,
  verifyPincode
} from '../thunks/deliveryThunks';

const initialState = {
  loading: false,
  error: null,
  tracking: {
    orderId: null,
    waybillNumber: null,
    currentStatus: null,
    location: null,
    lastUpdate: null,
    isDelivered: false,
    estimatedDeliveryDate: null
  },
  pincodeAvailability: {
    pincode: '',
    isAvailable: false,
    status: null,
    estimatedDeliveryDays: null,
    region: null,
    state: null
  }
};

const deliverySlice = createSlice({
  name: 'delivery',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearTracking: (state) => {
      state.tracking = initialState.tracking;
    }
  },
  extraReducers: (builder) => {
    // TRACK ORDER
    builder
      .addCase(trackOrder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(trackOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.tracking = action.payload;
      })
      .addCase(trackOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CHECK PINCODE AVAILABILITY
    builder
      .addCase(checkPincodeAvailability.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(checkPincodeAvailability.fulfilled, (state, action) => {
        state.loading = false;
        state.pincodeAvailability = action.payload;
      })
      .addCase(checkPincodeAvailability.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // VERIFY PINCODE
    builder
      .addCase(verifyPincode.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyPincode.fulfilled, (state, action) => {
        state.loading = false;
        state.pincodeAvailability = action.payload;
      })
      .addCase(verifyPincode.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, clearTracking, resetDelivery } = deliverySlice.actions;
export default deliverySlice.reducer;
