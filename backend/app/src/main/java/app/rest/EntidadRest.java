package app.rest;

import app.dtos.DTOEntidad;
import app.dtos.DTOParametrosBusqueda;
import app.servicios.ServicioEntidad;
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
public class EntidadRest {

    private final ServicioEntidad servicioEntidad;

    @GetMapping
    public ResponseEntity<List<DTOEntidad>> obtenerEntidades(
            @RequestParam(required = false) @Size(max = 100) String info,
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
    public ResponseEntity<DTOEntidad> obtenerEntidadPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id) {
        System.out.println("Obteniendo la entidad con ID: " + id);
        DTOEntidad dtoEntidad = servicioEntidad.obtenerEntidadPorId(id);

        if (dtoEntidad == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DTOEntidad>> borrarTodasLasEntidades() {
        System.out.println("Borrando todas las entidades");
        List<DTOEntidad> entidadesDTO = servicioEntidad.borrarTodasLasEntidades();
        return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DTOEntidad> borrarEntidadPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id) {
        DTOEntidad dtoEntidad = servicioEntidad.borrarEntidadPorId(id);
        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<DTOEntidad> crearEntidad(@Valid @RequestBody DTOEntidad dtoEntidad) {
        DTOEntidad dtoEntidadNueva = servicioEntidad.crearEntidad(dtoEntidad);
        return new ResponseEntity<>(dtoEntidadNueva, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DTOEntidad> actualizarEntidad(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") int id, 
            @Valid @RequestBody DTOEntidad dtoParcial) {
        System.out.println("Actualizando entidad con ID: " + id);
        DTOEntidad dtoActualizado = servicioEntidad.actualizarEntidad(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }
}
