package app.repositorios;

import app.entidades.Ejercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    Optional<Ejercicio> findByName(String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByNameContainingIgnoreCase(String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, ignorando mayúsculas) con paginación
     * @param nombre Nombre a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByNameContainingIgnoreCase(String nombre, Pageable pageable);
    
    /**
     * Busca ejercicios por enunciado (contiene, ignorando mayúsculas)
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByStatementContainingIgnoreCase(String enunciado);
    
    /**
     * Busca ejercicios por enunciado (contiene, ignorando mayúsculas) con paginación
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByStatementContainingIgnoreCase(String enunciado, Pageable pageable);
    
    /**
     * Busca ejercicios por nombre o enunciado (contiene, ignorando mayúsculas) con paginación
     * @param nombre Nombre a buscar
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByNameContainingIgnoreCaseOrStatementContainingIgnoreCase(String nombre, String enunciado, Pageable pageable);
    
    /**
     * Busca ejercicios por ID de clase
     * @param claseId ID de la clase
     * @return Lista de ejercicios de la clase
     */
    List<Ejercicio> findByClassId(String claseId);
    
    /**
     * Busca ejercicios por ID de clase con paginación
     * @param claseId ID de la clase
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByClassId(String claseId, Pageable pageable);
    
    /**
     * Búsqueda flexible de ejercicios con múltiples filtros opcionales
     * Permite combinar filtros de nombre, enunciado, clase, estado y fechas
     */
    @Query(value = "SELECT * FROM ejercicios e WHERE " +
           "(:nombre IS NULL OR normalize_text(e.name) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:enunciado IS NULL OR normalize_text(e.statement) LIKE '%' || normalize_text(:enunciado) || '%') AND " +
           "(:classId IS NULL OR e.class_id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.end_date > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.end_date <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.start_date > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN NOT EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       ELSE TRUE END))",
           countQuery = "SELECT COUNT(*) FROM ejercicios e WHERE " +
           "(:nombre IS NULL OR normalize_text(e.name) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:enunciado IS NULL OR normalize_text(e.statement) LIKE '%' || normalize_text(:enunciado) || '%') AND " +
           "(:classId IS NULL OR e.class_id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.end_date > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.end_date <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.start_date > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN NOT EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       ELSE TRUE END))",
           nativeQuery = true)
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
     */
    @Query(value = "SELECT * FROM ejercicios e WHERE " +
           "(normalize_text(e.name) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(e.statement) LIKE '%' || normalize_text(:searchTerm) || '%')",
           countQuery = "SELECT COUNT(*) FROM ejercicios e WHERE " +
           "(normalize_text(e.name) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(e.statement) LIKE '%' || normalize_text(:searchTerm) || '%')",
           nativeQuery = true)
    Page<Ejercicio> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Búsqueda combinada con término general y filtros específicos
     */
    @Query(value = "SELECT * FROM ejercicios e WHERE " +
           "(:searchTerm IS NULL OR (" +
           "normalize_text(e.name) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(e.statement) LIKE '%' || normalize_text(:searchTerm) || '%')) AND " +
           "(:nombre IS NULL OR normalize_text(e.name) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:enunciado IS NULL OR normalize_text(e.statement) LIKE '%' || normalize_text(:enunciado) || '%') AND " +
           "(:classId IS NULL OR e.class_id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.end_date > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.end_date <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.start_date > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN NOT EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       ELSE TRUE END))",
           countQuery = "SELECT COUNT(*) FROM ejercicios e WHERE " +
           "(:searchTerm IS NULL OR (" +
           "normalize_text(e.name) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
           "normalize_text(e.statement) LIKE '%' || normalize_text(:searchTerm) || '%')) AND " +
           "(:nombre IS NULL OR normalize_text(e.name) LIKE '%' || normalize_text(:nombre) || '%') AND " +
           "(:enunciado IS NULL OR normalize_text(e.statement) LIKE '%' || normalize_text(:enunciado) || '%') AND " +
           "(:classId IS NULL OR e.class_id = :classId) AND " +
           "(:status IS NULL OR (" +
           "  CASE WHEN :status = 'ACTIVE' THEN e.end_date > :now " +
           "       WHEN :status = 'EXPIRED' THEN e.end_date <= :now " +
           "       WHEN :status = 'FUTURE' THEN e.start_date > :now " +
           "       WHEN :status = 'WITH_DELIVERIES' THEN EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       WHEN :status = 'WITHOUT_DELIVERIES' THEN NOT EXISTS (SELECT 1 FROM entregas_ejercicio ee WHERE ee.ejercicio_entity_id = e.id) " +
           "       ELSE TRUE END))",
           nativeQuery = true)
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
    List<Ejercicio> findByStartDateBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca ejercicios por rango de fechas de fin de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByEndDateBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
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
    @Query("SELECT COUNT(e) FROM Ejercicio e WHERE e.classId = :claseId")
    Long countByClaseId(@Param("claseId") String claseId);
    
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
}
