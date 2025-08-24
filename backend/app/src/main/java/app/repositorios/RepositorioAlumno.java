package app.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.entidades.Alumno;

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
    
    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.enrolled = :matriculado")
    long countByMatriculado(@Param("matriculado") boolean matriculado);
    
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
    
    @Query("SELECT a FROM Alumno a WHERE a.enrolled = :matriculado")
    Page<Alumno> findByMatriculadoPaged(@Param("matriculado") boolean matriculado, Pageable pageable);
    
    // NEW: General search methods for "q" parameter
    
    /**
     * General search across multiple fields using the "q" parameter
     * Searches in nombre, apellidos, dni, and email fields
     */
    @Query(value = "SELECT * FROM usuarios a WHERE " +
           "(normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
           "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%') AND " +
           "a.tipo_usuario = 'ALUMNO'",
           countQuery = "SELECT COUNT(*) FROM usuarios a WHERE " +
           "(normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
           "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%') AND " +
           "a.tipo_usuario = 'ALUMNO'",
           nativeQuery = true)
    Page<Alumno> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Combined search with general term and specific filters
     */
    @Query(value = "SELECT * FROM usuarios a WHERE " +
           "(:searchTerm IS NULL OR (" +
           "normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
           "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%')) AND " +
           "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:apellidos IS NULL OR normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%') AND " +
           "(:dni IS NULL OR LOWER(a.dni) LIKE '%' || LOWER(:dni) || '%') AND " +
           "(:email IS NULL OR LOWER(a.email) LIKE '%' || LOWER(:email) || '%') AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) AND " +
           "a.tipo_usuario = 'ALUMNO'",
           countQuery = "SELECT COUNT(*) FROM usuarios a WHERE " +
           "(:searchTerm IS NULL OR (" +
           "normalize_text(a.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(a.apellidos) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "LOWER(a.dni) LIKE '%' || LOWER(:searchTerm) || '%' OR " +
           "LOWER(a.email) LIKE '%' || LOWER(:searchTerm) || '%')) AND " +
           "(:nombre IS NULL OR normalize_text(a.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:apellidos IS NULL OR normalize_text(a.apellidos) LIKE '%' || normalize_text(:apellidos) || '%') AND " +
           "(:dni IS NULL OR LOWER(a.dni) LIKE '%' || LOWER(:dni) || '%') AND " +
           "(:email IS NULL OR LOWER(a.email) LIKE '%' || LOWER(:email) || '%') AND " +
           "(:matriculado IS NULL OR a.matriculado = :matriculado) AND " +
           "a.tipo_usuario = 'ALUMNO'",
           nativeQuery = true)
    Page<Alumno> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("dni") String dni,
        @Param("email") String email,
        @Param("matriculado") Boolean matriculado,
        Pageable pageable
    );
}
