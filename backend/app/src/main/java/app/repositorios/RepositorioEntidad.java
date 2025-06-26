package app.repositorios;

import app.entidades.Entidad;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@Validated
public class RepositorioEntidad {

    @PersistenceContext
    EntityManager em;

    public List<Entidad> obtenerTodasLasEntidades() {
        return em.createQuery("SELECT e FROM Entidad e", Entidad.class).getResultList();
    }

    public Optional<Entidad> obtenerEntidadPorId(@NotNull @Min(1) int id) {
        Entidad entidad = em.find(Entidad.class, id);
        if (entidad == null)
            return Optional.empty();

        return Optional.of(entidad);
    }

    public List<Entidad> obtenerEntidadesPorInfo(@NotNull @Size(max = 100) String info) {
        String query = "SELECT e FROM Entidad e WHERE e.info = :info";
        return em.createQuery(query, Entidad.class).setParameter("info", info).getResultList();
    }

    public Entidad crearEntidad(@NotNull Entidad entidad) {
        em.merge(entidad);
        em.persist(entidad);
        return entidad;
    }

    public List<Entidad> borrarTodasLasEntidades() {
        List<Entidad> entidades = obtenerTodasLasEntidades();
        if (entidades.isEmpty())
            return List.of();

        for (Entidad entidad : entidades) {
            em.merge(entidad);
            em.remove(entidad);
        }
        return entidades;
    }

    public Optional<Entidad> borrarEntidadPorId(@NotNull @Min(1) int id) {
        Optional<Entidad> entidadOptional = obtenerEntidadPorId(id);
        if (entidadOptional.isEmpty())
            return Optional.empty();

        Entidad entidad = entidadOptional.get();
        em.merge(entidad);
        em.remove(entidad);
        return Optional.of(entidad);
    }

    public Optional<Entidad> actualizarEntidad(@NotNull @Min(1) int id, @NotNull @Size(max = 100) String info) {
        Optional<Entidad> entidadOptional = obtenerEntidadPorId(id);
        if (entidadOptional.isEmpty()) {
            return Optional.empty();
        }

        Entidad entidad = entidadOptional.get();
        entidad.setInfo(info);
        em.merge(entidad);
        return Optional.of(entidad);
    }
}
