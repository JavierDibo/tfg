<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();
</script>

<div class="my-12 flex flex-col items-center justify-center text-center">
	<div class="mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-red-100 text-red-500">
		<svg
			xmlns="http://www.w3.org/2000/svg"
			class="h-10 w-10"
			fill="none"
			viewBox="0 0 24 24"
			stroke="currentColor"
		>
			<path
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
			/>
		</svg>
	</div>
	<h2 class="mb-2 text-xl font-semibold text-gray-900">Error al cargar estadísticas</h2>
	<p class="mb-4 text-gray-600">No se han podido cargar los datos estadísticos.</p>
	<button
		on:click={() => dispatch('retry')}
		class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
	>
		Reintentar
	</button>
</div>
