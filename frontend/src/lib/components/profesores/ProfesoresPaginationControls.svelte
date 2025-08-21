<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type { PageData } from '../../../routes/profesores/$types';
	import type { PageDisplayInfo } from '$lib/types/pagination';

	const dispatch = createEventDispatcher();

	let {
		pageDisplayInfo,
		currentPagination,
		sortFields,
		pageSizeOptions
	}: {
		pageDisplayInfo: PageDisplayInfo | null;
		currentPagination: PageData['pagination'];
		sortFields: { value: string; label: string }[];
		pageSizeOptions: number[];
	} = $props();

	function onPageChange(e: Event) {
		const target = e.target as HTMLInputElement;
		dispatch('goToPage', Number(target.value));
	}
</script>

{#if pageDisplayInfo}
	<div class="flex flex-wrap items-center justify-between gap-4">
		<div class="flex items-center gap-2">
			<button
				class="rounded-md bg-gray-200 px-3 py-1 text-sm disabled:opacity-50"
				disabled={pageDisplayInfo.isFirstPage}
				onclick={() => dispatch('goToPage', 0)}>Primera</button
			>
			<button
				class="rounded-md bg-gray-200 px-3 py-1 text-sm disabled:opacity-50"
				disabled={pageDisplayInfo.isFirstPage}
				onclick={() => dispatch('goToPage', currentPagination.page - 1)}
			>
				Anterior
			</button>
			<span class="text-sm">
				Página
				<input
					type="number"
					value={currentPagination.page + 1}
					onchange={onPageChange}
					class="w-16 rounded-md border-gray-300 text-center"
					min="1"
					max={pageDisplayInfo.totalPages}
				/>
				de {pageDisplayInfo.totalPages}
			</span>
			<button
				class="rounded-md bg-gray-200 px-3 py-1 text-sm disabled:opacity-50"
				disabled={pageDisplayInfo.isLastPage}
				onclick={() => dispatch('goToPage', currentPagination.page + 1)}
			>
				Siguiente
			</button>
			<button
				class="rounded-md bg-gray-200 px-3 py-1 text-sm disabled:opacity-50"
				disabled={pageDisplayInfo.isLastPage}
				onclick={() => dispatch('goToPage', pageDisplayInfo.totalPages - 1)}>Última</button
			>
		</div>

		<div class="flex items-center gap-4">
			<select
				class="rounded-md border-gray-300 text-sm"
				value={currentPagination.sortBy}
				onchange={(e) => dispatch('changeSorting', (e.target as HTMLSelectElement).value)}
			>
				{#each sortFields as field (field.value)}
					<option value={field.value}>{field.label}</option>
				{/each}
			</select>
			<select
				class="rounded-md border-gray-300 text-sm"
				value={currentPagination.size}
				onchange={(e) => dispatch('changePageSize', Number((e.target as HTMLSelectElement).value))}
			>
				{#each pageSizeOptions as size (size)}
					<option value={size}>Mostrar {size}</option>
				{/each}
			</select>
		</div>
	</div>
{/if}
