<script lang="ts">
	import { goto } from '$app/navigation';

	let {
		searchTerm = '',
		onSearch,
		showCreateButton = false
	} = $props<{
		searchTerm?: string;
		onSearch: (term: string) => void;
		showCreateButton?: boolean;
	}>();

	let localSearchTerm = $state(searchTerm);

	function handleSearch() {
		onSearch(localSearchTerm);
	}

	function handleKeyPress(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleSearch();
		}
	}

	function handleCreateClass() {
		goto('/clases/nuevo');
	}
</script>

<div class="mb-6 flex flex-col gap-4 sm:flex-row">
	<div class="flex-1">
		<div class="relative">
			<div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
				<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
					/>
				</svg>
			</div>
			<input
				type="text"
				bind:value={localSearchTerm}
				onkeypress={handleKeyPress}
				placeholder="Buscar clases por título, descripción..."
				class="block w-full rounded-md border border-gray-300 bg-white py-2 pr-3 pl-10 leading-5 placeholder-gray-500 focus:border-blue-500 focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:outline-none sm:text-sm"
			/>
		</div>
	</div>

	<div class="flex gap-2">
		<button
			onclick={handleSearch}
			class="inline-flex items-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
		>
			<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
				/>
			</svg>
			Buscar
		</button>

		{#if showCreateButton}
			<button
				onclick={handleCreateClass}
				class="inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
			>
				<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 6v6m0 0v6m0-6h6m-6 0H6"
					/>
				</svg>
				Nueva Clase
			</button>
		{/if}
	</div>
</div>
