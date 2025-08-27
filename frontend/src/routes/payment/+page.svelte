<script lang="ts">
	import PaymentForm from '$lib/components/PaymentForm.svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';

	// Payment configuration
	let amount = $state(99.99);
	let description = $state('Advanced Mathematics Course');

	// Authentication check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		}
	});

	function handleError(error: Error) {
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
		studentId={authStore.user?.id?.toString() || ''}
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
		color: #1f2937;
		font-size: 2rem;
		font-weight: 600;
	}
</style>
