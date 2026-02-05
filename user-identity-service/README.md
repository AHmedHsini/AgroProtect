# User Identity Microservice

Production-grade User & Identity microservice for the Agricultural Microfinance Platform.

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                    User Identity Service                        │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ Controllers │  │   Services   │  │    Repositories     │   │
│  ├─────────────┤  ├──────────────┤  ├─────────────────────┤   │
│  │ Auth        │  │ AuthService  │  │ UserRepository      │   │
│  │ User        │──│ UserService  │──│ RefreshTokenRepo    │   │
│  │ Biometric   │  │ TokenService │  │ BiometricDataRepo   │   │
│  │ Admin       │  │ OtpService   │  │ AuditLogRepo        │   │
│  │ Health      │  │ BiometricSvc │  │ ...                 │   │
│  └─────────────┘  └──────────────┘  └─────────────────────┘   │
├────────────────────────────────────────────────────────────────┤
│  Security Layer: JWT (RS256) | Rate Limiting | Encryption      │
└────────────────────────────────────────────────────────────────┘
           │                    │                    │
           ▼                    ▼                    ▼
       ┌───────┐           ┌───────┐           ┌──────────┐
       │ MySQL │           │ Redis │           │ ML Face  │
       │  DB   │           │ Cache │           │ Service  │
       └───────┘           └───────┘           └──────────┘
```

## 🚀 Features

- **Authentication**: Email/password, Google OAuth2, Phone OTP
- **JWT Security**: RS256 asymmetric keys, refresh token rotation
- **Session Management**: Multi-device, per-device revocation
- **Biometric**: Face enrollment with liveness detection
- **Account Security**: Lockout, password history, rate limiting
- **GDPR Compliance**: Consent tracking, account deletion
- **Audit Logging**: All security events logged

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0
- Redis 7.0+
- Python 3.9+ (for ML Face Service)

## ⚙️ Environment Variables

Create a `.env` file with these variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=identity_db
DB_USERNAME=your_user
DB_PASSWORD=your_password

# JWT (RS256 keys - base64 encoded)
JWT_PRIVATE_KEY=your_base64_private_key
JWT_PUBLIC_KEY=your_base64_public_key
JWT_ACCESS_TTL=900000      # 15 minutes
JWT_REFRESH_TTL=604800000  # 7 days

# Encryption
AES_SECRET_KEY=your_32_char_aes_key

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/oauth2/callback/google

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Email (SMTP)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your_email
EMAIL_PASSWORD=your_app_password
EMAIL_FROM=noreply@agriplatform.com

# ML Face Service
ML_SERVICE_URL=http://localhost:8001
ML_SERVICE_API_KEY=your_ml_api_key

# Security
CORS_ALLOWED_ORIGINS=http://localhost:4200
RATE_LIMIT_PER_MIN=60
ACCOUNT_LOCK_THRESHOLD=5
```

## 🔑 Generate RSA Keys

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Extract public key
openssl rsa -in private.pem -pubout -out public.pem

# Base64 encode for environment variable
cat private.pem | base64 -w 0 > private_key_base64.txt
cat public.pem | base64 -w 0 > public_key_base64.txt
```

## 🏃 Running Locally

### 1. Start MySQL & Redis (XAMPP or Docker)

```bash
# Using Docker
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8
docker run -d --name redis -p 6379:6379 redis:7
```

### 2. Create Database

```sql
CREATE DATABASE identity_db;
```

### 3. Start the Service

```bash
cd user-identity-service
mvn spring-boot:run
```

### 4. Start ML Face Service (Optional)

```bash
cd ml-face-service
pip install -r requirements.txt
python main.py
```

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/auth/register` | Register new user |
| POST | `/v1/auth/login` | Login with email/password |
| POST | `/v1/auth/refresh` | Refresh access token |
| POST | `/v1/auth/logout` | Logout current session |
| POST | `/v1/auth/logout-all` | Logout all sessions |
| GET | `/v1/auth/email/verify` | Verify email |
| POST | `/v1/auth/password/reset-request` | Request password reset |
| POST | `/v1/auth/password/reset` | Reset password |

### User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/users/me` | Get current user profile |
| PUT | `/v1/users/me` | Update profile |
| POST | `/v1/users/me/password` | Change password |
| GET | `/v1/users/me/sessions` | List active sessions |
| DELETE | `/v1/users/me/sessions/{id}` | Revoke session |
| DELETE | `/v1/users/me` | Request account deletion |

### Biometric
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/biometric/enroll` | Enroll face |
| POST | `/v1/biometric/verify` | Verify face |
| DELETE | `/v1/biometric/me` | Remove biometric |
| GET | `/v1/biometric/me/status` | Check enrollment status |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/admin/users` | Search users |
| GET | `/v1/admin/users/{uuid}` | Get user by UUID |
| PUT | `/v1/admin/users/{uuid}/status` | Update user status |
| PUT | `/v1/admin/users/{uuid}/roles` | Assign roles |
| POST | `/v1/admin/token/validate` | Validate token (internal) |

## 📖 API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

## 🔒 Security

- **JWT RS256**: Asymmetric signing for secure cross-service verification
- **Refresh Token Rotation**: New refresh token on each use
- **Password Policy**: 12+ chars, mixed case, digits, special chars
- **Password History**: Prevents reuse of last 5 passwords
- **Account Lockout**: 5 failed attempts → 15 min lockout
- **Rate Limiting**: Redis-based sliding window
- **AES-256-GCM**: Biometric embedding encryption
- **Audit Logging**: All security events tracked

## 📁 Project Structure

```
user-identity-service/
├── src/main/java/com/agriplatform/identity/
│   ├── config/          # Spring configurations
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions
│   ├── repository/      # JPA repositories
│   ├── security/        # JWT, filters, encryption
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.yml  # Configuration
│   └── db/migration/    # Flyway migrations
└── pom.xml
```

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn verify
```

## 📦 Building for Production

```bash
mvn clean package -DskipTests
java -jar target/user-identity-service-1.0.0.jar
```

## 🐳 Docker

```bash
# Build image
docker build -t user-identity-service .

# Run
docker run -p 8080:8080 --env-file .env user-identity-service
```

## 📝 License

Proprietary - AgriPlatform
