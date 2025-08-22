<script lang="ts">
	import type { DTOClaseConDetalles } from '$lib/generated/api/models/DTOClaseConDetalles';

	let { clase, onClose, onSave } = $props<{
		clase: DTOClaseConDetalles;
		onClose: () => void;
		onSave: (updatedClase: DTOClaseConDetalles) => void;
	}>();

	let formData = $state({
		titulo: clase.titulo || '',
		descripcion: clase.descripcion || '',
		precio: clase.precio || 0,
		presencialidad: clase.presencialidad || 'ONLINE',
		nivel: clase.nivel || 'PRINCIPIANTE',
		imagenPortada: clase.imagenPortada || ''
	});

	function handleSubmit() {
		const updatedClase: DTOClaseConDetalles = {
			...clase,
			...formData
		};
		onSave(updatedClase);
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
	role="dialog"
	aria-modal="true"
	aria-labelledby="modal-title"
	tabindex="-1"
>
	<!-- Modal content -->
	<div class="relative top-20 mx-auto w-96 rounded-md border bg-white p-5 shadow-lg">
		<div class="mt-3">
			<div class="mb-4 flex items-center justify-between">
				<h3 id="modal-title" class="text-lg font-medium text-gray-900">Editar Clase</h3>
				<button
					onclick={onClose}
					class="text-gray-400 hover:text-gray-600"
					aria-label="Cerrar modal"
				>
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
					<label for="titulo" class="block text-sm font-medium text-gray-700">Título</label>
					<input
						type="text"
						id="titulo"
						bind:value={formData.titulo}
						required
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					/>
				</div>

				<div>
					<label for="descripcion" class="block text-sm font-medium text-gray-700"
						>Descripción</label
					>
					<textarea
						id="descripcion"
						bind:value={formData.descripcion}
						rows="3"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					></textarea>
				</div>

				<div>
					<label for="precio" class="block text-sm font-medium text-gray-700">Precio (€)</label>
					<input
						type="number"
						id="precio"
						bind:value={formData.precio}
						min="0"
						step="0.01"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					/>
				</div>

				<div>
					<label for="presencialidad" class="block text-sm font-medium text-gray-700"
						>Presencialidad</label
					>
					<select
						id="presencialidad"
						bind:value={formData.presencialidad}
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					>
						<option value="ONLINE">Online</option>
						<option value="PRESENCIAL">Presencial</option>
					</select>
				</div>

				<div>
					<label for="nivel" class="block text-sm font-medium text-gray-700">Nivel</label>
					<select
						id="nivel"
						bind:value={formData.nivel}
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					>
						<option value="PRINCIPIANTE">Principiante</option>
						<option value="INTERMEDIO">Intermedio</option>
						<option value="AVANZADO">Avanzado</option>
					</select>
				</div>

				<div>
					<label for="imagenPortada" class="block text-sm font-medium text-gray-700"
						>URL de la imagen de portada</label
					>
					<input
						type="url"
						id="imagenPortada"
						bind:value={formData.imagenPortada}
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
					/>
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
						class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						Guardar Cambios
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
