package app.dtos;

import app.validation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para la petición de registro de un profesor
 * Contiene todos los datos necesarios para crear un profesor
 */
public record DTOPeticionRegistroProfesor(
        @NotNull
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
        String usuario,
        
        @NotNull
        @Size(min = 6)
        String password,
        
        @NotNull
        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
        String nombre,
        
        @NotNull
        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los apellidos solo pueden contener letras y espacios")
        String apellidos,
        
        @NotNull
        @ValidDNI
        String dni,
        
        @NotNull
        @ValidEmail
        String email,
        
        @ValidPhone
        String numeroTelefono,
        
        List<String> clasesId
) {
    
    /**
     * Constructor por defecto con lista vacía de clases
     */
    public DTOPeticionRegistroProfesor(String usuario, String password, String nombre, 
                                      String apellidos, String dni, String email, 
                                      String numeroTelefono) {
        this(usuario, password, nombre, apellidos, dni, email, numeroTelefono, List.of());
    }
}
