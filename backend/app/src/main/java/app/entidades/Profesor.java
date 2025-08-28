package app.entidades;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Profesor
 * Representa a un profesor en la plataforma
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("PROFESOR")
public class Profesor extends Usuario {
    
    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
    private List<Clase> classes = new ArrayList<>();
    
    // TODO: Legacy ID-based fields for backward compatibility (to be removed after migration)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profesor_clases", joinColumns = @JoinColumn(name = "profesor_id"))
    @Column(name = "clase_id")
    private List<String> classIds = new ArrayList<>();
    
    /**
     * Obtiene la lista de clases del profesor, inicializando si es null
     * @return Lista de clases
     */
    public List<String> getClassIds() {
        if (this.classIds == null) {
            this.classIds = new ArrayList<>();
        }
        return this.classIds;
    }
    
    public Profesor() {
        super();
        this.setRole(Role.PROFESOR);
    }
    
    public Profesor(String username, String password, String firstName, String lastName, 
                   String dni, String email, String phoneNumber) {
        super(username, password, firstName, lastName, dni, email, phoneNumber);
        this.setRole(Role.PROFESOR);
    }
    
    /**
     * metodo para resetear password según UML
     * TODO: Implementar lógica de reseteo por email
     */
    public void resetearpassword() {
        // TODO: Implementar según especificaciones del proyecto
        throw new UnsupportedOperationException("metodo resetearpassword por implementar");
    }
    
    /**
     * Agrega una clase al profesor usando relación JPA
     * @param clase Entidad Clase
     */
    public void agregarClase(Clase clase) {
        if (!this.classes.contains(clase)) {
            this.classes.add(clase);
            clase.getTeachers().add(this); // Maintain bidirectional relationship
        }
    }
    
    /**
     * Remueve una clase del profesor usando relación JPA
     * @param clase Entidad Clase
     */
    public void removerClase(Clase clase) {
        this.classes.remove(clase);
        clase.getTeachers().remove(this);
    }
    
    /**
     * Verifica si el profesor imparte una clase específica usando relación JPA
     * @param clase Entidad Clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClase(Clase clase) {
        return this.classes.contains(clase);
    }
    
    /**
     * Verifica si el profesor imparte una clase específica por ID
     * @param claseId ID de la clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClasePorId(Long claseId) {
        return this.classes.stream().anyMatch(c -> c.getId().equals(claseId));
    }
    
    /**
     * Obtiene el número de clases que imparte el profesor usando relación JPA
     * @return Número de clases
     */
    public int getNumeroClases() {
        return this.classes.size();
    }
    

}