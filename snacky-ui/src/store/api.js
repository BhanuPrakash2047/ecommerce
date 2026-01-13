import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

// Public endpoints that don't require authentication
const PUBLIC_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/products',
  '/media/',
  '/oauth2/',
  '/login/'
];

// Helper function to check if an endpoint is public
const isPublicEndpoint = (url) => {
  // Extract path from URL (remove query params and base URL)
  const path = new URL(url, window.location.origin).pathname;
  return PUBLIC_ENDPOINTS.some(endpoint => path.includes(endpoint));
};

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle responses
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle 401 - Token expired or invalid
    // Only redirect to login if the request was to a protected endpoint
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || '';
      
      // Only redirect to login if this is NOT a public endpoint
      if (!isPublicEndpoint(requestUrl)) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    
    // Return error with specific message
    const errorMessage = 
      error.response?.data?.message || 
      error.response?.data?.error || 
      error.message || 
      'An unexpected error occurred';
    
    return Promise.reject({
      status: error.response?.status,
      message: errorMessage,
      data: error.response?.data
    });
  }
);

export default apiClient;
