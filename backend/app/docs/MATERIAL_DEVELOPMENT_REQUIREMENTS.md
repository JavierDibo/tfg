# Material Entity Development Requirements

## Overview
This document outlines the complete requirements and implementation details for developing the Material entity, services, repositories, and REST API. This serves as a template for developing similar entities like Ejercicio.

## 1. Entity Requirements

### 1.1 Material Entity (`src/main/java/app/entidades/Material.java`)
- **Fields**: Use English names for all variables
  - `id` (String) - Primary key
  - `name` (String) - Material name (renamed from `nombre`)
  - `url` (String) - Material URL
- **Constructor**: Accept English parameter names
- **Getters/Setters**: Standard JPA annotations
- **Validation**: Basic field validation

### 1.2 Key Implementation Notes
- **English Naming**: All variables must use English names (e.g., `name` instead of `nombre`)
- **JPA Annotations**: Use `@Entity`, `@Id`, `@Column` as needed
- **Constructor Updates**: Update constructor to use new English field names

## 2. DTO Requirements

### 2.1 DTOMaterial (`src/main/java/app/dtos/DTOMaterial.java`)
- **Record Structure**: Use Java record for immutability
- **Fields**: Match entity fields with English names
- **Constructor**: Accept Material entity and map to DTO fields
- **Helper Methods**: Include utility methods for:
  - `getFileExtension()` - Extract extension from URL
  - `isDocument()` - Check if PDF, DOC, DOCX, TXT, RTF, MD
  - `isImage()` - Check if JPG, PNG, GIF, BMP, SVG
  - `isVideo()` - Check if MP4, AVI, MOV, WMV, FLV, WEBM
  - `getMaterialType()` - Return "DOCUMENT", "IMAGE", "VIDEO", or "OTHER"
  - `hasValidUrl()` and `hasValidName()` - Validation helpers

### 2.2 Key Implementation Notes
- **English Field Names**: All DTO fields must use English names
- **Entity Mapping**: Constructor must call entity methods with English names (e.g., `material.getName()`)
- **Utility Methods**: Include comprehensive helper methods for client-side filtering

## 3. Repository Requirements

### 3.1 RepositorioMaterial (`src/main/java/app/repositorios/RepositorioMaterial.java`)
- **Extends**: `JpaRepository<Material, String>`
- **Required Imports**: 
  ```java
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  ```

### 3.2 Method Requirements
- **Basic CRUD**: Inherited from JpaRepository
- **Search Methods**: Use English field names
  - `findByName(String name)` (renamed from `findByNombre`)
  - `findByNameContainingIgnoreCase(String name, Pageable pageable)` (paginated)
  - `findByUrlContainingIgnoreCase(String url, Pageable pageable)` (paginated)
  - `findByNameContainingIgnoreCaseOrUrlContainingIgnoreCase(String name, String url, Pageable pageable)` (paginated)
- **Type-Specific Methods**:
  - `findDocuments(Pageable pageable)`
  - `findImages(Pageable pageable)`
  - `findVideos(Pageable pageable)`
  - `findByFileExtension(String extension, Pageable pageable)`
- **Count Methods**:
  - `countAllMaterials()`
  - `countDocuments()`
  - `countImages()`
  - `countVideos()`

### 3.3 Key Implementation Notes
- **English Method Names**: All repository methods must use English field names
- **Paginated Versions**: Include both `List` and `Page<Material>` versions for search methods
- **Missing Imports**: Ensure `Page` and `Pageable` imports are present

## 4. Service Requirements

### 4.1 ServicioMaterial (`src/main/java/app/servicios/ServicioMaterial.java`)
- **Annotations**: `@Service`, `@RequiredArgsConstructor`, `@Transactional`
- **Dependencies**: Inject `RepositorioMaterial`

### 4.2 Method Requirements
- **CRUD Operations**:
  - `obtenerMaterialPorId(String id)`
  - `crearMaterial(String name, String url)`
  - `actualizarMaterial(String id, String name, String url)`
  - `borrarMaterialPorId(String id)`
- **Search Operations** (use English parameter names):
  - `obtenerMaterialPorNombre(String name)` (renamed from `obtenerMaterialPorNombre`)
  - `obtenerMaterialesPorNombre(String name)`
  - `obtenerMaterialesPorUrl(String url)`
  - `obtenerMaterialesPorExtension(String extension)`
- **Type-Specific Operations**:
  - `obtenerDocumentos()`
  - `obtenerImagenes()`
  - `obtenerVideos()`
- **Pagination**: `obtenerMaterialesPaginados(String q, String name, String url, String type, int page, int size, String sortBy, String sortDirection)`
- **Statistics**: `contarMateriales()`, `contarDocumentos()`, `contarImagenes()`, `contarVideos()`

### 4.3 Key Implementation Notes
- **English Parameter Names**: All method parameters must use English names
- **Repository Calls**: Update all repository method calls to use English names
- **Validation**: Include proper input validation and error handling
- **Pagination Logic**: Handle all filter combinations in the main paginated method

## 5. REST API Requirements

### 5.1 MaterialRest (`src/main/java/app/rest/MaterialRest.java`)
- **Base Path**: `@RequestMapping("/api/material")` (singular, not plural)
- **Annotations**: `@RestController`, `@RequiredArgsConstructor`, `@CrossOrigin`, `@Validated`, `@Tag`
- **Extends**: `BaseRestController`

### 5.2 Endpoint Requirements (RESTful Only)
- **GET `/api/material`** - Paginated list with filters (q, name, url, type, page, size, sortBy, sortDirection)
- **GET `/api/material/{id}`** - Get specific material by ID
- **POST `/api/material`** - Create new material
- **PUT `/api/material/{id}`** - Update existing material
- **DELETE `/api/material/{id}`** - Delete material by ID
- **GET `/api/material/stats`** - Get material statistics

### 5.3 Security Requirements
- **Read Access**: `@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")`
- **Write Access**: `@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")`
- **Statistics**: `@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")`

### 5.4 Documentation Requirements
- **Swagger Annotations**: `@Operation`, `@ApiResponses`, `@Parameter`
- **Validation**: `@Size`, `@Min`, `@Max`, `@Pattern` constraints
- **Response Codes**: 200, 201, 204, 400, 403, 404, 409

### 5.5 Key Implementation Notes
- **RESTful Design**: Only include core REST operations, avoid verb-based endpoints
- **Filtering**: All filtering done through query parameters on main GET endpoint
- **No Verb Endpoints**: Remove endpoints like `/search/name`, `/type/documents`, etc.
- **Consistent Naming**: Use singular noun in base path (`/api/material` not `/api/materials`)

## 6. Common Issues and Fixes

### 6.1 Compilation Errors
- **Field Not Found**: Update all references to use English field names
- **Method Undefined**: Rename repository methods to use English field names
- **Missing Imports**: Add `Page` and `Pageable` imports to repositories
- **Parameter Mismatch**: Update service method parameters to use English names

### 6.2 Refactoring Steps
1. **Entity**: Rename fields to English, update constructor
2. **DTO**: Update record fields, constructor, and method references
3. **Repository**: Rename methods, add paginated versions, add missing imports
4. **Service**: Update method calls, parameter names, and setter calls
5. **REST**: Remove non-RESTful endpoints, keep only core CRUD operations

## 7. Testing Considerations

### 7.1 Unit Tests
- Test all CRUD operations
- Test pagination with various filters
- Test validation and error handling
- Test security annotations

### 7.2 Integration Tests
- Test complete request/response cycles
- Test filter combinations
- Test security access control

## 8. Validation Checklist

- [ ] All variables use English names
- [ ] All repository methods have paginated versions
- [ ] Service methods call correct repository methods
- [ ] REST API follows RESTful principles
- [ ] Security annotations are properly applied
- [ ] Swagger documentation is complete
- [ ] No compilation errors
- [ ] All filtering functionality preserved in main endpoint

## 9. Template for Ejercicio Development

Use this document as a template for developing the Ejercicio entity by:
1. Replacing "Material" with "Ejercicio" throughout
2. Adapting field names (e.g., `name` → `name`, `statement`, `startDate`, `endDate`, `classId`)
3. Adjusting helper methods based on Ejercicio-specific needs
4. Updating validation rules for Ejercicio fields
5. Modifying security requirements as needed for Ejercicio operations
