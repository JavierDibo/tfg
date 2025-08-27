<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type {
		DTOClase,
		DTOAlumno,
		DTORespuestaAlumnosClase,
		DTOProfesor,
		DTOEjercicio
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let clase = $state<DTOClase | null>(null);
	let claseDetalles = $state<DTOClase | null>(null);
	let numeroAlumnos = $state(0);
	let numeroProfesores = $state(0);
	let enrollmentStatus = $state<{
		isEnrolled: boolean;
		claseId?: number;
		alumnoId?: number;
	} | null>(null);
	let enrollmentLoading = $state(false);
	let enrollmentError = $state<string | null>(null);

	// Students management state
	let students = $state<DTOAlumno[]>([]);
	let studentsLoading = $state(false);
	let studentsError = $state<string | null>(null);
	let unenrollLoading = $state<number | null>(null);

	// Professor profile state
	let profesores = $state<DTOProfesor[]>([]);
	let profesorLoading = $state(false);
	let profesorError = $state<string | null>(null);

	// Exercises state
	let ejercicios = $state<DTOEjercicio[]>([]);
	let ejerciciosLoading = $state(false);
	let ejerciciosError = $state<string | null>(null);

	// Get class ID from URL
	const claseId = $derived(Number($page.params.id));

	// Check if current user is the teacher of this class
	let isClassTeacher = $state(false);

	$effect(() => {
		if ((!clase && !claseDetalles) || !authStore.user?.id) {
			isClassTeacher = false;
		} else {
			const currentClase = clase || claseDetalles;
			isClassTeacher = currentClase?.profesoresId?.includes(authStore.user.id.toString()) || false;
		}
	});

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
			if (authStore.isProfesor || authStore.isAdmin) {
				loadStudents();
			}
		}
	});

	// Load professor data when class is loaded (only for teachers/admins)
	$effect(() => {
		if (clase?.id && (authStore.isProfesor || authStore.isAdmin)) {
			loadProfesores();
		}
	});

	// Load exercises when class is loaded
	$effect(() => {
		if (currentClase?.id) {
			loadEjercicios();
		}
	});

	async function loadClase() {
		loading = true;
		error = null;

		try {
			if (authStore.isAlumno) {
				// For students, use the student-specific API that includes professor information
				claseDetalles = await EnrollmentService.getMyClassDetails(claseId);

				// Set statistics from the detailed response
				if (claseDetalles?.id) {
					numeroAlumnos = claseDetalles.numeroAlumnos || 0;
					numeroProfesores = claseDetalles.numeroProfesores || 0;
				}
			} else {
				// For teachers and admins, use the general class API
				clase = await ClaseService.getClaseById(claseId);

				// Load additional statistics - these methods no longer exist in the new API
				// The statistics are now included in the class details response
				if (clase?.id) {
					numeroAlumnos = clase.numeroAlumnos || 0;
					numeroProfesores = clase.numeroProfesores || 0;
				}
			}
		} catch (err) {
			error = `Error al cargar la clase: ${err}`;
			console.error('Error loading class:', err);
		} finally {
			loading = false;
		}
	}

	async function loadProfesores() {
		if (!claseId) return;

		profesorLoading = true;
		profesorError = null;

		try {
			// Use the new API endpoint to get professors for this class
			profesores = await ClaseService.getProfesoresPorClase(claseId);
		} catch (err) {
			profesorError = `Error al cargar la información de los profesores: ${err}`;
			console.error('Error loading professors:', err);
		} finally {
			profesorLoading = false;
		}
	}

	async function checkEnrollmentStatus() {
		if (!claseId) return;

		try {
			const status = await EnrollmentService.checkMyEnrollmentStatus(claseId);
			enrollmentStatus = {
				isEnrolled: status.isEnrolled || false,
				claseId: status.claseId,
				alumnoId: status.alumnoId
			};
		} catch (err) {
			console.warn('Error checking enrollment status:', err);
			// Don't show error to user, just assume not enrolled
			enrollmentStatus = { isEnrolled: false };
		}
	}

	async function loadStudents() {
		if (!claseId) return;

		studentsLoading = true;
		studentsError = null;

		try {
			const response: DTORespuestaAlumnosClase =
				await EnrollmentService.getStudentsInClass(claseId);
			students = response.content || [];
		} catch (err) {
			studentsError = `Error al cargar los estudiantes: ${err}`;
			console.error('Error loading students:', err);
		} finally {
			studentsLoading = false;
		}
	}

	async function loadEjercicios() {
		if (!claseId) return;

		ejerciciosLoading = true;
		ejerciciosError = null;

		try {
			const response = await EjercicioService.getEjercicios({ classId: claseId.toString() });
			ejercicios = response.content || [];
		} catch (err) {
			ejerciciosError = `Error al cargar los ejercicios: ${err}`;
			console.error('Error loading exercises:', err);
		} finally {
			ejerciciosLoading = false;
		}
	}

	async function handleUnenrollStudent(studentId: number) {
		if (!claseId) return;

		unenrollLoading = studentId;

		try {
			await EnrollmentService.unenrollStudentFromClass(studentId, claseId);

			// Remove student from local state
			students = students.filter((student) => student.id !== studentId);

			// Update student count
			if (clase) {
				numeroAlumnos = Math.max(0, numeroAlumnos - 1);
			}
		} catch (err) {
			studentsError = `Error al dar de baja al estudiante: ${err}`;
			console.error('Error unenrolling student:', err);
		} finally {
			unenrollLoading = null;
		}
	}

	async function handleEnrollment() {
		if (!claseId) return;

		enrollmentLoading = true;
		enrollmentError = null;

		try {
			const currentClase = clase || claseDetalles;
			if (!currentClase) {
				throw new Error('No se pudo obtener la información de la clase');
			}

			const result = await EnrollmentService.handleEnrollmentAction(claseId, currentClase);

			if (result.action === 'redirect') {
				goto(result.redirectUrl!);
				return;
			}

			// Refresh student count - use the class details from the response
			if (claseDetalles?.id) {
				// Reload the class to get updated statistics
				const updatedClaseDetalles = await EnrollmentService.getMyClassDetails(claseDetalles.id);
				numeroAlumnos = updatedClaseDetalles.alumnosCount || 0;
			} else if (clase?.id) {
				// Reload the class to get updated statistics
				const updatedClase = await ClaseService.getClaseById(clase.id);
				numeroAlumnos = updatedClase.numeroAlumnos || 0;
			}
		} catch (err) {
			enrollmentError = `Error al inscribirse: ${err}`;
		} finally {
			enrollmentLoading = false;
		}
	}

	// Get the current class data (either from clase or claseDetalles)
	let currentClase = $derived(clase || claseDetalles);
</script>

<svelte:head>
	<title>{currentClase?.titulo || 'Clase'} - Academia</title>
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
	{:else if currentClase}
		<!-- Class Details -->
		<div class="overflow-hidden rounded-lg bg-white shadow-lg">
			<!-- Header with Image -->
			<div class="relative h-64 bg-gradient-to-r from-blue-500 to-purple-600">
				{#if currentClase.imagenPortada}
					<img
						src={currentClase.imagenPortada}
						alt={currentClase.titulo}
						class="h-full w-full object-cover"
					/>
				{/if}
				<div class="bg-opacity-40 absolute inset-0 bg-black"></div>
				<div class="absolute right-0 bottom-0 left-0 p-6 text-white">
					<h1 class="mb-2 text-4xl font-bold">{currentClase.titulo || 'Sin título'}</h1>
					<div class="flex items-center space-x-4">
						<span class="text-2xl font-bold">{FormatterUtils.formatPrice(currentClase.precio)}</span
						>
						<span
							class="rounded-full px-3 py-1 text-sm font-medium {FormatterUtils.getNivelColor(
								currentClase.nivel
							)}"
						>
							{FormatterUtils.formatNivel(currentClase.nivel)}
						</span>
						<span
							class="rounded-full px-3 py-1 text-sm font-medium {FormatterUtils.getPresencialidadColor(
								currentClase.presencialidad
							)}"
						>
							{FormatterUtils.getPresencialidadText(currentClase.presencialidad)}
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
						{currentClase.descripcion || 'No hay descripción disponible para esta clase.'}
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
						<div class="text-2xl font-bold text-purple-600">
							{'numeroMateriales' in currentClase
								? currentClase.numeroMateriales
								: currentClase.material?.length || 0}
						</div>
						<div class="text-sm text-gray-600">Materiales</div>
					</div>
					<div class="rounded-lg bg-orange-50 p-4 text-center">
						<div class="text-2xl font-bold text-orange-600">
							{'numeroEjercicios' in currentClase
								? currentClase.numeroEjercicios
								: currentClase.ejerciciosId?.length || 0}
						</div>
						<div class="text-sm text-gray-600">Ejercicios</div>
					</div>
				</div>

				<!-- Professor Profile Section -->
				{#if (authStore.isProfesor || authStore.isAdmin) && (profesores.length > 0 || profesorLoading)}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">
							{profesores.length === 1 ? 'Profesor de la Clase' : 'Profesores de la Clase'}
						</h2>

						{#if profesorLoading}
							<div class="flex items-center justify-center py-8">
								<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
							</div>
						{:else if profesorError}
							<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
								{profesorError}
							</div>
						{:else if profesores.length > 0}
							<!-- Teacher/Admin view: Show professors from separate API call -->
							<div class="space-y-4">
								{#each profesores as profesor (profesor.id)}
									<div class="overflow-hidden rounded-lg border border-gray-200 bg-white">
										<div class="p-4">
											<div class="flex items-center justify-between">
												<div class="flex items-center space-x-3">
													<!-- Professor Avatar -->
													<div class="flex-shrink-0">
														<div
															class="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-purple-600"
														>
															<span class="text-lg font-bold text-white">
																{profesor.firstName?.[0]}{profesor.lastName?.[0]}
															</span>
														</div>
													</div>

													<!-- Professor Information -->
													<div class="min-w-0 flex-1">
														<h3 class="text-base font-semibold text-gray-900">
															{profesor.firstName}
															{profesor.lastName}
														</h3>
														<p class="text-sm text-gray-600">
															<a
																href="mailto:{profesor.email}"
																class="text-blue-600 hover:text-blue-800"
															>
																{profesor.email}
															</a>
														</p>
													</div>
												</div>

												<!-- View Profile Button -->
												{#if authStore.isAdmin || authStore.isProfesor}
													<a
														href="/profesores/{profesor.id}"
														class="text-sm font-medium text-blue-600 hover:text-blue-800"
													>
														Ver perfil completo →
													</a>
												{/if}
											</div>
										</div>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				{/if}

				<!-- Materials Section -->
				{#if currentClase.material && currentClase.material.length > 0}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">Materiales</h2>
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#each currentClase.material as material (material.id)}
								<div class="rounded-lg bg-gray-50 p-4">
									<h3 class="mb-2 font-semibold text-gray-900">
										{material.name || 'Sin nombre'}
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

				<!-- Exercises Section -->
				<div class="mb-8">
					<div class="mb-4 flex items-center justify-between">
						<h2 class="text-2xl font-semibold text-gray-900">Ejercicios</h2>
						{#if authStore.isProfesor || authStore.isAdmin}
							<a
								href="/ejercicios?classId={currentClase.id}"
								class="text-sm font-medium text-blue-600 hover:text-blue-800"
							>
								Ver todos los ejercicios →
							</a>
						{/if}
					</div>

					{#if ejerciciosLoading}
						<div class="flex items-center justify-center py-8">
							<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
						</div>
					{:else if ejerciciosError}
						<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
							{ejerciciosError}
						</div>
					{:else if ejercicios.length === 0}
						<div class="rounded-lg bg-gray-50 p-6">
							<div class="text-center">
								<div class="mb-2 text-4xl text-gray-400">📝</div>
								<h3 class="mb-2 text-lg font-medium text-gray-900">Ejercicios de la clase</h3>
								<p class="mb-4 text-gray-500">
									Esta clase tiene {'numeroEjercicios' in currentClase
										? currentClase.numeroEjercicios
										: 0} ejercicios asignados.
								</p>
								<a
									href="/ejercicios?classId={currentClase.id}"
									class="inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
								>
									Ver ejercicios
								</a>
							</div>
						</div>
					{:else}
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
							{#each ejercicios as ejercicio (ejercicio.id)}
								<div
									class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
								>
									<div class="mb-3 flex items-start justify-between">
										<h3 class="line-clamp-2 font-semibold text-gray-900">
											{ejercicio.name || 'Sin nombre'}
										</h3>
										<span
											class="ml-2 flex-shrink-0 rounded-full px-2 py-1 text-xs font-medium {FormatterUtils.getExerciseStatusColor(
												ejercicio.estado
											)}"
										>
											{ejercicio.estado || 'N/A'}
										</span>
									</div>

									{#if ejercicio.statement}
										<p class="mb-3 line-clamp-3 text-sm text-gray-600">
											{ejercicio.statement}
										</p>
									{/if}

									<div class="space-y-2 text-xs text-gray-500">
										{#if ejercicio.startDate}
											<div class="flex items-center">
												<svg
													class="mr-1 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
													/>
												</svg>
												Inicio: {FormatterUtils.formatDate(ejercicio.startDate)}
											</div>
										{/if}
										{#if ejercicio.endDate}
											<div class="flex items-center">
												<svg
													class="mr-1 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
													/>
												</svg>
												Fin: {FormatterUtils.formatDate(ejercicio.endDate)}
											</div>
										{/if}
										{#if ejercicio.numeroEntregas !== undefined}
											<div class="flex items-center">
												<svg
													class="mr-1 h-3 w-3"
													fill="none"
													stroke="currentColor"
													viewBox="0 0 24 24"
												>
													<path
														stroke-linecap="round"
														stroke-linejoin="round"
														stroke-width="2"
														d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
													/>
												</svg>
												Entregas: {ejercicio.numeroEntregas}
											</div>
										{/if}
									</div>

									<div class="mt-4 flex justify-end">
										<a
											href="/ejercicios/{ejercicio.id}"
											class="text-sm font-medium text-blue-600 hover:text-blue-800"
										>
											Ver ejercicio →
										</a>
									</div>
								</div>
							{/each}
						</div>
					{/if}
				</div>

				<!-- Class Type Information -->
				{#if currentClase.tipoClase}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">Tipo de Clase</h2>
						<div class="rounded-lg bg-gray-50 p-4">
							<span
								class="rounded-full bg-orange-100 px-3 py-1 text-sm font-medium text-orange-800"
							>
								{currentClase.tipoClase}
							</span>
						</div>
					</div>
				{/if}

				<!-- Students Section (for teachers and admins) -->
				{#if (authStore.isProfesor && isClassTeacher) || authStore.isAdmin}
					<div class="mb-8">
						<h2 class="mb-4 text-2xl font-semibold text-gray-900">Estudiantes Inscritos</h2>

						{#if studentsLoading}
							<div class="flex items-center justify-center py-8">
								<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
							</div>
						{:else if studentsError}
							<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
								{studentsError}
							</div>
						{:else if students.length === 0}
							<div class="rounded-lg bg-gray-50 p-8 text-center">
								<div class="mb-2 text-4xl">👥</div>
								<h3 class="mb-2 text-lg font-medium text-gray-900">No hay estudiantes inscritos</h3>
								<p class="text-gray-500">Aún no se ha inscrito ningún estudiante en esta clase.</p>
							</div>
						{:else}
							<div class="overflow-hidden rounded-lg border border-gray-200">
								<table class="min-w-full divide-y divide-gray-200">
									<thead class="bg-gray-50">
										<tr>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Estudiante
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Email
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												DNI
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Teléfono
											</th>
											<th
												class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Estado
											</th>
											<th
												class="px-6 py-3 text-right text-xs font-medium tracking-wider text-gray-500 uppercase"
											>
												Acciones
											</th>
										</tr>
									</thead>
									<tbody class="divide-y divide-gray-200 bg-white">
										{#each students as student (student.id)}
											<tr class="hover:bg-gray-50">
												<td class="px-6 py-4 whitespace-nowrap">
													<div class="flex items-center">
														<div class="h-10 w-10 flex-shrink-0">
															<div
																class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100"
															>
																<span class="text-sm font-medium text-blue-600">
																	{student.firstName?.[0]}{student.lastName?.[0]}
																</span>
															</div>
														</div>
														<div class="ml-4">
															<div class="text-sm font-medium text-gray-900">
																{student.firstName}
																{student.lastName}
															</div>
															<div class="text-sm text-gray-500">
																@{student.username}
															</div>
														</div>
													</div>
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.email}
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.dni}
												</td>
												<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
													{student.phoneNumber || 'N/A'}
												</td>
												<td class="px-6 py-4 whitespace-nowrap">
													<span
														class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {student.enabled
															? 'bg-green-100 text-green-800'
															: 'bg-red-100 text-red-800'}"
													>
														{student.enabled ? 'Activo' : 'Inactivo'}
													</span>
												</td>
												<td class="px-6 py-4 text-right text-sm font-medium whitespace-nowrap">
													{#if authStore.isAdmin}
														<button
															onclick={() => handleUnenrollStudent(student.id)}
															disabled={unenrollLoading === student.id}
															class="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700 disabled:opacity-50"
														>
															{unenrollLoading === student.id ? 'Cargando...' : 'Dar de baja'}
														</button>
													{/if}
												</td>
											</tr>
										{/each}
									</tbody>
								</table>
							</div>
						{/if}
					</div>
				{/if}

				<!-- Action Buttons -->
				<div class="flex flex-col gap-4 border-t border-gray-200 pt-6 sm:flex-row">
					{#if authStore.isAlumno}
						<button
							onclick={handleEnrollment}
							class="flex-1 rounded-lg bg-blue-600 px-6 py-3 font-bold text-white hover:bg-blue-700 disabled:opacity-50"
							disabled={enrollmentLoading || enrollmentStatus?.isEnrolled}
						>
							{enrollmentLoading
								? 'Cargando...'
								: enrollmentStatus?.isEnrolled
									? 'Ya inscrito'
									: 'Inscribirse en la clase'}
						</button>
					{/if}
					{#if authStore.isAdmin || authStore.isProfesor}
						<a
							href="/clases/{currentClase.id}/editar"
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
