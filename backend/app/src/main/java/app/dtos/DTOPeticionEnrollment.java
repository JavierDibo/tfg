package app.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para la petición de inscripción de un alumno en una clase
 * Utilizado por profesores para gestionar las inscripciones de sus clases
 */
public record DTOPeticionEnrollment(
        @NotNull(message = "El ID del alumno no puede ser nulo")
        @Positive(message = "El ID del alumno debe ser positivo")
        Long alumnoId,
        
        @NotNull(message = "El ID de la clase no puede ser nulo")
        @Positive(message = "El ID de la clase debe ser positivo")
        Long claseId
) {
    
    /**
     * Constructor por defecto
     */
    public DTOPeticionEnrollment {
        // Validaciones adicionales si es necesario
    }
}
