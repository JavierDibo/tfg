package app.entidades;

import app.entidades.enums.EEstadoEjercicio;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad EntregaEjercicio
 * Representa la entrega de un ejercicio por parte de un alumno
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "entregas_ejercicio")
public class EntregaEjercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    @Column(precision = 4, scale = 2)
    private BigDecimal nota;
    
    @NotNull
    private LocalDateTime fechaEntrega;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EEstadoEjercicio estado;
    
    // Representación simplificada de archivos como strings
    // TODO: En una implementación real se podría usar un sistema de archivos o URLs
    @ElementCollection
    @CollectionTable(name = "entrega_archivos", joinColumns = @JoinColumn(name = "entrega_id"))
    @Column(name = "archivo_path")
    private List<String> archivosEntregados = new ArrayList<>();
    
    @NotNull
    @Size(max = 255)
    private String alumnoEntreganteId;
    
    @NotNull
    @Size(max = 255)
    private String ejercicioId;
    
    // Relación bidireccional con Ejercicio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_entity_id")
    private Ejercicio ejercicio;
    
    public EntregaEjercicio() {
        this.fechaEntrega = LocalDateTime.now();
        this.estado = EEstadoEjercicio.ENTREGADO;
    }
    
    public EntregaEjercicio(String alumnoEntreganteId, String ejercicioId) {
        this();
        this.alumnoEntreganteId = alumnoEntreganteId;
        this.ejercicioId = ejercicioId;
    }
    
    /**
     * Agrega un archivo a la entrega
     * @param archivoPath Path o nombre del archivo
     */
    public void agregarArchivo(String archivoPath) {
        if (!this.archivosEntregados.contains(archivoPath)) {
            this.archivosEntregados.add(archivoPath);
        }
    }
    
    /**
     * Remueve un archivo de la entrega
     * @param archivoPath Path o nombre del archivo
     */
    public void removerArchivo(String archivoPath) {
        this.archivosEntregados.remove(archivoPath);
    }
    
    /**
     * Califica la entrega
     * @param nota Nota a asignar (0-10)
     */
    public void calificar(BigDecimal nota) {
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }
        this.nota = nota;
        this.estado = EEstadoEjercicio.CALIFICADO;
    }
    
    /**
     * Verifica si la entrega está calificada
     * @return true si está calificada, false en caso contrario
     */
    public boolean estaCalificada() {
        return this.estado == EEstadoEjercicio.CALIFICADO && this.nota != null;
    }
    
    /**
     * Verifica si la entrega fue realizada fuera de plazo
     * @param ejercicioRef Ejercicio de referencia para verificar plazo
     * @return true si fue entregada fuera de plazo, false en caso contrario
     */
    public boolean esFueraDePlazo(Ejercicio ejercicioRef) {
        return this.fechaEntrega.isAfter(ejercicioRef.getFechaFinalPlazo());
    }
    
    /**
     * Obtiene el número de archivos entregados
     * @return Número de archivos
     */
    public int contarArchivos() {
        return this.archivosEntregados.size();
    }
    
    /**
     * Marca la entrega como pendiente (permite modificaciones)
     */
    public void marcarComoPendiente() {
        if (this.estado == EEstadoEjercicio.CALIFICADO) {
            throw new IllegalStateException("No se puede marcar como pendiente una entrega ya calificada");
        }
        this.estado = EEstadoEjercicio.PENDIENTE;
    }
    
    /**
     * Confirma la entrega
     */
    public void confirmarEntrega() {
        if (this.archivosEntregados.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar una entrega sin archivos");
        }
        this.estado = EEstadoEjercicio.ENTREGADO;
        this.fechaEntrega = LocalDateTime.now();
    }
}
