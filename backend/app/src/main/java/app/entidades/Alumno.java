package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad Alumno
 * Representa a un alumno en la plataforma
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ALUMNO")
public class Alumno extends Usuario {
    
    @NotNull
    @Column(name = "fecha_inscripcion")
    private LocalDateTime fechaInscripcion = LocalDateTime.now();
    
    @NotNull
    private boolean matriculado = false;
    
    // TODO: Relaciones a implementar más adelante según UML:
    // - clasesId: List<string>
    // - pagosId: List<string> 
    // - entregasId: List<string>
    
    public Alumno() {
        super();
        this.setRol(Rol.ALUMNO);
    }
    
    public Alumno(String usuario, String password, String nombre, String apellidos, 
                  String dni, String email, String numeroTelefono) {
        super(usuario, password, nombre, apellidos, dni, email, numeroTelefono);
        this.setRol(Rol.ALUMNO);
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
