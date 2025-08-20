<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOAlumno } from '$lib/generated/models';
	
	export let alumnos: DTOAlumno[];
	export let isLoading: boolean;
	export let currentPage: number;
	export let pageSize: number;
	
	const dispatch = createEventDispatcher();
	
	function handleEdit(alumno: DTOAlumno) {
		dispatch('edit', alumno);
	}
	
	function handleDelete(alumno: DTOAlumno) {
		dispatch('delete', alumno);
	}
	
	function handleView(alumno: DTOAlumno) {
		dispatch('view', alumno);
	}
	
	function formatDate(dateString: string) {
		return new Date(dateString).toLocaleDateString('es-ES');
	}
</script>

<div class="bg-white shadow overflow-hidden sm:rounded-md">
	<div class="px-4 py-5 sm:p-6">
		{#if isLoading}
			<div class="flex justify-center items-center py-12">
				<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
			</div>
		{:else if alumnos.length === 0}
			<div class="text-center py-12">
				<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
				</svg>
				<h3 class="mt-2 text-sm font-medium text-gray-900">No se encontraron alumnos</h3>
				<p class="mt-1 text-sm text-gray-500">Intenta ajustar los filtros de búsqueda.</p>
			</div>
		{:else}
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200">
					<thead class="bg-gray-50">
						<tr>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Alumno</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entidad</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
							<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
						</tr>
					</thead>
					<tbody class="bg-white divide-y divide-gray-200">
						{#each alumnos as alumno}
							<tr class="hover:bg-gray-50">
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="flex items-center">
										<div class="flex-shrink-0 h-10 w-10">
											<div class="h-10 w-10 rounded-full bg-indigo-100 flex items-center justify-center">
												<span class="text-sm font-medium text-indigo-600">
													{alumno.nombre?.charAt(0) || 'A'}
												</span>
											</div>
										</div>
										<div class="ml-4">
											<div class="text-sm font-medium text-gray-900">{alumno.nombre}</div>
											<div class="text-sm text-gray-500">ID: {alumno.id}</div>
										</div>
									</div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{alumno.email}</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{alumno.telefono || '-'}</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{alumno.entidad?.nombre || '-'}</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {alumno.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
										{alumno.activo ? 'Activo' : 'Inactivo'}
									</span>
								</td>
								<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
									<div class="flex space-x-2">
										<button
											on:click={() => handleView(alumno)}
											class="text-indigo-600 hover:text-indigo-900"
										>
											Ver
										</button>
										<button
											on:click={() => handleEdit(alumno)}
											class="text-blue-600 hover:text-blue-900"
										>
											Editar
										</button>
										<button
											on:click={() => handleDelete(alumno)}
											class="text-red-600 hover:text-red-900"
										>
											Eliminar
										</button>
									</div>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		{/if}
	</div>
</div>
