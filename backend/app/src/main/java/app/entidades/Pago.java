package app.entidades;

import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;
import app.validation.stripe.ValidStripePaymentIntentId;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Pago
 * Representa un pago realizado por un alumno
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "pagos")
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    private LocalDateTime fechaPago;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal importe;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EMetodoPago metodoPago;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EEstadoPago estado;
    
    @NotNull
    @Size(max = 255)
    private String alumnoId;
    
    @NotNull
    private Boolean facturaCreada = false;
    
    // Items del pago embebidos
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pago_items", joinColumns = @JoinColumn(name = "pago_id"))
    private List<ItemPago> items = new ArrayList<>();
    
    // Stripe fields (add after existing fields)
    @ValidStripePaymentIntentId
    private String stripePaymentIntentId;
    
    private String stripeChargeId;
    
    @Size(max = 500, message = "La razón del fallo no puede exceder 500 caracteres")
    private String failureReason;
    
    private LocalDateTime fechaExpiracion;
    
    public Pago() {
        this.fechaPago = LocalDateTime.now();
        this.estado = EEstadoPago.EXITO; // Por defecto, según UML solo se crea si hay confirmación
    }
    
    public Pago(BigDecimal importe, EMetodoPago metodoPago, String alumnoId) {
        this();
        this.importe = importe;
        this.metodoPago = metodoPago;
        this.alumnoId = alumnoId;
    }
    
    /**
     * Agrega un ítem al pago
     * @param item ItemPago a agregar
     */
    public void agregarItem(ItemPago item) {
        this.items.add(item);
        recalcularImporte();
    }
    
    /**
     * Remueve un ítem del pago por ID
     * @param itemId ID del ítem a remover
     */
    public void removerItem(String itemId) {
        this.items.removeIf(item -> item.getId().equals(itemId));
        recalcularImporte();
    }
    
    /**
     * Recalcula el importe total basado en los ítems
     */
    private void recalcularImporte() {
        this.importe = this.items.stream()
                .map(ItemPago::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Verifica si se puede crear una factura para este pago
     * @return true si se puede crear factura, false en caso contrario
     */
    public boolean puedeCrearFactura() {
        return !this.facturaCreada && this.estado == EEstadoPago.EXITO;
    }
    
    /**
     * Marca el pago como facturado
     */
    public void marcarComoFacturado() {
        if (!puedeCrearFactura()) {
            throw new IllegalStateException("No se puede crear una factura para este pago");
        }
        this.facturaCreada = true;
    }
    
    /**
     * Verifica si el pago es un pago manual (efectivo)
     * @return true si es pago manual, false en caso contrario
     */
    public boolean esPagoManual() {
        return this.metodoPago == EMetodoPago.EFECTIVO;
    }
    
    /**
     * Procesa el reembolso del pago
     */
    public void procesarReembolso() {
        if (this.estado != EEstadoPago.EXITO) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos exitosos");
        }
        this.estado = EEstadoPago.REEMBOLSADO;
    }
}
