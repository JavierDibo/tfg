<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOProfesor } from '$lib/generated/api';

	const dispatch = createEventDispatcher();

	let {
		showModal,
		profesorToDelete
	}: { showModal: boolean; profesorToDelete: DTOProfesor | null } = $props();
</script>

{#if showModal}
	<div
		class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black"
		onclick={() => (showModal = false)}
		onkeydown={(e) => e.key === 'Enter' && (showModal = false)}
		role="button"
		tabindex="0"
	>
		<div
			class="rounded-lg bg-white p-8 shadow-2xl"
			onclick={(e) => e.stopPropagation()}
			onkeydown={(e) => e.key === 'Enter' && e.stopPropagation()}
			role="button"
			tabindex="0"
		>
			<h2 class="mb-4 text-2xl font-bold">Confirmar Eliminación</h2>
			{#if profesorToDelete}
				<p>
					¿Estás seguro de que deseas eliminar al profesor
					<span class="font-semibold">{profesorToDelete.nombreCompleto}</span>?
				</p>
				<p class="mt-2 text-sm text-gray-600">Esta acción no se puede deshacer.</p>
			{/if}
			<div class="mt-6 flex justify-end gap-4">
				<button
					class="rounded-lg bg-gray-200 px-4 py-2 text-gray-800 hover:bg-gray-300"
					onclick={() => (showModal = false)}>Cancelar</button
				>
				<button
					class="rounded-lg bg-red-600 px-4 py-2 text-white hover:bg-red-700"
					onclick={() => dispatch('confirmDelete')}>Eliminar</button
				>
			</div>
		</div>
	</div>
{/if}
