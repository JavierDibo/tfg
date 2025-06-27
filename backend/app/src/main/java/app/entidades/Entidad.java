package app.entidades;

import app.dtos.DTOEntidad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad
 * Persistente
 */
@Data
@EqualsAndHashCode
@Entity
public class Entidad {
    @NotNull
    @Id
    @SequenceGenerator(name = "seq_entidad", sequenceName = "seq_entidad", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_entidad")
    int id;
    @NotNull
    @Size(max = 100)
    String info = "info por defecto";
    @NotNull
    @Size(max = 100)
    String otraInfo = "otra info por defecto";


    @Transient // no se mapea
    LocalDateTime fechaCreacion = LocalDateTime.now();

    public Entidad() {    }

    public Entidad(String info, String otraInfo) {
        this.info = info;
    }

    public Entidad(DTOEntidad dtoEntidad) {
        this.info = dtoEntidad.info();
        this.otraInfo = dtoEntidad.otraInfo();
    }
}