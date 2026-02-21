# 🚀 Quick Startup Guide

## All fixes have been applied! Follow these steps to start your application:

---

## ✅ Pre-Flight Checklist

Before starting, ensure:

1. **Redis is running:**
   ```powershell
   docker ps | findstr redis
   ```
   If not running:
   ```powershell
   docker start redis
   # OR start a new container
   docker run -d -p 6379:6379 --name redis redis:alpine
   ```

2. **MySQL is running (XAMPP):**
   - Open XAMPP Control Panel
   - Start MySQL service

3. **Database exists:**
   - Database: `agroprotect_db`
   - Should be created automatically on first run

---

## 🏃 Start the Application

### Option 1: From IntelliJ IDEA (Recommended)
1. Open the project in IntelliJ
2. Locate `AgroProtectApplication.java`
3. Right-click → "Run 'AgroProtectApplication'"
4. Wait for the application to start (look for "Started AgroProtectApplication" in console)

### Option 2: From Command Line
```powershell
cd C:\Users\hsini\OneDrive\Bureau\S2_Project\AgroProtect\PI
mvn spring-boot:run
```

---

## 🧪 Test the Application

### Step 1: Get an Access Token

Login to get a valid JWT token:

```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "expert@agroprotect.com",
  "password": "Expert@123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "uuid": "a12dca5d-7e12-4499-8100-f3266c639464",
      "email": "expert@agroprotect.com",
      "roles": ["EXPERT"]
    }
  }
}
```

**Copy the `accessToken` value!**

---

### Step 2: Create a Sinistre

Use the token from Step 1:

```bash
POST http://localhost:8080/api/v1/microassurance/sinistres
Authorization: Bearer <paste-your-token-here>
Content-Type: application/json

{
  "typeSinistre": "SECHERESSE",
  "description": "Severe drought causing 60% crop loss in Kairouan region",
  "montantEstime": 5000.00,
  "localisation": "Kairouan",
  "contratAssuranceId": 1
}
```

**Expected Response:**
```json
{
  "id": 1,
  "typeSinistre": "SECHERESSE",
  "description": "Severe drought causing 60% crop loss in Kairouan region",
  "statut": "DECLARE",
  "dateDeclaration": "2026-02-21T22:45:00.000Z",
  "montantEstime": 5000.00,
  "localisation": "Kairouan",
  "contratAssuranceId": 1,
  "createdByUserId": 1,
  "createdAt": "2026-02-21T22:45:00.000Z",
  "updatedAt": "2026-02-21T22:45:00.000Z"
}
```

✅ **Success!** Your sinistre has been created!

---

### Step 3: List All Sinistres

```bash
GET http://localhost:8080/api/v1/microassurance/sinistres
Authorization: Bearer <your-token>
```

---

### Step 4: Get a Specific Sinistre

```bash
GET http://localhost:8080/api/v1/microassurance/sinistres/1
Authorization: Bearer <your-token>
```

---

## 📋 Available Endpoints

### Authentication
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/register` - Register
- `POST /api/v1/auth/refresh` - Refresh token

### Sinistres
- `POST /api/v1/microassurance/sinistres` - Create sinistre
- `GET /api/v1/microassurance/sinistres` - List sinistres
- `GET /api/v1/microassurance/sinistres/{id}` - Get sinistre by ID
- `PATCH /api/v1/microassurance/sinistres/{id}/validate` - Validate sinistre (EXPERT/ADMIN)
- `PATCH /api/v1/microassurance/sinistres/{id}/refuse` - Refuse sinistre (EXPERT/ADMIN)

### Indemnisations
- `POST /api/v1/microassurance/sinistres/{sinistreId}/indemnisations` - Create indemnisation (ADMIN)
- `GET /api/v1/microassurance/indemnisations` - List indemnisations
- `GET /api/v1/microassurance/indemnisations/{id}` - Get indemnisation by ID
- `PATCH /api/v1/microassurance/indemnisations/{id}/pay` - Process payment (ADMIN)

---

## 🔍 Verify Application Health

### Check Health Endpoint
```bash
GET http://localhost:8080/api/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "database": "OK",
  "redis": "OK"
}
```

---

## 📖 API Documentation

Once the application is running, access Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

This provides an interactive API documentation where you can:
- View all available endpoints
- Test endpoints directly from the browser
- See request/response schemas

---

## ❗ Troubleshooting

### Issue: Application fails to start
**Solution:**
1. Check if MySQL is running (XAMPP)
2. Check if Redis is running (`docker ps`)
3. Check console logs for specific errors

### Issue: Token expired
**Solution:**
- Tokens expire after 15 minutes
- Get a new token using the login endpoint

### Issue: 401 Unauthorized
**Solution:**
- Ensure you're including the token in the Authorization header
- Format: `Authorization: Bearer <token>`
- Verify the token is not expired

### Issue: 403 Forbidden
**Solution:**
- Your user role doesn't have permission for this endpoint
- Some endpoints require ADMIN or EXPERT role

### Issue: Database connection error
**Solution:**
- Start MySQL in XAMPP
- Check database credentials in `application.yml`
- Default: `root` user with empty password

### Issue: Redis connection error
**Solution:**
```powershell
docker start redis
# OR
docker run -d -p 6379:6379 --name redis redis:alpine
```

---

## 📝 Default Test Users

The application creates these default users on startup:

### Admin User
- **Email:** admin@agroprotect.com
- **Password:** Admin@123
- **Role:** ADMIN

### Expert User
- **Email:** expert@agroprotect.com
- **Password:** Expert@123
- **Role:** EXPERT

### Regular User
- **Email:** user@agroprotect.com
- **Password:** User@123
- **Role:** USER

---

## 🎯 Next Steps

1. ✅ Start the application
2. ✅ Test authentication
3. ✅ Create a sinistre
4. ✅ Explore other endpoints
5. ✅ Build your frontend integration

---

## 💡 Tips

- Use Postman or Thunder Client (VS Code) for API testing
- Save your tokens to avoid logging in repeatedly
- Check application logs for debugging
- Use Swagger UI for exploring the API

---

**You're all set! Happy coding! 🎉**
