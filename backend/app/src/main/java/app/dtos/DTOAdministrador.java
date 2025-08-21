package app.dtos;

import app.entidades.Administrador;
import app.entidades.Usuario;

import java.time.LocalDateTime;

/**
 * DTO para la entidad Administrador
 * Contiene la información del administrador sin datos sensibles
 */
public record DTOAdministrador(
        Long id,
        String usuario,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String numeroTelefono,
        Usuario.Rol rol,
        boolean enabled,
        LocalDateTime fechaCreacion
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Administrador
     */
    public DTOAdministrador(Administrador administrador) {
        this(
                administrador.getId(),
                administrador.getUsuario(),
                administrador.getNombre(),
                administrador.getApellidos(),
                administrador.getDni(),
                administrador.getEmail(),
                administrador.getNumeroTelefono(),
                administrador.getRol(),
                administrador.isEnabled(),
                LocalDateTime.now() // placeholder, en una implementación real sería fechaCreacion de la entidad
        );
    }
    
    /**
     * Métod estático para crear desde entidad
     */
    public static DTOAdministrador from(Administrador administrador) {
        return new DTOAdministrador(administrador);
    }
    
    /**
     * Obtiene el nombre completo del administrador
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellidos;
    }
}
