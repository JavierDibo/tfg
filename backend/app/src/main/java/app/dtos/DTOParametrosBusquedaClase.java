package app.dtos;

import app.entidades.enums.EPresencialidad;
import app.entidades.enums.ENivel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para parámetros de búsqueda de clases
 * Contiene filtros y criterios de ordenación
 */
public record DTOParametrosBusquedaClase(
        // Filtros de búsqueda
        String titulo,
        String descripcion,
        EPresencialidad presencialidad,
        ENivel nivel,
        BigDecimal precioMinimo,
        BigDecimal precioMaximo,
        Boolean soloConPlazasDisponibles,
        Boolean soloProximas,
        
        // Parámetros de paginación
        @NotNull
        @Min(value = 0, message = "La página debe ser >= 0")
        Integer pagina,
        
        @NotNull
        @Min(value = 1, message = "El tamaño de página debe ser >= 1")
        Integer tamanoPagina,
        
        // Parámetros de ordenación
        @NotNull
        @Size(min = 2, message = "El campo de ordenación no puede estar vacío")
        String ordenCampo,
        
        @NotNull
        String ordenDireccion
) {
    /**
     * Constructor con valores por defecto para paginación y ordenación
     */
    public DTOParametrosBusquedaClase(String titulo, String descripcion, EPresencialidad presencialidad,
                                     ENivel nivel, BigDecimal precioMinimo, BigDecimal precioMaximo,
                                     Boolean soloConPlazasDisponibles, Boolean soloProximas) {
        this(titulo, descripcion, presencialidad, nivel, precioMinimo, precioMaximo,
             soloConPlazasDisponibles, soloProximas, 0, 10, "id", "ASC");
    }
    
    /**
     * Constructor con valores mínimos requeridos para paginación
     */
    public DTOParametrosBusquedaClase(Integer pagina, Integer tamanoPagina) {
        this(null, null, null, null, null, null, null, null,
             pagina, tamanoPagina, "id", "ASC");
    }
    
    /**
     * Constructor con valores mínimos requeridos para paginación y ordenación
     */
    public DTOParametrosBusquedaClase(Integer pagina, Integer tamanoPagina, String ordenCampo, String ordenDireccion) {
        this(null, null, null, null, null, null, null, null,
             pagina, tamanoPagina, ordenCampo, ordenDireccion);
    }
    
    /**
     * Constructor por defecto para casos donde no se necesitan parámetros específicos
     */
    public DTOParametrosBusquedaClase() {
        this(null, null, null, null, null, null, null, null,
             0, 10, "id", "ASC");
    }
}
