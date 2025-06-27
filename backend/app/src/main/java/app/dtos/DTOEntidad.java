package app.dtos;

import app.entidades.Entidad;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DTOEntidad(Integer id, @Size(max=100) String info, @Size(max=100) String otraInfo) {

    public DTOEntidad(Entidad entidad) {
        this(entidad.getId(), entidad.getInfo(), entidad.getOtraInfo());
    }
}
