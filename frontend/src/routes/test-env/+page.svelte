<script lang="ts">
	import { getStripe } from '$lib/stripe.js';

	// State
	let stripeLoaded = $state(false);
	let stripeError = $state('');

	// Test environment variable loading
	const envCheck = {
		stripeKey: import.meta.env.PUBLIC_STRIPE_PUBLISHABLE_KEY,
		hasKey: !!import.meta.env.PUBLIC_STRIPE_PUBLISHABLE_KEY,
		allEnvVars: Object.keys(import.meta.env).filter((key) => key.startsWith('PUBLIC_'))
	};

	// Load Stripe on component mount
	$effect(() => {
		loadStripe();
	});

	async function loadStripe() {
		try {
			const stripe = await getStripe();
			stripeLoaded = !!stripe;
		} catch (err) {
			console.error('Failed to load Stripe:', err);
			stripeError = err instanceof Error ? err.message : 'Unknown error';
		}
	}
</script>

<svelte:head>
	<title>Environment Test</title>
</svelte:head>

<div class="test-page">
	<h1>Environment Variable Test</h1>

	<div class="test-results">
		<h2>Stripe Configuration Test</h2>

		<div class="test-item">
			<strong>Stripe Key Present:</strong>
			<span class={envCheck.hasKey ? 'success' : 'error'}>
				{envCheck.hasKey ? '✅ Yes' : '❌ No'}
			</span>
		</div>

		<div class="test-item">
			<strong>Stripe Key Length:</strong>
			<span>{envCheck.stripeKey?.length || 0} characters</span>
		</div>

		<div class="test-item">
			<strong>Stripe Key Prefix:</strong>
			<span>{envCheck.stripeKey?.substring(0, 20) || 'N/A'}...</span>
		</div>

		<div class="test-item">
			<strong>Stripe Object Loaded:</strong>
			<span class={stripeLoaded ? 'success' : 'error'}>
				{stripeLoaded ? '✅ Yes' : '❌ No'}
			</span>
		</div>

		{#if stripeError}
			<div class="test-item">
				<strong>Stripe Error:</strong>
				<span class="error">{stripeError}</span>
			</div>
		{/if}

		<div class="test-item">
			<strong>All PUBLIC_ Environment Variables:</strong>
			<ul>
				{#each envCheck.allEnvVars as envVar (envVar)}
					<li>{envVar}: {import.meta.env[envVar] ? '✅ Set' : '❌ Not Set'}</li>
				{/each}
			</ul>
		</div>
	</div>
</div>

<style>
	.test-page {
		max-width: 800px;
		margin: 0 auto;
		padding: 2rem;
	}

	.test-results {
		margin-top: 2rem;
		padding: 1rem;
		border: 1px solid #e5e7eb;
		border-radius: 0.5rem;
		background-color: #f9fafb;
	}

	.test-item {
		margin: 1rem 0;
		padding: 0.5rem;
		border-bottom: 1px solid #e5e7eb;
	}

	.test-item:last-child {
		border-bottom: none;
	}

	.success {
		color: #059669;
		font-weight: 600;
	}

	.error {
		color: #dc2626;
		font-weight: 600;
	}

	ul {
		margin: 0.5rem 0;
		padding-left: 1.5rem;
	}

	li {
		margin: 0.25rem 0;
	}
</style>
