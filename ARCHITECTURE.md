# Authentication System Architecture

## 🏗️ System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND APPLICATION                          │
│                        (React/Vue/JavaScript)                           │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
          ┌─────────▼────────┐  ┌──▼──────────┐  ┌─▼───────────────┐
          │  Email/Password  │  │  Get Token  │  │  OAuth2 Google  │
          │      Form        │  │ from URL    │  │    Button       │
          └─────────┬────────┘  └──┬──────────┘  └─┬───────────────┘
                    │              │              │
                    │              │              │
         ┌──────────▼──────────────▼──────────────▼──────────────┐
         │         SPRING BOOT APPLICATION (8080)                │
         └──────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼─────┐  ┌──────────▼──────────┐  ┌───▼───────────┐
   │ /api/auth │  │ /oauth2/authorize  │  │ /login/oauth2 │
   │  /login   │  │      /google       │  │   /code/*     │
   │/register  │  │                    │  │               │
   └────┬─────┘  └──────────┬──────────┘  └───┬───────────┘
        │                   │                  │
        │                   ▼                  │
        │        ┌──────────────────────┐     │
        │        │  Google OAuth2 API   │     │
        │        │   User Credentials   │     │
        │        │    & Consent Flow    │     │
        │        └──────────┬───────────┘     │
        │                   │                 │
        │                   │◄────────────────┘
        │                   │
        │    ┌──────────────▼──────────────┐
        │    │   AUTHENTICATION FILTERS   │
        │    │                            │
        │    │  ┌─────────────────────┐  │
        │    │  │ JWT Filter          │  │
        │    │  │ • Extract token     │  │
        │    │  │ • Validate sig      │  │
        │    │  │ • Set Auth Context  │  │
        │    │  └─────────────────────┘  │
        │    │                            │
        │    │  ┌─────────────────────┐  │
        │    │  │ OAuth2 Handler      │  │
        │    │  │ • Load user service │  │
        │    │  │ • Create/update usr │  │
        │    │  │ • Gen JWT token     │  │
        │    │  └─────────────────────┘  │
        │    └──────────────┬─────────────┘
        │                   │
        └───────────────────▼──────────────────┐
                            │                  │
                    ┌───────▼────────┐         │
                    │ AUTH SERVICE   │         │
                    │                │         │
                    │ • Login logic  │         │
                    │ • Register     │         │
                    │ • Profile mgmt │         │
                    └───────┬────────┘         │
                            │                  │
        ┌───────────────────┼──────────────────▼────┐
        │                   │                       │
    ┌───▼──────┐        ┌──▼─────┐            ┌────▼────┐
    │ USER DB  │        │ JWT    │            │ SECURITY│
    │          │        │ UTIL   │            │ CONFIG  │
    │ • Users  │        │        │            │         │
    │ • Roles  │        │ • Gen  │            │ • CORS  │
    │ • Auth   │        │ • Val  │            │ • Rules │
    │ • Status │        │ • Exp  │            │ • Roles │
    └──────────┘        └────────┘            └─────────┘
```

---

## 🔄 Authentication Flow Diagrams

### JWT Login Flow

```
User              Frontend           AuthController      Database
  │                  │                    │                 │
  │─── Email/Pwd ───→│                    │                 │
  │                  │─ POST /login ─────→│                 │
  │                  │                    │─ Find user ────→│
  │                  │                    │←─ User object ──│
  │                  │                    │                 │
  │                  │                    │ Validate pwd    │
  │                  │                    │                 │
  │                  │                    │ Generate JWT    │
  │                  │← {token, user} ────│                 │
  │                  │                    │                 │
  │ Store in         │                    │                 │
  │ localStorage     │                    │                 │
  │←─ {token, user} ─│                    │                 │
  │                  │                    │                 │
```

### OAuth2 Google Flow

```
User         Frontend        Spring Boot         Google OAuth2        User DB
  │              │               │                    │                  │
  │─ Click Login─→│               │                    │                  │
  │              │─ Redirect ────→│                    │                  │
  │              │   /oauth2/     │                    │                  │
  │              │   google       │─ Redirect ───────→│                  │
  │              │                │   Authorization    │                  │
  │              │                │   Request          │                  │
  │──────── Google Login Screen ───────────────────→│                  │
  │                                                 │                  │
  │         User Grants Consent                    │                  │
  │                                                 │                  │
  │                    │←─ Auth Code ──────────────│                  │
  │                    │                           │                  │
  │                    │─ Exchange Code ──────────→│                  │
  │                    │ for User Info             │                  │
  │                    │                           │                  │
  │                    │←─ User Info ──────────────│                  │
  │                    │ {email, name, pic}        │                  │
  │                    │                           │                  │
  │                    │─ Check if exists ──────────────────────────→│
  │                    │                           │                  │
  │                    │←─ Create/Update ──────────────────────────────│
  │                    │                           │                  │
  │                    │ Generate JWT Token        │                  │
  │                    │                           │                  │
  │←─ Redirect + Token ─│                           │                  │
  │  /oauth2/success    │                           │                  │
  │  ?token=JWT&...     │                           │                  │
  │                    │                           │                  │
  │ Store in           │                           │                  │
  │ localStorage        │                           │                  │
```

### Protected Resource Access

```
Frontend                   Spring Boot              Database
    │                          │                       │
    │ GET /api/resource ──────→│                       │
    │ Headers:                 │                       │
    │ Auth: Bearer JWT         │                       │
    │                          │                       │
    │                    ┌─────▼──────┐               │
    │                    │ JWT Filter  │               │
    │                    │             │               │
    │                    │ 1. Extract  │               │
    │                    │ 2. Validate │               │
    │                    │ 3. Set Auth │               │
    │                    └─────┬──────┘               │
    │                          │                       │
    │                    ┌─────▼──────────┐           │
    │                    │ Security Check │           │
    │                    │                │           │
    │                    │ @PreAuthorize  │           │
    │                    │ isAuth? hasRole│           │
    │                    └─────┬──────────┘           │
    │                          │                       │
    │                  ┌───────▼────────┐            │
    │                  │ Service Logic  │            │
    │                  │                │            │
    │                  │ Access DB ────→│────────────│
    │                  │                │            │
    │                  └───────┬────────┘            │
    │                          │                       │
    │ ←─ {data} ──────────────│                       │
    │                          │                       │
```

---

## 📊 Class Diagram

```
┌─────────────────────────────────────┐
│         SecurityConfig               │
│  (Main Configuration)               │
├─────────────────────────────────────┤
│ - corsConfigurationSource()         │
│ - filterChain(HttpSecurity)         │
│ - passwordEncoder()                 │
└────────┬────────────────────────────┘
         │ uses
         │
    ┌────┴──────────────────────────────────────┐
    │                                           │
┌───▼──────────────────┐       ┌──────────────┴──┐
│ JwtAuthenticationFilter│       │ OAuth2 Handlers │
├──────────────────────┤       ├─────────────────┤
│ - doFilterInternal()  │       │ Success/Failure │
│ - getTokenFromRequest │       │ Handlers        │
│ - shouldNotFilter()   │       └─────────────────┘
└──────────┬───────────┘
           │ uses
           │
    ┌──────▼─────────────┐
    │    JwtUtil         │
    ├────────────────────┤
    │ - generateToken()  │
    │ - validateToken()  │
    │ - getUsername()    │
    │ - isExpired()      │
    └────────────────────┘

┌──────────────────────────────┐
│   OAuth2UserService          │
├──────────────────────────────┤
│ - loadUser()                 │
│ - processOAuth2User()        │
└────┬─────────────────────────┘
     │ uses
     │
┌────▼──────────────────┐
│  UserRepository       │
├──────────────────────┤
│ - findByEmail()      │
│ - existsByEmail()    │
│ - findByEmailAndAuth │
└──────────────────────┘

┌────────────────────────┐
│   AuthController       │
├────────────────────────┤
│ - login()              │
│ - register()           │
│ - getProfile()         │
│ - updateProfile()      │
│ - oauth2Success()      │
│ - validate()           │
│ - refresh()            │
└────────┬───────────────┘
         │ uses
         │
    ┌────▼──────────────┐
    │  AuthService      │
    ├───────────────────┤
    │ - login()         │
    │ - register()      │
    │ - getProfile()    │
    │ - updateProfile() │
    └───────────────────┘
```

---

## 🔐 Security Layers

```
┌──────────────────────────────────────────────┐
│        REQUEST                               │
└──────────────────────┬───────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   CORS Filter               │
        │ Check origin & credentials  │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │   Public Endpoints Check       │
        │ Allow: login, register, oauth2 │
        └──────────────┬─────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │  JWT Filter (if Bearer token)   │
        │ Extract, Validate, Set Auth    │
        └──────────────┬─────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │ OAuth2 Filter (if OAuth2 request)│
        │ Load user, generate JWT        │
        └──────────────┬─────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │  Authorization Filter           │
        │ @PreAuthorize checks           │
        │ hasRole(), isAuthenticated()    │
        └──────────────┬─────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │  Controller Method               │
        │ Process Request                 │
        └──────────────┬─────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │  RESPONSE                       │
        └─────────────────────────────────┘
```

---

## 📈 Data Flow

### Login Process Data Flow

```
User Input
    │
    ├─→ {email, password}
    │
    ▼
LoginRequest (validated)
    │
    ├─→ AuthController.login()
    │
    ▼
AuthService.login()
    │
    ├─→ UserRepository.findByEmail()
    │
    ├─→ PasswordEncoder.matches()
    │
    ├─→ JwtUtil.generateToken()
    │
    ▼
JwtResponse {
    token: "JWT_STRING",
    user: {
        id, email, role,
        active, createdAt
    }
}
    │
    ├─→ Frontend
    │
    ├─→ localStorage.setItem('token', JWT)
```

### Protected Request Data Flow

```
Frontend + JWT Token
    │
    ├─→ Authorization: Bearer JWT_STRING
    │
    ▼
JwtAuthenticationFilter
    │
    ├─→ Extract token from header
    │
    ├─→ JwtUtil.validateToken()
    │
    ├─→ JwtUtil.getUsernameFromToken()
    │
    ├─→ SecurityContext.setAuthentication()
    │
    ▼
Controller Method
    │
    ├─→ @PreAuthorize verification
    │
    ├─→ authentication.getName() = user email
    │
    ▼
Process Request & Response
```

---

## 🔄 Token Lifecycle

```
TOKEN GENERATION
    │
    ├─ Login: email + password
    │   ▼
    │   JwtUtil.generateToken(email)
    │   ├─ setSubject(email)
    │   ├─ setIssuedAt(now)
    │   ├─ setExpiration(now + 24h)
    │   ├─ signWith(SECRET_KEY)
    │   └─ compact() → JWT_STRING
    │
    └─ OAuth2: Google authentication
        ▼
        OAuth2SuccessHandler
        ├─ Get user email from Google
        ├─ Create/update user in DB
        └─ Generate same JWT token

TOKEN USAGE (24 hours)
    │
    ├─ Store in localStorage
    │
    ├─ Send in Authorization header
    │   └─ Authorization: Bearer <JWT>
    │
    └─ JwtFilter validates on each request
        ├─ Extract token
        ├─ Verify signature
        ├─ Check expiration
        └─ Set authentication

TOKEN REFRESH
    │
    └─ POST /api/auth/refresh
        ├─ Validate current token
        ├─ Generate new token
        └─ Return new JWT

TOKEN EXPIRATION
    │
    ├─ After 24 hours
    │
    ├─ JwtUtil.isTokenExpired() = true
    │
    ├─ JwtFilter detects expiration
    │
    └─ Return 401 Unauthorized
        └─ Frontend redirects to login
```

---

## 🗄️ Database Schema

```
┌─────────────────────────────────┐
│           users                 │
├─────────────────────────────────┤
│ id (PK, AUTO_INCREMENT)         │
│ email (UNIQUE, NOT NULL)        │
│ password (VARCHAR, NULLABLE)    │ ← NULL for OAuth users
│ auth_provider (ENUM)            │ ← LOCAL or GOOGLE
│ role (ENUM)                     │ ← USER, ADMIN, etc.
│ active (BOOLEAN, DEFAULT: true) │
│ created_at (TIMESTAMP)          │
│ updated_at (TIMESTAMP)          │
│ last_login (TIMESTAMP, NULL)    │
└─────────────────────────────────┘

Indexes:
- UNIQUE (email)
- INDEX (auth_provider)
- INDEX (active)
- INDEX (created_at)
```

---

## 🔀 Decision Tree for Authentication

```
User makes request
    │
    ├─ Is Authorization header present?
    │
    ├─ YES ──┐
    │        └─→ Has Bearer token?
    │           │
    │           ├─ YES ──┐
    │           │        └─→ JwtAuthenticationFilter
    │           │           ├─ Validate signature
    │           │           ├─ Check expiration
    │           │           └─ Set authentication
    │           │
    │           └─ NO ──┐
    │                   └─ Continue (no JWT)
    │
    └─ NO ──┐
            └─→ Is OAuth2 request?
                │
                ├─ YES ──┐
                │        └─→ OAuth2 Handler
                │           ├─ Redirect to Google
                │           ├─ Process callback
                │           └─ Generate JWT
                │
                └─ NO ──┐
                        └─→ Is public endpoint?
                            │
                            ├─ YES ──→ Allow
                            │
                            └─ NO ──→ Check Auth
                                     ├─ Authenticated?
                                     │  ├─ YES ──→ Check role
                                     │  │
                                     │  └─ NO ──→ 401 Unauthorized
                                     │
                                     └─ @PreAuthorize
                                        ├─ Satisfied? ──→ Proceed
                                        │
                                        └─ NOT Satisfied ──→ 403 Forbidden
```

---

## 🌐 CORS Configuration

```
Request from: http://localhost:3000

    │
    ▼
SecurityConfig.corsConfigurationSource()

    │
    ├─ Allowed Origins:
    │  ├─ http://localhost:3000
    │  ├─ http://localhost:5173
    │  ├─ http://127.0.0.1:3000
    │  └─ http://127.0.0.1:5173
    │
    ├─ Allowed Methods:
    │  ├─ GET, POST, PUT, DELETE
    │  ├─ OPTIONS, PATCH
    │
    ├─ Allowed Headers:
    │  ├─ Content-Type
    │  ├─ Authorization
    │  └─ *
    │
    ├─ Allow Credentials: true
    │
    └─ Max Age: 3600s
```

---

This architecture ensures:
✅ **Security** - Multiple validation layers  
✅ **Flexibility** - Both JWT and OAuth2  
✅ **Scalability** - Stateless design  
✅ **User Experience** - Quick OAuth2 login  
✅ **Maintainability** - Clear separation of concerns  

