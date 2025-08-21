<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import ClasesDataTable from '$lib/components/clases/ClasesDataTable.svelte';
	import ClasesSearchSection from '$lib/components/clases/ClasesSearchSection.svelte';
	import ClasesMessages from '$lib/components/clases/ClasesMessages.svelte';
	import ClasesPaginationControls from '$lib/components/clases/ClasesPaginationControls.svelte';
	import ClasesStats from '$lib/components/clases/ClasesStats.svelte';

	let { children } = $props();

	// State
	let clases = $state<DTOClase[]>([]);
	let loading = $state(true);
	let error = $state<string | null>(null);
	let searchTerm = $state('');
	let currentPage = $state(1);
	let totalPages = $state(1);
	let totalElements = $state(0);
	let pageSize = $state(10);
	let enrollmentLoading = $state<number | null>(null); // Track which class is being enrolled/unenrolled

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load classes
	async function loadClases() {
		try {
			loading = true;
			error = null;

			const response = await ClaseService.getAllClases();
			clases = response;
			totalElements = response.length;
			totalPages = Math.ceil(totalElements / pageSize);
		} catch (err) {
			console.error('Error loading classes:', err);
			error = 'Error al cargar las clases';
		} finally {
			loading = false;
		}
	}

	// Handle search
	function handleSearch(term: string) {
		searchTerm = term;
		currentPage = 1;
		loadClases();
	}

	// Handle page change
	function handlePageChange(page: number) {
		currentPage = page;
		loadClases();
	}

	// Handle enrollment/disenrollment
	async function handleEnrollment(claseId: number, enroll: boolean) {
		if (!authStore.isAlumno || !authStore.user?.sub) {
			error = 'Debes estar autenticado como alumno para inscribirte';
			return;
		}

		enrollmentLoading = claseId;
		try {
			if (enroll) {
				await ClaseService.enrollInClase(claseId);
			} else {
				await ClaseService.unenrollFromClase(claseId);
			}
			await loadClases(); // Reload to update enrollment status
		} catch (err) {
			console.error('Error handling enrollment:', err);
			error = enroll ? 'Error al inscribirse en la clase' : 'Error al desinscribirse de la clase';
		} finally {
			enrollmentLoading = null;
		}
	}

	// Check if user is enrolled in a class
	function isEnrolled(clase: DTOClase): boolean {
		return (
			authStore.isAlumno &&
			!!authStore.user?.sub &&
			(clase.alumnosId?.includes(authStore.user.sub) ?? false)
		);
	}

	// Check if user is teacher of a class
	function isTeacher(clase: DTOClase): boolean {
		return (
			authStore.isProfesor &&
			!!authStore.user?.sub &&
			(clase.profesoresId?.includes(authStore.user.sub) ?? false)
		);
	}

	onMount(() => {
		loadClases();
	});
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<h1 class="mb-2 text-3xl font-bold text-gray-900">Clases</h1>
		<p class="text-gray-600">
			{#if authStore.isAlumno}
				Explora y únete a las clases disponibles
			{:else if authStore.isProfesor}
				Gestiona tus clases y materiales
			{:else if authStore.isAdmin}
				Gestiona todas las clases del sistema
			{:else}
				Explora las clases disponibles
			{/if}
		</p>
	</div>

	<ClasesStats {clases} />

	<div class="rounded-lg bg-white p-6 shadow-md">
		<ClasesSearchSection
			{searchTerm}
			onSearch={handleSearch}
			showCreateButton={authStore.isProfesor || authStore.isAdmin}
		/>

		<ClasesMessages {error} {loading} />

		{#if !loading && !error && clases.length > 0}
			<ClasesDataTable
				{clases}
				{isEnrolled}
				{isTeacher}
				onEnrollment={handleEnrollment}
				showEnrollmentButtons={authStore.isAlumno}
				showTeacherActions={authStore.isProfesor || authStore.isAdmin}
				enrollmentLoading={enrollmentLoading}
			/>

			<ClasesPaginationControls
				{currentPage}
				{totalPages}
				{totalElements}
				{pageSize}
				onPageChange={handlePageChange}
			/>
		{/if}
	</div>
</div>
