package app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Administrador
 * Representa a un administrador en la plataforma
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {
    
    public Administrador() {
        super();
        this.setRol(Rol.ADMIN);
    }
    
    public Administrador(String usuario, String password, String nombre, String apellidos, 
                        String dni, String email, String numeroTelefono) {
        super(usuario, password, nombre, apellidos, dni, email, numeroTelefono);
        this.setRol(Rol.ADMIN);
    }
    
    /**
     * Método para resetear password según UML
     * TODO: Implementar lógica de reseteo por email
     */
    public void resetearpassword() {
        // TODO: Implementar según especificaciones del proyecto
        throw new UnsupportedOperationException("Método resetearpassword por implementar");
    }
}
