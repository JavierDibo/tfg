package app.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for user-specific operations
 * Handles operations like "my classes", student information, etc.
 */
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Operations", description = "API for user-specific operations")
public class UserClaseRest extends BaseRestController {

    private final ServicioClase servicioClase;
    private final ServicioAlumno servicioAlumno;
    private final SecurityUtils securityUtils;

    // ===== OPERATIONS FOR PROFESSORS =====

    /**
     * Gets the classes of the authenticated professor
     */
    @GetMapping("/classes")
    @PreAuthorize("hasRole('PROFESOR')")
    @Operation(summary = "Get my classes", description = "Gets the classes of the authenticated professor")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class, type = "array")
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires PROFESOR permissions")
    })
    public ResponseEntity<List<DTOClase>> obtenerMisClases() {
        String profesorId = securityUtils.getCurrentUserId().toString();
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }

    // ===== OPERATIONS FOR STUDENTS =====

    /**
     * Gets the classes in which the authenticated student is enrolled
     */
    @GetMapping("/enrollments")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(summary = "Get my enrolled classes", description = "Gets the classes in which the authenticated student is enrolled")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of enrolled classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClaseInscrita.class, type = "array")
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ALUMNO permissions")
    })
    public ResponseEntity<List<DTOClaseInscrita>> obtenerMisClasesInscritas() {
        Long alumnoId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(servicioClase.obtenerClasesInscritasConDetalles(alumnoId));
    }

    // ===== STUDENT CONSULTATION OPERATIONS =====

    /**
     * Gets the list of students enrolled in a class with pagination
     */
    @GetMapping("/classes/{claseId}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get students in a class",
        description = "Gets the list of students enrolled in a specific class. " +
                     "The level of detail of the information depends on the user's role: " +
                     "- ADMIN: complete information of all students " +
                     "- PROFESOR: complete information if they are the professor of the class, public information if not " +
                     "- ALUMNO: only public information of the students"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of students retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaAlumnosClase.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<DTORespuestaAlumnosClase> obtenerAlumnosDeClase(
            @Parameter(description = "Class ID", example = "1")
            @PathVariable Long claseId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC") String sortDirection) {
        
        // Validate and normalize parameters
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "firstName", "lastName", "email");
        sortDirection = validateSortDirection(sortDirection);
        
        // Get authenticated user information
        String userRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(
            claseId, page, size, sortBy, sortDirection, userRole, currentUserId));
    }

    /**
     * Gets public information of students in a class
     */
    @GetMapping("/classes/{claseId}/students/public")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get public information of students in a class",
        description = "Gets the list of students enrolled in a class with only public information (first name and last name). " +
                     "This endpoint always returns public information regardless of the user's role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of public students retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumnoPublico.class, type = "array")
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<List<DTOAlumnoPublico>> obtenerAlumnosPublicosDeClase(
            @Parameter(description = "Class ID", example = "1")
            @PathVariable Long claseId) {
        
        List<DTOAlumnoPublico> alumnosPublicos = servicioClase.obtenerAlumnosPublicosDeClase(claseId);
        return ResponseEntity.ok(alumnosPublicos);
    }
}
