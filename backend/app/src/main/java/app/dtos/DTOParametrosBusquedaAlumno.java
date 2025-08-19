package app.dtos;

public record DTOParametrosBusquedaAlumno(
    String nombre,
    String apellidos,
    String dni,
    String email,
    Boolean matriculado
) {}
