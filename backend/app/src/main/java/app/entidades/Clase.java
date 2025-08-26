package app.entidades;

import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Clase (abstracta)
 * Representa una clase en la plataforma (puede ser Taller o Curso)
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "clases")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_clase", discriminatorType = DiscriminatorType.STRING)
public abstract class Clase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    @Size(max = 200)
    private String title;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EPresencialidad format;
    
    @Size(max = 500)
    private String image;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EDificultad difficulty;
    
    // Relaciones como listas de IDs (según UML)
    // TODO: Estas se refactorizarán a relaciones JPA cuando sea necesario
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clase_alumnos", joinColumns = @JoinColumn(name = "clase_id"))
    @Column(name = "alumno_id")
    private List<String> studentIds = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clase_profesores", joinColumns = @JoinColumn(name = "clase_id"))
    @Column(name = "profesor_id")
    private List<String> teacherIds = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clase_ejercicios", joinColumns = @JoinColumn(name = "clase_id"))
    @Column(name = "ejercicio_id")
    private List<String> exerciseIds = new ArrayList<>();
    
    // Relación Many-to-Many con Material
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "clase_materiales",
        joinColumns = @JoinColumn(name = "clase_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> material = new ArrayList<>();
    
    public Clase() {}
    
    public Clase(String title, String description, BigDecimal price,
                 EPresencialidad format, String image, EDificultad difficulty) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.format = format;
        this.image = image;
        this.difficulty = difficulty;
    }
    
    /**
     * Agrega un alumno a la clase
     * @param alumnoId ID del alumno
     */
    public void agregarAlumno(String alumnoId) {
        if (!this.studentIds.contains(alumnoId)) {
            this.studentIds.add(alumnoId);
        }
    }
    
    /**
     * Remueve un alumno de la clase
     * @param alumnoId ID del alumno
     */
    public void removerAlumno(String alumnoId) {
        this.studentIds.remove(alumnoId);
    }
    
    /**
     * Agrega un profesor a la clase
     * @param profesorId ID del profesor
     */
    public void agregarProfesor(String profesorId) {
        if (!this.teacherIds.contains(profesorId)) {
            this.teacherIds.add(profesorId);
        }
    }
    
    /**
     * Remueve un profesor de la clase
     * @param profesorId ID del profesor
     */
    public void removerProfesor(String profesorId) {
        this.teacherIds.remove(profesorId);
    }
    
    /**
     * Agrega un ejercicio a la clase
     * @param ejercicioId ID del ejercicio
     */
    public void agregarEjercicio(String ejercicioId) {
        if (!this.exerciseIds.contains(ejercicioId)) {
            this.exerciseIds.add(ejercicioId);
        }
    }
    
    /**
     * Remueve un ejercicio de la clase
     * @param ejercicioId ID del ejercicio
     */
    public void removerEjercicio(String ejercicioId) {
        this.exerciseIds.remove(ejercicioId);
    }
    
    /**
     * Agrega material a la clase
     * @param nuevoMaterial Material a agregar
     */
    public void agregarMaterial(Material nuevoMaterial) {
        this.material.add(nuevoMaterial);
    }
    
    /**
     * Remueve material de la clase por ID
     * @param materialId ID del material
     */
    public void removerMaterial(String materialId) {
        this.material.removeIf(m -> m.getId().equals(materialId));
    }
}
