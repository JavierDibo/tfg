package app.rest;

import app.dtos.DTOEntidad;
import app.dtos.DTOParametrosBusqueda;
import app.excepciones.ResourceNotFoundException;
import app.servicios.ServicioEntidad;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entidades")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated // Activa validación de parámetros
@Tag(name = "Entidades", description = "API para gestión de entidades genéricas")
public class EntidadRest {

    private final ServicioEntidad servicioEntidad;

    @GetMapping
    @Operation(
        summary = "Obtener entidades",
        description = "Obtiene todas las entidades o las filtra por parámetros de búsqueda"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de entidades obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        )
    })
    public ResponseEntity<List<DTOEntidad>> obtenerEntidades(
            @Parameter(description = "Información para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String info,
            @Parameter(description = "Otra información para filtrar", required = false)
            @RequestParam(required = false) @Size(max = 100) String otraInfo) {
        
        DTOParametrosBusqueda parametros = new DTOParametrosBusqueda(info, otraInfo);
        
        if (info == null && otraInfo == null) {
            System.out.println("Obteniendo todas las entidades");
        } else {
            System.out.println("Obteniendo entidades con parámetros - info: " + info + ", otraInfo: " + otraInfo);
        }
        
        List<DTOEntidad> entidadesDTO = servicioEntidad.obtenerEntidadesPorParametros(parametros);
        return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener entidad por ID",
        description = "Obtiene una entidad específica por su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Entidad encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Entidad no encontrada"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID inválido"
        )
    })
    public ResponseEntity<DTOEntidad> obtenerEntidadPorId(
            @Parameter(description = "ID de la entidad", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id) {
        System.out.println("Obteniendo la entidad con ID: " + id);
        DTOEntidad dtoEntidad = servicioEntidad.obtenerEntidadPorId(id);
        
        if (dtoEntidad == null) {
            throw new ResourceNotFoundException("Entidad", "ID", id);
        }

        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Borrar todas las entidades",
        description = "Elimina todas las entidades del sistema (requiere rol ADMIN)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Todas las entidades eliminadas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN"
        )
    })
    public ResponseEntity<List<DTOEntidad>> borrarTodasLasEntidades() {
        System.out.println("Borrando todas las entidades");
        List<DTOEntidad> entidadesDTO = servicioEntidad.borrarTodasLasEntidades();
        return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Borrar entidad por ID",
        description = "Elimina una entidad específica por su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Entidad eliminada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID inválido"
        )
    })
    public ResponseEntity<DTOEntidad> borrarEntidadPorId(
            @Parameter(description = "ID de la entidad a eliminar", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id) {
        DTOEntidad dtoEntidad = servicioEntidad.borrarEntidadPorId(id);
        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @PostMapping
    @Operation(
        summary = "Crear nueva entidad",
        description = "Crea una nueva entidad en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Entidad creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
        )
    })
    public ResponseEntity<DTOEntidad> crearEntidad(
            @Parameter(description = "Datos de la entidad a crear", required = true)
            @Valid @RequestBody DTOEntidad dtoEntidad) {
        DTOEntidad dtoEntidadNueva = servicioEntidad.crearEntidad(dtoEntidad);
        return new ResponseEntity<>(dtoEntidadNueva, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Actualizar entidad",
        description = "Actualiza parcialmente una entidad existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Entidad actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntidad.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Entidad no encontrada"
        )
    })
    public ResponseEntity<DTOEntidad> actualizarEntidad(
            @Parameter(description = "ID de la entidad a actualizar", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id, 
            @Parameter(description = "Datos parciales para actualizar", required = true)
            @Valid @RequestBody DTOEntidad dtoParcial) {
        System.out.println("Actualizando entidad con ID: " + id);
        DTOEntidad dtoActualizado = servicioEntidad.actualizarEntidad(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }
}
