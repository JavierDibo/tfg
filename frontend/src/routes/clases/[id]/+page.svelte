<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import type { Material } from '$lib/generated/api/models/Material';
	import ClaseDetail from '$lib/components/clases/ClaseDetail.svelte';
	import ClaseMaterials from '$lib/components/clases/ClaseMaterials.svelte';
	import ClaseEnrollment from '$lib/components/clases/ClaseEnrollment.svelte';
	import ClaseStudents from '$lib/components/clases/ClaseStudents.svelte';
	import ClaseEditModal from '$lib/components/clases/ClaseEditModal.svelte';
	import MaterialAddModal from '$lib/components/clases/MaterialAddModal.svelte';

	let { children } = $props();

	// Get class ID from URL
	const claseId = Number($page.params.id);

	// State
	let clase = $state<DTOClase | null>(null);
	let loading = $state(true);
	let error = $state<string | null>(null);
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

			const response = await ClaseService.getClaseById(claseId);
			clase = response;
		} catch (err) {
			console.error('Error loading class:', err);
			error = 'Error al cargar la clase';
		} finally {
			loading = false;
		}
	}

	// Handle enrollment/disenrollment
	async function handleEnrollment(enroll: boolean) {
		if (!authStore.isAlumno || !authStore.user?.sub) {
			error = 'Debes estar autenticado como alumno para inscribirte';
			return;
		}

		enrollmentLoading = true;
		try {
			if (enroll) {
				await ClaseService.enrollInClase(claseId);
			} else {
				await ClaseService.unenrollFromClase(claseId);
			}
			await loadClase(); // Reload to update enrollment status
		} catch (err) {
			console.error('Error handling enrollment:', err);
			error = enroll ? 'Error al inscribirse en la clase' : 'Error al desinscribirse de la clase';
		} finally {
			enrollmentLoading = false;
		}
	}

	// Handle class update
	async function handleClassUpdate(updatedClase: DTOClase) {
		try {
			// Note: The API doesn't seem to have an update method, so we'll reload
			await loadClase();
			showEditModal = false;
		} catch (err) {
			console.error('Error updating class:', err);
			error = 'Error al actualizar la clase';
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
			console.error('Error adding material:', err);
			error = 'Error al agregar el material';
		}
	}

	// Handle material removal
	async function handleMaterialRemove(materialId: string) {
		if (!clase?.id) return;

		try {
			await ClaseService.removeMaterialFromClase(clase.id, materialId);
			await loadClase(); // Reload to update materials
		} catch (err) {
			console.error('Error removing material:', err);
			error = 'Error al eliminar el material';
		}
	}

	// Check if user is enrolled
	function isEnrolled(): boolean {
		return !!(
			authStore.isAlumno &&
			authStore.user?.sub &&
			clase?.alumnosId?.includes(authStore.user.sub)
		);
	}

	// Check if user is teacher
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

	onMount(() => {
		if (isNaN(claseId)) {
			goto('/clases');
			return;
		}
		loadClase();
	});
</script>

<div class="container mx-auto px-4 py-8">
	{#if loading}
		<div class="flex h-64 items-center justify-center">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
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
	{:else if clase}
		<div class="mb-6">
			<button
				onclick={() => goto('/clases')}
				class="inline-flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm leading-4 font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
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

		<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
			<!-- Main content -->
			<div class="space-y-6 lg:col-span-2">
				<ClaseDetail {clase} />

				<ClaseMaterials
					{clase}
					canEdit={canEdit()}
					onAddMaterial={() => (showMaterialModal = true)}
					onRemoveMaterial={handleMaterialRemove}
				/>
			</div>

			<!-- Sidebar -->
			<div class="space-y-6">
				<ClaseEnrollment
					{clase}
					isEnrolled={isEnrolled()}
					onEnrollment={handleEnrollment}
					showEnrollment={authStore.isAlumno}
					loading={enrollmentLoading}
				/>

				{#if canEdit()}
					<ClaseStudents {clase} />
				{/if}

				{#if canEdit()}
					<div class="rounded-lg bg-white p-6 shadow">
						<h3 class="mb-4 text-lg font-medium text-gray-900">Acciones del Profesor</h3>
						<div class="space-y-3">
							<button
								onclick={() => (showEditModal = true)}
								class="inline-flex w-full items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
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
								class="inline-flex w-full items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
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
			<ClaseEditModal {clase} onClose={() => (showEditModal = false)} onSave={handleClassUpdate} />
		{/if}

		{#if showMaterialModal}
			<MaterialAddModal onClose={() => (showMaterialModal = false)} onAdd={handleMaterialAdd} />
		{/if}
	{/if}
</div>
