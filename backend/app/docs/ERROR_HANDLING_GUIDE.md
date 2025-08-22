# Error Handling Guide

## Overview

This guide explains the improved error handling system implemented in the backend API. The new system provides clean, meaningful error messages for the frontend while reducing console verbosity.

## Key Improvements

### 1. Standardized Error Responses

All API errors now return a consistent `ErrorResponse` structure:

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 400,
  "error": "Bad Request",
  "message": "Los datos enviados contienen errores",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/clases",
  "fieldErrors": {
    "titulo": "El título es obligatorio",
    "precio": "El precio debe ser mayor que 0"
  }
}
```

### 2. Clean Console Logging

- **Before**: Verbose stack traces cluttering the console
- **After**: Clean, concise error messages with relevant context

Example of old logging:
```
2025-08-22T19:11:11.271+02:00 ERROR 17348 --- [app] [nio-8080-exec-5] app.excepciones.GlobalExceptionHandler   : Error interno del servidor: Access Denied
org.springframework.security.authorization.AuthorizationDeniedException: Access Denied
    at org.springframework.security.authorization.method.ThrowingMethodAuthorizationDeniedHandler.handleDeniedInvocation(ThrowingMethodAuthorizationDeniedHandler.java:38)
    ... (100+ lines of stack trace)
```

Example of new logging:
```
19:11:11.271 [nio-8080-exec-5] WARN  app.excepciones.GlobalExceptionHandler - Access denied: No tienes permisos para realizar esta acción
```

### 3. Meaningful Error Codes

Each error type has a specific error code for frontend handling:

- `VALIDATION_ERROR` - Input validation errors
- `RESOURCE_NOT_FOUND` - Entity not found
- `ACCESS_DENIED` - Authorization errors
- `AUTHENTICATION_FAILED` - Login failures
- `DUPLICATE_DATA` - Data integrity violations
- `TYPE_MISMATCH_ERROR` - Parameter type conversion errors
- `ENDPOINT_NOT_FOUND` - 404 errors

## Exception Hierarchy

### Base Exception
- `ApiException` - Base class for all API exceptions

### Specific Exceptions
- `ResourceNotFoundException` - For 404 errors
- `ValidationException` - For validation errors
- `AccessDeniedException` - For authorization errors

### Updated Existing Exceptions
- `EntidadNoEncontradaException` - Now extends `ApiException`
- `AlumnoNoEncontradoException` - Now extends `ApiException`

## Usage Examples

### 1. Throwing Resource Not Found Errors

```java
// In REST controllers
@GetMapping("/{id}")
public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
    DTOClase clase = servicioClase.obtenerClasePorId(id);
    if (clase == null) {
        throw new ResourceNotFoundException("Clase", "ID", id);
    }
    return ResponseEntity.ok(clase);
}
```

### 2. Using ExceptionUtils Helper

```java
// In service classes
import app.util.ExceptionUtils;

public DTOClase obtenerClasePorId(Long id) {
    Clase clase = repositorioClase.findById(id).orElse(null);
    ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", id);
    return new DTOClase(clase);
}
```

### 3. Throwing Validation Errors

```java
// For field-specific validation errors
Map<String, String> fieldErrors = new HashMap<>();
fieldErrors.put("email", "El email no es válido");
fieldErrors.put("password", "La contraseña debe tener al menos 8 caracteres");

ExceptionUtils.throwValidationError("Datos de registro inválidos", fieldErrors);
```

## Error Response Structure

### ErrorResponse Fields

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | LocalDateTime | When the error occurred |
| `status` | int | HTTP status code |
| `error` | String | HTTP status reason phrase |
| `message` | String | User-friendly error message |
| `errorCode` | String | Application-specific error code |
| `path` | String | Request path that caused the error |
| `fieldErrors` | Map<String,String> | Field-specific validation errors |
| `violations` | Map<String,String> | Parameter validation violations |
| `details` | String | Additional error details (optional) |

## Common Error Scenarios

### 1. Validation Errors (400)

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 400,
  "error": "Bad Request",
  "message": "Los datos enviados contienen errores",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/alumnos",
  "fieldErrors": {
    "dni": "El DNI debe tener 8 dígitos",
    "email": "El email no es válido"
  }
}
```

### 2. Resource Not Found (404)

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 404,
  "error": "Not Found",
  "message": "El alumno solicitado no existe",
  "errorCode": "ALUMNO_NOT_FOUND",
  "path": "/api/alumnos/123"
}
```

### 3. Access Denied (403)

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permisos para realizar esta acción",
  "errorCode": "ACCESS_DENIED",
  "path": "/api/admin/users"
}
```

### 4. Type Mismatch (400)

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 400,
  "error": "Bad Request",
  "message": "El parámetro 'id' con valor 'NaN' no es válido",
  "errorCode": "TYPE_MISMATCH_ERROR",
  "path": "/api/clases/NaN"
}
```

## Logging Configuration

The application now uses cleaner logging with the following configuration:

```yaml
logging:
  level:
    root: INFO
    app: INFO
    org.springframework.security: WARN
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
    org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## Frontend Integration

### Error Handling in Frontend

```javascript
// Example of handling API errors in frontend
async function fetchClase(id) {
  try {
    const response = await fetch(`/api/clases/${id}`);
    if (!response.ok) {
      const errorData = await response.json();
      
      switch (errorData.errorCode) {
        case 'RESOURCE_NOT_FOUND':
          showError('La clase solicitada no existe');
          break;
        case 'ACCESS_DENIED':
          showError('No tienes permisos para ver esta clase');
          break;
        case 'VALIDATION_ERROR':
          showFieldErrors(errorData.fieldErrors);
          break;
        default:
          showError(errorData.message || 'Error inesperado');
      }
      return;
    }
    
    const clase = await response.json();
    // Handle success
  } catch (error) {
    showError('Error de conexión');
  }
}
```

## Migration Guide

### For Existing Code

1. **Replace generic exceptions** with specific ones:
   ```java
   // Before
   throw new RuntimeException("Clase no encontrada");
   
   // After
   throw new ResourceNotFoundException("Clase", "ID", id);
   ```

2. **Use ExceptionUtils** for common patterns:
   ```java
   // Before
   if (clase == null) {
       throw new EntidadNoEncontradaException("Clase no encontrada");
   }
   
   // After
   ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", id);
   ```

3. **Update REST controllers** to handle null returns:
   ```java
   // Before
   return ResponseEntity.ok(servicioClase.obtenerClasePorId(id));
   
   // After
   DTOClase clase = servicioClase.obtenerClasePorId(id);
   if (clase == null) {
       throw new ResourceNotFoundException("Clase", "ID", id);
   }
   return ResponseEntity.ok(clase);
   ```

## Benefits

1. **Cleaner Console**: No more verbose stack traces
2. **Better Frontend Experience**: Consistent, meaningful error messages
3. **Easier Debugging**: Specific error codes and messages
4. **Improved UX**: User-friendly error messages in Spanish
5. **Consistent API**: Standardized error response format
6. **Better Logging**: Relevant information without noise

## Testing Error Handling

You can test the error handling by:

1. **Invalid IDs**: `GET /api/clases/NaN`
2. **Non-existent resources**: `GET /api/clases/999999`
3. **Invalid data**: `POST /api/alumnos` with invalid JSON
4. **Unauthorized access**: Access admin endpoints without proper roles
5. **Validation errors**: Submit forms with missing required fields
