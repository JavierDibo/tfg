<script lang="ts">
	import type { ProfesorStatistics } from '$lib/services/profesorService';

	export let statistics: ProfesorStatistics;
</script>

<div class="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
	<!-- Total Profesores -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<div class="flex items-center justify-between">
			<h3 class="text-lg font-semibold text-gray-900">Total Profesores</h3>
			<span class="text-3xl font-bold text-blue-600">{statistics.totalCount}</span>
		</div>
		<p class="mt-2 text-sm text-gray-600">Profesores registrados en el sistema</p>
	</div>

	<!-- Profesores Activos -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<div class="flex items-center justify-between">
			<h3 class="text-lg font-semibold text-gray-900">Profesores Activos</h3>
			<span class="text-3xl font-bold text-green-600">{statistics.activeCount}</span>
		</div>
		<p class="mt-2 text-sm text-gray-600">
			{Math.round((statistics.activeCount / statistics.totalCount) * 100) || 0}% del total
		</p>
	</div>

	<!-- Profesores Inactivos -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<div class="flex items-center justify-between">
			<h3 class="text-lg font-semibold text-gray-900">Profesores Inactivos</h3>
			<span class="text-3xl font-bold text-red-600">{statistics.inactiveCount}</span>
		</div>
		<p class="mt-2 text-sm text-gray-600">
			{Math.round((statistics.inactiveCount / statistics.totalCount) * 100) || 0}% del total
		</p>
	</div>

	<!-- Nuevos este mes -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<div class="flex items-center justify-between">
			<h3 class="text-lg font-semibold text-gray-900">Nuevos este mes</h3>
			<span class="text-3xl font-bold text-purple-600">{statistics.newThisMonth}</span>
		</div>
		<p class="mt-2 text-sm text-gray-600">
			{statistics.newThisYear} nuevos este año
		</p>
	</div>
</div>
