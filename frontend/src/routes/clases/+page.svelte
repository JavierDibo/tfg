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
		EnhancedDataTable,
		EntityPaginationControls,
		EntityMessages,
		EntityDeleteModal,
		createColumns,
		commonColumns
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
	let sortBy = $state('id');
	let sortDirection = $state<'ASC' | 'DESC'>('ASC');

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
				sortBy: sortBy,
				sortDirection: sortDirection
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

			clases = response.content || [];
			totalElements = response.totalElements || 0;
			totalPages = response.totalPages || 0;
			currentPage = response.page || 0;
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

	// Enhanced table configuration using utilities
	const tableColumns = createColumns({
		id: commonColumns.class.id,
		title: commonColumns.class.title,
		description: commonColumns.class.description,
		level: commonColumns.class.level,
		presenciality: commonColumns.class.presenciality,
		price: commonColumns.class.price
	});

	const tableActions = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (clase: DTOClase) => goto(`/clases/${clase.id}`),
			condition: () => true
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red',
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
		sortBy: sortBy,
		sortDirection: sortDirection
	});

	const sortFields = [
		{ value: 'id', label: 'ID' },
		{ value: 'titulo', label: 'Título' },
		{ value: 'descripcion', label: 'Descripción' },
		{ value: 'nivel', label: 'Nivel' },
		{ value: 'presencialidad', label: 'Presencialidad' },
		{ value: 'precio', label: 'Precio' }
	];

	const pageSizeOptions = [10, 20, 50, 100];

	function changeSorting(field: string | { value: string; direction: 'ASC' | 'DESC' }) {
		if (typeof field === 'string') {
			// Toggle direction if same field, otherwise set to ASC
			if (sortBy === field) {
				sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
			} else {
				sortBy = field;
				sortDirection = 'ASC';
			}
		} else {
			sortBy = field.value;
			sortDirection = field.direction;
		}

		currentPage = 0; // Reset to first page when sorting
		loadClases();
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
			<!-- Enhanced Classes Table -->
			<EnhancedDataTable
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
				theme="modern"
				showRowNumbers={true}
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

<EntityDeleteModal
	showModal={showDeleteModal}
	entity={claseToDelete as unknown as Record<string, unknown>}
	entityName="clase"
	entityNameCapitalized="Clase"
	displayNameField="titulo"
	on:cancelDelete={cancelDelete}
	on:confirmDelete={deleteClase}
/>
