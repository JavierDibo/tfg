package app.repositorios;

import app.entidades.Ejercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ejercicio
 * Proporciona operaciones de acceso a datos para ejercicios
 */
@Repository
public interface RepositorioEjercicio extends JpaRepository<Ejercicio, Long> {
    
    /**
     * Busca un ejercicio por su nombre
     * @param nombre Nombre del ejercicio
     * @return Optional<Ejercicio>
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.name = :nombre")
    Optional<Ejercicio> findByName(@Param("nombre") String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Ejercicio> findByNameContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, ignorando mayúsculas) con paginación
     * @param nombre Nombre a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    Page<Ejercicio> findByNameContainingIgnoreCase(@Param("nombre") String nombre, Pageable pageable);
    
    /**
     * Busca ejercicios por enunciado (contiene, ignorando mayúsculas)
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))")
    List<Ejercicio> findByStatementContainingIgnoreCase(@Param("enunciado") String enunciado);
    
    /**
     * Busca ejercicios por enunciado (contiene, ignorando mayúsculas) con paginación
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))")
    Page<Ejercicio> findByStatementContainingIgnoreCase(@Param("enunciado") String enunciado, Pageable pageable);
    
    /**
     * Busca ejercicios por nombre o enunciado (contiene, ignorando mayúsculas) con paginación
     * @param nombre Nombre a buscar
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE " +
           "UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%')) OR " +
           "UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))")
    Page<Ejercicio> findByNameContainingIgnoreCaseOrStatementContainingIgnoreCase(
        @Param("nombre") String nombre, 
        @Param("enunciado") String enunciado, 
        Pageable pageable
    );
    
    /**
     * Busca ejercicios por ID de clase
     * @param claseId ID de la clase
     * @return Lista de ejercicios de la clase
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    List<Ejercicio> findByClassId(@Param("claseId") String claseId);
    
    /**
     * Busca ejercicios por ID de clase con paginación
     * @param claseId ID de la clase
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    Page<Ejercicio> findByClassId(@Param("claseId") String claseId, Pageable pageable);
    
    /**
     * Búsqueda flexible de ejercicios con múltiples filtros opcionales
     * Permite combinar filtros de nombre, enunciado, clase, estado y fechas
     * @param nombre Filtro por nombre
     * @param enunciado Filtro por enunciado
     * @param classId Filtro por ID de clase
     * @param status Filtro por estado
     * @param now Fecha actual para cálculos de estado
     * @param pageable Configuración de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE " +
           "(:nombre IS NULL OR UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%'))) AND " +
           "(:enunciado IS NULL OR UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))) AND " +
           "(:classId IS NULL OR e.clase.id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.endDate > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.endDate <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.startDate > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN SIZE(e.entregas) > 0 " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN SIZE(e.entregas) = 0 " +
           "       ELSE TRUE END))")
    Page<Ejercicio> findByFiltrosFlexibles(
        @Param("nombre") String nombre,
        @Param("enunciado") String enunciado,
        @Param("classId") String classId,
        @Param("status") String status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
    
    /**
     * Búsqueda general con término de búsqueda "q" que busca en nombre y enunciado
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE " +
           "UPPER(e.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(e.statement) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    Page<Ejercicio> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Búsqueda combinada con término general y filtros específicos
     * @param searchTerm Término de búsqueda general
     * @param nombre Filtro por nombre
     * @param enunciado Filtro por enunciado
     * @param classId Filtro por ID de clase
     * @param status Filtro por estado
     * @param now Fecha actual para cálculos de estado
     * @param pageable Configuración de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE " +
           "(:searchTerm IS NULL OR (" +
           "UPPER(e.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(e.statement) LIKE UPPER(CONCAT('%', :searchTerm, '%')))) AND " +
           "(:nombre IS NULL OR UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%'))) AND " +
           "(:enunciado IS NULL OR UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))) AND " +
           "(:classId IS NULL OR e.clase.id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.endDate > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.endDate <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.startDate > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN SIZE(e.entregas) > 0 " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN SIZE(e.entregas) = 0 " +
           "       ELSE TRUE END))")
    Page<Ejercicio> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("nombre") String nombre,
        @Param("enunciado") String enunciado,
        @Param("classId") String classId,
        @Param("status") String status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
    
    /**
     * Busca ejercicios por rango de fechas de inicio de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.startDate BETWEEN :fechaInicio AND :fechaFin")
    List<Ejercicio> findByStartDateBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca ejercicios por rango de fechas de fin de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.endDate BETWEEN :fechaInicio AND :fechaFin")
    List<Ejercicio> findByEndDateBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Obtiene todos los ejercicios ordenados por ID
     * @return Lista de ejercicios ordenada
     */
    @Query("SELECT e FROM Ejercicio e ORDER BY e.id")
    List<Ejercicio> findAllOrderedById();
    
    /**
     * Obtiene todos los ejercicios ordenados por fecha de inicio de plazo
     * @return Lista de ejercicios ordenada por fecha
     */
    @Query("SELECT e FROM Ejercicio e ORDER BY e.startDate ASC")
    List<Ejercicio> findAllOrderedByFechaInicio();
    
    /**
     * Obtiene todos los ejercicios ordenados por fecha de fin de plazo
     * @return Lista de ejercicios ordenada por fecha de fin
     */
    @Query("SELECT e FROM Ejercicio e ORDER BY e.endDate ASC")
    List<Ejercicio> findAllOrderedByFechaFinal();
    
    /**
     * Busca ejercicios que están actualmente en plazo
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios en plazo
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora >= e.startDate AND :ahora <= e.endDate")
    List<Ejercicio> findEjerciciosEnPlazo(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que están actualmente en plazo con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios en plazo
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora >= e.startDate AND :ahora <= e.endDate")
    Page<Ejercicio> findEjerciciosEnPlazo(@Param("ahora") LocalDateTime ahora, Pageable pageable);
    
    /**
     * Busca ejercicios que han vencido
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios vencidos
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora > e.endDate")
    List<Ejercicio> findEjerciciosVencidos(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que han vencido con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios vencidos
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora > e.endDate")
    Page<Ejercicio> findEjerciciosVencidos(@Param("ahora") LocalDateTime ahora, Pageable pageable);
    
    /**
     * Busca ejercicios que aún no han comenzado
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios futuros
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora < e.startDate")
    List<Ejercicio> findEjerciciosFuturos(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que aún no han comenzado con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios futuros
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora < e.startDate")
    Page<Ejercicio> findEjerciciosFuturos(@Param("ahora") LocalDateTime ahora, Pageable pageable);
    
    /**
     * Busca ejercicios que vencen en las próximas X horas
     * @param limite Fecha y hora límite
     * @return Lista de ejercicios próximos a vencer
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.endDate <= :limite AND e.endDate > CURRENT_TIMESTAMP")
    List<Ejercicio> findEjerciciosProximosAVencer(@Param("limite") LocalDateTime limite);
    
    /**
     * Cuenta ejercicios por clase
     * @param claseId ID de la clase
     * @return Número de ejercicios
     */
    @Query("SELECT COUNT(e) FROM Ejercicio e WHERE e.clase.id = :claseId")
    Long countByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Busca ejercicios con entregas (que tienen al menos una entrega)
     * @return Lista de ejercicios con entregas
     */
    @Query("SELECT DISTINCT e FROM Ejercicio e WHERE SIZE(e.entregas) > 0")
    List<Ejercicio> findEjerciciosConEntregas();
    
    /**
     * Busca ejercicios con entregas (que tienen al menos una entrega) con paginación
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios con entregas
     */
    @Query("SELECT DISTINCT e FROM Ejercicio e WHERE SIZE(e.entregas) > 0")
    Page<Ejercicio> findEjerciciosConEntregas(Pageable pageable);
    
    /**
     * Busca ejercicios sin entregas
     * @return Lista de ejercicios sin entregas
     */
    @Query("SELECT e FROM Ejercicio e WHERE SIZE(e.entregas) = 0")
    List<Ejercicio> findEjerciciosSinEntregas();
    
    /**
     * Busca ejercicios sin entregas con paginación
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios sin entregas
     */
    @Query("SELECT e FROM Ejercicio e WHERE SIZE(e.entregas) = 0")
    Page<Ejercicio> findEjerciciosSinEntregas(Pageable pageable);
    
    /**
     * Cuenta el total de entregas de un ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Número de entregas
     */
    @Query("SELECT SIZE(e.entregas) FROM Ejercicio e WHERE e.id = :ejercicioId")
    Integer countEntregasByEjercicioId(@Param("ejercicioId") Long ejercicioId);

    /**
     * Busca ejercicios por clase
     * @param claseId ID de la clase
     * @return Lista de ejercicios de la clase
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    List<Ejercicio> findByClaseId(@Param("claseId") Long claseId);

    /**
     * Busca ejercicios por clase con paginación
     * @param claseId ID de la clase
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios de la clase
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    Page<Ejercicio> findByClaseId(@Param("claseId") Long claseId, Pageable pageable);

    /**
     * Busca un ejercicio por ID con todas sus relaciones cargadas
     * @param ejercicioId ID del ejercicio
     * @return Optional<Ejercicio> con relaciones cargadas
     */
    @Query("SELECT DISTINCT e FROM Ejercicio e " +
           "LEFT JOIN FETCH e.clase " +
           "LEFT JOIN FETCH e.entregas " +
           "WHERE e.id = :ejercicioId")
    Optional<Ejercicio> findByIdWithRelationships(@Param("ejercicioId") Long ejercicioId);

    // ========== ENTITY GRAPH METHODS ==========

    /**
     * Busca un ejercicio por ID con clase cargada usando EntityGraph
     * @param ejercicioId ID del ejercicio
     * @return Optional<Ejercicio> con clase cargada
     */
    @EntityGraph(value = "Ejercicio.withClase")
    Optional<Ejercicio> findByIdWithClase(@Param("ejercicioId") Long ejercicioId);

    /**
     * Busca un ejercicio por ID con entregas cargadas usando EntityGraph
     * @param ejercicioId ID del ejercicio
     * @return Optional<Ejercicio> con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    Optional<Ejercicio> findByIdWithEntregas(@Param("ejercicioId") Long ejercicioId);

    /**
     * Busca un ejercicio por ID con todas sus relaciones cargadas usando EntityGraph
     * @param ejercicioId ID del ejercicio
     * @return Optional<Ejercicio> con todas las relaciones cargadas
     */
    @EntityGraph(value = "Ejercicio.withAllRelationships")
    Optional<Ejercicio> findByIdWithAllRelationships(@Param("ejercicioId") Long ejercicioId);

    /**
     * Busca ejercicios por clase con entregas cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de ejercicios con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    List<Ejercicio> findByClaseIdWithEntregas(@Param("claseId") Long claseId);

    /**
     * Busca ejercicios por clase con clase cargada usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de ejercicios con clase cargada
     */
    @EntityGraph(value = "Ejercicio.withClase")
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    List<Ejercicio> findByClaseIdWithClase(@Param("claseId") Long claseId);

    /**
     * Busca ejercicios por clase con todas las relaciones cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de ejercicios con todas las relaciones cargadas
     */
    @EntityGraph(value = "Ejercicio.withAllRelationships")
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    List<Ejercicio> findByClaseIdWithAllRelationships(@Param("claseId") Long claseId);

    /**
     * Busca ejercicios en plazo con entregas cargadas usando EntityGraph
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios en plazo con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    @Query("SELECT e FROM Ejercicio e WHERE :ahora >= e.startDate AND :ahora <= e.endDate")
    List<Ejercicio> findEjerciciosEnPlazoWithEntregas(@Param("ahora") LocalDateTime ahora);

    /**
     * Busca ejercicios vencidos con entregas cargadas usando EntityGraph
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios vencidos con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    @Query("SELECT e FROM Ejercicio e WHERE :ahora > e.endDate")
    List<Ejercicio> findEjerciciosVencidosWithEntregas(@Param("ahora") LocalDateTime ahora);

    /**
     * Busca ejercicios con entregas usando EntityGraph
     * @return Lista de ejercicios con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    @Query("SELECT DISTINCT e FROM Ejercicio e WHERE SIZE(e.entregas) > 0")
    List<Ejercicio> findEjerciciosConEntregasWithEntregas();

    /**
     * Busca ejercicios por nombre con clase cargada usando EntityGraph
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios con clase cargada
     */
    @EntityGraph(value = "Ejercicio.withClase")
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.name) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Ejercicio> findByNameContainingIgnoreCaseWithClase(@Param("nombre") String nombre);

    /**
     * Busca ejercicios por enunciado con clase cargada usando EntityGraph
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios con clase cargada
     */
    @EntityGraph(value = "Ejercicio.withClase")
    @Query("SELECT e FROM Ejercicio e WHERE UPPER(e.statement) LIKE UPPER(CONCAT('%', :enunciado, '%'))")
    List<Ejercicio> findByStatementContainingIgnoreCaseWithClase(@Param("enunciado") String enunciado);

    /**
     * Obtiene todos los ejercicios con clase cargada usando EntityGraph
     * @return Lista de ejercicios con clase cargada
     */
    @EntityGraph(value = "Ejercicio.withClase")
    List<Ejercicio> findAllWithClase();

    /**
     * Obtiene todos los ejercicios con entregas cargadas usando EntityGraph
     * @return Lista de ejercicios con entregas cargadas
     */
    @EntityGraph(value = "Ejercicio.withEntregas")
    List<Ejercicio> findAllWithEntregas();

    /**
     * Obtiene todos los ejercicios con todas las relaciones cargadas usando EntityGraph
     * @return Lista de ejercicios con todas las relaciones cargadas
     */
    @EntityGraph(value = "Ejercicio.withAllRelationships")
    List<Ejercicio> findAllWithAllRelationships();
}
