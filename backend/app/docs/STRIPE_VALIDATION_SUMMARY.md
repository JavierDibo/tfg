# Stripe Validation - Ultra-Simple EUR Demo Implementation

## Overview
Minimal validation system for Stripe integration in the Academia App demo. **EUR-only, essential validations only**.

## Validation Components (Only What You Need)

### 1. Amount Validation (`@ValidStripeAmount`)
- **File:** `StripeAmountValidator.java`
- **Purpose:** Validates EUR payment amounts are within Stripe limits
- **Rules:** 
  - Minimum: €0.50
  - Maximum: €999,999.99
  - Must be positive

### 2. Currency Validation (`@ValidCurrency`)
- **File:** `CurrencyValidator.java`
- **Purpose:** Validates currency is EUR only
- **Rules:**
  - Must be exactly "EUR" (case-insensitive)
  - No other currencies supported for demo

### 3. Payment Intent ID Validation (`@ValidStripePaymentIntentId`)
- **File:** `StripePaymentIntentIdValidator.java`
- **Purpose:** Validates Stripe Payment Intent ID format
- **Rules:**
  - Must start with "pi_"
  - Followed by 24 alphanumeric characters
  - Example: `pi_1234567890abcdefghijklmn`

## What We Removed (Overkill for Demo)

❌ **Charge ID Validation** - Not needed because:
- Charge IDs are set by Stripe webhooks, not user input
- No validation needed for system-generated values
- Just adds complexity without benefit

❌ **Duplicate Validators** - Removed duplicate Payment Intent ID validators

## Usage in DTOs

```java
public record DTOPeticionCrearPago(
    @NotNull
    @ValidStripeAmount
    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    BigDecimal importe,
    
    @NotNull
    @Size(max = 255)
    String alumnoId,
    
    @NotBlank
    String description,
    
    @NotBlank
    @ValidCurrency  // Only accepts EUR
    String currency
) {
    // Constructor defaults to EUR
    public DTOPeticionCrearPago(BigDecimal importe, String alumnoId, String description) {
        this(importe, alumnoId, description, "EUR");
    }
}
```

## Usage in Entities

```java
@Entity
public class Pago {
    // ... other fields
    
    @ValidStripePaymentIntentId
    private String stripePaymentIntentId;
    
    // No validation needed - set by Stripe webhooks
    private String stripeChargeId;
    
    // ... other fields
}
```

## Ultra-Simple Benefits

- ✅ **Only 3 validators** - Essential validations only
- ✅ **No Charge ID validation** - System-generated, no validation needed
- ✅ **EUR-only** - No currency complexity
- ✅ **Demo-perfect** - Shows core functionality without over-engineering
- ✅ **Easy to explain** - Clear, focused validation scope

## Testing

```java
@Test
void shouldValidateValidEURPaymentRequest() {
    DTOPeticionCrearPago request = new DTOPeticionCrearPago(
        new BigDecimal("50.00"), "123", "Test payment", "EUR"
    );
    
    Set<ConstraintViolation<DTOPeticionCrearPago>> violations = 
        validator.validate(request);
    
    assertThat(violations).isEmpty();
}

@Test
void shouldRejectNonEURCurrency() {
    DTOPeticionCrearPago request = new DTOPeticionCrearPago(
        new BigDecimal("50.00"), "123", "Test payment", "USD"
    );
    
    Set<ConstraintViolation<DTOPeticionCrearPago>> violations = 
        validator.validate(request);
    
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
}
```

## Final Validation Files

```
src/main/java/app/validation/stripe/
├── ValidStripeAmount.java              # Amount validation annotation
├── StripeAmountValidator.java          # EUR amount validation logic
├── ValidCurrency.java                  # Currency validation annotation  
├── CurrencyValidator.java              # EUR-only validation logic
├── ValidStripePaymentIntentId.java     # Payment Intent ID annotation
└── StripePaymentIntentIdValidator.java # Payment Intent ID validation logic
```

**Total: 6 files, 3 validators** - Perfect for your TFG demo! 🎯

This ultra-simple approach focuses only on what's essential for demonstrating Stripe integration without any unnecessary complexity.
