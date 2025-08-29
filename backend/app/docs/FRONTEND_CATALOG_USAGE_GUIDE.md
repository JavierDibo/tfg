# Frontend Catalog Usage Guide

## Quick Start

### 1. Generated API Client Function

After running `npm run api`, you'll have access to:

```typescript
// Generated function from OpenAPI
api.getClasesCatalog(params?: {
  q?: string;
  titulo?: string;
  descripcion?: string;
  dificultad?: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO';
  presencialidad?: 'ONLINE' | 'PRESENCIAL' | 'HIBRIDO';
  profesorId?: string;
  cursoId?: string;
  tallerId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
})
```

### 2. Basic Usage

```typescript
// Get all classes with enrollment status
const response = await api.getClasesCatalog({
  page: 0,
  size: 20
});

// Response includes enrollment status for students
response.data.content.forEach(clase => {
  if (clase.isEnrolled) {
    // Student is enrolled in this class
    console.log(`✅ Enrolled in: ${clase.titulo}`);
  } else {
    // Student can enroll in this class
    console.log(`📚 Available: ${clase.titulo}`);
  }
});
```

### 3. Filtering Examples

```typescript
// Get only online classes
const onlineClasses = await api.getClasesCatalog({
  presencialidad: 'ONLINE',
  page: 0,
  size: 50
});

// Search by title
const javaClasses = await api.getClasesCatalog({
  q: 'Java',
  page: 0,
  size: 20
});

// Filter by difficulty
const advancedClasses = await api.getClasesCatalog({
  dificultad: 'AVANZADO',
  page: 0,
  size: 20
});
```

### 4. Frontend Display Logic

```typescript
// Display classes with enrollment indicators
const displayClasses = (classes: ClaseConEstadoInscripcion[]) => {
  classes.forEach(clase => {
    if (clase.isEnrolled) {
      // Show enrolled badge
      renderEnrolledClass(clase);
    } else {
      // Show enroll button
      renderAvailableClass(clase);
    }
  });
};

// Filter enrolled vs available classes
const enrolledClasses = response.data.content.filter(c => c.isEnrolled);
const availableClasses = response.data.content.filter(c => !c.isEnrolled);
```

### 5. Response Structure

```typescript
interface ClaseConEstadoInscripcion {
  id: number;
  titulo: string;
  descripcion: string;
  precio: number;
  presencialidad: 'ONLINE' | 'PRESENCIAL' | 'HIBRIDO';
  imagenPortada: string;
  nivel: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO';
  tipoClase: 'CURSO' | 'TALLER';
  
  // Enrollment status (for students)
  isEnrolled: boolean;
  fechaInscripcion?: string; // ISO date string, only if enrolled
}
```

## Key Points

- **For Students**: `isEnrolled` shows enrollment status, `fechaInscripcion` shows enrollment date
- **For Admins/Professors**: `isEnrolled` is always `false`, `fechaInscripcion` is always `null`
- **Pagination**: Use `page` (0-indexed) and `size` (max 100) for pagination
- **Filters**: All standard filters work (search, difficulty, modality, etc.)

## Migration from Old Endpoints

**Before:**
```typescript
// Had to use two separate endpoints
const allClasses = await api.getClases();
const availableClasses = await api.getClasesDisponibles();
```

**After:**
```typescript
// Single endpoint with enrollment status
const catalog = await api.getClasesCatalog();
// Filter as needed in frontend
const enrolled = catalog.data.content.filter(c => c.isEnrolled);
const available = catalog.data.content.filter(c => !c.isEnrolled);
```
