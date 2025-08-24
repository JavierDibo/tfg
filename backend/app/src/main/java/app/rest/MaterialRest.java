package app.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioMaterial;
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
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for Material management
 * Handles CRUD operations for educational materials
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Materials", description = "API for educational material management")
public class MaterialRest extends BaseRestController {

    private final ServicioMaterial servicioMaterial;

    // ===== READ OPERATIONS =====

    /**
     * Gets paginated materials with optional filters
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get paginated materials",
        description = "Gets a paginated list of materials with optional filters"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of materials retrieved successfully",
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
            description = "Access denied - Not authorized to view materials"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOMaterial>> obtenerMateriales(
            @Parameter(description = "General search term (searches in name and URL)")
            @RequestParam(required = false) @Size(max = 100) String q,
            
            @Parameter(description = "Material name to filter by")
            @RequestParam(required = false) @Size(max = 200) String name,
            
            @Parameter(description = "Material URL to filter by")
            @RequestParam(required = false) @Size(max = 500) String url,
            
            @Parameter(description = "Material type to filter by (DOCUMENT, IMAGE, VIDEO)")
            @RequestParam(required = false) String type,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") @Pattern(regexp="ASC|DESC") String sortDirection) {

        DTORespuestaPaginada<DTOMaterial> respuesta = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Gets a specific material by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get material by ID",
        description = "Gets a specific material by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Material retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOMaterial.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this material"
        )
    })
    public ResponseEntity<DTOMaterial> obtenerMaterialPorId(
            @Parameter(description = "Material ID")
            @PathVariable String id) {

        DTOMaterial material = servicioMaterial.obtenerMaterialPorId(id);
        return ResponseEntity.ok(material);
    }



    // ===== CREATE OPERATIONS =====

    /**
     * Creates a new material
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Create new material",
        description = "Creates a new educational material"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Material created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOMaterial.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid material data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Material with this name already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to create materials"
        )
    })
    public ResponseEntity<DTOMaterial> crearMaterial(
            @Parameter(description = "Material name")
            @RequestParam @Size(max = 200) String name,
            
            @Parameter(description = "Material URL")
            @RequestParam @Size(max = 500) String url) {

        DTOMaterial material = servicioMaterial.crearMaterial(name, url);
        return ResponseEntity.status(HttpStatus.CREATED).body(material);
    }

    // ===== UPDATE OPERATIONS =====

    /**
     * Updates an existing material
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Update material",
        description = "Updates an existing material"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Material updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOMaterial.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid material data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Material with this name already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to update materials"
        )
    })
    public ResponseEntity<DTOMaterial> actualizarMaterial(
            @Parameter(description = "Material ID")
            @PathVariable String id,
            
            @Parameter(description = "Material name")
            @RequestParam @Size(max = 200) String name,
            
            @Parameter(description = "Material URL")
            @RequestParam @Size(max = 500) String url) {

        DTOMaterial material = servicioMaterial.actualizarMaterial(id, name, url);
        return ResponseEntity.ok(material);
    }

    // ===== DELETE OPERATIONS =====

    /**
     * Deletes a material by ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Delete material",
        description = "Deletes a material by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Material deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to delete materials"
        )
    })
    public ResponseEntity<Void> borrarMaterial(
            @Parameter(description = "Material ID")
            @PathVariable String id) {

        servicioMaterial.borrarMaterialPorId(id);
        return ResponseEntity.noContent().build();
    }

    // ===== STATISTICS OPERATIONS =====

    /**
     * Gets material statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get material statistics",
        description = "Gets statistics about materials"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view statistics"
        )
    })
    public ResponseEntity<MaterialStats> obtenerEstadisticas() {
        long totalMateriales = servicioMaterial.contarMateriales();
        long totalDocumentos = servicioMaterial.contarDocumentos();
        long totalImagenes = servicioMaterial.contarImagenes();
        long totalVideos = servicioMaterial.contarVideos();

        MaterialStats stats = new MaterialStats(totalMateriales, totalDocumentos, totalImagenes, totalVideos);
        return ResponseEntity.ok(stats);
    }

    /**
     * Record for material statistics
     */
    public record MaterialStats(
        long totalMaterials,
        long totalDocuments,
        long totalImages,
        long totalVideos
    ) {}
}
