# JWT Authentication Implementation Guide

## Overview

This guide explains the complete JWT-based authentication flow implemented in your Spring Boot application. The system follows the standard JWT authentication pattern with secure token generation, validation, and role-based access control.

## Flow Implementation

### 1. Login Process
```
Usuario envía credenciales → Spring Security verifica → JWT generado → Token devuelto
```

### 2. Request Authentication
```
Cliente incluye JWT en header → Filtro intercepta → Token validado → Contexto establecido
```

## Components Created

### 1. User Entity (`Usuario.java`)
- Implements `UserDetails` for Spring Security integration
- Contains user information: username, password, email, name, role
- Supports two roles: `ADMIN` and `USUARIO`

### 2. DTOs
- `LoginRequest`: Credentials for login
- `LoginResponse`: JWT token and user info
- `RegistroRequest`: User registration data

### 3. JWT Service (`JwtService.java`)
- Token generation and validation
- Configurable secret key and expiration time
- Uses HS256 algorithm for signing

### 4. Authentication Filter (`JwtAuthenticationFilter.java`)
- Intercepts all requests
- Extracts and validates JWT tokens
- Sets Spring Security context

### 5. Authentication Service (`AuthenticationService.java`)
- Handles login and registration logic
- Uses BCrypt for password encoding
- Generates JWT tokens upon successful authentication

### 6. Security Configuration (`SecurityConfig.java`)
- Configures stateless session management
- Sets up JWT filter chain
- Configures CORS for frontend integration
- Defines public and protected endpoints

## API Endpoints

### Public Endpoints (No Authentication Required)
```
POST /api/auth/login
POST /api/auth/registro
GET  /api/test/public
GET  /v3/api-docs/**
GET  /swagger-ui/**
GET  /actuator/**
```

### Protected Endpoints (Authentication Required)
```
GET  /api/test/protected
GET  /api/test/admin
GET  /api/test/user-info
```

## Usage Examples

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevo_usuario",
    "contraseña": "password123",
    "email": "usuario@example.com",
    "nombre": "Juan",
    "apellidos": "Pérez"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "nuevo_usuario",
  "email": "usuario@example.com",
  "nombre": "Juan",
  "apellidos": "Pérez",
  "rol": "USUARIO"
}
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "contraseña": "admin123"
  }'
```

### 3. Accessing Protected Endpoints
```bash
curl -X GET http://localhost:8080/api/test/protected \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 4. Testing Admin Endpoint
```bash
curl -X GET http://localhost:8080/api/test/admin \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## Configuration

### JWT Settings (`application.yml`)
```yaml
jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000  # 24 hours
```

### Database Configuration
The system automatically creates the `usuarios` table and populates it with test users:
- **Admin**: username=`admin`, password=`admin123`
- **User**: username=`usuario`, password=`user123`

## Security Features

### 1. Password Security
- BCrypt password encoding
- Minimum 6 characters required
- Secure password storage

### 2. Token Security
- HS256 algorithm for signing
- Configurable expiration time
- Secure secret key management

### 3. CORS Configuration
- Configured for frontend integration
- Supports localhost:5173 and localhost:3000
- Allows Authorization header

### 4. Role-Based Access Control
- `ROLE_ADMIN`: Full access
- `ROLE_USUARIO`: Standard user access
- Custom endpoint protection

## Frontend Integration

### JavaScript/Fetch Example
```javascript
// Login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
});

const { token } = await loginResponse.json();

// Use token for subsequent requests
const protectedResponse = await fetch('/api/test/protected', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Axios Example
```javascript
// Set up axios with JWT interceptor
axios.defaults.baseURL = 'http://localhost:8080';

// Add token to all requests
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

## Testing the Implementation

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Test Public Endpoint
```bash
curl http://localhost:8080/api/test/public
```

### 3. Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "contraseña": "admin123"}'
```

### 4. Test Protected Endpoint
```bash
# Replace YOUR_JWT_TOKEN with the token from login response
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/test/protected
```

## Troubleshooting

### Common Issues

1. **Token Expired**: Check JWT expiration time in configuration
2. **Invalid Token**: Ensure proper Bearer format in Authorization header
3. **CORS Errors**: Verify CORS configuration matches your frontend origin
4. **Database Connection**: Ensure PostgreSQL is running and accessible

### Debug Information

The application logs will show:
- User creation during startup
- Authentication attempts
- Token validation results
- Security context information

## Security Best Practices

1. **Change Default Secret**: Update the JWT secret in production
2. **HTTPS Only**: Use HTTPS in production environments
3. **Token Expiration**: Set appropriate token expiration times
4. **Password Policy**: Implement strong password requirements
5. **Rate Limiting**: Consider adding rate limiting for auth endpoints
6. **Audit Logging**: Log authentication events for security monitoring

## Next Steps

1. **Add Refresh Tokens**: Implement token refresh mechanism
2. **Email Verification**: Add email verification for new registrations
3. **Password Reset**: Implement password reset functionality
4. **Multi-Factor Authentication**: Add 2FA support
5. **Session Management**: Add logout and session invalidation
6. **API Rate Limiting**: Implement rate limiting for security 