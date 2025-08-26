# Payment Implementation Plan - Stripe Integration (PoC for TFG Demo)

## Overview

Simple Stripe payment integration for Academia App demo. Focus: create payment → confirm on frontend → webhook updates status.

## Demo Scope (Minimal & Clean)

**What we'll build:**
- Create Stripe payment intent via backend
- Frontend confirms payment with Stripe Elements
- Webhook updates payment status
- List/view payments

**What we'll skip:**
- Refunds, invoices, customers, saved methods
- Complex filters, multiple currencies
- Subscriptions, alternative payment methods

## Implementation Steps

### Step 1: Add Stripe Dependency

**File:** `pom.xml`
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.8.0</version>
</dependency>
```

### Step 2: Update Pago Entity

**File:** `src/main/java/app/entidades/Pago.java`

Add these fields to the existing `Pago` class:
```java
// Stripe fields (add after existing fields)
private String stripePaymentIntentId;
private String stripeChargeId;
private String failureReason;
private LocalDateTime fechaExpiracion;
```

### Step 3: Create Stripe Configuration

**File:** `src/main/java/app/config/StripeConfig.java`
```java
package app.config;

import com.stripe.Stripe;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    private String publishableKey;
    private String webhookSecret;
    private String currency = "EUR";
    private boolean testMode = true;
    
    // getters and setters
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    // ... add other getters/setters
}

@Configuration
public class StripeConfig {
    
    private final StripeProperties stripeProperties;
    
    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }
    
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        Stripe.setMaxNetworkRetries(2);
    }
}
```

### Step 4: Create ServicioPago

**File:** `src/main/java/app/servicios/ServicioPago.java`
```java
package app.servicios;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
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
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioPago {
    
    private final RepositorioPago repositorioPago;
    private final SecurityUtils securityUtils;
    
    public DTOPago crearPago(DTOPeticionCrearPago peticion) {
        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToCents(peticion.importe()))
                .setCurrency(peticion.currency())
                .setDescription(peticion.description())
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Create Pago entity
            Pago pago = new Pago();
            pago.setImporte(peticion.importe());
            pago.setMetodoPago(EMetodoPago.STRIPE);
            pago.setEstado(EEstadoPago.PENDIENTE);
            pago.setAlumnoId(peticion.alumnoId());
            pago.setStripePaymentIntentId(paymentIntent.getId());
            pago.setFechaExpiracion(LocalDateTime.now().plusHours(24));
            
            Pago savedPago = repositorioPago.save(pago);
            
            // Return DTO with client_secret
            return new DTOPago(
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
            
        } catch (StripeException e) {
            throw new RuntimeException("Error creating payment: " + e.getMessage());
        }
    }
    
    public DTOPago obtenerPagoPorId(Long id) {
        Pago pago = repositorioPago.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
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
        Pago pago = repositorioPago.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        switch (eventType) {
            case "payment_intent.succeeded":
                pago.setEstado(EEstadoPago.EXITO);
                pago.setStripeChargeId(chargeId);
                break;
            case "payment_intent.payment_failed":
                pago.setEstado(EEstadoPago.ERROR);
                pago.setFailureReason(failureReason);
                break;
            case "payment_intent.processing":
                pago.setEstado(EEstadoPago.PROCESANDO);
                break;
        }
        
        repositorioPago.save(pago);
    }
    
    private Long convertToCents(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).longValue();
    }
}
```

### Step 5: Update DTOs

**File:** `src/main/java/app/dtos/DTOPeticionCrearPago.java`
```java
package app.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DTOPeticionCrearPago(
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal importe,
    @NotNull @Size(max = 255) String alumnoId,
    @NotBlank String description,
    @NotBlank String currency
) {}
```

**File:** `src/main/java/app/dtos/DTOPago.java`
```java
package app.dtos;

import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DTOPago(
    Long id,
    LocalDateTime fechaPago,
    BigDecimal importe,
    EMetodoPago metodoPago,
    EEstadoPago estado,
    String alumnoId,
    Boolean facturaCreada,
    List<ItemPago> items,
    String stripePaymentIntentId,
    String stripeChargeId,
    String failureReason,
    String clientSecret // Only present in create response
) {
    
    public DTOPago(Pago pago) {
        this(
            pago.getId(),
            pago.getFechaPago(),
            pago.getImporte(),
            pago.getMetodoPago(),
            pago.getEstado(),
            pago.getAlumnoId(),
            pago.getFacturaCreada(),
            pago.getItems(),
            pago.getStripePaymentIntentId(),
            pago.getStripeChargeId(),
            pago.getFailureReason(),
            null // Never include client_secret when loading from DB
        );
    }
}
```

### Step 6: Create PagoRest Controller

**File:** `src/main/java/app/rest/PagoRest.java`
```java
package app.rest;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payments", description = "API for payment management")
public class PagoRest extends BaseRestController {
    
    private final ServicioPago servicioPago;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Get paginated payments")
    public ResponseEntity<DTORespuestaPaginada<DTOPago>> obtenerPagos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaPago") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        DTORespuestaPaginada<DTOPago> response = servicioPago.obtenerPagos(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or #id == authentication.principal.id")
    @Operation(summary = "Get specific payment")
    public ResponseEntity<DTOPago> obtenerPagoPorId(@PathVariable Long id) {
        DTOPago pago = servicioPago.obtenerPagoPorId(id);
        return ResponseEntity.ok(pago);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create payment")
    public ResponseEntity<DTOPago> crearPago(@Valid @RequestBody DTOPeticionCrearPago peticion) {
        DTOPago pago = servicioPago.crearPago(peticion);
        return ResponseEntity.status(201).body(pago);
    }
    
    @PostMapping("/stripe/webhook")
    @Operation(summary = "Stripe webhook endpoint")
    public ResponseEntity<Void> procesarWebhookStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            // Validate webhook signature
            Event event = Stripe.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
            
            // Process the event
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject();
                servicioPago.procesarEventoStripe(
                    event.getType(),
                    paymentIntent.getId(),
                    paymentIntent.getLatestCharge(),
                    null
                );
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject();
                servicioPago.procesarEventoStripe(
                    event.getType(),
                    paymentIntent.getId(),
                    null,
                    paymentIntent.getLastPaymentError() != null ? 
                        paymentIntent.getLastPaymentError().getMessage() : "Payment failed"
                );
            }
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

### Step 7: Add Repository Method

**File:** `src/main/java/app/repositorios/RepositorioPago.java`

Add this method to the existing interface:
```java
Optional<Pago> findByStripePaymentIntentId(String stripePaymentIntentId);
```

### Step 8: Configuration

**File:** `src/main/resources/application.yml`
```yaml
stripe:
  secret-key: ${STRIPE_SECRET_KEY}
  publishable-key: ${STRIPE_PUBLISHABLE_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  currency: EUR
  test-mode: true
```

**Environment variables to set:**
```bash
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

## Frontend Integration (Demo)

1. **Create Payment:**
```javascript
const response = await fetch('/api/pagos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    importe: 50.00,
    alumnoId: "123",
    description: "Course payment",
    currency: "EUR"
  })
});
const payment = await response.json();
// payment.clientSecret contains the client_secret
```

2. **Confirm Payment with Stripe Elements:**
```javascript
const { error } = await stripe.confirmPayment({
  clientSecret: payment.clientSecret,
  confirmParams: {
    return_url: 'https://your-domain.com/success',
  },
});
```

## Testing

1. **Use Stripe test cards:**
   - Success: `4242424242424242`
   - Decline: `4000000000000002`

2. **Test webhook locally:**
```bash
stripe listen --forward-to localhost:8080/api/pagos/stripe/webhook
```

## Testing (Spring Boot Standards)

### Step 9: Unit Tests

**File:** `src/test/java/app/servicios/ServicioPagoTest.java`
```java
package app.servicios;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
import app.repositorios.RepositorioPago;
import app.util.SecurityUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioPagoTest {

    @Mock
    private RepositorioPago repositorioPago;
    
    @Mock
    private SecurityUtils securityUtils;
    
    @InjectMocks
    private ServicioPago servicioPago;
    
    private DTOPeticionCrearPago peticionValida;
    private Pago pagoGuardado;
    
    @BeforeEach
    void setUp() {
        peticionValida = new DTOPeticionCrearPago(
            new BigDecimal("50.00"),
            "123",
            "Test payment",
            "EUR"
        );
        
        pagoGuardado = new Pago();
        pagoGuardado.setId(1L);
        pagoGuardado.setImporte(new BigDecimal("50.00"));
        pagoGuardado.setMetodoPago(EMetodoPago.STRIPE);
        pagoGuardado.setEstado(EEstadoPago.PENDIENTE);
        pagoGuardado.setAlumnoId("123");
        pagoGuardado.setStripePaymentIntentId("pi_test_123");
        pagoGuardado.setFechaPago(LocalDateTime.now());
    }
    
    @Test
    void crearPago_DeberiaCrearPagoExitoso() throws StripeException {
        // Given
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // Mock Stripe PaymentIntent creation
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_123_secret_abc");
        
        // Use PowerMock or similar to mock static Stripe.create() method
        // For this example, we'll assume the service handles this internally
        
        // When
        DTOPago resultado = servicioPago.crearPago(peticionValida);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.importe()).isEqualTo(new BigDecimal("50.00"));
        assertThat(resultado.metodoPago()).isEqualTo(EMetodoPago.STRIPE);
        assertThat(resultado.estado()).isEqualTo(EEstadoPago.PENDIENTE);
        assertThat(resultado.clientSecret()).isEqualTo("pi_test_123_secret_abc");
        
        verify(repositorioPago).save(any(Pago.class));
    }
    
    @Test
    void obtenerPagoPorId_DeberiaRetornarPago() {
        // Given
        when(repositorioPago.findById(1L)).thenReturn(Optional.of(pagoGuardado));
        
        // When
        DTOPago resultado = servicioPago.obtenerPagoPorId(1L);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.importe()).isEqualTo(new BigDecimal("50.00"));
    }
    
    @Test
    void obtenerPagoPorId_DeberiaLanzarExcepcionSiNoExiste() {
        // Given
        when(repositorioPago.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> servicioPago.obtenerPagoPorId(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Payment not found");
    }
    
    @Test
    void procesarEventoStripe_DeberiaActualizarEstadoExitoso() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("payment_intent.succeeded", "pi_test_123", "ch_test_123", null);
        
        // Then
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.EXITO &&
            "ch_test_123".equals(pago.getStripeChargeId())
        ));
    }
    
    @Test
    void procesarEventoStripe_DeberiaActualizarEstadoError() {
        // Given
        when(repositorioPago.findByStripePaymentIntentId("pi_test_123"))
            .thenReturn(Optional.of(pagoGuardado));
        when(repositorioPago.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // When
        servicioPago.procesarEventoStripe("payment_intent.payment_failed", "pi_test_123", null, "Card declined");
        
        // Then
        verify(repositorioPago).save(argThat(pago -> 
            pago.getEstado() == EEstadoPago.ERROR &&
            "Card declined".equals(pago.getFailureReason())
        ));
    }
}
```

### Step 10: Integration Tests

**File:** `src/test/java/app/rest/PagoRestTest.java`
```java
package app.rest;

import app.dtos.DTOPago;
import app.dtos.DTOPeticionCrearPago;
import app.dtos.DTORespuestaPaginada;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
import app.servicios.ServicioPago;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoRest.class)
class PagoRestTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ServicioPago servicioPago;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPagos_DeberiaRetornarListaPaginada() throws Exception {
        // Given
        DTOPago pago = new DTOPago(
            1L, LocalDateTime.now(), new BigDecimal("50.00"),
            EMetodoPago.STRIPE, EEstadoPago.EXITO, "123", false,
            List.of(), "pi_test_123", "ch_test_123", null, null
        );
        
        Page<DTOPago> page = new PageImpl<>(List.of(pago));
        DTORespuestaPaginada<DTOPago> respuesta = DTORespuestaPaginada.fromPage(page, "fechaPago", "DESC");
        
        when(servicioPago.obtenerPagos(any())).thenReturn(respuesta);
        
        // When & Then
        mockMvc.perform(get("/api/pagos")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].importe").value(50.00));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerPagoPorId_DeberiaRetornarPago() throws Exception {
        // Given
        DTOPago pago = new DTOPago(
            1L, LocalDateTime.now(), new BigDecimal("50.00"),
            EMetodoPago.STRIPE, EEstadoPago.EXITO, "123", false,
            List.of(), "pi_test_123", "ch_test_123", null, null
        );
        
        when(servicioPago.obtenerPagoPorId(1L)).thenReturn(pago);
        
        // When & Then
        mockMvc.perform(get("/api/pagos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.importe").value(50.00))
            .andExpect(jsonPath("$.estado").value("EXITO"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void crearPago_DeberiaCrearPagoExitoso() throws Exception {
        // Given
        DTOPeticionCrearPago peticion = new DTOPeticionCrearPago(
            new BigDecimal("50.00"), "123", "Test payment", "EUR"
        );
        
        DTOPago pagoCreado = new DTOPago(
            1L, LocalDateTime.now(), new BigDecimal("50.00"),
            EMetodoPago.STRIPE, EEstadoPago.PENDIENTE, "123", false,
            List.of(), "pi_test_123", null, null, "pi_test_123_secret_abc"
        );
        
        when(servicioPago.crearPago(any(DTOPeticionCrearPago.class))).thenReturn(pagoCreado);
        
        // When & Then
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andExpect(jsonPath("$.clientSecret").value("pi_test_123_secret_abc"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void crearPago_DeberiaValidarCamposObligatorios() throws Exception {
        // Given
        DTOPeticionCrearPago peticionInvalida = new DTOPeticionCrearPago(
            null, null, "", "EUR"
        );
        
        // When & Then
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
            .andExpect(status().isBadRequest());
    }
}
```

### Step 11: Test Configuration

**File:** `src/test/resources/application-test.yml`
```yaml
stripe:
  secret-key: sk_test_fake_key_for_testing
  publishable-key: pk_test_fake_key_for_testing
  webhook-secret: whsec_fake_webhook_secret_for_testing
  currency: EUR
  test-mode: true

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  security:
    user:
      name: testuser
      password: testpass
```

### Step 12: Repository Tests

**File:** `src/test/java/app/repositorios/RepositorioPagoTest.java`
```java
package app.repositorios;

import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.entidades.enums.EMetodoPago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositorioPagoTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private RepositorioPago repositorioPago;
    
    @Test
    void findByStripePaymentIntentId_DeberiaEncontrarPago() {
        // Given
        Pago pago = new Pago();
        pago.setImporte(new BigDecimal("50.00"));
        pago.setMetodoPago(EMetodoPago.STRIPE);
        pago.setEstado(EEstadoPago.PENDIENTE);
        pago.setAlumnoId("123");
        pago.setStripePaymentIntentId("pi_test_123");
        pago.setFechaPago(LocalDateTime.now());
        
        entityManager.persistAndFlush(pago);
        
        // When
        Optional<Pago> resultado = repositorioPago.findByStripePaymentIntentId("pi_test_123");
        
        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getStripePaymentIntentId()).isEqualTo("pi_test_123");
    }
    
    @Test
    void findByStripePaymentIntentId_DeberiaRetornarVacioSiNoExiste() {
        // When
        Optional<Pago> resultado = repositorioPago.findByStripePaymentIntentId("pi_inexistente");
        
        // Then
        assertThat(resultado).isEmpty();
    }
}
```

### Step 13: Webhook Integration Test

**File:** `src/test/java/app/integration/PagoWebhookIntegrationTest.java`
```java
package app.integration;

import app.entidades.Pago;
import app.entidades.enums.EEstadoPago;
import app.repositorios.RepositorioPago;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class PagoWebhookIntegrationTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private RepositorioPago repositorioPago;
    
    @Test
    void webhook_DeberiaActualizarEstadoPago() {
        // Given - Create a payment in database
        Pago pago = new Pago();
        pago.setImporte(new BigDecimal("50.00"));
        pago.setMetodoPago(EMetodoPago.STRIPE);
        pago.setEstado(EEstadoPago.PENDIENTE);
        pago.setAlumnoId("123");
        pago.setStripePaymentIntentId("pi_test_123");
        pago.setFechaPago(LocalDateTime.now());
        
        repositorioPago.save(pago);
        
        // Create mock webhook payload (simplified for test)
        String webhookPayload = """
            {
              "id": "evt_test_123",
              "type": "payment_intent.succeeded",
              "data": {
                "object": {
                  "id": "pi_test_123",
                  "latest_charge": "ch_test_123"
                }
              }
            }
            """;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Stripe-Signature", "fake_signature_for_testing");
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/pagos/stripe/webhook",
            HttpMethod.POST,
            new HttpEntity<>(webhookPayload, headers),
            String.class
        );
        
        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        Optional<Pago> pagoActualizado = repositorioPago.findByStripePaymentIntentId("pi_test_123");
        assertThat(pagoActualizado).isPresent();
        assertThat(pagoActualizado.get().getEstado()).isEqualTo(EEstadoPago.EXITO);
    }
}
```

## Testing Best Practices

1. **Unit Tests**: Test business logic in isolation
2. **Integration Tests**: Test REST endpoints with MockMvc
3. **Repository Tests**: Test data access with @DataJpaTest
4. **Webhook Tests**: Test webhook processing end-to-end
5. **Use @ActiveProfiles("test")** for test-specific configuration
6. **Mock Stripe API calls** in unit tests
7. **Use H2 in-memory database** for tests
8. **Test both success and failure scenarios**

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ServicioPagoTest

# Run with coverage
mvn test jacoco:report
```

## Demo Flow

1. Create payment → get `client_secret`
2. Show Stripe Elements on frontend
3. User enters card details
4. Confirm payment
5. Webhook updates status
6. Show updated payment in list

That's it! Clean, simple, and demonstrates full-stack skills.
