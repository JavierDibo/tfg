<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOClaseConDetalles } from '$lib/generated/api/models/DTOClaseConDetalles';
	import type { Material } from '$lib/generated/api/models/Material';
	import type { ErrorInfo } from '$lib/utils/errorHandler';
	import { ErrorHandler } from '$lib/utils/errorHandler';
	import ClaseDetail from '$lib/components/clases/ClaseDetail.svelte';
	import ClaseMaterials from '$lib/components/clases/ClaseMaterials.svelte';
	import ClaseEnrollment from '$lib/components/clases/ClaseEnrollment.svelte';
	import ClaseStudentManagement from '$lib/components/clases/ClaseStudentManagement.svelte';
	import ClaseTeachers from '$lib/components/clases/ClaseTeachers.svelte';
	import ClaseStudents from '$lib/components/clases/ClaseStudents.svelte';
	import ClaseEditModal from '$lib/components/clases/ClaseEditModal.svelte';
	import MaterialAddModal from '$lib/components/clases/MaterialAddModal.svelte';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';

	// Get class ID from URL
	const claseId = Number($page.params.id);

	// State
	let clase = $state<DTOClaseConDetalles | null>(null);
	let loading = $state(true);
	let error = $state<ErrorInfo | null>(null);
	let showEditModal = $state(false);
	let showMaterialModal = $state(false);
	let enrollmentLoading = $state(false);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load class details
	async function loadClase() {
		try {
			loading = true;
			error = null;

			// Use the appropriate endpoint based on user role
			if (authStore.isAlumno) {
				// Students get enhanced details with enrollment status
				clase = await ClaseService.getClaseDetailsForMe(claseId);
			} else {
				// Teachers and admins get basic class details
				const basicClase = await ClaseService.getClaseById(claseId);
				// Convert to DTOClaseConDetalles format for consistency
				clase = {
					...basicClase,
					isEnrolled: false, // Teachers/admins are not enrolled as students
					fechaInscripcion: undefined
				};
			}
		} catch (err) {
			error = await ErrorHandler.parseError(err);
			console.error('Error loading class:', err);
		} finally {
			loading = false;
		}
	}

	// Handle enrollment/disenrollment
	async function handleEnrollment(enroll: boolean) {
		console.log('handleEnrollment called with:', {
			enroll,
			claseId,
			isAlumno: authStore.isAlumno,
			userSub: authStore.user?.sub
		});

		if (!authStore.isAlumno || !authStore.user?.sub) {
			error = {
				message: 'Debes estar autenticado como alumno para inscribirte',
				type: 'error'
			};
			return;
		}

		enrollmentLoading = true;
		try {
			console.log('Making API call for:', enroll ? 'enrollment' : 'unenrollment');
			if (enroll) {
				await ClaseService.enrollInClase(claseId);
			} else {
				await ClaseService.unenrollFromClase(claseId);
			}

			console.log('API call successful, reloading class data');
			// Reload class data to update enrollment status
			await loadClase();
		} catch (err) {
			console.error('Error handling enrollment:', err);
			error = await ErrorHandler.parseError(err);
		} finally {
			enrollmentLoading = false;
		}
	}

	// Handle class update
	async function handleClassUpdate() {
		try {
			await loadClase();
			showEditModal = false;
		} catch (err) {
			error = await ErrorHandler.parseError(err);
		}
	}

	// Handle material addition
	async function handleMaterialAdd(material: Material) {
		if (!clase?.id) return;

		try {
			await ClaseService.addMaterialToClase(clase.id, material);
			await loadClase(); // Reload to update materials
			showMaterialModal = false;
		} catch (err) {
			error = await ErrorHandler.parseError(err);
		}
	}

	// Handle material removal
	async function handleMaterialRemove(materialId: string) {
		if (!clase?.id) return;

		try {
			await ClaseService.removeMaterialFromClase(clase.id, materialId);
			await loadClase(); // Reload to update materials
		} catch (err) {
			error = await ErrorHandler.parseError(err);
		}
	}

	// Check if user is enrolled
	function isEnrolled(): boolean {
		const enrolled = clase?.isEnrolled || false;
		console.log('isEnrolled check:', { enrolled, claseIsEnrolled: clase?.isEnrolled, clase });
		return enrolled;
	}

	// Check if user is teacher of this class
	function isTeacher(): boolean {
		return !!(
			authStore.isProfesor &&
			authStore.user?.sub &&
			clase?.profesoresId?.includes(authStore.user.sub)
		);
	}

	// Check if user can edit (teacher or admin)
	function canEdit(): boolean {
		return isTeacher() || authStore.isAdmin;
	}

	// Check if user can view enrollment options
	function canEnroll(): boolean {
		return authStore.isAlumno && !isTeacher();
	}

	onMount(() => {
		if (isNaN(claseId)) {
			goto('/clases');
			return;
		}
		loadClase();
	});
</script>

<div class="min-h-screen bg-gray-50">
	<div class="container mx-auto px-4 py-8">
		{#if loading}
			<div class="flex h-64 items-center justify-center">
				<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
			</div>
		{:else if error}
			<ErrorDisplay {error} onRetry={loadClase} onDismiss={() => (error = null)} />
		{:else if clase}
			<!-- Header with back button -->
			<div class="mb-6">
				<button
					onclick={() => goto('/clases')}
					class="inline-flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm leading-4 font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
				>
					<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 19l-7-7 7-7"
						/>
					</svg>
					Volver a Clases
				</button>
			</div>

			<!-- Main content grid -->
			<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
				<!-- Main content area -->
				<div class="space-y-6 lg:col-span-2">
					<!-- Class details -->
					<ClaseDetail {clase} />

					<!-- Materials section -->
					<ClaseMaterials
						{clase}
						canEdit={canEdit()}
						onAddMaterial={() => (showMaterialModal = true)}
						onRemoveMaterial={handleMaterialRemove}
					/>
				</div>

				<!-- Sidebar -->
				<div class="space-y-6">
					<!-- Enrollment section (only for students) -->
					{#if canEnroll()}
						{console.log('Rendering ClaseEnrollment with:', {
							clase,
							isEnrolled: isEnrolled(),
							enrollmentLoading
						})}
						<ClaseEnrollment
							{clase}
							isEnrolled={isEnrolled()}
							onEnrollment={handleEnrollment}
							showEnrollment={true}
							loading={enrollmentLoading}
						/>
					{:else}
						{console.log('Not rendering ClaseEnrollment because canEnroll() returned false')}
					{/if}

					<!-- Teachers section -->
					<ClaseTeachers {clase} />

					<!-- Students section -->
					<ClaseStudents {clase} />

					<!-- Student management (only for teachers/admins) -->
					{#if canEdit()}
						<ClaseStudentManagement {clase} />
					{/if}

					<!-- Teacher actions (only for teachers/admins) -->
					{#if canEdit()}
						<div class="rounded-lg bg-white p-6 shadow">
							<h3 class="mb-4 text-lg font-medium text-gray-900">Acciones del Profesor</h3>
							<div class="space-y-3">
								<button
									onclick={() => (showEditModal = true)}
									class="inline-flex w-full items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
								>
									<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
										/>
									</svg>
									Editar Clase
								</button>
								<button
									onclick={() => (showMaterialModal = true)}
									class="inline-flex w-full items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
								>
									<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 6v6m0 0v6m0-6h6m-6 0H6"
										/>
									</svg>
									Agregar Material
								</button>
							</div>
						</div>
					{/if}
				</div>
			</div>

			<!-- Modals -->
			{#if showEditModal}
				<ClaseEditModal
					{clase}
					onClose={() => (showEditModal = false)}
					onSave={handleClassUpdate}
				/>
			{/if}

			{#if showMaterialModal}
				<MaterialAddModal onClose={() => (showMaterialModal = false)} onAdd={handleMaterialAdd} />
			{/if}
		{/if}
	</div>
</div>
