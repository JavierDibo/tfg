# OpenAPI Array Specification Fixes

## Summary

Fixed critical OpenAPI specification issues where endpoints returning `List<T>` (arrays) were incorrectly documented as returning single objects instead of arrays. This was causing the frontend API generation to create incorrect TypeScript types and causing parsing issues.

## Problem Description

The frontend was experiencing issues where:
- Generated API methods were returning `Promise<DTOClaseInscrita>` instead of `Promise<Array<DTOClaseInscrita>>`
- JSON parsing was failing because the response was an array but the type expected a single object
- This forced the frontend to use custom HTTP requests instead of the generated API

## Root Cause

The backend's OpenAPI specification was missing proper array type definitions. When a method returns `ResponseEntity<List<SomeDTO>>`, the `@ApiResponse` annotation must specify that it returns an array, not a single object.

## Fixes Applied

### 1. AlumnoRest.java - Fixed `obtenerClasesInscritas` endpoint

**Before:**
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Classes enrolled retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DTOClaseInscrita.class)  // ❌ Missing array type
        )
    )
})
```

**After:**
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Classes enrolled retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DTOClaseInscrita.class, type = "array")  // ✅ Fixed
        )
    )
})
```

### 2. UserClaseRest.java - Fixed multiple endpoints

**Added proper @ApiResponse annotations to:**
- `obtenerMisClases()` - Returns `List<DTOClase>`
- `obtenerMisClasesInscritas()` - Returns `List<DTOClaseInscrita>`
- `obtenerAlumnosPublicosDeClase()` - Returns `List<DTOAlumnoPublico>`

**Example fix:**
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Lista de clases obtenida exitosamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DTOClase.class, type = "array")
        )
    ),
    @ApiResponse(responseCode = "403", description = "No autorizado")
})
```

### 3. ProfesorRest.java - Fixed `obtenerProfesoresPorClase` endpoint

**Before:**
```java
@ApiResponse(
    responseCode = "200",
    description = "Professors of the class obtained successfully"  // ❌ Missing schema
)
```

**After:**
```java
@ApiResponse(
    responseCode = "200",
    description = "Professors of the class obtained successfully",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = DTOProfesor.class, type = "array")
    )
)
```

### 4. ClaseRest.java - Fixed multiple endpoints

**Added proper @ApiResponse annotations to:**
- `obtenerClases()` - Returns `List<DTOClase>`
- `buscarClasesPorTitulo()` - Returns `List<DTOClase>`
- `buscarClasesPorTerminoGeneral()` - Returns `List<DTOClase>`
- `obtenerClasesPorAlumno()` - Returns `List<DTOClase>`
- `obtenerClasesPorProfesor()` - Returns `List<DTOClase>`

**Also added missing imports:**
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
```

### 5. EnrollmentRest.java - Fixed `obtenerMisClasesInscritas` endpoint

**Added proper @ApiResponse annotation:**
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Clases inscritas obtenidas exitosamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DTOClaseInscrita.class, type = "array")
        )
    ),
    @ApiResponse(responseCode = "403", description = "No autorizado")
})
```

**Also added missing imports:**
```java
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
```

## Technical Details

### Correct OpenAPI 3.0 Syntax

For endpoints returning arrays, use:
```java
@Schema(implementation = SomeDTO.class, type = "array")
```

This tells OpenAPI that the response is an array of the specified DTO type.

### Files Modified

1. `src/main/java/app/rest/AlumnoRest.java`
2. `src/main/java/app/rest/UserClaseRest.java`
3. `src/main/java/app/rest/ProfesorRest.java`
4. `src/main/java/app/rest/ClaseRest.java`
5. `src/main/java/app/rest/EnrollmentRest.java`

## Impact

### Before Fix
- Frontend generated API returned incorrect types (`Promise<DTO>` instead of `Promise<DTO[]>`)
- JSON parsing failed with `undefined` fields
- Frontend had to use custom HTTP requests as workarounds

### After Fix
- Frontend will generate correct array types (`Promise<DTO[]>`)
- JSON parsing will work correctly
- Frontend can use the generated API methods directly
- No more custom HTTP request workarounds needed

## Next Steps

1. **Regenerate Frontend API**: Run `npm run api` in the frontend to regenerate the API with correct types
2. **Update Frontend Code**: Replace custom HTTP requests with generated API methods
3. **Test**: Verify that all list endpoints work correctly with the generated API

## Testing

All changes have been tested:
- ✅ Compilation successful
- ✅ All existing tests pass
- ✅ No breaking changes to existing functionality

The backend is now ready to generate correct OpenAPI specifications for all array-returning endpoints.
