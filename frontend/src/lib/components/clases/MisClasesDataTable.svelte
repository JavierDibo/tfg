<script lang="ts">
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';
	import { goto } from '$app/navigation';

	let {
		clases,
		onEnrollment,
		enrollmentLoading = null
	} = $props<{
		clases: DTOClase[];
		onEnrollment: (claseId: number, enroll: boolean) => void;
		enrollmentLoading?: number | null;
	}>();

	function getPresencialidadLabel(presencialidad?: string): string {
		switch (presencialidad) {
			case 'ONLINE':
				return 'Online';
			case 'PRESENCIAL':
				return 'Presencial';
			default:
				return 'No especificado';
		}
	}

	function getNivelLabel(nivel?: string): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'Principiante';
			case 'INTERMEDIO':
				return 'Intermedio';
			case 'AVANZADO':
				return 'Avanzado';
			default:
				return 'No especificado';
		}
	}

	function getNivelColor(nivel?: string): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'bg-green-100 text-green-800';
			case 'INTERMEDIO':
				return 'bg-yellow-100 text-yellow-800';
			case 'AVANZADO':
				return 'bg-red-100 text-red-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}
</script>

<div class="overflow-x-auto">
	<table class="min-w-full divide-y divide-gray-200">
		<thead class="bg-gray-50">
			<tr>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Clase
				</th>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Tipo
				</th>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Nivel
				</th>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Presencialidad
				</th>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Precio
				</th>
				<th class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">
					Acciones
				</th>
			</tr>
		</thead>
		<tbody class="divide-y divide-gray-200 bg-white">
			{#each clases as clase (clase.id)}
				<tr class="hover:bg-gray-50">
					<td class="px-6 py-4 whitespace-nowrap">
						<div class="flex items-center">
							<div class="h-10 w-10 flex-shrink-0">
								{#if clase.imagenPortada}
									<img
										class="h-10 w-10 rounded-lg object-cover"
										src={clase.imagenPortada}
										alt={clase.titulo}
									/>
								{:else}
									<div class="flex h-10 w-10 items-center justify-center rounded-lg bg-gray-200">
										<svg
											class="h-6 w-6 text-gray-400"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
											/>
										</svg>
									</div>
								{/if}
							</div>
							<div class="ml-4">
								<div class="text-sm font-medium text-gray-900">
									<button
										onclick={() => goto(`/clases/${clase.id}`)}
										class="rounded hover:text-blue-600 focus:ring-2 focus:ring-blue-500 focus:outline-none"
									>
										{clase.titulo}
									</button>
								</div>
								<div class="text-sm text-gray-500">
									{clase.descripcion?.substring(0, 50)}{clase.descripcion &&
									clase.descripcion.length > 50
										? '...'
										: ''}
								</div>
							</div>
						</div>
					</td>
					<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
						{clase.tipoClase || 'Clase'}
					</td>
					<td class="px-6 py-4 whitespace-nowrap">
						<span
							class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {getNivelColor(
								clase.nivel
							)}"
						>
							{getNivelLabel(clase.nivel)}
						</span>
					</td>
					<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
						{getPresencialidadLabel(clase.presencialidad)}
					</td>
					<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-900">
						{clase.precio ? `€${clase.precio}` : 'Gratis'}
					</td>
					<td class="px-6 py-4 text-sm font-medium whitespace-nowrap">
						<div class="flex space-x-2">
							{#if enrollmentLoading === clase.id}
								<button
									disabled
									class="cursor-not-allowed rounded bg-gray-50 px-2 py-1 text-xs text-gray-400"
								>
									<svg class="mr-1 inline h-3 w-3 animate-spin" fill="none" viewBox="0 0 24 24">
										<circle
											class="opacity-25"
											cx="12"
											cy="12"
											r="10"
											stroke="currentColor"
											stroke-width="4"
										></circle>
										<path
											class="opacity-75"
											fill="currentColor"
											d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
										></path>
									</svg>
									Procesando...
								</button>
							{:else}
								<button
									onclick={() => onEnrollment(clase.id!, false)}
									class="rounded bg-red-50 px-2 py-1 text-xs text-red-600 hover:bg-red-100 hover:text-red-900"
								>
									Desinscribirse
								</button>
							{/if}

							<button
								onclick={() => goto(`/clases/${clase.id}`)}
								class="rounded bg-gray-50 px-2 py-1 text-xs text-gray-600 hover:bg-gray-100 hover:text-gray-900"
							>
								Ver
							</button>
						</div>
					</td>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
