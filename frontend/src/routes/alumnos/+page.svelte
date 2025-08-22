<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { PageData } from './$types';
	import { getPageDisplayInfo, type PaginatedAlumnosResponse } from '$lib/types/pagination';
	import {
		EntityDataTable,
		EntitySearchSection,
		EntityPaginationControls,
		EntityDeleteModal,
		EntityMessages,
		type EntityColumn,
		type EntityAction
	} from '$lib/components/common';

	// Props from load function
	const { data }: { data: PageData } = $props();

	// Minimal state - only what's not in URL
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let paginatedData = $state<PaginatedAlumnosResponse | null>(null);

	// Modal state
	let showDeleteModal = $state(false);
	let alumnoToDelete: DTOAlumno | null = $state(null);

	// All derived from URL - no duplication
	const currentPagination = $derived(data.pagination);
	const currentFilters = $derived(data.filters);
	const currentUrl = $derived($page.url);

	// UI helpers
	const pageDisplayInfo = $derived(
		paginatedData?.page ? getPageDisplayInfo(paginatedData.page) : null
	);

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

	// Track URL changes separately from data updates
	let lastUrlKey = $state('');

	// Load data when URL changes (pagination/filters) or auth changes
	$effect(() => {
		if (authStore.isAuthenticated && (authStore.isAdmin || authStore.isProfesor)) {
			const urlKey = `${currentPagination.page}-${currentPagination.size}-${currentPagination.sortBy}-${currentPagination.sortDirection}-${JSON.stringify(currentFilters)}`;

			if (urlKey !== lastUrlKey) {
				lastUrlKey = urlKey;
				loadData();
			}
		}
	});

	// Helper function to normalize text for case and accent insensitive search
	function normalizeText(text: string): string {
		return text
			.toLowerCase()
			.normalize('NFD') // Decompose accented characters
			.replace(/[\u0300-\u036f]/g, '') // Remove accent marks
			.trim();
	}

	// Simple data loading
	async function loadData() {
		loading = true;
		error = null;

		try {
			// Build search parameters based on search mode
			let searchParams: Record<string, unknown> = {
				...currentPagination,
				matriculado: currentFilters.matriculado
			};

			if (currentFilters.searchMode === 'advanced') {
				// Advanced mode: use specific field filters
				if (currentFilters.nombre?.trim()) searchParams.nombre = currentFilters.nombre.trim();
				if (currentFilters.apellidos?.trim())
					searchParams.apellidos = currentFilters.apellidos.trim();
				if (currentFilters.dni?.trim()) searchParams.dni = currentFilters.dni.trim();
				if (currentFilters.email?.trim()) searchParams.email = currentFilters.email.trim();
			}

			// Handle general search vs normal pagination
			if (currentFilters.searchMode === 'simple' && currentFilters.busquedaGeneral?.trim()) {
				// For general search, we need to search across all fields
				const searchTerm = normalizeText(currentFilters.busquedaGeneral);
				console.log('🔍 SEARCHING for:', `"${currentFilters.busquedaGeneral}" -> "${searchTerm}"`);

				// Get all students
				const allStudents = await AlumnoService.getAllFilteredAlumnos({
					matriculado: currentFilters.matriculado
				});
				console.log('📊 Total students:', allStudents.length);

				// Show a sample of actual data from backend
				if (allStudents.length > 0) {
					const sample = allStudents[0];
					console.log('📝 Sample student data:', {
						nombre: sample.nombre,
						apellidos: sample.apellidos,
						usuario: sample.usuario,
						normalized_nombre: normalizeText(sample.nombre || ''),
						normalized_apellidos: normalizeText(sample.apellidos || '')
					});
				}

				// Split search term into words for multi-word search
				const searchWords = searchTerm.split(/\s+/).filter((word) => word.length > 0);
				console.log('🔍 Search words:', searchWords);

				// Filter students - all words must be found somewhere in the searchable fields
				const filteredStudents = allStudents.filter((alumno: any) => {
					const searchableFields = [
						normalizeText(alumno.nombre || ''),
						normalizeText(alumno.apellidos || ''),
						normalizeText(alumno.usuario || ''),
						normalizeText(alumno.dni || ''),
						normalizeText(alumno.email || ''),
						normalizeText(alumno.numeroTelefono || '')
					];

					// Combine all searchable fields into one string
					const allFieldsText = searchableFields.join(' ');

					// Check that ALL search words are found in the combined text
					return searchWords.every((word) => allFieldsText.includes(word));
				});
				console.log('✅ Found matches:', filteredStudents.length);

				// Implement client-side pagination
				const pageSize = currentPagination.size;
				const currentPageIndex = currentPagination.page;
				const startIndex = currentPageIndex * pageSize;
				const endIndex = startIndex + pageSize;
				const paginatedResults = filteredStudents.slice(startIndex, endIndex);

				// Create paginated response structure - force reactivity
				const newPaginatedData = {
					content: paginatedResults,
					page: {
						size: pageSize,
						number: currentPageIndex,
						totalElements: filteredStudents.length,
						totalPages: Math.ceil(filteredStudents.length / pageSize),
						first: currentPageIndex === 0,
						last: currentPageIndex >= Math.ceil(filteredStudents.length / pageSize) - 1,
						hasNext: currentPageIndex < Math.ceil(filteredStudents.length / pageSize) - 1,
						hasPrevious: currentPageIndex > 0
					}
				};

				// Force Svelte 5 reactivity by reassigning
				paginatedData = newPaginatedData;
				console.log('📄 Paginated results for UI:', {
					totalResults: filteredStudents.length,
					currentPage: currentPageIndex,
					pageSize: pageSize,
					resultsOnThisPage: paginatedResults.length,
					firstResult: paginatedResults[0]?.nombre,
					paginatedDataSet: !!paginatedData
				});
			} else {
				// Normal backend pagination for advanced search or no search
				paginatedData = await AlumnoService.getPaginatedAlumnos(searchParams);
			}
		} catch (err) {
			error = `Error al cargar alumnos: ${err}`;
			console.error('Error loading alumnos:', err);
		} finally {
			loading = false;
		}
	}

	// Simple navigation - just update URL
	function goToPage(page: number) {
		const url = new URL(currentUrl);
		url.searchParams.set('page', Math.max(0, page - 1).toString());
		goto(url.toString());
	}

	function changePageSize(newSize: number) {
		const url = new URL(currentUrl);
		url.searchParams.set('size', newSize.toString());
		url.searchParams.set('page', '0'); // Reset to first page
		goto(url.toString());
	}

	function changeSorting(newSortBy: string | { value: string; direction: 'ASC' | 'DESC' }) {
		const url = new URL(currentUrl);

		if (typeof newSortBy === 'string') {
			url.searchParams.set('sortBy', newSortBy);

			// Toggle direction if same field
			const currentDirection = currentPagination.sortDirection === 'ASC' ? 'DESC' : 'ASC';
			url.searchParams.set('sortDirection', currentDirection);
		} else {
			url.searchParams.set('sortBy', newSortBy.value);
			url.searchParams.set('sortDirection', newSortBy.direction);
		}

		url.searchParams.set('page', '0'); // Reset to first page
		goto(url.toString());
	}

	function updateFilters(newFilters: Record<string, string | boolean | undefined>) {
		const url = new URL(currentUrl);
		url.searchParams.set('page', '0'); // Reset page

		Object.entries(newFilters).forEach(([key, value]) => {
			if (value !== undefined && value !== '') {
				url.searchParams.set(key, value.toString());
			} else {
				url.searchParams.delete(key);
			}
		});

		goto(url.toString());
	}

	function clearFilters() {
		const url = new URL(currentUrl);
		['nombre', 'apellidos', 'dni', 'email', 'matriculado', 'busquedaGeneral'].forEach((key) => {
			url.searchParams.delete(key);
		});
		url.searchParams.set('page', '0');
		url.searchParams.set('searchMode', 'simple');
		goto(url.toString());
	}

	function switchSearchMode(mode: 'simple' | 'advanced') {
		const url = new URL(currentUrl);
		url.searchParams.set('searchMode', mode);
		url.searchParams.set('page', '0');

		if (mode === 'simple') {
			// Clear advanced filters
			['nombre', 'apellidos', 'dni', 'email'].forEach((key) => {
				url.searchParams.delete(key);
			});
		} else {
			// Clear general search
			url.searchParams.delete('busquedaGeneral');
		}

		goto(url.toString());
	}

	// Student actions
	async function toggleEnrollmentStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) {
			error = 'No tienes permisos para cambiar el estado de matrícula';
			return;
		}

		// Prevent any potential focus-related scrolling
		(document.activeElement as HTMLElement)?.blur();

		try {
			loading = true;

			const updatedAlumno = await AlumnoService.changeEnrollmentStatus(
				alumno.id!,
				!alumno.matriculado
			);

			// Update the local data
			if (paginatedData?.content) {
				const index = paginatedData.content.findIndex((a: DTOAlumno) => a.id === alumno.id);
				if (index !== -1) {
					paginatedData.content[index] = updatedAlumno;
					paginatedData = { ...paginatedData }; // Trigger reactivity
				}
			}

			successMessage = `Estado de matrícula ${updatedAlumno.matriculado ? 'activado' : 'desactivado'} correctamente`;
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
		} finally {
			loading = false;
		}
	}

	async function toggleAccountStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) {
			error = 'No tienes permisos para habilitar/deshabilitar cuentas';
			return;
		}

		// Prevent any potential focus-related scrolling
		(document.activeElement as HTMLElement)?.blur();

		try {
			loading = true;

			const updatedAlumno = await AlumnoService.toggleAccountStatus(alumno.id!, !alumno.enabled);

			// Update the local data
			if (paginatedData?.content) {
				const index = paginatedData.content.findIndex((a: DTOAlumno) => a.id === alumno.id);
				if (index !== -1) {
					paginatedData.content[index] = updatedAlumno;
					paginatedData = { ...paginatedData }; // Trigger reactivity
				}
			}

			successMessage = `Cuenta ${updatedAlumno.enabled ? 'habilitada' : 'deshabilitada'} correctamente`;
		} catch (err) {
			error = `Error al cambiar estado de cuenta: ${err}`;
		} finally {
			loading = false;
		}
	}

	function confirmDelete(alumno: DTOAlumno) {
		alumnoToDelete = alumno;
		showDeleteModal = true;
	}

	async function deleteAlumno() {
		if (!alumnoToDelete || !authStore.isAdmin) return;

		try {
			loading = true;

			await AlumnoService.deleteAlumno(alumnoToDelete.id!);

			successMessage = 'Alumno eliminado correctamente';
			showDeleteModal = false;
			alumnoToDelete = null;

			// Reload data
			await loadData();
		} catch (err) {
			error = `Error al eliminar alumno: ${err}`;
			showDeleteModal = false;
			alumnoToDelete = null;
		} finally {
			loading = false;
		}
	}

	// Utility functions
	async function exportResults() {
		try {
			loading = true;

			// Build filters from current filter state - only include non-empty values
			const exportFilters: {
				nombre?: string;
				apellidos?: string;
				dni?: string;
				email?: string;
				matriculado?: boolean;
			} = {};

			// Add filters that are actually being used
			if (currentFilters.nombre?.trim()) exportFilters.nombre = currentFilters.nombre.trim();
			if (currentFilters.apellidos?.trim())
				exportFilters.apellidos = currentFilters.apellidos.trim();
			if (currentFilters.dni?.trim()) exportFilters.dni = currentFilters.dni.trim();
			if (currentFilters.email?.trim()) exportFilters.email = currentFilters.email.trim();
			if (currentFilters.matriculado !== undefined && currentFilters.matriculado !== null) {
				exportFilters.matriculado = currentFilters.matriculado;
			}

			// Get ALL filtered results (not just current page)
			const allFilteredData = await AlumnoService.getAllFilteredAlumnos(exportFilters);

			// Filter by general search if in simple mode (case and accent insensitive)
			let finalData = allFilteredData;
			if (currentFilters.searchMode === 'simple' && currentFilters.busquedaGeneral?.trim()) {
				const term = normalizeText(currentFilters.busquedaGeneral);
				const searchWords = term.split(/\s+/).filter((word) => word.length > 0);

				finalData = allFilteredData.filter((alumno) => {
					const searchableFields = [
						normalizeText(alumno.nombre || ''),
						normalizeText(alumno.apellidos || ''),
						normalizeText(alumno.usuario || ''),
						normalizeText(alumno.dni || ''),
						normalizeText(alumno.email || ''),
						normalizeText(alumno.numeroTelefono || '')
					];

					const allFieldsText = searchableFields.join(' ');
					return searchWords.every((word) => allFieldsText.includes(word));
				});
			}

			if (finalData.length === 0) {
				error = 'No hay datos para exportar con los filtros actuales';
				return;
			}

			const csvContent = generateCSV(finalData);
			downloadCSV(csvContent, `alumnos-export-${finalData.length}-records.csv`);

			successMessage = `Exportados ${finalData.length} alumnos correctamente`;
		} catch (err) {
			error = `Error al exportar datos: ${err}`;
		} finally {
			loading = false;
		}
	}

	function generateCSV(data: DTOAlumno[]): string {
		const headers = [
			'ID',
			'Usuario',
			'Nombre',
			'Apellidos',
			'DNI',
			'Email',
			'Teléfono',
			'Fecha Inscripción',
			'Matriculado',
			'Habilitado'
		];
		const rows = data.map((alumno) => [
			alumno.id || '',
			alumno.usuario,
			alumno.nombre,
			alumno.apellidos,
			alumno.dni,
			alumno.email,
			alumno.numeroTelefono || '',
			alumno.fechaInscripcion ? formatDate(alumno.fechaInscripcion) : '',
			alumno.matriculado ? 'Sí' : 'No',
			alumno.enabled ? 'Sí' : 'No'
		]);

		return [headers, ...rows].map((row) => row.map((field) => `"${field}"`).join(',')).join('\n');
	}

	function downloadCSV(content: string, filename: string) {
		const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
		const link = document.createElement('a');
		if (link.download !== undefined) {
			const url = URL.createObjectURL(blob);
			link.setAttribute('href', url);
			link.setAttribute('download', filename);
			link.style.visibility = 'hidden';
			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
		}
	}

	function formatDate(date: Date | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	// Configuration for table columns
	const tableColumns: EntityColumn<DTOAlumno>[] = [
		{
			key: 'nombre',
			header: 'Alumno',
			sortable: true,
			formatter: (_, alumno) => `${alumno.nombre} ${alumno.apellidos}`
		},
		{
			key: 'dni',
			header: 'DNI',
			sortable: true
		},
		{
			key: 'email',
			header: 'Email',
			sortable: true
		},
		{
			key: 'numeroTelefono',
			header: 'Teléfono'
		},
		{
			key: 'fechaInscripcion',
			header: 'Fecha Inscripción',
			sortable: true,
			formatter: (date) => formatDate(new Date(date as string))
		},
		{
			key: 'matriculado',
			header: 'Estado',
			sortable: true,
			formatter: (_, alumno) => {
				return `
					<div class="space-y-1">
						<span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
							alumno.matriculado ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
						}">
							${alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
						</span>
						<br />
						<span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
							alumno.enabled ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'
						}">
							${alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
						</span>
					</div>
				`;
			}
		}
	];

	// Table actions for students
	const tableActions: EntityAction<DTOAlumno>[] = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (alumno) => goto(`/alumnos/${alumno.id}`)
		},
		{
			label: 'Matrícula',
			dynamicLabel: (alumno) => (alumno.matriculado ? 'Desmatricular' : 'Matricular'),
			color: 'green',
			hoverColor: 'green',
			condition: (alumno) => !!authStore.isAdmin,
			action: toggleEnrollmentStatus
		},
		{
			label: 'Cuenta',
			dynamicLabel: (alumno) => (alumno.enabled ? 'Deshabilitar' : 'Habilitar'),
			color: 'yellow',
			hoverColor: 'yellow',
			condition: (alumno) => !!authStore.isAdmin,
			action: toggleAccountStatus
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red',
			condition: (alumno) => !!authStore.isAdmin,
			action: confirmDelete
		}
	];

	// Search fields configuration
	const advancedSearchFields = [
		{ key: 'nombre', label: 'Nombre', type: 'text', placeholder: 'Ej: Juan' },
		{ key: 'apellidos', label: 'Apellidos', type: 'text', placeholder: 'Ej: García López' },
		{ key: 'dni', label: 'DNI', type: 'text', placeholder: 'Ej: 12345678Z' },
		{ key: 'email', label: 'Email', type: 'email', placeholder: 'Ej: juan@universidad.es' }
	];

	// Status field configuration for search filters
	const statusField = {
		key: 'matriculado',
		label: 'Estado de Matrícula',
		options: [
			{ value: '', label: 'Todos' },
			{ value: 'true', label: '✅ Matriculados' },
			{ value: 'false', label: '❌ No Matriculados' }
		]
	};

	// Sort fields for pagination controls
	const sortFields = [
		{ value: 'id', label: 'ID' },
		{ value: 'usuario', label: 'Usuario' },
		{ value: 'nombre', label: 'Nombre' },
		{ value: 'apellidos', label: 'Apellidos' },
		{ value: 'dni', label: 'DNI' },
		{ value: 'email', label: 'Email' },
		{ value: 'matriculado', label: 'Estado Matrícula' },
		{ value: 'fechaCreacion', label: 'Fecha Creación' }
	];

	const pageSizeOptions = [10, 20, 50, 100];
</script>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
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

	<!-- Messages Component -->
	<EntityMessages
		{successMessage}
		{error}
		on:clearSuccess={() => (successMessage = null)}
		on:clearError={() => (error = null)}
	/>

	<!-- Search and Filter Section -->
	<EntitySearchSection
		{currentFilters}
		{paginatedData}
		{loading}
		entityNamePlural="alumnos"
		advancedFields={advancedSearchFields}
		{statusField}
		on:switchSearchMode={(e) => switchSearchMode(e.detail)}
		on:updateFilters={(e) => updateFilters(e.detail)}
		on:clearFilters={clearFilters}
		on:exportResults={exportResults}
	/>

	<!-- Pagination Controls Top -->
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

	<!-- Students Table -->
	<EntityDataTable
		{loading}
		{paginatedData}
		{currentPagination}
		{authStore}
		columns={tableColumns}
		actions={tableActions}
		entityName="alumno"
		entityNamePlural="alumnos"
		on:changeSorting={(e) => changeSorting(e.detail)}
	/>

	<!-- Pagination Bottom - Centered -->
	{#if pageDisplayInfo && pageDisplayInfo.totalPages > 1}
		<div class="mt-6 flex flex-col items-center gap-4">
			<EntityPaginationControls
				{pageDisplayInfo}
				{currentPagination}
				{sortFields}
				{pageSizeOptions}
				justifyContent="center"
				showSortAndSize={false}
				on:goToPage={(e) => goToPage(e.detail)}
				on:changePageSize={(e) => changePageSize(e.detail)}
				on:changeSorting={(e) => changeSorting(e.detail)}
			/>
		</div>
	{/if}
</div>

<!-- Delete Confirmation Modal -->
<EntityDeleteModal
	showModal={showDeleteModal}
	entity={alumnoToDelete}
	entityName="alumno"
	entityNameCapitalized="Alumno"
	displayNameField="nombre"
	on:cancelDelete={() => {
		showDeleteModal = false;
		alumnoToDelete = null;
	}}
	on:confirmDelete={deleteAlumno}
/>
