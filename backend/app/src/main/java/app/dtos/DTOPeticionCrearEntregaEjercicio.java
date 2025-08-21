package app.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para la petición de creación de una entrega de ejercicio
 * Contiene los datos necesarios para que un alumno entregue un ejercicio
 */
public record DTOPeticionCrearEntregaEjercicio(
        @NotNull
        @Size(max = 255)
        String alumnoEntreganteId,
        
        @NotNull
        @Size(max = 255)
        String ejercicioId,
        
        List<String> archivosEntregados
) {
    
    /**
     * Constructor por defecto con lista vacía de archivos
     */
    public DTOPeticionCrearEntregaEjercicio(String alumnoEntreganteId, String ejercicioId) {
        this(alumnoEntreganteId, ejercicioId, List.of());
    }
    
    /**
     * Verifica si la entrega tiene archivos
     */
    public boolean tieneArchivos() {
        return archivosEntregados != null && !archivosEntregados.isEmpty();
    }
    
    /**
     * Obtiene el número de archivos
     */
    public int getNumeroArchivos() {
        return archivosEntregados != null ? archivosEntregados.size() : 0;
    }
}
