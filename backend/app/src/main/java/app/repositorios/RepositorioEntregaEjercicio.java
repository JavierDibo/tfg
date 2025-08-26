package app.repositorios;

import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad EntregaEjercicio
 * Proporciona operaciones de acceso a datos para entregas de ejercicios
 */
@Repository
public interface RepositorioEntregaEjercicio extends JpaRepository<EntregaEjercicio, Long> {
    
    /**
     * Busca entregas por ID de alumno
     * @param alumnoId ID del alumno
     * @return Lista de entregas del alumno
     */
    List<EntregaEjercicio> findByAlumnoEntreganteId(String alumnoId);
    
    /**
     * Busca entregas por ID de alumno con paginación
     * @param alumnoId ID del alumno
     * @param pageable Parámetros de paginación
     * @return Página de entregas del alumno
     */
    Page<EntregaEjercicio> findByAlumnoEntreganteId(String alumnoId, Pageable pageable);
    
    /**
     * Busca entregas por ID de ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Lista de entregas del ejercicio
     */
    List<EntregaEjercicio> findByEjercicioId(String ejercicioId);
    
    /**
     * Busca entregas por ID de ejercicio con paginación
     * @param ejercicioId ID del ejercicio
     * @param pageable Parámetros de paginación
     * @return Página de entregas del ejercicio
     */
    Page<EntregaEjercicio> findByEjercicioId(String ejercicioId, Pageable pageable);
    
    /**
     * Busca una entrega específica por alumno y ejercicio
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @return Optional<EntregaEjercicio>
     */
    Optional<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioId(String alumnoId, String ejercicioId);
    
    /**
     * Busca entregas por estado
     * @param estado Estado de la entrega
     * @return Lista de entregas
     */
    List<EntregaEjercicio> findByEstado(EEstadoEjercicio estado);
    
    /**
     * Busca entregas por estado con paginación
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByEstado(EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por rango de fechas de entrega
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de entregas
     */
    List<EntregaEjercicio> findByFechaEntregaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca entregas por rango de notas
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @return Lista de entregas
     */
    List<EntregaEjercicio> findByNotaBetween(BigDecimal notaMin, BigDecimal notaMax);
    
    /**
     * Busca entregas por rango de notas con paginación
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByNotaBetween(BigDecimal notaMin, BigDecimal notaMax, Pageable pageable);
    
    /**
     * Busca entregas por alumno y ejercicio con paginación
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioId(String alumnoId, String ejercicioId, Pageable pageable);
    
    /**
     * Busca entregas por alumno y estado con paginación
     * @param alumnoId ID del alumno
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEstado(String alumnoId, EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por ejercicio y estado con paginación
     * @param ejercicioId ID del ejercicio
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByEjercicioIdAndEstado(String ejercicioId, EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por alumno, ejercicio y estado con paginación
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioIdAndEstado(String alumnoId, String ejercicioId, EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por alumno y rango de notas con paginación
     * @param alumnoId ID del alumno
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndNotaBetween(String alumnoId, BigDecimal notaMin, BigDecimal notaMax, Pageable pageable);
    
    /**
     * Busca entregas por ejercicio y rango de notas con paginación
     * @param ejercicioId ID del ejercicio
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    Page<EntregaEjercicio> findByEjercicioIdAndNotaBetween(String ejercicioId, BigDecimal notaMin, BigDecimal notaMax, Pageable pageable);
    
    /**
     * Búsqueda flexible de entregas con múltiples filtros opcionales
     * Permite combinar filtros de alumno, ejercicio, estado y notas
     */
    @Query(value = "SELECT * FROM entregas_ejercicios ee WHERE " +
           "(:alumnoId IS NULL OR ee.alumno_entregante_id = :alumnoId) AND " +
           "(:ejercicioId IS NULL OR ee.ejercicio_id = :ejercicioId) AND " +
           "(:estado IS NULL OR ee.estado = :estado) AND " +
           "(:notaMin IS NULL OR ee.nota >= :notaMin) AND " +
           "(:notaMax IS NULL OR ee.nota <= :notaMax)",
           countQuery = "SELECT COUNT(*) FROM entregas_ejercicios ee WHERE " +
           "(:alumnoId IS NULL OR ee.alumno_entregante_id = :alumnoId) AND " +
           "(:ejercicioId IS NULL OR ee.ejercicio_id = :ejercicioId) AND " +
           "(:estado IS NULL OR ee.estado = :estado) AND " +
           "(:notaMin IS NULL OR ee.nota >= :notaMin) AND " +
           "(:notaMax IS NULL OR ee.nota <= :notaMax)",
           nativeQuery = true)
    Page<EntregaEjercicio> findByFiltrosFlexibles(
        @Param("alumnoId") String alumnoId,
        @Param("ejercicioId") String ejercicioId,
        @Param("estado") String estado,
        @Param("notaMin") BigDecimal notaMin,
        @Param("notaMax") BigDecimal notaMax,
        Pageable pageable
    );
    
    /**
     * Busca entregas que tienen nota (están calificadas)
     * @return Lista de entregas calificadas
     */
    List<EntregaEjercicio> findByNotaIsNotNull();
    
    /**
     * Busca entregas que no tienen nota (no están calificadas)
     * @return Lista de entregas sin calificar
     */
    List<EntregaEjercicio> findByNotaIsNull();
    
    /**
     * Obtiene todas las entregas ordenadas por ID
     * @return Lista de entregas ordenada
     */
    @Query("SELECT e FROM EntregaEjercicio e ORDER BY e.id")
    List<EntregaEjercicio> findAllOrderedById();
    
    /**
     * Obtiene todas las entregas ordenadas por fecha de entrega descendente
     * @return Lista de entregas ordenada por fecha
     */
    @Query("SELECT e FROM EntregaEjercicio e ORDER BY e.fechaEntrega DESC")
    List<EntregaEjercicio> findAllOrderedByFechaDesc();
    
    /**
     * Obtiene todas las entregas ordenadas por nota descendente
     * @return Lista de entregas ordenada por nota
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.nota IS NOT NULL ORDER BY e.nota DESC")
    List<EntregaEjercicio> findAllOrderedByNotaDesc();
    
    /**
     * Busca entregas calificadas de un alumno
     * @param alumnoId ID del alumno
     * @return Lista de entregas calificadas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.estado = 'CALIFICADO'")
    List<EntregaEjercicio> findEntregasCalificadasByAlumno(@Param("alumnoId") String alumnoId);
    
    /**
     * Busca entregas pendientes de calificación
     * @return Lista de entregas pendientes
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.estado = 'ENTREGADO'")
    List<EntregaEjercicio> findEntregasPendientesCalificacion();
    
    /**
     * Busca entregas de un ejercicio que están calificadas
     * @param ejercicioId ID del ejercicio
     * @return Lista de entregas calificadas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId AND e.estado = 'CALIFICADO'")
    List<EntregaEjercicio> findEntregasCalificadasByEjercicio(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Calcula la nota promedio de un alumno
     * @param alumnoId ID del alumno
     * @return Nota promedio
     */
    @Query("SELECT AVG(e.nota) FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.nota IS NOT NULL")
    BigDecimal calcularNotaPromedioByAlumno(@Param("alumnoId") String alumnoId);
    
    /**
     * Calcula la nota promedio de un ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Nota promedio
     */
    @Query("SELECT AVG(e.nota) FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId AND e.nota IS NOT NULL")
    BigDecimal calcularNotaPromedioByEjercicio(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Obtiene la nota promedio de un ejercicio (alias para compatibilidad)
     * @param ejercicioId ID del ejercicio
     * @return Nota promedio
     */
    @Query("SELECT AVG(e.nota) FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId AND e.nota IS NOT NULL")
    BigDecimal findAverageGradeByEjercicioId(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Obtiene la nota promedio de un alumno (alias para compatibilidad)
     * @param alumnoId ID del alumno
     * @return Nota promedio
     */
    @Query("SELECT AVG(e.nota) FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.nota IS NOT NULL")
    BigDecimal findAverageGradeByAlumnoId(@Param("alumnoId") String alumnoId);
    
    /**
     * Cuenta entregas por estado
     * @param estado Estado de las entregas
     * @return Número de entregas
     */
    @Query("SELECT COUNT(e) FROM EntregaEjercicio e WHERE e.estado = :estado")
    Long countByEstado(@Param("estado") EEstadoEjercicio estado);
    
    /**
     * Cuenta entregas de un alumno
     * @param alumnoId ID del alumno
     * @return Número de entregas
     */
    @Query("SELECT COUNT(e) FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId")
    Long countByAlumnoEntreganteId(@Param("alumnoId") String alumnoId);
    
    /**
     * Cuenta entregas de un ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Número de entregas
     */
    @Query("SELECT COUNT(e) FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId")
    Long countByEjercicioId(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Cuenta entregas calificadas de un alumno
     * @param alumnoId ID del alumno
     * @return Número de entregas calificadas
     */
    @Query("SELECT COUNT(e) FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.estado = 'CALIFICADO'")
    Long countEntregasCalificadasByAlumno(@Param("alumnoId") String alumnoId);
    
    /**
     * Busca entregas que tienen más de X archivos
     * @param numeroArchivos Número mínimo de archivos
     * @return Lista de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE SIZE(e.archivosEntregados) > :numeroArchivos")
    List<EntregaEjercicio> findEntregasConMasDeXArchivos(@Param("numeroArchivos") int numeroArchivos);
    
    /**
     * Busca entregas sin archivos
     * @return Lista de entregas sin archivos
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE SIZE(e.archivosEntregados) = 0")
    List<EntregaEjercicio> findEntregasSinArchivos();
}
