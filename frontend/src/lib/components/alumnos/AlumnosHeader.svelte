<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	
	export let totalAlumnos: number;
	export let isLoading: boolean;
	
	const dispatch = createEventDispatcher();
	
	function handleRefresh() {
		dispatch('refresh');
	}
	
	function handleNuevo() {
		dispatch('nuevo');
	}
</script>

<div class="bg-white shadow-sm border-b border-gray-200">
	<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
		<div class="py-6">
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-2xl font-bold text-gray-900">Alumnos</h1>
					<p class="mt-1 text-sm text-gray-500">
						{isLoading ? 'Cargando...' : `${totalAlumnos} alumnos registrados`}
					</p>
				</div>
				<div class="flex space-x-3">
					<button
						on:click={handleRefresh}
						disabled={isLoading}
						class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
					>
						<svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
						</svg>
						Actualizar
					</button>
					<button
						on:click={handleNuevo}
						disabled={isLoading}
						class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
					>
						<svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
						</svg>
						Nuevo Alumno
					</button>
				</div>
			</div>
		</div>
	</div>
</div>
