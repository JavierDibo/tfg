package app.dtos;

import app.entidades.Usuario;

public record DTORespuestaLogin(
    String token,
    String type,
    Long id,
    String username,
    String email,
    String nombre,
    String apellidos,
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