<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { ProfesorService, type ProfesorStatistics } from '$lib/services/profesorService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { EntityMessages } from '$lib/components/common';
	import {
		EstadisticasHeader,
		EstadisticasStatsGrid,
		EstadisticasCharts,
		EstadisticasQuickActions,
		EstadisticasLoadingState,
		EstadisticasErrorState,
		EstadisticasLastUpdated
	} from '$lib/components/profesores';

	// State management
	let statistics: ProfesorStatistics | null = $state(null);
	let loading = $state(false);
	let error = $state<string | null>(null);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAdmin) {
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
			statistics = await ProfesorService.getStatistics();
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
	<EntityMessages successMessage={null} {error} on:clearError={() => (error = null)} />

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
