package app.dtos;

import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.ENivel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la petición de creación de un taller
 * Extiende los datos de clase con información específica del taller
 */
public record DTOPeticionCrearTaller(
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
        @Min(value = 1, message = "La duración debe ser al menos 1 hora")
        Integer duracionHoras,
        
        @NotNull
        LocalDate fechaRealizacion,
        
        @NotNull
        LocalTime horaComienzo
) {
    
    /**
     * Constructor por defecto con listas vacías
     */
    public DTOPeticionCrearTaller(String titulo, String descripcion, BigDecimal precio,
                                 EPresencialidad presencialidad, String imagenPortada, ENivel nivel,
                                 Integer duracionHoras, LocalDate fechaRealizacion, LocalTime horaComienzo) {
        this(titulo, descripcion, precio, presencialidad, imagenPortada, nivel, 
             List.of(), List.of(), duracionHoras, fechaRealizacion, horaComienzo);
    }
}
