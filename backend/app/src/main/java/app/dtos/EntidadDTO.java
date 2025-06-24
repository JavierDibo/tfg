package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntidadDTO {
    private Integer id;
    private String info;
    
    // Constructor without id (for creation)
    public EntidadDTO(String info) {
        this.info = info;
    }
}
