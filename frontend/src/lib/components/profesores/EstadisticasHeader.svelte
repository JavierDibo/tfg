<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();
</script>

<div class="mb-6 flex items-center justify-between">
	<div>
		<h1 class="text-3xl font-bold text-gray-900">Estadísticas de Profesores</h1>
		<p class="text-gray-600">Visualización y análisis de los datos de profesores</p>
	</div>

	<button
		on:click={() => dispatch('refresh')}
		class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
	>
		Actualizar Datos
	</button>
</div>
