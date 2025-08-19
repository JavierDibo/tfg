package app.dtos;

import jakarta.validation.constraints.*;

public record DTOPeticionRegistroAlumno(
    @NotBlank(message = "El usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String usuario,
    
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String contraseña,
    
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,
    
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    String apellidos,
    
    @NotBlank(message = "El DNI no puede estar vacío")
    @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
    String dni,
    
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    String email,
    
    @Size(max = 15, message = "El número de teléfono no puede exceder 15 caracteres")
    String numeroTelefono
) {}
