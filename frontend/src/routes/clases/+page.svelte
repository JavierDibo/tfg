<script lang="ts">
	import { goto } from '$app/navigation';
	import type {
		DTOClase,
		DTOParametrosBusquedaClaseNivelEnum,
		DTOParametrosBusquedaClasePresencialidadEnum
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import {
		EntitySearchSection,
		EntityDataTable,
		EntityPaginationControls,
		EntityMessages
	} from '$lib/components/common';
	import type {
		EntityFilters,
		PaginatedEntities,
		EntityColumn,
		EntityAction
	} from '$lib/components/common/types';
	import { getSearchConfig } from '$lib/components/common/searchConfigs';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let clases = $state<DTOClase[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);

	// Search configuration
	const searchConfig = getSearchConfig('clases');

	// Search and filters
	let currentFilters = $state<EntityFilters>({
		searchMode: 'simple',
		q: '',
		titulo: '',
		descripcion: '',
		nivel: '',
		presencialidad: '',
		precioMinimo: '',
		precioMaximo: ''
	});

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

	// Data loading with new search parameters
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

			// Add search parameters
			if (currentFilters.q && typeof currentFilters.q === 'string' && currentFilters.q.trim()) {
				params.q = currentFilters.q.trim();
			}
			if (
				currentFilters.titulo &&
				typeof currentFilters.titulo === 'string' &&
				currentFilters.titulo.trim()
			) {
				params.titulo = currentFilters.titulo.trim();
			}
			if (
				currentFilters.descripcion &&
				typeof currentFilters.descripcion === 'string' &&
				currentFilters.descripcion.trim()
			) {
				params.descripcion = currentFilters.descripcion.trim();
			}
			if (
				currentFilters.nivel &&
				typeof currentFilters.nivel === 'string' &&
				currentFilters.nivel.trim()
			) {
				params.nivel = currentFilters.nivel.trim() as DTOParametrosBusquedaClaseNivelEnum;
			}
			if (
				currentFilters.presencialidad &&
				typeof currentFilters.presencialidad === 'string' &&
				currentFilters.presencialidad.trim()
			) {
				params.presencialidad =
					currentFilters.presencialidad.trim() as DTOParametrosBusquedaClasePresencialidadEnum;
			}
			if (
				currentFilters.precioMinimo &&
				typeof currentFilters.precioMinimo === 'string' &&
				currentFilters.precioMinimo.trim()
			) {
				params.precioMin = parseFloat(currentFilters.precioMinimo);
			}
			if (
				currentFilters.precioMaximo &&
				typeof currentFilters.precioMaximo === 'string' &&
				currentFilters.precioMaximo.trim()
			) {
				params.precioMax = parseFloat(currentFilters.precioMaximo);
			}

			const response = await ClaseService.getPaginatedClases(params);

			clases = response.contenido || [];
			totalElements = response.totalElementos || 0;
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

	function changePageSize(newSize: number) {
		pageSize = newSize;
		currentPage = 0;
		loadClases();
	}

	// Search and filter functions
	function updateFilters(filters: Record<string, unknown>) {
		Object.assign(currentFilters, filters);
		currentPage = 0;
		loadClases();
	}

	function clearFilters() {
		currentFilters = {
			searchMode: 'simple',
			q: '',
			titulo: '',
			descripcion: '',
			nivel: '',
			presencialidad: '',
			precioMinimo: '',
			precioMaximo: ''
		};
		currentPage = 0;
		loadClases();
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		currentFilters.searchMode = mode;
	}

	// Delete functions
	function openDeleteModal(clase: DTOClase) {
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

	// Table configuration
	const tableColumns = [
		{ key: 'titulo', header: 'Título', sortable: true },
		{ key: 'descripcion', header: 'Descripción', sortable: true },
		{ key: 'nivel', header: 'Nivel', sortable: true },
		{ key: 'presencialidad', header: 'Presencialidad', sortable: true },
		{ key: 'precio', header: 'Precio', sortable: true }
	];

	const tableActions = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue-700',
			action: (clase: DTOClase) => goto(`/clases/${clase.id}`),
			condition: () => true
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red-700',
			action: (clase: DTOClase) => openDeleteModal(clase),
			condition: () => authStore.isAdmin
		}
	];

	// Pagination configuration
	const pageDisplayInfo = $derived({
		currentPage,
		totalPages,
		totalElements,
		pageSize
	});

	const currentPagination = $derived({
		page: currentPage,
		size: pageSize,
		sortBy: 'titulo',
		sortDirection: 'ASC' as const
	});

	const sortFields = [
		{ value: 'titulo', label: 'Título' },
		{ value: 'descripcion', label: 'Descripción' },
		{ value: 'nivel', label: 'Nivel' },
		{ value: 'presencialidad', label: 'Presencialidad' },
		{ value: 'precio', label: 'Precio' }
	];

	const pageSizeOptions = [10, 20, 50, 100];

	function changeSorting(field: string) {
		// Implement sorting logic
		console.log('Sorting by:', field);
	}

	function exportResults() {
		// Implement export functionality
		console.log('Export functionality to be implemented');
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
			<EntitySearchSection
				{currentFilters}
				paginatedData={{
					content: clases as unknown as Record<string, unknown>[],
					totalElements,
					totalPages,
					currentPage
				} as PaginatedEntities<Record<string, unknown>>}
				{loading}
				entityNamePlural={searchConfig.entityNamePlural}
				advancedFields={searchConfig.advancedFields}
				statusField={searchConfig.statusField}
				entityType={searchConfig.entityType}
				on:updateFilters={(e) => updateFilters(e.detail)}
				on:clearFilters={clearFilters}
				on:switchSearchMode={(e) => switchSearchMode(e.detail)}
				on:exportResults={exportResults}
			/>
		</div>

		<!-- Messages -->
		{#if error}
			<EntityMessages {error} on:clearError={() => (error = null)} />
		{/if}

		{#if successMessage}
			<EntityMessages {successMessage} on:clearSuccess={() => (successMessage = null)} />
		{/if}

		<!-- Loading State -->
		{#if loading}
			<div class="flex items-center justify-center py-8">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else}
			<!-- Classes Grid -->
			<EntityDataTable
				{loading}
				paginatedData={{
					content: clases as unknown as Record<string, unknown>[],
					totalElements,
					totalPages,
					currentPage
				} as PaginatedEntities<Record<string, unknown>>}
				{currentPagination}
				{authStore}
				columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
				actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
				entityName="clase"
				entityNamePlural="clases"
				on:changeSorting={(e) => changeSorting(e.detail)}
			/>

			<!-- Pagination -->
			<EntityPaginationControls
				{pageDisplayInfo}
				{currentPagination}
				{sortFields}
				{pageSizeOptions}
				on:goToPage={(e) => goToPage(e.detail)}
				on:changePageSize={(e) => changePageSize(e.detail)}
				on:changeSorting={(e) => changeSorting(e.detail)}
			/>
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
