<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	
	export let successMessage: string | null;
	export let error: string | null;
	
	const dispatch = createEventDispatcher();
	
	function clearSuccess() {
		dispatch('clearSuccess');
	}
	
	function clearError() {
		dispatch('clearError');
	}
</script>

<!-- Success/Error Messages - Fixed positioning -->
{#if successMessage}
	<div
		class="fixed top-4 left-1/2 z-50 w-auto max-w-md -translate-x-1/2 transform rounded-lg border border-green-400 bg-green-100 px-6 py-3 text-green-700 shadow-lg"
	>
		<div class="flex items-center justify-between">
			<span>{successMessage}</span>
			<button
				onclick={clearSuccess}
				class="ml-4 font-bold text-green-500 hover:text-green-700"
			>
				×
			</button>
		</div>
	</div>
{/if}

{#if error}
	<div
		class="fixed top-4 left-1/2 z-50 w-auto max-w-md -translate-x-1/2 transform rounded-lg border border-red-400 bg-red-100 px-6 py-3 text-red-700 shadow-lg"
	>
		<div class="flex items-center justify-between">
			<span>{error}</span>
			<button
				onclick={clearError}
				class="ml-4 font-bold text-red-500 hover:text-red-700"
			>
				×
			</button>
		</div>
	</div>
{/if}
