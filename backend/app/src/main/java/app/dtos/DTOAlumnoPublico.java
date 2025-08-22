package app.dtos;

import app.entidades.Alumno;
import jakarta.validation.constraints.*;

/**
 * DTO para información pública del alumno (vista para otros estudiantes)
 * Solo muestra nombre y apellidos, ocultando información sensible como email, ID, etc.
 */
public record DTOAlumnoPublico(
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,
    
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    String apellidos
) {

    /**
     * Constructor que crea un DTO desde una entidad Alumno
     * Solo incluye información pública (nombre y apellidos)
     */
    public DTOAlumnoPublico(Alumno alumno) {
        this(
            alumno.getNombre(),
            alumno.getApellidos()
        );
    }
    
    /**
     * método estático para crear desde entidad
     */
    public static DTOAlumnoPublico from(Alumno alumno) {
        return new DTOAlumnoPublico(alumno);
    }
    
    /**
     * Obtiene el nombre completo del alumno
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellidos;
    }
}
