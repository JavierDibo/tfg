package app.repositorios;

import app.entidades.Pago;
import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Pago
 * Proporciona operaciones de acceso a datos para pagos
 */
@Repository
public interface RepositorioPago extends JpaRepository<Pago, Long> {
    
    /**
     * Busca pagos por ID de alumno
     * @param alumnoId ID del alumno
     * @return Lista de pagos del alumno
     */
    List<Pago> findByAlumnoId(String alumnoId);
    
    /**
     * Busca pagos por metodo de pago
     * @param metodoPago metodo de pago
     * @return Lista de pagos
     */
    List<Pago> findByMetodoPago(EMetodoPago metodoPago);
    
    /**
     * Busca pagos por estado
     * @param estado Estado del pago
     * @return Lista de pagos
     */
    List<Pago> findByEstado(EEstadoPago estado);
    
    /**
     * Busca pagos por rango de fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de pagos
     */
    List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca pagos por rango de importe
     * @param importeMin Importe mínimo
     * @param importeMax Importe máximo
     * @return Lista de pagos
     */
    List<Pago> findByImporteBetween(BigDecimal importeMin, BigDecimal importeMax);
    
    /**
     * Busca pagos que tienen factura creada
     * @param facturaCreada true para buscar con factura, false para sin factura
     * @return Lista de pagos
     */
    List<Pago> findByFacturaCreada(Boolean facturaCreada);
    
    /**
     * Obtiene todos los pagos ordenados por ID
     * @return Lista de pagos ordenada
     */
    @Query("SELECT p FROM Pago p ORDER BY p.id")
    List<Pago> findAllOrderedById();
    
    /**
     * Obtiene todos los pagos ordenados por fecha de pago descendente
     * @return Lista de pagos ordenada por fecha
     */
    @Query("SELECT p FROM Pago p ORDER BY p.fechaPago DESC")
    List<Pago> findAllOrderedByFechaDesc();
    
    /**
     * Obtiene todos los pagos ordenados por importe descendente
     * @return Lista de pagos ordenada por importe
     */
    @Query("SELECT p FROM Pago p ORDER BY p.importe DESC")
    List<Pago> findAllOrderedByImporteDesc();
    
    /**
     * Busca pagos exitosos de un alumno
     * @param alumnoId ID del alumno
     * @return Lista de pagos exitosos
     */
    @Query("SELECT p FROM Pago p WHERE p.alumnoId = :alumnoId AND p.estado = 'EXITO'")
    List<Pago> findPagosExitososByAlumnoId(@Param("alumnoId") String alumnoId);
    
    /**
     * Busca pagos que pueden ser facturados (exitosos y sin factura)
     * @return Lista de pagos facturables
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'EXITO' AND p.facturaCreada = false")
    List<Pago> findPagosFacturables();
    
    /**
     * Busca pagos manuales (efectivo) de un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de pagos manuales
     */
    @Query("SELECT p FROM Pago p WHERE p.metodoPago = 'EFECTIVO' AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    List<Pago> findPagosManualesByPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                         @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Calcula el total de ingresos de un alumno
     * @param alumnoId ID del alumno
     * @return Suma total de pagos exitosos
     */
    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM Pago p WHERE p.alumnoId = :alumnoId AND p.estado = 'EXITO'")
    BigDecimal calcularTotalIngresosByAlumno(@Param("alumnoId") String alumnoId);
    
    /**
     * Calcula el total de ingresos en un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Suma total de pagos exitosos en el período
     */
    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM Pago p WHERE p.estado = 'EXITO' AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalIngresosByPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                            @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Cuenta pagos por metodo de pago
     * @param metodoPago metodo de pago
     * @return Número de pagos
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.metodoPago = :metodoPago")
    Long countByMetodoPago(@Param("metodoPago") EMetodoPago metodoPago);
    
    /**
     * Cuenta pagos exitosos de un alumno
     * @param alumnoId ID del alumno
     * @return Número de pagos exitosos
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.alumnoId = :alumnoId AND p.estado = 'EXITO'")
    Long countPagosExitososByAlumno(@Param("alumnoId") String alumnoId);
}
