# REST API Standardization Guide
## Implementing Consistent Pagination and API Patterns

### Current State Analysis

After analyzing your existing REST classes, I've identified several inconsistencies that need to be addressed:

1. **Mixed pagination approaches**: Some endpoints use `/paged` paths, others use POST with body parameters
2. **Inconsistent parameter naming**: `page` vs `numeroPagina`, `size` vs `tamanoPagina`
3. **Different filtering strategies**: Some use query parameters, others use request bodies
4. **Varying response structures**: Some return lists directly, others use pagination DTOs
5. **Inconsistent endpoint patterns**: Some follow REST conventions, others have custom paths

### Standardized REST API Design

#### Core REST Endpoints Pattern

| Method | Resource Path | Description | Query Parameters | Returns | Response Codes |
|--------|---------------|-------------|------------------|---------|----------------|
| GET | `/{resource}` | Get paginated collection | `page`, `size`, `sortBy`, `sortDirection`, `filter` | `DTORespuestaPaginada<T>` | 200, 400, 403 |
| GET | `/{resource}/{id}` | Get specific resource | None | Single DTO object | 200, 404, 403 |
| POST | `/{resource}` | Create new resource | None | Created DTO object | 201, 400, 409, 403 |
| PUT | `/{resource}/{id}` | Replace entire resource | None | Updated DTO object | 200, 400, 404, 403 |
| PATCH | `/{resource}/{id}` | Partial update | None | Updated DTO object | 200, 400, 404, 403 |
| DELETE | `/{resource}/{id}` | Delete resource | None | Empty body | 204, 404, 403 |

#### Standard Query Parameters

All collection endpoints (`GET /{resource}`) should support these parameters:

| Parameter | Type | Default | Description | Validation |
|-----------|------|---------|-------------|------------|
| `page` | int | 0 | Page number (0-indexed) | `@Min(0)` |
| `size` | int | 20 | Page size | `@Min(1) @Max(100)` |
| `sortBy` | String | "id" | Field to sort by | `@Size(max=50)` |
| `sortDirection` | String | "ASC" | Sort direction | `@Pattern(regexp="ASC\|DESC")` |
| `filter` | String | null | Simple filter (field:value) | `@Size(max=100)` |

#### Standard Response Structure

All paginated responses should use the existing `DTORespuestaPaginada<T>` structure, but updating the names to have consistent naming across all endpoints:
- `page` (not `numeroPagina`)
- `size` (not `tamanoPagina`)
- `sortBy` (not `ordenadoPor`)
- `sortDirection` (not `direccionOrden`)

```java
public record DTORespuestaPaginada<T>(
    List<T> contenido,           // List of items for current page
    int numeroPagina,            // Current page number (0-indexed)
    int tamanoPagina,            // Page size
    long totalElementos,         // Total count across all pages
    int totalPaginas,            // Total number of pages
    boolean esPrimera,           // Is first page
    boolean esUltima,            // Is last page
    boolean tieneContenido,      // Has content
    String ordenadoPor,          // Field sorted by
    String direccionOrden        // Sort direction
)
```

### Implementation Plan

#### Phase 1: Standardize Existing Endpoints

1. **AlumnoRest.java** ✅ (Already well-structured)
   - Keep current `/paged` endpoint
   - Standardize parameter names if needed

2. **ProfesorRest.java** ✅ (Already well-structured)
   - Keep current `/paged` endpoint
   - Remove deprecated non-paginated endpoint

3. **ClaseRest.java** 🔄 (Needs standardization)
   - Convert POST `/buscar` to GET with query parameters
   - Add standard pagination endpoint
   - Keep role-based filtering logic

4. **EnrollmentRest.java** ✅ (No pagination needed - simple CRUD)
   - Keep current structure
   - Add pagination only if needed for bulk operations

5. **UserClaseRest.java** ✅ (Already well-structured)
   - Keep current pagination patterns

#### Phase 2: Create Standard Base Classes

Create a base REST controller with common pagination logic:

```java
public abstract class BaseRestController<T, DTO, SearchParams> {
    
    protected ResponseEntity<DTORespuestaPaginada<DTO>> getPaginatedResponse(
            SearchParams params, 
            int page, 
            int size, 
            String sortBy, 
            String sortDirection) {
        
        // Common pagination logic
        // Common validation
        // Common error handling
    }
}
```

#### Phase 3: Implement Consistent Error Handling

Standardize HTTP status codes across all endpoints:

| Scenario | Status Code | Response Body |
|----------|-------------|---------------|
| Success | 200 | Data or paginated response |
| Created | 201 | Created resource |
| No Content | 204 | Empty (for DELETE) |
| Bad Request | 400 | Validation error details |
| Unauthorized | 401 | Authentication required |
| Forbidden | 403 | Insufficient permissions |
| Not Found | 404 | Resource not found |
| Conflict | 409 | Duplicate or constraint violation |

### Specific Changes Required

#### 1. ClaseRest.java - Convert to Standard Pattern

**Current (POST with body):**
```java
@PostMapping("/buscar")
public ResponseEntity<DTORespuestaPaginada<DTOClase>> buscarClases(
    @Valid @RequestBody DTOParametrosBusquedaClase parametros)
```

**New (GET with query parameters):**
```java
@GetMapping
public ResponseEntity<DTORespuestaPaginada<DTOClase>> obtenerClases(
    @RequestParam(required = false) String titulo,
    @RequestParam(required = false) String descripcion,
    @RequestParam(required = false) String tipo,
    @RequestParam(required = false) Boolean activa,
    @RequestParam(required = false) String profesorId,
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "ASC") @Pattern(regexp="ASC|DESC") String sortDirection)
```

#### 2. Standardize Parameter Names

Use consistent naming across all endpoints:
- `page` (not `numeroPagina`)
- `size` (not `tamanoPagina`)
- `sortBy` (not `ordenadoPor`)
- `sortDirection` (not `direccionOrden`)

#### 3. Remove Deprecated Endpoints

- Remove non-paginated versions of collection endpoints
- Keep only the paginated versions

### Benefits of This Standardization

1. **Consistency**: All endpoints follow the same pattern
2. **Maintainability**: Easier to maintain and extend
3. **Frontend Integration**: Consistent API contract for Svelte frontend
4. **Documentation**: Clearer API documentation with Swagger
5. **Testing**: Easier to write comprehensive tests
6. **Performance**: Consistent pagination prevents large data transfers

### Testing Checklist

- [ ] All paginated endpoints return consistent response structure
- [ ] Parameter validation works consistently across all endpoints
- [ ] Error responses follow standard format
- [ ] Pagination metadata is accurate
- [ ] Sorting works correctly on all sortable fields
- [ ] Filtering works consistently
- [ ] Role-based access control maintained
- [ ] Swagger documentation updated