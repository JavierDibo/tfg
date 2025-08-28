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
    @Column(name = "name", length = 200, nullable = false)
    private String name;
    
    @NotNull
    @Size(max = 2000)
    @Column(name = "statement", length = 2000, nullable = false, columnDefinition = "VARCHAR(2000)")
    private String statement;
    
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id")
    private Clase clase;
    
    // TODO: Legacy ID-based field for backward compatibility (to be removed after migration)
    @NotNull
    @Size(max = 255)
    @Column(name = "class_id", length = 255, nullable = false)
    private String classId;
    
    // Relación con entregas - por ahora como lista de entidades
    // TODO: Esta relación se puede refactorizar según necesidades
    @OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntregaEjercicio> entregas = new ArrayList<>();
    
    public Ejercicio() {}
    
    public Ejercicio(String name, String statement, LocalDateTime startDate, 
                    LocalDateTime endDate, String classId) {
        this.name = name;
        this.statement = statement;
        this.startDate = startDate;
        this.endDate = endDate;
        this.classId = classId;
        
        // Validación: la fecha final debe ser posterior a la inicial
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }
    }
    
    public Ejercicio(String name, String statement, LocalDateTime startDate, 
                    LocalDateTime endDate, Clase clase) {
        this.name = name;
        this.statement = statement;
        this.startDate = startDate;
        this.endDate = endDate;
        this.clase = clase;
        this.classId = clase != null ? clase.getId().toString() : null;
        
        // Validación: la fecha final debe ser posterior a la inicial
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }
    }
    
    /**
     * Verifica si el ejercicio está actualmente disponible para entrega
     * @return true si está en plazo, false en caso contrario
     */
    public boolean estaEnPlazo() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(this.startDate) && !ahora.isAfter(this.endDate);
    }
    
    /**
     * Verifica si el plazo del ejercicio ya ha vencido
     * @return true si ha vencido, false en caso contrario
     */
    public boolean haVencido() {
        return LocalDateTime.now().isAfter(this.endDate);
    }
    
    /**
     * Verifica si el ejercicio aún no ha comenzado
     * @return true si no ha comenzado, false en caso contrario
     */
    public boolean noHaComenzado() {
        return LocalDateTime.now().isBefore(this.startDate);
    }
    
    /**
     * Obtiene la entrega de un alumno específico usando JPA relationship
     * @param alumno Entidad Alumno
     * @return EntregaEjercicio si existe, null en caso contrario
     */
    public EntregaEjercicio obtenerEntregaDeAlumno(Alumno alumno) {
        return this.entregas.stream()
                .filter(entrega -> entrega.getAlumno().equals(alumno))
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
     * Setter personalizado para endDate que valida que sea posterior a startDate
     */
    public void setEndDate(LocalDateTime endDate) {
        if (this.startDate != null && endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }
        this.endDate = endDate;
    }
}
