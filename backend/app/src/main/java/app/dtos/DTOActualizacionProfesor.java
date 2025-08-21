package app.dtos;

import app.validation.NotEmptyIfPresent;
import app.validation.ValidDNI;
import app.validation.ValidEmail;
import app.validation.ValidPhone;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO para actualización parcial de datos de un profesor
 * Todos los campos son opcionales, solo se actualizan los campos no nulos
 */
public record DTOActualizacionProfesor(
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @NotEmptyIfPresent
    String nombre,

    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @NotEmptyIfPresent
    String apellidos,

    @ValidDNI
    @NotEmptyIfPresent
    String dni,

    @ValidEmail
    @NotEmptyIfPresent
    String email,

    @ValidPhone
    @NotEmptyIfPresent
    String numeroTelefono,

    Set<String> clasesId
) {
    /**
     * Verifica si todos los campos están vacíos/nulos
     * @return true si todos los campos son nulos
     */
    public boolean estaVacio() {
        return nombre == null && 
               apellidos == null && 
               dni == null && 
               email == null && 
               numeroTelefono == null && 
               (clasesId == null || clasesId.isEmpty());
    }
}
