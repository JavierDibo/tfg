<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { DTOAlumno } from '$lib/generated/api';
	
	export let showDeleteModal: boolean;
	export let alumnoToDelete: DTOAlumno | null;
	
	const dispatch = createEventDispatcher();
	
	function cancelDelete() {
		dispatch('cancelDelete');
	}
	
	function confirmDelete() {
		dispatch('confirmDelete');
	}
</script>

{#if showDeleteModal && alumnoToDelete}
	<div class="bg-opacity-50 fixed inset-0 z-50 h-full w-full overflow-y-auto bg-gray-600">
		<div class="relative top-20 mx-auto w-96 rounded-md border bg-white p-5 shadow-lg">
			<div class="mt-3 text-center">
				<h3 class="text-lg font-medium text-gray-900">Confirmar Eliminación</h3>
				<div class="mt-2 px-7 py-3">
					<p class="text-sm text-gray-500">
						¿Estás seguro de que quieres eliminar al alumno
						<strong>{alumnoToDelete.nombre} {alumnoToDelete.apellidos}</strong>? Esta acción no se
						puede deshacer.
					</p>
				</div>
				<div class="flex justify-center space-x-4 px-4 py-3">
					<button
						onclick={(e) => {
							e.preventDefault();
							cancelDelete();
						}}
						class="rounded-md bg-gray-500 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-gray-600"
					>
						Cancelar
					</button>
					<button
						onclick={(e) => {
							e.preventDefault();
							confirmDelete();
						}}
						class="rounded-md bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700"
					>
						Eliminar
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}
