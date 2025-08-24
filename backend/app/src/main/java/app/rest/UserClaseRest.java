package app.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOAlumnoPublico;
import app.dtos.DTOClase;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTORespuestaAlumnosClase;
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
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para operaciones específicas del usuario autenticado
 * Maneja operaciones como "mis clases", información de estudiantes, etc.
 */
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Tag(name = "Operaciones de Usuario", description = "API para operaciones específicas del usuario autenticado")
public class UserClaseRest {

    private final ServicioClase servicioClase;
    private final ServicioAlumno servicioAlumno;
    private final SecurityUtils securityUtils;

    // ===== OPERACIONES PARA PROFESORES =====

    /**
     * Obtiene las clases del profesor autenticado
     */
    @GetMapping("/classes")
    @PreAuthorize("hasRole('PROFESOR')")
    @Operation(summary = "Obtener mis clases", description = "Obtiene las clases del profesor autenticado")
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
    public ResponseEntity<List<DTOClase>> obtenerMisClases() {
        String profesorId = securityUtils.getCurrentUserId().toString();
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }

    // ===== OPERACIONES PARA ALUMNOS =====

    /**
     * Obtiene las clases en las que está inscrito el estudiante autenticado
     */
    @GetMapping("/enrolled-classes")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Obtener mis clases inscritas", description = "Obtiene las clases en las que está inscrito el estudiante autenticado")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases inscritas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClaseInscrita.class, type = "array")
            )
        ),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<DTOClaseInscrita>> obtenerMisClasesInscritas() {
        Long alumnoId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(servicioClase.obtenerClasesInscritasConDetalles(alumnoId));
    }

    // ===== OPERACIONES DE CONSULTA DE ALUMNOS =====

    /**
     * Obtiene la lista de alumnos inscritos en una clase con paginación
     */
    @GetMapping("/classes/{claseId}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Obtener alumnos de una clase",
        description = "Obtiene la lista de alumnos inscritos en una clase específica. " +
                     "El nivel de detalle de la información depende del rol del usuario: " +
                     "- ADMIN: información completa de todos los alumnos " +
                     "- PROFESOR: información completa si es profesor de la clase, información pública si no " +
                     "- ALUMNO: solo información pública de los alumnos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de alumnos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaAlumnosClase.class)
            )
        ),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Clase no encontrada")
    })
    public ResponseEntity<DTORespuestaAlumnosClase> obtenerAlumnosDeClase(
            @Parameter(description = "ID de la clase", example = "1")
            @PathVariable Long claseId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el que ordenar", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenación (ASC/DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        // Obtener información del usuario autenticado
        String userRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(
            claseId, page, size, sortBy, sortDirection, userRole, currentUserId));
    }

    /**
     * Obtiene información pública de los alumnos de una clase
     */
    @GetMapping("/classes/{claseId}/students/public")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Obtener información pública de alumnos de una clase",
        description = "Obtiene la lista de alumnos inscritos en una clase con solo información pública (nombre y apellidos). " +
                     "Este endpoint siempre devuelve información pública independientemente del rol del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de alumnos públicos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumnoPublico.class, type = "array")
            )
        ),
        @ApiResponse(responseCode = "403", description = "No autorizado"),
        @ApiResponse(responseCode = "404", description = "Clase no encontrada")
    })
    public ResponseEntity<List<DTOAlumnoPublico>> obtenerAlumnosPublicosDeClase(
            @Parameter(description = "ID de la clase", example = "1")
            @PathVariable Long claseId) {
        
        List<DTOAlumnoPublico> alumnosPublicos = servicioClase.obtenerAlumnosPublicosDeClase(claseId);
        return ResponseEntity.ok(alumnosPublicos);
    }
}
