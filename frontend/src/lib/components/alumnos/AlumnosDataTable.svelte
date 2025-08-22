<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOAlumno } from '$lib/generated/api';
	import type {
		PaginatedEntities,
		EntityPagination,
		AuthStoreType
	} from '$lib/components/common/types';

	export let loading: boolean;
	export let paginatedData: PaginatedEntities<DTOAlumno>;
	export let currentPagination: EntityPagination;
	export let authStore: AuthStoreType;

	const dispatch = createEventDispatcher();

	function formatDate(date: Date | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	function changeSorting(field: string) {
		dispatch('changeSorting', field);
	}

	function viewAlumno(id: string | number) {
		dispatch('viewAlumno', id);
	}

	function toggleEnrollmentStatus(alumno: DTOAlumno) {
		dispatch('toggleEnrollmentStatus', alumno);
	}

	function toggleAccountStatus(alumno: DTOAlumno) {
		dispatch('toggleAccountStatus', alumno);
	}

	function confirmDelete(alumno: DTOAlumno) {
		dispatch('confirmDelete', alumno);
	}

	// Removed filterAlumnos function - filtering is now handled in the main page component
	// This prevents double-filtering and ensures proper accent-insensitive search
</script>

<div class="overflow-hidden rounded-lg bg-white shadow-md">
	{#if loading && !paginatedData?.content}
		<!-- Initial loading state -->
		<div class="py-8 text-center">
			<div class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-500"></div>
			<p class="mt-2 text-gray-600">Cargando alumnos...</p>
		</div>
	{:else if !paginatedData?.content || paginatedData.content.length === 0}
		<div class="py-8 text-center">
			<p class="text-gray-500">No se encontraron alumnos que coincidan con los filtros.</p>
		</div>
	{:else}
		<!-- Desktop Table -->
		<div class="hidden overflow-x-auto lg:block">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							<button
								onclick={() => changeSorting('nombre')}
								class="flex items-center hover:text-gray-700"
							>
								Alumno
								{#if currentPagination.sortBy === 'nombre'}
									<span class="ml-1">{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</button>
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							<button
								onclick={() => changeSorting('dni')}
								class="flex items-center hover:text-gray-700"
							>
								DNI
								{#if currentPagination.sortBy === 'dni'}
									<span class="ml-1">{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</button>
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							<button
								onclick={() => changeSorting('email')}
								class="flex items-center hover:text-gray-700"
							>
								Email
								{#if currentPagination.sortBy === 'email'}
									<span class="ml-1">{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</button>
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
							>Teléfono</th
						>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							<button
								onclick={() => changeSorting('fechaCreacion')}
								class="flex items-center hover:text-gray-700"
							>
								Fecha Inscripción
								{#if currentPagination.sortBy === 'fechaCreacion'}
									<span class="ml-1">{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</button>
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
						>
							<button
								onclick={() => changeSorting('matriculado')}
								class="flex items-center hover:text-gray-700"
							>
								Estado
								{#if currentPagination.sortBy === 'matriculado'}
									<span class="ml-1">{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span>
								{/if}
							</button>
						</th>
						<th
							class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
							>Acciones</th
						>
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					<!-- Skeleton rows during loading actions -->
					{#if loading && paginatedData?.content}
						{#each Array(currentPagination.size) as _, i (i)}
							<tr class="animate-pulse">
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="flex items-center">
										<div class="space-y-2">
											<div class="h-4 w-32 rounded bg-gray-200"></div>
											<div class="h-3 w-20 rounded bg-gray-200"></div>
										</div>
									</div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="h-4 w-24 rounded bg-gray-200"></div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="h-4 w-40 rounded bg-gray-200"></div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="h-4 w-28 rounded bg-gray-200"></div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="h-4 w-24 rounded bg-gray-200"></div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="space-y-1">
										<div class="h-6 w-20 rounded-full bg-gray-200"></div>
										<div class="h-6 w-24 rounded-full bg-gray-200"></div>
									</div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="flex space-x-2">
										<div class="h-4 w-8 rounded bg-gray-200"></div>
										<div class="h-4 w-16 rounded bg-gray-200"></div>
										<div class="h-4 w-12 rounded bg-gray-200"></div>
									</div>
								</td>
							</tr>
						{/each}
					{:else}
						{#each paginatedData.content as alumno (alumno.id)}
							<tr class="hover:bg-gray-50">
								<td class="px-6 py-4 whitespace-nowrap">
									<div>
										<div class="text-sm font-medium text-gray-900">
											{alumno.nombre}
											{alumno.apellidos}
										</div>
										<div class="text-sm text-gray-500">@{alumno.usuario}</div>
									</div>
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{alumno.dni}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{alumno.email}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{alumno.numeroTelefono || '-'}
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
									{formatDate(alumno.fechaInscripcion)}
								</td>
								<td class="px-6 py-4 whitespace-nowrap">
									<div class="space-y-1">
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.matriculado
												? 'bg-green-100 text-green-800'
												: 'bg-yellow-100 text-yellow-800'}"
										>
											{alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
										</span>
										<br />
										<span
											class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
												? 'bg-blue-100 text-blue-800'
												: 'bg-red-100 text-red-800'}"
										>
											{alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
										</span>
									</div>
								</td>
								<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500">
									<div class="flex space-x-2">
										<button
											onclick={(e) => {
												e.preventDefault();
												viewAlumno(alumno.id!);
											}}
											class="text-blue-600 hover:text-blue-900"
											title="Ver/Editar"
										>
											Ver
										</button>
										{#if authStore.isAdmin}
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													toggleEnrollmentStatus(alumno);
												}}
												class="text-green-600 hover:text-green-900"
												title={alumno.matriculado ? 'Desmatricular' : 'Matricular'}
											>
												{alumno.matriculado ? 'Desmatricular' : 'Matricular'}
											</button>
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													toggleAccountStatus(alumno);
												}}
												class="text-yellow-600 hover:text-yellow-900"
												title={alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
											>
												{alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
											</button>
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													confirmDelete(alumno);
												}}
												class="text-red-600 hover:text-red-900"
												title="Eliminar"
											>
												Eliminar
											</button>
										{/if}
									</div>
								</td>
							</tr>
						{/each}
					{/if}
				</tbody>
			</table>
		</div>

		<!-- Mobile Cards -->
		<div class="lg:hidden">
			<!-- Skeleton cards during loading actions -->
			{#if loading && paginatedData?.content}
				{#each Array(currentPagination.size) as _, i (i)}
					<div class="animate-pulse border-b border-gray-200 p-4">
						<div class="mb-2 flex items-start justify-between">
							<div class="space-y-2">
								<div class="h-5 w-32 rounded bg-gray-200"></div>
								<div class="h-4 w-20 rounded bg-gray-200"></div>
							</div>
							<div class="h-6 w-24 rounded-full bg-gray-200"></div>
						</div>
						<div class="space-y-2">
							<div class="h-4 w-full rounded bg-gray-200"></div>
							<div class="h-4 w-3/4 rounded bg-gray-200"></div>
							<div class="h-4 w-1/2 rounded bg-gray-200"></div>
						</div>
						<div class="mt-3 flex space-x-2">
							<div class="h-4 w-16 rounded bg-gray-200"></div>
							<div class="h-4 w-20 rounded bg-gray-200"></div>
							<div class="h-4 w-12 rounded bg-gray-200"></div>
						</div>
					</div>
				{/each}
			{:else}
				{#each paginatedData.content as alumno (alumno.id)}
					<div class="border-b border-gray-200 p-4">
						<div class="mb-2 flex items-start justify-between">
							<div>
								<h3 class="font-medium text-gray-900">{alumno.nombre} {alumno.apellidos}</h3>
								<p class="text-sm text-gray-500">@{alumno.usuario}</p>
							</div>
							<div class="text-right">
								<span
									class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.matriculado
										? 'bg-green-100 text-green-800'
										: 'bg-yellow-100 text-yellow-800'}"
								>
									{alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
								</span>
							</div>
						</div>
						<div class="space-y-1 text-sm text-gray-600">
							<p><strong>DNI:</strong> {alumno.dni}</p>
							<p><strong>Email:</strong> {alumno.email}</p>
							{#if alumno.numeroTelefono}
								<p><strong>Teléfono:</strong> {alumno.numeroTelefono}</p>
							{/if}
							<p><strong>Inscripción:</strong> {formatDate(alumno.fechaInscripcion)}</p>
							<p>
								<strong>Estado:</strong>
								<span
									class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
										? 'bg-blue-100 text-blue-800'
										: 'bg-red-100 text-red-800'}"
								>
									{alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
								</span>
							</p>
						</div>
						<div class="mt-3 flex space-x-2">
							<button
								onclick={(e) => {
									e.preventDefault();
									viewAlumno(alumno.id!);
								}}
								class="text-sm text-blue-600 hover:text-blue-900"
							>
								Ver/Editar
							</button>
							{#if authStore.isAdmin}
								<button
									onclick={(e) => {
										e.preventDefault();
										e.currentTarget.blur();
										toggleEnrollmentStatus(alumno);
									}}
									class="text-sm text-green-600 hover:text-green-900"
								>
									{alumno.matriculado ? 'Desmatricular' : 'Matricular'}
								</button>
								<button
									onclick={(e) => {
										e.preventDefault();
										e.currentTarget.blur();
										toggleAccountStatus(alumno);
									}}
									class="text-sm text-yellow-600 hover:text-yellow-900"
								>
									{alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
								</button>
								<button
									onclick={(e) => {
										e.preventDefault();
										e.currentTarget.blur();
										confirmDelete(alumno);
									}}
									class="text-sm text-red-600 hover:text-red-900"
								>
									Eliminar
								</button>
							{/if}
						</div>
					</div>
				{/each}
			{/if}
		</div>
	{/if}
</div>
