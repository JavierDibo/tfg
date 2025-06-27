package app.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DTOEntidad(Integer id, @NotNull @Size(max=100) String info) {}
