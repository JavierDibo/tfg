package app.repositorios;

import app.entidades.Ejercicio;
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
    Optional<Ejercicio> findByNombre(String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca ejercicios por enunciado (contiene, ignorando mayúsculas)
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByEnunciadoContainingIgnoreCase(String enunciado);
    
    /**
     * Busca ejercicios por ID de clase
     * @param claseId ID de la clase
     * @return Lista de ejercicios de la clase
     */
    List<Ejercicio> findByClaseId(String claseId);
    
    /**
     * Busca ejercicios por rango de fechas de inicio de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByFechaInicioPlazoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca ejercicios por rango de fechas de fin de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByFechaFinalPlazoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
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
    @Query("SELECT e FROM Ejercicio e ORDER BY e.fechaInicioPlazo ASC")
    List<Ejercicio> findAllOrderedByFechaInicio();
    
    /**
     * Obtiene todos los ejercicios ordenados por fecha de fin de plazo
     * @return Lista de ejercicios ordenada por fecha de fin
     */
    @Query("SELECT e FROM Ejercicio e ORDER BY e.fechaFinalPlazo ASC")
    List<Ejercicio> findAllOrderedByFechaFinal();
    
    /**
     * Busca ejercicios que están actualmente en plazo
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios en plazo
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora >= e.fechaInicioPlazo AND :ahora <= e.fechaFinalPlazo")
    List<Ejercicio> findEjerciciosEnPlazo(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que han vencido
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios vencidos
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora > e.fechaFinalPlazo")
    List<Ejercicio> findEjerciciosVencidos(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que aún no han comenzado
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios futuros
     */
    @Query("SELECT e FROM Ejercicio e WHERE :ahora < e.fechaInicioPlazo")
    List<Ejercicio> findEjerciciosFuturos(@Param("ahora") LocalDateTime ahora);
    
    /**
     * Busca ejercicios que vencen en las próximas X horas
     * @param limite Fecha y hora límite
     * @return Lista de ejercicios próximos a vencer
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.fechaFinalPlazo <= :limite AND e.fechaFinalPlazo > CURRENT_TIMESTAMP")
    List<Ejercicio> findEjerciciosProximosAVencer(@Param("limite") LocalDateTime limite);
    
    /**
     * Cuenta ejercicios por clase
     * @param claseId ID de la clase
     * @return Número de ejercicios
     */
    @Query("SELECT COUNT(e) FROM Ejercicio e WHERE e.claseId = :claseId")
    Long countByClaseId(@Param("claseId") String claseId);
    
    /**
     * Busca ejercicios con entregas (que tienen al menos una entrega)
     * @return Lista de ejercicios con entregas
     */
    @Query("SELECT DISTINCT e FROM Ejercicio e WHERE SIZE(e.entregas) > 0")
    List<Ejercicio> findEjerciciosConEntregas();
    
    /**
     * Busca ejercicios sin entregas
     * @return Lista de ejercicios sin entregas
     */
    @Query("SELECT e FROM Ejercicio e WHERE SIZE(e.entregas) = 0")
    List<Ejercicio> findEjerciciosSinEntregas();
    
    /**
     * Cuenta el total de entregas de un ejercicio
     * @param ejercicioId ID del ejercicio
     * @return Número de entregas
     */
    @Query("SELECT SIZE(e.entregas) FROM Ejercicio e WHERE e.id = :ejercicioId")
    Integer countEntregasByEjercicioId(@Param("ejercicioId") Long ejercicioId);
}
