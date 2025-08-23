package app.dtos;

import java.time.LocalDateTime;
import java.util.List;

import app.entidades.Alumno;
import app.entidades.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para la entidad Alumno
 * Contiene la información del alumno sin datos sensibles
 */
@Schema(description = "Datos de un alumno")
public record DTOAlumno(
    @Schema(description = "ID único del alumno", example = "1", required = true)
    @Positive(message = "El ID debe ser positivo")
    Long id,
    
    @Schema(description = "Nombre de usuario único", example = "alumno123", required = true)
    @NotBlank(message = "El usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String usuario,
    
    @Schema(description = "Nombre del alumno", example = "Juan", required = true)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,
    
    @Schema(description = "Apellidos del alumno", example = "Pérez García", required = true)
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    String apellidos,
    
    @Schema(description = "DNI del alumno", example = "12345678A", required = true)
    @NotBlank(message = "El DNI no puede estar vacío")
    @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
    String dni,
    
    @Schema(description = "Email del alumno", example = "juan.perez@email.com", required = true)
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    String email,
    
    @Schema(description = "Número de teléfono", example = "+34612345678", required = false)
    @Size(max = 15, message = "El número de teléfono no puede exceder 15 caracteres")
    String numeroTelefono,
    
    @Schema(description = "Fecha de inscripción", example = "2024-01-15T10:30:00", required = false)
    LocalDateTime fechaInscripcion,
    
    @Schema(description = "Indica si el alumno está matriculado", example = "true", required = true)
    boolean matriculado,
    
    @Schema(description = "Indica si la cuenta está habilitada", example = "true", required = true)
    boolean enabled,
    
    @Schema(description = "Lista de IDs de clases en las que está inscrito", example = "[\"1\", \"2\"]", required = false)
    List<String> clasesId,
    
    @Schema(description = "Lista de IDs de pagos realizados", example = "[\"1\", \"2\"]", required = false)
    List<String> pagosId,
    
    @Schema(description = "Lista de IDs de entregas de ejercicios", example = "[\"1\", \"2\"]", required = false)
    List<String> entregasId,
    
    @Schema(description = "Rol del usuario", example = "ALUMNO", required = true)
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