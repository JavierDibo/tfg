# API Logging System Demonstration

## What We've Implemented

The API logging system is now fully integrated into your Spring Boot application. Here's what happens automatically:

## Automatic Logging for All API Calls

### 1. **Request Logging**
Every API call to `/api/**` endpoints automatically logs:
```
[API-LOG] 2025-08-23 15:00:58 | POST /api/alumnos | User: prof_123 | RequestID: acbf11c4
[API-LOG] Request: {"nombre": "Juan", "email": "juan@test.com"}
```

### 2. **Response Logging**
Every API response automatically logs:
```
[API-LOG] 2025-08-23 15:00:58 | POST /api/alumnos | User: prof_123 | Duration: 45ms | Status: 200 | RequestID: acbf11c4
```

### 3. **Error Logging**
Error responses (status >= 400) automatically log the response body:
```
[API-LOG] Error Response: {"error": "Validation failed", "details": "Email is required"}
```

## How It Works

### **Zero Code Changes Required**
- All your existing REST controllers work unchanged
- All your existing services and repositories work unchanged
- The logging happens automatically in the background

### **Security Features**
- **Sensitive data masking**: Passwords, tokens, and secrets are automatically masked
- **Auth endpoint exclusion**: `/api/auth/**` endpoints are excluded from logging
- **User context**: JWT tokens are automatically parsed to identify users

### **Performance Features**
- **Request correlation**: Each request gets a unique ID for tracking
- **Timing metrics**: Response time is automatically measured
- **Efficient logging**: Uses Spring's content caching for minimal overhead

## Testing the System

### **1. Start Your Application**
```bash
mvn spring-boot:run
```

### **2. Make API Calls**
Any API call to your endpoints will now be logged. For example:

**Creating a student:**
```bash
curl -X POST http://localhost:8080/api/alumnos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"nombre": "Maria", "email": "maria@test.com"}'
```

**You'll see logs like:**
```
[API-LOG] 2025-08-23 15:05:12 | POST /api/alumnos | User: prof_123 | RequestID: d8e9f0a1
[API-LOG] Request: {"nombre": "Maria", "email": "maria@test.com"}
[API-LOG] 2025-08-23 15:05:12 | POST /api/alumnos | User: prof_123 | Duration: 67ms | Status: 201 | RequestID: d8e9f0a1
```

### **3. Test Error Scenarios**
Make an invalid request to see error logging:
```bash
curl -X POST http://localhost:8080/api/alumnos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"nombre": ""}'
```

**You'll see error logs:**
```
[API-LOG] 2025-08-23 15:05:15 | POST /api/alumnos | User: prof_123 | RequestID: b2c3d4e5
[API-LOG] Request: {"nombre": ""}
[API-LOG] 2025-08-23 15:05:15 | POST /api/alumnos | User: prof_123 | Duration: 23ms | Status: 400 | RequestID: b2c3d4e5
[API-LOG] Error Response: {"error": "Validation failed", "details": "Name is required"}
```

## Configuration Options

### **Log Level Control**
In `application.yml`, you can control logging verbosity:
```yaml
logging:
  level:
    app.config.ApiLoggingInterceptor: INFO  # Full logging
    # app.config.ApiLoggingInterceptor: WARN  # Minimal logging
    # app.config.ApiLoggingInterceptor: ERROR # Errors only
```

### **Endpoint Customization**
In `WebConfig.java`, you can modify which endpoints are logged:
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiLoggingInterceptor)
            .addPathPatterns("/api/**")           // Include all API endpoints
            .excludePathPatterns("/api/auth/**"); // Exclude auth endpoints
}
```

## Benefits You Now Have

1. **Complete API Visibility**: See every request and response
2. **Performance Monitoring**: Track response times automatically
3. **User Tracking**: Know who made each request
4. **Debugging**: Easy correlation of requests and responses
5. **Security**: Automatic masking of sensitive data
6. **Zero Maintenance**: Works automatically for all new endpoints

## Next Steps

The logging system is production-ready and will work immediately. You can:

1. **Start using it**: Just run your application - logging happens automatically
2. **Customize**: Modify log formats or endpoint inclusion as needed
3. **Monitor**: Use the logs to track API usage and performance
4. **Debug**: Use request IDs to correlate requests across your system

Your API now has enterprise-level observability with zero code changes required! 