# Entity Graph Usage Guide

## Overview
This guide explains how to use the Named Entity Graphs we've added to optimize JPA queries and prevent N+1 problems.

## What Are Entity Graphs?

Entity Graphs tell JPA which relationships to load eagerly for specific queries, without changing the global fetch type configuration.

## Available Entity Graphs

### Clase Entity Graphs
- `Clase.withStudentsAndTeachers` - Loads students, teachers, and material
- `Clase.withExercises` - Loads exercises
- `Clase.withAllRelationships` - Loads everything (students, teachers, exercises, material)

### Alumno Entity Graphs
- `Alumno.withClasses` - Loads classes
- `Alumno.withAllRelationships` - Loads classes, payments, and submissions

### Ejercicio Entity Graphs
- `Ejercicio.withClase` - Loads the class
- `Ejercicio.withEntregas` - Loads submissions
- `Ejercicio.withAllRelationships` - Loads class and submissions

## How to Use Entity Graphs

### 1. In Repository Methods

```java
// In RepositorioClase.java
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForDashboard();

@EntityGraph(value = "Clase.withExercises")
List<Clase> findAllForExerciseManagement();

@EntityGraph(value = "Clase.withAllRelationships")
Optional<Clase> findByIdWithAllRelationships(@Param("claseId") Long claseId);
```

### 2. In Service Methods

```java
// In ServicioClase.java
@Transactional(readOnly = true)
public List<DTOClase> obtenerClasesParaDashboard() {
    // This will use the Entity Graph to load students, teachers, and material
    return repositorioClase.findAllForDashboard()
            .stream()
            .map(DTOClase::new)
            .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public DTOClase obtenerClaseConDetalles(Long claseId) {
    // This will use the Entity Graph to load everything
    Clase clase = repositorioClase.findByIdWithAllRelationships(claseId)
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
    return new DTOClase(clase);
}
```

### 3. Dynamic Entity Graphs

You can also create Entity Graphs dynamically:

```java
// In RepositorioClase.java
@EntityGraph(attributePaths = {"students", "teachers"})
List<Clase> findAllWithStudentsAndTeachers();

@EntityGraph(attributePaths = {"exercises.entregas"})
List<Clase> findAllWithExercisesAndSubmissions();
```

## Use Case Examples

### 1. Admin Dashboard
```java
// Load classes with students and teachers for admin view
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForAdminDashboard();
```

### 2. Exercise Management
```java
// Load classes with exercises for professor view
@EntityGraph(value = "Clase.withExercises")
List<Clase> findAllForExerciseManagement();
```

### 3. Student Profile
```java
// Load student with all their classes and payments
@EntityGraph(value = "Alumno.withAllRelationships")
Optional<Alumno> findByIdWithAllRelationships(@Param("alumnoId") Long alumnoId);
```

### 4. Exercise Details
```java
// Load exercise with class and all submissions
@EntityGraph(value = "Ejercicio.withAllRelationships")
Optional<Ejercicio> findByIdWithAllRelationships(@Param("ejercicioId") Long ejercicioId);
```

## Performance Benefits

### Before (N+1 Problem)
```java
// This would cause N+1 queries
List<Clase> clases = repositorioClase.findAll();
for (Clase clase : clases) {
    // Each access to students/teachers triggers a new query
    System.out.println(clase.getStudents().size());
    System.out.println(clase.getTeachers().size());
}
```

### After (Optimized)
```java
// This uses Entity Graph - single query with JOINs
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> clases = repositorioClase.findAllForDashboard();
for (Clase clase : clases) {
    // No additional queries - data already loaded
    System.out.println(clase.getStudents().size());
    System.out.println(clase.getTeachers().size());
}
```

## When to Use Each Entity Graph

### Clase Entity Graphs
- **`withStudentsAndTeachers`**: Admin dashboards, class overview pages
- **`withExercises`**: Exercise management, professor views
- **`withAllRelationships`**: Detailed class views, comprehensive reports

### Alumno Entity Graphs
- **`withClasses`**: Student dashboard, enrollment views
- **`withAllRelationships`**: Student profile pages, comprehensive student data

### Ejercicio Entity Graphs
- **`withClase`**: Exercise details when you need class info
- **`withEntregas`**: Exercise grading, submission review
- **`withAllRelationships`**: Complete exercise analysis

## Best Practices

### 1. Use Specific Entity Graphs
```java
// Good - loads only what you need
@EntityGraph(value = "Clase.withStudentsAndTeachers")
List<Clase> findAllForDashboard();

// Avoid - loads everything when you only need students
@EntityGraph(value = "Clase.withAllRelationships")
List<Clase> findAllForDashboard();
```

### 2. Combine with Query Filters
```java
@EntityGraph(value = "Clase.withExercises")
@Query("SELECT c FROM Clase c WHERE c.difficulty = :difficulty")
List<Clase> findByDifficultyWithExercises(@Param("difficulty") EDificultad difficulty);
```

### 3. Use in Read-Only Transactions
```java
@Transactional(readOnly = true)
public List<DTOClase> obtenerClasesParaDashboard() {
    return repositorioClase.findAllForDashboard()
            .stream()
            .map(DTOClase::new)
            .collect(Collectors.toList());
}
```

## Monitoring Performance

With the development configuration we added, you can monitor Entity Graph performance:

```yaml
# In application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
```

This will show you:
- The actual SQL queries generated
- Query execution statistics
- Performance metrics

## Migration Strategy

### Phase 1: Add Entity Graph Methods (✅ Done)
- Added Entity Graphs to entities
- Added optimized repository methods

### Phase 2: Update Service Layer
- Replace existing methods with Entity Graph versions
- Update DTOs to handle loaded relationships

### Phase 3: Update REST Controllers
- Use optimized service methods
- Monitor performance improvements

## Example Service Updates

```java
// Before
public DTOClase obtenerClase(Long claseId) {
    Clase clase = repositorioClase.findById(claseId)
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
    return new DTOClase(clase);
}

// After - with Entity Graph
public DTOClase obtenerClase(Long claseId) {
    Clase clase = repositorioClase.findByIdWithAllRelationships(claseId)
            .orElseThrow(() -> new ResourceNotFoundException("Clase no encontrada"));
    return new DTOClase(clase);
}
```

## Conclusion

Entity Graphs provide a flexible way to optimize JPA queries without changing your global fetch strategy. Use them for specific use cases where you know which relationships will be needed.
