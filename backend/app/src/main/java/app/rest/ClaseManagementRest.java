package app.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOClase;
import app.dtos.DTOClaseConDetalles;
import app.dtos.DTOEstadoInscripcion;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTORespuestaEnrollment;
import app.entidades.Material;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for class element management and enrollment operations
 * Handles management operations like adding/removing professors, exercises, materials, and student enrollments
 */
@RestController
@RequestMapping("/api/clases/{claseId}")
@RequiredArgsConstructor
@Validated
@Tag(name = "Class Management", description = "API for class element management and enrollment operations")
public class ClaseManagementRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== STUDENT ENROLLMENT MANAGEMENT =====

    /**
     * Enrolls a student in a class (for professors and administrators)
     */
    @PostMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Enroll student in class", description = "Enrolls a student in a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
        @ApiResponse(responseCode = "400", description = "Enrollment error - Invalid data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnClase(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {
        
        DTOPeticionEnrollment peticion = new DTOPeticionEnrollment(studentId, claseId);
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
    @DeleteMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Unenroll student from class", description = "Unenrolls a student from a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student unenrolled successfully"),
        @ApiResponse(responseCode = "400", description = "Unenrollment error - Invalid data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeClase(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {
        
        DTOPeticionEnrollment peticion = new DTOPeticionEnrollment(studentId, claseId);
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Gets the enrollment status of a student in a specific class
     */
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Get student enrollment in class", description = "Gets the enrollment information of a student in a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrollment information retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTOEstadoInscripcion> obtenerInscripcionEstudiante(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {
        
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(studentId, claseId);
        return ResponseEntity.ok(estado);
    }

    /**
     * Gets detailed information about a class for a specific student
     */
    @GetMapping("/students/{studentId}/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Get class details for student", description = "Gets detailed information about a class for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Class details retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTOClaseConDetalles> obtenerDetallesClaseParaEstudiante(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {
        
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, studentId);
        return ResponseEntity.ok(detalles);
    }

    // ===== SELF-ENROLLMENT OPERATIONS (for students) =====

    /**
     * Allows a student to enroll themselves in a class
     */
    @PostMapping("/students/me")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Self-enrollment in class", description = "Allows a student to enroll themselves in a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Self-enrollment successful"),
        @ApiResponse(responseCode = "400", description = "Enrollment error - Invalid data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ALUMNO permissions"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
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
    @DeleteMapping("/students/me")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Self-unenrollment from class", description = "Allows a student to unenroll themselves from a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Self-unenrollment successful"),
        @ApiResponse(responseCode = "400", description = "Unenrollment error - Invalid data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ALUMNO permissions"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<DTORespuestaEnrollment> darseDeBajaDeClase(@PathVariable Long claseId) {
        DTORespuestaEnrollment respuesta = servicioClase.darseDeBajaDeClase(claseId);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Gets the enrollment status of the authenticated student in a class
     */
    @GetMapping("/students/me")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Get my enrollment in class", description = "Gets the enrollment information of the authenticated student in a class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrollment information retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ALUMNO permissions"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<DTOEstadoInscripcion> obtenerMiInscripcion(@PathVariable Long claseId) {
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(alumnoId, claseId);
        return ResponseEntity.ok(estado);
    }

    /**
     * Gets detailed information about a class for the authenticated student
     */
    @GetMapping("/students/me/details")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Get class details for me", description = "Gets detailed information about a class for the authenticated student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Class details retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ALUMNO permissions"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<DTOClaseConDetalles> obtenerMisDetallesClase(@PathVariable Long claseId) {
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, alumnoId);
        return ResponseEntity.ok(detalles);
    }

    // ===== PROFESSOR MANAGEMENT =====

    /**
     * Adds a professor to a class
     */
    @PostMapping("/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add professor to class", description = "Adds a professor to a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Professor added successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN permissions"),
        @ApiResponse(responseCode = "404", description = "Class or professor not found")
    })
    public ResponseEntity<DTOClase> agregarProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.agregarProfesor(claseId, profesorId));
    }

    /**
     * Removes a professor from a class
     */
    @DeleteMapping("/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove professor from class", description = "Removes a professor from a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Professor removed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN permissions"),
        @ApiResponse(responseCode = "404", description = "Class or professor not found")
    })
    public ResponseEntity<DTOClase> removerProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.removerProfesor(claseId, profesorId));
    }

    // ===== EXERCISE MANAGEMENT =====

    /**
     * Adds an exercise to a class
     */
    @PostMapping("/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Add exercise to class", description = "Adds an exercise to a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exercise added successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or exercise not found")
    })
    public ResponseEntity<DTOClase> agregarEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.agregarEjercicio(claseId, ejercicioId));
    }

    /**
     * Removes an exercise from a class
     */
    @DeleteMapping("/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Remove exercise from class", description = "Removes an exercise from a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exercise removed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or exercise not found")
    })
    public ResponseEntity<DTOClase> removerEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.removerEjercicio(claseId, ejercicioId));
    }

    // ===== MATERIAL MANAGEMENT =====

    /**
     * Adds material to a class
     */
    @PostMapping("/material")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Add material to class", description = "Adds material to a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid material data"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<DTOClase> agregarMaterial(
            @PathVariable Long claseId,
            @Valid @RequestBody Material material) {
        return ResponseEntity.ok(servicioClase.agregarMaterial(claseId, material));
    }

    /**
     * Removes material from a class
     */
    @DeleteMapping("/material/{materialId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Remove material from class", description = "Removes material from a specific class")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material removed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "404", description = "Class or material not found")
    })
    public ResponseEntity<DTOClase> removerMaterial(
            @PathVariable Long claseId,
            @PathVariable String materialId) {
        return ResponseEntity.ok(servicioClase.removerMaterial(claseId, materialId));
    }
}
