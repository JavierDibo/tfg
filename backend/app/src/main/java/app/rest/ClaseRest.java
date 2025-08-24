package app.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import app.excepciones.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;

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
            
            @Parameter(description = "Filter by format (PRESENCIAL, ONLINE, HIBRIDO)")
            @RequestParam(required = false) EPresencialidad presencialidad,
            
            @Parameter(description = "Filter by difficulty level (PRINCIPIANTE, INTERMEDIO, AVANZADO)")
            @RequestParam(required = false) EDificultad nivel,
            
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) @Min(0) Double precioMinimo,
            
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) @Min(0) Double precioMaximo,
            
            @Parameter(description = "Filter for classes with available spots only")
            @RequestParam(required = false) Boolean soloConPlazasDisponibles,
            
            @Parameter(description = "Filter for upcoming classes only")
            @RequestParam(required = false) Boolean soloProximas,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC") String sortDirection) {
        
        // Validate and normalize parameters
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "titulo", "precio", "presencialidad", "nivel");
        sortDirection = validateSortDirection(sortDirection);
        
        // Create search parameters DTO
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            q, titulo, descripcion, presencialidad, nivel,
            precioMinimo != null ? java.math.BigDecimal.valueOf(precioMinimo) : null,
            precioMaximo != null ? java.math.BigDecimal.valueOf(precioMaximo) : null,
            soloConPlazasDisponibles, soloProximas,
            page, size, sortBy, sortDirection
        );
        
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClasesSegunRol(parametros);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Gets a class by its ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID", description = "Gets a specific class by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Class found successfully"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
        DTOClase clase = servicioClase.obtenerClasePorId(id);
        if (clase == null) {
            throw new ResourceNotFoundException("Class", "ID", id);
        }
        return ResponseEntity.ok(clase);
    }

    // ===== CREATION OPERATIONS =====

    /**
     * Creates a new course
     */
    @PostMapping("/cursos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create course", description = "Creates a new course. Requires ADMIN or PROFESOR permissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course created successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<DTOCurso> crearCurso(@Valid @RequestBody DTOPeticionCrearCurso peticion) {
        DTOCurso curso = servicioClase.crearCurso(
                new DTOPeticionCrearClase(
                        peticion.titulo(),
                        peticion.descripcion(),
                        peticion.precio(),
                        peticion.presencialidad(),
                        peticion.imagenPortada(),
                        peticion.nivel(),
                        peticion.profesoresId(),
                        peticion.material()
                ),
                peticion.fechaInicio(),
                peticion.fechaFin()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(curso);
    }

    /**
     * Creates a new workshop
     */
    @PostMapping("/talleres")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create workshop", description = "Creates a new workshop. Requires ADMIN or PROFESOR permissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Workshop created successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<DTOTaller> crearTaller(@Valid @RequestBody DTOPeticionCrearTaller peticion) {
        DTOTaller taller = servicioClase.crearTaller(
                new DTOPeticionCrearClase(
                        peticion.titulo(),
                        peticion.descripcion(),
                        peticion.precio(),
                        peticion.presencialidad(),
                        peticion.imagenPortada(),
                        peticion.nivel(),
                        peticion.profesoresId(),
                        peticion.material()
                ),
                peticion.duracionHoras(),
                peticion.fechaRealizacion(),
                peticion.horaComienzo()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taller);
    }

    // ===== DELETION OPERATIONS =====

    /**
     * Deletes a class by its ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class by ID", description = "Deletes a class by its ID. Requires ADMIN permissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Class deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Class not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN permissions")
    })
    public ResponseEntity<Void> borrarClasePorId(@PathVariable Long id) {
        boolean resultado = servicioClase.borrarClasePorId(id);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
