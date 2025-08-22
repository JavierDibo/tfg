<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PageDisplayInfo } from '$lib/types/pagination';
	import type { EntityPagination } from '$lib/components/common/types';

	export let pageDisplayInfo: PageDisplayInfo;
	export let currentPagination: EntityPagination;
	export let sortFields: Array<{ value: string; label: string }>;
	export let pageSizeOptions: number[];

	const dispatch = createEventDispatcher();

	let pageInput = '';

	function goToPage(page: number) {
		dispatch('goToPage', page);
	}

	function handlePageInputSubmit() {
		const pageNumber = parseInt(pageInput);
		if (pageNumber >= 1 && pageNumber <= pageDisplayInfo.totalPages) {
			goToPage(pageNumber);
			pageInput = '';
		}
	}

	function handlePageInputKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handlePageInputSubmit();
		}
	}

	function changePageSize(size: number) {
		dispatch('changePageSize', size);
	}

	function changeSorting(sortBy: string) {
		dispatch('changeSorting', sortBy);
	}
</script>

{#if pageDisplayInfo}
	<div class="mb-6 rounded-lg bg-white p-4 shadow-md">
		<div class="flex flex-col gap-4">
			<!-- Controls Row -->
			<div class="flex flex-col items-center justify-between gap-4 sm:flex-row">
				<!-- Page Size Selector -->
				<div class="flex items-center gap-2">
					<label for="pageSize" class="text-sm font-medium text-gray-700"
						>Elementos por página:</label
					>
					<select
						id="pageSize"
						value={currentPagination.size}
						onchange={(e) => changePageSize(parseInt((e.target as HTMLSelectElement).value))}
						class="rounded-md border border-gray-300 px-3 py-1 focus:ring-2 focus:ring-blue-500 focus:outline-none"
					>
						{#each pageSizeOptions as size (size)}
							<option value={size}>{size}</option>
						{/each}
					</select>
				</div>

				<!-- Sort Controls -->
				<div class="flex items-center gap-2">
					<label for="sortField" class="text-sm font-medium text-gray-700">Ordenar por:</label>
					<select
						id="sortField"
						value={currentPagination.sortBy}
						onchange={(e) => changeSorting((e.target as HTMLSelectElement).value)}
						class="rounded-md border border-gray-300 px-3 py-1 focus:ring-2 focus:ring-blue-500 focus:outline-none"
					>
						{#each sortFields as field (field.value)}
							<option value={field.value}>{field.label}</option>
						{/each}
					</select>
					<button
						onclick={() => changeSorting(currentPagination.sortBy)}
						class="rounded-md border border-gray-300 px-2 py-1 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						title="Cambiar dirección de ordenamiento"
					>
						{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}
					</button>
				</div>

				<!-- Results Summary -->
				<div class="text-sm text-gray-600">
					Mostrando {pageDisplayInfo.startItem}-{pageDisplayInfo.endItem} de {pageDisplayInfo.totalItems}
					alumnos
				</div>
			</div>
		</div>
	</div>

	<!-- Page Navigation - Centered -->
	{#if pageDisplayInfo.totalPages > 1}
		<div class="flex justify-center">
			<div class="flex items-center space-x-2">
				<button
					onclick={() => goToPage(pageDisplayInfo.currentPage - 1)}
					disabled={!pageDisplayInfo.hasPrevious}
					class="rounded-md border px-3 py-2 text-sm {!pageDisplayInfo.hasPrevious
						? 'cursor-not-allowed bg-gray-100 text-gray-400'
						: 'bg-white text-gray-700 hover:bg-gray-50'}"
				>
					Anterior
				</button>

				<!-- Page numbers -->
				{#each Array.from({ length: Math.min(10, pageDisplayInfo.totalPages) }, (_, i) => {
					const start = Math.max(1, pageDisplayInfo.currentPage - 5);
					return start + i;
				}).filter((page) => page <= pageDisplayInfo.totalPages) as page (page)}
					<button
						onclick={() => goToPage(page)}
						class="rounded-md border px-3 py-2 text-sm {page === pageDisplayInfo.currentPage
							? 'bg-blue-600 text-white'
							: 'bg-white text-gray-700 hover:bg-gray-50'}"
					>
						{page}
					</button>
				{/each}

				<button
					onclick={() => goToPage(pageDisplayInfo.currentPage + 1)}
					disabled={!pageDisplayInfo.hasNext}
					class="rounded-md border px-3 py-2 text-sm {!pageDisplayInfo.hasNext
						? 'cursor-not-allowed bg-gray-100 text-gray-400'
						: 'bg-white text-gray-700 hover:bg-gray-50'}"
				>
					Siguiente
				</button>
			</div>
		</div>

		<!-- Page Jump Input - Centered -->
		<div class="mt-3 flex justify-center">
			<div class="flex items-center gap-2">
				<label for="pageInput" class="text-sm font-medium text-gray-700">Ir a página:</label>
				<input
					id="pageInput"
					type="number"
					bind:value={pageInput}
					onkeydown={handlePageInputKeydown}
					min="1"
					max={pageDisplayInfo.totalPages}
					placeholder="Ej: 27"
					autocomplete="off"
					inputmode="numeric"
					class="w-20 rounded-md border border-gray-300 px-2 py-1 text-center text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
				/>
				<button
					onclick={handlePageInputSubmit}
					disabled={!pageInput ||
						parseInt(pageInput) < 1 ||
						parseInt(pageInput) > pageDisplayInfo.totalPages}
					class="rounded-md border px-3 py-1 text-sm {!pageInput ||
					parseInt(pageInput) < 1 ||
					parseInt(pageInput) > pageDisplayInfo.totalPages
						? 'cursor-not-allowed bg-gray-100 text-gray-400'
						: 'bg-blue-600 text-white hover:bg-blue-700'}"
				>
					Ir
				</button>
			</div>
		</div>

		<!-- Page info - Centered -->
		<div class="mt-2 text-center">
			<div class="text-sm text-gray-600">
				Página {pageDisplayInfo.currentPage} de {pageDisplayInfo.totalPages}
			</div>
		</div>
	{/if}
{/if}
