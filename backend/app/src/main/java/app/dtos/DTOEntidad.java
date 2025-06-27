package app.dtos;

import app.entidades.Entidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DTOEntidad(
    @Positive(message = "El ID debe ser positivo")
    Integer id, 
    
    @NotBlank(message = "La información no puede estar vacía")
    @Size(max = 100, message = "La información no puede exceder 100 caracteres") 
    String info, 
    
    @NotBlank(message = "La otra información no puede estar vacía")
    @Size(max = 100, message = "La otra información no puede exceder 100 caracteres") 
    String otraInfo
) {

    public DTOEntidad(Entidad entidad) {
        this(entidad.getId(), entidad.getInfo(), entidad.getOtraInfo());
    }
}
