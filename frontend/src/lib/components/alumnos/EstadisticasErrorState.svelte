<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher<{
		retry: void;
	}>();

	function handleRetry() {
		dispatch('retry');
	}
</script>

<div class="py-12 text-center">
	<p class="text-lg text-gray-500">No se pudieron cargar las estadísticas</p>
	<button
		onclick={handleRetry}
		class="mt-4 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
	>
		Intentar de Nuevo
	</button>
</div>
