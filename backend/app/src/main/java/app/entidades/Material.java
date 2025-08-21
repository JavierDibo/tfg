package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Material (Agregado)
 * Representa material didáctico asociado a una clase
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Embeddable
public class Material {
    
    @NotNull
    @Size(max = 255)
    private String id;
    
    @NotNull
    @Size(max = 200)
    private String nombre;
    
    @NotNull
    @Size(max = 500)
    private String url;
    
    public Material() {}
    
    public Material(String id, String nombre, String url) {
        this.id = id;
        this.nombre = nombre;
        this.url = url;
    }
}
