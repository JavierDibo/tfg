<script lang="ts">
	import type { AlumnoStatistics } from '$lib/services/alumnoService';

	interface Props {
		statistics: AlumnoStatistics | null;
	}

	const { statistics }: Props = $props();

	// Computed properties  
	const enrollmentPercentage = $derived(() => {
		if (!statistics || statistics.total === 0) return 0;
		return Math.round((statistics.matriculados / statistics.total) * 100);
	});

	const nonEnrollmentPercentage = $derived(() => {
		if (!statistics || statistics.total === 0) return 0;
		return Math.round((statistics.noMatriculados / statistics.total) * 100);
	});

	// Helper to display statistics values properly with Svelte 5
	const displayStats = $derived(() => statistics);
</script>

<!-- Statistics Grid -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
	<!-- Total Students -->
	<div class="bg-white rounded-lg shadow-md p-6">
		<div class="flex items-center">
			<div class="flex-shrink-0">
				<div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
					</svg>
				</div>
			</div>
			<div class="ml-4">
				<p class="text-sm font-medium text-gray-500">Total de Alumnos</p>
				<p class="text-2xl font-bold text-gray-900">{displayStats()?.total || 0}</p>
			</div>
		</div>
	</div>

	<!-- Enrolled Students -->
	<div class="bg-white rounded-lg shadow-md p-6">
		<div class="flex items-center">
			<div class="flex-shrink-0">
				<div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
					</svg>
				</div>
			</div>
			<div class="ml-4">
				<p class="text-sm font-medium text-gray-500">Alumnos Matriculados</p>
				<p class="text-2xl font-bold text-gray-900">{displayStats()?.matriculados || 0}</p>
				<p class="text-sm text-green-600">{enrollmentPercentage()}% del total</p>
			</div>
		</div>
	</div>

	<!-- Non-enrolled Students -->
	<div class="bg-white rounded-lg shadow-md p-6">
		<div class="flex items-center">
			<div class="flex-shrink-0">
				<div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z" />
					</svg>
				</div>
			</div>
			<div class="ml-4">
				<p class="text-sm font-medium text-gray-500">No Matriculados</p>
				<p class="text-2xl font-bold text-gray-900">{displayStats()?.noMatriculados || 0}</p>
				<p class="text-sm text-yellow-600">{nonEnrollmentPercentage()}% del total</p>
			</div>
		</div>
	</div>
</div>
