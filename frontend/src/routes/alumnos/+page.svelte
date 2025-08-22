<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let alumnos = $state<DTOAlumno[]>([]);
	let totalElements = $state(0);
	let totalPages = $state(0);
	let currentPage = $state(0);
	let pageSize = $state(20);

	// Search and filters
	let searchTerm = $state('');
	let matriculadoFilter = $state<boolean | null>(null);

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

	// Simple data loading
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

			// Add filters
			if (searchTerm.trim()) {
				params.nombre = searchTerm.trim();
			}
			if (matriculadoFilter !== null) {
				params.matriculado = matriculadoFilter;
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

	// function changePageSize(newSize: number) {
	// 	pageSize = newSize;
	// 	currentPage = 0;
	// 	loadAlumnos();
	// }

	// Search and filter functions
	function handleSearch() {
		currentPage = 0;
		loadAlumnos();
	}

	function clearFilters() {
		searchTerm = '';
		matriculadoFilter = null;
		currentPage = 0;
		loadAlumnos();
	}

	// Student actions
	async function toggleEnrollmentStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) {
			error = 'No tienes permisos para cambiar el estado de matrícula';
			return;
		}

		try {
			loading = true;
			const updatedAlumno = await AlumnoService.changeEnrollmentStatus(
				alumno.id!,
				!alumno.matriculado
			);

			// Update the local data
			const index = alumnos.findIndex((a) => a.id === alumno.id);
			if (index !== -1) {
				alumnos[index] = updatedAlumno;
				alumnos = [...alumnos]; // Trigger reactivity
			}

			successMessage = `Estado de matrícula actualizado para ${updatedAlumno.nombre}`;
			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
			console.error('Error toggling enrollment status:', err);
		} finally {
			loading = false;
		}
	}

	async function toggleEnabledStatus(alumno: DTOAlumno) {
		if (!authStore.isAdmin) {
			error = 'No tienes permisos para cambiar el estado del alumno';
			return;
		}

		try {
			loading = true;
			const updatedAlumno = await AlumnoService.toggleEnabled(alumno.id!, !alumno.enabled);

			// Update the local data
			const index = alumnos.findIndex((a) => a.id === alumno.id);
			if (index !== -1) {
				alumnos[index] = updatedAlumno;
				alumnos = [...alumnos]; // Trigger reactivity
			}

			successMessage = `Estado actualizado para ${updatedAlumno.nombre}`;
			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			error = `Error al cambiar estado: ${err}`;
			console.error('Error toggling enabled status:', err);
		} finally {
			loading = false;
		}
	}

	async function deleteAlumno(alumno: DTOAlumno) {
		if (!authStore.isAdmin) {
			error = 'No tienes permisos para eliminar alumnos';
			return;
		}

		try {
			loading = true;
			await AlumnoService.deleteAlumno(alumno.id!);

			// Remove from local data
			alumnos = alumnos.filter((a) => a.id !== alumno.id);
			totalElements--;

			successMessage = `Alumno ${alumno.nombre} eliminado exitosamente`;
			setTimeout(() => {
				successMessage = null;
			}, 3000);

			showDeleteModal = false;
			alumnoToDelete = null;
		} catch (err) {
			error = `Error al eliminar alumno: ${err}`;
			console.error('Error deleting alumno:', err);
		} finally {
			loading = false;
		}
	}

	function openDeleteModal(alumno: DTOAlumno) {
		alumnoToDelete = alumno;
		showDeleteModal = true;
	}

	function closeDeleteModal() {
		showDeleteModal = false;
		alumnoToDelete = null;
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<h1 class="mb-2 text-3xl font-bold text-gray-900">Alumnos</h1>
		<p class="text-gray-600">Gestiona los alumnos del sistema</p>
	</div>

	<!-- Messages -->
	{#if error}
		<div class="mb-4 rounded-md border border-red-200 bg-red-50 p-4">
			<div class="flex">
				<div class="flex-shrink-0">
					<svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
						<path
							fill-rule="evenodd"
							d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
							clip-rule="evenodd"
						/>
					</svg>
				</div>
				<div class="ml-3">
					<h3 class="text-sm font-medium text-red-800">Error</h3>
					<div class="mt-2 text-sm text-red-700">{error}</div>
				</div>
			</div>
		</div>
	{/if}

	{#if successMessage}
		<div class="mb-4 rounded-md border border-green-200 bg-green-50 p-4">
			<div class="flex">
				<div class="flex-shrink-0">
					<svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
						<path
							fill-rule="evenodd"
							d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
							clip-rule="evenodd"
						/>
					</svg>
				</div>
				<div class="ml-3">
					<h3 class="text-sm font-medium text-green-800">Éxito</h3>
					<div class="mt-2 text-sm text-green-700">{successMessage}</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- Search and Filters -->
	<div class="mb-6 rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
		<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
			<!-- Search -->
			<div>
				<label for="search" class="block text-sm font-medium text-gray-700">Buscar</label>
				<div class="mt-1 flex rounded-md shadow-sm">
					<input
						type="text"
						id="search"
						bind:value={searchTerm}
						placeholder="Nombre del alumno..."
						class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						onkeydown={(e) => e.key === 'Enter' && handleSearch()}
					/>
					<button
						type="button"
						onclick={handleSearch}
						class="ml-3 inline-flex items-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						Buscar
					</button>
				</div>
			</div>

			<!-- Matriculado Filter -->
			<div>
				<label for="matriculado" class="block text-sm font-medium text-gray-700">
					Estado de Matrícula
				</label>
				<select
					id="matriculado"
					bind:value={matriculadoFilter}
					onchange={handleSearch}
					class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
				>
					<option value={null}>Todos</option>
					<option value={true}>Matriculados</option>
					<option value={false}>No Matriculados</option>
				</select>
			</div>

			<!-- Actions -->
			<div class="flex items-end">
				<button
					type="button"
					onclick={clearFilters}
					class="inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
				>
					Limpiar Filtros
				</button>
				{#if authStore.isAdmin}
					<a
						href="/alumnos/nuevo"
						class="ml-3 inline-flex items-center rounded-md border border-transparent bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:outline-none"
					>
						Nuevo Alumno
					</a>
				{/if}
			</div>
		</div>
	</div>

	<!-- Loading State -->
	{#if loading}
		<div class="flex h-64 items-center justify-center">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else}
		<!-- Students Table -->
		<div class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Alumno
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Contacto
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Estado
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							Acciones
						</th>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					{#each alumnos as alumno (alumno.id)}
						<tr>
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="flex items-center">
									<div>
										<div class="text-sm font-medium text-gray-900">
											{alumno.nombre}
											{alumno.apellidos}
										</div>
										<div class="text-sm text-gray-500">@{alumno.usuario}</div>
									</div>
								</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="text-sm text-gray-900">{alumno.email}</div>
								{#if alumno.numeroTelefono}
									<div class="text-sm text-gray-500">{alumno.numeroTelefono}</div>
								{/if}
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="flex flex-col space-y-1">
									<span
										class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {alumno.matriculado
											? 'bg-green-100 text-green-800'
											: 'bg-red-100 text-red-800'}"
									>
										{alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
									</span>
									<span
										class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {alumno.enabled
											? 'bg-blue-100 text-blue-800'
											: 'bg-gray-100 text-gray-800'}"
									>
										{alumno.enabled ? 'Activo' : 'Inactivo'}
									</span>
								</div>
							</td>
							<td class="px-6 py-4 text-sm font-medium whitespace-nowrap">
								<div class="flex space-x-2">
									<a href="/alumnos/{alumno.id}" class="text-blue-600 hover:text-blue-900"> Ver </a>
									{#if authStore.isAdmin}
										<button
											onclick={() => toggleEnrollmentStatus(alumno)}
											class="text-green-600 hover:text-green-900"
										>
											{alumno.matriculado ? 'Desmatricular' : 'Matricular'}
										</button>
										<button
											onclick={() => toggleEnabledStatus(alumno)}
											class="text-orange-600 hover:text-orange-900"
										>
											{alumno.enabled ? 'Desactivar' : 'Activar'}
										</button>
										<button
											onclick={() => openDeleteModal(alumno)}
											class="text-red-600 hover:text-red-900"
										>
											Eliminar
										</button>
									{/if}
								</div>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>

			<!-- Empty State -->
			{#if alumnos.length === 0}
				<div class="py-12 text-center">
					<svg
						class="mx-auto h-12 w-12 text-gray-400"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
						/>
					</svg>
					<h3 class="mt-2 text-sm font-medium text-gray-900">No hay alumnos</h3>
					<p class="mt-1 text-sm text-gray-500">
						{#if searchTerm || matriculadoFilter !== null}
							No se encontraron alumnos con los filtros aplicados.
						{:else}
							Comienza agregando un nuevo alumno.
						{/if}
					</p>
				</div>
			{/if}
		</div>

		<!-- Pagination -->
		{#if totalPages > 1}
			<div class="mt-6 flex items-center justify-between">
				<div class="flex flex-1 justify-between sm:hidden">
					<button
						onclick={() => goToPage(currentPage - 1)}
						disabled={currentPage === 0}
						class="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
					>
						Anterior
					</button>
					<button
						onclick={() => goToPage(currentPage + 1)}
						disabled={currentPage >= totalPages - 1}
						class="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
					>
						Siguiente
					</button>
				</div>
				<div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
					<div>
						<p class="text-sm text-gray-700">
							Mostrando <span class="font-medium">{currentPage * pageSize + 1}</span> a{' '}
							<span class="font-medium">
								{Math.min((currentPage + 1) * pageSize, totalElements)}
							</span>{' '}
							de <span class="font-medium">{totalElements}</span> resultados
						</p>
					</div>
					<div>
						<nav class="isolate inline-flex -space-x-px rounded-md shadow-sm">
							<button
								onclick={() => goToPage(currentPage - 1)}
								disabled={currentPage === 0}
								class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-gray-300 ring-inset hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
							>
								<span class="sr-only">Anterior</span>
								<svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
									<path
										fill-rule="evenodd"
										d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z"
										clip-rule="evenodd"
									/>
								</svg>
							</button>
							{#each Array.from({ length: totalPages }, (_, i) => i) as pageNum (pageNum)}
								<button
									onclick={() => goToPage(pageNum)}
									class="relative inline-flex items-center px-4 py-2 text-sm font-semibold {pageNum ===
									currentPage
										? 'z-10 bg-blue-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600'
										: 'text-gray-900 ring-1 ring-gray-300 ring-inset hover:bg-gray-50 focus:z-20 focus:outline-offset-0'}"
								>
									{pageNum + 1}
								</button>
							{/each}
							<button
								onclick={() => goToPage(currentPage + 1)}
								disabled={currentPage >= totalPages - 1}
								class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-gray-300 ring-inset hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
							>
								<span class="sr-only">Siguiente</span>
								<svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
									<path
										fill-rule="evenodd"
										d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
										clip-rule="evenodd"
									/>
								</svg>
							</button>
						</nav>
					</div>
				</div>
			</div>
		{/if}
	{/if}
</div>

<!-- Delete Confirmation Modal -->
{#if showDeleteModal && alumnoToDelete}
	<div class="fixed inset-0 z-50 overflow-y-auto">
		<div
			class="flex min-h-screen items-end justify-center px-4 pt-4 pb-20 text-center sm:block sm:p-0"
		>
			<div class="bg-opacity-75 fixed inset-0 bg-gray-500 transition-opacity"></div>

			<div
				class="inline-block transform overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:align-middle"
			>
				<div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
					<div class="sm:flex sm:items-start">
						<div
							class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10"
						>
							<svg
								class="h-6 w-6 text-red-600"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
								/>
							</svg>
						</div>
						<div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
							<h3 class="text-lg leading-6 font-medium text-gray-900">Eliminar Alumno</h3>
							<div class="mt-2">
								<p class="text-sm text-gray-500">
									¿Estás seguro de que quieres eliminar a {alumnoToDelete.nombre}
									{alumnoToDelete.apellidos}? Esta acción no se puede deshacer.
								</p>
							</div>
						</div>
					</div>
				</div>
				<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
					<button
						type="button"
						onclick={() => deleteAlumno(alumnoToDelete!)}
						class="inline-flex w-full justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:ring-offset-2 focus:outline-none sm:ml-3 sm:w-auto sm:text-sm"
					>
						Eliminar
					</button>
					<button
						type="button"
						onclick={closeDeleteModal}
						class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
					>
						Cancelar
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}
