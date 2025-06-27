package app.rest;

import app.aop.Validador;
import app.dtos.DTOEntidad;
import app.servicios.ServicioEntidad;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entidades")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
public class EntidadRest {

    @Autowired
    ServicioEntidad servicioEntidad;

    @GetMapping
    public ResponseEntity<List<DTOEntidad>> obtenerEntidadPorInfo(@RequestParam(required = false) @Size(max = 100) String info) {
        if (info == null) {
            System.out.println("Obteniendo todas las entidades");
            List<DTOEntidad> entidadesDTO = servicioEntidad.obtenerTodasLasEntidadesDTO();
            return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
        } else {
            System.out.println("Obteniendo entidades con info: " + info);
            List<DTOEntidad> entidadesDTO = servicioEntidad.obtenerEntidadesPorInfoDTO(info);
            return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
        }
    }

    @Validador
    @GetMapping("/{id}")
    public ResponseEntity<DTOEntidad> obtenerEntidadPorId(@PathVariable int id) {
        System.out.println("Obteniendo la entidad con ID: " + id);
        DTOEntidad dtoEntidad = servicioEntidad.obtenerEntidadPorIdDTO(id);

        if (dtoEntidad == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<List<DTOEntidad>> borrarTodasLasEntidades() {
        System.out.println("Borrando todas las entidades");
        List<DTOEntidad> entidadesDTO = servicioEntidad.borrarTodasLasEntidadesDTO();
        return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
    }

    @Validador
    @DeleteMapping("/{id}")
    public ResponseEntity<DTOEntidad> borrarEntidadPorId(@PathVariable int id) {
        DTOEntidad dtoEntidad = servicioEntidad.borrarEntidadPorIdDTO(id);
        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @Validador
    @PostMapping
    public ResponseEntity<DTOEntidad> crearEntidad(@RequestBody DTOEntidad dtoEntidad) {
        DTOEntidad dtoEntidadNueva = servicioEntidad.crearEntidadDTO(dtoEntidad);
        return new ResponseEntity<>(dtoEntidadNueva, HttpStatus.CREATED);
    }

    @Validador
    @PatchMapping("/{id}")
    public ResponseEntity<DTOEntidad> actualizarEntidad(@PathVariable int id, @RequestBody DTOEntidad dtoParcial) {
        System.out.println("Actualizando entidad con ID: " + id);
        DTOEntidad dtoActualizado = servicioEntidad.actualizarEntidadDTO(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }
}
