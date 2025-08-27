# Svelte 5 + Stripe Integration Guide

## Overview
Your backend handles all the server-side Stripe integration (creating PaymentIntents, webhook processing, etc.), but you need Stripe.js on the frontend to collect payment details and confirm payments. This guide shows you how to integrate Stripe with Svelte 5 using OpenAPI-generated clients for type-safe API calls.

## Installation

### 1. Install Stripe.js
```bash
npm install @stripe/stripe-js
```

### 2. Install OpenAPI Generator
```bash
npm install --save-dev openapi-typescript-codegen
```

### 3. Get Your Stripe Publishable Key
You'll need your Stripe publishable key (starts with `pk_`). This is different from your backend's secret key.

## Setup

### 1. Generate API Client from OpenAPI
Your backend exposes OpenAPI documentation. Generate the TypeScript client:

```bash
# Generate API client from your backend OpenAPI spec
npx openapi --input http://localhost:8080/v3/api-docs --output src/lib/api

# Or if your backend uses a different OpenAPI endpoint
npx openapi --input http://localhost:8080/swagger-ui.html --output src/lib/api
```

This will create type-safe API clients for all your backend endpoints.

### 2. Create Stripe Configuration
Create a file for your Stripe configuration:

```javascript
// src/lib/stripe.js
import { loadStripe } from '@stripe/stripe-js';

// Replace with your actual publishable key
const STRIPE_PUBLISHABLE_KEY = 'pk_test_your_publishable_key_here';

export const stripe = await loadStripe(STRIPE_PUBLISHABLE_KEY);
```

### 3. Create Payment Service with OpenAPI Client
Create a service using the auto-generated API client:

```javascript
// src/lib/paymentService.js
import { PaymentControllerApi } from '$lib/api'; // Auto-generated

export class PaymentService {
  constructor(token) {
    this.api = new PaymentControllerApi({
      basePath: 'http://localhost:8080',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  async createPayment(paymentData) {
    return await this.api.createPayment(paymentData);
  }

  async getPayment(id) {
    return await this.api.getPayment(id);
  }

  async checkPaymentStatus(id) {
    return await this.api.checkPaymentStatus(id);
  }

  async getPayments(page = 0, size = 20, sortBy = 'fechaPago', sortDirection = 'DESC') {
    return await this.api.getPayments(page, size, sortBy, sortDirection);
  }

  async getRecentPayments(limit = 10) {
    return await this.api.getRecentPayments(limit);
  }
}
```

### 4. Add Script to package.json
Add a script to regenerate the API client when your backend changes:

```json
{
  "scripts": {
    "generate-api": "openapi --input http://localhost:8080/v3/api-docs --output src/lib/api"
  }
}
```

## Svelte 5 Components

### 1. Basic Payment Form Component

```svelte
<!-- src/lib/components/PaymentForm.svelte -->
<script>
  import { stripe } from '$lib/stripe.js';
  import { PaymentService } from '$lib/paymentService.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  // Props
  export let amount = 0;
  export let description = '';
  export let studentId = '';
  export let onSuccess = () => {};
  export let onError = () => {};

  // State
  let loading = false;
  let error = '';
  let paymentService;

  onMount(() => {
    // Initialize payment service with auth token
    paymentService = new PaymentService(
      localStorage.getItem('jwt_token') // Get from your auth system
    );
  });

  async function handlePayment() {
    if (!stripe) {
      error = 'Stripe not loaded';
      return;
    }

    loading = true;
    error = '';

    try {
      // 1. Create payment on your backend
      const paymentData = {
        importe: amount,
        alumnoId: studentId,
        description,
        currency: 'EUR'
      };

      const payment = await paymentService.createPayment(paymentData);

      // 2. Confirm payment with Stripe
      const { error: stripeError } = await stripe.confirmPayment({
        clientSecret: payment.clientSecret,
        confirmParams: {
          return_url: `${window.location.origin}/payment-success?payment_id=${payment.id}`,
        },
      });

      if (stripeError) {
        throw new Error(stripeError.message);
      }

      onSuccess(payment);
    } catch (err) {
      error = err.message;
      onError(err);
    } finally {
      loading = false;
    }
  }
</script>

<div class="payment-form">
  <h2>Complete Payment</h2>
  
  <div class="payment-details">
    <p><strong>Amount:</strong> €{amount.toFixed(2)}</p>
    <p><strong>Description:</strong> {description}</p>
  </div>

  {#if error}
    <div class="error">
      {error}
    </div>
  {/if}

  <button 
    on:click={handlePayment} 
    disabled={loading || !stripe}
    class="pay-button"
  >
    {loading ? 'Processing...' : 'Pay €{amount.toFixed(2)}'}
  </button>
</div>
```

### 2. Advanced Payment Form with Stripe Elements

For more control over the payment form, use Stripe Elements:

```svelte
<!-- src/lib/components/StripePaymentForm.svelte -->
<script>
  import { stripe } from '$lib/stripe.js';
  import { PaymentService } from '$lib/paymentService.js';
  import { onMount, onDestroy } from 'svelte';

  // Props
  export let amount = 0;
  export let description = '';
  export let studentId = '';
  export let onSuccess = () => {};
  export let onError = () => {};

  // State
  let loading = false;
  let error = '';
  let paymentService;
  let elements;
  let paymentElement;
  let paymentForm;

  onMount(async () => {
    paymentService = new PaymentService(
      localStorage.getItem('jwt_token')
    );

    if (stripe) {
      // Create payment intent first
      try {
        const payment = await paymentService.createPayment({
          importe: amount,
          alumnoId: studentId,
          description,
          currency: 'EUR'
        });

        // Create elements
        elements = stripe.elements({
          clientSecret: payment.clientSecret,
          appearance: {
            theme: 'stripe',
            variables: {
              colorPrimary: '#6772e5',
            }
          }
        });

        // Create payment element
        paymentElement = elements.create('payment');
        paymentElement.mount('#payment-element');
      } catch (err) {
        error = err.message;
      }
    }
  });

  onDestroy(() => {
    if (paymentElement) {
      paymentElement.destroy();
    }
  });

  async function handleSubmit(event) {
    event.preventDefault();
    
    if (!stripe || !elements) {
      return;
    }

    loading = true;
    error = '';

    const { error: submitError } = await elements.submit();

    if (submitError) {
      error = submitError.message;
      loading = false;
      return;
    }

    // Confirm payment
    const { error: confirmError } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/payment-success`,
      },
    });

    if (confirmError) {
      error = confirmError.message;
    }

    loading = false;
  }
</script>

<form on:submit={handleSubmit} bind:this={paymentForm}>
  <div class="payment-form">
    <h2>Payment Details</h2>
    
    <div class="payment-summary">
      <p><strong>Amount:</strong> €{amount.toFixed(2)}</p>
      <p><strong>Description:</strong> {description}</p>
    </div>

    <div id="payment-element">
      <!-- Stripe Elements will be mounted here -->
    </div>

    {#if error}
      <div class="error">
        {error}
      </div>
    {/if}

    <button 
      type="submit" 
      disabled={loading || !stripe}
      class="pay-button"
    >
      {loading ? 'Processing...' : 'Pay €{amount.toFixed(2)}'}
    </button>
  </div>
</form>
```

### 3. Payment Success Page

```svelte
<!-- src/routes/payment-success/+page.svelte -->
<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { PaymentService } from '$lib/paymentService.js';

  let paymentStatus = 'checking';
  let payment = null;
  let error = '';

  onMount(async () => {
    const paymentId = $page.url.searchParams.get('payment_id');
    
    if (!paymentId) {
      paymentStatus = 'error';
      error = 'No payment ID found';
      return;
    }

    try {
      const paymentService = new PaymentService(
        localStorage.getItem('jwt_token')
      );

      // Check payment status
      const status = await paymentService.checkPaymentStatus(paymentId);
      
      if (status.isSuccessful) {
        paymentStatus = 'success';
        payment = await paymentService.getPayment(paymentId);
      } else {
        paymentStatus = 'failed';
      }
    } catch (err) {
      paymentStatus = 'error';
      error = err.message;
    }
  });
</script>

<svelte:head>
  <title>Payment Result</title>
</svelte:head>

<div class="payment-result">
  {#if paymentStatus === 'checking'}
    <div class="loading">
      <h2>Processing Payment...</h2>
      <p>Please wait while we confirm your payment.</p>
    </div>
  {:else if paymentStatus === 'success'}
    <div class="success">
      <h2>Payment Successful! 🎉</h2>
      {#if payment}
        <div class="payment-details">
          <p><strong>Amount:</strong> €{payment.importe}</p>
          <p><strong>Date:</strong> {new Date(payment.fechaPago).toLocaleDateString()}</p>
          <p><strong>Status:</strong> {payment.estado}</p>
        </div>
      {/if}
      <a href="/dashboard" class="button">Go to Dashboard</a>
    </div>
  {:else if paymentStatus === 'failed'}
    <div class="failed">
      <h2>Payment Failed</h2>
      <p>Your payment could not be processed. Please try again.</p>
      <a href="/payment" class="button">Try Again</a>
    </div>
  {:else if paymentStatus === 'error'}
    <div class="error">
      <h2>Error</h2>
      <p>{error}</p>
      <a href="/payment" class="button">Back to Payment</a>
    </div>
  {/if}
</div>
```

### 4. Payment History Component

```svelte
<!-- src/lib/components/PaymentHistory.svelte -->
<script>
  import { onMount } from 'svelte';
  import { PaymentService } from '$lib/paymentService.js';

  let payments = [];
  let loading = true;
  let error = '';

  onMount(async () => {
    try {
      const paymentService = new PaymentService(
        localStorage.getItem('jwt_token')
      );

      // Get recent payments (adjust based on your needs)
      const response = await paymentService.getPayments(0, 10);
      payments = response.contenido || [];
    } catch (err) {
      error = err.message;
    } finally {
      loading = false;
    }
  });

  function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString();
  }

  function getStatusColor(status) {
    switch (status) {
      case 'EXITO': return '#2e7d32';
      case 'PENDIENTE': return '#f57c00';
      case 'ERROR': return '#d32f2f';
      default: return '#666';
    }
  }
</script>

<div class="payment-history">
  <h2>Payment History</h2>

  {#if loading}
    <p>Loading payments...</p>
  {:else if error}
    <p class="error">{error}</p>
  {:else if payments.length === 0}
    <p>No payments found.</p>
  {:else}
    <div class="payments-list">
      {#each payments as payment}
        <div class="payment-item">
          <div class="payment-header">
            <span class="payment-id">#{payment.id}</span>
            <span 
              class="payment-status" 
              style="color: {getStatusColor(payment.estado)}"
            >
              {payment.estado}
            </span>
          </div>
          <div class="payment-details">
            <p><strong>Amount:</strong> €{payment.importe}</p>
            <p><strong>Date:</strong> {formatDate(payment.fechaPago)}</p>
            <p><strong>Method:</strong> {payment.metodoPago}</p>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>
```

## Usage Examples

### 1. Simple Payment Page

```svelte
<!-- src/routes/payment/+page.svelte -->
<script>
  import PaymentForm from '$lib/components/PaymentForm.svelte';
  import { goto } from '$app/navigation';

  let amount = 99.99;
  let description = 'Advanced Mathematics Course';
  let studentId = 'current-user-id'; // Get from your auth system

  function handleSuccess(payment) {
    // Redirect to success page
    goto(`/payment-success?payment_id=${payment.id}`);
  }

  function handleError(error) {
    console.error('Payment failed:', error);
    // Show error toast or handle error
  }
</script>

<svelte:head>
  <title>Payment</title>
</svelte:head>

<div class="payment-page">
  <h1>Complete Your Payment</h1>
  
  <PaymentForm 
    {amount}
    {description}
    {studentId}
    onSuccess={handleSuccess}
    onError={handleError}
  />
</div>

<style>
  .payment-page {
    max-width: 600px;
    margin: 50px auto;
    padding: 20px;
  }

  h1 {
    text-align: center;
    margin-bottom: 30px;
  }
</style>
```

### 2. Course Purchase Page

```svelte
<!-- src/routes/courses/[id]/purchase/+page.svelte -->
<script>
  import { page } from '$app/stores';
  import PaymentForm from '$lib/components/PaymentForm.svelte';
  import { goto } from '$app/navigation';

  // Get course data (you'll need to implement this)
  let course = null;
  let loading = true;

  onMount(async () => {
    const courseId = $page.params.id;
    // Fetch course details
    // course = await getCourse(courseId);
    loading = false;
  });

  function handlePaymentSuccess(payment) {
    goto(`/payment-success?payment_id=${payment.id}`);
  }
</script>

{#if loading}
  <p>Loading...</p>
{:else if course}
  <div class="purchase-page">
    <h1>Purchase {course.title}</h1>
    
    <div class="course-summary">
      <h2>{course.title}</h2>
      <p>{course.description}</p>
      <p class="price">€{course.price}</p>
    </div>

    <PaymentForm 
      amount={course.price}
      description={`Payment for ${course.title}`}
      studentId="current-user-id"
      onSuccess={handlePaymentSuccess}
    />
  </div>
{/if}
```

## Environment Variables

Create a `.env` file in your Svelte project root:

```env
PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
PUBLIC_BACKEND_URL=http://localhost:8080
```

Then update your Stripe configuration:

```javascript
// src/lib/stripe.js
import { loadStripe } from '@stripe/stripe-js';

const STRIPE_PUBLISHABLE_KEY = import.meta.env.PUBLIC_STRIPE_PUBLISHABLE_KEY;

export const stripe = await loadStripe(STRIPE_PUBLISHABLE_KEY);
```

## Type Safety with OpenAPI

The auto-generated API client provides full TypeScript support:

```typescript
// Auto-generated types from your backend
import type { DTOPago, DTOPeticionCrearPago } from '$lib/api';

// Type-safe payment creation
const paymentData: DTOPeticionCrearPago = {
  importe: 99.99,
  alumnoId: 'student123',
  description: 'Course payment',
  currency: 'EUR'
};

// Type-safe response
const payment: DTOPago = await paymentService.createPayment(paymentData);
```

### Benefits of OpenAPI Integration:
- ✅ **Full TypeScript support** with auto-generated types
- ✅ **IntelliSense and autocomplete** in your IDE
- ✅ **Compile-time error checking** for API calls
- ✅ **Automatic API updates** when backend changes
- ✅ **Consistent error handling** across all endpoints

## Key Points

1. **Backend Handles**: Payment intent creation, webhook processing, database storage
2. **Frontend Handles**: Payment form UI, Stripe Elements, payment confirmation
3. **Security**: Never expose your Stripe secret key on the frontend
4. **Testing**: Use Stripe test keys and test card numbers for development
5. **Error Handling**: Always handle payment errors gracefully
6. **User Experience**: Show loading states and clear success/failure messages

## Testing

### Test Card Numbers
Use Stripe's test card numbers:
- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **Requires Authentication**: `4000 0025 0000 3155`
- **3D Secure**: `4000 0025 0000 3155`

### Stripe CLI for Webhook Testing
Install Stripe CLI to test webhooks locally:

```bash
# Install Stripe CLI (Windows)
# Download from: https://github.com/stripe/stripe-cli/releases

# Or use Chocolatey
choco install stripe-cli

# Login to your Stripe account
stripe login

# Forward webhooks to your local backend
stripe listen --forward-to localhost:8080/api/pagos/stripe/webhook
```

This will give you a webhook endpoint URL to use in your Stripe Dashboard for testing.

### Test Payment Flow
1. Start your backend server
2. Start Stripe CLI webhook forwarding
3. Use test card numbers in your frontend
4. Check webhook events in Stripe CLI output
5. Verify payment status in your database

## Quick Start Checklist

### ✅ **Step 1: Install Dependencies**
```bash
npm install @stripe/stripe-js
npm install --save-dev openapi-typescript-codegen
```

### ✅ **Step 2: Generate API Client**
```bash
# Make sure your backend is running
npx openapi --input http://localhost:8080/v3/api-docs --output src/lib/api
```

### ✅ **Step 3: Get Your Stripe Keys**
1. Go to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Navigate to Developers → API Keys
3. Copy your **Publishable Key** (starts with `pk_test_`)

### ✅ **Step 4: Set Up Environment Variables**
Create `.env` file in your Svelte project root:
```env
PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
PUBLIC_BACKEND_URL=http://localhost:8080
```

### ✅ **Step 5: Create Required Files**
1. `src/lib/stripe.js` - Stripe configuration
2. `src/lib/paymentService.js` - API service (using OpenAPI client)
3. `src/lib/components/PaymentForm.svelte` - Payment form
4. `src/routes/payment-success/+page.svelte` - Success page

### ✅ **Step 6: Test Your Integration**
1. Start your backend server
2. Start Stripe CLI webhook forwarding
3. Test with card number: `4242 4242 4242 4242`
4. Verify payment appears in your database

### ✅ **Step 7: Production Deployment**
1. Switch to live Stripe keys
2. Update webhook endpoint in Stripe Dashboard
3. Test with real payment methods
4. Monitor webhook events

### ✅ **Step 8: API Client Updates**
When your backend API changes, regenerate the client:
```bash
npm run generate-api
```

## Troubleshooting

### Common Issues

**"Stripe not loaded" error:**
- Check your publishable key is correct
- Ensure Stripe.js is properly imported

**Webhook not receiving events:**
- Verify webhook endpoint URL in Stripe Dashboard
- Check Stripe CLI is forwarding correctly
- Ensure your backend endpoint is accessible

**Payment confirmation fails:**
- Verify `clientSecret` is being passed correctly
- Check payment intent status in Stripe Dashboard
- Ensure return URL is properly configured

**CORS errors:**
- Add your frontend domain to CORS configuration in backend
- Check `CrossOrigin` annotations in your REST controllers

**OpenAPI generation fails:**
- Ensure your backend is running and accessible
- Check the OpenAPI endpoint URL is correct
- Verify your backend exposes OpenAPI documentation at `/v3/api-docs`

**Type errors with API client:**
- Regenerate the API client after backend changes: `npm run generate-api`
- Check that your backend OpenAPI spec is up to date
- Ensure all required fields are provided in API calls

This setup gives you a complete Stripe integration with your Svelte 5 frontend while keeping all sensitive operations on your secure backend!
