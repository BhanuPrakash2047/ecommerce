import { createSlice } from '@reduxjs/toolkit';
import {
  fetchAllProducts,
  fetchProductById,
  filterProductsByPrice,
  searchProducts,
  fetchProductReviews,
  addReview,
  fetchFAQs,
  fetchProductImages,
  fetchProductVideos
} from '../thunks/productThunks';

const initialState = {
  loading: false,
  error: null,
  items: [],
  selectedProduct: null,
  reviews: [],
  faqs: [],
  images: [],
  videos: [],
  filters: {
    minPrice: null,
    maxPrice: null,
    searchQuery: null
  },
  pagination: {
    page: 0,
    pageSize: 10,
    totalProducts: 0
  }
};

const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearSelectedProduct: (state) => {
      state.selectedProduct = null;
      state.reviews = [];
      state.faqs = [];
      state.images = [];
      state.videos = [];
    }
  },
  extraReducers: (builder) => {
    // FETCH ALL PRODUCTS
    builder
      .addCase(fetchAllProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchAllProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH PRODUCT BY ID
    builder
      .addCase(fetchProductById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProductById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedProduct = action.payload;
      })
      .addCase(fetchProductById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FILTER PRODUCTS BY PRICE
    builder
      .addCase(filterProductsByPrice.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(filterProductsByPrice.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(filterProductsByPrice.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // SEARCH PRODUCTS
    builder
      .addCase(searchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(searchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH REVIEWS
    builder
      .addCase(fetchProductReviews.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProductReviews.fulfilled, (state, action) => {
        state.loading = false;
        state.reviews = action.payload;
      })
      .addCase(fetchProductReviews.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // ADD REVIEW
    builder
      .addCase(addReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addReview.fulfilled, (state, action) => {
        state.loading = false;
        state.reviews.push(action.payload);
      })
      .addCase(addReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH FAQs
    builder
      .addCase(fetchFAQs.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFAQs.fulfilled, (state, action) => {
        state.loading = false;
        state.faqs = action.payload;
      })
      .addCase(fetchFAQs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH IMAGES
    builder
      .addCase(fetchProductImages.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProductImages.fulfilled, (state, action) => {
        state.loading = false;
        state.images = action.payload;
      })
      .addCase(fetchProductImages.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH VIDEOS
    builder
      .addCase(fetchProductVideos.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProductVideos.fulfilled, (state, action) => {
        state.loading = false;
        state.videos = action.payload;
      })
      .addCase(fetchProductVideos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, setFilters, clearSelectedProduct } = productSlice.actions;
export default productSlice.reducer;
