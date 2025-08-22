package app.dtos;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de inscripción de un alumno en una clase
 * Proporciona información detallada sobre el resultado de la operación
 */
public record DTORespuestaEnrollment(
        boolean success,
        String message,
        Long alumnoId,
        Long claseId,
        String nombreAlumno,
        String tituloClase,
        LocalDateTime fechaOperacion,
        String tipoOperacion // "ENROLLMENT" o "UNENROLLMENT"
) {
    
    /**
     * Constructor para una operación exitosa
     */
    public static DTORespuestaEnrollment success(Long alumnoId, Long claseId, 
                                                String nombreAlumno, String tituloClase, 
                                                String tipoOperacion) {
        return new DTORespuestaEnrollment(
                true,
                "Operación realizada con éxito",
                alumnoId,
                claseId,
                nombreAlumno,
                tituloClase,
                LocalDateTime.now(),
                tipoOperacion
        );
    }
    
    /**
     * Constructor para una operación fallida
     */
    public static DTORespuestaEnrollment failure(Long alumnoId, Long claseId, 
                                                String message, String tipoOperacion) {
        return new DTORespuestaEnrollment(
                false,
                message,
                alumnoId,
                claseId,
                null,
                null,
                LocalDateTime.now(),
                tipoOperacion
        );
    }
}
