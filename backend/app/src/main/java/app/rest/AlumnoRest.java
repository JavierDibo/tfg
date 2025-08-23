package app.rest;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPerfilAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.dtos.DTORespuestaPaginada;

import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Alumnos", description = "API para gestión de alumnos")
public class AlumnoRest {

    private final ServicioAlumno servicioAlumno;
    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // Endpoint con paginación (recomendado)
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Obtener alumnos paginados",
        description = "Obtiene una lista paginada de alumnos con filtros opcionales (requiere rol ADMIN o PROFESOR)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista paginada de alumnos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN o PROFESOR"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnosPaginados(
            @Parameter(description = "Término de búsqueda general (busca en nombre, apellidos, DNI, email)", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Nombre del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String nombre,
            @Parameter(description = "Apellidos del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @Parameter(description = "DNI del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 20) String dni,
            @Parameter(description = "Email del alumno para filtrar", required = false)
            @RequestParam(required = false) String email,
            @Parameter(description = "Estado de matriculación para filtrar", required = false)
            @RequestParam(required = false) Boolean matriculado,
            @Parameter(description = "Número de página (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página", required = false)
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Campo por el que ordenar", required = false)
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenación (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
            q, nombre, apellidos, dni, email, matriculado);
        
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.buscarAlumnosPorParametrosPaginados(
            parametros, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Endpoint para que los profesores obtengan todos los alumnos disponibles para inscribir
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return Lista paginada de todos los alumnos disponibles
     */
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Obtener alumnos disponibles",
        description = "Obtiene una lista paginada de alumnos habilitados y matriculados para inscripción (requiere rol ADMIN o PROFESOR)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista paginada de alumnos disponibles obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN o PROFESOR"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnosDisponibles(
            @Parameter(description = "Número de página (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página", required = false)
            @RequestParam(defaultValue = "50") @Min(1) int size,
            @Parameter(description = "Campo por el que ordenar", required = false)
            @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenación (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        // Obtener todos los alumnos habilitados y matriculados
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.obtenerAlumnosDisponiblesPaginados(
            page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Endpoint sin paginación (mantenido por compatibilidad - DEPRECATED)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Deprecated(since = "1.1", forRemoval = true)
    @Operation(
        summary = "Obtener alumnos (DEPRECATED)",
        description = "Obtiene una lista de alumnos con filtros opcionales sin paginación (DEPRECATED - usar /paged)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de alumnos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN o PROFESOR"
        )
    })
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnos(
            @Parameter(description = "Término de búsqueda general (busca en nombre, apellidos, DNI, email)", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Nombre del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String nombre,
            @Parameter(description = "Apellidos del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @Parameter(description = "DNI del alumno para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 20) String dni,
            @Parameter(description = "Email del alumno para filtrar", required = false)
            @RequestParam(required = false) String email,
            @Parameter(description = "Estado de matriculación para filtrar", required = false)
            @RequestParam(required = false) Boolean matriculado) {
        
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
            q, nombre, apellidos, dni, email, matriculado);
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.buscarAlumnosPorParametros(parametros);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    @Operation(
        summary = "Obtener alumno por ID",
        description = "Obtiene un alumno específico por su ID. Los alumnos solo pueden ver su propio perfil."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Alumno encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Alumno no encontrado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - No autorizado para ver este alumno"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorId(
            @Parameter(description = "ID del alumno", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuario}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Obtener alumno por usuario",
        description = "Obtiene un alumno específico por su nombre de usuario. Para alumnos, usar el endpoint /mi-perfil."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Alumno encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Alumno no encontrado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - No autorizado para ver este alumno"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorUsuario(
            @Parameter(description = "Nombre de usuario del alumno", required = true)
            @PathVariable @Size(max = 50) String usuario) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorUsuario(usuario);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener alumno por email",
        description = "Obtiene un alumno específico por su email (requiere rol ADMIN)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Alumno encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Alumno no encontrado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorEmail(
            @Parameter(description = "Email del alumno", required = true)
            @PathVariable String email) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorEmail(email);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener alumno por DNI",
        description = "Obtiene un alumno específico por su DNI (requiere rol ADMIN)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Alumno encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Alumno no encontrado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorDni(
            @Parameter(description = "DNI del alumno", required = true)
            @PathVariable @Size(max = 20) String dni) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorDni(dni);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/matriculados/paged")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Obtener alumnos matriculados paginados",
        description = "Obtiene una lista paginada de alumnos matriculados (requiere rol ADMIN o PROFESOR)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista paginada de alumnos matriculados obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN o PROFESOR"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnosMatriculadosPaginados(
            @Parameter(description = "Número de página (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página", required = false)
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Campo por el que ordenar", required = false)
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenación (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(
            true, page, size, sortBy, sortDirection);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @GetMapping("/no-matriculados/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnosNoMatriculadosPaginados(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(
            false, page, size, sortBy, sortDirection);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @GetMapping("/matriculados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Deprecated(since = "1.1", forRemoval = true)
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnosMatriculados() {
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.obtenerAlumnosPorMatriculado(true);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @GetMapping("/no-matriculados")
    @PreAuthorize("hasRole('ADMIN')")
    @Deprecated(since = "1.1", forRemoval = true)
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnosNoMatriculados() {
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.obtenerAlumnosPorMatriculado(false);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> crearAlumno(
            @Valid @RequestBody DTOPeticionRegistroAlumno peticion) {
        
        DTOAlumno dtoAlumnoNuevo = servicioAlumno.crearAlumno(peticion);
        return new ResponseEntity<>(dtoAlumnoNuevo, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    public ResponseEntity<DTOAlumno> actualizarAlumno(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id, 
            @Valid @RequestBody DTOActualizacionAlumno dtoParcial) {
        
        DTOAlumno dtoActualizado = servicioAlumno.actualizarAlumno(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @PatchMapping("/{id}/matricula")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> cambiarEstadoMatricula(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean matriculado = request.get("matriculado");
        if (matriculado == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOAlumno dtoActualizado = servicioAlumno.cambiarEstadoMatricula(id, matriculado);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @PatchMapping("/{id}/habilitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> habilitarDeshabilitarAlumno(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean habilitar = request.get("habilitar");
        if (habilitar == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOAlumno dtoActualizado = servicioAlumno.habilitarDeshabilitarAlumno(id, habilitar);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> borrarAlumnoPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.borrarAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    // Endpoints de estadísticas para administradores
    @GetMapping("/estadisticas/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerTotalAlumnos() {
        long total = servicioAlumno.contarTotalAlumnos();
        return new ResponseEntity<>(Map.of("total", total), HttpStatus.OK);
    }

    @GetMapping("/estadisticas/matriculas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasMatriculas() {
        Map<String, Long> estadisticas = Map.of(
            "matriculados", servicioAlumno.contarAlumnosMatriculados(),
            "no_matriculados", servicioAlumno.contarAlumnosNoMatriculados()
        );
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    // ===== NUEVOS ENDPOINTS PARA ESTUDIANTES =====

    /**
     * Obtiene las clases en las que está inscrito un estudiante con información detallada del profesor
     * GET /api/alumnos/{alumnoId}/clases
     * @param alumnoId ID del estudiante
     * @return Lista de DTOClaseInscrita con información del profesor
     */
    @GetMapping("/{alumnoId}/clases")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #alumnoId == authentication.principal.id)")
    public ResponseEntity<List<DTOClaseInscrita>> obtenerClasesInscritasConDetalles(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long alumnoId) {
        
        List<DTOClaseInscrita> clases = servicioClase.obtenerClasesInscritasConDetalles(alumnoId);
        return new ResponseEntity<>(clases, HttpStatus.OK);
    }

    /**
     * Obtiene el perfil del estudiante autenticado (sin información sensible)
     * GET /api/alumnos/mi-perfil
     * @return DTOPerfilAlumno del estudiante actual
     */
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DTOPerfilAlumno> obtenerMiPerfil() {
        String usuario = securityUtils.getCurrentUsername();
        DTOPerfilAlumno perfil = servicioAlumno.obtenerPerfilAlumnoPorUsuario(usuario);
        return new ResponseEntity<>(perfil, HttpStatus.OK);
    }
}
