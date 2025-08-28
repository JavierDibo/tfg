// LLM_EDIT_TIMESTAMP: 25 ago. 14:13
package app.rest;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTOPeticionCrearEntregaEjercicio;
import app.dtos.DTOPeticionActualizarEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioEntregaEjercicio;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Exercise Deliveries", description = "API for exercise delivery management")
public class EntregaEjercicioRest extends BaseRestController {

    private final ServicioEntregaEjercicio servicioEntregaEjercicio;
    private final SecurityUtils securityUtils;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @Operation(
        summary = "Get paginated exercise deliveries",
        description = "Gets a paginated list of exercise deliveries with optional filters. Students can only see their own deliveries."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of deliveries retrieved successfully",
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
            description = "Access denied - Not authorized to view these deliveries"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOEntregaEjercicio>> obtenerEntregas(
            @Parameter(description = "Student ID to filter by", required = false)
            @RequestParam(required = false) @Size(max = 255) String alumnoId,
            @Parameter(description = "Exercise ID to filter by", required = false)
            @RequestParam(required = false) @Size(max = 255) String ejercicioId,
            @Parameter(description = "Delivery status to filter by (PENDIENTE, ENTREGADO, CALIFICADO)", required = false)
            @RequestParam(required = false) String estado,
            @Parameter(description = "Minimum grade to filter by", required = false)
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal notaMin,
            @Parameter(description = "Maximum grade to filter by", required = false)
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal notaMax,
            @Parameter(description = "Page number (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", required = false)
            @RequestParam(defaultValue = "id") @Size(max = 50) String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "fechaEntrega", "nota", "estado", "alumnoEntreganteId", "ejercicioId");
        sortDirection = validateSortDirection(sortDirection);
        
        // Handle role-based filtering
        if (securityUtils.hasRole("ALUMNO") && !securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            // Students can only see their own deliveries
            Long userId = securityUtils.getCurrentUserId();
            alumnoId = userId.toString();
        }
        
        DTORespuestaPaginada<DTOEntregaEjercicio> respuesta = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @Operation(
        summary = "Get delivery by ID",
        description = "Gets a specific delivery by its ID. Students can only see their own deliveries."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delivery found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this delivery"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> obtenerEntregaPorId(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.obtenerEntregaPorId(id);
        return new ResponseEntity<>(dtoEntrega, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @Operation(
        summary = "Create new delivery",
        description = "Creates a new exercise delivery (requires ALUMNO role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Delivery created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Delivery already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ALUMNO role is required"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> crearEntrega(
            @Parameter(description = "Delivery creation data", required = true)
            @Valid @RequestBody DTOPeticionCrearEntregaEjercicio peticion) {
        
        // Students can only create deliveries for themselves
        Long userId = securityUtils.getCurrentUserId();
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.crearEntrega(
            userId,
            peticion.ejercicioId(),
            peticion.archivosEntregados()
        );
        
        return new ResponseEntity<>(dtoEntrega, HttpStatus.CREATED);
    }

    // Standard PUT replace endpoint
    @PutMapping("/{id}")
    @Operation(
        summary = "Replace delivery",
        description = "Replaces an entire delivery. Students can only update their own deliveries (files only). Teachers and admins can grade deliveries and add comments using the 'nota' and 'comentarios' fields."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delivery replaced successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this delivery"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> reemplazarEntrega(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Delivery replacement data", required = true)
            @Valid @RequestBody DTOPeticionActualizarEntregaEjercicio peticion) {
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.actualizarEntrega(
            id,
            peticion.archivosEntregados(),
            peticion.nota(),
            peticion.comentarios()
        );
        
        return new ResponseEntity<>(dtoEntrega, HttpStatus.OK);
    }

    // Standard PATCH partial update endpoint
    @PatchMapping("/{id}")
    @Operation(
        summary = "Partially update delivery",
        description = "Partially updates a delivery. Students can only update their own deliveries (files only). Teachers and admins can grade deliveries and add comments using the 'nota' and 'comentarios' fields. Use this endpoint to grade a delivery with comments or add comments to an existing delivery."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delivery updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this delivery"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> actualizarEntregaParcial(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Partial delivery update data", required = true)
            @RequestBody DTOPeticionActualizarEntregaEjercicio peticion) {
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.actualizarEntrega(
            id,
            peticion.archivosEntregados(),
            peticion.nota(),
            peticion.comentarios()
        );
        
        return new ResponseEntity<>(dtoEntrega, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete delivery",
        description = "Deletes a delivery from the system (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Delivery deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Void> eliminarEntrega(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioEntregaEjercicio.borrarEntregaPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Additional endpoints for specific use cases

    @GetMapping("/estadisticas")
    @Operation(
        summary = "Get delivery statistics",
        description = "Gets statistics about deliveries (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Object> obtenerEstadisticas() {
        long totalEntregas = servicioEntregaEjercicio.contarEntregas();
        long entregasCalificadas = servicioEntregaEjercicio.contarEntregasPorEstado(app.entidades.enums.EEstadoEjercicio.CALIFICADO);
        long entregasPendientes = servicioEntregaEjercicio.contarEntregasPorEstado(app.entidades.enums.EEstadoEjercicio.PENDIENTE);
        long entregasEntregadas = servicioEntregaEjercicio.contarEntregasPorEstado(app.entidades.enums.EEstadoEjercicio.ENTREGADO);
        
        var estadisticas = new Object() {
            public final long total = totalEntregas;
            public final long calificadas = entregasCalificadas;
            public final long pendientes = entregasPendientes;
            public final long entregadas = entregasEntregadas;
            public final double porcentajeCalificadas = totalEntregas > 0 ? (entregasCalificadas * 100.0) / totalEntregas : 0.0;
        };
        
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    // ===== OPTIMIZED ENTITY GRAPH ENDPOINTS =====

    /**
     * Gets a delivery with its student loaded using Entity Graph
     */
    @GetMapping("/{id}/con-alumno")
    @Operation(
        summary = "Get delivery with student",
        description = "Gets a delivery with its student loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delivery with student retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this delivery"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> obtenerEntregaConAlumno(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.obtenerEntregaConAlumno(id);
        return new ResponseEntity<>(dtoEntrega, HttpStatus.OK);
    }

    /**
     * Gets a delivery with its exercise loaded using Entity Graph
     */
    @GetMapping("/{id}/con-ejercicio")
    @Operation(
        summary = "Get delivery with exercise",
        description = "Gets a delivery with its exercise loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delivery with exercise retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Delivery not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this delivery"
        )
    })
    public ResponseEntity<DTOEntregaEjercicio> obtenerEntregaConEjercicio(
            @Parameter(description = "ID of the delivery", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.obtenerEntregaConEjercicio(id);
        return new ResponseEntity<>(dtoEntrega, HttpStatus.OK);
    }
}
