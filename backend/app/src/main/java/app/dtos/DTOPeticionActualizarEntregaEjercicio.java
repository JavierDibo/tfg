package app.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de actualización de una entrega de ejercicio
 * Contiene los datos necesarios para actualizar una entrega, incluyendo calificación
 */
public record DTOPeticionActualizarEntregaEjercicio(
        @Size(max = 255)
        String alumnoEntreganteId,
        
        @Size(max = 255)
        String ejercicioId,
        
        List<String> archivosEntregados,
        
        @DecimalMin("0.0")
        @DecimalMax("10.0")
        BigDecimal nota,
        
        @Size(max = 1000)
        String comentarios
) {
    
    /**
     * Constructor para actualización sin calificación
     */
    public DTOPeticionActualizarEntregaEjercicio(String alumnoEntreganteId, String ejercicioId, List<String> archivosEntregados) {
        this(alumnoEntreganteId, ejercicioId, archivosEntregados, null, null);
    }
    
    /**
     * Constructor para calificación únicamente
     */
    public DTOPeticionActualizarEntregaEjercicio(BigDecimal nota) {
        this(null, null, null, nota, null);
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
    
    /**
     * Verifica si la entrega tiene calificación
     */
    public boolean tieneCalificacion() {
        return nota != null;
    }
    
    /**
     * Verifica si la nota es válida (entre 0 y 10)
     */
    public boolean notaValida() {
        return nota == null || (nota.compareTo(BigDecimal.ZERO) >= 0 && nota.compareTo(BigDecimal.TEN) <= 0);
    }
    
    /**
     * Verifica si la entrega tiene comentarios
     */
    public boolean tieneComentarios() {
        return comentarios != null && !comentarios.trim().isEmpty();
    }
    
    /**
     * Obtiene los comentarios formateados
     */
    public String getComentariosFormateados() {
        return comentarios != null ? comentarios.trim() : "";
    }
    
    /**
     * Constructor para calificación con comentarios
     */
    public DTOPeticionActualizarEntregaEjercicio(BigDecimal nota, String comentarios) {
        this(null, null, null, nota, comentarios);
    }
}
