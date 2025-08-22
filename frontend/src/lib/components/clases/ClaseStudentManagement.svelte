<script lang="ts">
	import { onMount } from 'svelte';
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import type { DTOAlumno } from '$lib/generated/api/models/DTOAlumno';
	import type { DTORespuestaPaginadaDTOAlumno } from '$lib/generated/api/models/DTORespuestaPaginadaDTOAlumno';
	import type { DTORespuestaEnrollment } from '$lib/generated/api/models/DTORespuestaEnrollment';
	import { ClaseService } from '$lib/services/claseService';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import AvailableStudentsModal from './AvailableStudentsModal.svelte';

	let { clase } = $props<{
		clase: DTOClase;
	}>();

	// State
	let enrolledStudents = $state<DTOAlumno[]>([]);
	let availableStudents = $state<DTOAlumno[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let success = $state<string | null>(null);
	let currentPage = $state(1);
	let totalPages = $state(1);
	let totalElements = $state(0);
	let pageSize = $state(10);

	// Available students state
	let availableStudentsPage = $state(1);
	let availableStudentsTotalPages = $state(1);
	let availableStudentsTotalElements = $state(0);
	let loadingAvailable = $state(false);

	// Modal state
	let showEnrollModal = $state(false);
	let showUnenrollModal = $state(false);
	let showAvailableStudentsModal = $state(false);
	let selectedStudent = $state<DTOAlumno | null>(null);
	let processingEnrollment = $state(false);

	// Load enrolled students for the class
	async function loadEnrolledStudents() {
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

			enrolledStudents = response.content || [];
			totalElements = response.page?.totalElements || 0;
			totalPages = response.page?.totalPages || 1;
		} catch (err) {
			console.error('Error loading enrolled students:', err);
			error = 'Error al cargar los alumnos inscritos';
		} finally {
			loading = false;
		}
	}

	// Load available students for enrollment
	async function loadAvailableStudents() {
		if (!clase?.id) return;

		try {
			loadingAvailable = true;

			const response: DTORespuestaPaginadaDTOAlumno = await AlumnoService.getAvailableStudents({
				page: availableStudentsPage - 1, // API uses 0-based pagination
				size: 20,
				sortBy: 'nombre',
				sortDirection: 'ASC'
			});

			// Filter out students already enrolled in this class
			const enrolledStudentIds = enrolledStudents.map(s => s.id);
			availableStudents = (response.content || []).filter(
				student => !enrolledStudentIds.includes(student.id)
			);
			availableStudentsTotalElements = response.page?.totalElements || 0;
			availableStudentsTotalPages = response.page?.totalPages || 1;
		} catch (err) {
			console.error('Error loading available students:', err);
			error = 'Error al cargar los alumnos disponibles';
		} finally {
			loadingAvailable = false;
		}
	}

	// Handle page change for enrolled students
	function handlePageChange(page: number) {
		currentPage = page;
		loadEnrolledStudents();
	}

	// Handle page change for available students
	function handleAvailableStudentsPageChange(page: number) {
		availableStudentsPage = page;
		loadAvailableStudents();
	}

	// Enroll a student in the class
	async function enrollStudent(student: DTOAlumno) {
		if (!clase?.id || !student?.id) return;

		try {
			processingEnrollment = true;
			error = null;
			success = null;

			const response: DTORespuestaEnrollment = await ClaseService.enrollStudentInClass(
				student.id,
				clase.id
			);

			if (response.success) {
				success = `Alumno ${response.nombreAlumno} inscrito exitosamente en ${response.tituloClase}`;
				showEnrollModal = false;
				selectedStudent = null;
				
				// Reload both lists
				await loadEnrolledStudents();
				await loadAvailableStudents();
			} else {
				error = response.message || 'Error al inscribir al alumno';
			}
		} catch (err) {
			console.error('Error enrolling student:', err);
			error = 'Error al inscribir al alumno';
		} finally {
			processingEnrollment = false;
		}
	}

	// Handle enrollment from available students modal
	async function handleEnrollFromModal(student: DTOAlumno) {
		await enrollStudent(student);
	}

	// Unenroll a student from the class
	async function unenrollStudent(student: DTOAlumno) {
		if (!clase?.id || !student?.id) return;

		try {
			processingEnrollment = true;
			error = null;
			success = null;

			const response: DTORespuestaEnrollment = await ClaseService.unenrollStudentFromClass(
				student.id,
				clase.id
			);

			if (response.success) {
				success = `Alumno ${response.nombreAlumno} desinscrito exitosamente de ${response.tituloClase}`;
				showUnenrollModal = false;
				selectedStudent = null;
				
				// Reload both lists
				await loadEnrolledStudents();
				await loadAvailableStudents();
			} else {
				error = response.message || 'Error al desinscribir al alumno';
			}
		} catch (err) {
			console.error('Error unenrolling student:', err);
			error = 'Error al desinscribir al alumno';
		} finally {
			processingEnrollment = false;
		}
	}

	// Open enroll modal
	function openEnrollModal(student: DTOAlumno) {
		selectedStudent = student;
		showEnrollModal = true;
	}

	// Open unenroll modal
	function openUnenrollModal(student: DTOAlumno) {
		selectedStudent = student;
		showUnenrollModal = true;
	}

	// Close modals
	function closeModals() {
		showEnrollModal = false;
		showUnenrollModal = false;
		showAvailableStudentsModal = false;
		selectedStudent = null;
		error = null;
		success = null;
	}

	// Open available students modal
	function openAvailableStudentsModal() {
		showAvailableStudentsModal = true;
	}

	// Check if user can manage enrollments
	const canManageEnrollments = $derived(
		authStore.isAdmin || (authStore.isProfesor && clase?.profesores?.some((p: any) => p.id === authStore.user?.id))
	);

	// Reload when class changes
	$effect(() => {
		if (clase?.id) {
			loadEnrolledStudents();
			loadAvailableStudents();
		}
	});

	onMount(() => {
		if (clase?.id) {
			loadEnrolledStudents();
			loadAvailableStudents();
		}
	});
</script>

<div class="space-y-6">
	<!-- Success/Error Messages -->
	{#if success}
		<div class="rounded-md border border-green-200 bg-green-50 p-4">
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
					<p class="text-sm font-medium text-green-800">{success}</p>
				</div>
			</div>
		</div>
	{/if}

	{#if error}
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
					<p class="text-sm font-medium text-red-800">{error}</p>
				</div>
			</div>
		</div>
	{/if}

	<!-- Enrolled Students Section -->
	<div class="rounded-lg bg-white p-6 shadow">
		<div class="mb-4 flex items-center justify-between">
			<h3 class="text-lg font-medium text-gray-900">Alumnos Inscritos</h3>
			{#if canManageEnrollments}
				<button
					onclick={openAvailableStudentsModal}
					class="inline-flex items-center rounded-md border border-transparent bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
				>
					<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 6v6m0 0v6m0-6h6m-6 0H6"
						/>
					</svg>
					Gestionar Inscripciones
				</button>
			{/if}
		</div>

		{#if loading}
			<div class="flex h-32 items-center justify-center">
				<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else if enrolledStudents.length > 0}
			<div class="space-y-3">
				{#each enrolledStudents as alumno}
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
									{alumno.nombre} {alumno.apellidos}
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
							{#if canManageEnrollments}
								<button
									onclick={() => openUnenrollModal(alumno)}
									class="rounded bg-red-50 p-1 text-red-600 hover:bg-red-100"
									title="Desinscribir alumno"
								>
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
										/>
									</svg>
								</button>
							{/if}
						</div>
					</div>
				{/each}
			</div>

			<!-- Pagination -->
			{#if totalPages > 1}
				<div class="mt-4 border-t border-gray-200 pt-4">
					<div class="flex items-center justify-between">
						<div class="text-sm text-gray-700">
							Mostrando {((currentPage - 1) * pageSize) + 1} a {Math.min(currentPage * pageSize, totalElements)} de {totalElements} alumnos
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

	<!-- Available Students Modal -->
	{#if showEnrollModal && selectedStudent}
		<div class="fixed inset-0 z-50 overflow-y-auto">
			<div class="flex min-h-screen items-end justify-center px-4 pt-4 pb-20 text-center sm:block sm:p-0">
				<div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

				<div class="inline-block transform overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:align-middle">
					<div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
						<div class="sm:flex sm:items-start">
							<div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
								<svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M12 6v6m0 0v6m0-6h6m-6 0H6"
									/>
								</svg>
							</div>
							<div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
								<h3 class="text-lg font-medium leading-6 text-gray-900">Inscribir Alumno</h3>
								<div class="mt-2">
									<p class="text-sm text-gray-500">
										¿Estás seguro de que quieres inscribir a <strong>{selectedStudent.nombre} {selectedStudent.apellidos}</strong> en la clase <strong>{clase.titulo}</strong>?
									</p>
								</div>
							</div>
						</div>
					</div>
					<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
						<button
							type="button"
							onclick={() => selectedStudent && enrollStudent(selectedStudent)}
							disabled={processingEnrollment}
							class="inline-flex w-full justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
						>
							{#if processingEnrollment}
								<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
									<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
									<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
								</svg>
								Procesando...
							{:else}
								Inscribir
							{/if}
						</button>
						<button
							type="button"
							onclick={closeModals}
							disabled={processingEnrollment}
							class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
						>
							Cancelar
						</button>
					</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- Unenroll Student Modal -->
	{#if showUnenrollModal && selectedStudent}
		<div class="fixed inset-0 z-50 overflow-y-auto">
			<div class="flex min-h-screen items-end justify-center px-4 pt-4 pb-20 text-center sm:block sm:p-0">
				<div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

				<div class="inline-block transform overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:align-middle">
					<div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
						<div class="sm:flex sm:items-start">
							<div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
								<svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
									/>
								</svg>
							</div>
							<div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
								<h3 class="text-lg font-medium leading-6 text-gray-900">Desinscribir Alumno</h3>
								<div class="mt-2">
									<p class="text-sm text-gray-500">
										¿Estás seguro de que quieres desinscribir a <strong>{selectedStudent.nombre} {selectedStudent.apellidos}</strong> de la clase <strong>{clase.titulo}</strong>?
									</p>
								</div>
							</div>
						</div>
					</div>
					<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
						<button
							type="button"
							onclick={() => selectedStudent && unenrollStudent(selectedStudent)}
							disabled={processingEnrollment}
							class="inline-flex w-full justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
						>
							{#if processingEnrollment}
								<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
									<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
									<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
								</svg>
								Procesando...
							{:else}
								Desinscribir
							{/if}
						</button>
						<button
							type="button"
							onclick={closeModals}
							disabled={processingEnrollment}
							class="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
						>
							Cancelar
						</button>
					</div>
				</div>
			</div>
		</div>
	{/if}

	<!-- Available Students Modal -->
	{#if showAvailableStudentsModal}
		<AvailableStudentsModal
			claseId={clase.id}
			enrolledStudentIds={enrolledStudents.map(s => s.id).filter((id): id is number => id !== undefined)}
			onEnrollStudent={handleEnrollFromModal}
			onClose={() => showAvailableStudentsModal = false}
		/>
	{/if}
</div>
