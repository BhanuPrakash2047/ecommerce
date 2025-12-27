# Frontend Integration Guide - JWT + OAuth2 Authentication

Complete examples for React, Vue, and vanilla JavaScript frontends to integrate with your authentication system.

---

## 🎯 Core Authentication Flow

```
┌─────────────────┐
│  Frontend App   │
└────────┬────────┘
         │
         ├─→ Traditional Login
         │   ├─ User enters email/password
         │   ├─ POST /api/auth/login
         │   ├─ Receive JWT token
         │   └─ Store in localStorage
         │
         └─→ Google OAuth2 Login
             ├─ Redirect to /oauth2/authorization/google
             ├─ Google handles auth
             ├─ Redirect back with token in URL
             ├─ Extract token
             └─ Store in localStorage

         ↓
    Use token for all requests:
    Authorization: Bearer <token>
```

---

## 🔧 Setup (All Frameworks)

### 1. Create API Client Service

#### API Configuration
```javascript
// api.js or api.ts
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const apiClient = {
  // Set Authorization header for all requests
  setAuthToken(token) {
    if (token) {
      localStorage.setItem('authToken', token);
    } else {
      localStorage.removeItem('authToken');
    }
  },

  getAuthToken() {
    return localStorage.getItem('authToken');
  },

  // Generic fetch with auth header
  async request(endpoint, options = {}) {
    const token = this.getAuthToken();
    
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      // Token invalid or expired
      this.setAuthToken(null);
      window.location.href = '/login';
    }

    return response;
  },

  // Auth endpoints
  login(email, password) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }).then(r => r.json());
  },

  register(email, password) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, role: 'USER' }),
    }).then(r => r.json());
  },

  getProfile() {
    return this.request('/api/auth/profile').then(r => r.json());
  },

  updateProfile(profileData) {
    return this.request('/api/auth/profile', {
      method: 'PUT',
      body: JSON.stringify(profileData),
    }).then(r => r.json());
  },

  validateToken() {
    const token = this.getAuthToken();
    if (!token) return Promise.resolve(false);
    
    return this.request('/api/auth/validate').then(r => r.json());
  },

  refreshToken() {
    return this.request('/api/auth/refresh', {
      method: 'POST',
    }).then(r => r.json());
  },

  logout() {
    this.setAuthToken(null);
  },
};
```

---

## ⚛️ React Integration

### 1. Create Authentication Context

```javascript
// AuthContext.js
import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from './api';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Initialize auth on mount
  useEffect(() => {
    const token = apiClient.getAuthToken();
    if (token) {
      // Validate existing token
      apiClient.validateToken()
        .then(res => {
          if (res.valid) {
            loadUserProfile();
          } else {
            apiClient.logout();
          }
        })
        .catch(() => apiClient.logout())
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const loadUserProfile = async () => {
    try {
      const userData = await apiClient.getProfile();
      setUser(userData);
      setError(null);
    } catch (err) {
      setError(err.message);
      setUser(null);
    }
  };

  const login = async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.login(email, password);
      apiClient.setAuthToken(response.token);
      setUser(response.user);
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.register(email, password);
      apiClient.setAuthToken(response.token);
      setUser(response.user);
      return response;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    apiClient.logout();
    setUser(null);
  };

  const handleOAuth2Redirect = () => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token) {
      apiClient.setAuthToken(token);
      loadUserProfile();
      // Clean URL
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      error,
      login,
      register,
      logout,
      isAuthenticated: !!user,
      handleOAuth2Redirect,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
```

### 2. Login Component

```javascript
// LoginPage.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login, handleOAuth2Redirect, isAuthenticated } = useAuth();

  // Handle OAuth2 callback
  useEffect(() => {
    handleOAuth2Redirect();
  }, []);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard');
    }
  }, [isAuthenticated]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Login to SnackEcommerce</h1>

        {error && <div className="alert alert-error">{error}</div>}

        {/* Traditional Login Form */}
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="divider">OR</div>

        {/* Google OAuth2 Login */}
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          className="btn btn-google"
        >
          🔵 Login with Google
        </a>

        <p className="signup-link">
          Don't have an account? <a href="/register">Sign up</a>
        </p>
      </div>

      <style>{`
        .login-container {
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 100vh;
          background: #f5f5f5;
        }

        .login-card {
          background: white;
          padding: 40px;
          border-radius: 8px;
          box-shadow: 0 2px 10px rgba(0,0,0,0.1);
          width: 100%;
          max-width: 400px;
        }

        .form-group {
          margin-bottom: 16px;
        }

        .form-group label {
          display: block;
          margin-bottom: 8px;
          font-weight: 500;
        }

        .form-group input {
          width: 100%;
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
        }

        button {
          width: 100%;
          padding: 10px;
          background: #007bff;
          color: white;
          border: none;
          border-radius: 4px;
          cursor: pointer;
        }

        .divider {
          text-align: center;
          margin: 20px 0;
          color: #999;
        }

        .btn-google {
          display: block;
          width: 100%;
          padding: 10px;
          background: #db4437;
          color: white;
          text-align: center;
          text-decoration: none;
          border-radius: 4px;
          margin-bottom: 16px;
        }

        .alert-error {
          padding: 12px;
          background: #f8d7da;
          color: #721c24;
          border-radius: 4px;
          margin-bottom: 16px;
        }
      `}</style>
    </div>
  );
}
```

### 3. Protected Route Component

```javascript
// ProtectedRoute.js
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function ProtectedRoute({ children, requiredRole = null }) {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/unauthorized" />;
  }

  return children;
}
```

### 4. Usage in App.js

```javascript
// App.js
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';
import { Dashboard } from './Dashboard';
import { ProtectedRoute } from './ProtectedRoute';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
```

---

## 🍃 Vue 3 Integration

### 1. Create Authentication Composable

```javascript
// composables/useAuth.js
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';

const authToken = ref(localStorage.getItem('authToken'));
const user = ref(null);
const loading = ref(false);
const error = ref(null);

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient = {
  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (authToken.value) {
      headers['Authorization'] = `Bearer ${authToken.value}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      logout();
    }

    return response.json();
  },

  login(email, password) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },

  register(email, password) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, role: 'USER' }),
    });
  },

  getProfile() {
    return this.request('/api/auth/profile');
  },

  validateToken() {
    if (!authToken.value) return Promise.resolve(false);
    return this.request('/api/auth/validate').then(r => r.valid);
  },
};

export function useAuth() {
  const router = useRouter();
  const isAuthenticated = computed(() => !!user.value);

  const setAuthToken = (token) => {
    authToken.value = token;
    if (token) {
      localStorage.setItem('authToken', token);
    } else {
      localStorage.removeItem('authToken');
    }
  };

  const login = async (email, password) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.login(email, password);
      setAuthToken(response.token);
      user.value = response.user;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const register = async (email, password) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.register(email, password);
      setAuthToken(response.token);
      user.value = response.user;
      return response;
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    setAuthToken(null);
    user.value = null;
    router.push('/login');
  };

  const handleOAuth2Redirect = async () => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token) {
      setAuthToken(token);
      const userData = await apiClient.getProfile();
      user.value = userData;
      window.history.replaceState({}, document.title, window.location.pathname);
      router.push('/dashboard');
    }
  };

  const initializeAuth = async () => {
    if (authToken.value) {
      try {
        const isValid = await apiClient.validateToken();
        if (isValid) {
          const userData = await apiClient.getProfile();
          user.value = userData;
        } else {
          logout();
        }
      } catch {
        logout();
      }
    }
  };

  return {
    user,
    authToken,
    loading,
    error,
    isAuthenticated,
    login,
    register,
    logout,
    handleOAuth2Redirect,
    initializeAuth,
  };
}
```

### 2. Login Component

```vue
<!-- LoginPage.vue -->
<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Login to SnackEcommerce</h1>

      <div v-if="error" class="alert-error">{{ error }}</div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Email</label>
          <input
            v-model="email"
            type="email"
            required
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label>Password</label>
          <input
            v-model="password"
            type="password"
            required
            :disabled="loading"
          />
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>
      </form>

      <div class="divider">OR</div>

      <a
        href="http://localhost:8080/oauth2/authorization/google"
        class="btn-google"
      >
        🔵 Login with Google
      </a>

      <p class="signup-link">
        Don't have an account?
        <router-link to="/register">Sign up</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();
const { login, handleOAuth2Redirect, isAuthenticated } = useAuth();

const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

onMounted(async () => {
  await handleOAuth2Redirect();
  if (isAuthenticated.value) {
    router.push('/dashboard');
  }
});

const handleLogin = async () => {
  error.value = '';
  loading.value = true;

  try {
    await login(email.value, password.value);
    router.push('/dashboard');
  } catch (err) {
    error.value = err.message || 'Login failed';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-google {
  display: block;
  width: 100%;
  padding: 10px;
  background: #db4437;
  color: white;
  text-align: center;
  text-decoration: none;
  border-radius: 4px;
  margin-bottom: 16px;
}

.alert-error {
  padding: 12px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 4px;
  margin-bottom: 16px;
}
</style>
```

---

## 🎭 Axios Interceptor (All Frameworks)

```javascript
// axiosSetup.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
});

// Request interceptor - add token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

Usage:
```javascript
import api from './axiosSetup';

// Automatically includes Authorization header
api.get('/api/auth/profile')
  .then(res => console.log(res.data))
  .catch(err => console.error(err));
```

---

## 📝 Vanilla JavaScript

```html
<!DOCTYPE html>
<html>
<head>
  <title>Login</title>
</head>
<body>
  <div id="app"></div>

  <script>
    const API_BASE_URL = 'http://localhost:8080';

    class AuthManager {
      constructor() {
        this.token = localStorage.getItem('authToken');
        this.user = null;
      }

      setToken(token) {
        this.token = token;
        if (token) {
          localStorage.setItem('authToken', token);
        } else {
          localStorage.removeItem('authToken');
        }
      }

      async request(endpoint, options = {}) {
        const headers = {
          'Content-Type': 'application/json',
          ...options.headers,
        };

        if (this.token) {
          headers['Authorization'] = `Bearer ${this.token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
          ...options,
          headers,
        });

        if (response.status === 401) {
          this.logout();
        }

        return response.json();
      }

      async login(email, password) {
        const data = await this.request('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password }),
        });
        this.setToken(data.token);
        this.user = data.user;
        return data;
      }

      async getProfile() {
        return this.request('/api/auth/profile');
      }

      logout() {
        this.setToken(null);
        this.user = null;
        window.location.href = '/login';
      }

      handleOAuth2Redirect() {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        if (token) {
          this.setToken(token);
          window.history.replaceState({}, document.title, '/');
          window.location.href = '/dashboard';
        }
      }
    }

    // Initialize
    const auth = new AuthManager();

    // Login form
    document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('email').value;
      const password = document.getElementById('password').value;

      try {
        await auth.login(email, password);
        window.location.href = '/dashboard';
      } catch (err) {
        alert('Login failed: ' + err.message);
      }
    });

    // Check OAuth2 redirect
    auth.handleOAuth2Redirect();
  </script>
</body>
</html>
```

---

## ✅ Checklist

- [ ] API client configured
- [ ] Authentication context/composable created
- [ ] Login form implemented
- [ ] Protected routes set up
- [ ] Token interceptor configured
- [ ] OAuth2 redirect handling implemented
- [ ] Token validation on app load
- [ ] Error handling implemented
- [ ] Logout functionality added
- [ ] Google OAuth2 link added to UI

---

**Happy coding! Your frontend is now ready to integrate with the authentication system.** 🚀
