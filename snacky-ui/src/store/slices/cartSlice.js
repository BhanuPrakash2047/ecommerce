/* eslint-disable no-unused-vars */
import { createSlice } from '@reduxjs/toolkit';
import {
  fetchCart,
  addToCart,
  updateCartItemQuantity,
  removeFromCart,
  applyCoupon,
  removeCoupon,
  validateCart,
  clearCart,
  getEligibleCoupons
} from '../thunks/cartThunks';

const initialState = {
  loading: false,
  error: null,
  cartId: null,
  userId: null,
  items: [],
  subtotal: '0',
  discountAmount: '0',
  total: '0',
  appliedCouponId: null,
  appliedCouponCode: null,
  alerts: [],
  validationErrors: {
    stockErrors: [],
    unavailableProducts: [],
    deletedProducts: []
  },
  eligibleCoupons: []
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    resetCart: (state) => {
      return initialState;
    }
  },
  extraReducers: (builder) => {
    // FETCH CART
    builder
      .addCase(fetchCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.loading = false;
        state.cartId = action.payload.cartId;
        state.userId = action.payload.userId;
        state.items = action.payload.items || [];
        state.subtotal = action.payload.subtotal;
        state.discountAmount = action.payload.discountAmount;
        state.total = action.payload.total;
        state.appliedCouponId = action.payload.appliedCouponId;
        state.appliedCouponCode = action.payload.appliedCouponCode;
        state.alerts = action.payload.alerts || [];
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // ADD TO CART
    builder
      .addCase(addToCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addToCart.fulfilled, (state, action) => {
        state.loading = false;
        state.cartId = action.payload.cartId;
        state.userId = action.payload.userId;
        state.items = action.payload.items || [];
        state.subtotal = action.payload.subtotal;
        state.discountAmount = action.payload.discountAmount;
        state.total = action.payload.total;
        state.alerts = action.payload.alerts || [];
      })
      .addCase(addToCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE QUANTITY
    builder
      .addCase(updateCartItemQuantity.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateCartItemQuantity.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.items || [];
        state.subtotal = action.payload.subtotal;
        state.discountAmount = action.payload.discountAmount;
        state.total = action.payload.total;
        state.alerts = action.payload.alerts || [];
      })
      .addCase(updateCartItemQuantity.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // REMOVE FROM CART
    builder
      .addCase(removeFromCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeFromCart.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.items || [];
        state.subtotal = action.payload.subtotal;
        state.discountAmount = action.payload.discountAmount;
        state.total = action.payload.total;
        state.alerts = action.payload.alerts || [];
      })
      .addCase(removeFromCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // APPLY COUPON
    builder
      .addCase(applyCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(applyCoupon.fulfilled, (state, action) => {
        state.loading = false;
        state.appliedCouponId = action.payload.appliedCouponId;
        state.appliedCouponCode = action.payload.appliedCouponCode;
        state.discountAmount = action.payload.discountAmount;
        state.total = action.payload.total;
        state.alerts = action.payload.alerts || [];
      })
      .addCase(applyCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // REMOVE COUPON
    builder
      .addCase(removeCoupon.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeCoupon.fulfilled, (state, action) => {
        state.loading = false;
        state.appliedCouponId = null;
        state.appliedCouponCode = null;
        state.discountAmount = '0';
        state.total = action.payload.total;
      })
      .addCase(removeCoupon.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // VALIDATE CART
    builder
      .addCase(validateCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(validateCart.fulfilled, (state, action) => {
        state.loading = false;
        state.validationErrors = action.payload;
      })
      .addCase(validateCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CLEAR CART
    builder
      .addCase(clearCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(clearCart.fulfilled, (state) => {
        state.loading = false;
        return initialState;
      })
      .addCase(clearCart.rejected, (state, action) => {
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
        state.eligibleCoupons = action.payload.eligibleCoupons || [];
      })
      .addCase(getEligibleCoupons.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, resetCart } = cartSlice.actions;
export default cartSlice.reducer;
