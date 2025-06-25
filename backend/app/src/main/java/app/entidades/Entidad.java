package app.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Entidad {
    @Id
    @SequenceGenerator(name = "seq_entidad", sequenceName = "seq_entidad", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_entidad")
    int id;
    String info;

    public Entidad() {
        this.info = "info por defecto";
    }

    public Entidad(String info) {
        this.info = info;
    }
}