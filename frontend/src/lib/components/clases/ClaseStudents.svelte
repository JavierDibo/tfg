<script lang="ts">
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';

	let { clase } = $props<{
		clase: DTOClase;
	}>();
</script>

<div class="rounded-lg bg-white p-6 shadow">
	<h3 class="mb-4 text-lg font-medium text-gray-900">Alumnos Inscritos</h3>

	{#if clase.alumnosId && clase.alumnosId.length > 0}
		<div class="space-y-3">
			{#each clase.alumnosId as alumnoId}
				<div class="flex items-center justify-between rounded-lg bg-gray-50 p-3">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100">
								<svg
									class="h-4 w-4 text-blue-600"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
									/>
								</svg>
							</div>
						</div>
						<div class="ml-3">
							<p class="text-sm font-medium text-gray-900">{alumnoId}</p>
							<p class="text-xs text-gray-500">ID: {alumnoId}</p>
						</div>
					</div>
					<div class="flex items-center space-x-2">
						<span
							class="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800"
						>
							Inscrito
						</span>
					</div>
				</div>
			{/each}
		</div>

		<div class="mt-4 border-t border-gray-200 pt-4">
			<p class="text-sm text-gray-600">
				Total de alumnos inscritos: <span class="font-medium">{clase.alumnosId.length}</span>
			</p>
		</div>
	{:else}
		<div class="py-8 text-center">
			<svg
				class="mx-auto h-12 w-12 text-gray-400"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
				/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">No hay alumnos inscritos</h3>
			<p class="mt-1 text-sm text-gray-500">Aún no se han inscrito alumnos en esta clase.</p>
		</div>
	{/if}
</div>
