package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Material
 * Representa material didáctico asociado a una clase
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "materiales")
public class Material {
    
    @Id
    @NotNull
    @Size(max = 255)
    private String id;
    
    @NotNull
    @Size(max = 200)
    private String name;
    
    @NotNull
    @Size(max = 500)
    private String url;
    
    public Material() {}
    
    public Material(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
}
