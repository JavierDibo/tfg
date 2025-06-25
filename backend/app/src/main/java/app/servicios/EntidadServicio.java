package app.servicios;

import app.entidades.Entidad;
import app.repositorios.EntidadRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntidadServicio {

    @Autowired
    private EntidadRepositorio entidadRepositorio;

    // Crear una nueva entidad
    @Transactional
    public Entidad crearEntidad(Entidad entidad) {
        return entidadRepositorio.save(entidad);
    }

    // Buscar entidad por ID
    @Transactional
    public Optional<Entidad> buscarPorId(int id) {
        Optional<Entidad> entidad = entidadRepositorio.findById(id);
        if (entidad.isEmpty()) {
            return Optional.empty();
        }
        return entidad;
    }

    // Actualizar una entidad existente
    @Transactional
    public Optional<Entidad> actualizarEntidad(Entidad entidad) {
        Optional<Entidad> entidadRecibida = entidadRepositorio.findById(entidad.getId());

        if (entidadRecibida.isEmpty())
            return Optional.empty();

        Entidad entidadExistente = entidadRepositorio.save(entidadRecibida.get());

        return Optional.of(entidadExistente);
    }

    @Transactional
    public List<Entidad> devolverTodasLasEntidades() {
        return entidadRepositorio.findAll();
    }

    @Transactional
    public void borrarTodasLasEntidades(List<Entidad> entidades) {
        entidadRepositorio.deleteAll(entidades);
    }
}