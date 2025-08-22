package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos de petición para iniciar sesión")
public record DTOPeticionLogin(
    @Schema(description = "Nombre de usuario", example = "usuario123", required = true)
    @NotBlank(message = "El username es obligatorio")
    String username,
    
    @Schema(description = "Contraseña del usuario", example = "password123", required = true)
    @NotBlank(message = "La password es obligatoria")
    String password
) {} 