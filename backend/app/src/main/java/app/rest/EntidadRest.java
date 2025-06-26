package app.rest;

import app.aop.Validador;
import app.dtos.DTOEntidad;
import app.entidades.Entidad;
import app.servicios.ServicioEntidad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/entidades")
@CrossOrigin(origins = {"localhost:8080", "http://localhost:5173"})
public class EntidadRest {

    @Autowired
    ServicioEntidad servicioEntidad;

    @GetMapping("/obtener")
    public ResponseEntity<List<DTOEntidad>> obtenerEntidadPorInfo(@RequestParam(required = false) @Size(max = 100) String info) {
        if (info == null) {
            System.out.println("Obteniendo todas las entidades");
            List<Entidad> entidades = servicioEntidad.obtenerTodasLasEntidades();
            List<DTOEntidad> entidadesDTO = entidades
                    .stream()
                    .map(e -> new DTOEntidad(e.getId(), e.getInfo()))
                    .toList();
            return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
        } else {
            System.out.println("Obteniendo entidades con info: " + info);
            List<Entidad> entidades = servicioEntidad.obtenerEntidadesPorInfo(info);
            if (entidades.isEmpty()) {
                return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
            }
            List<DTOEntidad> entidadesDTO = entidades
                    .stream()
                    .map(e -> new DTOEntidad(e.getId(), e.getInfo()))
                    .toList();
            return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
        }
    }

    @Validador
    @GetMapping("/obtener/{id}")
    public ResponseEntity<DTOEntidad> obtenerEntidadPorId(@PathVariable int id) {
        System.out.println("Obteniendo la entidad con ID: " + id);
        Optional<Entidad> entidad = servicioEntidad.obtenerEntidadPorId(id);

        if (entidad.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        DTOEntidad dtoEntidad = entidad.map(e -> new DTOEntidad(e.getId(), e.getInfo())).orElse(null);
        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }

    @DeleteMapping("/borrar")
    public ResponseEntity<List<DTOEntidad>> borrarTodasLasEntidades() {
        System.out.println("Borrando todas las entidades");
        List<Entidad> entidades = servicioEntidad.borrarTodasLasEntidades();
        List<DTOEntidad> entidadesDTO = entidades
                .stream()
                .map(entidad -> new DTOEntidad(entidad.getId(), entidad.getInfo()))
                .toList();
        return new ResponseEntity<>(entidadesDTO, HttpStatus.OK);
    }

    @Validador
    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<DTOEntidad> borrarEntidadPorId(@PathVariable int id) {
        Optional<Entidad> entidadOptional = servicioEntidad.borrarEntidadPorId(id);
        if (entidadOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Entidad entidad = entidadOptional.get();
        return new ResponseEntity<>(new DTOEntidad(entidad.getId(), entidad.getInfo()), HttpStatus.OK);
    }

    @Validador
    @PostMapping("/crear")
    public ResponseEntity<DTOEntidad> crearEntidad(@RequestParam String info) {
        System.out.println("Creando entidad con info: " + info);
        Entidad entidad = new Entidad(info);
        Entidad entidadCreada = servicioEntidad.crearEntidad(entidad);
        DTOEntidad dtoEntidad = new DTOEntidad(entidadCreada.getId(), entidadCreada.getInfo());
        return new ResponseEntity<>(dtoEntidad, HttpStatus.CREATED);
    }

    @Validador
    @PatchMapping("/actualizar/{id}")
    public ResponseEntity<DTOEntidad> actualizarEntidad(@PathVariable int id, @RequestParam String info) {
        System.out.println("Actualizando entidad con ID: " + id + " a info: " + info);
        Optional<Entidad> entidadOptional = servicioEntidad.actualizarEntidad(id, info);
        if (entidadOptional.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Entidad entidad = entidadOptional.get();
        DTOEntidad dtoEntidad = new DTOEntidad(entidad.getId(), entidad.getInfo());
        return new ResponseEntity<>(dtoEntidad, HttpStatus.OK);
    }
}
