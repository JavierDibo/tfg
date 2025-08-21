package app.dtos;

import app.entidades.Alumno;
import app.entidades.Usuario;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la entidad Alumno
 * Contiene la información del alumno sin datos sensibles
 */
public record DTOAlumno(
    @Positive(message = "El ID debe ser positivo")
    Long id,
    
    @NotBlank(message = "El usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String usuario,
    
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
    String numeroTelefono,
    
    LocalDateTime fechaInscripcion,
    
    boolean matriculado,
    
    boolean enabled,
    
    List<String> clasesId,
    
    List<String> pagosId,
    
    List<String> entregasId,
    
    Usuario.Rol rol
) {

    /**
     * Constructor que crea un DTO desde una entidad Alumno
     */
    public DTOAlumno(Alumno alumno) {
        this(
            alumno.getId(),
            alumno.getUsuario(),
            alumno.getNombre(),
            alumno.getApellidos(),
            alumno.getDni(),
            alumno.getEmail(),
            alumno.getNumeroTelefono(),
            alumno.getFechaInscripcion(),
            alumno.isMatriculado(),
            alumno.isEnabled(),
            alumno.getClasesId(),
            alumno.getPagosId(),
            alumno.getEntregasId(),
            alumno.getRol()
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOAlumno from(Alumno alumno) {
        return new DTOAlumno(alumno);
    }
    
    /**
     * Obtiene el nombre completo del alumno
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellidos;
    }
    
    /**
     * Verifica si el alumno está inscrito en alguna clase
     */
    public boolean tieneClases() {
        return this.clasesId != null && !this.clasesId.isEmpty();
    }
    
    /**
     * Cuenta el número de clases en las que está inscrito
     */
    public int getNumeroClases() {
        return this.clasesId != null ? this.clasesId.size() : 0;
    }
    
    /**
     * Verifica si el alumno tiene pagos registrados
     */
    public boolean tienePagos() {
        return this.pagosId != null && !this.pagosId.isEmpty();
    }
    
    /**
     * Cuenta el número de pagos realizados
     */
    public int getNumeroPagos() {
        return this.pagosId != null ? this.pagosId.size() : 0;
    }
    
    /**
     * Verifica si el alumno tiene entregas de ejercicios
     */
    public boolean tieneEntregas() {
        return this.entregasId != null && !this.entregasId.isEmpty();
    }
    
    /**
     * Cuenta el número de entregas realizadas
     */
    public int getNumeroEntregas() {
        return this.entregasId != null ? this.entregasId.size() : 0;
    }
}