package app.dtos;

import app.entidades.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con token JWT")
public record DTORespuestaLogin(
    @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    String token,
    
    @Schema(description = "Tipo de token", example = "Bearer", required = true)
    String type,
    
    @Schema(description = "ID del usuario autenticado", example = "1", required = true)
    Long id,
    
    @Schema(description = "Nombre de usuario", example = "usuario123", required = true)
    String username,
    
    @Schema(description = "Email del usuario", example = "usuario@example.com", required = true)
    String email,
    
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    String nombre,
    
    @Schema(description = "Apellidos del usuario", example = "Pérez", required = true)
    String apellidos,
    
    @Schema(description = "Rol del usuario", example = "ALUMNO", required = true)
    String rol
) {
    public static DTORespuestaLogin from(Usuario usuario, String token) {
        return new DTORespuestaLogin(
            token,
            "Bearer",
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getRol().name()
        );
    }
} 