package app.dtos;

import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.ENivel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la petición de creación de un curso
 * Extiende los datos de clase con información específica del curso
 */
public record DTOPeticionCrearCurso(
        @NotNull
        @Size(max = 200)
        String titulo,
        
        @Size(max = 1000)
        String descripcion,
        
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal precio,
        
        @NotNull
        EPresencialidad presencialidad,
        
        @Size(max = 500)
        String imagenPortada,
        
        @NotNull
        ENivel nivel,
        
        List<String> profesoresId,
        List<Material> material,
        
        @NotNull
        LocalDate fechaInicio,
        
        @NotNull
        LocalDate fechaFin
) {
    
    /**
     * Constructor por defecto con listas vacías
     */
    public DTOPeticionCrearCurso(String titulo, String descripcion, BigDecimal precio,
                                EPresencialidad presencialidad, String imagenPortada, ENivel nivel,
                                LocalDate fechaInicio, LocalDate fechaFin) {
        this(titulo, descripcion, precio, presencialidad, imagenPortada, nivel, 
             List.of(), List.of(), fechaInicio, fechaFin);
    }
    
    /**
     * Valida que la fecha de fin sea posterior a la de inicio
     */
    public boolean fechasValidas() {
        return fechaFin.isAfter(fechaInicio);
    }
}
