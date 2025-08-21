<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { fade } from 'svelte/transition';

	export let successMessage: string | null = null;
	export let error: string | null = null;

	const dispatch = createEventDispatcher();
</script>

{#if successMessage}
	<div
		class="mb-4 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700"
		transition:fade
	>
		{successMessage}
	</div>
{/if}

{#if error}
	<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700" transition:fade>
		{error}
		<button
			on:click={() => dispatch('clearError')}
			class="float-right text-red-500 hover:text-red-700"
		>
			×
		</button>
	</div>
{/if}
