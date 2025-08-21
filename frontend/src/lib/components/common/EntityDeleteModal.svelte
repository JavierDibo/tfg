<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { BaseEntity } from './types';

	const dispatch = createEventDispatcher();

	export let showModal: boolean = false;
	export let entity: BaseEntity | null = null;
	export let entityName: string = 'elemento';
	export let entityNameCapitalized: string = 'Elemento';
	export let displayNameField: string = 'nombre';
	export let confirmText: string = 'Eliminar';
	export let cancelText: string = 'Cancelar';
	export let warningText: string = 'Esta acción no se puede deshacer.';

	function getDisplayName(): string {
		if (!entity) return '';

		// Try to generate a display name from common name fields
		const nameFields = [displayNameField, 'nombreCompleto', 'nombre', 'title', 'label', 'id'];

		for (const field of nameFields) {
			if (entity[field]) {
				if (field === 'nombre' && entity['apellidos']) {
					return `${entity.nombre} ${entity.apellidos}`;
				}
				return entity[field];
			}
		}

		// If no name field found, return ID or "unknown"
		return entity.id || 'desconocido';
	}

	function handleCancel() {
		dispatch('cancelDelete');
	}

	function handleConfirm() {
		dispatch('confirmDelete');
	}
</script>

{#if showModal}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black p-4">
		<div
			class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl"
			onclick={(e) => e.stopPropagation()}
			onkeydown={(e) => e.stopPropagation()}
			tabindex="-1"
			role="dialog"
			aria-modal="true"
			aria-labelledby="modal-title"
		>
			<h2 id="modal-title" class="mb-4 text-xl font-bold text-red-600">
				Eliminar {entityNameCapitalized}
			</h2>

			<p class="mb-6 text-gray-700">
				¿Está seguro que desea eliminar el {entityName} <strong>{getDisplayName()}</strong>?
			</p>

			{#if warningText}
				<p class="mb-6 text-sm text-red-600">{warningText}</p>
			{/if}

			<div class="flex justify-end space-x-3">
				<button
					onclick={handleCancel}
					class="rounded-md bg-gray-200 px-4 py-2 text-gray-800 hover:bg-gray-300 focus:ring-2 focus:ring-gray-400 focus:outline-none"
				>
					{cancelText}
				</button>
				<button
					onclick={handleConfirm}
					class="rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:outline-none"
				>
					{confirmText}
				</button>
			</div>
		</div>
	</div>
{/if}
