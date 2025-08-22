<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import ClasesDataTable from '$lib/components/clases/ClasesDataTable.svelte';
	import MisClasesDataTable from '$lib/components/clases/MisClasesDataTable.svelte';
	import MisClasesProfesorDataTable from '$lib/components/clases/MisClasesProfesorDataTable.svelte';
	import MisAlumnos from '$lib/components/clases/MisAlumnos.svelte';
	import ClasesSearchSection from '$lib/components/clases/ClasesSearchSection.svelte';
	import ClasesMessages from '$lib/components/clases/ClasesMessages.svelte';
	import ClasesPaginationControls from '$lib/components/clases/ClasesPaginationControls.svelte';
	import ClasesStats from '$lib/components/clases/ClasesStats.svelte';

	// State
	let clases = $state<DTOClase[]>([]);
	let misClases = $state<DTOClase[]>([]);
	let loading = $state(true);
	let misClasesLoading = $state(false);
	let error = $state<string | null>(null);
	let searchTerm = $state('');
	let currentPage = $state(1);
	let totalPages = $state(1);
	let totalElements = $state(0);
	let pageSize = $state(10);
	let enrollmentLoading = $state<number | null>(null); // Track which class is being enrolled/unenrolled
	let activeTab = $state<'all' | 'my'>(authStore.isProfesor ? 'my' : 'all');
	// Track which classes student is enrolled in (for future use)
	// let enrolledClassIds = $state<Set<number>>(new Set());

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		// For teachers, default to 'my' tab and only show their classes
		if (authStore.isProfesor) {
			activeTab = 'my';
		}
	});

	// Load all classes
	async function loadClases() {
		try {
			loading = true;
			error = null;

			let response: DTOClase[];

			if (searchTerm.trim()) {
				// Use the new search API when there's a search term
				response = await ClaseService.searchClasesByTitle(searchTerm.trim());
			} else {
				// Load all classes when no search term
				response = await ClaseService.getAllClases();
			}

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

	// Load my classes (enrolled for students, teaching for teachers)
	async function loadMisClases() {
		if (!authStore.user?.sub) return;

		try {
			misClasesLoading = true;
			error = null;

			let response: DTOClase[];

			if (authStore.isAlumno) {
				response = await ClaseService.getClasesByAlumno(authStore.user.sub);
				// Update enrolled class IDs for enrollment status tracking
				// enrolledClassIds = new Set(response.map((clase) => clase.id!));
			} else if (authStore.isProfesor) {
				// Use the new mis-clases endpoint for teachers
				response = await ClaseService.getMisClases();
			} else {
				response = [];
			}

			misClases = response;
		} catch (err) {
			console.error('Error loading my classes:', err);
			error = 'Error al cargar mis clases';
		} finally {
			misClasesLoading = false;
		}
	}

	// Handle search
	function handleSearch(term: string) {
		searchTerm = term;
		currentPage = 1;
		if (activeTab === 'all') {
			loadClases();
		}
	}

	// Handle page change
	function handlePageChange(page: number) {
		currentPage = page;
		if (activeTab === 'all') {
			loadClases();
		}
	}

	// Handle tab change
	function handleTabChange(tab: 'all' | 'my') {
		activeTab = tab;
		if (tab === 'my') {
			loadMisClases();
		} else {
			loadClases();
		}
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
			// Reload both views to update enrollment status
			await loadClases();
			await loadMisClases();
		} catch (err) {
			console.error('Error handling enrollment:', err);
			error = enroll ? 'Error al inscribirse en la clase' : 'Error al desinscribirse de la clase';
		} finally {
			enrollmentLoading = null;
		}
	}

	// Check if user is enrolled in a class
	function isEnrolled(clase: DTOClase): boolean {
		// For now, use the existing logic
		// In the future, we could use the new enrollment status endpoint
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
		if (authStore.isProfesor) {
			// Teachers only see their classes
			loadMisClases();
		} else {
			// Students and admins can see all classes
			loadClases();
			if (authStore.isAlumno) {
				loadMisClases();
			}
		}
	});
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<h1 class="mb-2 text-3xl font-bold text-gray-900">Clases</h1>
		<p class="text-gray-600">
			{#if authStore.isAlumno}
				Explora y gestiona tus clases inscritas
			{:else if authStore.isProfesor}
				Gestiona tus clases asignadas y alumnos
			{:else if authStore.isAdmin}
				Gestiona todas las clases del sistema
			{:else}
				Explora las clases disponibles
			{/if}
		</p>
	</div>

	<!-- Tabs for students and teachers -->
	{#if authStore.isAlumno || authStore.isProfesor}
		<div class="mb-6">
			<div class="border-b border-gray-200">
				<nav class="-mb-px flex space-x-8">
					<button
						onclick={() => handleTabChange('my')}
						class="border-b-2 px-1 py-2 text-sm font-medium whitespace-nowrap {activeTab === 'my'
							? 'border-blue-500 text-blue-600'
							: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
					>
						{#if authStore.isAlumno}
							Mis Clases Inscritas
						{:else if authStore.isProfesor}
							Mis Clases
						{/if}
					</button>
					{#if authStore.isAlumno}
						<button
							onclick={() => handleTabChange('all')}
							class="border-b-2 px-1 py-2 text-sm font-medium whitespace-nowrap {activeTab === 'all'
								? 'border-blue-500 text-blue-600'
								: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
						>
							Todas las Clases
						</button>
					{/if}
				</nav>
			</div>
		</div>
	{/if}

	<!-- Stats based on active tab -->
	{#if activeTab === 'my'}
		<ClasesStats clases={misClases} />
	{:else}
		<ClasesStats {clases} />
	{/if}

	<div class="rounded-lg bg-white p-6 shadow-md">
		<!-- Search section only for "all" tab or admins -->
		{#if (activeTab === 'all' || authStore.isAdmin) && !authStore.isProfesor}
			<ClasesSearchSection
				{searchTerm}
				onSearch={handleSearch}
				showCreateButton={authStore.isProfesor || authStore.isAdmin}
			/>
		{/if}

		<ClasesMessages {error} loading={activeTab === 'my' ? misClasesLoading : loading} />

		{#if activeTab === 'my'}
			<!-- My Classes View -->
			{#if !misClasesLoading && !error}
				{#if misClases.length > 0}
					{#if authStore.isAlumno}
						<MisClasesDataTable
							clases={misClases}
							onEnrollment={handleEnrollment}
							{enrollmentLoading}
						/>
					{:else if authStore.isProfesor}
						<div class="space-y-6">
							<MisClasesProfesorDataTable clases={misClases} />
							<MisAlumnos clases={misClases} />
						</div>
					{/if}
				{:else}
					<div class="py-12 text-center">
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
								d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
							/>
						</svg>
						<h3 class="mt-2 text-sm font-medium text-gray-900">
							{#if authStore.isAlumno}
								No tienes clases inscritas
							{:else if authStore.isProfesor}
								No tienes clases asignadas
							{/if}
						</h3>
						<p class="mt-1 text-sm text-gray-500">
							{#if authStore.isAlumno}
								Inscríbete en algunas clases para comenzar
							{:else if authStore.isProfesor}
								Contacta al administrador para que te asigne clases
							{/if}
						</p>
					</div>
				{/if}
			{/if}
		{:else}
			<!-- All Classes View -->
			{#if !loading && !error && clases.length > 0}
				<ClasesDataTable
					{clases}
					{isEnrolled}
					{isTeacher}
					onEnrollment={handleEnrollment}
					showEnrollmentButtons={authStore.isAlumno}
					showTeacherActions={authStore.isProfesor || authStore.isAdmin}
					{enrollmentLoading}
				/>

				<ClasesPaginationControls
					{currentPage}
					{totalPages}
					{totalElements}
					{pageSize}
					onPageChange={handlePageChange}
				/>
			{/if}
		{/if}
	</div>
</div>
