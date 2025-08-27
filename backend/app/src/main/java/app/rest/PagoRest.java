package app.rest;

import app.config.StripeProperties;
import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTORespuestaPaginada;
import app.excepciones.StripeWebhookException;
import app.servicios.ServicioPago;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for payment management
 * Handles CRUD operations for payments and Stripe webhook processing
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Slf4j
@Tag(name = "Payments", description = "API for payment management")
public class PagoRest extends BaseRestController {
    
    private final ServicioPago servicioPago;
    private final StripeProperties stripeProperties;
    
    // ===== READ OPERATIONS =====
    
    /**
     * Gets paginated payments according to the authenticated user's role:
     * - ADMIN: all payments
     * - PROFESOR: all payments (for monitoring)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @Operation(
        summary = "Get paginated payments",
        description = "Gets a paginated list of payments. Only ADMIN and PROFESOR can access payment data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of payments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view payments"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOPago>> obtenerPagos(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            
            @Parameter(description = "Field to sort by", example = "fechaPago")
            @RequestParam(defaultValue = "fechaPago") @Size(max = 50) String sortBy,
            
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        DTORespuestaPaginada<DTOPago> response = servicioPago.obtenerPagos(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets a specific payment by ID
     * Security: ADMIN/PROFESOR can see any payment, students can only see their own
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR') or (hasRole('ALUMNO') and @servicioPago.isPaymentOwnedByUser(#id, authentication.principal.id))")
    @Operation(
        summary = "Get specific payment",
        description = "Gets a specific payment by ID. Students can only access their own payments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOPago.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this payment"
        )
    })
    public ResponseEntity<DTOPago> obtenerPagoPorId(
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long id) {
        DTOPago pago = servicioPago.obtenerPagoPorId(id);
        return ResponseEntity.ok(pago);
    }
    
    // ===== CREATE OPERATIONS =====
    
    /**
     * Creates a new payment with Stripe integration
     * ADMIN and PROFESOR can create payments for any student
     * ALUMNO can only create payments for themselves
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
    @Operation(
        summary = "Create payment",
        description = "Creates a new payment with Stripe integration. Returns client_secret for frontend confirmation. Students can only create payments for themselves."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Payment created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOPago.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid payment data"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to create payments or trying to create payment for another student"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Payment processing error"
        )
    })
    public ResponseEntity<DTOPago> crearPago(
            @Parameter(description = "Payment creation request")
            @Valid @RequestBody DTOPeticionCrearPago peticion) {
        DTOPago pago = servicioPago.crearPago(peticion);
        return ResponseEntity.status(201).body(pago);
    }
    
    // ===== DEMO/UTILITY OPERATIONS =====
    
    /**
     * Checks if a payment was successful (demo utility)
     * Students can only check their own payments
     */
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR') or (hasRole('ALUMNO') and @servicioPago.isPaymentOwnedByUser(#id, authentication.principal.id))")
    @Operation(
        summary = "Check payment status",
        description = "Checks if a payment was successful (demo utility). Students can only check their own payments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to check this payment"
        )
    })
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long id) {
        DTOPago pago = servicioPago.obtenerPagoPorId(id);
        boolean isSuccessful = servicioPago.isPaymentSuccessful(id);
        
        Map<String, Object> response = Map.of(
            "paymentId", id,
            "isSuccessful", isSuccessful,
            "status", pago.estado().toString(),
            "paymentStatus", pago.estado().toString(),
            "description", pago.getDescripcionEstado()
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets recent payments (demo utility)
     * Only ADMIN and PROFESOR can access
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @Operation(
        summary = "Get recent payments",
        description = "Gets recent payments for demo purposes. Only ADMIN and PROFESOR can access."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Recent payments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOPago.class, type = "array")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view recent payments"
        )
    })
    public ResponseEntity<List<DTOPago>> getRecentPayments(
            @Parameter(description = "Maximum number of payments to return", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        List<DTOPago> recentPayments = servicioPago.getRecentPayments(limit);
        return ResponseEntity.ok(recentPayments);
    }
    
    /**
     * Gets recent payments for the authenticated student
     * Students can only see their own payments
     */
    @GetMapping("/my-recent")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(
        summary = "Get my recent payments",
        description = "Gets recent payments for the authenticated student. Students can only see their own payments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Recent payments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOPago.class, type = "array")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view payments"
        )
    })
    public ResponseEntity<List<DTOPago>> getMyRecentPayments(
            @Parameter(description = "Maximum number of payments to return", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        List<DTOPago> myRecentPayments = servicioPago.getMyRecentPayments(limit);
        return ResponseEntity.ok(myRecentPayments);
    }
    
    // ===== STRIPE WEBHOOK =====
    
    /**
     * Processes Stripe webhook events
     * This endpoint is called by Stripe to update payment status
     */
    @PostMapping("/stripe/webhook")
    @Operation(
        summary = "Stripe webhook endpoint",
        description = "Processes Stripe webhook events to update payment status. Called by Stripe automatically."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhook processed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid webhook signature or payload"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error processing webhook"
        )
    })
    public ResponseEntity<Void> procesarWebhookStripe(
            @Parameter(description = "Webhook payload from Stripe")
            @RequestBody String payload,
            
            @Parameter(description = "Stripe signature for verification")
            @RequestHeader("Stripe-Signature") String signature,
            
            @Parameter(description = "Stripe event ID for idempotency")
            @RequestHeader(value = "Stripe-Event-Id", required = false) String eventId) {
        
        try {
            // Debug webhook configuration
            log.info("Webhook processing - Test mode: {}, Webhook secret: {}", 
                stripeProperties.isTestMode(), 
                stripeProperties.getWebhookSecret().substring(0, 10) + "...");
            
            // Debug payload and signature
            log.info("Webhook payload length: {}", payload.length());
            log.info("Webhook signature: {}", signature.substring(0, 20) + "...");
            
            // Validate webhook signature
            Event event;
            try {
                event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
                log.info("Webhook signature verification successful");
            } catch (SignatureVerificationException e) {
                log.error("Webhook signature verification failed: {}", e.getMessage());
                throw e;
            }
            
            log.info("Processing Stripe webhook event: {} with ID: {}", event.getType(), event.getId());
            log.info("Event data object type: {}", event.getDataObjectDeserializer().getObject().map(Object::getClass).orElse(null));
            
            // Debug the raw event data
            log.info("Event data object present: {}", event.getDataObjectDeserializer().getObject().isPresent());
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                Object obj = event.getDataObjectDeserializer().getObject().get();
                log.info("Event data object class: {}", obj.getClass().getName());
                log.info("Event data object toString: {}", obj.toString());
            } else {
                log.warn("Event data object is not present - this might indicate a webhook secret mismatch");
            }
            
            // Check for duplicate event processing (idempotency)
            if (eventId != null && isEventAlreadyProcessed(eventId)) {
                log.info("Event {} already processed, skipping", eventId);
                return ResponseEntity.ok().build();
            }
            
            // Process the event
            if ("payment_intent.succeeded".equals(event.getType())) {
                log.info("Processing payment_intent.succeeded event");
                
                // Try multiple approaches to extract PaymentIntent
                PaymentIntent paymentIntent = null;
                
                // Method 1: Try direct deserialization
                var dataObject = event.getDataObjectDeserializer().getObject();
                if (dataObject.isPresent() && dataObject.get() instanceof PaymentIntent) {
                    paymentIntent = (PaymentIntent) dataObject.get();
                    log.info("PaymentIntent extracted via direct deserialization: {}", paymentIntent.getId());
                } else {
                    // Method 2: Try manual JSON parsing
                    try {
                        String eventJson = event.toJson();
                        log.info("Event JSON: {}", eventJson);
                        
                        // Extract payment_intent ID from the event
                        if (eventJson.contains("\"id\"")) {
                            // Use a simple approach to extract the payment intent ID
                            int idIndex = eventJson.indexOf("\"id\"");
                            int startQuote = eventJson.indexOf("\"", idIndex + 4);
                            int endQuote = eventJson.indexOf("\"", startQuote + 1);
                            if (startQuote > 0 && endQuote > startQuote) {
                                String paymentIntentId = eventJson.substring(startQuote + 1, endQuote);
                                if (paymentIntentId.startsWith("pi_")) {
                                    log.info("Extracted PaymentIntent ID from JSON: {}", paymentIntentId);
                                    
                                    // Fetch the PaymentIntent from Stripe API
                                    try {
                                        paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                                        log.info("PaymentIntent retrieved from Stripe API: {}", paymentIntent.getId());
                                    } catch (Exception e) {
                                        log.error("Failed to retrieve PaymentIntent from Stripe API: {}", e.getMessage());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing event JSON: {}", e.getMessage());
                    }
                }
                
                if (paymentIntent != null) {
                    log.info("Calling procesarEventoStripe for PaymentIntent: {}", paymentIntent.getId());
                    try {
                        servicioPago.procesarEventoStripe(
                            event.getType(),
                            paymentIntent.getId(),
                            paymentIntent.getLatestCharge(),
                            null
                        );
                        log.info("Payment succeeded for PaymentIntent: {}", paymentIntent.getId());
                    } catch (Exception e) {
                        log.error("Error processing payment event: {}", e.getMessage(), e);
                        throw e;
                    }
                } else {
                    log.error("Could not extract PaymentIntent from webhook event");
                }
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent paymentIntent = null;
                var dataObject = event.getDataObjectDeserializer().getObject();
                if (dataObject.isPresent() && dataObject.get() instanceof PaymentIntent) {
                    paymentIntent = (PaymentIntent) dataObject.get();
                }
                
                if (paymentIntent != null) {
                    String failureReason = paymentIntent.getLastPaymentError() != null ? 
                        paymentIntent.getLastPaymentError().getMessage() : "Payment failed";
                    servicioPago.procesarEventoStripe(
                        event.getType(),
                        paymentIntent.getId(),
                        null,
                        failureReason
                    );
                    log.warn("Payment failed for PaymentIntent: {} - Reason: {}", paymentIntent.getId(), failureReason);
                }
            } else if ("payment_intent.processing".equals(event.getType())) {
                PaymentIntent paymentIntent = null;
                var dataObject = event.getDataObjectDeserializer().getObject();
                if (dataObject.isPresent() && dataObject.get() instanceof PaymentIntent) {
                    paymentIntent = (PaymentIntent) dataObject.get();
                }
                
                if (paymentIntent != null) {
                    servicioPago.procesarEventoStripe(
                        event.getType(),
                        paymentIntent.getId(),
                        null,
                        null
                    );
                    log.info("Payment processing for PaymentIntent: {}", paymentIntent.getId());
                }
            }
            
            return ResponseEntity.ok().build();
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            throw new StripeWebhookException("Invalid webhook signature", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new StripeWebhookException("Error processing webhook", e.getMessage());
        }
    }
    
    /**
     * Check if a Stripe event has already been processed to ensure idempotency
     * In a production environment, this should use a database or cache
     */
    private boolean isEventAlreadyProcessed(String eventId) {
        // TODO: Implement proper idempotency check using database or cache
        // For now, return false to process all events
        // In production, you should store processed event IDs and check against them
        return false;
    }
}
