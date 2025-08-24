# Enrollment and Enabled Status Update Support

## Summary

The backend has been updated to support changing student enrollment and enabled status through the existing PATCH endpoint (`/api/alumnos/{id}`). This allows the frontend to toggle these statuses without requiring separate endpoints.

## Changes Made

### 1. Updated DTOActualizacionAlumno

**File:** `src/main/java/app/dtos/DTOActualizacionAlumno.java`

Added two new optional fields to the DTO:
- `Boolean enrolled` - Controls student enrollment status
- `Boolean enabled` - Controls student account enabled status

```java
public record DTOActualizacionAlumno(
    // ... existing fields ...
    Boolean enrolled,
    Boolean enabled
) {
}
```

### 2. Enhanced ServicioAlumno.actualizarAlumno

**File:** `src/main/java/app/servicios/ServicioAlumno.java`

Updated the `actualizarAlumno` method to handle the new fields:

```java
// Handle enrollment status update
if (dtoParcial.enrolled() != null) {
    alumno.setEnrolled(dtoParcial.enrolled());
}

// Handle enabled status update
if (dtoParcial.enabled() != null) {
    alumno.setEnabled(dtoParcial.enabled());
}
```

## How It Works

### Frontend Usage

The frontend can now send PATCH requests to update enrollment and enabled status:

```javascript
// To enroll a student
await fetch(`/api/alumnos/${studentId}`, {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ enrolled: true })
});

// To disable a student account
await fetch(`/api/alumnos/${studentId}`, {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ enabled: false })
});

// To update both at once
await fetch(`/api/alumnos/${studentId}`, {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ 
    enrolled: true, 
    enabled: false 
  })
});
```

### Security

The PATCH endpoint maintains the same security model:
- **ADMIN users** can update any student's enrollment and enabled status
- **Students** can only update their own profile (but not enrollment/enabled status)
- **PROFESSOR users** cannot update student status

### Validation

- Both fields are optional (can be `null`)
- Only non-null values are applied to the student record
- Existing validation for other fields remains unchanged

## Benefits

1. **Unified API**: Uses the existing PATCH endpoint instead of creating separate endpoints
2. **Backward Compatible**: Existing functionality remains unchanged
3. **Flexible**: Can update individual fields or multiple fields in a single request
4. **Consistent**: Follows the same patterns as other partial updates

## Testing

The changes have been tested and all existing tests continue to pass. The backend is ready to handle enrollment and enabled status updates from the frontend.

## Frontend Integration

The frontend can now use the existing PATCH endpoint to toggle enrollment and enabled status. The request body should include only the fields that need to be updated, making it efficient and flexible.
