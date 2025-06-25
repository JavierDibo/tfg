package app.servicios;

import app.entidades.Entidad;
import app.repositorios.RepositorioEntidad;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioEntidad {

    @Autowired
    private RepositorioEntidad repositorioEntidad;

    // Crear una nueva entidad
    @Transactional
    public Entidad crearEntidad(Entidad entidad) {
        return repositorioEntidad.save(entidad);
    }

    @Transactional
    public Entidad crearEntidad(String info) {
        Entidad entidad = new Entidad(info);
        return repositorioEntidad.save(entidad);
    }

    // Buscar entidad por ID
    @Transactional
    public Optional<Entidad> buscarPorId(int id) {
        Optional<Entidad> entidad = repositorioEntidad.findById(id);
        if (entidad.isEmpty()) {
            return Optional.empty();
        }
        return entidad;
    }

    // Actualizar una entidad existente
    @Transactional
    public Optional<Entidad> actualizarEntidad(Entidad entidad) {
        Optional<Entidad> entidadRecibida = repositorioEntidad.findById(entidad.getId());

        if (entidadRecibida.isEmpty())
            return Optional.empty();

        Entidad entidadExistente = repositorioEntidad.save(entidadRecibida.get());

        return Optional.of(entidadExistente);
    }

    @Transactional
    public List<Entidad> devolverTodasLasEntidades() {
        return repositorioEntidad.findAll();
    }

    @Transactional
    public void borrarTodasLasEntidades(List<Entidad> entidades) {
        repositorioEntidad.deleteAll(entidades);
    }

    @Transactional
    public Optional<Entidad> borrarEntidadPorId(int id) {
        Optional<Entidad> entidad = repositorioEntidad.findById(id);
        if (entidad.isEmpty()) {
            System.out.println("No se encontró la entidad con ID: " + id);
            return Optional.empty();
        }
        repositorioEntidad.deleteById(id);
        return entidad;
    }
}