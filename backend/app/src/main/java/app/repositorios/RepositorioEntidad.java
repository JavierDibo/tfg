package app.repositorios;

import app.aop.exceptions.EntidadNoEncontradaException;
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
        return em.createQuery("SELECT e FROM Entidad e ORDER BY id", Entidad.class).getResultList();
    }

    public Entidad obtenerEntidadPorId(@NotNull @Min(1) int id) {
        Entidad entidad = em.find(Entidad.class, id);
        if (entidad == null) {
            throw new EntidadNoEncontradaException("Entidad con ID " + id + " no encontrada.");
        }
        return entidad;
    }

    public List<Entidad> obtenerEntidadesPorInfo(@NotNull @Size(max = 100) String info) {
        String query = "SELECT e FROM Entidad e WHERE e.info LIKE :info ORDER BY id";
        return em.createQuery(query, Entidad.class).setParameter("info", "%" + info + "%").getResultList();
    }

    public Entidad crearEntidad(@NotNull Entidad entidad) {
        if (em.contains(entidad)) {
            em.persist(entidad); // no op
            return entidad;
        }
        return em.merge(entidad);
    }

    public List<Entidad> borrarTodasLasEntidades() {
        List<Entidad> entidades = obtenerTodasLasEntidades();
        if (entidades.isEmpty())
            return List.of();

        for (Entidad entidad : entidades) {
            em.remove(em.merge(entidad));
        }
        return entidades;
    }

    public Entidad borrarEntidadPorId(@NotNull @Min(1) int id) {
        Entidad entidad = obtenerEntidadPorId(id);
        em.remove(em.merge(entidad));
        return entidad;
    }

    public Entidad actualizarEntidad(@NotNull Entidad entidad) {
        Entidad entidadObtenida = obtenerEntidadPorId(entidad.getId());
        em.merge(entidadObtenida);
        return entidad;
    }
}
