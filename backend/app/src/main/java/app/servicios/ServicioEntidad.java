package app.servicios;

import app.dtos.DTOEntidad;
import app.entidades.Entidad;
import app.repositorios.RepositorioEntidad;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioEntidad {

    @Autowired
    private RepositorioEntidad repositorioEntidad;

    public List<Entidad> obtenerTodasLasEntidades() {
        List<Entidad> entidades = repositorioEntidad.obtenerTodasLasEntidades();
        if (entidades.isEmpty()) {
            return List.of();
        }
        return entidades;
    }

    public Optional<Entidad> obtenerEntidadPorId(@NotNull @Min(1) int id) {
        return repositorioEntidad.obtenerEntidadPorId(id);
    }

    public List<Entidad> obtenerEntidadesPorInfo(@NotNull @Size(max=100) String info) {
        List<Entidad> entidades = repositorioEntidad.obtenerEntidadesPorInfo(info);
        if (entidades.isEmpty())
            return List.of();
        else
            return entidades;
    }

    public Entidad crearEntidad(@NotNull Entidad entidad) {
        return repositorioEntidad.crearEntidad(entidad);
    }

    public Optional<Entidad> actualizarEntidad(@NotNull @Min(1) int id, @NotNull @Size(max=100) String info) {
        return repositorioEntidad.actualizarEntidad(id, info);
    }

    public List<Entidad> borrarTodasLasEntidades() {
        // Por implementar
        return null;
    }

    public Optional<Entidad> borrarEntidadPorId(int id) {
        return repositorioEntidad.borrarEntidadPorId(id);
    }
}