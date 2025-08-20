package app.dtos;

import jakarta.validation.constraints.NotBlank;

public record DTOPeticionLogin(
    @NotBlank(message = "El username es obligatorio")
    String username,
    
    @NotBlank(message = "La password es obligatoria")
    String password
) {} 