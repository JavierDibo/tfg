<script lang="ts">
	import { goto } from '$app/navigation';
	import type {
		DTOClase,
		DTOParametrosBusquedaClaseNivelEnum,
		DTOParametrosBusquedaClasePresencialidadEnum
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let clases = $state<DTOClase[]>([]);

	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);

	// Search and filters
	let searchTerm = $state('');
	let nivelFilter = $state<DTOParametrosBusquedaClaseNivelEnum | null>(null);
	let presencialidadFilter = $state<DTOParametrosBusquedaClasePresencialidadEnum | null>(null);
	let precioMinFilter = $state<number | null>(null);
	let precioMaxFilter = $state<number | null>(null);

	// Modal state
	let showDeleteModal = $state(false);
	let claseToDelete: DTOClase | null = $state(null);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load data when component mounts or filters change
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadClases();
		}
	});

	// Data loading
	async function loadClases() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage,
				size: pageSize,
				sortBy: 'titulo',
				sortDirection: 'ASC'
			};

			// Add filters
			if (searchTerm.trim()) {
				params.titulo = searchTerm.trim();
			}
			if (nivelFilter) {
				params.nivel = nivelFilter;
			}
			if (presencialidadFilter) {
				params.presencialidad = presencialidadFilter;
			}
			if (precioMinFilter !== null) {
				params.precioMin = precioMinFilter;
			}
			if (precioMaxFilter !== null) {
				params.precioMax = precioMaxFilter;
			}

			const response = await ClaseService.getPaginatedClases(params);

			clases = response.contenido || [];
			totalPages = response.totalPaginas || 0;
			currentPage = response.numeroPagina || 0;
		} catch (err) {
			error = `Error al cargar clases: ${err}`;
			console.error('Error loading clases:', err);
		} finally {
			loading = false;
		}
	}

	// Navigation functions
	function goToPage(page: number) {
		currentPage = page;
		loadClases();
	}

	// Search and filter functions
	function handleSearch() {
		currentPage = 0;
		loadClases();
	}

	function clearFilters() {
		searchTerm = '';
		nivelFilter = null;
		presencialidadFilter = null;
		precioMinFilter = null;
		precioMaxFilter = null;
		currentPage = 0;
		loadClases();
	}

	// Delete functions
	function confirmDelete(clase: DTOClase) {
		claseToDelete = clase;
		showDeleteModal = true;
	}

	async function deleteClase() {
		if (!claseToDelete?.id) return;

		try {
			await ClaseService.borrarClasePorId(claseToDelete.id);
			successMessage = 'Clase eliminada correctamente';
			showDeleteModal = false;
			claseToDelete = null;
			loadClases();
		} catch (err) {
			error = `Error al eliminar clase: ${err}`;
			console.error('Error deleting clase:', err);
		}
	}

	function cancelDelete() {
		showDeleteModal = false;
		claseToDelete = null;
	}

	// Utility functions
	function formatPrice(precio: number | undefined): string {
		if (precio === undefined || precio === null) return 'N/A';
		return `€${precio.toFixed(2)}`;
	}

	function getNivelColor(nivel: string | undefined): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'bg-green-100 text-green-800';
			case 'INTERMEDIO':
				return 'bg-yellow-100 text-yellow-800';
			case 'AVANZADO':
				return 'bg-red-100 text-red-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function getPresencialidadColor(presencialidad: string | undefined): string {
		switch (presencialidad) {
			case 'ONLINE':
				return 'bg-blue-100 text-blue-800';
			case 'PRESENCIAL':
				return 'bg-purple-100 text-purple-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}
</script>

<svelte:head>
	<title>Clases - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="mb-6 flex items-center justify-between">
			<h1 class="text-3xl font-bold text-gray-900">
				{#if authStore.isAdmin}
					Gestión de Clases
				{:else if authStore.isProfesor}
					Mis Clases
				{:else if authStore.isAlumno}
					Clases Disponibles
				{:else}
					Clases
				{/if}
			</h1>
			{#if authStore.isAdmin || authStore.isProfesor}
				<a
					href="/clases/nuevo"
					class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
				>
					Nueva Clase
				</a>
			{/if}
		</div>

		<!-- Search and Filters Section -->
		<div class="mb-6 rounded-lg bg-white p-6 shadow-md">
			<div class="mb-4 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
				<!-- Search -->
				<div>
					<label for="search" class="mb-1 block text-sm font-medium text-gray-700">Buscar</label>
					<input
						id="search"
						type="text"
						bind:value={searchTerm}
						placeholder="Buscar por título..."
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						onkeydown={(e) => e.key === 'Enter' && handleSearch()}
					/>
				</div>

				<!-- Nivel Filter -->
				<div>
					<label for="nivel" class="mb-1 block text-sm font-medium text-gray-700">Nivel</label>
					<select
						id="nivel"
						bind:value={nivelFilter}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
					>
						<option value={null}>Todos los niveles</option>
						<option value="PRINCIPIANTE">Principiante</option>
						<option value="INTERMEDIO">Intermedio</option>
						<option value="AVANZADO">Avanzado</option>
					</select>
				</div>

				<!-- Presencialidad Filter -->
				<div>
					<label for="presencialidad" class="mb-1 block text-sm font-medium text-gray-700"
						>Presencialidad</label
					>
					<select
						id="presencialidad"
						bind:value={presencialidadFilter}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
					>
						<option value={null}>Todas</option>
						<option value="ONLINE">Online</option>
						<option value="PRESENCIAL">Presencial</option>
					</select>
				</div>

				<!-- Price Range -->
				<div>
					<fieldset>
						<legend class="mb-1 block text-sm font-medium text-gray-700">Precio</legend>
						<div class="flex gap-2">
							<input
								id="precioMin"
								type="number"
								bind:value={precioMinFilter}
								placeholder="Min"
								class="w-1/2 rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							/>
							<input
								id="precioMax"
								type="number"
								bind:value={precioMaxFilter}
								placeholder="Max"
								class="w-1/2 rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							/>
						</div>
					</fieldset>
				</div>
			</div>

			<div class="flex gap-2">
				<button
					onclick={handleSearch}
					class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
				>
					Buscar
				</button>
				<button
					onclick={clearFilters}
					class="rounded-md bg-gray-500 px-4 py-2 text-white hover:bg-gray-600"
				>
					Limpiar
				</button>
			</div>
		</div>

		<!-- Messages -->
		{#if error}
			<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				{error}
			</div>
		{/if}

		{#if successMessage}
			<div class="mb-4 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		<!-- Loading State -->
		{#if loading}
			<div class="flex items-center justify-center py-8">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else}
			<!-- Classes Grid -->
			<div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
				{#each clases as clase (clase.id)}
					<div
						class="overflow-hidden rounded-lg bg-white shadow-md transition-shadow hover:shadow-lg"
					>
						{#if clase.imagenPortada}
							<img src={clase.imagenPortada} alt={clase.titulo} class="h-48 w-full object-cover" />
						{:else}
							<div class="flex h-48 w-full items-center justify-center bg-gray-200">
								<span class="text-gray-500">Sin imagen</span>
							</div>
						{/if}

						<div class="p-6">
							<div class="mb-2 flex items-start justify-between">
								<h3 class="line-clamp-2 text-xl font-semibold text-gray-900">
									{clase.titulo || 'Sin título'}
								</h3>
								<span class="text-lg font-bold text-blue-600">
									{formatPrice(clase.precio)}
								</span>
							</div>

							<p class="mb-4 line-clamp-3 text-gray-600">
								{clase.descripcion || 'Sin descripción'}
							</p>

							<div class="mb-4 flex flex-wrap gap-2">
								<span
									class="rounded-full px-2 py-1 text-xs font-medium {getNivelColor(clase.nivel)}"
								>
									{clase.nivel || 'N/A'}
								</span>
								<span
									class="rounded-full px-2 py-1 text-xs font-medium {getPresencialidadColor(
										clase.presencialidad
									)}"
								>
									{clase.presencialidad || 'N/A'}
								</span>
								{#if clase.tipoClase}
									<span
										class="rounded-full bg-orange-100 px-2 py-1 text-xs font-medium text-orange-800"
									>
										{clase.tipoClase}
									</span>
								{/if}
							</div>

							<div class="mb-4 flex items-center justify-between text-sm text-gray-500">
								<span>👥 {clase.numeroAlumnos || 0} alumnos</span>
								<span>👨‍🏫 {clase.numeroProfesores || 0} profesores</span>
								<span>📚 {clase.numeroMateriales || 0} materiales</span>
							</div>

							<div class="flex gap-2">
								<a
									href="/clases/{clase.id}"
									class="flex-1 rounded-md bg-blue-600 px-4 py-2 text-center text-white hover:bg-blue-700"
								>
									Ver detalles
								</a>
								{#if (authStore.isAdmin || authStore.isProfesor) && clase.id}
									<button
										onclick={() => confirmDelete(clase)}
										class="rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700"
									>
										Eliminar
									</button>
								{/if}
							</div>
						</div>
					</div>
				{/each}
			</div>

			<!-- Empty State -->
			{#if clases.length === 0 && !loading}
				<div class="py-12 text-center">
					<div class="mb-4 text-6xl text-gray-400">📚</div>
					<h3 class="mb-2 text-lg font-medium text-gray-900">No se encontraron clases</h3>
					<p class="text-gray-500">Intenta ajustar los filtros de búsqueda</p>
				</div>
			{/if}

			<!-- Pagination -->
			{#if totalPages > 1}
				<div class="mt-8 flex justify-center">
					<nav class="flex items-center space-x-2">
						<button
							onclick={() => goToPage(currentPage - 1)}
							disabled={currentPage === 0}
							class="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
						>
							Anterior
						</button>

						{#each Array.from({ length: totalPages }, (_, i) => i) as page (page)}
							<button
								onclick={() => goToPage(page)}
								class="rounded-md px-3 py-2 text-sm font-medium {currentPage === page
									? 'z-10 bg-blue-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-blue-600'
									: 'border border-gray-300 bg-white text-gray-500 hover:bg-gray-50'}"
							>
								{page + 1}
							</button>
						{/each}

						<button
							onclick={() => goToPage(currentPage + 1)}
							disabled={currentPage === totalPages - 1}
							class="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
						>
							Siguiente
						</button>
					</nav>
				</div>
			{/if}
		{/if}
	</div>
</div>

<!-- Delete Confirmation Modal -->
{#if showDeleteModal}
	<div class="bg-opacity-50 fixed inset-0 z-50 h-full w-full overflow-y-auto bg-gray-600">
		<div class="relative top-20 mx-auto w-96 rounded-md border bg-white p-5 shadow-lg">
			<div class="mt-3 text-center">
				<h3 class="mb-4 text-lg font-medium text-gray-900">Confirmar eliminación</h3>
				<p class="mb-6 text-sm text-gray-500">
					¿Estás seguro de que quieres eliminar la clase "{claseToDelete?.titulo}"? Esta acción no
					se puede deshacer.
				</p>
				<div class="flex justify-center space-x-4">
					<button
						onclick={cancelDelete}
						class="rounded-md bg-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-400"
					>
						Cancelar
					</button>
					<button
						onclick={deleteClase}
						class="rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700"
					>
						Eliminar
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}

<style>
	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.line-clamp-3 {
		display: -webkit-box;
		-webkit-line-clamp: 3;
		line-clamp: 3;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
</style>
