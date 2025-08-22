package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta paginada genérica")
public record DTORespuestaPaginada<T>(
    @Schema(description = "Lista de elementos de la página actual", required = true)
    List<T> contenido,
    
    @Schema(description = "Número de página actual (0-indexed)", example = "0", required = true)
    int numeroPagina,
    
    @Schema(description = "Tamaño de la página", example = "20", required = true)
    int tamanoPagina,
    
    @Schema(description = "Número total de elementos", example = "150", required = true)
    long totalElementos,
    
    @Schema(description = "Número total de páginas", example = "8", required = true)
    int totalPaginas,
    
    @Schema(description = "Indica si es la primera página", example = "true", required = true)
    boolean esPrimera,
    
    @Schema(description = "Indica si es la última página", example = "false", required = true)
    boolean esUltima,
    
    @Schema(description = "Indica si la página tiene contenido", example = "true", required = true)
    boolean tieneContenido,
    
    @Schema(description = "Campo por el que está ordenado", example = "id", required = true)
    String ordenadoPor,
    
    @Schema(description = "Dirección de ordenación", example = "ASC", required = true)
    String direccionOrden
) {
    public static <T> DTORespuestaPaginada<T> of(
            List<T> contenido,
            int numeroPagina,
            int tamanoPagina,
            long totalElementos,
            String ordenadoPor,
            String direccionOrden) {
        
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);
        boolean esPrimera = numeroPagina == 0;
        boolean esUltima = numeroPagina >= totalPaginas - 1;
        boolean tieneContenido = !contenido.isEmpty();
        
        return new DTORespuestaPaginada<>(
                contenido,
                numeroPagina,
                tamanoPagina,
                totalElementos,
                totalPaginas,
                esPrimera,
                esUltima,
                tieneContenido,
                ordenadoPor,
                direccionOrden
        );
    }
}
