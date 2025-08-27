<script lang="ts">
	import { PagoService } from '$lib/services/pagoService.js';
	import type { DTOPago } from '$lib/generated/api';

	// State using Svelte 5 syntax
	let payments = $state<DTOPago[]>([]);
	let loading = $state(true);
	let error = $state('');

	// Load payments using $effect
	$effect(() => {
		loadPayments();
	});

	async function loadPayments() {
		try {
			// Get recent payments
			payments = await PagoService.getRecentPayments(10);
		} catch (err: unknown) {
			error = err instanceof Error ? err.message : 'An unknown error occurred';
		} finally {
			loading = false;
		}
	}

	function formatDate(dateString: string | Date | undefined): string {
		if (!dateString) return 'N/A';
		return new Date(dateString).toLocaleDateString();
	}

	function getStatusColor(status: string | undefined): string {
		switch (status) {
			case 'EXITO':
				return '#2e7d32';
			case 'PENDIENTE':
				return '#f57c00';
			case 'PROCESANDO':
				return '#1976d2';
			case 'ERROR':
				return '#d32f2f';
			case 'REEMBOLSADO':
				return '#7b1fa2';
			default:
				return '#666';
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
			{#each payments as payment (payment.id)}
				<div class="payment-item">
					<div class="payment-header">
						<span class="payment-id">#{payment.id}</span>
						<span class="payment-status" style="color: {getStatusColor(payment.estado)}">
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

<style>
	.payment-history {
		max-width: 800px;
		margin: 0 auto;
		padding: 1rem;
	}

	h2 {
		margin-bottom: 1.5rem;
		color: #1f2937;
		font-size: 1.5rem;
		font-weight: 600;
	}

	.error {
		color: #dc2626;
		font-size: 0.875rem;
	}

	.payments-list {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.payment-item {
		padding: 1rem;
		border: 1px solid #e5e7eb;
		border-radius: 0.5rem;
		background-color: white;
		box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
	}

	.payment-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 0.75rem;
		padding-bottom: 0.75rem;
		border-bottom: 1px solid #f3f4f6;
	}

	.payment-id {
		font-weight: 600;
		color: #374151;
	}

	.payment-status {
		font-weight: 500;
		font-size: 0.875rem;
		text-transform: uppercase;
	}

	.payment-details p {
		margin: 0.25rem 0;
		color: #6b7280;
		font-size: 0.875rem;
	}

	.payment-details strong {
		color: #374151;
	}
</style>
