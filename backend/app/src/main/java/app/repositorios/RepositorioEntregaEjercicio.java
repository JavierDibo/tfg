package app.repositorios;

import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
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
     * Busca entregas por ID de ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Lista de entregas del ejercicio
     */
    List<EntregaEjercicio> findByEjercicioId(String ejercicioId);
    
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
    Long countByAlumnoId(@Param("alumnoId") String alumnoId);
    
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
