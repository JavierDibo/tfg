<script lang="ts">
	import type { ProfesorStatistics } from '$lib/services/profesorService';

	export let statistics: ProfesorStatistics;
</script>

<div class="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
	<!-- Estado de Profesores Chart -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Estado de Profesores</h3>
		<div class="flex h-64 items-center justify-center">
			<!-- Placeholder for chart - In a real implementation this would be a proper chart component -->
			<div class="relative h-48 w-48 rounded-full">
				<!-- Active segment -->
				<div
					class="absolute inset-0 overflow-hidden rounded-full"
					style="clip-path: polygon(50% 50%, 100% 50%, 100% 0%, 50% 0%);"
				>
					<div class="h-full w-full bg-green-500"></div>
				</div>
				<!-- Inactive segment -->
				<div
					class="absolute inset-0 overflow-hidden rounded-full"
					style="clip-path: polygon(50% 50%, 50% 0%, 0% 0%, 0% 100%, 100% 100%, 100% 50%);"
				>
					<div class="h-full w-full bg-red-500"></div>
				</div>
				<!-- Center circle -->
				<div
					class="absolute top-1/2 left-1/2 flex h-24 w-24 -translate-x-1/2 -translate-y-1/2 flex-col items-center justify-center rounded-full bg-white shadow-md"
				>
					<span class="text-xs text-gray-500">Activos</span>
					<span class="text-lg font-bold text-gray-900"
						>{Math.round((statistics.activeCount / statistics.totalCount) * 100)}%</span
					>
				</div>
			</div>
		</div>
		<div class="mt-4 flex justify-center space-x-8">
			<div class="flex items-center">
				<div class="mr-2 h-3 w-3 rounded-full bg-green-500"></div>
				<span class="text-sm text-gray-600">Activos: {statistics.activeCount}</span>
			</div>
			<div class="flex items-center">
				<div class="mr-2 h-3 w-3 rounded-full bg-red-500"></div>
				<span class="text-sm text-gray-600">Inactivos: {statistics.inactiveCount}</span>
			</div>
		</div>
	</div>

	<!-- Profesores por Mes Chart -->
	<div class="rounded-lg bg-white p-6 shadow-md">
		<h3 class="mb-4 text-lg font-semibold text-gray-900">Profesores Nuevos por Mes</h3>
		<div class="flex h-64 items-center justify-center">
			<!-- Placeholder for chart - In a real implementation this would be a proper chart component -->
			<div class="flex h-48 w-full items-end justify-around space-x-2">
				<div class="flex w-8 flex-col items-center">
					<div class="h-16 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">Ene</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-24 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">Feb</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-32 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">Mar</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-20 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">Abr</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-12 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">May</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-36 w-full rounded-t-md bg-blue-500"></div>
					<span class="mt-2 text-xs text-gray-500">Jun</span>
				</div>
				<div class="flex w-8 flex-col items-center">
					<div class="h-{statistics.newThisMonth * 5 + 10} w-full rounded-t-md bg-blue-600"></div>
					<span class="mt-2 text-xs font-bold text-blue-600">Actual</span>
				</div>
			</div>
		</div>
		<p class="mt-4 text-center text-sm text-gray-600">
			{statistics.newThisYear} profesores nuevos en {new Date().getFullYear()}
		</p>
	</div>
</div>
