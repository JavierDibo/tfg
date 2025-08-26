# Test Failures Report

**Date:** 2025-08-26  
**Total Tests Run:** 529  
**Failures:** 99  
**Errors:** 21  
**Skipped:** 0  

## Summary

The test suite has significant issues across multiple test classes. The main problems are:

1. **Mockito Argument Matcher Issues** - Multiple tests have incorrect argument matcher usage
2. **NullPointerException in SecurityUtils** - Security utility injection problems
3. **HTTP Status Code Mismatches** - REST endpoints returning unexpected status codes
4. **JSON Path Issues** - Response structure doesn't match expected format
5. **Sorting Parameter Mismatches** - Tests expecting UNSORTED but getting specific sort orders

---

## 1. ServicioEjercicioTest - Mockito Argument Matcher Issues

**Class:** `app.servicios.ServicioEjercicioTest`  
**Issue:** Invalid use of argument matchers - mixing matchers with raw values

### Affected Methods:
- `testObtenerEjerciciosPaginadosConFiltrosCombinados` (line 245)
- `testObtenerEjerciciosPaginadosSinFiltros` (line 416)
- `testObtenerEjerciciosPaginadosConPaginacion` (line 505)
- `testObtenerEjerciciosPaginadosSoloConNombre` (line 331)
- `testObtenerEjerciciosPaginadosSoloConBusquedaGeneral` (line 288)
- `testObtenerEjerciciosPaginadosSoloConClassId` (line 374)
- `testObtenerEjerciciosPaginadosConFiltrosVacios` (line 458)

**Problem:** Tests are using argument matchers incorrectly. When using matchers like `any()`, all arguments must be provided by matchers.

**Fix Required:** Replace raw values with appropriate matchers (e.g., `eq("value")`)

---

## 2. ServicioMaterialTest - Multiple Issues

**Class:** `app.servicios.ServicioMaterialTest`  
**Issues:** Mock verification failures, assertion failures, unnecessary stubbing

### Affected Methods:

#### Mock Verification Failures:
- `testObtenerMaterialesPaginados_SoloConNombre_DebeFiltrarCorrectamente` (line 182)
- `testObtenerMaterialesPaginados_SinFiltros_DebeRetornarTodos` (line 267)
- `testObtenerMaterialesPaginados_ConPaginacion_DebeAplicarCorrectamente` (line 395)
- `testObtenerMaterialesPaginados_ConTipoEnMinusculas_DebeConvertirAMayusculas` (line 349)
- `testObtenerMaterialesPaginados_ConFiltrosVacios_DebeTratarComoNull` (line 308)
- `testObtenerMaterialesPaginados_SoloConBusquedaGeneral_DebeFiltrarCorrectamente` (line 140)
- `testObtenerMaterialesPaginados_SoloConTipo_DebeFiltrarCorrectamente` (line 224)

**Problem:** Tests expect `UNSORTED` but actual calls use specific sort orders (`name: ASC`, `name: DESC`)

#### Assertion Failures:
- `testObtenerMaterialesPaginados_ConFiltrosCombinados_DebeFiltrarCorrectamente` (line 93)
  - Expected: 20, Actual: 2

#### Unnecessary Stubbing Errors:
- `testObtenerMaterialesPaginados_ConTamañoInvalido_DebeLanzarExcepcion` (line 467)
- `testObtenerMaterialesPaginados_ConParametrosInvalidos_DebeLanzarExcepcion` (line 440)

---

## 3. ServicioAlumnoTest - SecurityUtils NullPointerException

**Class:** `app.servicios.ServicioAlumnoTest`  
**Issue:** SecurityUtils is null, causing NullPointerException

### Affected Methods:
- `testCrearAlumno` (line 165)
- `testObtenerAlumnoPorIdExiste` (line 91)
- `testObtenerAlumnoPorEmailExiste` (line 113)
- `testObtenerAlumnoPorUsuarioExiste` (line 135)
- `testObtenerAlumnoPorDniExiste` (line 146)
- `testActualizarAlumno` (line 221)
- `testCambiarEstadoMatricula` (line 253)
- `testHabilitarDeshabilitarAlumno` (line 270)
- `testBorrarAlumnoPorId` (line 283)

**Problem:** SecurityUtils dependency is not properly injected in test setup

**Additional Issues:**
- `testCrearAlumnoEmailExiste` - Expected ValidationException, got NullPointerException
- `testCrearAlumnoUsuarioExiste` - Expected ValidationException, got NullPointerException

---

## 4. ServicioClaseTest - Multiple Issues

**Class:** `app.servicios.ServicioClaseTest`  
**Issues:** Mock verification failures, NullPointerException, assertion failures

### Affected Methods:

#### Mock Verification Failures:
- `testBuscarClasesConFiltrosCombinados` (line 587)
- `testBuscarClasesConPaginacion` (line 706)
- `testBuscarClasesSegunRolParaAdmin` (line 758)
- `testBuscarClasesSegunRolParaAlumno` (line 811)
- `testBuscarClasesSoloConBusquedaGeneral` (line 615)
- `testBuscarClasesSoloConFiltrosEspecificos` (line 647)

**Problem:** Tests expect `UNSORTED` but actual calls use specific sort orders (`titulo: ASC`, `titulo: DESC`)

#### NullPointerException:
- `testBuscarClases` (line 283)
- `testBuscarClasesConFiltrosVacios` (line 724)

**Problem:** `resultado` is null when trying to call `map()` method

#### Other Issues:
- `testCrearCursoActualizaProfesorClases` (line 484) - Mock not invoked
- `testDTOProfesorPublicoClassCountCorrecto` (line 505) - Expected: 1, Actual: 0

---

## 5. REST Controller Tests - HTTP Status Code Issues

### AlumnoRestTest
**Class:** `app.rest.AlumnoRestTest`  
**Issues:** Mock verification failures

**Affected Methods:**
- `testObtenerAlumnosPaginadosConParametroQYFiltrosEspecificos` (line 170)
- `testObtenerAlumnosPaginadosSinParametroQ` (line 217)

**Problem:** Custom argument matcher not matching actual calls

### AutenticacionRestTest
**Class:** `app.rest.AutenticacionRestTest`  
**Issues:** HTTP status code mismatches

**Affected Methods:**
- `testLoginCredencialesIncorrectas` - Expected: 401, Actual: 500
- `testLoginJsonMalformado` - Expected: 400, Actual: 500
- `testLoginSinContentType` - Expected: 415, Actual: 500
- `testRegistro` - Expected: 200, Actual: 400
- `testRegistroEmailExistente` - Expected: 409, Actual: 400
- `testRegistroJsonMalformado` - Expected: 400, Actual: 500
- `testRegistroSinContentType` - Expected: 415, Actual: 500
- `testRegistroUsuarioExistente` - Expected: 409, Actual: 400

### ClaseManagementRestTest
**Class:** `app.rest.ClaseManagementRestTest`  
**Issues:** All tests returning 404 instead of expected status codes

**Affected Methods:**
- `testAgregarAlumno` - Expected: 200, Actual: 404
- `testAgregarEjercicio` - Expected: 200, Actual: 404
- `testAgregarMaterial` - Expected: 200, Actual: 404
- `testAgregarProfesor` - Expected: 200, Actual: 404
- `testRemoverAlumno` - Expected: 200, Actual: 404
- `testRemoverEjercicio` - Expected: 200, Actual: 404
- `testRemoverMaterial` - Expected: 200, Actual: 404
- `testRemoverProfesor` - Expected: 200, Actual: 404
- And several validation tests

### ClaseRestIntegrationTest
**Class:** `app.rest.ClaseRestIntegrationTest`  
**Issues:** Multiple status code and response format issues

**Affected Methods:**
- `testObtenerClases` - JSON structure mismatch (expecting array, got object)
- `testBorrarClasePorIdConRolAdmin` - Expected: 204, Actual: 500
- `testBorrarClasePorIdConRolProfesor` - Expected: 403, Actual: 500
- `testBuscarClasesConParametros` - Expected: 200, Actual: 500
- `testDarseDeBajaDeClase` - Expected: 201, Actual: 403
- `testInscribirseEnClase` - Expected: 201, Actual: 403
- `testObtenerClasesPorAlumno` - Expected: 201, Actual: 403
- `testObtenerClasesPorProfesor` - Expected: 201, Actual: 403
- And several other management tests returning 404

### EjercicioRestTest
**Class:** `app.rest.EjercicioRestTest`  
**Issues:** JSON path issues and status code mismatches

**Affected Methods:**
- `testCrearEjercicio_Success` - No value at JSON path "$.id"
- `testActualizarEjercicioParcial_Success` - No value at JSON path "$.id"
- `testObtenerEjercicioPorId_Success` - No value at JSON path "$.id"
- `testReemplazarEjercicio_Success` - No value at JSON path "$.id"
- `testObtenerEjercicios_Success` - No value at JSON path "$.contenido"
- `testObtenerEjercicios_EmptyResult` - No value at JSON path "$.contenido"
- `testObtenerEjercicios_WithFilters` - No value at JSON path "$.contenido.length()"
- `testCrearEjercicio_InvalidInput` - Expected: 400, Actual: 201
- `testCrearEjercicio_UnauthorizedRole` - Expected: 403, Actual: 201
- `testEliminarEjercicio_NotFound` - Expected: 404, Actual: 204
- `testEliminarEjercicio_UnauthorizedRole` - Expected: 403, Actual: 204
- `testObtenerEjercicioPorId_InvalidId` - Expected: 400, Actual: 200
- `testObtenerEjercicioPorId_NotFound` - Expected: 404, Actual: 200
- `testObtenerEjercicios_InvalidPaginationParameters` - Expected: 400, Actual: 200
- `testReemplazarEjercicio_UnauthorizedRole` - Expected: 403, Actual: 200
- `testEliminarEjercicio_Success` - Mock not invoked

### EntregaEjercicioRestTest
**Class:** `app.rest.EntregaEjercicioRestTest`  
**Issues:** JSON path issues and status code mismatches

**Affected Methods:**
- `testCrearEntrega_InvalidInput` - Expected: 400, Actual: 201
- `testObtenerEntregaPorId_InvalidId` - Expected: 400, Actual: 200
- `testObtenerEntregasPaginadas_Success` - No value at JSON path "$.contenido"
- `testObtenerEntregasPaginadas_EmptyResult` - No value at JSON path "$.contenido"
- `testObtenerEntregasPaginadas_StudentSeesOnlyOwnDeliveries` - No value at JSON path "$.contenido.length()"
- `testObtenerEntregasPaginadas_WithFilters` - No value at JSON path "$.contenido.length()"
- `testReemplazarEntrega_InvalidGradeRange` - Unnecessary stubbing

### MaterialRestTest
**Class:** `app.rest.MaterialRestTest`  
**Issues:** Multiple status code and JSON path issues

**Affected Methods:**
- `testCrearMaterial_InvalidInput` - Expected: 400, Actual: 201
- `testCrearMaterial_UnauthorizedRole` - Expected: 403, Actual: 201
- `testActualizarMaterial_InvalidInput` - Expected: 400, Actual: 200
- `testActualizarMaterial_UnauthorizedRole` - Expected: 403, Actual: 200
- `testEliminarMaterial_InvalidId` - Expected: 400, Actual: 204
- `testEliminarMaterial_UnauthorizedRole` - Expected: 403, Actual: 204
- `testObtenerMaterialPorId_InvalidId` - Expected: 400, Actual: 200
- `testObtenerMateriales_Success` - Expected: 200, Actual: 500
- `testObtenerMateriales_EmptyResult` - Expected: 200, Actual: 500
- `testObtenerMateriales_WithFilters` - No value at JSON path "$.contenido.length()"
- `testObtenerMateriales_InvalidPaginationParameters` - Expected: 400, Actual: 200
- `testObtenerMateriales_ExceedMaxPageSize` - Expected: 400, Actual: 200
- `testObtenerMateriales_InvalidSortDirection` - Expected: 400, Actual: 200
- `testObtenerMateriales_MaxPageSize` - Expected: 200, Actual: 500
- `testObtenerMateriales_AdminAccess` - Expected: 200, Actual: 500
- `testObtenerMateriales_ProfesorAccess` - Expected: 200, Actual: 500
- `testObtenerMateriales_AlumnoAccess` - Expected: 200, Actual: 500
- `testObtenerMateriales_ValidSortDirection` - Expected: 200, Actual: 500

### ProfesorRestTest
**Class:** `app.rest.ProfesorRestTest`  
**Issues:** Status code mismatch

**Affected Methods:**
- `testCrearProfesor` - Expected: 200, Actual: 400

---

## 6. Other Issues

### EntregaEjercicioRestTest
- `testReemplazarEntrega_InvalidGradeRange` - Unnecessary stubbing detected

---

## Recommendations

### Immediate Fixes Required:

1. **Fix Mockito Argument Matchers**
   - Review all tests using `any()` and ensure all arguments use matchers
   - Replace raw values with `eq()` matchers where appropriate

2. **Fix SecurityUtils Injection**
   - Ensure SecurityUtils is properly mocked in ServicioAlumnoTest
   - Add `@Mock` annotation and proper setup

3. **Fix Sorting Parameter Expectations**
   - Update tests to expect actual sort orders instead of UNSORTED
   - Or modify service methods to use UNSORTED when no sort is specified

4. **Fix REST Endpoint Status Codes**
   - Review authentication and authorization logic
   - Ensure proper exception handling returns correct HTTP status codes
   - Fix endpoint mappings that are returning 404

5. **Fix JSON Response Structure**
   - Ensure REST endpoints return expected JSON structure
   - Fix pagination response format issues

6. **Remove Unnecessary Stubbings**
   - Clean up unused mock configurations
   - Use `@MockitoSettings(strictness = Strictness.LENIENT)` where appropriate

### Priority Order:
1. SecurityUtils injection issues (blocking multiple tests)
2. Mockito argument matcher issues (blocking service tests)
3. REST endpoint 404 issues (blocking integration tests)
4. Status code mismatches
5. JSON structure issues
6. Unnecessary stubbing warnings
