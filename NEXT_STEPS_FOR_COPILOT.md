# User Identity Microservice - Next Steps

Hi Copilot, please help me execute the following steps to run and verify the User Identity Microservice.

## 1. Environment Setup

### Database
- Ensure MySQL is running on port **3306**.
- Create the database if it doesn't exist:
  ```sql
  CREATE DATABASE identity_db;
  ```

### Redis
- Ensure Redis is running on port **6379**.
  - If using Docker: `docker run -d -p 6379:6379 redis:7`

### JWT Keys
- Verify that `user-identity-service/.env` contains valid base64-encoded `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY`.

---

## 2. Run Python ML Service (Face Recognition)

This service is required for biometric enrollment.

1. Open a new terminal.
2. Navigate to the ML service directory:
   ```bash
   cd "user-identity-service/../ml-face-service"
   ```
   *(Note: Adjust path as necessary)*
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
   *Note: You may need to install CMake and Visual Studio C++ Build Tools first for `dlib`.*
4. Run the service:
   ```bash
   python main.py
   ```
   - It should start on http://localhost:8001

---

## 3. Run Java Spring Boot Service

1. Open a new terminal.
2. Navigate to the Java service directory:
   ```bash
   cd user-identity-service
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   - It should start on http://localhost:8080

---

## 4. Verification

Once both services are running, verify the endpoints:

1. **Swagger UI**: Open http://localhost:8080/swagger-ui.html
2. **Health Check**: GET http://localhost:8080/actuator/health
3. **ML Service Health**: GET http://localhost:8001/health

### Test Flow to Guide Me Through:
1. **Register** a new user via `POST /v1/auth/register`.
2. **Login** to get the JWT token via `POST /v1/auth/login`.
3. **Extract** the token from the response.
4. **Authorize** inside Swagger UI using the `Bearer <token>`.
5. **Get Profile** via `GET /v1/users/me` to confirm authentication works.
