package app.repositorios;

import app.entidades.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioEntidad extends JpaRepository<Entidad, Integer> {
}
