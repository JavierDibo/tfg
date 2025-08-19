package app.repositorios;

import app.entidades.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioAlumno extends JpaRepository<Alumno, Long> {
    
    Optional<Alumno> findByUsuario(String usuario);
    
    Optional<Alumno> findByEmail(String email);
    
    Optional<Alumno> findByDni(String dni);
    
    boolean existsByUsuario(String usuario);
    
    boolean existsByEmail(String email);
    
    boolean existsByDni(String dni);
    
    @Query("SELECT a FROM Alumno a WHERE a.nombre LIKE %:nombre% ORDER BY a.id")
    List<Alumno> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT a FROM Alumno a WHERE a.apellidos LIKE %:apellidos% ORDER BY a.id")
    List<Alumno> findByApellidosContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    @Query("SELECT a FROM Alumno a WHERE a.matriculado = :matriculado ORDER BY a.id")
    List<Alumno> findByMatriculado(@Param("matriculado") boolean matriculado);
    
    @Query("SELECT a FROM Alumno a WHERE " +
           "(:nombre IS NULL OR a.nombre LIKE %:nombre%) AND " +
           "(:apellidos IS NULL OR a.apellidos LIKE %:apellidos%) AND " +
           "(:dni IS NULL OR a.dni LIKE %:dni%) AND " +
           "(:email IS NULL OR a.email LIKE %:email%) AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) " +
           "ORDER BY a.id")
    List<Alumno> findByFiltros(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("dni") String dni,
        @Param("email") String email,
        @Param("matriculado") Boolean matriculado
    );
    
    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.matriculado = :matriculado")
    long countByMatriculado(@Param("matriculado") boolean matriculado);
    
    @Query("SELECT a FROM Alumno a ORDER BY a.id")
    List<Alumno> findAllOrderedById();
}
