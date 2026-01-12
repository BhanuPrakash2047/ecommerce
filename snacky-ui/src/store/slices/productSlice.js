/* eslint-disable no-unused-vars */
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
  fetchProductVideos,
  uploadProductImage,
  deleteProductImage,
  uploadProductVideo,
  deleteProductVideo,
  updateProductReview,
  deleteProductReview,
  createFAQ,
  updateFAQ,
  deleteFAQ,
  downloadProductImage,
  downloadProductVideo,
  createProduct,
  updateProduct,
  deleteProduct,
  fetchAvailableProducts,
  fetchCouponEligibleProducts,
  searchProductsAdvanced
} from '../thunks/productThunks';

const initialState = {
  loading: false,
  error: null,
  items: [],
  itemsWithImages: [],
  selectedProduct: null,
  reviews: [],
  reviewsMetadata: {
    totalElements: 0,
    totalPages: 0,
    pageNumber: 0,
    pageSize: 10,
    empty: true
  },
  faqs: [],
  images: [],
  videos: [],
  productImages: {}, // Store images keyed by productId
  productReviews: {}, // Store reviews keyed by productId for each product
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
    },
    storeProductImage: (state, action) => {
      const { productId, images } = action.payload;
      state.productImages[productId] = images;
      
      // Update items with image
      state.items = state.items.map(item => 
        item.id === productId 
          ? { ...item, image: images && images.length > 0 ? images[0] : item.image }
          : item
      );
    },
    mergeProductsWithImages: (state) => {
      // Merge stored images with products
      state.itemsWithImages = state.items.map(product => ({
        ...product,
        image: state.productImages[product.id] && state.productImages[product.id].length > 0 
          ? state.productImages[product.id][0] 
          : product.image
      }));
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
        const productId = action.meta.arg;
        
        // Extract reviews from paginated response
        const reviewsData = action.payload?.content || [];
        state.reviews = reviewsData;
        
        // Store reviews by product ID
        state.productReviews[productId] = reviewsData;
        
        // Store pagination metadata
        state.reviewsMetadata = {
          totalElements: action.payload?.totalElements || 0,
          totalPages: action.payload?.totalPages || 0,
          pageNumber: action.payload?.number || 0,
          pageSize: action.payload?.size || 10,
          empty: action.payload?.empty || true
        };
      })
      .addCase(fetchProductReviews.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.reviews = [];
        state.reviewsMetadata = {
          totalElements: 0,
          totalPages: 0,
          pageNumber: 0,
          pageSize: 10,
          empty: true
        };
      });

    // ADD REVIEW
    builder
      .addCase(addReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addReview.fulfilled, (state, action) => {
        state.loading = false;
        // Add new review to the current reviews array
        state.reviews.unshift(action.payload);
        
        // Also update productReviews if productId is available
        if (action.meta && action.meta.arg && action.meta.arg.productId) {
          const productId = action.meta.arg.productId;
          if (!state.productReviews[productId]) {
            state.productReviews[productId] = [];
          }
          state.productReviews[productId].unshift(action.payload);
        }
        
        // Update metadata
        state.reviewsMetadata.totalElements += 1;
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
        
        // Store images by product ID (if payload includes productId)
        // This allows us to store multiple product images in the state
        if (action.meta && action.meta.arg) {
          const productId = action.meta.arg;
          state.productImages[productId] = action.payload;
          
          // Update product item with image
          state.items = state.items.map(item =>
            item.id === productId
              ? { ...item, image: action.payload && action.payload.length > 0 ? action.payload[0] : item.image }
              : item
          );
        }
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

    // UPLOAD IMAGE
    builder
      .addCase(uploadProductImage.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(uploadProductImage.fulfilled, (state, action) => {
        state.loading = false;
        state.images.push(action.payload);
      })
      .addCase(uploadProductImage.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE IMAGE
    builder
      .addCase(deleteProductImage.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteProductImage.fulfilled, (state, action) => {
        state.loading = false;
        const imageId = action.meta.arg;
        state.images = state.images.filter(img => img.id !== imageId);
      })
      .addCase(deleteProductImage.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPLOAD VIDEO
    builder
      .addCase(uploadProductVideo.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(uploadProductVideo.fulfilled, (state, action) => {
        state.loading = false;
        state.videos.push(action.payload);
      })
      .addCase(uploadProductVideo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE VIDEO
    builder
      .addCase(deleteProductVideo.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteProductVideo.fulfilled, (state, action) => {
        state.loading = false;
        const videoId = action.meta.arg;
        state.videos = state.videos.filter(vid => vid.id !== videoId);
      })
      .addCase(deleteProductVideo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE REVIEW
    builder
      .addCase(updateProductReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateProductReview.fulfilled, (state, action) => {
        state.loading = false;
        const reviewIndex = state.reviews.findIndex(r => r.id === action.payload.id);
        if (reviewIndex !== -1) {
          state.reviews[reviewIndex] = action.payload;
        }
      })
      .addCase(updateProductReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE REVIEW
    builder
      .addCase(deleteProductReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteProductReview.fulfilled, (state, action) => {
        state.loading = false;
        const reviewId = action.meta.arg;
        state.reviews = state.reviews.filter(r => r.id !== reviewId);
      })
      .addCase(deleteProductReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CREATE FAQ
    builder
      .addCase(createFAQ.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createFAQ.fulfilled, (state, action) => {
        state.loading = false;
        state.faqs.push(action.payload);
      })
      .addCase(createFAQ.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE FAQ
    builder
      .addCase(updateFAQ.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateFAQ.fulfilled, (state, action) => {
        state.loading = false;
        const faqIndex = state.faqs.findIndex(f => f.id === action.payload.id);
        if (faqIndex !== -1) {
          state.faqs[faqIndex] = action.payload;
        }
      })
      .addCase(updateFAQ.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE FAQ
    builder
      .addCase(deleteFAQ.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteFAQ.fulfilled, (state, action) => {
        state.loading = false;
        const faqId = action.meta.arg;
        state.faqs = state.faqs.filter(f => f.id !== faqId);
      })
      .addCase(deleteFAQ.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DOWNLOAD IMAGE
    builder
      .addCase(downloadProductImage.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(downloadProductImage.fulfilled, (state, action) => {
        state.loading = false;
      })
      .addCase(downloadProductImage.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DOWNLOAD VIDEO
    builder
      .addCase(downloadProductVideo.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(downloadProductVideo.fulfilled, (state, action) => {
        state.loading = false;
      })
      .addCase(downloadProductVideo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // CREATE PRODUCT (ADMIN)
    builder
      .addCase(createProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createProduct.fulfilled, (state, action) => {
        state.loading = false;
        state.items.push(action.payload);
      })
      .addCase(createProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // UPDATE PRODUCT (ADMIN)
    builder
      .addCase(updateProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateProduct.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.items.findIndex(p => p.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
        if (state.selectedProduct?.id === action.payload.id) {
          state.selectedProduct = action.payload;
        }
      })
      .addCase(updateProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // DELETE PRODUCT (ADMIN)
    builder
      .addCase(deleteProduct.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteProduct.fulfilled, (state, action) => {
        state.loading = false;
        const productId = action.meta.arg;
        state.items = state.items.filter(p => p.id !== productId);
      })
      .addCase(deleteProduct.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH AVAILABLE PRODUCTS
    builder
      .addCase(fetchAvailableProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAvailableProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchAvailableProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // FETCH COUPON ELIGIBLE PRODUCTS
    builder
      .addCase(fetchCouponEligibleProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCouponEligibleProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchCouponEligibleProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });

    // ADVANCED SEARCH
    builder
      .addCase(searchProductsAdvanced.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchProductsAdvanced.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(searchProductsAdvanced.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearError, setFilters, clearSelectedProduct, storeProductImage, mergeProductsWithImages } = productSlice.actions;
export default productSlice.reducer;
