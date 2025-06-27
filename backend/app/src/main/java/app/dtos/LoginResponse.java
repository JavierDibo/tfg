package app.dtos;

import app.entidades.Usuario;

public record LoginResponse(
    String token,
    String type,
    Long id,
    String username,
    String email,
    String nombre,
    String apellidos,
    String rol
) {
    public static LoginResponse from(Usuario usuario, String token) {
        return new LoginResponse(
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