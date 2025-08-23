package app.rest;

import app.dtos.*;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para gestión de inscripciones y matrículas
 * Maneja todas las operaciones relacionadas con la inscripción de alumnos en clases
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "API para gestión de inscripciones y matrículas")
public class EnrollmentRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== OPERACIONES DE INSCRIPCIÓN PARA PROFESORES/ADMIN =====

    /**
     * Inscribe un alumno en una clase (para profesores y administradores)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Inscribir alumno en clase", description = "Permite a profesores y administradores inscribir alumnos en clases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inscripción exitosa"),
        @ApiResponse(responseCode = "400", description = "Error en la inscripción"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnClase(
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        DTORespuestaEnrollment respuesta = servicioClase.inscribirAlumnoEnClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Da de baja un alumno de una clase (para profesores y administradores)
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Dar de baja alumno de clase", description = "Permite a profesores y administradores dar de baja alumnos de clases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Baja exitosa"),
        @ApiResponse(responseCode = "400", description = "Error en la baja"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeClase(
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    // ===== OPERACIONES DE INSCRIPCIÓN PARA ALUMNOS =====

    /**
     * Permite a un alumno inscribirse en una clase
     */
    @PostMapping("/{claseId}/self-enroll")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Auto-inscripción en clase", description = "Permite a un alumno inscribirse en una clase")
    public ResponseEntity<DTOClase> inscribirseEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.inscribirseEnClase(claseId));
    }

    /**
     * Permite a un alumno darse de baja de una clase
     */
    @DeleteMapping("/{claseId}/self-unenroll")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Auto-baja de clase", description = "Permite a un alumno darse de baja de una clase")
    public ResponseEntity<DTOClase> darseDeBajaDeClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.darseDeBajaDeClase(claseId));
    }

    // ===== OPERACIONES DE CONSULTA DE ESTADO =====

    /**
     * Verifica el estado de inscripción de un estudiante en una clase específica
     */
    @GetMapping("/{claseId}/status/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Verificar estado de inscripción", description = "Verifica si un estudiante está inscrito en una clase específica")
    public ResponseEntity<DTOEstadoInscripcion> verificarEstadoInscripcion(
            @PathVariable Long claseId,
            @PathVariable Long alumnoId) {
        
        // Para alumnos, solo permitir acceder a su propia información a través del endpoint /my-status
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(alumnoId, claseId);
        return ResponseEntity.ok(estado);
    }

    /**
     * Verifica el estado de inscripción del estudiante autenticado en una clase
     */
    @GetMapping("/{claseId}/my-status")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Verificar mi estado de inscripción", description = "Verifica si el estudiante autenticado está inscrito en una clase")
    public ResponseEntity<DTOEstadoInscripcion> verificarMiEstadoInscripcion(
            @PathVariable Long claseId) {
        
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(alumnoId, claseId);
        return ResponseEntity.ok(estado);
    }

    // ===== OPERACIONES DE CONSULTA DE CLASES =====

    /**
     * Obtiene las clases en las que está inscrito el estudiante autenticado
     */
    @GetMapping("/my-enrolled-classes")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Obtener mis clases inscritas", description = "Obtiene las clases en las que está inscrito el estudiante autenticado")
    public ResponseEntity<List<DTOClaseInscrita>> obtenerMisClasesInscritas() {
        Long alumnoId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(servicioClase.obtenerClasesInscritasConDetalles(alumnoId));
    }

    /**
     * Obtiene información detallada de una clase para un estudiante específico
     */
    @GetMapping("/{claseId}/details-for-student/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Obtener detalles de clase para estudiante", description = "Obtiene información detallada de una clase para un estudiante específico")
    public ResponseEntity<DTOClaseConDetalles> obtenerClaseConDetallesParaEstudiante(
            @PathVariable Long claseId,
            @PathVariable Long alumnoId) {
        
        // Para alumnos, usar el endpoint /details-for-me
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, alumnoId);
        return ResponseEntity.ok(detalles);
    }

    /**
     * Obtiene información detallada de una clase para el estudiante autenticado
     */
    @GetMapping("/{claseId}/details-for-me")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Obtener detalles de clase para mí", description = "Obtiene información detallada de una clase para el estudiante autenticado")
    public ResponseEntity<DTOClaseConDetalles> obtenerClaseConDetallesParaMi(
            @PathVariable Long claseId) {
        
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, alumnoId);
        return ResponseEntity.ok(detalles);
    }
}
