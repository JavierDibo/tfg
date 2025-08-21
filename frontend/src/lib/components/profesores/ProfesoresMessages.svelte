<script lang="ts">
	import { slide } from 'svelte/transition';

	let {
		successMessage,
		error,
		onClearSuccess,
		onClearError
	}: {
		successMessage: string | null;
		error: string | null;
		onClearSuccess: () => void;
		onClearError: () => void;
	} = $props();
</script>

{#if successMessage}
	<div
		transition:slide={{ duration: 300 }}
		class="mb-4 rounded-lg bg-green-100 p-4 text-sm text-green-700"
		role="alert"
	>
		<span class="font-medium">Éxito!</span>
		{successMessage}
		<button onclick={onClearSuccess} class="float-right font-bold">X</button>
	</div>
{/if}

{#if error}
	<div
		transition:slide={{ duration: 300 }}
		class="mb-4 rounded-lg bg-red-100 p-4 text-sm text-red-700"
		role="alert"
	>
		<span class="font-medium">Error!</span>
		{error}
		<button onclick={onClearError} class="float-right font-bold">X</button>
	</div>
{/if}
