package app.repositorios;

import app.entidades.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    @Query(value = "SELECT * FROM usuarios a WHERE normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%' AND a.tipo_usuario = 'ALUMNO' ORDER BY a.id", nativeQuery = true)
    List<Alumno> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query(value = "SELECT * FROM usuarios a WHERE normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%' AND a.tipo_usuario = 'ALUMNO' ORDER BY a.id", nativeQuery = true)
    List<Alumno> findByApellidosContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    @Query("SELECT a FROM Alumno a WHERE a.matriculado = :matriculado ORDER BY a.id")
    List<Alumno> findByMatriculado(@Param("matriculado") boolean matriculado);
    
    @Query(value = "SELECT * FROM usuarios a WHERE " +
           "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:apellidos IS NULL OR normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%') AND " +
           "(:dni IS NULL OR LOWER(a.dni) LIKE '%' || LOWER(:dni) || '%') AND " +
           "(:email IS NULL OR LOWER(a.email) LIKE '%' || LOWER(:email) || '%') AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) AND " +
           "a.tipo_usuario = 'ALUMNO' " +
           "ORDER BY a.id", nativeQuery = true)
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
    
    // metodos con paginación
    @Query("SELECT a FROM Alumno a")
    Page<Alumno> findAllPaged(Pageable pageable);
    
    @Query(value = "SELECT * FROM usuarios a WHERE " +
           "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:apellidos IS NULL OR normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%') AND " +
           "(:dni IS NULL OR LOWER(a.dni) LIKE '%' || LOWER(:dni) || '%') AND " +
           "(:email IS NULL OR LOWER(a.email) LIKE '%' || LOWER(:email) || '%') AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) AND " +
           "a.tipo_usuario = 'ALUMNO'",
           countQuery = "SELECT COUNT(*) FROM usuarios a WHERE " +
           "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:apellidos IS NULL OR normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%') AND " +
           "(:dni IS NULL OR LOWER(a.dni) LIKE '%' || LOWER(:dni) || '%') AND " +
           "(:email IS NULL OR LOWER(a.email) LIKE '%' || LOWER(:email) || '%') AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) AND " +
           "a.tipo_usuario = 'ALUMNO'",
           nativeQuery = true)
    Page<Alumno> findByFiltrosPaged(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("dni") String dni,
        @Param("email") String email,
        @Param("matriculado") Boolean matriculado,
        Pageable pageable
    );
    
    @Query("SELECT a FROM Alumno a WHERE a.matriculado = :matriculado")
    Page<Alumno> findByMatriculadoPaged(@Param("matriculado") boolean matriculado, Pageable pageable);
}
