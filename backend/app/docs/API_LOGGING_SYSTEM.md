# API Logging System

## Overview
The API Logging System provides comprehensive logging for all API calls in the application, capturing request data, response data, performance metrics, and user context automatically.

> **🚀 NEW: Enhanced Logging Features Available!** 
> 
> The logging system has been significantly enhanced with comprehensive request/response body logging, detailed user context, and improved security features. See [Enhanced Logging Features](ENHANCED_LOGGING_FEATURES.md) for complete details.

## Architecture

### Components

1. **ApiLoggingInterceptor** (`app.config.ApiLoggingInterceptor`)
   - Main interceptor that logs request and response data
   - Captures timing information and user context
   - Masks sensitive data automatically

2. **WebConfig** (`app.config.WebConfig`)
   - Registers the interceptor for all API endpoints
   - Excludes sensitive endpoints (e.g., `/api/auth/**`)

3. **RequestResponseLoggingFilter** (`app.config.RequestResponseLoggingFilter`)
   - Wraps requests and responses to enable body reading
   - Ensures proper content caching for logging

## Features

### Automatic Logging
- **Request Logging**: HTTP method, URI, user info, request body (for all operations), query parameters, headers
- **Response Logging**: Status code, response time, response body, response headers, response size
- **User Context**: JWT token extraction, user identification, role information, enabled status, authorities
- **Performance Metrics**: Request duration in milliseconds
- **Request Correlation**: Unique request ID for tracking
- **Comprehensive Coverage**: All API endpoints with detailed request/response information

### Data Masking
Automatically masks sensitive information:
- Passwords
- JWT tokens
- Secret fields
- Authorization headers
- Any field containing sensitive data

### Enhanced Log Format
```
[API-LOG] 2024-01-15 10:30:15 | POST /api/alumnos | User: prof_123 (Role: PROFESOR) | RequestID: a1b2c3d4
[API-LOG] User Context: prof_123 | Enabled: true | Authorities: [ROLE_PROFESOR]
[API-LOG] Content-Type: application/json
[API-LOG] Request Body: {"nombre": "Juan", "email": "juan@test.com"}
[API-LOG] 2024-01-15 10:30:15 | POST /api/alumnos | User: prof_123 (Role: PROFESOR) | Duration: 45ms | Status: 200 | RequestID: a1b2c3d4
[API-LOG] Response Body: {"id": 1, "nombre": "Juan", "email": "juan@test.com"}
[API-LOG] Response Content-Type: application/json
[API-LOG] Response Size: 45 characters
```

## Configuration

### Logging Levels
The system respects Spring Boot logging configuration:
```yaml
logging:
  level:
    app.config.ApiLoggingInterceptor: INFO  # Enable API logging
```

### Endpoint Coverage
- **Included**: All `/api/**` endpoints
- **Excluded**: `/api/auth/**` endpoints (to avoid logging sensitive authentication data, active during development though)

## Usage

### No Code Changes Required
The logging system works automatically for all existing and new API endpoints. No modifications to controllers, services, or repositories are needed.

### Customization
To modify logging behavior:
1. Edit `ApiLoggingInterceptor.java` for log format changes
2. Modify `WebConfig.java` for endpoint inclusion/exclusion
3. Update `RequestResponseLoggingFilter.java` for body reading behavior

## Performance Impact

- **Minimal overhead**: Uses efficient content caching
- **Async-friendly**: Logging doesn't block request processing
- **Configurable**: Can be easily disabled in production if needed

## Security Considerations

- **Sensitive data masking**: Automatic masking of passwords and tokens
- **Authentication exclusion**: Auth endpoints are excluded from logging
- **User privacy**: Only logs necessary information for debugging

## Troubleshooting

### Common Issues

1. **No logs appearing**
   - Check logging level configuration
   - Verify interceptor is registered in WebConfig

2. **Request body not logged**
   - Ensure ContentCachingRequestWrapper is being used
   - Check if RequestResponseLoggingFilter is active

3. **Performance issues**
   - Consider reducing log level in production
   - Review endpoint inclusion patterns

### Testing
Run the test suite to verify logging functionality:
```bash
mvn test -Dtest=ApiLoggingInterceptorTest
```

## Future Enhancements

- **Structured logging**: JSON format for better parsing
- **Metrics integration**: Prometheus/Grafana integration
- **Advanced filtering**: Configurable endpoint-specific logging
- **Audit trail**: Long-term storage of API access logs 