package app.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOClase;
import app.dtos.DTOCurso;
import app.dtos.DTOParametrosBusquedaClase;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOPeticionCrearCurso;
import app.dtos.DTOPeticionCrearTaller;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOTaller;
import app.entidades.enums.EDificultad;
import app.entidades.enums.EPresencialidad;
import app.servicios.ServicioClase;
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
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import app.util.SecurityUtils;

/**
 * REST controller for basic CRUD operations of classes (Courses and Workshops)
 * Handles creation, reading, updating and deletion of classes
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
@Validated
@Tag(name = "Classes", description = "API for basic management of classes, courses and workshops")
public class ClaseRest extends BaseRestController {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== READ OPERATIONS =====

    /**
     * Gets paginated classes according to the authenticated user's role:
     * - ADMIN: all classes
     * - PROFESSOR: only the classes they teach
     * - STUDENT: all classes (catalog)
     */
    @GetMapping
    @Operation(
        summary = "Get paginated classes",
        description = "Gets a paginated list of classes filtered according to the user's role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of classes retrieved successfully",
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
            description = "Access denied - Not authorized to view these classes"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> obtenerClases(
            @Parameter(description = "General search term across title and description")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by title")
            @RequestParam(required = false) String titulo,
            
            @Parameter(description = "Filter by description")
            @RequestParam(required = false) String descripcion,
            
            @Parameter(description = "Filter by difficulty level")
            @RequestParam(required = false) EDificultad dificultad,
            
            @Parameter(description = "Filter by modality (PRESENCIAL, ONLINE, HIBRIDO)")
            @RequestParam(required = false) EPresencialidad presencialidad,
            
            @Parameter(description = "Filter by professor ID")
            @RequestParam(required = false) String profesorId,
            
            @Parameter(description = "Filter by course ID")
            @RequestParam(required = false) String cursoId,
            
            @Parameter(description = "Filter by workshop ID")
            @RequestParam(required = false) String tallerId,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "titulo", "descripcion", "dificultad", "presencialidad", "profesorId", "cursoId", "tallerId");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            q, titulo, descripcion, presencialidad, dificultad, 
            null, null, null, null, page, size, sortBy, sortDirection);
        
        DTORespuestaPaginada<DTOClase> respuesta = servicioClase.buscarClasesSegunRol(parametros);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Gets a specific class by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get class by ID",
        description = "Gets a specific class by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Class found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this class"
        )
    })
    public ResponseEntity<DTOClase> obtenerClasePorId(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOClase dtoClase = servicioClase.obtenerClasePorId(id);
        return new ResponseEntity<>(dtoClase, HttpStatus.OK);
    }

    // ===== OPTIMIZED ENTITY GRAPH ENDPOINTS =====

    /**
     * Gets classes optimized for dashboard display with students and teachers loaded
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Get classes for dashboard",
        description = "Gets classes optimized for dashboard display with students and teachers loaded using Entity Graph"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view classes"
        )
    })
    public ResponseEntity<List<DTOClase>> obtenerClasesParaDashboard() {
        List<DTOClase> clases = servicioClase.obtenerClasesParaDashboard();
        return new ResponseEntity<>(clases, HttpStatus.OK);
    }

    /**
     * Gets classes optimized for exercise management with exercises loaded
     */
    @GetMapping("/ejercicios")
    @Operation(
        summary = "Get classes for exercise management",
        description = "Gets classes optimized for exercise management with exercises loaded using Entity Graph"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise management classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view classes"
        )
    })
    public ResponseEntity<List<DTOClase>> obtenerClasesParaGestionEjercicios() {
        List<DTOClase> clases = servicioClase.obtenerClasesParaGestionEjercicios();
        return new ResponseEntity<>(clases, HttpStatus.OK);
    }

    /**
     * Gets a class with all its details and relationships loaded using Entity Graph
     */
    @GetMapping("/{id}/detalles")
    @Operation(
        summary = "Get class with all details",
        description = "Gets a class with all its relationships loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Class details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this class"
        )
    })
    public ResponseEntity<DTOClase> obtenerClaseConDetalles(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOClase dtoClase = servicioClase.obtenerClaseConDetalles(id);
        return new ResponseEntity<>(dtoClase, HttpStatus.OK);
    }

    // ===== CREATE OPERATIONS =====

    /**
     * Creates a new course
     */
    @PostMapping("/cursos")
    @Operation(
        summary = "Create new course",
        description = "Creates a new course in the system (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Course created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOCurso.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Course already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<DTOCurso> crearCurso(
            @Parameter(description = "Course creation data", required = true)
            @Valid @RequestBody DTOPeticionCrearCurso peticion) {
        
        DTOPeticionCrearClase peticionClase = new DTOPeticionCrearClase(
            peticion.titulo(), peticion.descripcion(), peticion.precio(), 
            peticion.presencialidad(), peticion.imagenPortada(), peticion.nivel(), 
            peticion.profesoresId(), peticion.material());
        
        DTOCurso dtoCursoNuevo = servicioClase.crearCurso(peticionClase, peticion.fechaInicio(), peticion.fechaFin());
        return new ResponseEntity<>(dtoCursoNuevo, HttpStatus.CREATED);
    }

    /**
     * Creates a new workshop
     */
    @PostMapping("/talleres")
    @Operation(
        summary = "Create new workshop",
        description = "Creates a new workshop in the system (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Workshop created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOTaller.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Workshop already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<DTOTaller> crearTaller(
            @Parameter(description = "Workshop creation data", required = true)
            @Valid @RequestBody DTOPeticionCrearTaller peticion) {
        
        DTOPeticionCrearClase peticionClase = new DTOPeticionCrearClase(
            peticion.titulo(), peticion.descripcion(), peticion.precio(), 
            peticion.presencialidad(), peticion.imagenPortada(), peticion.nivel(), 
            peticion.profesoresId(), peticion.material());
        
        DTOTaller dtoTallerNuevo = servicioClase.crearTaller(peticionClase, peticion.duracionHoras(), 
            peticion.fechaRealizacion(), peticion.horaComienzo());
        return new ResponseEntity<>(dtoTallerNuevo, HttpStatus.CREATED);
    }

    // ===== DELETE OPERATIONS =====

    /**
     * Deletes a class
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete class",
        description = "Deletes a class from the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Class deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Class not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<Void> eliminarClase(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioClase.borrarClasePorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Gets paginated classes excluding those where the authenticated student is enrolled
     * For students: shows only classes they can enroll in
     * For admins/professors: shows all classes (same as regular endpoint)
     */
    @GetMapping("/disponibles")
    @Operation(
        summary = "Get available classes for enrollment",
        description = "Gets a paginated list of classes excluding those where the student is already enrolled"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of available classes retrieved successfully",
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
            description = "Access denied - Not authorized to view these classes"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> obtenerClasesDisponibles(
            @Parameter(description = "General search term across title and description")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by title")
            @RequestParam(required = false) String titulo,
            
            @Parameter(description = "Filter by description")
            @RequestParam(required = false) String descripcion,
            
            @Parameter(description = "Filter by difficulty level")
            @RequestParam(required = false) EDificultad dificultad,
            
            @Parameter(description = "Filter by modality (PRESENCIAL, ONLINE, HIBRIDO)")
            @RequestParam(required = false) EPresencialidad presencialidad,
            
            @Parameter(description = "Filter by minimum price")
            @RequestParam(required = false) @Min(value = 0, message = "Minimum price must be 0 or greater") Double precioMinimo,
            
            @Parameter(description = "Filter by maximum price")
            @RequestParam(required = false) @Min(value = 0, message = "Maximum price must be 0 or greater") Double precioMaximo,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") Integer page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") Integer size,
            
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") @Pattern(regexp = "^(id|titulo|descripcion|precio|nivel|presencialidad)$", message = "Invalid sort field") String sortBy,
            
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be ASC or DESC") String sortDirection) {
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "titulo", "descripcion", "dificultad", "presencialidad", "profesorId", "cursoId", "tallerId");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            q, titulo, descripcion, presencialidad, dificultad, 
            precioMinimo != null ? BigDecimal.valueOf(precioMinimo) : null, 
            precioMaximo != null ? BigDecimal.valueOf(precioMaximo) : null, 
            null, null, page, size, sortBy, sortDirection
        );
        
        DTORespuestaPaginada<DTOClase> respuesta = servicioClase.buscarClasesExcluyendoInscritas(parametros);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Debug endpoint to test role detection and show what's happening
     */
    @GetMapping("/debug-user-info")
    @Operation(
        summary = "Debug user info",
        description = "Debug endpoint to check current user role and permissions"
    )
    public ResponseEntity<String> debugUserInfo() {
        try {
            String userInfo = String.format(
                "User ID: %d, Role: %s, Is Admin: %b, Is Professor: %b, Is Student: %b",
                securityUtils.getCurrentUserId(),
                securityUtils.getCurrentUserRole(),
                securityUtils.isAdmin(),
                securityUtils.isProfessor(),
                securityUtils.isStudent()
            );
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.ok("Error getting user info: " + e.getMessage());
        }
    }
}
