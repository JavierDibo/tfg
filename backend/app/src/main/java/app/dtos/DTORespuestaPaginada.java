package app.dtos;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO para respuestas paginadas que incluye tanto el contenido como los metadatos de paginación.
 * Sigue las mejores prácticas de REST API según Spring Boot.
 * 
 * @param <T> El tipo de contenido de la página
 */
public record DTORespuestaPaginada<T>(
    List<T> content,           // Contenido de la página actual
    DTOMetadatosPaginacion page // Metadatos de paginación
) {
    
    /**
     * Constructor que convierte un Page de Spring Data en DTORespuestaPaginada
     */
    public DTORespuestaPaginada(Page<T> page) {
        this(
            page.getContent(),
            new DTOMetadatosPaginacion(
                page.getNumber(),           // página actual (0-indexed)
                page.getSize(),             // tamaño de página
                page.getTotalElements(),    // total de elementos
                page.getTotalPages(),       // total de páginas
                page.isFirst(),             // es la primera página
                page.isLast(),              // es la última página
                page.hasNext(),             // tiene página siguiente
                page.hasPrevious()          // tiene página anterior
            )
        );
    }
    
    /**
     * Record para los metadatos de paginación siguiendo las convenciones de Spring Boot
     */
    public record DTOMetadatosPaginacion(
        int number,                    // Número de página actual (0-indexed)
        int size,                      // Tamaño de la página
        long totalElements,            // Total de elementos en todas las páginas
        int totalPages,                // Total de páginas
        boolean first,                 // Indica si es la primera página
        boolean last,                  // Indica si es la última página
        boolean hasNext,               // Indica si hay página siguiente
        boolean hasPrevious            // Indica si hay página anterior
    ) {}
}
