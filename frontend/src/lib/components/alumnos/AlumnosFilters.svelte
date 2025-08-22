<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOEntidad } from '$lib/generated/api';

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

<div class="border-b border-gray-200 bg-white shadow-sm">
	<div class="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
		<div class="flex flex-col items-center justify-between gap-4 sm:flex-row">
			<div class="max-w-md flex-1">
				<label for="search" class="sr-only">Buscar alumnos</label>
				<div class="relative">
					<div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
						<svg
							class="h-5 w-5 text-gray-400"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
							/>
						</svg>
					</div>
					<input
						id="search"
						type="text"
						bind:value={searchTerm}
						on:input={handleSearch}
						placeholder="Buscar por nombre, email o teléfono..."
						class="block w-full rounded-md border border-gray-300 bg-white py-2 pr-3 pl-10 leading-5 placeholder-gray-500 focus:border-indigo-500 focus:placeholder-gray-400 focus:ring-1 focus:ring-indigo-500 focus:outline-none sm:text-sm"
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
						class="mt-1 block w-full rounded-md border-gray-300 py-2 pr-10 pl-3 text-base focus:border-indigo-500 focus:ring-indigo-500 focus:outline-none disabled:opacity-50 sm:text-sm"
					>
						<option value="">Todas las entidades</option>
						{#each entidades as entidad (entidad.id)}
							<option value={entidad.id}>{entidad.info}</option>
						{/each}
					</select>
				</div>

				<button
					on:click={handleClearFilters}
					disabled={isLoading}
					class="inline-flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:outline-none disabled:opacity-50"
				>
					Limpiar
				</button>
			</div>
		</div>
	</div>
</div>
