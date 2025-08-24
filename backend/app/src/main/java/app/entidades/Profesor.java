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
    private List<String> clasesId = new ArrayList<>();
    
    /**
     * Obtiene la lista de clases del profesor, inicializando si es null
     * @return Lista de clases
     */
    public List<String> getClasesId() {
        if (this.clasesId == null) {
            this.clasesId = new ArrayList<>();
        }
        return this.clasesId;
    }
    
    public Profesor() {
        super();
        this.setRole(Role.PROFESOR);
    }
    
    public Profesor(String usuario, String password, String nombre, String apellidos, 
                   String dni, String email, String numeroTelefono) {
        super(usuario, password, nombre, apellidos, dni, email, numeroTelefono);
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
     * @param claseId ID de la clase
     */
    public void agregarClase(String claseId) {
        if (this.clasesId == null) {
            this.clasesId = new ArrayList<>();
        }
        if (!this.clasesId.contains(claseId)) {
            this.clasesId.add(claseId);
        }
    }
    
    /**
     * Remueve una clase del profesor
     * @param claseId ID de la clase
     */
    public void removerClase(String claseId) {
        if (this.clasesId != null) {
            this.clasesId.remove(claseId);
        }
    }
    
    /**
     * Verifica si el profesor imparte una clase específica
     * @param claseId ID de la clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClase(String claseId) {
        return this.clasesId != null && this.clasesId.contains(claseId);
    }
    
    /**
     * Obtiene el número de clases que imparte el profesor
     * @return Número de clases
     */
    public int getNumeroClases() {
        return this.clasesId != null ? this.clasesId.size() : 0;
    }
}