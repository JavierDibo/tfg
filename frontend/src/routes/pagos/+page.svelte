<script lang="ts">
	import PaymentHistory from '$lib/components/PaymentHistory.svelte';
	import PaymentForm from '$lib/components/PaymentForm.svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State management
	let showPaymentForm = $state(false);
	let selectedAmount = $state(0);
	let selectedDescription = $state('');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	function handlePaymentError(error: Error) {
		console.error('Payment failed:', error);
		// Show error notification and close the form
		closePaymentForm();
	}

	function openPaymentForm(amount: number, description: string) {
		selectedAmount = amount;
		selectedDescription = description;
		showPaymentForm = true;
	}

	function closePaymentForm() {
		showPaymentForm = false;
		selectedAmount = 0;
		selectedDescription = '';
	}
</script>

<svelte:head>
	<title>Payment Management</title>
</svelte:head>

<div class="pagos-page">
	<div class="header">
		<h1>Payment Management</h1>
		<div class="header-actions">
			{#if authStore.user?.roles?.includes('ALUMNO')}
				<button class="view-history-btn" onclick={() => goto('/pagos/my-payments')}>
					View My Payments
				</button>
			{/if}
			<button class="new-payment-btn" onclick={() => openPaymentForm(50, 'Course Payment')}>
				New Payment
			</button>
		</div>
	</div>

	{#if showPaymentForm}
		<div
			class="modal-overlay"
			onclick={closePaymentForm}
			onkeydown={(e) => e.key === 'Escape' && closePaymentForm()}
			role="dialog"
			aria-modal="true"
			tabindex="-1"
		>
			<div
				class="modal-content"
				onclick={(e) => e.stopPropagation()}
				onkeydown={(e) => e.key === 'Escape' && closePaymentForm()}
				role="dialog"
				tabindex="-1"
			>
				<div class="modal-header">
					<h2>Create New Payment</h2>
					<button class="close-btn" onclick={closePaymentForm}>&times;</button>
				</div>
				<PaymentForm
					amount={selectedAmount}
					description={selectedDescription}
					studentId={authStore.user?.id?.toString() || ''}
					onError={handlePaymentError}
				/>
			</div>
		</div>
	{/if}

	<div class="content">
		<div class="quick-payments">
			<h2>Quick Payments</h2>
			<div class="payment-options">
				<button class="payment-option" onclick={() => openPaymentForm(25, 'Basic Course')}>
					<h3>Basic Course</h3>
					<p class="price">€25.00</p>
				</button>
				<button class="payment-option" onclick={() => openPaymentForm(50, 'Advanced Course')}>
					<h3>Advanced Course</h3>
					<p class="price">€50.00</p>
				</button>
				<button class="payment-option" onclick={() => openPaymentForm(100, 'Premium Course')}>
					<h3>Premium Course</h3>
					<p class="price">€100.00</p>
				</button>
			</div>
		</div>

		<div class="payment-history-section">
			<h2>Recent Payments</h2>
			<PaymentHistory />
		</div>
	</div>
</div>

<style>
	.pagos-page {
		max-width: 1200px;
		margin: 0 auto;
		padding: 2rem;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 2rem;
	}

	.header-actions {
		display: flex;
		gap: 1rem;
		align-items: center;
	}

	h1 {
		color: #1f2937;
		font-size: 2rem;
		font-weight: 600;
		margin: 0;
	}

	.new-payment-btn {
		padding: 0.75rem 1.5rem;
		background-color: #3b82f6;
		color: white;
		border: none;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.new-payment-btn:hover {
		background-color: #2563eb;
	}

	.view-history-btn {
		padding: 0.75rem 1.5rem;
		background-color: #6b7280;
		color: white;
		border: none;
		border-radius: 0.375rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.view-history-btn:hover {
		background-color: #4b5563;
	}

	.content {
		display: grid;
		gap: 2rem;
	}

	.quick-payments h2,
	.payment-history-section h2 {
		color: #1f2937;
		font-size: 1.5rem;
		font-weight: 600;
		margin-bottom: 1rem;
	}

	.payment-options {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}

	.payment-option {
		padding: 1.5rem;
		border: 1px solid #e5e7eb;
		border-radius: 0.5rem;
		background-color: white;
		cursor: pointer;
		transition: all 0.2s;
		text-align: center;
	}

	.payment-option:hover {
		border-color: #3b82f6;
		box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
	}

	.payment-option h3 {
		margin: 0 0 0.5rem 0;
		color: #1f2937;
		font-size: 1.125rem;
		font-weight: 600;
	}

	.price {
		margin: 0;
		color: #3b82f6;
		font-size: 1.25rem;
		font-weight: 700;
	}

	.modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1000;
	}

	.modal-content {
		background-color: white;
		border-radius: 0.5rem;
		padding: 2rem;
		max-width: 500px;
		width: 90%;
		max-height: 90vh;
		overflow-y: auto;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 1.5rem;
	}

	.modal-header h2 {
		margin: 0;
		color: #1f2937;
		font-size: 1.5rem;
		font-weight: 600;
	}

	.close-btn {
		background: none;
		border: none;
		font-size: 1.5rem;
		cursor: pointer;
		color: #6b7280;
		padding: 0;
		width: 2rem;
		height: 2rem;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 0.25rem;
	}

	.close-btn:hover {
		background-color: #f3f4f6;
		color: #374151;
	}

	@media (max-width: 768px) {
		.pagos-page {
			padding: 1rem;
		}

		.header {
			flex-direction: column;
			gap: 1rem;
			text-align: center;
		}

		.payment-options {
			grid-template-columns: 1fr;
		}
	}
</style>
