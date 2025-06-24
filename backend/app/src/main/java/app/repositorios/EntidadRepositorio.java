package app.repositorios;

import app.entidades.Entidad;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface EntidadRepositorio extends JpaRepository<Entidad, Integer> {
    // JpaRepository already provides save(), findById(), etc.
    // You can add custom query methods here if needed
}
