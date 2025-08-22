package app.dtos;

import app.entidades.Alumno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO para información pública del alumno (vista para otros estudiantes)
 * Solo muestra nombre y apellidos, ocultando información sensible como email, ID, etc.
 */
@Schema(description = "Información pública del alumno (solo nombre y apellidos)")
public record DTOAlumnoPublico(
    @Schema(description = "Nombre del alumno", example = "Juan")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,
    
    @Schema(description = "Apellidos del alumno", example = "García López")
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
