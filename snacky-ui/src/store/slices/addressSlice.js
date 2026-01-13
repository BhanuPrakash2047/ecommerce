import { createSlice } from '@reduxjs/toolkit';
import {
  fetchAddresses,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '../thunks/addressThunks';

const initialState = {
  loading: false,
  error: null,
  items: [],
  selectedAddressId: null,
  defaultAddress: null
};

const addressSlice = createSlice({
  name: 'addresses',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    selectAddress: (state, action) => {
      state.selectedAddressId = action.payload;
    },
    clearSelectedAddress: (state) => {
      state.selectedAddressId = null;
    },
    resetAddress: () => {
      return initialState;
    }
  },
  extraReducers: (builder) => {
    // FETCH ADDRESSES
    builder
      .addCase(fetchAddresses.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAddresses.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
        const defaultAddr = action.payload.find(addr => addr.isDefault);
        if (defaultAddr) {
          state.defaultAddress = defaultAddr;
        }
      })
      .addCase(fetchAddresses.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // ADD ADDRESS
    builder
      .addCase(addAddress.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addAddress.fulfilled, (state, action) => {
        state.loading = false;
        state.items.push(action.payload);
        if (action.payload.isDefault) {
          state.defaultAddress = action.payload;
        }
      })
      .addCase(addAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE ADDRESS
    builder
      .addCase(updateAddress.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateAddress.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.items.findIndex(addr => addr.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
        if (action.payload.isDefault) {
          state.defaultAddress = action.payload;
        }
      })
      .addCase(updateAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE ADDRESS
    builder
      .addCase(deleteAddress.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteAddress.fulfilled, (state, action) => {
        state.loading = false;
        const addressId = action.meta.arg;
        state.items = state.items.filter(addr => addr.id !== addressId);
        if (state.defaultAddress?.id === addressId) {
          state.defaultAddress = null;
        }
      })
      .addCase(deleteAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // SET DEFAULT ADDRESS
    builder
      .addCase(setDefaultAddress.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(setDefaultAddress.fulfilled, (state, action) => {
        state.loading = false;
        // Update all items - set isDefault to false except the selected one
        state.items = state.items.map(addr => ({
          ...addr,
          isDefault: addr.id === action.payload.id
        }));
        state.defaultAddress = action.payload;
      })
      .addCase(setDefaultAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, selectAddress, clearSelectedAddress, resetAddress } = addressSlice.actions;
export default addressSlice.reducer;
