<script lang="ts">
	import { onMount } from 'svelte';
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import type { DTOAlumno } from '$lib/generated/api/models/DTOAlumno';
	import type { DTORespuestaPaginadaDTOAlumno } from '$lib/generated/api/models/DTORespuestaPaginadaDTOAlumno';
	import { ClaseService } from '$lib/services/claseService';

	let { clase } = $props<{
		clase: DTOClase;
	}>();

	// State
	let alumnos = $state<DTOAlumno[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let currentPage = $state(1);
	let totalPages = $state(1);
	let totalElements = $state(0);
	let pageSize = $state(10);

	// Load students for the class
	async function loadAlumnos() {
		if (!clase?.id) return;

		try {
			loading = true;
			error = null;

			const response: DTORespuestaPaginadaDTOAlumno = await ClaseService.getAlumnosDeClase(
				clase.id,
				{
					page: currentPage - 1, // API uses 0-based pagination
					size: pageSize,
					sortBy: 'nombre',
					sortDirection: 'ASC'
				}
			);

			alumnos = response.content || [];
			totalElements = response.page?.totalElements || 0;
			totalPages = response.page?.totalPages || 1;
		} catch (err) {
			console.error('Error loading students:', err);
			error = 'Error al cargar los alumnos';
		} finally {
			loading = false;
		}
	}

	// Handle page change
	function handlePageChange(page: number) {
		currentPage = page;
		loadAlumnos();
	}

	// Reload when class changes
	$effect(() => {
		if (clase?.id) {
			loadAlumnos();
		}
	});

	onMount(() => {
		if (clase?.id) {
			loadAlumnos();
		}
	});
</script>

<div class="rounded-lg bg-white p-6 shadow">
	<h3 class="mb-4 text-lg font-medium text-gray-900">Alumnos Inscritos</h3>

	{#if loading}
		<div class="flex h-32 items-center justify-center">
			<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<div class="rounded-md border border-red-200 bg-red-50 p-4">
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
	{:else if alumnos.length > 0}
		<div class="space-y-3">
			{#each alumnos as alumno (alumno.id)}
				<div class="flex items-center justify-between rounded-lg bg-gray-50 p-3">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100">
								<svg
									class="h-4 w-4 text-blue-600"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
									/>
								</svg>
							</div>
						</div>
						<div class="ml-3">
							<p class="text-sm font-medium text-gray-900">
								{alumno.nombre}
								{alumno.apellidos}
							</p>
							<p class="text-xs text-gray-500">{alumno.email}</p>
						</div>
					</div>
					<div class="flex items-center space-x-2">
						<span
							class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800"
						>
							Inscrito
						</span>
					</div>
				</div>
			{/each}
		</div>

		<!-- Pagination -->
		{#if totalPages > 1}
			<div class="mt-4 border-t border-gray-200 pt-4">
				<div class="flex items-center justify-between">
					<div class="text-sm text-gray-700">
						Mostrando {(currentPage - 1) * pageSize + 1} a {Math.min(
							currentPage * pageSize,
							totalElements
						)} de {totalElements} alumnos
					</div>
					<div class="flex space-x-2">
						{#if currentPage > 1}
							<button
								onclick={() => handlePageChange(currentPage - 1)}
								class="rounded bg-gray-50 px-3 py-1 text-sm text-gray-600 hover:bg-gray-100"
							>
								Anterior
							</button>
						{/if}
						{#if currentPage < totalPages}
							<button
								onclick={() => handlePageChange(currentPage + 1)}
								class="rounded bg-gray-50 px-3 py-1 text-sm text-gray-600 hover:bg-gray-100"
							>
								Siguiente
							</button>
						{/if}
					</div>
				</div>
			</div>
		{:else}
			<div class="mt-4 border-t border-gray-200 pt-4">
				<p class="text-sm text-gray-600">
					Total de alumnos inscritos: <span class="font-medium">{totalElements}</span>
				</p>
			</div>
		{/if}
	{:else}
		<div class="py-8 text-center">
			<svg
				class="mx-auto h-12 w-12 text-gray-400"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
				/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">No hay alumnos inscritos</h3>
			<p class="mt-1 text-sm text-gray-500">Aún no se han inscrito alumnos en esta clase.</p>
		</div>
	{/if}
</div>
