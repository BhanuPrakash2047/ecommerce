# JWT + OAuth2 Authentication - Quick Reference

## 🚀 Quick Start (5 minutes)

### 1. Get Google Credentials
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create project → Enable OAuth2 → Create Web credentials
3. Add redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret

### 2. Update application.properties
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_SECRET

jwt.secret=your-secret-key-at-least-32-characters-long
```

### 3. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

---

## 📡 API Reference

### Public Endpoints

| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/auth/login` | `{email, password}` | `{token, user}` |
| POST | `/api/auth/register` | `{email, password, role}` | `{token, user}` |
| GET | `/oauth2/authorization/google` | - | Redirect to Google |

### Protected Endpoints (Add Header: `Authorization: Bearer <token>`)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/auth/profile` | Get current user |
| PUT | `/api/auth/profile` | Update user |
| POST | `/api/auth/refresh` | Get new token |
| POST | `/api/auth/validate` | Verify token validity |

---

## 🔐 Authentication Methods

### Method 1: JWT (Email/Password)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "user": { "id": 1, "email": "user@example.com", ... }
# }
```

### Method 2: OAuth2 (Google)
```bash
# Open in browser:
http://localhost:8080/oauth2/authorization/google

# After login, redirected to:
http://localhost:8080/oauth2/success?token=<JWT>&email=<email>
```

---

## 💾 Using Token in Requests

### Headers
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIi...
```

### cURL Example
```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### JavaScript Fetch
```javascript
const token = localStorage.getItem('token');

fetch('/api/auth/profile', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
}).then(r => r.json()).then(console.log);
```

---

## 🛡️ Authorization Levels

```java
@PreAuthorize("isAuthenticated()")     // Any authenticated user
@PreAuthorize("hasRole('ADMIN')")      // Only ADMIN role
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // Multiple roles
```

---

## ❓ Common Questions

**Q: Do I need both JWT and OAuth2?**
- No, pick one or offer both. Both ultimately use JWT for API requests.

**Q: How long is the token valid?**
- Default: 24 hours. Change in `jwt.expiration` property.

**Q: How to logout?**
- Simply delete the token from client storage. No server-side logout needed.

**Q: Can I add other OAuth2 providers?**
- Yes! Add GitHub/Facebook config to `application.properties` and update `OAuth2UserService`.

**Q: Token works in Postman but not in browser?**
- Check CORS configuration in `SecurityConfig.java`. Add your frontend URL.

---

## 🐛 Debug Logging

Enable debug logs in `application.properties`:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.snackecommerce=DEBUG
```

---

## 📦 Dependencies Included

✓ Spring Security  
✓ Spring OAuth2 Client  
✓ JWT (JJWT)  
✓ Validation  

No additional dependencies needed!

---

## 🔗 Important URLs

- Docs: [OAUTH2_JWT_SETUP_GUIDE.md](OAUTH2_JWT_SETUP_GUIDE.md)
- Login: `http://localhost:8080/oauth2/authorization/google`
- Register: `POST /api/auth/register`
- Profile: `GET /api/auth/profile`

---

**Time to implement: ~15-20 minutes**  
**Difficulty: Easy-Medium**  
**Support: Check setup guide for troubleshooting**
