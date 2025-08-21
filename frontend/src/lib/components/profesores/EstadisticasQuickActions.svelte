<script lang="ts">
	import { goto } from '$app/navigation';
	import type { ProfesorStatistics } from '$lib/services/profesorService';

	export let statistics: ProfesorStatistics;
	export let isAdmin: boolean = false;
</script>

<div class="rounded-lg bg-white p-6 shadow-md">
	<h3 class="mb-4 text-lg font-semibold text-gray-900">Acciones Rápidas</h3>

	<div class="space-y-4">
		<button
			on:click={() => goto('/profesores')}
			class="flex w-full items-center justify-between rounded-md border border-gray-200 px-4 py-3 text-left hover:bg-gray-50"
		>
			<span class="text-gray-700">Ver lista de profesores</span>
			<span class="text-sm text-gray-500">{statistics.totalCount} profesores</span>
		</button>

		{#if isAdmin}
			<button
				on:click={() => goto('/profesores/nuevo')}
				class="flex w-full items-center justify-between rounded-md border border-gray-200 px-4 py-3 text-left hover:bg-gray-50"
			>
				<span class="text-gray-700">Crear nuevo profesor</span>
				<span class="rounded-full bg-green-100 px-2 py-1 text-xs font-semibold text-green-800">
					+ Añadir
				</span>
			</button>
		{/if}

		{#if isAdmin}
			<button
				on:click={() => goto('/profesores?habilitado=false')}
				class="flex w-full items-center justify-between rounded-md border border-gray-200 px-4 py-3 text-left hover:bg-gray-50"
			>
				<span class="text-gray-700">Ver profesores inactivos</span>
				<span class="text-sm text-gray-500">{statistics.inactiveCount} profesores</span>
			</button>
		{/if}
	</div>
</div>
