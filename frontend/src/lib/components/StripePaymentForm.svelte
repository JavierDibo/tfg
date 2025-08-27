<script lang="ts">
	import { getStripe } from '$lib/stripe.js';
	import type { Stripe } from '@stripe/stripe-js';
	import { PagoService } from '$lib/services/pagoService.js';

	// Props using Svelte 5 syntax
	const {
		amount = 0,
		description = '',
		studentId = ''
	} = $props<{
		amount?: number;
		description?: string;
		studentId?: string;
	}>();

	// State using Svelte 5 syntax
	let loading = $state(false);
	let error = $state('');
	let stripeInstance = $state<Stripe | null>(null);
	let stripeLoaded = $state(false);
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let elements: any; // Stripe Elements type
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	let paymentElement: any; // Stripe PaymentElement type
	let paymentForm: HTMLFormElement;

	// Load Stripe on component mount
	$effect(() => {
		loadStripe();
	});

	async function loadStripe() {
		try {
			stripeInstance = await getStripe();
			stripeLoaded = true;
		} catch (err) {
			console.error('Failed to load Stripe:', err);
			error = 'Payment system is currently unavailable. Please try again later.';
		}
	}

	// Derived values
	const isDisabled = $derived(loading || !stripeLoaded);
	const buttonText = $derived(loading ? 'Processing...' : `Pay €${amount.toFixed(2)}`);

	// Initialize Stripe elements using $effect
	$effect(() => {
		if (stripeLoaded && stripeInstance) {
			initializeStripeElements();
		}

		// Cleanup function
		return () => {
			if (paymentElement) {
				paymentElement.destroy();
			}
		};
	});

	async function initializeStripeElements() {
		if (!stripeInstance) return;

		try {
			// Create payment intent first
			const payment = await PagoService.createPayment({
				importe: amount,
				alumnoId: studentId,
				description,
				currency: 'EUR'
			});

			// Create elements
			elements = stripeInstance.elements({
				clientSecret: payment.clientSecret || '',
				appearance: {
					theme: 'stripe',
					variables: {
						colorPrimary: '#6772e5'
					}
				}
			});

			// Create payment element
			paymentElement = elements.create('payment');
			paymentElement.mount('#payment-element');
		} catch (err: unknown) {
			error = err instanceof Error ? err.message : 'An unknown error occurred';
		}
	}

	async function handleSubmit(event: Event) {
		event.preventDefault();

		if (!stripeInstance || !elements) {
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
		const { error: confirmError } = await stripeInstance.confirmPayment({
			elements,
			confirmParams: {
				return_url: `${window.location.origin}/payment-success`
			}
		});

		if (confirmError) {
			error = confirmError.message || 'Payment confirmation failed';
			loading = false;
		}
	}
</script>

<form onsubmit={handleSubmit} bind:this={paymentForm}>
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

		<button type="submit" disabled={isDisabled} class="pay-button">
			{buttonText}
		</button>
	</div>
</form>

<style>
	.payment-form {
		max-width: 600px;
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

	.payment-summary {
		margin-bottom: 1.5rem;
		padding: 1rem;
		background-color: #f9fafb;
		border-radius: 0.375rem;
	}

	.payment-summary p {
		margin: 0.5rem 0;
		color: #374151;
	}

	#payment-element {
		margin-bottom: 1.5rem;
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
</style>
