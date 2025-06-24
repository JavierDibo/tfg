package app.entidades;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entidad {
    @Id
    @SequenceGenerator(name = "entidad_seq", sequenceName = "entidad_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entidad_seq")
    int id;
    String info;
}