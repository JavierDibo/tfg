package app.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOActualizacionProfesor;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOProfesor;
import app.dtos.DTORespuestaEnrollment;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioClase;
import app.servicios.ServicioProfesor;
import app.util.SecurityUtils;
import app.validation.ValidEmail;
import app.validation.ValidDNI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profesores")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Professors", description = "API for professor management")
public class ProfesorRest extends BaseRestController {

    private final ServicioProfesor servicioProfesor;
    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get paginated professors",
        description = "Gets a paginated list of professors with optional filters. Professors can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of professors retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view these professors"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOProfesor>> obtenerProfesores(
            @Parameter(description = "General search term (searches in firstName, lastName, email, username, dni)", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Professor's first name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String firstName,
            @Parameter(description = "Professor's last name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String lastName,
            @Parameter(description = "Professor's email to filter by", required = false)
            @RequestParam(required = false) String email,
            @Parameter(description = "Professor's username to filter by", required = false)
            @RequestParam(required = false) @Size(max = 50) String username,
            @Parameter(description = "Professor's DNI to filter by", required = false)
            @RequestParam(required = false) @Size(max = 20) String dni,
            @Parameter(description = "Account enabled status to filter by", required = false)
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Class ID to filter by", required = false)
            @RequestParam(required = false) String claseId,
            @Parameter(description = "If true, searches for professors without assigned classes", required = false)
            @RequestParam(required = false) Boolean sinClases,
            @Parameter(description = "Page number (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", required = false)
            @RequestParam(defaultValue = "id") @Size(max = 50) String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC") String sortDirection) {
        
        // Handle professor profile request (professors can only see their own profile)
        if (securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ADMIN")) {
            Long userId = securityUtils.getCurrentUserId();
            DTOProfesor dtoProfesor = servicioProfesor.obtenerProfesorPorId(userId);
            // Convert to paginated response with single item
            DTORespuestaPaginada<DTOProfesor> respuesta = DTORespuestaPaginada.of(
                List.of(dtoProfesor), 0, 1, 1, "id", "ASC");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "nombre", "apellidos", "dni", "email", "usuario", "habilitado", "fechaCreacion");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
            q, firstName, lastName, email, username, dni, enabled, claseId, sinClases);
        
        DTORespuestaPaginada<DTOProfesor> respuesta = servicioProfesor.buscarProfesoresPorParametrosPaginados(
            parametros, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    @Operation(
        summary = "Get professor by ID",
        description = "Gets a specific professor by their ID. Professors can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this professor"
        )
    })
    public ResponseEntity<DTOProfesor> obtenerProfesorPorId(
            @Parameter(description = "Professor ID", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOProfesor dtoProfesor = servicioProfesor.obtenerProfesorPorId(id);
        return new ResponseEntity<>(dtoProfesor, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new professor",
        description = "Creates a new professor in the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Professor created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Professor already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role required"
        )
    })
    public ResponseEntity<DTOProfesor> crearProfesor(
            @Valid @RequestBody DTOPeticionRegistroProfesor peticion) {
        
        DTOProfesor dtoProfesorNuevo = servicioProfesor.crearProfesor(peticion);
        return new ResponseEntity<>(dtoProfesorNuevo, HttpStatus.CREATED);
    }

    // Standard PATCH partial update endpoint
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #id == authentication.principal.id)")
    @Operation(
        summary = "Update professor partially",
        description = "Partially updates an existing professor. Professors can only update their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to update this professor"
        )
    })
    public ResponseEntity<DTOProfesor> actualizarProfesor(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id, 
            @Valid @RequestBody DTOActualizacionProfesor dtoParcial) {
        
        DTOProfesor dtoActualizado = servicioProfesor.actualizarProfesor(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete professor",
        description = "Deletes a professor from the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role required"
        )
    })
    public ResponseEntity<Map<String, Object>> borrarProfesorPorId(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        boolean borrado = servicioProfesor.borrarProfesorPorId(id);
        
        Map<String, Object> response = Map.of(
            "success", borrado,
            "message", "Professor deleted successfully",
            "professorId", id
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Business logic endpoints

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Change enabled status",
        description = "Enables or disables a professor in the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Enabled status updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role required"
        )
    })
    public ResponseEntity<DTOProfesor> cambiarEstadoProfesor(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @RequestBody Map<String, Boolean> estado) {
        
        Boolean enabled = estado.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("Must provide the 'enabled' field");
        }
        
        DTOProfesor dtoActualizado = servicioProfesor.cambiarEstadoProfesor(id, enabled);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    // Class management endpoints

    @PutMapping("/{id}/clases/{claseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Assign class to professor",
        description = "Assigns a specific class to a professor"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Class assigned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor or class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to assign classes"
        )
    })
    public ResponseEntity<DTOProfesor> asignarClase(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @PathVariable String claseId) {
        
        DTOProfesor dtoActualizado = servicioProfesor.asignarClase(id, claseId);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/clases/{claseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Remove class from professor",
        description = "Removes a specific class from a professor"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Class removed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor or class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to remove classes"
        )
    })
    public ResponseEntity<DTOProfesor> removerClase(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @PathVariable String claseId) {
        
        DTOProfesor dtoActualizado = servicioProfesor.removerClase(id, claseId);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    // Utility endpoints for specific lookups

    // Class-specific endpoints

    @GetMapping("/clase/{claseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get professors by class",
        description = "Gets the professors who teach a specific class"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professors of the class obtained successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class, type = "array")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view these professors"
        )
    })
    public ResponseEntity<List<DTOProfesor>> obtenerProfesoresPorClase(
            @Parameter(description = "Class ID", required = true)
            @PathVariable String claseId) {
        
        List<DTOProfesor> profesores = servicioProfesor.obtenerProfesoresPorClase(claseId);
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    @GetMapping("/{id}/clases/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Count professor's classes",
        description = "Gets the number of classes assigned to a professor"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Number of classes counted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this professor"
        )
    })
    public ResponseEntity<Map<String, Object>> contarClasesProfesor(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        Integer numeroClases = servicioProfesor.contarClasesProfesor(id);
        
        Map<String, Object> response = Map.of(
            "professorId", id,
            "numeroClases", numeroClases
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ===== ENDPOINTS DE BÚSQUEDA ESPECÍFICA =====

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get professor by email",
        description = "Gets a specific professor by their email address"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<DTOProfesor> obtenerProfesorPorEmail(
            @PathVariable @ValidEmail String email) {
        
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorEmail(email);
        return ResponseEntity.ok(profesor);
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get professor by DNI",
        description = "Gets a specific professor by their DNI number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<DTOProfesor> obtenerProfesorPorDni(
            @PathVariable @ValidDNI String dni) {
        
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorDni(dni);
        return ResponseEntity.ok(profesor);
    }

    // Student management endpoints for professors

    @PostMapping("/{profesorId}/clases/{claseId}/alumnos")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #profesorId == authentication.principal.id)")
    @Operation(
        summary = "Enroll student in professor's class",
        description = "Allows a professor to enroll a student in one of their classes"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student enrolled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaEnrollment.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or enrollment error"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to enroll in this class"
        )
    })
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnMiClase(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long profesorId,
            @PathVariable String claseId,
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        
        // Verify that the professor is trying to enroll in their own class
        if (!peticion.classId().toString().equals(claseId)) {
            return ResponseEntity.badRequest().body(
                DTORespuestaEnrollment.failure(peticion.studentId(), peticion.classId(), 
                    "The class ID in the URL does not match the request ID", "ENROLLMENT")
            );
        }
        
        DTORespuestaEnrollment respuesta = servicioClase.inscribirAlumnoEnClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    @DeleteMapping("/{profesorId}/clases/{claseId}/alumnos")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #profesorId == authentication.principal.id)")
    @Operation(
        summary = "Unenroll student from professor's class",
        description = "Allows a professor to unenroll a student from one of their classes"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student unenrolled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaEnrollment.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or unenrollment error"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to unenroll in this class"
        )
    })
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeMiClase(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long profesorId,
            @PathVariable String claseId,
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        
        // Verify that the professor is trying to unenroll from their own class
        if (!peticion.classId().toString().equals(claseId)) {
            return ResponseEntity.badRequest().body(
                DTORespuestaEnrollment.failure(peticion.studentId(), peticion.classId(), 
                    "The class ID in the URL does not match the request ID", "UNENROLLMENT")
            );
        }
        
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
}
