package app.dtos;

import app.entidades.Profesor;
import app.entidades.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la entidad Profesor
 * Contiene la información del profesor sin datos sensibles
 */
public record DTOProfesor(
        Long id,
        String usuario,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String numeroTelefono,
        Usuario.Rol rol,
        boolean enabled,
        List<String> clasesId,
        LocalDateTime fechaCreacion
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Profesor
     */
    public DTOProfesor(Profesor profesor) {
        this(
                profesor.getId(),
                profesor.getUsuario(),
                profesor.getNombre(),
                profesor.getApellidos(),
                profesor.getDni(),
                profesor.getEmail(),
                profesor.getNumeroTelefono(),
                profesor.getRol(),
                profesor.isEnabled(),
                profesor.getClasesId(),
                LocalDateTime.now() // placeholder, en una implementación real sería fechaCreacion de la entidad
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOProfesor from(Profesor profesor) {
        return new DTOProfesor(profesor);
    }
    
    /**
     * Obtiene el nombre completo del profesor
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellidos;
    }
    
    /**
     * Verifica si el profesor tiene clases asignadas
     */
    public boolean tieneClases() {
        return this.clasesId != null && !this.clasesId.isEmpty();
    }
    
    /**
     * Cuenta el número de clases asignadas
     */
    public int getNumeroClases() {
        return this.clasesId != null ? this.clasesId.size() : 0;
    }
}
