<script lang="ts">
	import { goto } from '$app/navigation';
	import type { AlumnoStatistics } from '$lib/services/alumnoService';

	interface Props {
		statistics: AlumnoStatistics | null;
		isAdmin: boolean;
	}

	const { statistics, isAdmin }: Props = $props();

	// Helper to display statistics values properly with Svelte 5
	const displayStats = $derived(() => statistics);
</script>

<!-- Quick Actions -->
<div class="rounded-lg bg-white p-6 shadow-md">
	<h3 class="mb-4 text-lg font-semibold text-gray-900">Acciones Rápidas</h3>

	<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
		<button
			onclick={() => goto('/alumnos')}
			class="flex items-center justify-center rounded-lg border border-gray-300 p-4 transition-colors hover:bg-gray-50"
		>
			<div class="text-center">
				<svg
					class="mx-auto mb-2 h-8 w-8 text-blue-600"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
					/>
				</svg>
				<p class="text-sm font-medium text-gray-900">Ver Todos</p>
				<p class="text-xs text-gray-500">Lista completa</p>
			</div>
		</button>

		{#if isAdmin}
			<button
				onclick={() => goto('/alumnos/nuevo')}
				class="flex items-center justify-center rounded-lg border border-gray-300 p-4 transition-colors hover:bg-gray-50"
			>
				<div class="text-center">
					<svg
						class="mx-auto mb-2 h-8 w-8 text-green-600"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 6v6m0 0v6m0-6h6m-6 0H6"
						/>
					</svg>
					<p class="text-sm font-medium text-gray-900">Nuevo Alumno</p>
					<p class="text-xs text-gray-500">Registrar estudiante</p>
				</div>
			</button>

			<button
				onclick={() => goto('/alumnos?matriculado=false')}
				class="flex items-center justify-center rounded-lg border border-gray-300 p-4 transition-colors hover:bg-gray-50"
			>
				<div class="text-center">
					<svg
						class="mx-auto mb-2 h-8 w-8 text-yellow-600"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z"
						/>
					</svg>
					<p class="text-sm font-medium text-gray-900">No Matriculados</p>
					<p class="text-xs text-gray-500">{displayStats()?.noMatriculados || 0} estudiantes</p>
				</div>
			</button>
		{/if}

		<button
			onclick={() => goto('/alumnos?matriculado=true')}
			class="flex items-center justify-center rounded-lg border border-gray-300 p-4 transition-colors hover:bg-gray-50"
		>
			<div class="text-center">
				<svg
					class="mx-auto mb-2 h-8 w-8 text-green-600"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
				<p class="text-sm font-medium text-gray-900">Matriculados</p>
				<p class="text-xs text-gray-500">{displayStats()?.matriculados || 0} estudiantes</p>
			</div>
		</button>
	</div>
</div>
