package app.servicios;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
import app.excepciones.PaymentNotFoundException;
import app.excepciones.PaymentProcessingException;
import app.repositorios.RepositorioPago;
import app.util.SecurityUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioPago {
    
    private final RepositorioPago repositorioPago;
    private final SecurityUtils securityUtils;
    
    public DTOPago crearPago(DTOPeticionCrearPago peticion) {
        // Security check: Students can only create payments for themselves
        if (securityUtils.hasRole("ALUMNO")) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!peticion.alumnoId().equals(currentUserId.toString())) {
                throw new RuntimeException("No tienes permisos para crear pagos para otros alumnos");
            }
        }
        
        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToCents(peticion.importe()))
                .setCurrency(peticion.currency().toLowerCase()) // Stripe expects lowercase
                .setDescription(peticion.description())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Debug: Log the payment intent details
            System.out.println("=== STRIPE PAYMENT INTENT CREATED ===");
            System.out.println("PaymentIntent ID: " + paymentIntent.getId());
            System.out.println("PaymentIntent Status: " + paymentIntent.getStatus());
            System.out.println("Client Secret: " + paymentIntent.getClientSecret());
            System.out.println("Amount: " + paymentIntent.getAmount());
            System.out.println("Currency: " + paymentIntent.getCurrency());
            System.out.println("=====================================");
            
            // Create Pago entity
            Pago pago = new Pago();
            pago.setImporte(peticion.importe());
            pago.setMetodoPago(EMetodoPago.STRIPE);
            pago.setEstado(EEstadoPago.PENDIENTE);
            pago.setAlumnoId(peticion.alumnoId());
            pago.setStripePaymentIntentId(paymentIntent.getId());
            pago.setFechaExpiracion(LocalDateTime.now().plusHours(24));
            
            Pago savedPago = repositorioPago.save(pago);
            
            // Create DTO with client_secret
            DTOPago dtoPago = new DTOPago(
                savedPago.getId(),
                savedPago.getFechaPago(),
                savedPago.getImporte(),
                savedPago.getMetodoPago(),
                savedPago.getEstado(),
                savedPago.getAlumnoId(),
                savedPago.getFacturaCreada(),
                savedPago.getItems(),
                savedPago.getStripePaymentIntentId(),
                savedPago.getStripeChargeId(),
                savedPago.getFailureReason(),
                paymentIntent.getClientSecret() // Only in response, never stored
            );
            
            // Debug: Log what's being returned
            System.out.println("=== RESPONSE DTO ===");
            System.out.println("DTO Client Secret: " + dtoPago.clientSecret());
            System.out.println("DTO Payment Intent ID: " + dtoPago.stripePaymentIntentId());
            System.out.println("===================");
            
            return dtoPago;
            
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error creating payment", e.getMessage());
        }
    }
    
    public DTOPago obtenerPagoPorId(Long id) {
        Pago pago = repositorioPago.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("id", id));
        
        return new DTOPago(pago);
    }
    
    public DTORespuestaPaginada<DTOPago> obtenerPagos(Pageable pageable) {
        Page<Pago> pagoPage = repositorioPago.findAll(pageable);
        return DTORespuestaPaginada.fromPage(
            pagoPage.map(DTOPago::new),
            pageable.getSort().toString(),
            "DESC"
        );
    }
    
    public void procesarEventoStripe(String eventType, String paymentIntentId, String chargeId, String failureReason) {
        System.out.println("=== PROCESSING STRIPE EVENT ===");
        System.out.println("Event Type: " + eventType);
        System.out.println("Payment Intent ID: " + paymentIntentId);
        System.out.println("Charge ID: " + chargeId);
        System.out.println("Failure Reason: " + failureReason);
        
        Pago pago = repositorioPago.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new PaymentNotFoundException("stripePaymentIntentId", paymentIntentId));
        
        System.out.println("Found payment with ID: " + pago.getId());
        System.out.println("Current status: " + pago.getEstado());
        
        switch (eventType) {
            case "payment_intent.succeeded":
                pago.setEstado(EEstadoPago.EXITO);
                pago.setStripeChargeId(chargeId);
                System.out.println("Updated status to: EXITO");
                break;
            case "payment_intent.payment_failed":
                pago.setEstado(EEstadoPago.ERROR);
                pago.setFailureReason(failureReason);
                System.out.println("Updated status to: ERROR");
                break;
            case "payment_intent.processing":
                pago.setEstado(EEstadoPago.PROCESANDO);
                System.out.println("Updated status to: PROCESANDO");
                break;
        }
        
        Pago savedPago = repositorioPago.save(pago);
        System.out.println("Payment saved with new status: " + savedPago.getEstado());
        System.out.println("=================================");
    }
    
    private Long convertToCents(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).longValue();
    }
    
    /**
     * Check if a payment was successful
     * Useful for demo purposes
     * @param paymentId ID of the payment to check
     * @return true if payment was successful, false otherwise
     */
    public boolean isPaymentSuccessful(Long paymentId) {
        Pago pago = repositorioPago.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("id", paymentId));
        return EEstadoPago.EXITO.equals(pago.getEstado());
    }
    
    /**
     * Get recent payments for demo purposes
     * @param limit maximum number of payments to return
     * @return List of recent payments
     */
    public List<DTOPago> getRecentPayments(int limit) {
        return repositorioPago.findAllOrderedByFechaDesc()
            .stream()
            .limit(limit)
            .map(DTOPago::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent payments for the authenticated student
     * @param limit maximum number of payments to return
     * @return List of recent payments for the current student
     */
    public List<DTOPago> getMyRecentPayments(int limit) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return repositorioPago.findByAlumnoIdOrderByFechaPagoDesc(currentUserId.toString())
            .stream()
            .limit(limit)
            .map(DTOPago::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a payment is owned by a specific user
     * Used for security authorization in REST endpoints
     * @param paymentId ID of the payment to check
     * @param userId ID of the user to check ownership against
     * @return true if the payment belongs to the user, false otherwise
     */
    public boolean isPaymentOwnedByUser(Long paymentId, Long userId) {
        try {
            Pago pago = repositorioPago.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("id", paymentId));
            return pago.getAlumnoId().equals(userId.toString());
        } catch (PaymentNotFoundException e) {
            return false;
        }
    }
}
