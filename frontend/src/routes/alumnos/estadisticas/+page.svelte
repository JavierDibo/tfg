<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { AlumnoService, type AlumnoStatistics } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { AlumnosMessages } from '$lib/components/alumnos';
	import {
		EstadisticasHeader,
		EstadisticasStatsGrid,
		EstadisticasCharts,
		EstadisticasQuickActions,
		EstadisticasLoadingState,
		EstadisticasErrorState,
		EstadisticasLastUpdated
	} from '$lib/components/alumnos';

	// State management
	let statistics: AlumnoStatistics | null = $state(null);
	let loading = $state(false);
	let error = $state<string | null>(null);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAdmin && !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(() => {
		loadStatistics();
	});

	async function loadStatistics() {
		loading = true;
		error = null;

		try {
			statistics = await AlumnoService.getStatistics();
		} catch (err) {
			error = `Error al cargar estadísticas: ${err}`;
		} finally {
			loading = false;
		}
	}
</script>

<div class="container mx-auto px-4 py-8">
	<!-- Header Component -->
	<EstadisticasHeader on:refresh={loadStatistics} />

	<!-- Messages Component -->
	<AlumnosMessages successMessage={null} {error} on:clearError={() => (error = null)} />

	<!-- Loading State -->
	{#if loading}
		<EstadisticasLoadingState />
	{:else if statistics}
		<!-- Statistics Grid Component -->
		<EstadisticasStatsGrid {statistics} />

		<!-- Charts Component -->
		<EstadisticasCharts {statistics} />

		<!-- Quick Actions Component -->
		<EstadisticasQuickActions {statistics} isAdmin={authStore.isAdmin} />

		<!-- Last Updated Component -->
		<EstadisticasLastUpdated />
	{:else if !loading}
		<!-- Error State Component -->
		<EstadisticasErrorState on:retry={loadStatistics} />
	{/if}
</div>
