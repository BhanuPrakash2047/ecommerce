import { createSlice } from '@reduxjs/toolkit';
import {
  fetchAvailableCoupons,
  getEligibleCoupons,
  validateCoupon,
  createCoupon,
  updateCoupon,
  deactivateCoupon,
  deleteCoupon
} from '../thunks/couponThunks';

const initialState = {
  loading: false,
  error: null,
  availableCoupons: [],
  eligibleCoupons: [],
  appliedCoupon: null,
  validationResult: null
};

const couponSlice = createSlice({
  name: 'coupons',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearValidationResult: (state) => {
      state.validationResult = null;
    }
  },
  extraReducers: (builder) => {
    // FETCH AVAILABLE COUPONS
    builder
      .addCase(fetchAvailableCoupons.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAvailableCoupons.fulfilled, (state, action) => {
        state.loading = false;
        state.availableCoupons = action.payload;
      })
      .addCase(fetchAvailableCoupons.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // GET ELIGIBLE COUPONS
    builder
      .addCase(getEligibleCoupons.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getEligibleCoupons.fulfilled, (state, action) => {
        state.loading = false;
        // Handle different response structures
        if (Array.isArray(action.payload)) {
          state.eligibleCoupons = action.payload;
        } else if (action.payload?.eligibleCoupons && Array.isArray(action.payload.eligibleCoupons)) {
          state.eligibleCoupons = action.payload.eligibleCoupons;
        } else if (action.payload?.content && Array.isArray(action.payload.content)) {
          state.eligibleCoupons = action.payload.content;
        } else {
          state.eligibleCoupons = [];
        }
      })
      .addCase(getEligibleCoupons.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.eligibleCoupons = [];
      });

    // VALIDATE COUPON
    builder
      .addCase(validateCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(validateCoupon.fulfilled, (state, action) => {
        state.loading = false;
        state.validationResult = action.payload;
      })
      .addCase(validateCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CREATE COUPON (ADMIN)
    builder
      .addCase(createCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createCoupon.fulfilled, (state, action) => {
        state.loading = false;
        state.availableCoupons.push(action.payload);
      })
      .addCase(createCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE COUPON (ADMIN)
    builder
      .addCase(updateCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateCoupon.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.availableCoupons.findIndex(c => c.id === action.payload.id);
        if (index !== -1) {
          state.availableCoupons[index] = action.payload;
        }
      })
      .addCase(updateCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DEACTIVATE COUPON (ADMIN)
    builder
      .addCase(deactivateCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deactivateCoupon.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.availableCoupons.findIndex(c => c.id === action.payload.id);
        if (index !== -1) {
          state.availableCoupons[index].active = false;
        }
      })
      .addCase(deactivateCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE COUPON (ADMIN)
    builder
      .addCase(deleteCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteCoupon.fulfilled, (state, action) => {
        state.loading = false;
        const couponId = action.meta.arg;
        state.availableCoupons = state.availableCoupons.filter(c => c.id !== couponId);
      })
      .addCase(deleteCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, clearValidationResult } = couponSlice.actions;
export default couponSlice.reducer;
