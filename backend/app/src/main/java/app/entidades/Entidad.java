package app.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String info;

    public Entidad() {
        this.info = "info por defecto";
    }

    public Entidad(String info) {
        this.info = info;
    }
}