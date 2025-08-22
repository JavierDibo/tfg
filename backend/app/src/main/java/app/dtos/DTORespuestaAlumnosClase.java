package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO específico para respuestas de alumnos de una clase
 * Puede contener información completa (DTOAlumno) o pública (DTOAlumnoPublico)
 * según el nivel de acceso del usuario
 */
@Schema(description = "Respuesta paginada de alumnos de una clase con diferentes niveles de información según el rol del usuario")
public record DTORespuestaAlumnosClase(
    @Schema(description = "Lista de alumnos (información completa o pública según el rol)")
    List<Object> content,
    
    @Schema(description = "Metadatos de paginación")
    DTOMetadatosPaginacion page,
    
    @Schema(description = "Tipo de información devuelta: 'COMPLETA' para admin/profesor de la clase, 'PUBLICA' para otros")
    String tipoInformacion
) {
    
    /**
     * Constructor que convierte un Page de Spring Data en DTORespuestaAlumnosClase
     * @param page Page con alumnos (DTOAlumno o DTOAlumnoPublico)
     * @param tipoInformacion Tipo de información devuelta
     */
    public DTORespuestaAlumnosClase(Page<?> page, String tipoInformacion) {
        this(
            (List<Object>) page.getContent(),
            new DTOMetadatosPaginacion(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
            ),
            tipoInformacion
        );
    }
    
    /**
     * Record para los metadatos de paginación
     */
    public record DTOMetadatosPaginacion(
        @Schema(description = "Número de página actual (0-indexed)")
        int number,
        
        @Schema(description = "Tamaño de la página")
        int size,
        
        @Schema(description = "Total de elementos en todas las páginas")
        long totalElements,
        
        @Schema(description = "Total de páginas")
        int totalPages,
        
        @Schema(description = "Indica si es la primera página")
        boolean first,
        
        @Schema(description = "Indica si es la última página")
        boolean last,
        
        @Schema(description = "Indica si hay página siguiente")
        boolean hasNext,
        
        @Schema(description = "Indica si hay página anterior")
        boolean hasPrevious
    ) {}
    
    /**
     * Constantes para el tipo de información
     */
    public static final String TIPO_COMPLETA = "COMPLETA";
    public static final String TIPO_PUBLICA = "PUBLICA";
}
