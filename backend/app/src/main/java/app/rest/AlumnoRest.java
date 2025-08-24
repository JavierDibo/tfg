package app.rest;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOParametrosBusquedaAlumno;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Students", description = "API for student management")
public class AlumnoRest extends BaseRestController {

    private final ServicioAlumno servicioAlumno;
    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get paginated students",
        description = "Gets a paginated list of students with optional filters. Students can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of students retrieved successfully",
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
            description = "Access denied - Not authorized to view these students"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnos(
            @Parameter(description = "General search term (searches in firstName, lastName, dni, email)", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Student's first name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String firstName,
            @Parameter(description = "Student's last name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String lastName,
            @Parameter(description = "Student's DNI to filter by", required = false)
            @RequestParam(required = false) @Size(max = 20) String dni,
            @Parameter(description = "Student's email to filter by", required = false)
            @RequestParam(required = false) String email,
            @Parameter(description = "Enrollment status to filter by", required = false)
            @RequestParam(required = false) Boolean enrolled,
            @Parameter(description = "Account enabled status to filter by", required = false)
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Filter only available students (enabled and enrolled)", required = false)
            @RequestParam(required = false) Boolean available,
            @Parameter(description = "Page number (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", required = false)
            @RequestParam(defaultValue = "id") @Size(max = 50) String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC") String sortDirection) {
        
        // Handle student profile request (students can only see their own profile)
        if (securityUtils.hasRole("ALUMNO") && !securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            Long userId = securityUtils.getCurrentUserId();
            DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorId(userId);
            // Convert to paginated response with single item
            DTORespuestaPaginada<DTOAlumno> respuesta = DTORespuestaPaginada.of(
                List.of(dtoAlumno), 0, 1, 1, "id", "ASC");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "firstName", "lastName", "dni", "email", "enrolled", "enabled");
        sortDirection = validateSortDirection(sortDirection);
        
        // Handle special filtering for available students
        if (Boolean.TRUE.equals(available)) {
            DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.obtenerAlumnosDisponiblesPaginados(
                page, size, sortBy, sortDirection);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }
        
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
            q, firstName, lastName, dni, email, enrolled);
        
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.buscarAlumnosPorParametrosPaginados(
            parametros, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    @Operation(
        summary = "Get student by ID",
        description = "Gets a specific student by their ID. Students can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this student"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorId(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new student",
        description = "Creates a new student in the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Student created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Student already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<DTOAlumno> crearAlumno(
            @Valid @RequestBody DTOPeticionRegistroAlumno peticion) {
        
        DTOAlumno dtoAlumnoNuevo = servicioAlumno.crearAlumno(peticion);
        return new ResponseEntity<>(dtoAlumnoNuevo, HttpStatus.CREATED);
    }

    // Standard PATCH partial update endpoint with status management
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    @Operation(
        summary = "Update student partially",
        description = "Partially updates an existing student. Students can only update their own profile. Administrators can change enrollment and enabled status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to update this student"
        )
    })
    public ResponseEntity<DTOAlumno> actualizarAlumno(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id, 
            @Valid @RequestBody DTOActualizacionAlumno dtoParcial) {
        
        DTOAlumno dtoActualizado = servicioAlumno.actualizarAlumno(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete student",
        description = "Deletes a student from the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<DTOAlumno> borrarAlumnoPorId(
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.borrarAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    // Specialized endpoints for specific use cases that don't fit standard CRUD

    @GetMapping("/{id}/clases-inscritas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    @Operation(
        summary = "Get classes enrolled by student",
        description = "Gets the classes in which a specific student is enrolled"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Classes enrolled retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClaseInscrita.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view these classes"
        )
    })
    public ResponseEntity<List<DTOClaseInscrita>> obtenerClasesInscritas(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        List<DTOClaseInscrita> clasesInscritas = servicioClase.obtenerClasesInscritasConDetalles(id);
        return new ResponseEntity<>(clasesInscritas, HttpStatus.OK);
    }
}
