# Frontend Payment Integration Guide

## Overview
This guide explains how to integrate with the Academia App payment system from your frontend application. The system uses Stripe for payment processing and provides a RESTful API for payment management.

## Base URL
```
http://localhost:8080/api/pagos
```

## Authentication
All endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### 1. Create Payment (POST /api/pagos)

**Purpose:** Creates a new payment with Stripe integration and returns a client secret for frontend confirmation.

**Authorization:** ADMIN, PROFESOR only

**Request Body:**
```json
{
  "importe": 99.99,
  "alumnoId": "student123",
  "description": "Payment for Advanced Mathematics Course",
  "currency": "EUR"
}
```

**Request Fields:**
- `importe` (BigDecimal, required): Payment amount (€0.50 - €999,999.99)
- `alumnoId` (String, required): Student ID (max 255 chars)
- `description` (String, required): Payment description
- `currency` (String, required): Currency code (only "EUR" supported)

**Response (201 Created):**
```json
{
  "id": 1,
  "fechaPago": "2024-01-15T10:30:00",
  "importe": 99.99,
  "metodoPago": "STRIPE",
  "estado": "PENDIENTE",
  "alumnoId": "student123",
  "facturaCreada": false,
  "items": [],
  "stripePaymentIntentId": "pi_1234567890abcdefghijklmn",
  "stripeChargeId": null,
  "failureReason": null,
  "clientSecret": "pi_1234567890abcdefghijklmn_secret_abc123def456"
}
```

**Frontend Integration Example:**
```javascript
// Create payment
const createPayment = async (paymentData) => {
  try {
    const response = await fetch('/api/pagos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(paymentData)
    });
    
    if (response.ok) {
      const payment = await response.json();
      return payment.clientSecret; // Use this with Stripe.js
    }
  } catch (error) {
    console.error('Error creating payment:', error);
  }
};

// Use with Stripe.js
const clientSecret = await createPayment({
  importe: 99.99,
  alumnoId: "student123",
  description: "Course payment",
  currency: "EUR"
});

// Confirm payment with Stripe
const { error } = await stripe.confirmPayment({
  clientSecret,
  confirmParams: {
    return_url: "https://yourapp.com/success",
  },
});
```

### 2. Get Paginated Payments (GET /api/pagos)

**Purpose:** Retrieves a paginated list of payments.

**Authorization:** ADMIN, PROFESOR only

**Query Parameters:**
- `page` (int, optional): Page number (0-indexed, default: 0)
- `size` (int, optional): Page size (1-100, default: 20)
- `sortBy` (string, optional): Field to sort by (default: "fechaPago")
- `sortDirection` (string, optional): Sort direction "ASC" or "DESC" (default: "DESC")

**Example Request:**
```
GET /api/pagos?page=0&size=10&sortBy=fechaPago&sortDirection=DESC
```

**Response (200 OK):**
```json
{
  "contenido": [
    {
      "id": 1,
      "fechaPago": "2024-01-15T10:30:00",
      "importe": 99.99,
      "metodoPago": "STRIPE",
      "estado": "EXITO",
      "alumnoId": "student123",
      "facturaCreada": false,
      "items": [],
      "stripePaymentIntentId": "pi_1234567890abcdefghijklmn",
      "stripeChargeId": "ch_1234567890abcdefghijklmn",
      "failureReason": null,
      "clientSecret": null
    }
  ],
  "paginaActual": 0,
  "tamanoPagina": 10,
  "totalElementos": 25,
  "totalPaginas": 3,
  "ultima": false,
  "primera": true,
  "ordenamiento": "fechaPago: DESC"
}
```

### 3. Get Specific Payment (GET /api/pagos/{id})

**Purpose:** Retrieves a specific payment by ID.

**Authorization:** 
- ADMIN/PROFESOR: Can see any payment
- ALUMNO: Can only see their own payments

**Example Request:**
```
GET /api/pagos/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "fechaPago": "2024-01-15T10:30:00",
  "importe": 99.99,
  "metodoPago": "STRIPE",
  "estado": "EXITO",
  "alumnoId": "student123",
  "facturaCreada": false,
  "items": [],
  "stripePaymentIntentId": "pi_1234567890abcdefghijklmn",
  "stripeChargeId": "ch_1234567890abcdefghijklmn",
  "failureReason": null,
  "clientSecret": null
}
```

### 4. Check Payment Status (GET /api/pagos/{id}/status)

**Purpose:** Checks if a payment was successful (demo utility).

**Authorization:** 
- ADMIN/PROFESOR: Can check any payment
- ALUMNO: Can only check their own payments

**Example Request:**
```
GET /api/pagos/1/status
```

**Response (200 OK):**
```json
{
  "paymentId": 1,
  "isSuccessful": true,
  "status": "SUCCESS"
}
```

### 5. Get Recent Payments (GET /api/pagos/recent)

**Purpose:** Gets recent payments for demo purposes.

**Authorization:** ADMIN, PROFESOR only

**Query Parameters:**
- `limit` (int, optional): Maximum number of payments (1-50, default: 10)

**Example Request:**
```
GET /api/pagos/recent?limit=5
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "fechaPago": "2024-01-15T10:30:00",
    "importe": 99.99,
    "metodoPago": "STRIPE",
    "estado": "EXITO",
    "alumnoId": "student123",
    "facturaCreada": false,
    "items": [],
    "stripePaymentIntentId": "pi_1234567890abcdefghijklmn",
    "stripeChargeId": "ch_1234567890abcdefghijklmn",
    "failureReason": null,
    "clientSecret": null
  }
]
```

## Data Models

### Payment Status Enum (EEstadoPago)
```javascript
const PaymentStatus = {
  PENDIENTE: 'PENDIENTE',      // Payment pending
  PROCESANDO: 'PROCESANDO',    // Payment processing
  EXITO: 'EXITO',              // Payment successful
  ERROR: 'ERROR',              // Payment failed
  REEMBOLSADO: 'REEMBOLSADO'   // Payment refunded
};
```

### Payment Method Enum (EMetodoPago)
```javascript
const PaymentMethod = {
  EFECTIVO: 'EFECTIVO',        // Cash
  DEBITO: 'DEBITO',            // Debit card
  CREDITO: 'CREDITO',          // Credit card
  TRANSFERENCIA: 'TRANSFERENCIA', // Bank transfer
  STRIPE: 'STRIPE'             // Stripe payment
};
```

## Complete Frontend Integration Example

```javascript
class PaymentService {
  constructor(baseUrl, token) {
    this.baseUrl = baseUrl;
    this.token = token;
  }

  // Create a new payment
  async createPayment(paymentData) {
    const response = await fetch(`${this.baseUrl}/api/pagos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify(paymentData)
    });

    if (!response.ok) {
      throw new Error(`Payment creation failed: ${response.statusText}`);
    }

    return await response.json();
  }

  // Get paginated payments
  async getPayments(page = 0, size = 20, sortBy = 'fechaPago', sortDirection = 'DESC') {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDirection
    });

    const response = await fetch(`${this.baseUrl}/api/pagos?${params}`, {
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch payments: ${response.statusText}`);
    }

    return await response.json();
  }

  // Get specific payment
  async getPayment(id) {
    const response = await fetch(`${this.baseUrl}/api/pagos/${id}`, {
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch payment: ${response.statusText}`);
    }

    return await response.json();
  }

  // Check payment status
  async checkPaymentStatus(id) {
    const response = await fetch(`${this.baseUrl}/api/pagos/${id}/status`, {
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to check payment status: ${response.statusText}`);
    }

    return await response.json();
  }

  // Get recent payments
  async getRecentPayments(limit = 10) {
    const response = await fetch(`${this.baseUrl}/api/pagos/recent?limit=${limit}`, {
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch recent payments: ${response.statusText}`);
    }

    return await response.json();
  }
}

// Usage example with Stripe.js
class PaymentProcessor {
  constructor(stripe, paymentService) {
    this.stripe = stripe;
    this.paymentService = paymentService;
  }

  async processPayment(paymentData) {
    try {
      // 1. Create payment on backend
      const payment = await this.paymentService.createPayment(paymentData);
      
      // 2. Confirm payment with Stripe
      const { error } = await this.stripe.confirmPayment({
        clientSecret: payment.clientSecret,
        confirmParams: {
          return_url: window.location.origin + '/payment-success',
        },
      });

      if (error) {
        throw new Error(error.message);
      }

      return payment;
    } catch (error) {
      console.error('Payment processing failed:', error);
      throw error;
    }
  }

  async checkPaymentStatus(paymentId) {
    try {
      const status = await this.paymentService.checkPaymentStatus(paymentId);
      return status.isSuccessful;
    } catch (error) {
      console.error('Failed to check payment status:', error);
      return false;
    }
  }
}

// React component example
function PaymentForm({ onSuccess }) {
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const paymentData = {
        importe: parseFloat(amount),
        alumnoId: 'current-user-id', // Get from auth context
        description,
        currency: 'EUR'
      };

      const payment = await paymentProcessor.processPayment(paymentData);
      onSuccess(payment);
    } catch (error) {
      console.error('Payment failed:', error);
      // Handle error (show toast, etc.)
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="number"
        step="0.01"
        min="0.50"
        max="999999.99"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        placeholder="Amount (€)"
        required
      />
      <input
        type="text"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Payment description"
        required
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Processing...' : 'Pay'}
      </button>
    </form>
  );
}
```

## Error Handling

The API returns standard HTTP status codes:

- **200 OK**: Request successful
- **201 Created**: Payment created successfully
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Payment not found
- **500 Internal Server Error**: Server error

Error responses include details:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["importe must be greater than 0.01"]
}
```

## Security Notes

1. **JWT Token**: Always include valid JWT token in Authorization header
2. **Client Secret**: Only available in payment creation response, never stored in database
3. **Student Access**: Students can only access their own payments
4. **Admin/Professor Access**: Can view and create all payments
5. **Stripe Integration**: Uses Stripe webhooks for payment status updates

## Testing

For testing purposes, you can use the demo endpoints:
- `/api/pagos/{id}/status` - Check payment status
- `/api/pagos/recent` - Get recent payments

The system supports test mode with placeholder webhook secrets for easier development.
