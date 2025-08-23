# Enhanced Access Denied Responses Guide

## Overview

The API now provides detailed, relevant information when access is denied, helping developers and users understand why access was denied and what they need to do to gain access.

## Enhanced Error Response Structure

When access is denied, the API now returns an enhanced `ErrorResponse` with additional context:

```json
{
  "timestamp": "2025-01-22T19:11:11.271",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permisos suficientes para realizar esta acción",
  "errorCode": "ACCESS_DENIED",
  "path": "/api/clases/cursos",
  "requiredRole": "ADMIN",
  "currentUserRole": "ROLE_ALUMNO",
  "resourceType": "clases",
  "resourceId": null,
  "action": "POST",
  "suggestion": "Esta acción requiere permisos de administrador. Contacta con un administrador del sistema."
}
```

## New Fields in Access Denied Responses

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `requiredRole` | String | The role required to access this resource | `"ADMIN"`, `"PROFESOR"`, `"ALUMNO"` |
| `currentUserRole` | String | The current user's role (if authenticated) | `"ROLE_ALUMNO"`, `"ROLE_PROFESOR"` |
| `resourceType` | String | The type of resource being accessed | `"clases"`, `"enrollments"`, `"alumnos"` |
| `resourceId` | String | The specific resource ID (if applicable) | `"123"`, `"456"` |
| `action` | String | The HTTP method/action being performed | `"GET"`, `"POST"`, `"DELETE"` |
| `suggestion` | String | Helpful suggestion for resolving the issue | `"Contacta con un administrador del sistema"` |

## Types of Access Denied Scenarios

### 1. Authentication Required

When a user is not authenticated:

```json
{
  "status": 403,
  "message": "Autenticación requerida para acceder a este recurso",
  "errorCode": "ACCESS_DENIED",
  "currentUserRole": null,
  "suggestion": "Inicia sesión para acceder a esta funcionalidad"
}
```

### 2. Insufficient Privileges

When a user is authenticated but lacks the required role:

```json
{
  "status": 403,
  "message": "No tienes permisos suficientes para realizar esta acción",
  "errorCode": "ACCESS_DENIED",
  "requiredRole": "ADMIN",
  "currentUserRole": "ROLE_ALUMNO",
  "resourceType": "clases",
  "action": "POST",
  "suggestion": "Esta acción requiere permisos de administrador. Contacta con un administrador del sistema."
}
```

### 3. Resource-Specific Access Denied

When access is denied to a specific resource:

```json
{
  "status": 403,
  "message": "Acceso denegado a este recurso",
  "errorCode": "ACCESS_DENIED",
  "resourceType": "enrollments",
  "resourceId": "123",
  "action": "DELETE",
  "suggestion": "Para gestionar inscripciones, necesitas permisos de profesor o administrador."
}
```

## Role Hierarchy and Permissions

The system follows this role hierarchy:

- **ADMIN**: Full access to all resources
- **PROFESOR**: Access to classes, enrollments, and student information
- **ALUMNO**: Limited access to their own data and class enrollment

### Common Permission Requirements

| Resource | Action | Required Role | Description |
|----------|--------|---------------|-------------|
| `/api/clases/cursos` | POST | ADMIN, PROFESOR | Create courses |
| `/api/clases/{id}` | DELETE | ADMIN | Delete classes |
| `/api/enrollments` | POST/DELETE | ADMIN, PROFESOR | Manage enrollments |
| `/api/my/classes` | GET | PROFESOR | Get my classes |
| `/api/my/enrolled-classes` | GET | ALUMNO | Get my enrolled classes |

## Frontend Integration Examples

### React Example

```javascript
const handleApiError = (error) => {
  if (error.response?.status === 403) {
    const errorData = error.response.data;
    
    // Show specific error message
    showNotification(errorData.message, 'error');
    
    // Show suggestion if available
    if (errorData.suggestion) {
      showSuggestion(errorData.suggestion, 'info');
    }
    
    // Log detailed information for debugging
    console.log('Access denied details:', {
      requiredRole: errorData.requiredRole,
      currentRole: errorData.currentUserRole,
      resource: errorData.resourceType,
      action: errorData.action
    });
    
    // Handle specific scenarios
    if (errorData.currentUserRole === null) {
      // User not authenticated
      redirectToLogin();
    } else if (errorData.requiredRole === 'ADMIN') {
      // User needs admin privileges
      showAdminContactInfo();
    }
  }
};
```

### Angular Example

```typescript
handleError(error: any) {
  if (error.status === 403) {
    const errorData = error.error;
    
    // Display error message
    this.notificationService.showError(errorData.message);
    
    // Show suggestion
    if (errorData.suggestion) {
      this.notificationService.showInfo(errorData.suggestion);
    }
    
    // Handle based on error context
    if (!errorData.currentUserRole) {
      this.router.navigate(['/login']);
    } else if (errorData.requiredRole === 'ADMIN') {
      this.showAdminContactDialog();
    }
  }
}
```

## API Documentation Updates

All endpoints now include detailed `@ApiResponses` annotations for 403 errors:

```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Operación exitosa"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requieren permisos específicos"),
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
})
```

## Testing Access Denied Scenarios

### Test Cases

1. **Unauthenticated Access**
   ```bash
   curl -X POST /api/clases/cursos
   # Should return 403 with authentication required message
   ```

2. **Insufficient Privileges**
   ```bash
   # Login as ALUMNO
   curl -H "Authorization: Bearer <alumno-token>" -X POST /api/clases/cursos
   # Should return 403 with insufficient privileges message
   ```

3. **Resource-Specific Access**
   ```bash
   # Login as ALUMNO, try to access admin resource
   curl -H "Authorization: Bearer <alumno-token>" -X DELETE /api/clases/123
   # Should return 403 with specific resource access denied
   ```

## Benefits

1. **Better User Experience**: Users get clear information about why access was denied
2. **Easier Debugging**: Developers can quickly identify permission issues
3. **Improved Security**: Clear communication about required permissions
4. **Better Documentation**: API documentation now includes detailed error responses
5. **Frontend Integration**: Easy to handle different access denied scenarios in the UI

## Implementation Details

The enhanced access denied handling is implemented through:

1. **Enhanced ErrorResponse**: Added new fields for access denied context
2. **AccessDeniedUtils**: Utility class to extract relevant information
3. **GlobalExceptionHandler**: Enhanced to provide detailed access denied responses
4. **Controller Documentation**: Updated with detailed API responses

This implementation provides a comprehensive solution for handling access denied scenarios while maintaining security and providing excellent developer experience.
