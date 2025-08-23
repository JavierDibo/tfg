<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
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
		EntityAction,
		EntityPagination
	} from '$lib/components/common/types';
	import { getSearchConfig } from '$lib/components/common/searchConfigs';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let alumnos = $state<DTOAlumno[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);

	// Search configuration
	const searchConfig = getSearchConfig('alumnos');

	// Search and filters
	let currentFilters = $state<EntityFilters>({
		searchMode: 'simple',
		q: '',
		nombre: '',
		apellidos: '',
		dni: '',
		email: '',
		matriculado: ''
	});

	// Modal state
	let showDeleteModal = $state(false);
	let alumnoToDelete: DTOAlumno | null = $state(null);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAdmin && !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	// Load data when component mounts or filters change
	$effect(() => {
		if (authStore.isAuthenticated && (authStore.isAdmin || authStore.isProfesor)) {
			loadAlumnos();
		}
	});

	// Data loading with new search parameters
	async function loadAlumnos() {
		loading = true;
		error = null;

		try {
			const params: Record<string, unknown> = {
				page: currentPage,
				size: pageSize,
				sortBy: 'nombre',
				sortDirection: 'ASC'
			};

			// Add search parameters
			if (currentFilters.q && typeof currentFilters.q === 'string' && currentFilters.q.trim()) {
				params.q = currentFilters.q.trim();
			}
			if (
				currentFilters.nombre &&
				typeof currentFilters.nombre === 'string' &&
				currentFilters.nombre.trim()
			) {
				params.nombre = currentFilters.nombre.trim();
			}
			if (
				currentFilters.apellidos &&
				typeof currentFilters.apellidos === 'string' &&
				currentFilters.apellidos.trim()
			) {
				params.apellidos = currentFilters.apellidos.trim();
			}
			if (
				currentFilters.dni &&
				typeof currentFilters.dni === 'string' &&
				currentFilters.dni.trim()
			) {
				params.dni = currentFilters.dni.trim();
			}
			if (
				currentFilters.email &&
				typeof currentFilters.email === 'string' &&
				currentFilters.email.trim()
			) {
				params.email = currentFilters.email.trim();
			}
			if (currentFilters.matriculado !== '' && currentFilters.matriculado !== null) {
				params.matriculado = currentFilters.matriculado === 'true';
			}

			const response = await AlumnoService.getPaginatedAlumnos(params);

			alumnos = response.contenido || [];
			totalElements = response.totalElementos || 0;
			totalPages = response.totalPaginas || 0;
			currentPage = response.numeroPagina || 0;
		} catch (err) {
			error = `Error al cargar alumnos: ${err}`;
			console.error('Error loading alumnos:', err);
		} finally {
			loading = false;
		}
	}

	// Navigation functions
	function goToPage(page: number) {
		currentPage = page;
		loadAlumnos();
	}

	function changePageSize(newSize: number) {
		pageSize = newSize;
		currentPage = 0;
		loadAlumnos();
	}

	// Search and filter functions
	function updateFilters(filters: Record<string, unknown>) {
		Object.assign(currentFilters, filters);
		currentPage = 0;
		loadAlumnos();
	}

	function clearFilters() {
		currentFilters = {
			searchMode: 'simple',
			q: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			matriculado: ''
		};
		currentPage = 0;
		loadAlumnos();
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		currentFilters.searchMode = mode;
	}

	// Student actions
	async function toggleEnrollmentStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.changeEnrollmentStatus(alumno.id || 0, !alumno.matriculado);
			successMessage = `Alumno ${alumno.matriculado ? 'desmatriculado' : 'matriculado'} exitosamente`;
			loadAlumnos();
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
		} finally {
			loading = false;
		}
	}

	async function toggleEnabledStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.toggleEnabled(alumno.id || 0, !alumno.enabled);
			successMessage = `Alumno ${alumno.enabled ? 'deshabilitado' : 'habilitado'} exitosamente`;
			loadAlumnos();
		} catch (err) {
			error = `Error al cambiar estado del alumno: ${err}`;
		} finally {
			loading = false;
		}
	}

	function openDeleteModal(alumno: DTOAlumno) {
		alumnoToDelete = alumno;
		showDeleteModal = true;
	}

	async function deleteAlumno() {
		if (!alumnoToDelete || !authStore.isAdmin) return;

		try {
			loading = true;
			await AlumnoService.deleteAlumno(alumnoToDelete.id || 0);
			successMessage = 'Alumno eliminado exitosamente';
			showDeleteModal = false;
			alumnoToDelete = null;
			loadAlumnos();
		} catch (err) {
			error = `Error al eliminar alumno: ${err}`;
		} finally {
			loading = false;
		}
	}

	// Table configuration
	const tableColumns = [
		{ key: 'nombre', label: 'Nombre', sortable: true },
		{ key: 'apellidos', label: 'Apellidos', sortable: true },
		{ key: 'dni', label: 'DNI', sortable: true },
		{ key: 'email', label: 'Email', sortable: true },
		{ key: 'matriculado', label: 'Estado de Matrícula', sortable: true },
		{ key: 'enabled', label: 'Estado', sortable: true }
	];

	const tableActions = [
		{
			label: 'Ver',
			action: (alumno: DTOAlumno) => goto(`/alumnos/${alumno.id}`),
			condition: () => true
		},
		{
			label: 'Matricular',
			action: toggleEnrollmentStatus,
			condition: (alumno: DTOAlumno) => !alumno.matriculado && authStore.isAdmin
		},
		{
			label: 'Desmatricular',
			action: toggleEnrollmentStatus,
			condition: (alumno: DTOAlumno) => alumno.matriculado && authStore.isAdmin
		},
		{
			label: 'Activar',
			action: toggleEnabledStatus,
			condition: (alumno: DTOAlumno) => !alumno.enabled && authStore.isAdmin
		},
		{
			label: 'Desactivar',
			action: toggleEnabledStatus,
			condition: (alumno: DTOAlumno) => alumno.enabled && authStore.isAdmin
		},
		{
			label: 'Eliminar',
			action: openDeleteModal,
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
		sortBy: 'nombre',
		sortDirection: 'ASC' as const
	});

	const sortFields = [
		{ value: 'nombre', label: 'Nombre' },
		{ value: 'apellidos', label: 'Apellidos' },
		{ value: 'dni', label: 'DNI' },
		{ value: 'email', label: 'Email' },
		{ value: 'matriculado', label: 'Estado de Matrícula' },
		{ value: 'enabled', label: 'Estado' }
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

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Alumnos</h1>
		{#if authStore.isAdmin}
			<button
				onclick={() => goto('/alumnos/nuevo')}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
			>
				Nuevo Alumno
			</button>
		{/if}
	</div>

	<EntityMessages
		{successMessage}
		{error}
		on:clearSuccess={() => (successMessage = null)}
		on:clearError={() => (error = null)}
	/>

	<EntitySearchSection
		{currentFilters}
		paginatedData={{
			content: alumnos as unknown as Record<string, unknown>[],
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

	<div class="pt-10 pb-5">
		<EntityPaginationControls
			{pageDisplayInfo}
			{currentPagination}
			{sortFields}
			{pageSizeOptions}
			on:goToPage={(e) => goToPage(e.detail)}
			on:changePageSize={(e) => changePageSize(e.detail)}
			on:changeSorting={(e) => changeSorting(e.detail)}
		/>
	</div>

	<EntityDataTable
		{loading}
		paginatedData={{
			content: alumnos as unknown as Record<string, unknown>[],
			totalElements,
			totalPages,
			currentPage
		} as PaginatedEntities<Record<string, unknown>>}
		{currentPagination}
		{authStore}
		columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
		actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
		entityName="alumno"
		entityNamePlural="alumnos"
		on:changeSorting={(e) => changeSorting(e.detail)}
	/>

	{#if pageDisplayInfo && pageDisplayInfo.totalPages > 1}
		<div class="mt-6">
			<EntityPaginationControls
				{pageDisplayInfo}
				{currentPagination}
				{sortFields}
				{pageSizeOptions}
				on:goToPage={(e) => goToPage(e.detail)}
				on:changePageSize={(e) => changePageSize(e.detail)}
				on:changeSorting={(e) => changeSorting(e.detail)}
			/>
		</div>
	{/if}
</div>

<!-- Delete Modal -->
{#if showDeleteModal && alumnoToDelete}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="rounded-lg bg-white p-6 shadow-xl">
			<h3 class="mb-4 text-lg font-semibold">Confirmar eliminación</h3>
			<p class="mb-6 text-gray-600">
				¿Estás seguro de que quieres eliminar al alumno {alumnoToDelete.nombre}
				{alumnoToDelete.apellidos}? Esta acción no se puede deshacer.
			</p>
			<div class="flex justify-end space-x-3">
				<button
					onclick={() => (showDeleteModal = false)}
					class="rounded-md bg-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-400"
				>
					Cancelar
				</button>
				<button
					onclick={deleteAlumno}
					class="rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700"
				>
					Eliminar
				</button>
			</div>
		</div>
	</div>
{/if}
