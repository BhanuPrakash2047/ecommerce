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
      const response = await apiClient.get(`/products/${productId}/images`);
      return response.data;
    } catch (error) {
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
