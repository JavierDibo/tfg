<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher<{
		retry: void;
	}>();

	function handleRetry() {
		dispatch('retry');
	}
</script>

<div class="text-center py-12">
	<p class="text-gray-500 text-lg">No se pudieron cargar las estadísticas</p>
	<button
		onclick={handleRetry}
		class="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
	>
		Intentar de Nuevo
	</button>
</div>
