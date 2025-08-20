<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { goto } from '$app/navigation';

	const dispatch = createEventDispatcher<{
		refresh: void;
	}>();

	function handleRefresh() {
		dispatch('refresh');
	}

	function handleBackToAlumnos() {
		goto('/alumnos');
	}
</script>

<div class="flex justify-between items-center mb-6">
	<h1 class="text-3xl font-bold text-gray-900">Estadísticas de Alumnos</h1>
	<div class="space-x-2">
		<button
			onclick={handleRefresh}
			class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
		>
			Actualizar
		</button>
		<button
			onclick={handleBackToAlumnos}
			class="text-gray-600 hover:text-gray-800"
		>
			← Volver a Alumnos
		</button>
	</div>
</div>
