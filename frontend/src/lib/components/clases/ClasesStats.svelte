<script lang="ts">
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';

	let { clases } = $props<{
		clases: DTOClase[];
	}>();

	let totalClasses = $derived(clases.length);
	let totalStudents = $derived(
		clases.reduce((sum: number, clase: DTOClase) => sum + (clase.numeroAlumnos || 0), 0)
	);
	let totalMaterials = $derived(
		clases.reduce((sum: number, clase: DTOClase) => sum + (clase.numeroMateriales || 0), 0)
	);
	let onlineClasses = $derived(
		clases.filter((clase: DTOClase) => clase.presencialidad === 'ONLINE').length
	);
	let presencialClasses = $derived(
		clases.filter((clase: DTOClase) => clase.presencialidad === 'PRESENCIAL').length
	);
</script>

<div class="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
	<div class="overflow-hidden rounded-lg bg-white shadow">
		<div class="p-5">
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
						/>
					</svg>
				</div>
				<div class="ml-5 w-0 flex-1">
					<dl>
						<dt class="truncate text-sm font-medium text-gray-500">Total de Clases</dt>
						<dd class="text-lg font-medium text-gray-900">{totalClasses}</dd>
					</dl>
				</div>
			</div>
		</div>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow">
		<div class="p-5">
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
						/>
					</svg>
				</div>
				<div class="ml-5 w-0 flex-1">
					<dl>
						<dt class="truncate text-sm font-medium text-gray-500">Total de Alumnos</dt>
						<dd class="text-lg font-medium text-gray-900">{totalStudents}</dd>
					</dl>
				</div>
			</div>
		</div>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow">
		<div class="p-5">
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
						/>
					</svg>
				</div>
				<div class="ml-5 w-0 flex-1">
					<dl>
						<dt class="truncate text-sm font-medium text-gray-500">Total de Materiales</dt>
						<dd class="text-lg font-medium text-gray-900">{totalMaterials}</dd>
					</dl>
				</div>
			</div>
		</div>
	</div>

	<div class="overflow-hidden rounded-lg bg-white shadow">
		<div class="p-5">
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9v-9m0-9v9"
						/>
					</svg>
				</div>
				<div class="ml-5 w-0 flex-1">
					<dl>
						<dt class="truncate text-sm font-medium text-gray-500">Online / Presencial</dt>
						<dd class="text-lg font-medium text-gray-900">{onlineClasses} / {presencialClasses}</dd>
					</dl>
				</div>
			</div>
		</div>
	</div>
</div>
