package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Ejercicio
 * Representa un ejercicio asignado a una clase
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "ejercicios")
public class Ejercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    @Size(max = 200)
    private String nombre;
    
    @NotNull
    @Size(max = 2000)
    private String enunciado;
    
    @NotNull
    private LocalDateTime fechaInicioPlazo;
    
    @NotNull
    private LocalDateTime fechaFinalPlazo;
    
    @NotNull
    @Size(max = 255)
    private String claseId;
    
    // Relación con entregas - por ahora como lista de entidades
    // TODO: Esta relación se puede refactorizar según necesidades
    @OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntregaEjercicio> entregas = new ArrayList<>();
    
    public Ejercicio() {}
    
    public Ejercicio(String nombre, String enunciado, LocalDateTime fechaInicioPlazo, 
                    LocalDateTime fechaFinalPlazo, String claseId) {
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.fechaInicioPlazo = fechaInicioPlazo;
        this.fechaFinalPlazo = fechaFinalPlazo;
        this.claseId = claseId;
        
        // Validación: la fecha final debe ser posterior a la inicial
        if (fechaFinalPlazo.isBefore(fechaInicioPlazo)) {
            throw new IllegalArgumentException("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }
    }
    
    /**
     * Verifica si el ejercicio está actualmente disponible para entrega
     * @return true si está en plazo, false en caso contrario
     */
    public boolean estaEnPlazo() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(this.fechaInicioPlazo) && !ahora.isAfter(this.fechaFinalPlazo);
    }
    
    /**
     * Verifica si el plazo del ejercicio ya ha vencido
     * @return true si ha vencido, false en caso contrario
     */
    public boolean haVencido() {
        return LocalDateTime.now().isAfter(this.fechaFinalPlazo);
    }
    
    /**
     * Verifica si el ejercicio aún no ha comenzado
     * @return true si no ha comenzado, false en caso contrario
     */
    public boolean noHaComenzado() {
        return LocalDateTime.now().isBefore(this.fechaInicioPlazo);
    }
    
    /**
     * Obtiene la entrega de un alumno específico
     * @param alumnoId ID del alumno
     * @return EntregaEjercicio si existe, null en caso contrario
     */
    public EntregaEjercicio obtenerEntregaDeAlumno(String alumnoId) {
        return this.entregas.stream()
                .filter(entrega -> entrega.getAlumnoEntreganteId().equals(alumnoId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Cuenta el número de entregas realizadas
     * @return Número de entregas
     */
    public int contarEntregas() {
        return this.entregas.size();
    }
    
    /**
     * Cuenta el número de entregas calificadas
     * @return Número de entregas calificadas
     */
    public long contarEntregasCalificadas() {
        return this.entregas.stream()
                .filter(entrega -> entrega.getEstado() == app.entidades.enums.EEstadoEjercicio.CALIFICADO)
                .count();
    }
    
    /**
     * Agrega una entrega al ejercicio
     * @param entrega EntregaEjercicio a agregar
     */
    public void agregarEntrega(EntregaEjercicio entrega) {
        if (entrega.getEjercicio() != this) {
            entrega.setEjercicio(this);
        }
        this.entregas.add(entrega);
    }
    
    /**
     * Setter personalizado para fechaFinalPlazo que valida que sea posterior a fechaInicioPlazo
     */
    public void setFechaFinalPlazo(LocalDateTime fechaFinalPlazo) {
        if (this.fechaInicioPlazo != null && fechaFinalPlazo.isBefore(this.fechaInicioPlazo)) {
            throw new IllegalArgumentException("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }
        this.fechaFinalPlazo = fechaFinalPlazo;
    }
}
