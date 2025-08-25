// LLM_EDIT_TIMESTAMP: 25 ago. 13:59
package app.dtos;

import app.entidades.Clase;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para mostrar información detallada de una clase con información pública del profesor
 * y estado de inscripción para estudiantes
 */
public record DTOClaseConDetallesPublico(
        Long id,
        String titulo,
        String descripcion,
        BigDecimal precio,
        EPresencialidad presencialidad,
        String imagenPortada,
        EDificultad nivel,
        List<String> alumnosId,
        List<String> profesoresId,
        List<String> ejerciciosId,
        List<Material> material,
        String tipoClase, // "TALLER" o "CURSO"
        
        // Información pública del profesor principal
        DTOProfesorPublico profesor,
        
        // Estado de inscripción para el estudiante actual
        boolean isEnrolled,
        LocalDateTime fechaInscripcion,
        
        // Información adicional
        int alumnosCount,
        int profesoresCount
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Clase con información adicional
     */
    public DTOClaseConDetallesPublico(Clase clase, DTOProfesorPublico profesor, boolean isEnrolled, 
                               LocalDateTime fechaInscripcion, int alumnosCount, int profesoresCount) {
        this(
                clase.getId(),
                clase.getTitle(),
                clase.getDescription(),
                clase.getPrice(),
                clase.getFormat(),
                clase.getImage(),
                clase.getDifficulty(),
                clase.getStudentIds(),
                clase.getTeacherIds(),
                clase.getExerciseIds(),
                clase.getMaterial(),
                clase.getClass().getAnnotation(jakarta.persistence.DiscriminatorValue.class).value(),
                profesor,
                isEnrolled,
                fechaInscripcion,
                alumnosCount,
                profesoresCount
        );
    }
    
    /**
     * Verifica si la clase es online
     */
    public boolean esOnline() {
        return this.presencialidad == EPresencialidad.ONLINE;
    }
    
    /**
     * Verifica si la clase es presencial
     */
    public boolean esPresencial() {
        return this.presencialidad == EPresencialidad.PRESENCIAL;
    }
    
    /**
     * Verifica si es un taller
     */
    public boolean esTaller() {
        return "TALLER".equals(this.tipoClase);
    }
    
    /**
     * Verifica si es un curso
     */
    public boolean esCurso() {
        return "CURSO".equals(this.tipoClase);
    }
}
