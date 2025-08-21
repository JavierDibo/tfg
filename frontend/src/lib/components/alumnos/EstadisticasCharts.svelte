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

<!-- Visual Charts -->
<div class="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
	<!-- Enrollment Status Chart -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Estado de Matrícula</h3>

		{#if displayStats()?.total && displayStats()!.total > 0}
			<!-- Simple Bar Chart -->
			<div class="space-y-4">
				<div>
					<div class="mb-1 flex items-center justify-between">
						<span class="text-sm font-medium text-green-700">Matriculados</span>
						<span class="text-sm text-gray-500"
							>{displayStats()?.matriculados || 0} ({enrollmentPercentage()}%)</span
						>
					</div>
					<div class="h-3 w-full rounded-full bg-gray-200">
						<div
							class="h-3 rounded-full bg-green-500 transition-all duration-1000 ease-out"
							style="width: {enrollmentPercentage()}%"
						></div>
					</div>
				</div>

				<div>
					<div class="mb-1 flex items-center justify-between">
						<span class="text-sm font-medium text-yellow-700">No Matriculados</span>
						<span class="text-sm text-gray-500"
							>{displayStats()?.noMatriculados || 0} ({nonEnrollmentPercentage()}%)</span
						>
					</div>
					<div class="h-3 w-full rounded-full bg-gray-200">
						<div
							class="h-3 rounded-full bg-yellow-500 transition-all duration-1000 ease-out"
							style="width: {nonEnrollmentPercentage()}%"
						></div>
					</div>
				</div>
			</div>
		{:else}
			<p class="py-8 text-center text-gray-500">No hay datos para mostrar</p>
		{/if}
	</div>

	<!-- Summary Information -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Resumen Ejecutivo</h3>

		<div class="space-y-4">
			<div class="border-l-4 border-blue-500 pl-4">
				<h4 class="font-medium text-gray-900">Total de Estudiantes</h4>
				<p class="text-sm text-gray-600">
					Actualmente hay <strong>{displayStats()?.total || 0}</strong> estudiantes registrados en el
					sistema.
				</p>
			</div>

			<div class="border-l-4 border-green-500 pl-4">
				<h4 class="font-medium text-gray-900">Estudiantes Activos</h4>
				<p class="text-sm text-gray-600">
					<strong>{displayStats()?.matriculados || 0}</strong> estudiantes están matriculados y pueden
					acceder a los cursos.
				</p>
			</div>

			<div class="border-l-4 border-yellow-500 pl-4">
				<h4 class="font-medium text-gray-900">Estudiantes Pendientes</h4>
				<p class="text-sm text-gray-600">
					<strong>{displayStats()?.noMatriculados || 0}</strong> estudiantes están registrados pero aún
					no matriculados.
				</p>
			</div>

			{#if displayStats()?.noMatriculados && displayStats()!.noMatriculados > 0}
				<div class="rounded-md border border-yellow-200 bg-yellow-50 p-3">
					<p class="text-sm text-yellow-800">
						<strong>Acción recomendada:</strong> Considera revisar los estudiantes no matriculados para
						determinar si necesitan ser matriculados o dados de baja.
					</p>
				</div>
			{:else}
				<div class="rounded-md border border-green-200 bg-green-50 p-3">
					<p class="text-sm text-green-800">
						<strong>¡Excelente!</strong> Todos los estudiantes registrados están matriculados.
					</p>
				</div>
			{/if}
		</div>
	</div>
</div>
