package app.dtos;

import app.validation.*;
import jakarta.validation.constraints.*;

/**
 * DTO para actualizaciones parciales de alumno (PATCH)
 * Todos los campos son opcionales para permitir actualizaciones parciales
 */
public record DTOActualizacionAlumno(
    @NotEmptyIfPresent(message = "El nombre no puede estar vacío si se proporciona")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    String nombre,
    
    @NotEmptyIfPresent(message = "Los apellidos no pueden estar vacíos si se proporcionan")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los apellidos solo pueden contener letras y espacios")
    String apellidos,
    
    @NotEmptyIfPresent(message = "El DNI no puede estar vacío si se proporciona")
    @ValidDNI
    String dni,
    
    @NotEmptyIfPresent(message = "El email no puede estar vacío si se proporciona")
    @ValidEmail
    String email,
    
    @ValidPhone
    String numeroTelefono
) {
}
