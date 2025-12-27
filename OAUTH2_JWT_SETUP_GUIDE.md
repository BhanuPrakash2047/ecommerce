# JWT + OAuth2 Google Dual Authentication Setup Guide

## 📋 Overview

This guide provides complete instructions to set up dual authentication in your Spring Boot ecommerce application:
- **JWT Authentication**: Traditional username/password login with JWT tokens
- **OAuth2 Google Login**: One-click Google login

Both methods work seamlessly together with the same authorization mechanism (`@PreAuthorize`, roles, etc.).

---

## 🎯 How It Works

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     User Request                              │
└─────────────────────────────────────────────────────────────┘
                              │
                    Has Bearer Token?
                    /              \
                  YES              NO
                   │                │
                   ├─→ JWT Filter   │
                   │   Validates    │
                   │   Token        │
                   │                │
                   │           Is OAuth2 Request?
                   │           /              \
                   │         YES              NO
                   │          │                │
                   │          ├─→ OAuth2      │
                   │          │   Handler    ├─→ 401 Unauthorized
                   │          │
                   │    Generate JWT Token
                   │          │
                   └──────────┴─→ Authenticated ✓
```

### Key Points:

1. **JWT Requests**: Send token in `Authorization: Bearer <token>` header
2. **OAuth2 Requests**: Browser redirects to Google, then to callback URL with token
3. **Both methods**: Return JWT token to client for subsequent requests
4. **@PreAuthorize**: Works with both authentication methods

---

## 📦 Step 1: Dependencies Already Added

The `pom.xml` has been updated with:
```xml
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- jjwt (JWT library)
- spring-boot-starter-validation
```

No additional Maven dependencies needed.

---

## 🔐 Step 2: Google OAuth2 Setup (MOST IMPORTANT!)

### 2.1 Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click **Create Project**
3. Enter project name (e.g., "SnackEcommerce") and click **Create**
4. Wait for project creation to complete

### 2.2 Enable OAuth2 Consent Screen

1. From the menu, go to **APIs & Services → OAuth consent screen**
2. Select **External** user type
3. Click **Create**
4. Fill in required fields:
   - **App name**: SnackEcommerce
   - **User support email**: Your email
   - **Developer contact**: Your email
5. Click **Save and Continue**
6. **Scopes**: Click **Add or Remove Scopes**
   - Add: `openid`, `email`, `profile`
   - Click **Update**
7. Click **Save and Continue** → **Save and Continue** again
8. Review and click **Back to Dashboard**

### 2.3 Create OAuth2 Credentials

1. Go to **APIs & Services → Credentials**
2. Click **+ Create Credentials → OAuth 2.0 Client IDs**
3. Choose **Web Application**
4. Fill in:
   - **Name**: SnackEcommerce Web Client
   - **Authorized JavaScript origins**:
     ```
     http://localhost:8080
     http://localhost:3000
     http://localhost:5173
     ```
   - **Authorized redirect URIs**:
     ```
     http://localhost:8080/login/oauth2/code/google
     ```
5. Click **Create**
6. Copy the **Client ID** and **Client Secret**

---

## ⚙️ Step 3: Configure Application Properties

### 3.1 Update `application.properties`

The file has been pre-configured. Update these values:

```properties
# Database (update with your credentials)
spring.datasource.url=jdbc:mysql://localhost:3306/snack_ecommerce?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password

# JWT Secret (⚠️ CHANGE THIS IN PRODUCTION)
jwt.secret=your-secret-key-change-this-in-production-environment-with-at-least-32-characters

# OAuth2 Google Credentials (from Step 2.3)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID_HERE
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET_HERE
```

### 3.2 For Production

Create `application-prod.properties`:
```properties
jwt.secret=<generate-secure-random-string-minimum-32-chars>
spring.security.oauth2.client.registration.google.client-id=<production-google-client-id>
spring.security.oauth2.client.registration.google.client-secret=<production-google-secret>
server.ssl.key-store=<path-to-keystore>
server.ssl.key-store-password=<keystore-password>
```

---

## 🛣️ Step 4: API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. **Traditional Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER",
    "active": true,
    "createdAt": "2025-12-27T10:00:00"
  }
}
```

#### 2. **User Registration**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "password123",
  "role": "USER"
}

Response: Same as login response
```

#### 3. **Google OAuth2 Login**
```
GET http://localhost:8080/oauth2/authorization/google
```

After Google login, user is redirected to:
```
http://localhost:8080/oauth2/success?token=<jwt-token>&email=<user-email>
```

Your frontend should:
1. Extract the `token` from URL parameter
2. Store it in localStorage: `localStorage.setItem('token', token)`
3. Redirect to dashboard

#### 4. **OAuth2 Error Callback**
```
GET /api/auth/oauth2/error?message=<error-message>
```

### Protected Endpoints (Authentication Required)

#### 1. **Get User Profile**
```http
GET /api/auth/profile
Authorization: Bearer <your-jwt-token>

Response:
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER",
  "active": true,
  "createdAt": "2025-12-27T10:00:00"
}
```

#### 2. **Update Profile**
```http
PUT /api/auth/profile
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "role": "ADMIN"
}

Response: Updated user object
```

#### 3. **Validate Token**
```http
POST /api/auth/validate
Authorization: Bearer <your-jwt-token>

Response:
{
  "valid": true,
  "email": "user@example.com"
}
```

#### 4. **Refresh Token**
```http
POST /api/auth/refresh
Authorization: Bearer <current-token>

Response:
{
  "token": "<new-jwt-token>"
}
```

---

## 💾 Step 5: Database Setup

### Create MySQL Database

```sql
CREATE DATABASE snack_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE snack_ecommerce;

-- Tables will be auto-created by Hibernate on first run
-- Verify by checking if 'users' table exists:
-- SHOW TABLES;
```

---

## 🚀 Step 6: Running the Application

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/snack-ecommerce-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

---

## 🌐 Step 7: Frontend Integration

### React/Vue Frontend Example

#### Setup JWT Authentication:

```javascript
// Store token after login/OAuth2
const token = response.token;
localStorage.setItem('token', token);

// Send token with requests
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};

fetch('/api/auth/profile', { headers })
  .then(res => res.json())
  .then(data => console.log('User:', data));
```

#### OAuth2 Login Button:

```html
<!-- Simple Google Login Button -->
<a href="http://localhost:8080/oauth2/authorization/google" 
   class="btn btn-google">
  Login with Google
</a>

<!-- Handle callback -->
<script>
  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');
  
  if (token) {
    localStorage.setItem('token', token);
    window.location.href = '/dashboard';
  }
</script>
```

### Axios Interceptor Example:

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

// Add token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 🛡️ Step 8: Using @PreAuthorize

### Control Access in Controllers

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  // Only authenticated users
  @GetMapping("/users")
  @PreAuthorize("isAuthenticated()")
  public List<User> getUsers() { ... }

  // Only ADMIN role
  @PostMapping("/users/ban/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public void banUser(@PathVariable Long userId) { ... }

  // Multiple roles
  @PutMapping("/settings")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public void updateSettings() { ... }

  // Custom logic
  @DeleteMapping("/orders/{orderId}")
  @PreAuthorize("hasRole('ADMIN') or #orderId == authentication.principal.id")
  public void deleteOrder(@PathVariable Long orderId) { ... }
}
```

---

## ❓ FAQ

### Q: Do I need to send a token after OAuth2 Google login?

**A: YES!** After Google login, the user receives a JWT token via the redirect URL. This token should be used for all subsequent API requests, just like the traditional JWT login.

**Flow:**
1. User clicks "Login with Google"
2. Redirected to Google login
3. After success, redirected to: `http://localhost:8080/oauth2/success?token=<JWT>&email=<email>`
4. Frontend extracts token and stores it
5. Uses token in `Authorization: Bearer <token>` for all requests

---

### Q: What's the difference between JWT and OAuth2?

**JWT (Traditional Login):**
- Client sends email + password
- Server validates and returns JWT token
- Client uses token for authenticated requests
- Stateless, no server-side session

**OAuth2 (Google Login):**
- Client redirects to Google
- Google handles authentication
- Server receives user info from Google
- Server generates JWT token
- Client uses same JWT token for requests

**Bottom line:** Both methods ultimately use JWT tokens for authenticated requests!

---

### Q: How long are tokens valid?

**Default:** 24 hours (86400000 milliseconds)

Modify in `application.properties`:
```properties
jwt.expiration=604800000  # 7 days
```

Use `/api/auth/refresh` endpoint to get new token before expiration.

---

### Q: How do I handle token expiration in frontend?

```javascript
// Check token validity
function isTokenExpired(token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
}

// Refresh if needed
async function ensureValidToken() {
  const token = localStorage.getItem('token');
  
  if (isTokenExpired(token)) {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      return data.token;
    } else {
      // Token refresh failed, redirect to login
      window.location.href = '/login';
    }
  }
  
  return token;
}
```

---

### Q: Can I use OAuth2 without JWT?

**Not recommended in this setup.** Our implementation generates JWT tokens after OAuth2 authentication for consistency and stateless API design. This allows:
- Uniform authentication across all endpoints
- Easy mobile app integration
- Microservices compatibility

---

### Q: How do I add other OAuth2 providers (GitHub, Facebook)?

Add to `application.properties`:

```properties
# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=user:email

# Facebook OAuth2
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FB_CLIENT_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FB_CLIENT_SECRET
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
```

Update `OAuth2UserService.java` to handle additional providers.

---

## 🐛 Troubleshooting

### Issue: "Invalid client id"

**Solution:**
- Verify `client-id` and `client-secret` in `application.properties`
- Check they match Google Cloud Console credentials
- Ensure spaces/typos are corrected

### Issue: Redirect URI mismatch

**Solution:**
- Ensure redirect URI in code matches Google Cloud Console exactly:
  ```
  http://localhost:8080/login/oauth2/code/google
  ```

### Issue: CORS errors

**Solution:**
- Check `SecurityConfig.java` CORS configuration
- Add your frontend URL to allowed origins:
  ```properties
  # In SecurityConfig.java
  configuration.setAllowedOrigins(Arrays.asList("http://your-frontend:3000"));
  ```

### Issue: "User not found" after OAuth2 login

**Solution:**
- Check that `OAuth2UserService.java` creates user in database
- Verify database connectivity
- Check logs for SQL errors

### Issue: JWT token invalid/expired

**Solution:**
- Verify `jwt.secret` is at least 32 characters
- Check token not truncated in headers/storage
- Use `/api/auth/refresh` to get new token

---

## 📝 File Structure

```
src/main/java/com/snackecommerce/
├── common/
│   ├── config/
│   │   ├── JwtAuthenticationFilter.java      ✓ JWT validation
│   │   ├── OAuth2SuccessHandler.java         ✓ OAuth2 success callback
│   │   ├── OAuth2FailureHandler.java         ✓ OAuth2 failure callback
│   │   ├── OAuth2UserService.java            ✓ Load OAuth2 user
│   │   ├── CustomOAuth2User.java             ✓ Custom OAuth2 user wrapper
│   │   └── SecurityConfig.java               ✓ Main security configuration
│   ├── exception/
│   │   ├── OAuth2AuthenticationException.java ✓
│   │   ├── InvalidJwtTokenException.java     ✓
│   │   └── GlobalExceptionHandler.java       ✓
│   └── util/
│       └── JwtUtil.java                      ✓ JWT generation/validation
├── user/
│   ├── controller/
│   │   └── AuthController.java               ✓ Auth endpoints
│   ├── service/
│   │   └── AuthService.java                  ✓ Auth business logic
│   ├── repository/
│   │   └── UserRepository.java               ✓ User queries
│   ├── entity/
│   │   └── User.java                         ✓ User entity
│   ├── enums/
│   │   ├── UserRole.java
│   │   └── AuthProvider.java
│   └── dto/
│       ├── UserResponse.java
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── JwtResponse.java

resources/
└── application.properties                    ✓ Configuration
```

---

## ✅ Verification Checklist

- [ ] Google Cloud Project created and OAuth2 credentials obtained
- [ ] `client-id` and `client-secret` added to `application.properties`
- [ ] Database created: `snack_ecommerce`
- [ ] MySQL credentials updated in `application.properties`
- [ ] `jwt.secret` changed to secure random string (32+ chars)
- [ ] Application builds successfully: `mvn clean install`
- [ ] Application runs: `mvn spring-boot:run`
- [ ] Can register user: `POST /api/auth/register`
- [ ] Can login: `POST /api/auth/login`
- [ ] Can access profile with JWT: `GET /api/auth/profile` with Bearer token
- [ ] Can login with Google: Visit `http://localhost:8080/oauth2/authorization/google`
- [ ] Can refresh token: `POST /api/auth/refresh` with Bearer token

---

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring OAuth2 Guide](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [JWT Documentation](https://jwt.io/)
- [Google OAuth2 Setup](https://developers.google.com/identity/protocols/oauth2)

---

**Happy coding! 🚀**
