package app.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "name", "url", "type", "createdAt");
        sortDirection = validateSortDirection(sortDirection);
        
        DTORespuestaPaginada<DTOMaterial> respuesta = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Gets a specific material by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get material by ID",
        description = "Gets a specific material by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Material found successfully",
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
            @Parameter(description = "ID of the material", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOMaterial dtoMaterial = servicioMaterial.obtenerMaterialPorId(id.toString());
        return new ResponseEntity<>(dtoMaterial, HttpStatus.OK);
    }

    // ===== CREATE OPERATIONS =====

    /**
     * Creates a new material
     */
    @PostMapping
    @Operation(
        summary = "Create new material",
        description = "Creates a new material in the system (requires ADMIN or PROFESOR role)"
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
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Material already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<DTOMaterial> crearMaterial(
            @Parameter(description = "Material name", required = true)
            @RequestParam @Size(max = 200) String name,
            @Parameter(description = "Material URL", required = true)
            @RequestParam @Size(max = 500) String url) {
        
        DTOMaterial dtoMaterialNuevo = servicioMaterial.crearMaterial(name, url);
        return new ResponseEntity<>(dtoMaterialNuevo, HttpStatus.CREATED);
    }

    // ===== UPDATE OPERATIONS =====

    /**
     * Updates an existing material
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update material",
        description = "Updates an existing material (requires ADMIN or PROFESOR role)"
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
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Material not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this material"
        )
    })
    public ResponseEntity<DTOMaterial> actualizarMaterial(
            @Parameter(description = "ID of the material", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Material name", required = true)
            @RequestParam @Size(max = 200) String name,
            @Parameter(description = "Material URL", required = true)
            @RequestParam @Size(max = 500) String url) {
        
        DTOMaterial dtoMaterialActualizado = servicioMaterial.actualizarMaterial(id.toString(), name, url);
        return new ResponseEntity<>(dtoMaterialActualizado, HttpStatus.OK);
    }

    // ===== DELETE OPERATIONS =====

    /**
     * Deletes a material
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete material",
        description = "Deletes a material from the system (requires ADMIN or PROFESOR role)"
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
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Void> eliminarMaterial(
            @Parameter(description = "ID of the material", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioMaterial.borrarMaterialPorId(id.toString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
