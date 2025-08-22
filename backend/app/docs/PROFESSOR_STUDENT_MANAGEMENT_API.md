# Professor Student Management API

This document describes the API endpoints that allow professors to manage students in their classes.

## Overview

The API provides an elegant way for professors to:
1. **Create classes** and enroll any student from the database
2. **Unenroll students** from any class they teach
3. **Cannot add or remove students** from classes they don't teach (security enforced)

## Security Model

- **Professors** can only manage students in classes they teach
- **Admins** can manage students in any class
- **Students** can only enroll/unenroll themselves (self-service)
- All operations are validated and logged

## API Endpoints

### 1. Get Available Students for Enrollment

**GET** `/api/alumnos/disponibles`

Returns all students that are available for enrollment (enabled and matriculated).

**Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50, max: 100)
- `sortBy` (optional): Sort field (default: "nombre")
- `sortDirection` (optional): Sort direction "ASC" or "DESC" (default: "ASC")

**Authorization:** `ADMIN` or `PROFESOR`

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "usuario": "alumno1",
      "nombre": "Juan",
      "apellidos": "Pérez",
      "dni": "12345678A",
      "email": "juan.perez@email.com",
      "numeroTelefono": "123456789",
      "fechaInscripcion": "2024-01-15T10:30:00",
      "matriculado": true,
      "enabled": true,
      "clasesId": [],
      "pagosId": [],
      "entregasId": [],
      "rol": "ALUMNO"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "size": 50,
  "number": 0,
  "first": true,
  "last": false
}
```

### 2. Enroll Student in Class (General)

**POST** `/api/clases/enrollment`

Enrolls a student in a class. Only works if the authenticated user is an admin or teaches the class.

**Authorization:** `ADMIN` or `PROFESOR`

**Request Body:**
```json
{
  "alumnoId": 1,
  "claseId": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "Operación realizada con éxito",
  "alumnoId": 1,
  "claseId": 5,
  "nombreAlumno": "Juan Pérez",
  "tituloClase": "Matemáticas Avanzadas",
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "ENROLLMENT"
}
```

### 3. Unenroll Student from Class (General)

**DELETE** `/api/clases/enrollment`

Removes a student from a class. Only works if the authenticated user is an admin or teaches the class.

**Authorization:** `ADMIN` or `PROFESOR`

**Request Body:**
```json
{
  "alumnoId": 1,
  "claseId": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "Operación realizada con éxito",
  "alumnoId": 1,
  "claseId": 5,
  "nombreAlumno": "Juan Pérez",
  "tituloClase": "Matemáticas Avanzadas",
  "fechaOperacion": "2024-01-15T14:35:00",
  "tipoOperacion": "UNENROLLMENT"
}
```

### 4. Professor-Specific Enrollment

**POST** `/api/profesores/{profesorId}/clases/{claseId}/alumnos`

Allows a professor to enroll a student in one of their specific classes.

**Authorization:** `ADMIN` or `PROFESOR` (must be the same professor)

**Request Body:**
```json
{
  "alumnoId": 1,
  "claseId": 5
}
```

### 5. Professor-Specific Unenrollment

**DELETE** `/api/profesores/{profesorId}/clases/{claseId}/alumnos`

Allows a professor to remove a student from one of their specific classes.

**Authorization:** `ADMIN` or `PROFESOR` (must be the same professor)

**Request Body:**
```json
{
  "alumnoId": 1,
  "claseId": 5
}
```

### 6. Get Students in a Class

**GET** `/api/clases/{claseId}/alumnos`

Returns all students enrolled in a specific class.

**Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sortBy` (optional): Sort field (default: "id")
- `sortDirection` (optional): Sort direction "ASC" or "DESC" (default: "ASC")

**Authorization:** `ADMIN` or `PROFESOR` (must teach the class)

### 7. Count Students in a Class

**GET** `/api/clases/{claseId}/alumnos/contar`

Returns the number of students enrolled in a specific class.

**Authorization:** `ADMIN` or `PROFESOR` (must teach the class)

**Response:**
```json
15
```

## Error Handling

### Common Error Responses

**Student not found:**
```json
{
  "success": false,
  "message": "Alumno con ID 999 no encontrado",
  "alumnoId": 999,
  "claseId": 5,
  "nombreAlumno": null,
  "tituloClase": null,
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "ENROLLMENT"
}
```

**Class not found:**
```json
{
  "success": false,
  "message": "Clase con ID 999 no encontrada",
  "alumnoId": 1,
  "claseId": 999,
  "nombreAlumno": null,
  "tituloClase": null,
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "ENROLLMENT"
}
```

**Permission denied:**
```json
{
  "success": false,
  "message": "No tienes permisos para modificar esta clase",
  "alumnoId": 1,
  "claseId": 5,
  "nombreAlumno": null,
  "tituloClase": null,
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "ENROLLMENT"
}
```

**Student already enrolled:**
```json
{
  "success": false,
  "message": "El alumno ya está inscrito en esta clase",
  "alumnoId": 1,
  "claseId": 5,
  "nombreAlumno": null,
  "tituloClase": null,
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "ENROLLMENT"
}
```

**Student not enrolled:**
```json
{
  "success": false,
  "message": "El alumno no está inscrito en esta clase",
  "alumnoId": 1,
  "claseId": 5,
  "nombreAlumno": null,
  "tituloClase": null,
  "fechaOperacion": "2024-01-15T14:30:00",
  "tipoOperacion": "UNENROLLMENT"
}
```

## Usage Examples

### Frontend Integration

```javascript
// Get available students
const getAvailableStudents = async (page = 0, size = 50) => {
  const response = await fetch(`/api/alumnos/disponibles?page=${page}&size=${size}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// Enroll a student
const enrollStudent = async (alumnoId, claseId) => {
  const response = await fetch('/api/clases/enrollment', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ alumnoId, claseId })
  });
  return response.json();
};

// Unenroll a student
const unenrollStudent = async (alumnoId, claseId) => {
  const response = await fetch('/api/clases/enrollment', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ alumnoId, claseId })
  });
  return response.json();
};
```

### cURL Examples

```bash
# Get available students
curl -X GET "http://localhost:8080/api/alumnos/disponibles?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Enroll a student
curl -X POST "http://localhost:8080/api/clases/enrollment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"alumnoId": 1, "claseId": 5}'

# Unenroll a student
curl -X DELETE "http://localhost:8080/api/clases/enrollment" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"alumnoId": 1, "claseId": 5}'
```

## Security Considerations

1. **Role-based access control**: Only professors and admins can manage enrollments
2. **Class ownership validation**: Professors can only manage students in classes they teach
3. **Input validation**: All inputs are validated using Bean Validation
4. **Transaction isolation**: Enrollment operations use SERIALIZABLE isolation to prevent race conditions
5. **Audit trail**: All operations are logged with timestamps

## Implementation Details

### DTOs Created

- `DTOPeticionEnrollment`: Request DTO for enrollment operations
- `DTORespuestaEnrollment`: Response DTO with detailed operation results

### Service Methods Added

- `ServicioClase.inscribirAlumnoEnClase()`: Enrolls a student in a class
- `ServicioClase.darDeBajaAlumnoDeClase()`: Removes a student from a class
- `ServicioAlumno.obtenerAlumnosDisponiblesPaginados()`: Gets available students

### Authorization Logic

The `puedeAccederAClase()` method in `ServicioClase` ensures that:
- Admins can access all classes
- Professors can only access classes they teach
- Students can access all classes (for viewing catalog)

This creates a secure and elegant API that meets all the requirements for professor student management.
