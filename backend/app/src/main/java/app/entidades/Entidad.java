package app.entidades;

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
    @Size(max = 100)
    String info;

    @Transient // no se mapea
    LocalDateTime fechaCreacion = LocalDateTime.now();

    public Entidad() {
        this.info = "info por defecto";
    }

    public Entidad(String info) {
        this.info = info;
    }
}