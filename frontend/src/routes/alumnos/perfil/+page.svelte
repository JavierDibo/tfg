<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOAlumno, DTOClase } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let alumno = $state<DTOAlumno | null>(null);
	let enrolledClasses = $state<DTOClase[]>([]);
	let enrolledClassesLoading = $state(false);
	let enrolledClassesError = $state<string | null>(null);

	// Get student ID from URL or use current user's ID
	const studentId = $derived(Number($page.params.id) || authStore.user?.id || 0);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load student data
	$effect(() => {
		if (authStore.isAuthenticated && studentId) {
			loadStudentData();
			loadEnrolledClasses();
		}
	});

	async function loadStudentData() {
		loading = true;
		error = null;

		try {
			alumno = await AlumnoService.getAlumno(studentId);
		} catch (err) {
			error = `Error al cargar los datos del alumno: ${err}`;
			console.error('Error loading student data:', err);
		} finally {
			loading = false;
		}
	}

	async function loadEnrolledClasses() {
		enrolledClassesLoading = true;
		enrolledClassesError = null;

		try {
			enrolledClasses = await EnrollmentService.getMyEnrolledClasses();
		} catch (err) {
			enrolledClassesError = `Error al cargar las clases inscritas: ${err}`;
			console.error('Error loading enrolled classes:', err);
		} finally {
			enrolledClassesLoading = false;
		}
	}

	function formatDate(date: Date | undefined): string {
		if (!date) return 'N/A';
		return new Date(date).toLocaleDateString('es-ES', {
			year: 'numeric',
			month: 'short',
			day: 'numeric'
		});
	}

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
	<title>Mi Perfil - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	{#if loading}
		<div class="flex justify-center py-12">
			<div class="text-center">
				<div class="mb-4 text-6xl">📚</div>
				<p class="text-gray-600">Cargando perfil...</p>
			</div>
		</div>
	{:else if error}
		<div class="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
			<h3 class="mb-2 text-lg font-medium text-red-800">Error</h3>
			<p class="text-red-600">{error}</p>
			<button
				onclick={() => goto('/clases')}
				class="mt-4 rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Clases
			</button>
		</div>
	{:else if alumno}
		<div class="space-y-8">
			<!-- Header -->
			<div class="flex items-center justify-between">
				<div>
					<h1 class="text-3xl font-bold text-gray-900">Mi Perfil</h1>
					<p class="mt-2 text-gray-600">Gestiona tu información personal y clases inscritas</p>
				</div>
				<a
					href="/clases"
					class="rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
				>
					Explorar Clases
				</a>
			</div>

			<!-- Personal Information -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Información Personal</h2>
				<div class="grid gap-4 md:grid-cols-2">
					<div>
						<label class="text-sm font-medium text-gray-500">Nombre</label>
						<p class="text-gray-900">{alumno.firstName} {alumno.lastName}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Usuario</label>
						<p class="text-gray-900">@{alumno.username}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Email</label>
						<p class="text-gray-900">{alumno.email}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">DNI</label>
						<p class="text-gray-900">{alumno.dni}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Teléfono</label>
						<p class="text-gray-900">{alumno.phoneNumber || 'No especificado'}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Estado</label>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
								? 'bg-green-100 text-green-800'
								: 'bg-red-100 text-red-800'}"
						>
							{alumno.enabled ? 'Activo' : 'Inactivo'}
						</span>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Fecha de Matrícula</label>
						<p class="text-gray-900">{formatDate(alumno.enrollmentDate)}</p>
					</div>
					<div>
						<label class="text-sm font-medium text-gray-500">Matriculado</label>
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enrolled
								? 'bg-green-100 text-green-800'
								: 'bg-yellow-100 text-yellow-800'}"
						>
							{alumno.enrolled ? 'Sí' : 'No'}
						</span>
					</div>
				</div>
			</div>

			<!-- Enrolled Classes -->
			<div class="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
				<div class="mb-4 flex items-center justify-between">
					<h2 class="text-xl font-semibold text-gray-900">Mis Clases Inscritas</h2>
					<span class="text-sm text-gray-500">{enrolledClasses.length} clases</span>
				</div>

				{#if enrolledClassesLoading}
					<div class="flex justify-center py-8">
						<p class="text-gray-600">Cargando clases...</p>
					</div>
				{:else if enrolledClassesError}
					<div class="rounded-lg border border-red-200 bg-red-50 p-4">
						<p class="text-red-600">{enrolledClassesError}</p>
					</div>
				{:else if enrolledClasses.length === 0}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
						<div class="mb-4 text-6xl">📚</div>
						<h3 class="mb-2 text-lg font-medium text-gray-900">No tienes clases inscritas</h3>
						<p class="mb-4 text-gray-600">
							Explora nuestras clases disponibles y comienza tu aprendizaje.
						</p>
						<a
							href="/clases"
							class="inline-block rounded-lg bg-blue-600 px-6 py-3 font-bold text-white hover:bg-blue-700"
						>
							Explorar Clases
						</a>
					</div>
				{:else}
					<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
						{#each enrolledClasses as clase (clase.id)}
							<div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow">
								<div class="mb-3">
									<h3 class="font-semibold text-gray-900">{clase.titulo}</h3>
									<p class="text-sm text-gray-600 line-clamp-2">{clase.descripcion}</p>
								</div>
								
								<div class="mb-3 space-y-2">
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Precio:</span>
										<span class="font-semibold text-green-600">{formatPrice(clase.precio)}</span>
									</div>
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Nivel:</span>
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {getNivelColor(clase.nivel)}"
										>
											{getNivelText(clase.nivel)}
										</span>
									</div>
									<div class="flex items-center justify-between">
										<span class="text-sm text-gray-500">Modalidad:</span>
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {getPresencialidadColor(clase.presencialidad)}"
										>
											{getPresencialidadText(clase.presencialidad)}
										</span>
									</div>
								</div>

								<div class="flex gap-2">
									<a
										href="/clases/{clase.id}"
										class="flex-1 rounded bg-blue-600 px-3 py-2 text-center text-sm font-medium text-white hover:bg-blue-700"
									>
										Ver Clase
									</a>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>
	{:else}
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">👤</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Perfil no encontrado</h3>
			<p class="mb-4 text-gray-500">No se pudo cargar la información del perfil.</p>
			<button
				onclick={() => goto('/clases')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Clases
			</button>
		</div>
	{/if}
</div>
