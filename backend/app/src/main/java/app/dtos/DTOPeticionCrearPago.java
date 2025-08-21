package app.dtos;

import app.entidades.ItemPago;
import app.entidades.enums.EMetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de creación de un pago
 * Contiene los datos necesarios para registrar un pago
 */
public record DTOPeticionCrearPago(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal importe,
        
        @NotNull
        EMetodoPago metodoPago,
        
        @NotNull
        @Size(max = 255)
        String alumnoId,
        
        List<ItemPago> items
) {
    
    /**
     * Constructor por defecto con lista vacía de items
     */
    public DTOPeticionCrearPago(BigDecimal importe, EMetodoPago metodoPago, String alumnoId) {
        this(importe, metodoPago, alumnoId, List.of());
    }
    
    /**
     * Calcula el total de los items para verificación
     */
    public BigDecimal calcularTotalItems() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(ItemPago::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Verifica si el importe coincide con el total de items
     */
    public boolean importeCoincideConItems() {
        return importe.compareTo(calcularTotalItems()) == 0;
    }
}
