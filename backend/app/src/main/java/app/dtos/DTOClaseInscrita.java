package app.dtos;

import app.entidades.Clase;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para mostrar información detallada de las clases en las que está inscrito un estudiante
 */
public record DTOClaseInscrita(
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
        
        // Información del profesor principal
        DTOProfesor profesor,
        
        // Fecha de inscripción del estudiante
        LocalDateTime fechaInscripcion
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Clase
     */
    public DTOClaseInscrita(Clase clase, DTOProfesor profesor, LocalDateTime fechaInscripcion) {
        this(
                clase.getId(),
                clase.getTitle(),
                clase.getDescription(),
                clase.getPrice(),
                clase.getFormat(),
                clase.getImage(),
                clase.getDifficulty(),
                clase.getStudents().stream()
                    .map(student -> student.getId().toString())
                    .collect(Collectors.toList()),
                clase.getTeachers().stream()
                    .map(teacher -> teacher.getId().toString())
                    .collect(Collectors.toList()),
                clase.getExercises().stream()
                    .map(exercise -> exercise.getId().toString())
                    .collect(Collectors.toList()),
                clase.getMaterial(),
                determinarTipoClase(clase),
                profesor,
                fechaInscripcion
        );
    }
    
    /**
     * Determina el tipo de clase de forma segura sin usar reflection
     */
    private static String determinarTipoClase(Clase clase) {
        if (clase instanceof app.entidades.Curso) {
            return "CURSO";
        } else if (clase instanceof app.entidades.Taller) {
            return "TALLER";
        } else {
            return "CLASE"; // Fallback para la clase abstracta
        }
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
    
    /**
     * Obtiene el número de alumnos inscritos
     */
    public int getNumeroAlumnos() {
        return this.alumnosId != null ? this.alumnosId.size() : 0;
    }
    
    /**
     * Obtiene el número de profesores asignados
     */
    public int getNumeroProfesores() {
        return this.profesoresId != null ? this.profesoresId.size() : 0;
    }
}
