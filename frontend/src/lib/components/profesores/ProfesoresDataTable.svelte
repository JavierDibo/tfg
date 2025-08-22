<script lang="ts">
	import type { DTOProfesor } from '$lib/generated/api';
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	let {
		loading,
		profesores,
		authStore
	}: {
		loading: boolean;
		profesores: DTOProfesor[];
		authStore: { isAdmin: boolean };
	} = $props();

	function formatDate(date: Date | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	function handleSort(field: string) {
		dispatch('changeSorting', field);
	}
</script>

<div class="overflow-x-auto rounded-lg border border-gray-200 bg-white shadow-md">
	<table class="min-w-full divide-y divide-gray-200">
		<thead class="bg-gray-50">
			<tr>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('id')}>ID</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('nombreCompleto')}>Nombre</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('email')}>Email</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('dni')}>DNI</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('enabled')}>Estado</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					><button onclick={() => handleSort('fechaCreacion')}>Fecha de Creación</button></th
				>
				<th
					scope="col"
					class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
					>Acciones</th
				>
			</tr>
		</thead>
		<tbody class="divide-y divide-gray-200 bg-white">
			{#if loading}
				<tr>
					<td colspan="7" class="py-12 text-center">Cargando profesores...</td>
				</tr>
			{:else if profesores.length === 0}
				<tr>
					<td colspan="7" class="py-12 text-center">No se encontraron profesores.</td>
				</tr>
			{:else}
				{#each profesores as profesor (profesor.id)}
					<tr class="hover:bg-gray-50">
						<td class="px-6 py-4 text-sm font-medium whitespace-nowrap text-gray-900"
							>{profesor.id}</td
						>
						<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500"
							>{profesor.nombreCompleto}</td
						>
						<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500">{profesor.email}</td>
						<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500">{profesor.dni}</td>
						<td class="px-6 py-4 text-sm whitespace-nowrap">
							{#if profesor.enabled}
								<span
									class="inline-flex rounded-full bg-green-100 px-2 text-xs leading-5 font-semibold text-green-800"
									>Habilitado</span
								>
							{:else}
								<span
									class="inline-flex rounded-full bg-red-100 px-2 text-xs leading-5 font-semibold text-red-800"
									>Deshabilitado</span
								>
							{/if}
						</td>
						<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500"
							>{formatDate(profesor.fechaCreacion)}</td
						>
						<td class="px-6 py-4 text-sm font-medium whitespace-nowrap">
							<div class="flex items-center gap-2">
								<button
									onclick={() => dispatch('viewProfesor', profesor.id)}
									class="text-blue-600 hover:text-blue-900">Ver</button
								>
								{#if authStore.isAdmin}
									<button
										onclick={() => dispatch('toggleAccountStatus', profesor)}
										class={profesor.enabled
											? 'text-yellow-600 hover:text-yellow-900'
											: 'text-green-600 hover:text-green-900'}
										>{profesor.enabled ? 'Deshabilitar' : 'Habilitar'}</button
									>
									<button
										onclick={() => dispatch('confirmDelete', profesor)}
										class="text-red-600 hover:text-red-900">Eliminar</button
									>
								{/if}
							</div>
						</td>
					</tr>
				{/each}
			{/if}
		</tbody>
	</table>
</div>
