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
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId")
    List<EntregaEjercicio> findByAlumnoEntreganteId(@Param("alumnoId") String alumnoId);
    
    /**
     * Busca entregas por ID de alumno con paginación
     * @param alumnoId ID del alumno
     * @param pageable Parámetros de paginación
     * @return Página de entregas del alumno
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId")
    Page<EntregaEjercicio> findByAlumnoEntreganteId(@Param("alumnoId") String alumnoId, Pageable pageable);
    
    /**
     * Busca entregas por ID de ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Lista de entregas del ejercicio
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId")
    List<EntregaEjercicio> findByEjercicioId(@Param("ejercicioId") String ejercicioId);
    
    /**
     * Busca entregas por ID de ejercicio con paginación
     * @param ejercicioId ID del ejercicio
     * @param pageable Parámetros de paginación
     * @return Página de entregas del ejercicio
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId")
    Page<EntregaEjercicio> findByEjercicioId(@Param("ejercicioId") String ejercicioId, Pageable pageable);
    
    /**
     * Busca una entrega específica por alumno y ejercicio
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @return Optional<EntregaEjercicio>
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.ejercicioId = :ejercicioId")
    Optional<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioId(@Param("alumnoId") String alumnoId, @Param("ejercicioId") String ejercicioId);
    
    /**
     * Busca entregas por estado
     * @param estado Estado de la entrega
     * @return Lista de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.estado = :estado")
    List<EntregaEjercicio> findByEstado(@Param("estado") EEstadoEjercicio estado);
    
    /**
     * Busca entregas por estado con paginación
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.estado = :estado")
    Page<EntregaEjercicio> findByEstado(@Param("estado") EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por rango de fechas de entrega
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.fechaEntrega BETWEEN :fechaInicio AND :fechaFin")
    List<EntregaEjercicio> findByFechaEntregaBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Busca entregas por rango de notas
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @return Lista de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.nota BETWEEN :notaMin AND :notaMax")
    List<EntregaEjercicio> findByNotaBetween(@Param("notaMin") BigDecimal notaMin, @Param("notaMax") BigDecimal notaMax);
    
    /**
     * Busca entregas por rango de notas con paginación
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.nota BETWEEN :notaMin AND :notaMax")
    Page<EntregaEjercicio> findByNotaBetween(@Param("notaMin") BigDecimal notaMin, @Param("notaMax") BigDecimal notaMax, Pageable pageable);
    
    /**
     * Busca entregas por alumno y ejercicio con paginación
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.ejercicioId = :ejercicioId")
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioId(@Param("alumnoId") String alumnoId, @Param("ejercicioId") String ejercicioId, Pageable pageable);
    
    /**
     * Busca entregas por alumno y estado con paginación
     * @param alumnoId ID del alumno
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.estado = :estado")
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEstado(@Param("alumnoId") String alumnoId, @Param("estado") EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por ejercicio y estado con paginación
     * @param ejercicioId ID del ejercicio
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId AND e.estado = :estado")
    Page<EntregaEjercicio> findByEjercicioIdAndEstado(@Param("ejercicioId") String ejercicioId, @Param("estado") EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por alumno, ejercicio y estado con paginación
     * @param alumnoId ID del alumno
     * @param ejercicioId ID del ejercicio
     * @param estado Estado de la entrega
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.ejercicioId = :ejercicioId AND e.estado = :estado")
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndEjercicioIdAndEstado(@Param("alumnoId") String alumnoId, @Param("ejercicioId") String ejercicioId, @Param("estado") EEstadoEjercicio estado, Pageable pageable);
    
    /**
     * Busca entregas por alumno y rango de notas con paginación
     * @param alumnoId ID del alumno
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.alumnoEntreganteId = :alumnoId AND e.nota BETWEEN :notaMin AND :notaMax")
    Page<EntregaEjercicio> findByAlumnoEntreganteIdAndNotaBetween(@Param("alumnoId") String alumnoId, @Param("notaMin") BigDecimal notaMin, @Param("notaMax") BigDecimal notaMax, Pageable pageable);
    
    /**
     * Busca entregas por ejercicio y rango de notas con paginación
     * @param ejercicioId ID del ejercicio
     * @param notaMin Nota mínima
     * @param notaMax Nota máxima
     * @param pageable Parámetros de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.ejercicioId = :ejercicioId AND e.nota BETWEEN :notaMin AND :notaMax")
    Page<EntregaEjercicio> findByEjercicioIdAndNotaBetween(@Param("ejercicioId") String ejercicioId, @Param("notaMin") BigDecimal notaMin, @Param("notaMax") BigDecimal notaMax, Pageable pageable);
    
    /**
     * Búsqueda flexible de entregas con múltiples filtros opcionales
     * Permite combinar filtros de alumno, ejercicio, estado y notas
     * @param alumnoId Filtro por ID de alumno
     * @param ejercicioId Filtro por ID de ejercicio
     * @param estado Filtro por estado
     * @param notaMin Filtro por nota mínima
     * @param notaMax Filtro por nota máxima
     * @param pageable Configuración de paginación
     * @return Página de entregas
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE " +
           "(:alumnoId IS NULL OR e.alumnoEntreganteId = :alumnoId) AND " +
           "(:ejercicioId IS NULL OR e.ejercicioId = :ejercicioId) AND " +
           "(:estado IS NULL OR e.estado = :estado) AND " +
           "(:notaMin IS NULL OR e.nota >= :notaMin) AND " +
           "(:notaMax IS NULL OR e.nota <= :notaMax)")
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
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.nota IS NOT NULL")
    List<EntregaEjercicio> findByNotaIsNotNull();
    
    /**
     * Busca entregas que no tienen nota (no están calificadas)
     * @return Lista de entregas sin calificar
     */
    @Query("SELECT e FROM EntregaEjercicio e WHERE e.nota IS NULL")
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
