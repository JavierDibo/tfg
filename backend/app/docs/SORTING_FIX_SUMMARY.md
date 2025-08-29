# Sorting Fix Summary

## Problem
The frontend was sending sorting parameters using Spanish field names (e.g., `sortBy=titulo`), but the Spring Data JPA entity properties are in English (e.g., `title`). This caused a `PropertyReferenceException` when trying to sort by fields like `titulo`, `descripcion`, etc.

## Error Message
```
PropertyReferenceException - No property 'titulo' found for type 'Clase'; Did you mean 'title'
```

## Root Cause
The issue was in the `ClaseRest.java` controller where:
1. The `validateSortBy` method was called with Spanish field names (`"titulo"`, `"descripcion"`, etc.)
2. These Spanish names were then passed directly to Spring Data JPA for sorting
3. Spring Data JPA tried to find properties with Spanish names on the `Clase` entity, which only has English property names

## Solution
Added a mapping method `mapSortFieldToEntityProperty()` in `ClaseRest.java` that translates Spanish field names to English entity property names:

```java
private String mapSortFieldToEntityProperty(String sortBy) {
    return switch (sortBy.toLowerCase()) {
        case "titulo" -> "title";
        case "descripcion" -> "description";
        case "dificultad" -> "difficulty";
        case "presencialidad" -> "format";
        case "precio" -> "price";
        case "nivel" -> "difficulty";
        default -> sortBy; // Keep as is for fields that don't need mapping (id, etc.)
    };
}
```

## Changes Made
1. **ClaseRest.java**: Added the mapping method and applied it to all three endpoints:
   - `GET /api/clases` (main endpoint)
   - `GET /api/clases/catalog` (catalog endpoint)
   - `GET /api/clases/disponibles` (available classes endpoint)

2. **Mapping Applied**: After validation but before passing to the service layer, the `sortBy` parameter is now mapped from Spanish to English field names.

3. **Validation Updated**: Added `precio` and `nivel` to the list of valid sort fields in all three endpoints to prevent validation errors.

## Field Mapping
| Spanish (Frontend) | English (Entity) |
|-------------------|------------------|
| `titulo`          | `title`          |
| `descripcion`     | `description`    |
| `dificultad`      | `difficulty`     |
| `presencialidad`  | `format`         |
| `precio`          | `price`          |
| `nivel`           | `difficulty`     |

## Testing
The fix ensures that:
- Frontend can continue using Spanish field names for sorting
- Backend correctly maps these to English entity property names
- Spring Data JPA can successfully sort by the correct entity properties
- No breaking changes to the API interface

## Result
The sorting functionality now works correctly without throwing `PropertyReferenceException` errors. The frontend can sort by `titulo` and it will be properly translated to sort by the `title` field in the database.
