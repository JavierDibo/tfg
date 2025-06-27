package app.dtos;

import jakarta.validation.constraints.NotBlank;

public record DTOPeticionLogin(
    @NotBlank(message = "El username es obligatorio")
    String username,
    
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {} 