<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const {
		currentFilters,
		paginatedData,
		loading = false
	} = $props<{
		currentFilters: any;
		paginatedData: any;
		loading?: boolean;
	}>();

	const dispatch = createEventDispatcher();

	// Local state for input values to prevent focus loss
	let localBusquedaGeneral = $state(currentFilters.busquedaGeneral);
	let localNombre = $state(currentFilters.nombre);
	let localApellidos = $state(currentFilters.apellidos);
	let localDni = $state(currentFilters.dni);
	let localEmail = $state(currentFilters.email);

	// Debounced search
	let searchTimeout: ReturnType<typeof setTimeout> | null = null;

	function debouncedSearch(value: string, filterKey: string) {
		if (searchTimeout) {
			clearTimeout(searchTimeout);
		}

		searchTimeout = setTimeout(() => {
			console.log('🔍 SEARCH:', value);
			updateFilters({ [filterKey]: value });
		}, 500); // 500ms delay
	}

	// Update local state when props change
	$effect(() => {
		localBusquedaGeneral = currentFilters.busquedaGeneral;
		localNombre = currentFilters.nombre;
		localApellidos = currentFilters.apellidos;
		localDni = currentFilters.dni;
		localEmail = currentFilters.email;
	});

	function switchSearchMode(mode: 'simple' | 'advanced') {
		dispatch('switchSearchMode', mode);
	}

	function updateFilters(filters: any) {
		dispatch('updateFilters', filters);
	}

	function clearFilters() {
		dispatch('clearFilters');
	}

	function exportResults() {
		dispatch('exportResults');
	}
</script>

<div class="mb-6 rounded-lg bg-white p-6 shadow-md">
	<!-- Search Mode Toggle -->
	<div class="mb-6 flex items-center justify-between">
		<h2 class="text-lg font-semibold">Búsqueda de Alumnos</h2>
		<div class="flex space-x-2">
			<button
				onclick={() => switchSearchMode('simple')}
				class="rounded-md px-3 py-1 text-sm transition-colors {currentFilters.searchMode ===
				'simple'
					? 'bg-blue-600 text-white'
					: 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
			>
				🔍 Búsqueda Simple
			</button>
			<button
				onclick={() => switchSearchMode('advanced')}
				class="rounded-md px-3 py-1 text-sm transition-colors {currentFilters.searchMode ===
				'advanced'
					? 'bg-blue-600 text-white'
					: 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
			>
				⚙️ Búsqueda Avanzada
			</button>
		</div>
	</div>

	{#if currentFilters.searchMode === 'simple'}
		<!-- Simple Search Mode -->
		<div class="space-y-4">
			<div>
				<label for="busquedaGeneral" class="mb-2 block text-sm font-medium text-gray-700">
					Búsqueda General
				</label>
				<div class="relative">
					<input
						id="busquedaGeneral"
						type="text"
						bind:value={localBusquedaGeneral}
						oninput={() => {
							debouncedSearch(localBusquedaGeneral, 'busquedaGeneral');
						}}
						onkeydown={(e) => {
							if (e.key === 'Enter') {
								if (searchTimeout) clearTimeout(searchTimeout);
								console.log('🔍 SEARCH (Enter):', localBusquedaGeneral);
								updateFilters({ busquedaGeneral: localBusquedaGeneral });
							}
						}}
						onblur={() => {
							if (searchTimeout) clearTimeout(searchTimeout);
							updateFilters({ busquedaGeneral: localBusquedaGeneral });
						}}
						class="w-full rounded-lg border border-gray-300 py-3 pr-4 pl-10 focus:border-transparent focus:ring-2 focus:ring-blue-500 focus:outline-none"
						placeholder="Buscar por nombre, apellidos, usuario, DNI, email... Ej: 'jose garcia' o 'maria lopez'"
					/>
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
							></path>
						</svg>
					</div>
				</div>
			</div>

			<!-- Quick State Filter -->
			<div>
				<label for="matriculadoSimple" class="mb-1 block text-sm font-medium text-gray-700"
					>Estado de Matrícula</label
				>
				<select
					id="matriculadoSimple"
					value={currentFilters.matriculado?.toString() || ''}
					onchange={(e) => {
						const value = (e.target as HTMLSelectElement).value;
						updateFilters({
							matriculado: value === '' ? undefined : value === 'true'
						});
					}}
					class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
				>
					<option value="">Todos</option>
					<option value="true">✅ Matriculados</option>
					<option value="false">❌ No Matriculados</option>
				</select>
			</div>
		</div>
	{:else}
		<!-- Advanced Search Mode -->
		<div class="space-y-6">
			<div>
				<h3 class="text-md mb-3 font-medium text-gray-800">📝 Campos de Texto</h3>
				<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
					<div>
						<label for="nombre" class="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
						<input
							id="nombre"
							type="text"
							bind:value={localNombre}
							onblur={() => updateFilters({ nombre: localNombre })}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									updateFilters({ nombre: localNombre });
								}
							}}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: Juan"
						/>
					</div>
					<div>
						<label for="apellidos" class="mb-1 block text-sm font-medium text-gray-700"
							>Apellidos</label
						>
						<input
							id="apellidos"
							type="text"
							bind:value={localApellidos}
							onblur={() => updateFilters({ apellidos: localApellidos })}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									updateFilters({ apellidos: localApellidos });
								}
							}}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: García López"
						/>
					</div>
					<div>
						<label for="dni" class="mb-1 block text-sm font-medium text-gray-700">DNI</label>
						<input
							id="dni"
							type="text"
							bind:value={localDni}
							onblur={() => updateFilters({ dni: localDni })}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									updateFilters({ dni: localDni });
								}
							}}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: 12345678Z"
						/>
					</div>
					<div>
						<label for="email" class="mb-1 block text-sm font-medium text-gray-700">Email</label>
						<input
							id="email"
							type="email"
							bind:value={localEmail}
							onblur={() => updateFilters({ email: localEmail })}
							onkeydown={(e) => {
								if (e.key === 'Enter') {
									updateFilters({ email: localEmail });
								}
							}}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Ej: juan@universidad.es"
						/>
					</div>
				</div>
			</div>

			<div>
				<h3 class="text-md mb-3 font-medium text-gray-800">⚙️ Estados</h3>
				<div>
					<label for="matriculadoAdvanced" class="mb-1 block text-sm font-medium text-gray-700"
						>Estado de Matrícula</label
					>
					<select
						id="matriculadoAdvanced"
						value={currentFilters.matriculado?.toString() || ''}
						onchange={(e) => {
							const value = (e.target as HTMLSelectElement).value;
							updateFilters({
								matriculado: value === '' ? undefined : value === 'true'
							});
						}}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
					>
						<option value="">Todos</option>
						<option value="true">✅ Matriculados</option>
						<option value="false">❌ No Matriculados</option>
					</select>
				</div>
			</div>
		</div>
	{/if}

	<!-- Action Buttons -->
	<div class="mt-6 flex flex-wrap gap-3 border-t pt-4">
		<button
			onclick={clearFilters}
			class="rounded-md bg-gray-500 px-4 py-2 text-white transition-colors hover:bg-gray-600 focus:ring-2 focus:ring-gray-500 focus:outline-none"
		>
			🗑️ Limpiar Filtros
		</button>
		{#if paginatedData?.content && paginatedData.content.length > 0}
			<button
				onclick={exportResults}
				disabled={loading}
				class="rounded-md px-4 py-2 text-white transition-colors focus:ring-2 focus:outline-none {loading
					? 'cursor-not-allowed bg-gray-400'
					: 'bg-green-500 hover:bg-green-600 focus:ring-green-500'}"
			>
				{#if loading}
					<span class="inline-flex items-center">
						<svg
							class="mr-2 -ml-1 h-4 w-4 animate-spin text-white"
							xmlns="http://www.w3.org/2000/svg"
							fill="none"
							viewBox="0 0 24 24"
						>
							<circle
								class="opacity-25"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							></circle>
							<path
								class="opacity-75"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							></path>
						</svg>
						Exportando...
					</span>
				{:else}
					📥 Exportar Todos los Filtrados
				{/if}
			</button>
		{/if}
	</div>
</div>
