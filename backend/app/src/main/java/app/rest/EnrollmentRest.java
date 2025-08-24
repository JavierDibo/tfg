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
 * REST controller for enrollment and registration management
 * Handles all operations related to student enrollment in classes
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "API for enrollment and registration management")
public class EnrollmentRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== ENROLLMENT OPERATIONS FOR PROFESSORS/ADMIN =====

    /**
     * Enrolls a student in a class (for professors and administrators)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Enroll student in class", description = "Allows professors and administrators to enroll students in classes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrollment successful"),
        @ApiResponse(responseCode = "400", description = "Enrollment error"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
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
     * Unenrolls a student from a class (for professors and administrators)
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Unenroll student from class", description = "Allows professors and administrators to unenroll students from classes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unenrollment successful"),
        @ApiResponse(responseCode = "400", description = "Unenrollment error"),
        @ApiResponse(responseCode = "403", description = "Not authorized")
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

    // ===== ENROLLMENT OPERATIONS FOR STUDENTS =====

    /**
     * Allows a student to enroll themselves in a class
     */
    @PostMapping("/{claseId}/self-enroll")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Self-enrollment in class", description = "Allows a student to enroll themselves in a class")
    public ResponseEntity<DTORespuestaEnrollment> inscribirseEnClase(@PathVariable Long claseId) {
        DTORespuestaEnrollment respuesta = servicioClase.inscribirseEnClase(claseId);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Allows a student to unenroll themselves from a class
     */
    @DeleteMapping("/{claseId}/self-unenroll")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Self-unenrollment from class", description = "Allows a student to unenroll themselves from a class")
    public ResponseEntity<DTORespuestaEnrollment> darseDeBajaDeClase(@PathVariable Long claseId) {
        DTORespuestaEnrollment respuesta = servicioClase.darseDeBajaDeClase(claseId);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
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
