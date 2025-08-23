# Enhanced Logging Features

## Overview

The enhanced logging system provides comprehensive API request/response logging with configurable options for response body logging.

## Features

### 1. Request Logging
- **Request details**: Method, URI, user info, request ID
- **User context**: Username, enabled status, authorities
- **Request body**: With sensitive data masking
- **Query parameters**: Complete query string
- **Headers**: Content-Type, User-Agent (excluding sensitive headers)

### 2. Response Logging
- **Response details**: Duration, status code, content type
- **Response body**: Configurable with JSON pretty-printing
- **Response size**: Character count
- **Exception handling**: Detailed error logging

### 3. Configurable Options

#### Response Body Logging
```yaml
app:
  logging:
    response-body:
      enabled: true          # Enable/disable response body logging
      max-length: 2000       # Maximum characters before truncation
      include-size: true     # Include response size in logs
```

#### Logging Levels
```yaml
logging:
  level:
    app.config.ApiLoggingInterceptor: DEBUG  # For detailed debugging
    app.config.ApiLoggingInterceptor: INFO   # For production use
```

## Configuration Profiles

### Development Profile (default)
- Response body logging: **Enabled**
- Max response length: **2000 characters**
- Logging level: **DEBUG**

### Production Profile
```bash
# Run with production profile
java -jar app.jar --spring.profiles.active=prod
```

- Response body logging: **Disabled** (security)
- Max response length: **500 characters** (if enabled)
- Logging level: **INFO**

## Security Considerations

### Sensitive Data Masking
The system automatically masks sensitive fields:
- `password`
- `token`
- `secret`
- `authorization`
- `jwt`

### Production Recommendations
1. **Disable response body logging** to prevent sensitive data exposure
2. **Use INFO level** instead of DEBUG
3. **Monitor log file sizes** and implement rotation
4. **Review logs regularly** for any sensitive information

## Troubleshooting

### Response Body Not Logging

1. **Check configuration**:
   ```yaml
   app.logging.response-body.enabled: true
   ```

2. **Verify logging level**:
   ```yaml
   logging.level.app.config.ApiLoggingInterceptor: DEBUG
   ```

3. **Check filter configuration**: Ensure `RequestResponseLoggingFilter` is active

4. **Debug output**: Look for debug messages about response wrapping

### Common Issues

1. **Response wrapper not working**: Check if filter is properly configured
2. **Empty response body**: Verify response content is being written
3. **Large responses**: Adjust `max-length` configuration
4. **Performance impact**: Disable in production or reduce logging verbosity

## Performance Impact

### Development
- **Minimal impact**: Response body logging enabled
- **Full visibility**: Complete request/response data
- **Debug friendly**: Detailed logging for troubleshooting

### Production
- **Optimized**: Response body logging disabled
- **Security focused**: No sensitive data in logs
- **Performance oriented**: Reduced logging overhead

## Best Practices

1. **Development**: Use DEBUG level with full response body logging
2. **Staging**: Use INFO level with limited response body logging
3. **Production**: Use INFO level with response body logging disabled
4. **Monitoring**: Implement log rotation and monitoring
5. **Security**: Regular audit of logged data

## Example Output

### With Response Body Logging
```
16:39:01.599 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] 2025-08-23 16:39:01 | GET /api/alumnos/paged | User: admin (Role: ADMIN) | RequestID: 6d22f8b7
16:39:01.599 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] Query Parameters: page=0&size=10
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] 2025-08-23 16:39:01 | GET /api/alumnos/paged | User: admin (Role: ADMIN) | Duration: 40ms | Status: 200 | RequestID: 6d22f8b7
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] Response Content-Type: application/json
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] Response Body (1234 chars):
{
  "content": [
    {
      "id": 1,
      "nombre": "Juan",
      "apellidos": "Pérez"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] Response Size: 1234 characters
```

### Without Response Body Logging
```
16:39:01.599 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] 2025-08-23 16:39:01 | GET /api/alumnos/paged | User: admin (Role: ADMIN) | RequestID: 6d22f8b7
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] 2025-08-23 16:39:01 | GET /api/alumnos/paged | User: admin (Role: ADMIN) | Duration: 40ms | Status: 200 | RequestID: 6d22f8b7
16:39:01.639 [http-nio-8080-exec-4] INFO  app.config.ApiLoggingInterceptor - [API-LOG] Response Content-Type: application/json
``` 