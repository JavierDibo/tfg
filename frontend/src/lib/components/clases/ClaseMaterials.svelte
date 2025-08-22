<script lang="ts">
	import type { DTOClaseConDetalles } from '$lib/generated/api/models/DTOClaseConDetalles';

	let {
		clase,
		canEdit = false,
		onAddMaterial,
		onRemoveMaterial
	} = $props<{
		clase: DTOClaseConDetalles;
		canEdit?: boolean;
		onAddMaterial: () => void;
		onRemoveMaterial: (materialId: string) => void;
	}>();

	function getFileIcon(url: string): string {
		const extension = url.split('.').pop()?.toLowerCase();
		switch (extension) {
			case 'pdf':
				return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
			case 'doc':
			case 'docx':
				return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
			case 'ppt':
			case 'pptx':
				return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
			case 'xls':
			case 'xlsx':
				return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
			case 'jpg':
			case 'jpeg':
			case 'png':
			case 'gif':
				return 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z';
			case 'mp4':
			case 'avi':
			case 'mov':
				return 'M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z';
			default:
				return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
		}
	}

	function getFileColor(url: string): string {
		const extension = url.split('.').pop()?.toLowerCase();
		switch (extension) {
			case 'pdf':
				return 'text-red-500';
			case 'doc':
			case 'docx':
				return 'text-blue-500';
			case 'ppt':
			case 'pptx':
				return 'text-orange-500';
			case 'xls':
			case 'xlsx':
				return 'text-green-500';
			case 'jpg':
			case 'jpeg':
			case 'png':
			case 'gif':
				return 'text-purple-500';
			case 'mp4':
			case 'avi':
			case 'mov':
				return 'text-indigo-500';
			default:
				return 'text-gray-500';
		}
	}
</script>

<div class="rounded-lg bg-white p-6 shadow-md">
	<div class="mb-6 flex items-center justify-between">
		<h2 class="text-xl font-semibold text-gray-900">Materiales de la Clase</h2>
		{#if canEdit}
			<button
				onclick={onAddMaterial}
				class="inline-flex items-center rounded-md border border-transparent bg-blue-600 px-3 py-2 text-sm leading-4 font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
			>
				<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 6v6m0 0v6m0-6h6m-6 0H6"
					/>
				</svg>
				Agregar Material
			</button>
		{/if}
	</div>

	{#if clase.material && clase.material.length > 0}
		<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
			{#each clase.material as material (material.id)}
				<div class="rounded-lg border border-gray-200 p-4 transition-shadow hover:shadow-md">
					<div class="flex items-start justify-between">
						<div class="flex min-w-0 flex-1 items-center">
							<div class="flex-shrink-0">
								<svg
									class="h-8 w-8 {getFileColor(material.url)}"
									fill="none"
									stroke="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d={getFileIcon(material.url)}
									/>
								</svg>
							</div>
							<div class="ml-3 min-w-0 flex-1">
								<p class="truncate text-sm font-medium text-gray-900">
									{material.nombre}
								</p>
								<p class="truncate text-xs text-gray-500">
									{material.url}
								</p>
							</div>
						</div>
						<div class="flex items-center space-x-2">
							<a
								href={material.url}
								target="_blank"
								rel="noopener noreferrer"
								class="p-1 text-blue-600 hover:text-blue-800"
								title="Ver material"
								aria-label="Ver material {material.nombre}"
							>
								<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
									/>
								</svg>
							</a>
							{#if canEdit}
								<button
									onclick={() => onRemoveMaterial(material.id)}
									class="p-1 text-red-600 hover:text-red-800"
									title="Eliminar material"
									aria-label="Eliminar material {material.nombre}"
								>
									<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
										/>
									</svg>
								</button>
							{/if}
						</div>
					</div>
				</div>
			{/each}
		</div>
	{:else}
		<div class="py-12 text-center">
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
					d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
				/>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">No hay materiales</h3>
			<p class="mt-1 text-sm text-gray-500">
				{#if canEdit}
					Comienza agregando materiales a esta clase.
				{:else}
					Esta clase aún no tiene materiales disponibles.
				{/if}
			</p>
			{#if canEdit}
				<div class="mt-6">
					<button
						onclick={onAddMaterial}
						class="inline-flex items-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 6v6m0 0v6m0-6h6m-6 0H6"
							/>
						</svg>
						Agregar Material
					</button>
				</div>
			{/if}
		</div>
	{/if}
</div>
