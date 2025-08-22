<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOClaseConDetalles } from '$lib/generated/api/models/DTOClaseConDetalles';
	import type { DTOAlumno } from '$lib/generated/api/models/DTOAlumno';
	import type { DTOAlumnoPublico } from '$lib/generated/api/models/DTOAlumnoPublico';

	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';

	let { clase } = $props<{
		clase: DTOClaseConDetalles;
	}>();

	// Type for student objects that can be either full DTOAlumno or public info
	type StudentInfo = DTOAlumno | DTOAlumnoPublico;
	let students = $state<StudentInfo[]>([]);

	// Type guard to check if student has full information
	function hasFullInfo(student: StudentInfo): student is DTOAlumno {
		return 'id' in student && 'email' in student && 'usuario' in student;
	}
	let loading = $state(false);
	let error = $state<string | null>(null);
	let unenrollingStudentId = $state<number | null>(null);

	// Load students when component mounts
	$effect(() => {
		if (clase?.id) {
			loadStudents();
		}
	});

	async function loadStudents() {
		if (!clase?.id) return;

		try {
			loading = true;
			error = null;

			// Get students enrolled in this class
			const response = await ClaseService.getAlumnosDeClase(clase.id, {
				page: 0,
				size: 100, // Get all students
				sortBy: 'nombre',
				sortDirection: 'ASC'
			});
			students = response.content || [];
		} catch (err) {
			console.error('Error loading students:', err);
			error = 'Error al cargar estudiantes';
		} finally {
			loading = false;
		}
	}

	function goToStudentProfile(student: StudentInfo) {
		// Only allow navigation if user is admin, teacher, or the student themselves
		// and if the student has an ID (full info)
		if ((authStore.isAdmin || authStore.isProfesor) && hasFullInfo(student) && student.id) {
			goto(`/alumnos/${student.id}`);
		}
	}

	// Check if user is teacher of this class
	function isTeacherOfThisClass(): boolean {
		return !!(
			authStore.isProfesor &&
			authStore.user?.sub &&
			clase?.profesoresId?.includes(authStore.user.sub)
		);
	}

	// Check if user can manage enrollments (teacher of this class or admin)
	function canManageEnrollments(): boolean {
		return isTeacherOfThisClass() || authStore.isAdmin;
	}

	// Check if user can see detailed student information
	function canSeeStudentDetails(): boolean {
		return authStore.isAdmin || authStore.isProfesor;
	}

	// Check if user can see student profile button
	function canSeeProfileButton(): boolean {
		return authStore.isAdmin || authStore.isProfesor;
	}

	// Handle unenrolling a student
	async function handleUnenrollStudent(student: StudentInfo) {
		if (!clase?.id || !hasFullInfo(student) || !student.id || !canManageEnrollments()) return;

		try {
			unenrollingStudentId = student.id;
			error = null;

			await ClaseService.unenrollStudentFromClass(student.id, clase.id);

			// Reload the students list
			await loadStudents();
		} catch (err) {
			console.error('Error unenrolling student:', err);
			error = 'Error al desinscribir al estudiante';
		} finally {
			unenrollingStudentId = null;
		}
	}
</script>

<div class="rounded-lg bg-white p-6 shadow-md">
	<h3 class="mb-4 text-lg font-medium text-gray-900">
		Estudiantes Inscritos ({students.length})
	</h3>

	{#if loading}
		<div class="py-4 text-center">
			<div class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-500"></div>
			<p class="mt-2 text-sm text-gray-600">Cargando estudiantes...</p>
		</div>
	{:else if error}
		<div class="rounded-md border border-red-200 bg-red-50 p-3">
			<p class="text-sm text-red-700">{error}</p>
		</div>
	{:else if students.length === 0}
		<div class="py-4 text-center">
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
			<p class="mt-2 text-sm text-gray-500">No hay estudiantes inscritos</p>
		</div>
	{:else}
		<div class="space-y-3">
			{#each students as student (hasFullInfo(student) ? student.id : `${student.nombre}-${student.apellidos}`)}
				<div class="flex items-center justify-between rounded-lg border p-3 hover:bg-gray-50">
					<div class="flex items-center space-x-3">
						<div class="flex h-10 w-10 items-center justify-center rounded-full bg-green-100">
							<span class="text-sm font-medium text-green-600">
								{student.nombre?.charAt(0)}{student.apellidos?.charAt(0)}
							</span>
						</div>
						<div>
							<h4 class="font-medium text-gray-900">
								{student.nombre}
								{student.apellidos}
							</h4>
							{#if canSeeStudentDetails() && hasFullInfo(student)}
								<p class="text-sm text-gray-600">{student.email}</p>
								<p class="text-xs text-gray-500">@{student.usuario}</p>
							{:else}
								<p class="text-sm text-gray-500">Estudiante inscrito</p>
							{/if}
						</div>
					</div>
					<div class="flex items-center space-x-2">
						{#if canSeeProfileButton() && hasFullInfo(student)}
							<button
								onclick={() => goToStudentProfile(student)}
								class="rounded-md bg-green-50 px-3 py-1 text-sm text-green-600 hover:bg-green-100 hover:text-green-900"
							>
								Ver Perfil
							</button>
						{/if}
						{#if canManageEnrollments() && hasFullInfo(student)}
							<button
								onclick={() => handleUnenrollStudent(student)}
								disabled={unenrollingStudentId === student.id}
								class="rounded bg-red-50 p-1 text-red-600 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
								title="Desinscribir estudiante"
								aria-label="Desinscribir estudiante {student.nombre} {student.apellidos}"
							>
								{#if unenrollingStudentId === student.id}
									<div class="h-4 w-4 animate-spin rounded-full border-b-2 border-red-600"></div>
								{:else}
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
										/>
									</svg>
								{/if}
							</button>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>
