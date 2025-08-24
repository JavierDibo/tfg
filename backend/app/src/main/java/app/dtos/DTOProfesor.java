package app.dtos;

import java.time.LocalDateTime;
import java.util.List;

import app.entidades.Profesor;
import app.entidades.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for Professor entity
 * Contains professor information without sensitive data
 */
@Schema(description = "Professor data")
public record DTOProfesor(
    @Schema(description = "Unique professor ID", example = "1", required = true)
    @Positive(message = "ID must be positive")
    Long id,
    
    @Schema(description = "Unique username", example = "professor123", required = true)
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Schema(description = "Professor's first name", example = "Maria", required = true)
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName,
    
    @Schema(description = "Professor's last name", example = "Garcia Lopez", required = true)
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName,
    
    @Schema(description = "Professor's DNI", example = "87654321B", required = true)
    @NotBlank(message = "DNI cannot be empty")
    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    String dni,
    
    @Schema(description = "Professor's email", example = "maria.garcia@email.com", required = true)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must have a valid format")
    String email,
    
    @Schema(description = "Phone number", example = "+34687654321", required = false)
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    String phoneNumber,
    
    @Schema(description = "User role", example = "PROFESSOR", required = true)
    Usuario.Role role,
    
    @Schema(description = "Indicates if the account is enabled", example = "true", required = true)
    boolean enabled,
    
    @Schema(description = "List of assigned class IDs", example = "[\"1\", \"2\"]", required = false)
    List<String> classIds,
    
    @Schema(description = "Profile creation date", example = "2024-01-15T10:30:00", required = false)
    LocalDateTime createdAt
) {

    /**
     * Constructor that creates a DTO from a Professor entity
     */
    public DTOProfesor(Profesor profesor) {
        this(
            profesor.getId(),
            profesor.getUsername(),
            profesor.getFirstName(),
            profesor.getLastName(),
            profesor.getDni(),
            profesor.getEmail(),
            profesor.getPhoneNumber(),
            profesor.getRole(),
            profesor.isEnabled(),
            profesor.getClasesId(),
            LocalDateTime.now() // placeholder, in a real implementation would be creation date from entity
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOProfesor from(Profesor profesor) {
        return new DTOProfesor(profesor);
    }
    
    /**
     * Gets the professor's full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
    
    /**
     * Checks if the professor has assigned classes
     */
    public boolean hasClasses() {
        return this.classIds != null && !this.classIds.isEmpty();
    }
    
    /**
     * Counts the number of assigned classes
     */
    public int getClassCount() {
        return this.classIds != null ? this.classIds.size() : 0;
    }
    
    /**
     * Checks if the professor is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }
    
    /**
     * Checks if the professor has no assigned classes
     */
    public boolean hasNoClasses() {
        return this.classIds == null || this.classIds.isEmpty();
    }
    
    /**
     * Gets the enabled status as text
     */
    public String getEnabledStatus() {
        return this.enabled ? "Enabled" : "Disabled";
    }
}
