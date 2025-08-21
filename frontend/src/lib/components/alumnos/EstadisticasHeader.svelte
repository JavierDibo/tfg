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

<div class="mb-6 flex items-center justify-between">
	<h1 class="text-3xl font-bold text-gray-900">Estadísticas de Alumnos</h1>
	<div class="space-x-2">
		<button
			onclick={handleRefresh}
			class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
		>
			Actualizar
		</button>
		<button onclick={handleBackToAlumnos} class="text-gray-600 hover:text-gray-800">
			← Volver a Alumnos
		</button>
	</div>
</div>
