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
    
    // Lista de clases que imparte el profesor
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
     * Agrega una clase al profesor
     * @param classId ID de la clase
     */
    public void agregarClase(String classId) {
        if (this.classIds == null) {
            this.classIds = new ArrayList<>();
        }
        if (!this.classIds.contains(classId)) {
            this.classIds.add(classId);
        }
    }
    
    /**
     * Remueve una clase del profesor
     * @param classId ID de la clase
     */
    public void removerClase(String classId) {
        if (this.classIds != null) {
            this.classIds.remove(classId);
        }
    }
    
    /**
     * Verifica si el profesor imparte una clase específica
     * @param classId ID de la clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClase(String classId) {
        return this.classIds != null && this.classIds.contains(classId);
    }
    
    /**
     * Obtiene el número de clases que imparte el profesor
     * @return Número de clases
     */
    public int getNumeroClases() {
        return this.classIds != null ? this.classIds.size() : 0;
    }
}