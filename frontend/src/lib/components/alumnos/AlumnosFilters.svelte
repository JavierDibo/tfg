<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOEntidad } from '$lib/generated/models';
	
	export let searchTerm: string;
	export let selectedEntidad: string;
	export let entidades: DTOEntidad[];
	export let isLoading: boolean;
	
	const dispatch = createEventDispatcher();
	
	function handleSearch(event: Event) {
		const target = event.target as HTMLInputElement;
		dispatch('search', target.value);
	}
	
	function handleEntidadChange(event: Event) {
		const target = event.target as HTMLSelectElement;
		dispatch('entidadChange', target.value);
	}
	
	function handleClearFilters() {
		dispatch('clearFilters');
	}
</script>

<div class="bg-white shadow-sm border-b border-gray-200">
	<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
		<div class="flex flex-col sm:flex-row gap-4 items-center justify-between">
			<div class="flex-1 max-w-md">
				<label for="search" class="sr-only">Buscar alumnos</label>
				<div class="relative">
					<div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
						<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
						</svg>
					</div>
					<input
						id="search"
						type="text"
						bind:value={searchTerm}
						on:input={handleSearch}
						placeholder="Buscar por nombre, email o teléfono..."
						class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
					/>
				</div>
			</div>
			
			<div class="flex items-center space-x-4">
				<div>
					<label for="entidad" class="block text-sm font-medium text-gray-700">Entidad</label>
					<select
						id="entidad"
						bind:value={selectedEntidad}
						on:change={handleEntidadChange}
						disabled={isLoading}
						class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md disabled:opacity-50"
					>
						<option value="">Todas las entidades</option>
						{#each entidades as entidad}
							<option value={entidad.id}>{entidad.nombre}</option>
						{/each}
					</select>
				</div>
				
				<button
					on:click={handleClearFilters}
					disabled={isLoading}
					class="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
				>
					Limpiar
				</button>
			</div>
		</div>
	</div>
</div>
