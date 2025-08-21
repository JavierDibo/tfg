<script lang="ts">
	import type { Material } from '$lib/generated/api/models/Material';

	let { onClose, onAdd } = $props<{
		onClose: () => void;
		onAdd: (material: Material) => void;
	}>();

	let formData = $state({
		nombre: '',
		url: ''
	});

	function handleSubmit() {
		if (!formData.nombre.trim() || !formData.url.trim()) {
			return;
		}

		const material: Material = {
			id: crypto.randomUUID(), // Generate a unique ID
			nombre: formData.nombre.trim(),
			url: formData.url.trim()
		};

		onAdd(material);
	}

	function handleKeyDown(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			onClose();
		}
	}
</script>

<!-- Modal backdrop -->
<div
	class="bg-opacity-50 fixed inset-0 z-50 h-full w-full overflow-y-auto bg-gray-600"
	onclick={onClose}
	onkeydown={handleKeyDown}
>
	<!-- Modal content -->
	<div class="relative top-20 mx-auto w-96 rounded-md border bg-white p-5 shadow-lg">
		<div class="mt-3">
			<div class="mb-4 flex items-center justify-between">
				<h3 class="text-lg font-medium text-gray-900">Agregar Material</h3>
				<button onclick={onClose} class="text-gray-400 hover:text-gray-600">
					<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M6 18L18 6M6 6l12 12"
						/>
					</svg>
				</button>
			</div>

			<form class="space-y-4">
				<div>
					<label for="nombre" class="block text-sm font-medium text-gray-700"
						>Nombre del Material</label
					>
					<input
						type="text"
						id="nombre"
						bind:value={formData.nombre}
						required
						placeholder="Ej: Apuntes de la clase 1"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					/>
				</div>

				<div>
					<label for="url" class="block text-sm font-medium text-gray-700">URL del Material</label>
					<input
						type="url"
						id="url"
						bind:value={formData.url}
						required
						placeholder="https://ejemplo.com/material.pdf"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					/>
					<p class="mt-1 text-xs text-gray-500">
						Puede ser un enlace a PDF, documento, imagen, video, etc.
					</p>
				</div>

				<div class="flex justify-end space-x-3 pt-4">
					<button
						type="button"
						onclick={onClose}
						class="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						Cancelar
					</button>
					<button
						type="button"
						onclick={handleSubmit}
						disabled={!formData.nombre.trim() || !formData.url.trim()}
						class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none disabled:cursor-not-allowed disabled:opacity-50"
					>
						Agregar Material
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
