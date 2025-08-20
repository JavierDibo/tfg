package app.dtos;

import app.validation.*;
import jakarta.validation.constraints.*;

public record DTOPeticionRegistroAlumno(
    @NotBlank(message = "El usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    String usuario,
    
    @NotBlank(message = "La password no puede estar vacía")
    @Size(min = 6, message = "La password debe tener al menos 6 caracteres")
    String password,
    
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    String nombre,
    
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los apellidos solo pueden contener letras y espacios")
    String apellidos,
    
    @NotBlank(message = "El DNI no puede estar vacío")
    @ValidDNI
    String dni,
    
    @NotBlank(message = "El email no puede estar vacío")
    @ValidEmail
    String email,
    
    @ValidPhone
    String numeroTelefono
) {}
