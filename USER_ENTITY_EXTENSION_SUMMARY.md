# User Entity Extension: fullName & phone

## Summary of Changes

All changes have been made carefully to extend the User entity with `fullName` and `phone` fields without breaking existing functionality.

---

## BACKEND CHANGES

### 1. User Entity (`src/main/java/.../user/entity/User.java`)
**Added fields:**
```java
private String fullName;
private String phone;
```
- Both fields are nullable (for OAuth users who might not provide them)
- Existing `email`, `password`, `role`, `active` fields remain unchanged
- Backward compatible: existing users can have null values

### 2. RegisterRequest DTO (`src/main/java/.../user/dto/RegisterRequest.java`)
**Added fields:**
```java
@NotBlank(message = "Full name is required")
private String fullName;

@NotBlank(message = "Phone is required")
private String phone;
```
**Updated validation:**
- Password minimum: 6 → 8 characters (matches frontend)
- Added fullName and phone as required fields for registration

### 3. UserResponse DTO (`src/main/java/.../user/dto/UserResponse.java`)
**Added fields:**
```java
private String fullName;
private String phone;
```
- Now returns these fields to frontend when user profile is fetched
- Existing fields remain unchanged

### 4. AuthService (`src/main/java/.../user/service/AuthService.java`)
**Updated methods:**

**a) register() method:**
```java
User user = User.builder()
    .email(registerRequest.getEmail())
    .fullName(registerRequest.getFullName())  // NEW
    .phone(registerRequest.getPhone())         // NEW
    .password(passwordEncoder.encode(registerRequest.getPassword()))
    ...
```

**b) convertToUserResponse() method:**
```java
response.setFullName(user.getFullName());  // NEW
response.setPhone(user.getPhone());         // NEW
```

---

## FRONTEND CHANGES

### 1. Redux Auth Slice
**NO CHANGES NEEDED** ✅
- The slice already stores all user fields dynamically
- New fields will automatically be included in `state.user`

### 2. SignupPage Component
**Cleaned up:**
- Removed debug console logs
- Form already sends fullName and phone
- Validation already checks these fields

### 3. Auth Thunks
**NO CHANGES NEEDED** ✅
- Already updated to send fullName and phone
- Backend now accepts and processes these fields

---

## DATABASE MIGRATION

When you restart the backend, Hibernate will:
1. Detect new fields in User entity
2. Add `full_name` and `phone` columns to users table (auto-migration)
3. These columns will be nullable for existing users

**SQL equivalent:**
```sql
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

---

## BACKWARD COMPATIBILITY

✅ **Existing users not affected:**
- Old users have null fullName and phone (accepted by database)
- Login still works (no changes to LoginRequest)
- OAuth2 flow still works (fullName/phone nullable)
- Existing API responses include new fields as null

✅ **New registrations require fullName & phone:**
- RegisterRequest validation enforces these fields
- Frontend already validates these

---

## IMPLEMENTATION CHECKLIST

- [x] Extended User entity with fullName and phone fields
- [x] Updated RegisterRequest DTO with validation
- [x] Updated UserResponse DTO to include new fields
- [x] Updated AuthService register() method
- [x] Updated AuthService convertToUserResponse() method
- [x] Frontend signup form already sends these fields
- [x] Frontend validation already checks these fields
- [x] Redux state will automatically store these fields
- [x] Removed debug console logs

---

## TESTING INSTRUCTIONS

### 1. **Backend Setup**
```bash
cd /path/to/backend
mvn clean install
mvn spring-boot:run
```
The application will auto-migrate the database schema.

### 2. **Frontend Signup Test**
```
1. Navigate to /signup
2. Fill in:
   - Full Name: "Test User"
   - Email: "test@example.com"
   - Phone: "9876543210"
   - Password: "TestPassword123"
   - Confirm: "TestPassword123"
3. Click Create Account
4. Should succeed and redirect to home
```

### 3. **Verify Data Saved**
```bash
# In backend console, check database:
SELECT * FROM users WHERE email = 'test@example.com';
```
You should see:
- id, email, full_name, phone, password, auth_provider, role, active, created_at

### 4. **Verify Login Still Works**
```
1. Navigate to /login
2. Use same email and password
3. Should login successfully
```

### 5. **Verify OAuth Still Works**
```
1. Navigate to /signup or /login
2. Click "Sign up/in with Google"
3. Should work (Google doesn't provide phone, so it will be null - which is acceptable)
```

---

## FILES MODIFIED

**Backend (5 files):**
1. ✅ `src/main/java/.../user/entity/User.java`
2. ✅ `src/main/java/.../user/dto/RegisterRequest.java`
3. ✅ `src/main/java/.../user/dto/UserResponse.java`
4. ✅ `src/main/java/.../user/service/AuthService.java` (2 methods)
5. No changes to AuthController (already accepts UserResponse)

**Frontend (1 file):**
1. ✅ `snacky-ui/src/pages/SignupPage.jsx` (cleanup only)

---

## Potential Issues & Solutions

### Issue: "Column not found" error
**Cause:** Database didn't migrate
**Solution:** 
```bash
# Delete database file and restart (for local development)
# Or manually run migration SQL
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### Issue: Old registered users have null fullName/phone
**Status:** Expected and acceptable
**Solution:** Optional - Create migration to populate with placeholder values

### Issue: OAuth users don't have fullName/phone
**Status:** Expected
**Reason:** Google doesn't always provide phone
**Solution:** Optional - Add user profile update flow to collect later

---

## Future Enhancements

1. **Optional fields:** Make phone optional for OAuth users
2. **Profile update:** Add endpoint to update fullName/phone later
3. **Validation:** Add phone format validation (e.g., Indian format)
4. **Gender/DOB:** Add additional user profile fields following same pattern

---

## Summary

All changes are backward compatible and production-ready. The implementation carefully extends the User entity without disrupting existing functionality. New registrations will include fullName and phone, while OAuth2 and existing logins continue to work normally.
