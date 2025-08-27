<script lang="ts">
	import PaymentForm from '$lib/components/PaymentForm.svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { authStore } from '$lib/stores/authStore.svelte';

	// Get payment details from URL parameters
	const urlParams = $derived(new URLSearchParams($page.url.search));
	const classId = $derived(urlParams.get('classId'));
	const amountParam = $derived(urlParams.get('amount'));
	const descriptionParam = $derived(urlParams.get('description'));

	// Payment configuration
	let amount = $state(amountParam ? parseFloat(amountParam) : 99.99);
	let description = $state(descriptionParam ? decodeURIComponent(descriptionParam) : 'Advanced Mathematics Course');

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

	function handleSuccess() {
		// Redirect to payment success page with class information
		if (classId) {
			goto(`/payment-success?classId=${classId}&payment_success=true`);
		} else {
			goto('/payment-success?payment_success=true');
		}
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
		onSuccess={handleSuccess}
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
