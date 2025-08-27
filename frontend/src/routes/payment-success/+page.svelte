<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { PagoService } from '$lib/services/pagoService.js';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOPago } from '$lib/generated/api';

	// State management
	let paymentStatus = $state('checking');
	let payment = $state<DTOPago | null>(null);
	let error = $state('');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	// Payment processing with retry logic
	$effect(() => {
		const paymentId = $page.url.searchParams.get('payment_id');

		if (!paymentId) {
			paymentStatus = 'error';
			error = 'No payment ID found';
			return;
		}

		// Use an IIFE for async operations with retry logic
		(async () => {
			const maxRetries = 10; // Increased from 5 to 10
			let retryCount = 0;

			const checkPayment = async (): Promise<boolean> => {
				try {
					// Check payment status
					const status = await PagoService.checkPaymentStatus(parseInt(paymentId));
					console.log(`Payment status check:`, status);

					if (status.isSuccessful) {
						paymentStatus = 'success';
						payment = await PagoService.getPayment(parseInt(paymentId));
						return true;
					} else if (status.status === 'PENDIENTE' || status.status === 'PROCESANDO') {
						// Payment is still processing, retry
						console.log(`Payment still pending/processing: ${status.status}`);
						return false;
					} else {
						paymentStatus = 'failed';
						console.log(`Payment failed with status: ${status.status}`);
						return true;
					}
				} catch (err: unknown) {
					console.warn(`Payment status check attempt ${retryCount + 1} failed:`, err);
					return false;
				}
			};

			// Initial check
			let resolved = await checkPayment();

			// Retry logic for pending payments
			while (!resolved && retryCount < maxRetries) {
				retryCount++;
				console.log(`Retrying payment status check (attempt ${retryCount}/${maxRetries})`);

				// Wait 3 seconds before retry (increased from 2 seconds)
				await new Promise((resolve) => setTimeout(resolve, 3000));
				resolved = await checkPayment();
			}

			// If still not resolved after retries, show error
			if (!resolved) {
				paymentStatus = 'error';
				error = 'Unable to verify payment status. Please contact support.';
			}
		})();
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
					<p><strong>Date:</strong> {new Date(payment.fechaPago || '').toLocaleDateString()}</p>
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

<style>
	.payment-result {
		max-width: 600px;
		margin: 50px auto;
		padding: 2rem;
		text-align: center;
	}

	.loading,
	.success,
	.failed,
	.error {
		padding: 2rem;
		border-radius: 0.5rem;
		box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
	}

	.loading {
		background-color: #f0f9ff;
		border: 1px solid #bae6fd;
	}

	.success {
		background-color: #f0fdf4;
		border: 1px solid #bbf7d0;
	}

	.failed {
		background-color: #fef2f2;
		border: 1px solid #fecaca;
	}

	.error {
		background-color: #fef2f2;
		border: 1px solid #fecaca;
	}

	h2 {
		margin-bottom: 1rem;
		font-size: 1.5rem;
		font-weight: 600;
	}

	.loading h2 {
		color: #0369a1;
	}

	.success h2 {
		color: #059669;
	}

	.failed h2,
	.error h2 {
		color: #dc2626;
	}

	.payment-details {
		margin: 1.5rem 0;
		padding: 1rem;
		background-color: white;
		border-radius: 0.375rem;
		text-align: left;
	}

	.payment-details p {
		margin: 0.5rem 0;
		color: #374151;
	}

	.button {
		display: inline-block;
		margin-top: 1rem;
		padding: 0.75rem 1.5rem;
		background-color: #3b82f6;
		color: white;
		text-decoration: none;
		border-radius: 0.375rem;
		font-weight: 500;
		transition: background-color 0.2s;
	}

	.button:hover {
		background-color: #2563eb;
	}
</style>
