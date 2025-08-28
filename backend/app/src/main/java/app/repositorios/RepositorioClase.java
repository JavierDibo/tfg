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
import org.springframework.data.jpa.repository.EntityGraph;

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
    @Query("SELECT c FROM Clase c WHERE c.title = :title")
    Optional<Clase> findByTitle(@Param("title") String title);
    
    /**
     * Busca clases por título (contiene, ignorando mayúsculas)
     * @param title Título a buscar
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE UPPER(c.title) LIKE UPPER(CONCAT('%', :title, '%'))")
    List<Clase> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    /**
     * Busca clases por descripción (contiene, ignorando mayúsculas)
     * @param description Descripción a buscar
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE UPPER(c.description) LIKE UPPER(CONCAT('%', :description, '%'))")
    List<Clase> findByDescriptionContainingIgnoreCase(@Param("description") String description);
    
    /**
     * Busca clases por presencialidad
     * @param format Tipo de presencialidad
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE c.format = :format")
    List<Clase> findByFormat(@Param("format") EPresencialidad format);
    
    /**
     * Busca clases por nivel
     * @param difficulty Nivel de la clase
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE c.difficulty = :difficulty")
    List<Clase> findByDifficulty(@Param("difficulty") EDificultad difficulty);
    
    /**
     * Busca clases por rango de precio
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE c.price BETWEEN :precioMin AND :precioMax")
    List<Clase> findByPriceBetween(@Param("precioMin") BigDecimal precioMin, @Param("precioMax") BigDecimal precioMax);
    
    /**
     * Busca clases con precio menor o igual al especificado
     * @param precio Precio máximo
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE c.price <= :precio")
    List<Clase> findByPriceLessThanEqual(@Param("precio") BigDecimal precio);
    
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
    
    /**
     * General search across multiple fields using the "q" parameter
     * Searches in title and description fields
     * @param searchTerm Término de búsqueda
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<Clase> findByGeneralSearch(@Param("searchTerm") String searchTerm);
    
    /**
     * General search with pagination
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de clases
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    Page<Clase> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Combined search with general term and specific filters
     * @param searchTerm Término de búsqueda general
     * @param title Filtro por título
     * @param description Filtro por descripción
     * @param format Filtro por formato
     * @param difficulty Filtro por dificultad
     * @param precioMinimo Filtro por precio mínimo
     * @param precioMaximo Filtro por precio máximo
     * @param pageable Configuración de paginación
     * @return Página de clases
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
    @Query("SELECT c FROM Clase c JOIN c.students s WHERE s.id = :alumnoId")
    List<Clase> findByAlumnoId(@Param("alumnoId") Long alumnoId);
    
    /**
     * Busca clases que tienen un profesor específico asignado
     * @param profesorId ID del profesor
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c JOIN c.teachers t WHERE t.id = :profesorId")
    List<Clase> findByProfesorId(@Param("profesorId") Long profesorId);
    
    /**
     * Busca clases que tienen un ejercicio específico
     * @param ejercicioId ID del ejercicio
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c JOIN c.exercises e WHERE e.id = :ejercicioId")
    List<Clase> findByEjercicioId(@Param("ejercicioId") Long ejercicioId);
    
    /**
     * Cuenta el número de alumnos inscritos en una clase
     * @param claseId ID de la clase
     * @return Número de alumnos
     */
    @Query("SELECT COUNT(s) FROM Clase c JOIN c.students s WHERE c.id = :claseId")
    Integer countAlumnosByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Cuenta el número de profesores asignados a una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    @Query("SELECT COUNT(t) FROM Clase c JOIN c.teachers t WHERE c.id = :claseId")
    Integer countProfesoresByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Busca clases que no tienen alumnos inscritos
     * @return Lista de clases sin alumnos
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.students) = 0")
    List<Clase> findClasesSinAlumnos();
    
    /**
     * Busca clases que no tienen profesores asignados
     * @return Lista de clases sin profesores
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.teachers) = 0")
    List<Clase> findClasesSinProfesores();

    /**
     * Busca una clase por ID con todas sus relaciones cargadas
     * @param claseId ID de la clase
     * @return Optional<Clase> con relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c WHERE c.id = :claseId")
    Optional<Clase> findByIdWithRelationships(@Param("claseId") Long claseId);

    /**
     * Busca todas las clases con sus relaciones cargadas
     * @return Lista de clases con relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c")
    List<Clase> findAllWithRelationships();

    /**
     * Busca todas las clases con estudiantes y profesores cargados (optimizado)
     * @return Lista de clases con relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c")
    List<Clase> findAllForDashboard();

    /**
     * Busca todas las clases con ejercicios cargados (optimizado)
     * @return Lista de clases con ejercicios cargados
     */
    @EntityGraph(value = "Clase.withExercises")
    @Query("SELECT c FROM Clase c")
    List<Clase> findAllForExerciseManagement();

    /**
     * Busca una clase por ID con todas sus relaciones cargadas (optimizado)
     * @param claseId ID de la clase
     * @return Optional<Clase> con todas las relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c WHERE c.id = :claseId")
    Optional<Clase> findByIdWithAllRelationships(@Param("claseId") Long claseId);

    /**
     * Busca clases por profesor con relaciones cargadas (optimizado)
     * @param profesorId ID del profesor
     * @return Lista de clases con relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c JOIN c.teachers t WHERE t.id = :profesorId")
    List<Clase> findByProfesorIdWithRelationships(@Param("profesorId") Long profesorId);

    /**
     * Busca clases por alumno con relaciones cargadas (optimizado)
     * @param alumnoId ID del alumno
     * @return Lista de clases con relaciones cargadas
     */
    @EntityGraph(value = "Clase.withStudentsAndTeachers")
    @Query("SELECT c FROM Clase c JOIN c.students s WHERE s.id = :alumnoId")
    List<Clase> findByAlumnoIdWithRelationships(@Param("alumnoId") Long alumnoId);
}
