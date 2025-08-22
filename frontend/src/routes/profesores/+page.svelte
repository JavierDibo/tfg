<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOProfesor } from '$lib/generated/api';
	import { ProfesorService } from '$lib/services/profesorService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { PageData } from './$types';
	import { getPageDisplayInfo, type PaginatedData } from '$lib/types/pagination';
	import {
		EntityDataTable,
		EntitySearchSection,
		EntityPaginationControls,
		EntityDeleteModal,
		EntityMessages,
		type EntityColumn,
		type EntityAction,
		type PaginatedEntities
	} from '$lib/components/common';

	// Props from load function
	const { data }: { data: PageData } = $props();

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let allProfesores = $state<DTOProfesor[]>([]);
	let filteredAndSortedProfesores = $state<DTOProfesor[]>([]);

	// Modal state
	let showDeleteModal = $state(false);
	let profesorToDelete: DTOProfesor | null = $state(null);

	// Derived from URL
	const currentPagination = $derived(data.pagination);
	const currentFilters = $derived(data.filters);
	const currentUrl = $derived($page.url);

	// Load initial data
	$effect(() => {
		if (authStore.isAuthenticated && (authStore.isAdmin || authStore.isProfesor)) {
			loadData();
		}
	});

	// Reactive filtering, sorting, and pagination
	$effect(() => {
		let result = [...allProfesores];

		// Filtering
		if (currentFilters.searchMode === 'simple' && currentFilters.busquedaGeneral) {
			const searchTerm = normalizeText(currentFilters.busquedaGeneral);
			const searchWords = searchTerm.split(/\s+/).filter(Boolean);
			result = result.filter((p) => {
				const searchableText = [p.nombre, p.apellidos, p.usuario, p.dni, p.email, p.numeroTelefono]
					.map((field) => normalizeText(field || ''))
					.join(' ');
				return searchWords.every((word) => searchableText.includes(word));
			});
		} else if (currentFilters.searchMode === 'advanced') {
			if (currentFilters.nombre) {
				result = result.filter((p) =>
					normalizeText(p.nombre || '').includes(normalizeText(currentFilters.nombre!))
				);
			}
			if (currentFilters.apellidos) {
				result = result.filter((p) =>
					normalizeText(p.apellidos || '').includes(normalizeText(currentFilters.apellidos!))
				);
			}
			if (currentFilters.dni) {
				result = result.filter((p) =>
					normalizeText(p.dni || '').includes(normalizeText(currentFilters.dni!))
				);
			}
			if (currentFilters.email) {
				result = result.filter((p) =>
					normalizeText(p.email || '').includes(normalizeText(currentFilters.email!))
				);
			}
		}
		if (currentFilters.habilitado !== undefined) {
			result = result.filter((p) => p.enabled === currentFilters.habilitado);
		}

		// Sorting
		const { sortBy, sortDirection } = currentPagination;
		result.sort((a, b) => {
			const aValue = (a as Record<string, unknown>)[sortBy];
			const bValue = (b as Record<string, unknown>)[sortBy];
			if (aValue == null || bValue == null) return 0;

			let comparison = 0;
			if (typeof aValue === 'string' && typeof bValue === 'string') {
				comparison = aValue.localeCompare(bValue);
			} else if (aValue > bValue) {
				comparison = 1;
			} else if (aValue < bValue) {
				comparison = -1;
			}
			return sortDirection === 'DESC' ? -comparison : comparison;
		});

		filteredAndSortedProfesores = result;
	});

	// Derived paginated data and display info
	const { paginatedData, pageDisplayInfo } = $derived.by(() => {
		const start = currentPagination.page * currentPagination.size;
		const end = start + currentPagination.size;
		const pageContent = filteredAndSortedProfesores.slice(start, end);

		const totalElements = filteredAndSortedProfesores.length;
		const totalPages = Math.ceil(totalElements / currentPagination.size);

		const pageData = {
			content: pageContent,
			page: {
				size: currentPagination.size,
				number: currentPagination.page,
				totalElements: totalElements,
				totalPages: totalPages,
				first: currentPagination.page === 0,
				last: currentPagination.page >= totalPages - 1,
				hasNext: currentPagination.page < totalPages - 1,
				hasPrevious: currentPagination.page > 0
			}
		} as PaginatedData<DTOProfesor>;

		return {
			paginatedData: pageData,
			pageDisplayInfo: pageData.page ? getPageDisplayInfo(pageData.page) : null
		};
	});

	// Auth check
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
		} else if (!authStore.isAdmin) {
			goto('/');
		}
	});

	async function loadData() {
		loading = true;
		error = null;
		try {
			allProfesores = await ProfesorService.getAllProfesores({});
		} catch (err) {
			error = `Error al cargar profesores: ${(err as Error).message}`;
		} finally {
			loading = false;
		}
	}

	function normalizeText(text: string): string {
		return text
			.toLowerCase()
			.normalize('NFD')
			.replace(/[\u0300-\u036f]/g, '')
			.trim();
	}

	// Navigation and filter handlers
	function updateUrl(params: Record<string, string | number | boolean | undefined>) {
		const url = new URL(currentUrl);
		Object.entries(params).forEach(([key, value]) => {
			if (value !== undefined && value !== null && value !== '') {
				url.searchParams.set(key, String(value));
			} else {
				url.searchParams.delete(key);
			}
		});
		goto(url.toString());
	}

	function goToPage(page: number) {
		updateUrl({ page: Math.max(0, page - 1) });
	}

	function changePageSize(newSize: number) {
		updateUrl({ size: newSize, page: 0 });
	}

	function changeSorting(newSortBy: string | { value: string; direction: 'ASC' | 'DESC' }) {
		if (typeof newSortBy === 'string') {
			const newDirection =
				currentPagination.sortBy === newSortBy
					? currentPagination.sortDirection === 'ASC'
						? 'DESC'
						: 'ASC'
					: 'ASC';
			updateUrl({ sortBy: newSortBy, sortDirection: newDirection, page: 0 });
		} else {
			updateUrl({ sortBy: newSortBy.value, sortDirection: newSortBy.direction, page: 0 });
		}
	}

	function updateFilters(newFilters: Record<string, string | boolean | undefined>) {
		updateUrl({ ...newFilters, page: 0 });
	}

	function clearFilters() {
		const url = new URL(currentUrl);
		['nombre', 'apellidos', 'dni', 'email', 'habilitado', 'busquedaGeneral'].forEach((key) => {
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
			['nombre', 'apellidos', 'dni', 'email'].forEach((k) => url.searchParams.delete(k));
		} else {
			url.searchParams.delete('busquedaGeneral');
		}
		goto(url.toString());
	}

	async function toggleAccountStatus(profesor: DTOProfesor) {
		if (!authStore.isAdmin) return;
		try {
			loading = true;
			const updatedProfesor = await ProfesorService.toggleAccountStatus(
				profesor.id!,
				!profesor.enabled
			);
			const index = allProfesores.findIndex((p) => p.id === profesor.id);
			if (index !== -1) {
				allProfesores[index] = updatedProfesor;
				allProfesores = [...allProfesores]; // Trigger reactivity
			}
			successMessage = `Cuenta ${updatedProfesor.enabled ? 'habilitada' : 'deshabilitada'}`;
		} catch (err) {
			error = `Error al cambiar estado de cuenta: ${(err as Error).message}`;
		} finally {
			loading = false;
		}
	}

	function confirmDelete(profesor: DTOProfesor) {
		profesorToDelete = profesor;
		showDeleteModal = true;
	}

	async function deleteProfesor() {
		if (!profesorToDelete || !authStore.isAdmin) return;
		try {
			loading = true;
			await ProfesorService.deleteProfesor(profesorToDelete.id!);
			allProfesores = allProfesores.filter((p) => p.id !== profesorToDelete!.id);
			successMessage = 'Profesor eliminado correctamente';
		} catch (err) {
			error = `Error al eliminar profesor: ${(err as Error).message}`;
		} finally {
			showDeleteModal = false;
			profesorToDelete = null;
			loading = false;
		}
	}

	async function exportResults() {
		try {
			loading = true;

			// Filter by current filters
			const filteredData = [...filteredAndSortedProfesores];

			if (filteredData.length === 0) {
				error = 'No hay datos para exportar con los filtros actuales';
				return;
			}

			const csvContent = generateCSV(filteredData);
			downloadCSV(csvContent, `profesores-export-${filteredData.length}-records.csv`);

			successMessage = `Exportados ${filteredData.length} profesores correctamente`;
		} catch (err) {
			error = `Error al exportar datos: ${err}`;
		} finally {
			loading = false;
		}
	}

	function generateCSV(data: DTOProfesor[]): string {
		const headers = [
			'ID',
			'Nombre Completo',
			'DNI',
			'Email',
			'Teléfono',
			'Fecha Creación',
			'Habilitado'
		];
		const rows = data.map((profesor) => [
			profesor.id || '',
			profesor.nombreCompleto || `${profesor.nombre} ${profesor.apellidos}`,
			profesor.dni,
			profesor.email,
			profesor.numeroTelefono || '',
			profesor.fechaCreacion ? formatDate(profesor.fechaCreacion) : '',
			profesor.enabled ? 'Sí' : 'No'
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

	function formatDate(date: Date | string | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	const sortFields = [
		{ value: 'id', label: 'ID' },
		{ value: 'usuario', label: 'Usuario' },
		{ value: 'nombre', label: 'Nombre' },
		{ value: 'apellidos', label: 'Apellidos' },
		{ value: 'dni', label: 'DNI' },
		{ value: 'email', label: 'Email' },
		{ value: 'enabled', label: 'Habilitado' },
		{ value: 'fechaCreacion', label: 'Fecha Creación' }
	];

	const pageSizeOptions = [10, 20, 50, 100];

	// Table columns configuration
	const tableColumns: EntityColumn<DTOProfesor>[] = [
		{
			key: 'id',
			header: 'ID',
			sortable: true
		},
		{
			key: 'nombreCompleto',
			header: 'Nombre',
			sortable: true,
			formatter: (_, profesor) =>
				profesor.nombreCompleto || `${profesor.nombre} ${profesor.apellidos}`
		},
		{
			key: 'email',
			header: 'Email',
			sortable: true
		},
		{
			key: 'dni',
			header: 'DNI',
			sortable: true
		},
		{
			key: 'enabled',
			header: 'Estado',
			sortable: true,
			html: true,
			formatter: (_, profesor) => {
				return profesor.enabled
					? '<span class="inline-flex rounded-full bg-green-100 px-2 text-xs leading-5 font-semibold text-green-800">Habilitado</span>'
					: '<span class="inline-flex rounded-full bg-red-100 px-2 text-xs leading-5 font-semibold text-red-800">Deshabilitado</span>';
			}
		},
		{
			key: 'fechaCreacion',
			header: 'Fecha de Creación',
			sortable: true,
			formatter: (date) => formatDate(date as string | Date | undefined)
		}
	];

	// Table actions
	const tableActions: EntityAction<DTOProfesor>[] = [
		{
			label: 'Ver',
			color: 'blue',
			hoverColor: 'blue',
			action: (profesor) => goto(`/profesores/${profesor.id}`)
		},
		{
			label: 'Habilitar/Deshabilitar',
			dynamicLabel: (profesor) => (profesor.enabled ? 'Deshabilitar' : 'Habilitar'),
			color: 'yellow',
			hoverColor: 'yellow',
			condition: () => !!authStore.isAdmin,
			action: toggleAccountStatus
		},
		{
			label: 'Eliminar',
			color: 'red',
			hoverColor: 'red',
			condition: () => !!authStore.isAdmin,
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

	// Status field configuration
	const statusField = {
		key: 'habilitado',
		label: 'Estado de la cuenta',
		options: [
			{ value: '', label: 'Todos' },
			{ value: 'true', label: 'Habilitado' },
			{ value: 'false', label: 'Deshabilitado' }
		]
	};
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Profesores</h1>
		{#if authStore.isAdmin}
			<button
				onclick={() => goto('/profesores/nuevo')}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
			>
				Nuevo Profesor
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
		paginatedData={paginatedData as PaginatedEntities<Record<string, unknown>>}
		{loading}
		entityNamePlural="profesores"
		advancedFields={advancedSearchFields}
		{statusField}
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
		paginatedData={paginatedData as PaginatedEntities<Record<string, unknown>>}
		{currentPagination}
		{authStore}
		columns={tableColumns as unknown as EntityColumn<Record<string, unknown>>[]}
		actions={tableActions as unknown as EntityAction<Record<string, unknown>>[]}
		entityName="profesor"
		entityNamePlural="profesores"
		on:changeSorting={(e) => changeSorting(e.detail)}
	/>

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

<EntityDeleteModal
	showModal={showDeleteModal}
	entity={profesorToDelete as unknown as Record<string, unknown>}
	entityName="profesor"
	entityNameCapitalized="Profesor"
	displayNameField="nombreCompleto"
	on:cancelDelete={() => {
		showDeleteModal = false;
		profesorToDelete = null;
	}}
	on:confirmDelete={deleteProfesor}
/>
