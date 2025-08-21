<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PageData } from '../../../routes/profesores/$types';

	const dispatch = createEventDispatcher();

	let {
		currentFilters,
		itemCount,
		loading
	}: {
		currentFilters: PageData['filters'];
		itemCount: number;
		loading: boolean;
	} = $props();

	let internalFilters = $state({ ...currentFilters });

	function handleFilterChange() {
		dispatch('updateFilters', internalFilters);
	}

	function handleClearFilters() {
		internalFilters = {
			searchMode: 'simple',
			busquedaGeneral: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			habilitado: undefined
		};
		dispatch('clearFilters');
	}

	function switchMode(mode: 'simple' | 'advanced') {
		dispatch('switchSearchMode', mode);
	}
</script>

<div class="mb-6 rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
	<div class="flex items-center justify-between">
		<div class="flex items-center gap-4">
			<button
				class={`rounded-md px-3 py-1 text-sm ${currentFilters.searchMode === 'simple' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}
				onclick={() => switchMode('simple')}>Búsqueda Simple</button
			>
			<button
				class={`rounded-md px-3 py-1 text-sm ${currentFilters.searchMode === 'advanced' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}
				onclick={() => switchMode('advanced')}>Búsqueda Avanzada</button
			>
		</div>
		<div class="text-sm text-gray-600">
			{#if !loading}
				Mostrando <span class="font-semibold">{itemCount}</span> resultados
			{/if}
		</div>
	</div>

	<form
		onsubmit={(e) => {
			e.preventDefault();
			handleFilterChange();
		}}
		class="mt-4"
	>
		{#if currentFilters.searchMode === 'simple'}
			<div class="flex items-center gap-2">
				<input
					id="search"
					type="text"
					bind:value={internalFilters.busquedaGeneral}
					placeholder="Buscar por nombre, apellidos, DNI, email..."
					class="w-full rounded-md border-gray-300 shadow-sm"
				/>
				<button type="submit" class="rounded-md bg-blue-600 px-4 py-2 text-white">Buscar</button>
			</div>
		{:else}
			<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
				<input
					type="text"
					bind:value={internalFilters.nombre}
					placeholder="Nombre"
					class="rounded-md border-gray-300 shadow-sm"
				/>
				<input
					type="text"
					bind:value={internalFilters.apellidos}
					placeholder="Apellidos"
					class="rounded-md border-gray-300 shadow-sm"
				/>
				<input
					type="text"
					bind:value={internalFilters.dni}
					placeholder="DNI"
					class="rounded-md border-gray-300 shadow-sm"
				/>
				<input
					type="email"
					bind:value={internalFilters.email}
					placeholder="Email"
					class="rounded-md border-gray-300 shadow-sm"
				/>
			</div>
			<div class="mt-4 flex items-center justify-end gap-2">
				<button type="submit" class="rounded-md bg-blue-600 px-4 py-2 text-white">Filtrar</button>
				<button
					type="button"
					onclick={handleClearFilters}
					class="rounded-md bg-gray-500 px-4 py-2 text-white">Limpiar</button
				>
			</div>
		{/if}
		<div class="mt-4 flex items-center">
			<label for="status-select" class="mr-4">Estado de la cuenta:</label>
			<select
				id="status-select"
				bind:value={internalFilters.habilitado}
				onchange={handleFilterChange}
				class="rounded-md border-gray-300 shadow-sm"
			>
				<option value={undefined}>Todos</option>
				<option value={true}>Habilitado</option>
				<option value={false}>Deshabilitado</option>
			</select>
		</div>
	</form>
</div>
