# General Search Functionality Implementation

## Overview

This document describes the implementation of a hybrid search system that combines general search capabilities with existing specific filters. The system now supports both a general search parameter (`q`) and detailed specific filters, providing flexibility for different search scenarios.

## Key Features

### 1. General Search Parameter (`q`)
- **Purpose**: Single search term that searches across multiple relevant fields
- **Usage**: Provides quick, broad search capabilities
- **Implementation**: Uses database-level text normalization for accent-insensitive and case-insensitive search

### 2. Hybrid Search Approach
- **General Search Only**: When only `q` parameter is provided
- **Specific Filters Only**: When only specific parameters are provided (existing functionality)
- **Combined Search**: When both `q` and specific filters are provided (new functionality)

### 3. Backward Compatibility
- All existing search endpoints continue to work as before
- New `q` parameter is optional and doesn't break existing functionality
- Existing specific filters maintain their current behavior

## Implementation Details

### DTOs (Data Transfer Objects)

#### DTOParametrosBusquedaAlumno
```java
public record DTOParametrosBusquedaAlumno(
    String q,              // General search term across multiple fields
    String nombre,
    String apellidos,
    String dni,
    String email,
    Boolean matriculado
) {
    // Helper methods for search logic
    public boolean hasGeneralSearch() { /* ... */ }
    public boolean hasSpecificFilters() { /* ... */ }
    public boolean estaVacio() { /* ... */ }
}
```

**General Search Fields**: `nombre`, `apellidos`, `dni`, `email`

#### DTOParametrosBusquedaProfesor
```java
public record DTOParametrosBusquedaProfesor(
    String q,              // General search term across multiple fields
    String nombre,
    String apellidos,
    String email,
    String usuario,
    String dni,
    Boolean habilitado,
    String claseId,
    Boolean sinClases
) {
    // Helper methods for search logic
    public boolean hasGeneralSearch() { /* ... */ }
    public boolean hasSpecificFilters() { /* ... */ }
    public boolean estaVacio() { /* ... */ }
}
```

**General Search Fields**: `nombre`, `apellidos`, `email`, `usuario`, `dni`

#### DTOParametrosBusquedaClase
```java
public record DTOParametrosBusquedaClase(
    String q,                    // General search term across title and description
    String titulo,
    String descripcion,
    // ... other fields
) {
    // Helper methods for search logic
    public boolean hasGeneralSearch() { /* ... */ }
    public boolean hasSpecificFilters() { /* ... */ }
    public boolean estaVacio() { /* ... */ }
}
```

**General Search Fields**: `titulo`, `descripcion`

### Repository Layer

#### New Query Methods

Each repository now includes three types of search methods:

1. **General Search Only**: `findByGeneralSearch(String searchTerm, Pageable pageable)`
2. **Combined Search**: `findByGeneralAndSpecificFilters(...)`
3. **Existing Specific Filters**: (unchanged)

#### Example: RepositorioAlumno
```java
// General search using database normalization
@Query(value = "SELECT * FROM usuarios a WHERE " +
       "(normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
       "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
       "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
       "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%') AND " +
       "a.tipo_usuario = 'ALUMNO'", nativeQuery = true)
Page<Alumno> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

// Combined search with conditional logic
@Query(value = "SELECT * FROM usuarios a WHERE " +
       "(:searchTerm IS NULL OR (" +
       "normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
       "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
       "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
       "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%')) AND " +
       "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
       // ... other specific filters
       "a.tipo_usuario = 'ALUMNO'", nativeQuery = true)
Page<Alumno> findByGeneralAndSpecificFilters(
    @Param("searchTerm") String searchTerm, 
    @Param("nombre") String nombre,
    // ... other parameters
    Pageable pageable
);
```

### Service Layer

#### Search Logic Implementation

The service layer implements intelligent search logic that chooses the appropriate repository method based on the provided parameters:

```java
public DTORespuestaPaginada<DTOAlumno> buscarAlumnosPorParametrosPaginados(
        DTOParametrosBusquedaAlumno parametros, int page, int size, String sortBy, String sortDirection) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    Page<Alumno> pageAlumnos;
    
    if (parametros.hasGeneralSearch()) {
        if (parametros.hasSpecificFilters()) {
            // Combined search: general + specific filters
            pageAlumnos = repositorioAlumno.findByGeneralAndSpecificFilters(
                parametros.q(), parametros.nombre(), parametros.apellidos(),
                parametros.dni(), parametros.email(), parametros.matriculado(), pageable
            );
        } else {
            // General search only
            pageAlumnos = repositorioAlumno.findByGeneralSearch(parametros.q(), pageable);
        }
    } else {
        if (parametros.hasSpecificFilters()) {
            // Specific filters only (existing functionality)
            pageAlumnos = repositorioAlumno.findByFiltrosPaged(
                parametros.nombre(), parametros.apellidos(), parametros.dni(),
                parametros.email(), parametros.matriculado(), pageable
            );
        } else {
            // No filters - return all
            pageAlumnos = repositorioAlumno.findAllPaged(pageable);
        }
    }
    
    // Convert to DTOs and return
    return convertToPagedResponse(pageAlumnos);
}
```

### REST Controller Layer

#### New Endpoint Parameters

All search endpoints now accept an optional `q` parameter:

```java
@GetMapping("/paginados")
public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnosPaginados(
        @Parameter(description = "Término de búsqueda general (busca en nombre, apellidos, DNI, email)", required = false)
        @RequestParam(required = false) @Size(max = 100) String q,
        @RequestParam(required = false) @Size(max = 50) String nombre,
        @RequestParam(required = false) @Size(max = 100) String apellidos,
        @RequestParam(required = false) @Size(max = 9) String dni,
        @RequestParam(required = false) @Size(max = 100) String email,
        @RequestParam(required = false) Boolean matriculado,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamanoPagina,
        @RequestParam(defaultValue = "id") String ordenCampo,
        @RequestParam(defaultValue = "ASC") String ordenDireccion) {
    
    DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
        q, nombre, apellidos, dni, email, matriculado);
    
    return ResponseEntity.ok(servicioAlumno.buscarAlumnosPorParametrosPaginados(
        parametros, pagina, tamanoPagina, ordenCampo, ordenDireccion));
}
```

#### New General Search Endpoint for Classes

A new endpoint was added for general class search:

```java
@GetMapping("/buscar/general")
@Operation(summary = "Buscar clases con término general", description = "Busca clases con término general en título y descripción")
public ResponseEntity<List<DTOClase>> buscarClasesPorTerminoGeneral(@RequestParam String q) {
    DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
        q, null, null, null, null, null, null, null, 0, 100, "id", "ASC");
    return ResponseEntity.ok(servicioClase.buscarClasesSegunRol(parametros).contenido());
}
```

## Database Considerations

### Text Normalization

The system uses a `normalize_text()` database function for accent-insensitive and case-insensitive search:

```sql
normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%'
```

This function should be available in your PostgreSQL database. If not, you may need to create it or modify the queries to use standard SQL functions.

### Performance Considerations

1. **Indexing**: Ensure proper indexes on searchable fields
2. **Query Optimization**: The combined search queries use conditional logic to avoid unnecessary conditions
3. **Pagination**: All search methods support pagination to handle large result sets

## Testing

### Unit Tests

Comprehensive unit tests have been created to verify the search functionality:

```java
@WebMvcTest(AlumnoRest.class)
public class AlumnoRestTest {
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerAlumnosPaginadosConParametroQ() throws Exception {
        // Test general search only
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerAlumnosPaginadosConParametroQYFiltrosEspecificos() throws Exception {
        // Test combined search
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerAlumnosPaginadosSinParametroQ() throws Exception {
        // Test specific filters only
    }
}
```

## Usage Examples

### General Search Only
```
GET /api/alumnos/paginados?q=maria&pagina=0&tamanoPagina=10
```
Searches for "maria" across nombre, apellidos, DNI, and email fields.

### Combined Search
```
GET /api/alumnos/paginados?q=garcia&email=gmail&matriculado=true&pagina=0&tamanoPagina=10
```
Searches for "garcia" across all general fields AND filters by email containing "gmail" AND matriculado=true.

### Specific Filters Only (Existing)
```
GET /api/alumnos/paginados?nombre=Juan&apellidos=Garcia&pagina=0&tamanoPagina=10
```
Uses only specific filters (existing functionality).

## Migration Notes

### Backward Compatibility
- All existing search endpoints continue to work without modification
- The `q` parameter is optional and doesn't affect existing functionality
- No database schema changes required

### Dependencies
- Spring Data JPA
- PostgreSQL (for `normalize_text()` function)
- Spring Boot Validation

## Future Enhancements

1. **Fuzzy Search**: Implement fuzzy matching for typo tolerance
2. **Search Suggestions**: Add autocomplete functionality
3. **Search Analytics**: Track popular search terms
4. **Advanced Filters**: Add date ranges, numeric ranges, etc.
5. **Search Result Highlighting**: Highlight matching terms in results

## Troubleshooting

### Common Issues

1. **Database Function Not Found**: Ensure `normalize_text()` function exists in your PostgreSQL database
2. **Performance Issues**: Check database indexes on searchable fields
3. **Case Sensitivity**: Verify that the normalization function handles case properly

### Debugging

Enable SQL logging to debug query performance:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

## Conclusion

The general search functionality provides a flexible and powerful search system that maintains backward compatibility while adding new capabilities. The hybrid approach ensures that users can choose between quick general searches and detailed specific filtering based on their needs.
