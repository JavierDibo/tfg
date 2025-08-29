# Student Class Catalog Feature

## Overview

This feature allows students to view all available classes in the system with an indication of which classes they are currently enrolled in. This provides a comprehensive catalog view that helps students discover new classes while being aware of their current enrollments.

## Problem Solved

Previously, students had two separate endpoints:
- `/api/clases` - Shows all classes (catalog view)
- `/api/clases/disponibles` - Shows only classes they are NOT enrolled in

This created a fragmented experience where students couldn't easily see their enrollment status across all classes.

## Solution

A new endpoint `/api/clases/catalog` has been added that provides:

### For Students
- **All classes** in the system
- **Enrollment status** for each class (`isEnrolled: true/false`)
- **Enrollment date** for enrolled classes (`fechaInscripcion`)
- **Full class information** (title, description, price, etc.)

### For Admins/Professors
- **All classes** in the system (same as regular endpoint)
- **No enrollment status** (since it's not relevant for their role)
- **Full class information**

## Implementation Details

### New DTO: `DTOClaseConEstadoInscripcion`

```java
public record DTOClaseConEstadoInscripcion(
    // Standard class fields
    Long id,
    String titulo,
    String descripcion,
    BigDecimal precio,
    EPresencialidad presencialidad,
    String imagenPortada,
    EDificultad nivel,
    List<String> alumnosId,
    List<String> profesoresId,
    List<String> ejerciciosId,
    List<Material> material,
    String tipoClase,
    
    // Enrollment status information
    boolean isEnrolled,
    LocalDateTime fechaInscripcion
)
```

### New Service Method: `buscarClasesConEstadoInscripcion`

This method in `ServicioClase` handles the logic for:
- **Students**: Fetches all classes and checks enrollment status
- **Admins/Professors**: Returns all classes without enrollment status

### New REST Endpoint: `GET /api/clases/catalog`

**URL**: `/api/clases/catalog`

**Parameters**:
- All standard pagination and filtering parameters
- Same as the regular `/api/clases` endpoint

**Response**:
```json
{
  "content": [
    {
      "id": 1,
      "titulo": "Curso de Java",
      "descripcion": "Aprende Java desde cero",
      "precio": 99.99,
      "presencialidad": "ONLINE",
      "imagenPortada": "imagen1.jpg",
      "nivel": "PRINCIPIANTE",
      "alumnosId": ["1", "2", "3"],
      "profesoresId": ["1"],
      "ejerciciosId": ["1", "2"],
      "material": [...],
      "tipoClase": "CURSO",
      "isEnrolled": true,
      "fechaInscripcion": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "titulo": "Taller de Spring",
      "descripcion": "Taller intensivo de Spring Boot",
      "precio": 49.99,
      "presencialidad": "PRESENCIAL",
      "imagenPortada": "imagen2.jpg",
      "nivel": "INTERMEDIO",
      "alumnosId": ["1", "4"],
      "profesoresId": ["2"],
      "ejerciciosId": ["3"],
      "material": [...],
      "tipoClase": "TALLER",
      "isEnrolled": false,
      "fechaInscripcion": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true,
  "hasContent": true,
  "sortBy": "id",
  "sortDirection": "ASC"
}
```

## Usage Examples

### Frontend Integration

```javascript
// Fetch all classes with enrollment status
const response = await fetch('/api/clases/catalog?page=0&size=20');
const data = await response.json();

// Display classes with enrollment indicators
data.content.forEach(clase => {
  if (clase.isEnrolled) {
    // Show "Enrolled" badge
    displayClassWithEnrollmentBadge(clase);
  } else {
    // Show "Enroll Now" button
    displayClassWithEnrollButton(clase);
  }
});
```

### Filtering Enrolled Classes

```javascript
// Filter to show only enrolled classes
const enrolledClasses = data.content.filter(clase => clase.isEnrolled);

// Filter to show only available classes
const availableClasses = data.content.filter(clase => !clase.isEnrolled);
```

## Benefits

1. **Unified View**: Students can see all classes in one place
2. **Clear Enrollment Status**: Immediate visual indication of enrollment
3. **Better Discovery**: Students can discover classes they might want to enroll in
4. **Consistent Experience**: Same endpoint works for all user roles
5. **Backward Compatibility**: Existing endpoints remain unchanged

## Security

- **Students**: Can see all classes with their own enrollment status
- **Professors**: Can see all classes (no enrollment status needed)
- **Admins**: Can see all classes (no enrollment status needed)
- **Unauthorized users**: Access denied

## Performance Considerations

- Uses existing pagination to handle large datasets
- Efficient database queries with proper indexing
- Enrollment status calculated in a single query per student
- Caching can be added for frequently accessed data

## Future Enhancements

1. **Real Enrollment Dates**: Currently uses current timestamp, could be enhanced to use actual enrollment dates from a dedicated enrollment table
2. **Enrollment History**: Could include historical enrollment information
3. **Recommendations**: Could suggest classes based on enrollment patterns
4. **Notifications**: Could notify students about new classes or enrollment deadlines
