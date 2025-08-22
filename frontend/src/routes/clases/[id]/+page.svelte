<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOClase, DTOEstadoInscripcion } from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let clase = $state<DTOClase | null>(null);
	let numeroAlumnos = $state(0);
	let numeroProfesores = $state(0);
	let enrollmentStatus = $state<DTOEstadoInscripcion | null>(null);
	let enrollmentLoading = $state(false);
	let enrollmentError = $state<string | null>(null);

	// Get class ID from URL
	const claseId = $derived(Number($page.params.id));

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load class data
	$effect(() => {
		if (authStore.isAuthenticated && claseId) {
			loadClase();
			if (authStore.isAlumno) {
				checkEnrollmentStatus();
			}
		}
	});

	async function loadClase() {
		loading = true;
		error = null;

		try {
			// Load class details
			clase = await ClaseService.getClaseById(claseId);

			// Load additional statistics
			if (clase?.id) {
				try {
					numeroAlumnos = await ClaseService.contarAlumnosEnClase(clase.id);
					numeroProfesores = await ClaseService.contarProfesoresEnClase(clase.id);
				} catch (err) {
					console.warn('Error loading class statistics:', err);
				}
			}
		} catch (err) {
			error = `Error al cargar la clase: ${err}`;
			console.error('Error loading class:', err);
		} finally {
			loading = false;
		}
	}

	async function checkEnrollmentStatus() {
		if (!claseId) return;
		
		try {
			enrollmentStatus = await EnrollmentService.checkMyEnrollmentStatus(claseId);
		} catch (err) {
			console.warn('Error checking enrollment status:', err);
			// Don't show error to user, just assume not enrolled
			enrollmentStatus = { isEnrolled: false };
		}
	}

	async function handleEnrollment() {
		if (!claseId) return;
		
		enrollmentLoading = true;
		enrollmentError = null;

		try {
			const result = await EnrollmentService.toggleEnrollment(claseId);
			enrollmentStatus = { isEnrolled: result.isEnrolled, claseId, alumnoId: authStore.user?.id };
			
			// Refresh student count
			if (clase?.id) {
				numeroAlumnos = await ClaseService.contarAlumnosEnClase(clase.id);
			}
		} catch (err) {
			enrollmentError = enrollmentStatus?.isEnrolled 
				? `Error al darse de baja: ${err}` 
				: `Error al inscribirse: ${err}`;
		} finally {
			enrollmentLoading = false;
		}
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

	function getNivelText(nivel: string | undefined): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'Principiante';
			case 'INTERMEDIO':
				return 'Intermedio';
			case 'AVANZADO':
				return 'Avanzado';
			default:
				return 'N/A';
		}
	}

	function getPresencialidadText(presencialidad: string | undefined): string {
		switch (presencialidad) {
			case 'ONLINE':
				return 'Online';
			case 'PRESENCIAL':
				return 'Presencial';
			default:
				return 'N/A';
		}
	}
</script>

<svelte:head>
	<title>{clase?.titulo || 'Clase'} - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Back Button -->
	<div class="mb-6">
		<button
			onclick={() => goto('/clases')}
			class="flex items-center text-blue-600 hover:text-blue-800"
		>
			<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
			</svg>
			Volver a Clases
		</button>
	</div>

	<!-- Loading State -->
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<!-- Error State -->
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else if clase}
		<!-- Class Details -->
		<div class="overflow-hidden rounded-lg bg-white shadow-lg">
			<!-- Header with Image -->
			<div class="relative h-64 bg-gradient-to-r from-blue-500 to-purple-600">
				{#if clase.imagenPortada}
					<img src={clase.imagenPortada} alt={clase.titulo} class="h-full w-full object-cover" />
				{/if}
				<div class="bg-opacity-40 absolute inset-0 bg-black"></div>
				<div class="absolute right-0 bottom-0 left-0 p-6 text-white">
					<h1 class="mb-2 text-4xl font-bold">{clase.titulo || 'Sin título'}</h1>
					<div class="flex items-center space-x-4">
						<span class="text-2xl font-bold">{formatPrice(clase.precio)}</span>
						<span class="rounded-full px-3 py-1 text-sm font-medium {getNivelColor(clase.nivel)}">
							{getNivelText(clase.nivel)}
						</span>
						<span
							class="rounded-full px-3 py-1 text-sm font-medium {getPresencialidadColor(
								clase.presencialidad
							)}"
						>
							{getPresencialidadText(clase.presencialidad)}
						</span>
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="p-6">
				<!-- Description -->
				<div class="mb-8">
					<h2 class="mb-4 text-2xl font-semibold text-gray-900">Descripción</h2>
					<p class="leading-relaxed text-gray-700">
						{clase.descripcion || 'No hay descripción disponible para esta clase.'}
					</p>
				</div>

				<!-- Statistics Grid -->
				<div class="mb-8 grid grid-cols-1 gap-6 md:grid-cols-4">
					<div class="rounded-lg bg-blue-50 p-4 text-center">
						<div class="text-2xl font-bold text-blue-600">{numeroAlumnos}</div>
						<div class="text-sm text-gray-600">Alumnos inscritos</div>
					</div>
					<div class="rounded-lg bg-green-50 p-4 text-center">
						<div class="text-2xl font-bold text-green-600">{numeroProfesores}</div>
						<div class="text-sm text-gray-600">Profesores</div>
					</div>
					<div class="rounded-lg bg-purple-50 p-4 text-center">
						<div class="text-2xl font-bold text-purple-600">{clase.numeroMateriales || 0}</div>
						<div class="text-sm text-gray-600">Materiales</div>
					</div>
					<div class="rounded-lg bg-orange-50 p-4 text-center">
						<div class="text-2xl font-bold text-orange-600">{clase.numeroEjercicios || 0}</div>
						<div class="text-sm text-gray-600">Ejercicios</div>
					</div>
				</div>

				<!-- Materials Section -->
				{#if clase.material && clase.material.length > 0}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">Materiales</h2>
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#each clase.material as material (material.id)}
								<div class="rounded-lg bg-gray-50 p-4">
									<h3 class="mb-2 font-semibold text-gray-900">
										{material.nombre || 'Sin nombre'}
									</h3>
									{#if material.url}
										<a
											href={material.url}
											target="_blank"
											rel="noopener noreferrer"
											class="text-sm font-medium text-blue-600 hover:text-blue-800"
										>
											Ver material →
										</a>
									{/if}
								</div>
							{/each}
						</div>
					</div>
				{/if}

				<!-- Class Type Information -->
				{#if clase.tipoClase}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">Tipo de Clase</h2>
						<div class="rounded-lg bg-gray-50 p-4">
							<span
								class="rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-800"
							>
								{clase.tipoClase}
							</span>
						</div>
					</div>
				{/if}

				<!-- Action Buttons -->
				<div class="flex flex-col gap-4 border-t border-gray-200 pt-6 sm:flex-row">
					{#if authStore.isAlumno}
						<button
							onclick={handleEnrollment}
							class="flex-1 rounded-lg bg-blue-600 px-6 py-3 font-bold text-white hover:bg-blue-700 disabled:opacity-50"
							disabled={enrollmentLoading}
						>
							{enrollmentLoading ? 'Cargando...' : enrollmentStatus?.isEnrolled ? 'Desinscribirse' : 'Inscribirse en la clase'}
						</button>
					{/if}
					{#if authStore.isAdmin || authStore.isProfesor}
						<a
							href="/clases/{clase.id}/editar"
							class="flex-1 rounded-lg bg-green-600 px-6 py-3 text-center font-bold text-white hover:bg-green-700"
						>
							Editar Clase
						</a>
						<button
							class="flex-1 rounded-lg bg-red-600 px-6 py-3 font-bold text-white hover:bg-red-700"
						>
							Eliminar Clase
						</button>
					{/if}
				</div>
				
				<!-- Enrollment Error Message -->
				{#if enrollmentError}
					<div class="mt-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
						{enrollmentError}
					</div>
				{/if}
			</div>
		</div>
	{:else}
		<!-- Not Found State -->
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📚</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Clase no encontrada</h3>
			<p class="mb-4 text-gray-500">La clase que buscas no existe o ha sido eliminada.</p>
			<button
				onclick={() => goto('/clases')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Clases
			</button>
		</div>
	{/if}
</div>
