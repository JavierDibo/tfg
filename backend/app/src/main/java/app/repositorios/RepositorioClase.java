package app.repositorios;

import app.entidades.Clase;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repositorio para la entidad Clase
 * Proporciona operaciones de acceso a datos para clases (Talleres y Cursos)
 */
@Repository
public interface RepositorioClase extends JpaRepository<Clase, Long> {
    
    /**
     * Busca una clase por su título
     * @param title Título de la clase
     * @return Optional<Clase>
     */
    Optional<Clase> findByTitle(String title);
    
    /**
     * Busca clases por título (contiene, ignorando mayúsculas)
     * @param title Título a buscar
     * @return Lista de clases
     */
    List<Clase> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Busca clases por descripción (contiene, ignorando mayúsculas)
     * @param description Descripción a buscar
     * @return Lista de clases
     */
    List<Clase> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Busca clases por presencialidad
     * @param format Tipo de presencialidad
     * @return Lista de clases
     */
    List<Clase> findByFormat(EPresencialidad format);
    
    /**
     * Busca clases por nivel
     * @param difficulty Nivel de la clase
     * @return Lista de clases
     */
    List<Clase> findByDifficulty(EDificultad difficulty);
    
    /**
     * Busca clases por rango de precio
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de clases
     */
    List<Clase> findByPriceBetween(BigDecimal precioMin, BigDecimal precioMax);
    
    /**
     * Busca clases con precio menor o igual al especificado
     * @param precio Precio máximo
     * @return Lista de clases
     */
    List<Clase> findByPriceLessThanEqual(BigDecimal precio);
    
    /**
     * Obtiene todas las clases ordenadas por ID
     * @return Lista de clases ordenada
     */
    @Query("SELECT c FROM Clase c ORDER BY c.id")
    List<Clase> findAllOrderedById();
    
    /**
     * Obtiene todas las clases ordenadas por precio ascendente
     * @return Lista de clases ordenada por precio
     */
    @Query("SELECT c FROM Clase c ORDER BY c.price ASC")
    List<Clase> findAllOrderedByPrecioAsc();
    
    /**
     * Obtiene todas las clases ordenadas por título
     * @return Lista de clases ordenada por título
     */
    @Query("SELECT c FROM Clase c ORDER BY c.title ASC")
    List<Clase> findAllOrderedByTitulo();
    
    // NEW: General search methods for "q" parameter
    
    /**
     * General search across multiple fields using the "q" parameter
     * Searches in title and description fields
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<Clase> findByGeneralSearch(@Param("searchTerm") String searchTerm);
    
    /**
     * General search with pagination
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    Page<Clase> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Combined search with general term and specific filters
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "(:searchTerm IS NULL OR (" +
           "UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%')))) AND " +
           "(:title IS NULL OR UPPER(c.title) LIKE UPPER(CONCAT('%', :title, '%'))) AND " +
           "(:description IS NULL OR UPPER(c.description) LIKE UPPER(CONCAT('%', :description, '%'))) AND " +
           "(:format IS NULL OR c.format = :format) AND " +
           "(:difficulty IS NULL OR c.difficulty = :difficulty) AND " +
           "(:precioMinimo IS NULL OR c.price >= :precioMinimo) AND " +
           "(:precioMaximo IS NULL OR c.price <= :precioMaximo)")
    Page<Clase> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("title") String title,
        @Param("description") String description,
        @Param("format") EPresencialidad format,
        @Param("difficulty") EDificultad difficulty,
        @Param("precioMinimo") BigDecimal precioMinimo,
        @Param("precioMaximo") BigDecimal precioMaximo,
        Pageable pageable
    );
    
    /**
     * Busca clases que tienen un alumno específico inscrito
     * @param alumnoId ID del alumno
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE :alumnoId MEMBER OF c.studentIds")
    List<Clase> findByAlumnoId(@Param("alumnoId") String alumnoId);
    
    /**
     * Busca clases que tienen un profesor específico asignado
     * @param profesorId ID del profesor
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE :profesorId MEMBER OF c.teacherIds")
    List<Clase> findByProfesorId(@Param("profesorId") String profesorId);
    
    /**
     * Busca clases que tienen un ejercicio específico
     * @param ejercicioId ID del ejercicio
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE :ejercicioId MEMBER OF c.exerciseIds")
    List<Clase> findByEjercicioId(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Cuenta el número de alumnos inscritos en una clase
     * @param claseId ID de la clase
     * @return Número de alumnos
     */
    @Query("SELECT SIZE(c.studentIds) FROM Clase c WHERE c.id = :claseId")
    Integer countAlumnosByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Cuenta el número de profesores asignados a una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    @Query("SELECT SIZE(c.teacherIds) FROM Clase c WHERE c.id = :claseId")
    Integer countProfesoresByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Busca clases que no tienen alumnos inscritos
     * @return Lista de clases sin alumnos
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.studentIds) = 0")
    List<Clase> findClasesSinAlumnos();
    
    /**
     * Busca clases que no tienen profesores asignados
     * @return Lista de clases sin profesores
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.teacherIds) = 0")
    List<Clase> findClasesSinProfesores();
}
