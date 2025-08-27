# Payment Implementation Guide

## Overview
This document describes the complete payment functionality implementation using Stripe and your backend API. The implementation includes both basic and advanced payment forms, payment history, and success/failure handling.

## Architecture

### Backend Integration
- **Payment Creation**: Backend creates PaymentIntents and returns client secrets
- **Webhook Processing**: Backend handles Stripe webhooks for payment status updates
- **Database Storage**: All payment records are stored in your database
- **Security**: Sensitive operations (secret keys, webhooks) are handled server-side

### Frontend Integration
- **Stripe.js**: Client-side payment processing with Stripe Elements
- **Type Safety**: Full TypeScript support with auto-generated API types
- **Authentication**: Integrated with your existing auth system
- **Error Handling**: Comprehensive error handling and user feedback

## Components

### 1. PaymentForm.svelte
**Location**: `src/lib/components/PaymentForm.svelte`

A simple payment form that creates a payment intent and redirects to Stripe's hosted payment page.

**Props**:
- `amount`: Payment amount in euros
- `description`: Payment description
- `studentId`: Student ID (automatically from auth store)
- `onSuccess`: Callback for successful payment
- `onError`: Callback for payment errors

**Usage**:
```svelte
<PaymentForm
  amount={99.99}
  description="Advanced Mathematics Course"
  studentId={authStore.user?.id?.toString()}
  onSuccess={handleSuccess}
  onError={handleError}
/>
```

### 2. StripePaymentForm.svelte
**Location**: `src/lib/components/StripePaymentForm.svelte`

Advanced payment form with embedded Stripe Elements for more control over the payment experience.

**Features**:
- Embedded payment form
- Custom styling
- Real-time validation
- Better user experience

### 3. PaymentHistory.svelte
**Location**: `src/lib/components/PaymentHistory.svelte`

Displays recent payment history with status indicators and payment details.

### 4. Payment Pages

#### `/payment`
Simple payment page with a single payment form.

#### `/pagos`
Comprehensive payment management page with:
- Quick payment options
- Payment history
- Modal payment form

#### `/payment-success`
Payment result page that shows success/failure status.

## Services

### PagoService
**Location**: `src/lib/services/pagoService.ts`

Provides type-safe API calls to your backend payment endpoints.

**Methods**:
- `createPayment()`: Create a new payment
- `getPayment()`: Get payment by ID
- `getPayments()`: Get paginated payment list
- `checkPaymentStatus()`: Check payment status
- `getRecentPayments()`: Get recent payments
- `processStripeWebhook()`: Process Stripe webhooks

## Authentication Integration

The payment system is fully integrated with your authentication system:

```typescript
// Get current user ID from auth store
$: studentId = authStore.user?.id?.toString() || '';

// Redirect unauthenticated users
$: if (!authStore.isAuthenticated) {
  goto('/auth');
}
```

## Environment Setup

### 1. Environment Variables
Create a `.env` file in your project root:

```env
# Stripe Configuration
PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key_here

# Backend Configuration
PUBLIC_BACKEND_URL=http://localhost:8080
```

### 2. Stripe Configuration
**Location**: `src/lib/stripe.ts`

Automatically loads Stripe.js with your publishable key.

## Usage Examples

### Basic Payment Flow
```svelte
<script>
  import PaymentForm from '$lib/components/PaymentForm.svelte';
  import { authStore } from '$lib/stores/authStore.svelte';
  import { goto } from '$app/navigation';

  function handleSuccess(payment) {
    goto(`/payment-success?payment_id=${payment.id}`);
  }

  function handleError(error) {
    console.error('Payment failed:', error);
  }
</script>

<PaymentForm
  amount={99.99}
  description="Course Payment"
  studentId={authStore.user?.id?.toString()}
  onSuccess={handleSuccess}
  onError={handleError}
/>
```

### Payment Management Page
```svelte
<script>
  import PaymentHistory from '$lib/components/PaymentHistory.svelte';
  import PaymentForm from '$lib/components/PaymentForm.svelte';
</script>

<div class="payment-management">
  <h1>Payment Management</h1>
  
  <!-- Quick payment options -->
  <div class="quick-payments">
    <button on:click={() => openPaymentForm(25, 'Basic Course')}>
      Basic Course - €25
    </button>
    <button on:click={() => openPaymentForm(50, 'Advanced Course')}>
      Advanced Course - €50
    </button>
  </div>
  
  <!-- Payment history -->
  <PaymentHistory />
</div>
```

## Testing

### Test Card Numbers
Use these Stripe test card numbers:

- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **Requires Authentication**: `4000 0025 0000 3155`
- **3D Secure**: `4000 0025 0000 3155`

### Test Flow
1. Start your backend server
2. Set up Stripe CLI for webhook testing
3. Use test card numbers in your frontend
4. Verify payment status in your database

## Security Considerations

1. **Never expose secret keys** on the frontend
2. **Always verify webhook signatures** on the backend
3. **Use HTTPS** in production
4. **Validate payment amounts** on the backend
5. **Check user permissions** before allowing payments

## Error Handling

The implementation includes comprehensive error handling:

- **Network errors**: Connection issues with backend
- **Stripe errors**: Payment processing failures
- **Authentication errors**: Unauthorized access
- **Validation errors**: Invalid payment data

## Production Deployment

### 1. Switch to Live Keys
Update your environment variables with live Stripe keys:
```env
PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_live_your_live_key_here
```

### 2. Update Webhook Endpoint
In your Stripe Dashboard, update the webhook endpoint to your production URL:
```
https://yourdomain.com/api/pagos/stripe/webhook
```

### 3. Test with Real Payments
- Test with small amounts first
- Verify webhook processing
- Monitor payment status updates

## API Integration

The payment system uses your backend's OpenAPI-generated endpoints:

- `POST /api/pagos` - Create payment
- `GET /api/pagos/{id}` - Get payment details
- `GET /api/pagos/{id}/status` - Check payment status
- `GET /api/pagos` - List payments
- `POST /api/pagos/stripe/webhook` - Process webhooks

## Troubleshooting

### Common Issues

1. **"Stripe not loaded" error**
   - Check your publishable key is correct
   - Ensure Stripe.js is properly imported

2. **Payment confirmation fails**
   - Verify `clientSecret` is being passed correctly
   - Check payment intent status in Stripe Dashboard

3. **Webhook not receiving events**
   - Verify webhook endpoint URL in Stripe Dashboard
   - Check Stripe CLI is forwarding correctly

4. **Authentication errors**
   - Ensure user is logged in
   - Check JWT token is valid
   - Verify user has permission to make payments

## Next Steps

1. **Customize Payment Forms**: Modify styling and layout
2. **Add Payment Analytics**: Track payment metrics
3. **Implement Refunds**: Add refund functionality
4. **Add Payment Plans**: Support recurring payments
5. **Multi-currency Support**: Add support for other currencies

## Support

For issues or questions:
1. Check the Stripe documentation
2. Review your backend logs
3. Check the browser console for errors
4. Verify your environment variables are set correctly
