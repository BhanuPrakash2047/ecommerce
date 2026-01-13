import { createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api';

// FETCH ALL PRODUCTS
export const fetchAllProducts = createAsyncThunk(
  'products/fetchAll',
  async ({ page = 0, pageSize = 10 } = {}, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/products', {
        params: { page, pageSize }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FETCH PRODUCT BY ID
export const fetchProductById = createAsyncThunk(
  'products/fetchById',
  async (productId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/products/${productId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FILTER PRODUCTS BY PRICE
export const filterProductsByPrice = createAsyncThunk(
  'products/filterByPrice',
  async ({ minPrice, maxPrice, page = 0, pageSize = 10 }, { rejectWithValue }) => {
    try {
      if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
        return rejectWithValue('Invalid price range');
      }
      const response = await apiClient.get('/products/filter/price', {
        params: { minPrice, maxPrice, page, pageSize }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// SEARCH PRODUCTS
export const searchProducts = createAsyncThunk(
  'products/search',
  async (searchQuery, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/products/search', {
        params: { query: searchQuery }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FETCH PRODUCT REVIEWS
export const fetchProductReviews = createAsyncThunk(
  'products/fetchReviews',
  async (productId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/products/${productId}/reviews`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// ADD REVIEW
export const addReview = createAsyncThunk(
  'products/addReview',
  async ({ productId, rating, title, text }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post(`/products/${productId}/reviews`, {
        rating,
        title,
        text
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FETCH FAQS
export const fetchFAQs = createAsyncThunk(
  'products/fetchFAQs',
  async (productId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/products/${productId}/faqs`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// FETCH PRODUCT IMAGES
export const fetchProductImages = createAsyncThunk(
  'products/fetchImages',
  async (productId, { rejectWithValue }) => {
    try {
      // Step 1: Get the images metadata (list of images with their IDs)
      const metadataResponse = await apiClient.get(`/products/${productId}/images`);
      
      const images = Array.isArray(metadataResponse.data) 
        ? metadataResponse.data 
        : metadataResponse.data.images || [];
      
      if (!images || images.length === 0) {
        return ['https://via.placeholder.com/300x200?text=No+Image'];
      }
      
      // Step 2: Download the actual binary data for each image
      const imageUrls = await Promise.all(
        images.map(async (image) => {
          try {
            const imageId = image.id || image.imageId;
            if (!imageId) {
              console.warn('Image missing ID:', image);
              return null;
            }
            
            // Fetch the actual binary image data from the download endpoint
            const downloadResponse = await apiClient.get(
              `/media/images/${imageId}/download`,
              {
                responseType: 'blob'
              }
            );
            
            // Convert blob to data URL using FileReader
            return new Promise((resolve, reject) => {
              const reader = new FileReader();
              reader.onloadend = () => {
                console.log('Successfully converted image', imageId, 'to data URL');
                resolve(reader.result); // reader.result is already a data URL
              };reader.onerror = () => {
                console.error('FileReader error for image', imageId);
                reject(new Error('Failed to read image blob'));
              };
              reader.readAsDataURL(downloadResponse.data);
            });
          } catch (error) {
            console.error('Error downloading image:', error);
            return null;
          }
        })
      );
      
      // Filter out null values and return valid image URLs
      const validUrls = imageUrls.filter(url => url !== null);
      console.log('Final image URLs for product', productId, ':', validUrls);
      return validUrls.length > 0 ? validUrls : ['https://via.placeholder.com/300x200?text=No+Image'];
    } catch (error) {
      console.error('Error fetching product images:', error);
      return rejectWithValue(error.message);
    }
  }
);

// FETCH PRODUCT VIDEOS
export const fetchProductVideos = createAsyncThunk(
  'products/fetchVideos',
  async (productId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/products/${productId}/videos`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CREATE PRODUCT (ADMIN)
export const createProduct = createAsyncThunk(
  'products/create',
  async ({ name, price, originalPrice, isAvailable = true, isEligibleForCoupon = true }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post('/products', {
        name,
        price,
        originalPrice,
        isAvailable,
        isEligibleForCoupon
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE PRODUCT (ADMIN)
export const updateProduct = createAsyncThunk(
  'products/update',
  async ({ productId, name, price, originalPrice, isAvailable, isEligibleForCoupon }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/products/${productId}`, {
        name,
        price,
        originalPrice,
        isAvailable,
        isEligibleForCoupon
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE PRODUCT (ADMIN)
export const deleteProduct = createAsyncThunk(
  'products/delete',
  async (productId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/products/${productId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// GET AVAILABLE PRODUCTS
export const fetchAvailableProducts = createAsyncThunk(
  'products/fetchAvailable',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/products/available');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// GET COUPON ELIGIBLE PRODUCTS
export const fetchCouponEligibleProducts = createAsyncThunk(
  'products/fetchCouponEligible',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/products/coupon-eligible');
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// ADVANCED SEARCH
export const searchProductsAdvanced = createAsyncThunk(
  'products/searchAdvanced',
  async ({ name, minPrice, maxPrice, page = 0, pageSize = 10 }, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/products/search/advanced', {
        params: { name, minPrice, maxPrice, page, pageSize }
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPLOAD PRODUCT IMAGE (ADMIN)
export const uploadProductImage = createAsyncThunk(
  'products/uploadImage',
  async ({ productId, file, isPrimary = false }, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('isPrimary', isPrimary);

      const response = await apiClient.post(
        `/media/images/upload/${productId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE PRODUCT IMAGE (ADMIN)
export const deleteProductImage = createAsyncThunk(
  'products/deleteImage',
  async (imageId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/media/images/${imageId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPLOAD PRODUCT VIDEO (ADMIN)
export const uploadProductVideo = createAsyncThunk(
  'products/uploadVideo',
  async ({ productId, file, title, description = '', duration = 0 }, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('title', title);
      formData.append('description', description);
      formData.append('duration', duration);

      const response = await apiClient.post(
        `/media/videos/upload/${productId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE PRODUCT VIDEO (ADMIN)
export const deleteProductVideo = createAsyncThunk(
  'products/deleteVideo',
  async (videoId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/media/videos/${videoId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE PRODUCT REVIEW
export const updateProductReview = createAsyncThunk(
  'products/updateReview',
  async ({ reviewId, rating, title, text }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/products/reviews/${reviewId}`, {
        rating,
        title,
        text
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE PRODUCT REVIEW
export const deleteProductReview = createAsyncThunk(
  'products/deleteReview',
  async (reviewId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/products/reviews/${reviewId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// CREATE FAQ
export const createFAQ = createAsyncThunk(
  'products/createFAQ',
  async ({ productId, question, answer }, { rejectWithValue }) => {
    try {
      const response = await apiClient.post(`/products/${productId}/faqs`, {
        question,
        answer
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// UPDATE FAQ
export const updateFAQ = createAsyncThunk(
  'products/updateFAQ',
  async ({ faqId, question, answer }, { rejectWithValue }) => {
    try {
      const response = await apiClient.put(`/products/faqs/${faqId}`, {
        question,
        answer
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DELETE FAQ
export const deleteFAQ = createAsyncThunk(
  'products/deleteFAQ',
  async (faqId, { rejectWithValue }) => {
    try {
      const response = await apiClient.delete(`/products/faqs/${faqId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DOWNLOAD PRODUCT IMAGE
export const downloadProductImage = createAsyncThunk(
  'products/downloadImage',
  async (imageId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/media/images/${imageId}/download`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

// DOWNLOAD PRODUCT VIDEO
export const downloadProductVideo = createAsyncThunk(
  'products/downloadVideo',
  async (videoId, { rejectWithValue }) => {
    try {
      const response = await apiClient.get(`/media/videos/${videoId}/download`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);
