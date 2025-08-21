package app.dtos;

import app.entidades.Curso;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.ENivel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * DTO específico para la entidad Curso
 * Extiende la información de clase con datos específicos del curso
 */
public record DTOCurso(
        Long id,
        String titulo,
        String descripcion,
        BigDecimal precio,
        EPresencialidad presencialidad,
        String imagenPortada,
        ENivel nivel,
        List<String> alumnosId,
        List<String> profesoresId,
        List<String> ejerciciosId,
        List<Material> material,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Curso
     */
    public DTOCurso(Curso curso) {
        this(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescripcion(),
                curso.getPrecio(),
                curso.getPresencialidad(),
                curso.getImagenPortada(),
                curso.getNivel(),
                curso.getAlumnosId(),
                curso.getProfesoresId(),
                curso.getEjerciciosId(),
                curso.getMaterial(),
                curso.getFechaInicio(),
                curso.getFechaFin()
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOCurso from(Curso curso) {
        return new DTOCurso(curso);
    }
    
    /**
     * Calcula la duración del curso en días
     */
    public long getDuracionEnDias() {
        return ChronoUnit.DAYS.between(this.fechaInicio, this.fechaFin.plusDays(1));
    }
    
    /**
     * Calcula la duración del curso en semanas
     */
    public long getDuracionEnSemanas() {
        long dias = getDuracionEnDias();
        return (dias + 6) / 7; // Redondeo hacia arriba
    }
    
    /**
     * Verifica si el curso está actualmente en progreso
     */
    public boolean estaEnProgreso() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
    
    /**
     * Verifica si el curso ya ha finalizado
     */
    public boolean haFinalizado() {
        return LocalDate.now().isAfter(this.fechaFin);
    }
    
    /**
     * Verifica si el curso aún no ha comenzado
     */
    public boolean noHaComenzado() {
        return LocalDate.now().isBefore(this.fechaInicio);
    }
    
    /**
     * Calcula el porcentaje de progreso del curso
     */
    public double getPorcentajeProgreso() {
        LocalDate hoy = LocalDate.now();
        
        if (noHaComenzado()) {
            return 0.0;
        }
        
        if (haFinalizado()) {
            return 100.0;
        }
        
        long diasTranscurridos = ChronoUnit.DAYS.between(this.fechaInicio, hoy);
        long duracionTotal = getDuracionEnDias();
        
        return (diasTranscurridos * 100.0) / duracionTotal;
    }
    
    /**
     * Verifica si el curso es próximo a comenzar (en los próximos 7 días)
     */
    public boolean esProximoAComenzar() {
        if (haFinalizado() || estaEnProgreso()) {
            return false;
        }
        
        LocalDate limite = LocalDate.now().plusDays(7);
        return this.fechaInicio.isBefore(limite.plusDays(1));
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
