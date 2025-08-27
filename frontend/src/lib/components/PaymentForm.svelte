<script lang="ts">
	import { getStripe } from '$lib/stripe.js';
	import type { Stripe } from '@stripe/stripe-js';
	import { PagoService } from '$lib/services/pagoService.js';

	// Generate unique component ID for debugging
	const componentId = Math.random().toString(36).substring(2, 9);
	console.log(`PaymentForm component ${componentId} initialized`);

	// Props using Svelte 5 syntax
	const {
		amount = 0,
		description = '',
		studentId = '',
		onError = () => {}
	} = $props<{
		amount?: number;
		description?: string;
		studentId?: string;
		onError?: (error: Error) => void;
	}>();

	// State using Svelte 5 syntax
	let loading = $state(false);
	let error = $state('');
	let stripeInstance = $state<Stripe | null>(null);
	let stripeLoaded = $state(false);
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let elements = $state<any>(null);
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let paymentElement = $state<any>(null);
	let paymentForm = $state<HTMLFormElement | null>(null);
	let paymentElementReady = $state(false);
	let currentPaymentId = $state<string | null>(null);
	let paymentVerificationInterval = $state<NodeJS.Timeout | null>(null);

	// Load Stripe on component mount
	$effect(() => {
		console.log(`PaymentForm ${componentId}: Loading Stripe`);
		loadStripe();
	});

	async function loadStripe(retryCount = 0) {
		const maxRetries = 3;

		try {
			stripeInstance = await getStripe();
			stripeLoaded = true;
			console.log(`PaymentForm ${componentId}: Stripe loaded successfully`);
		} catch (err) {
			console.error(
				`PaymentForm ${componentId}: Failed to load Stripe (attempt ${retryCount + 1}):`,
				err
			);

			if (retryCount < maxRetries) {
				console.log(`PaymentForm ${componentId}: Retrying in 1 second...`);
				setTimeout(() => loadStripe(retryCount + 1), 1000);
			} else {
				error = 'Payment system is currently unavailable. Please refresh the page and try again.';
			}
		}
	}

	// Derived values
	const isDisabled = $derived(loading || !stripeLoaded || !paymentElementReady);
	const buttonText = $derived(loading ? 'Processing...' : `Pay €${amount.toFixed(2)}`);

	async function initializePayment() {
		if (!stripeInstance) {
			error = 'Payment system not loaded';
			return;
		}

		try {
			// 1. Create payment on your backend
			const paymentData = {
				importe: amount,
				alumnoId: studentId,
				description,
				currency: 'EUR'
			};

			console.log(`PaymentForm ${componentId}: Creating payment with data:`, paymentData);
			const payment = await PagoService.createPayment(paymentData);

			// === FRONTEND PAYMENT RESPONSE DEBUGGING ===
			console.log('=== FRONTEND PAYMENT RESPONSE ===');
			console.log('Full response:', payment);
			console.log('Client Secret from response:', payment.clientSecret);
			console.log('Payment Intent ID from response:', payment.stripePaymentIntentId);
			console.log('Payment ID:', payment.id);
			console.log('Payment Status:', payment.estado);
			console.log('==================================');

			// Verify client secret is not truncated
			if (payment.clientSecret && payment.clientSecret.length < 50) {
				console.warn('WARNING: Client secret appears to be truncated!');
				console.warn('Expected length: ~100+ characters');
				console.warn('Actual length:', payment.clientSecret.length);
			}

			// 2. Create Stripe Elements
			console.log(
				`PaymentForm ${componentId}: Creating Stripe Elements with client secret:`,
				payment.clientSecret?.substring(0, 20) + '...'
			);

			elements = stripeInstance.elements({
				clientSecret: payment.clientSecret || '',
				appearance: {
					theme: 'stripe',
					variables: {
						colorPrimary: '#3b82f6'
					}
				}
			});

			// 3. Create and mount payment element
			console.log(`PaymentForm ${componentId}: Creating payment element`);
			paymentElement = elements.create('payment');

			console.log(`PaymentForm ${componentId}: Mounting payment element to #payment-element`);
			paymentElement.mount('#payment-element');

			// Store the payment ID for use in handleSubmit
			currentPaymentId = payment.id?.toString() || null;

			console.log(`PaymentForm ${componentId}: Stripe Elements initialized successfully`);
			// Mark payment element as ready
			paymentElementReady = true;
			// Don't call onSuccess here - only call it after successful payment submission
		} catch (err: unknown) {
			console.error(`PaymentForm ${componentId}: Error initializing payment:`, err);
			const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
			error = errorMessage;
			onError(err instanceof Error ? err : new Error(errorMessage));
		}
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!stripeInstance || !elements) {
			error = 'Payment system not ready';
			return;
		}

		loading = true;
		error = '';

		try {
			// 1. Submit the payment element
			console.log(`PaymentForm ${componentId}: Submitting payment element`);
			const { error: submitError } = await elements.submit();

			if (submitError) {
				console.error(`PaymentForm ${componentId}: Submit error:`, submitError);
				throw new Error(submitError.message);
			}

			// 2. Confirm the payment
			console.log(
				`PaymentForm ${componentId}: Confirming payment with return URL: ${window.location.origin}/payment-success?payment_id=${currentPaymentId}`
			);

			const returnUrl = `${window.location.origin}/payment-success?payment_id=${currentPaymentId}`;

			// Use redirect: 'if_required' to prevent automatic redirect
			const { error: confirmError } = await stripeInstance.confirmPayment({
				elements,
				confirmParams: {
					return_url: returnUrl
				},
				redirect: 'if_required'
			});

			// If no error and payment was successful, open success page in new window
			if (!confirmError) {
				console.log(
					`PaymentForm ${componentId}: Payment confirmed successfully, opening success page in new window`
				);
				window.open(returnUrl, '_blank', 'width=800,height=600');
			}

			if (confirmError) {
				console.error(`PaymentForm ${componentId}: Confirmation error:`, confirmError);
				console.error(`PaymentForm ${componentId}: Error details:`, {
					type: confirmError.type,
					code: confirmError.code,
					message: confirmError.message,
					payment_intent: confirmError.payment_intent
				});

				// Handle specific error types
				switch (confirmError.type) {
					case 'card_error':
					case 'validation_error':
						throw new Error(confirmError.message || 'Payment validation failed');
					case 'authentication_error':
						throw new Error('Payment authentication failed. Please try again.');
					default:
						throw new Error(confirmError.message || 'Payment failed. Please try again.');
				}
			}

			console.log(`PaymentForm ${componentId}: Payment submitted successfully`);

			// Start payment verification polling (as backup to webhooks)
			if (currentPaymentId) {
				startPaymentVerificationPolling(parseInt(currentPaymentId));
			}

			// Note: User will be redirected to return_url automatically by Stripe
			// No need to call onSuccess here as the redirect will happen automatically
		} catch (err: unknown) {
			console.error(`PaymentForm ${componentId}: Payment error:`, err);
			const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
			error = errorMessage;
			onError(err instanceof Error ? err : new Error(errorMessage));
		} finally {
			loading = false;
		}
	}

	// Initialize payment when component is ready
	$effect(() => {
		if (stripeLoaded && stripeInstance) {
			initializePayment();
		}
	});

	// Payment verification polling function
	async function startPaymentVerificationPolling(paymentId: number) {
		const maxAttempts = 10; // 10 attempts with 2-second intervals = 20 seconds
		let attempts = 0;

		const poll = async () => {
			if (attempts >= maxAttempts) {
				console.log(
					`PaymentForm ${componentId}: Payment verification polling stopped after ${maxAttempts} attempts`
				);
				return;
			}

			try {
				const status = await PagoService.checkPaymentStatus(paymentId);
				if (status.isSuccessful) {
					console.log(`PaymentForm ${componentId}: Payment verified as successful via polling`);
					stopPaymentVerificationPolling();
					return;
				}
			} catch (error) {
				console.warn(
					`PaymentForm ${componentId}: Payment verification polling attempt ${attempts + 1} failed:`,
					error
				);
			}

			attempts++;
			paymentVerificationInterval = setTimeout(poll, 2000); // Poll every 2 seconds
		};

		// Start polling after a short delay to allow webhooks to process first
		paymentVerificationInterval = setTimeout(poll, 1000);
	}

	function stopPaymentVerificationPolling() {
		if (paymentVerificationInterval) {
			clearTimeout(paymentVerificationInterval);
			paymentVerificationInterval = null;
		}
	}

	// Cleanup on unmount
	$effect(() => {
		return () => {
			if (paymentElement) {
				paymentElement.destroy();
			}
			stopPaymentVerificationPolling();
		};
	});
</script>

<form onsubmit={handleSubmit} bind:this={paymentForm}>
	<div class="payment-form">
		<h2>Complete Payment</h2>

		<div class="payment-details">
			<p><strong>Amount:</strong> €{amount.toFixed(2)}</p>
			<p><strong>Description:</strong> {description}</p>
		</div>

		{#if !paymentElementReady}
			<div class="payment-element-loading">
				<p>Loading payment form...</p>
			</div>
		{/if}

		<div id="payment-element">
			<!-- Stripe Elements will be mounted here -->
		</div>

		{#if error}
			<div class="error">
				{error}
			</div>
		{/if}

		<button type="submit" disabled={isDisabled} class="pay-button">
			{buttonText}
		</button>

		<p class="payment-note">
			💡 The payment will open in a new window so you can keep this console open for debugging.
		</p>
	</div>
</form>

<style>
	.payment-form {
		max-width: 500px;
		margin: 0 auto;
		padding: 2rem;
		border: 1px solid #e5e7eb;
		border-radius: 0.5rem;
		background-color: white;
		box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
	}

	h2 {
		text-align: center;
		margin-bottom: 1.5rem;
		color: #1f2937;
		font-size: 1.5rem;
		font-weight: 600;
	}

	.payment-details {
		margin-bottom: 1.5rem;
		padding: 1rem;
		background-color: #f9fafb;
		border-radius: 0.375rem;
	}

	.payment-details p {
		margin: 0.5rem 0;
		color: #374151;
	}

	#payment-element {
		margin-bottom: 1.5rem;
	}

	.payment-element-loading {
		padding: 2rem;
		text-align: center;
		background-color: #f9fafb;
		border: 1px solid #e5e7eb;
		border-radius: 0.375rem;
		color: #6b7280;
	}

	.error {
		margin-bottom: 1rem;
		padding: 0.75rem;
		background-color: #fef2f2;
		border: 1px solid #fecaca;
		border-radius: 0.375rem;
		color: #dc2626;
		font-size: 0.875rem;
	}

	.pay-button {
		width: 100%;
		padding: 0.75rem 1.5rem;
		background-color: #3b82f6;
		color: white;
		border: none;
		border-radius: 0.375rem;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.pay-button:hover:not(:disabled) {
		background-color: #2563eb;
	}

	.pay-button:disabled {
		background-color: #9ca3af;
		cursor: not-allowed;
	}

	/* Stripe Elements styling */
	:global(.StripeElement) {
		padding: 0.75rem;
		border: 1px solid #d1d5db;
		border-radius: 0.375rem;
		background-color: white;
	}

	:global(.StripeElement--focus) {
		border-color: #3b82f6;
		box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
	}

	:global(.StripeElement--invalid) {
		border-color: #dc2626;
	}

	.payment-note {
		margin-top: 1rem;
		padding: 0.75rem;
		background-color: #f0f9ff;
		border: 1px solid #bae6fd;
		border-radius: 0.375rem;
		color: #0369a1;
		font-size: 0.875rem;
		text-align: center;
	}
</style>
