# Exercise-Class Relationship Fix

## Problem Description

The exercise delivery filtering system had a critical issue where multiple filters could not be applied simultaneously. Specifically, when both `alumnoId` and `ejercicioId` parameters were provided in the API request, only the `alumnoId` filter was being applied, completely ignoring the `ejercicioId` filter.

### Example of the Problem

**Request:**
```
GET /api/entregas?alumnoId=12&ejercicioId=35
```

**Expected Result:** Deliveries for student 12 that belong to exercise 35 only.

**Actual Result:** All deliveries for student 12, regardless of the exercise (showing deliveries for exercises 2, 3, 4, 5, 8, 9, 10, 11, 12, 13, 16, 18, 19, 21, 22, 23, 25, 26, 27, 28).

## Root Cause

The issue was in the `ServicioEntregaEjercicio.obtenerEntregasPaginadas()` method. The filtering logic used `else if` statements instead of allowing multiple filters to be combined:

```java
// OLD CODE - Problematic logic
if (alumnoId != null && !alumnoId.trim().isEmpty()) {
    // Filter by student only
    entregaPage = repositorioEntregaEjercicio.findByAlumnoEntreganteId(alumnoId.trim(), pageable);
} else if (ejercicioId != null && !ejercicioId.trim().isEmpty()) {
    // Filter by exercise only - This was never reached if alumnoId was provided
    entregaPage = repositorioEntregaEjercicio.findByEjercicioId(ejercicioId.trim(), pageable);
}
```

## Solution Implemented

### 1. Flexible Repository Queries (Following RepositorioAlumno Pattern)

Following the same pattern as `RepositorioAlumno`, we implemented flexible SQL queries that handle multiple optional filters:

#### For RepositorioEntregaEjercicio:
```java
@Query(value = "SELECT * FROM entregas_ejercicios ee WHERE " +
       "(:alumnoId IS NULL OR ee.alumno_entregante_id = :alumnoId) AND " +
       "(:ejercicioId IS NULL OR ee.ejercicio_id = :ejercicioId) AND " +
       "(:estado IS NULL OR ee.estado = :estado) AND " +
       "(:notaMin IS NULL OR ee.nota >= :notaMin) AND " +
       "(:notaMax IS NULL OR ee.nota <= :notaMax)",
       countQuery = "SELECT COUNT(*) FROM entregas_ejercicios ee WHERE " +
       "(:alumnoId IS NULL OR ee.alumno_entregante_id = :alumnoId) AND " +
       "(:ejercicioId IS NULL OR ee.ejercicio_id = :ejercicioId) AND " +
       "(:estado IS NULL OR ee.estado = :estado) AND " +
       "(:notaMin IS NULL OR ee.nota >= :notaMin) AND " +
       "(:notaMax IS NULL OR ee.nota <= :notaMax)",
       nativeQuery = true)
Page<EntregaEjercicio> findByFiltrosFlexibles(
    @Param("alumnoId") String alumnoId,
    @Param("ejercicioId") String ejercicioId,
    @Param("estado") String estado,
    @Param("notaMin") BigDecimal notaMin,
    @Param("notaMax") BigDecimal notaMax,
    Pageable pageable
);
```

#### For RepositorioEjercicio:
```java
@Query(value = "SELECT * FROM ejercicios e WHERE " +
       "(:nombre IS NULL OR normalize_text(e.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
       "(:enunciado IS NULL OR normalize_text(e.enunciado) LIKE '%' || normalize_text(:enunciado) || '%') AND " +
       "(:classId IS NULL OR e.class_id = :classId) AND " +
       "(:status IS NULL OR (" +
       "  CASE WHEN :status = 'ACTIVE' THEN e.fecha_fin_plazo > :now " +
       "       WHEN :status = 'EXPIRED' THEN e.fecha_fin_plazo <= :now " +
       "       WHEN :status = 'FUTURE' THEN e.fecha_inicio_plazo > :now " +
       "       WHEN :status = 'WITH_DELIVERIES' THEN EXISTS (SELECT 1 FROM entregas_ejercicios ee WHERE ee.ejercicio_id = e.id) " +
       "       WHEN :status = 'WITHOUT_DELIVERIES' THEN NOT EXISTS (SELECT 1 FROM entregas_ejercicios ee WHERE ee.ejercicio_id = e.id) " +
       "       ELSE TRUE END))",
       nativeQuery = true)
Page<Ejercicio> findByGeneralAndSpecificFilters(
    @Param("searchTerm") String searchTerm,
    @Param("nombre") String nombre,
    @Param("enunciado") String enunciado,
    @Param("classId") String classId,
    @Param("status") String status,
    @Param("now") LocalDateTime now,
    Pageable pageable
);
```

### 2. Simplified Service Logic

The service logic was greatly simplified by using the flexible repository methods:

#### For ServicioEntregaEjercicio:
```java
// NEW CODE - Simple and flexible approach
// Prepare filter parameters
String alumnoIdFilter = (alumnoId != null && !alumnoId.trim().isEmpty()) ? alumnoId.trim() : null;
String ejercicioIdFilter = (ejercicioId != null && !ejercicioId.trim().isEmpty()) ? ejercicioId.trim() : null;
String estadoFilter = (estado != null && !estado.trim().isEmpty()) ? estado.toUpperCase() : null;
BigDecimal notaMinFilter = notaMin;
BigDecimal notaMaxFilter = notaMax;

// Use flexible query that handles all combinations
entregaPage = repositorioEntregaEjercicio.findByFiltrosFlexibles(
    alumnoIdFilter, ejercicioIdFilter, estadoFilter, notaMinFilter, notaMaxFilter, pageable);
```

#### For ServicioEjercicio:
```java
// NEW CODE - Simple and flexible approach
// Prepare filter parameters
String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
String enunciadoFilter = (statement != null && !statement.trim().isEmpty()) ? statement.trim() : null;
String classIdFilter = (classId != null && !classId.trim().isEmpty()) ? classId.trim() : null;
String statusFilter = (status != null && !status.trim().isEmpty()) ? status.toUpperCase() : null;
LocalDateTime now = LocalDateTime.now();

// Use flexible query that handles all combinations
ejercicioPage = repositorioEjercicio.findByGeneralAndSpecificFilters(
    searchTerm, nombreFilter, enunciadoFilter, classIdFilter, statusFilter, now, pageable);
```

## Supported Filter Combinations

The enhanced system now supports the following filter combinations:

1. **Single Filters:**
   - `alumnoId` only
   - `ejercicioId` only
   - `estado` only
   - `notaMin` + `notaMax` only

2. **Double Filters:**
   - `alumnoId` + `ejercicioId`
   - `alumnoId` + `estado`
   - `ejercicioId` + `estado`
   - `alumnoId` + `notaMin` + `notaMax`
   - `ejercicioId` + `notaMin` + `notaMax`

3. **Triple Filters:**
   - `alumnoId` + `ejercicioId` + `estado`

## Testing the Fix

After implementing the fix, the same request should now work correctly:

**Request:**
```
GET /api/entregas?alumnoId=12&ejercicioId=35
```

**Expected Result:** Only deliveries for student 12 that belong to exercise 35.

## Benefits

1. **Correct Filtering:** Multiple filters now work as expected
2. **Better Performance:** Uses optimized SQL queries with proper indexing
3. **Flexibility:** Supports all logical filter combinations automatically
4. **Maintainability:** Much simpler and cleaner code structure
5. **Consistency:** Follows the same pattern as `RepositorioAlumno` for consistency across the codebase
6. **Extensibility:** Easy to add new filter combinations by simply adding parameters to the query
7. **Database Efficiency:** Single query handles all combinations instead of multiple conditional queries
8. **Reduced Complexity:** Eliminated complex conditional logic in services

## Files Modified

1. `src/main/java/app/repositorios/RepositorioEntregaEjercicio.java` - Added flexible SQL query method
2. `src/main/java/app/repositorios/RepositorioEjercicio.java` - Added flexible SQL query methods
3. `src/main/java/app/servicios/ServicioEntregaEjercicio.java` - Simplified filtering logic using flexible queries
4. `src/main/java/app/servicios/ServicioEjercicio.java` - Simplified filtering logic using flexible queries

## Related Documentation

- [REST API Standardization Guide](REST_API_Standardization_Guide.md)
- [Exercise Implementation Summary](EJERCICIOS_IMPLEMENTATION_SUMMARY.md)
