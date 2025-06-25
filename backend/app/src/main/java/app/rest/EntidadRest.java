package app.rest;

import app.entidades.Entidad;
import app.servicios.ServicioEntidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/entidades")
@CrossOrigin(origins = "localhost:8080")
public class EntidadRest {

    @Autowired
    ServicioEntidad servicioEntidad;

    @GetMapping("/lista")
    public List<Entidad> obtenerTodasLasEntidades() {
        System.out.println("\n\n\n\nObteniendo todas las entidades\n\n\n\n");
        return servicioEntidad.devolverTodasLasEntidades();
    }

    @DeleteMapping("/borrar")
    public void borrarTodasLasEntidades() {
        System.out.println("\n\n\n\nBorrando todas las entidades\n\n\n\n");
        List<Entidad> entidades = servicioEntidad.devolverTodasLasEntidades();
        servicioEntidad.borrarTodasLasEntidades(entidades);
    }

    @DeleteMapping("/borrar/{id}")
    public void borrarEntidadPorId(@PathVariable int id) {
        System.out.println("\n\n\n\nBorrando entidad: " + id + "\n\n\n\n");
        Optional<Entidad> entidad = servicioEntidad.borrarEntidadPorId(id);
        if (entidad.isEmpty()) {
            System.out.println("Entidad no encontrada con ID: " + id);
        } else {
            System.out.println("Entidad borrada: " + entidad.get().getId() + ", " + entidad.get().getInfo());
        }
    }

    @PostMapping("/crear")
    public void crearEntidad(@RequestParam String info) {
        System.out.println("\n\n\n\nCreando entidad con info: " + info + "\n\n\n\n");
        servicioEntidad.crearEntidad(info);
    }
}
