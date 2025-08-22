<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	export let currentPage: number;
	export let totalPages: number;
	export let isLoading: boolean;

	const dispatch = createEventDispatcher();

	function handlePageChange(page: number) {
		if (page >= 1 && page <= totalPages && page !== currentPage) {
			dispatch('pageChange', page);
		}
	}

	function getPageNumbers() {
		const pages = [];
		const maxVisible = 5;

		if (totalPages <= maxVisible) {
			for (let i = 1; i <= totalPages; i++) {
				pages.push(i);
			}
		} else {
			if (currentPage <= 3) {
				for (let i = 1; i <= 4; i++) {
					pages.push(i);
				}
				pages.push('...');
				pages.push(totalPages);
			} else if (currentPage >= totalPages - 2) {
				pages.push(1);
				pages.push('...');
				for (let i = totalPages - 3; i <= totalPages; i++) {
					pages.push(i);
				}
			} else {
				pages.push(1);
				pages.push('...');
				for (let i = currentPage - 1; i <= currentPage + 1; i++) {
					pages.push(i);
				}
				pages.push('...');
				pages.push(totalPages);
			}
		}

		return pages;
	}
</script>

{#if totalPages > 1}
	<div
		class="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6"
	>
		<div class="flex flex-1 justify-between sm:hidden">
			<button
				on:click={() => handlePageChange(currentPage - 1)}
				disabled={currentPage === 1 || isLoading}
				class="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
			>
				Anterior
			</button>
			<button
				on:click={() => handlePageChange(currentPage + 1)}
				disabled={currentPage === totalPages || isLoading}
				class="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
			>
				Siguiente
			</button>
		</div>
		<div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
			<div>
				<p class="text-sm text-gray-700">
					Mostrando página <span class="font-medium">{currentPage}</span> de
					<span class="font-medium">{totalPages}</span>
				</p>
			</div>
			<div>
				<nav
					class="relative z-0 inline-flex -space-x-px rounded-md shadow-sm"
					aria-label="Pagination"
				>
					<button
						on:click={() => handlePageChange(currentPage - 1)}
						disabled={currentPage === 1 || isLoading}
						class="relative inline-flex items-center rounded-l-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
					>
						<span class="sr-only">Anterior</span>
						<svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
							<path
								fill-rule="evenodd"
								d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
								clip-rule="evenodd"
							/>
						</svg>
					</button>

					{#each getPageNumbers() as page (page)}
						{#if page === '...'}
							<span
								class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700"
							>
								...
							</span>
						{:else}
							<button
								on:click={() => handlePageChange(Number(page))}
								disabled={isLoading}
								class="relative inline-flex items-center border px-4 py-2 text-sm font-medium {page ===
								currentPage
									? 'z-10 border-indigo-500 bg-indigo-50 text-indigo-600'
									: 'border-gray-300 bg-white text-gray-500 hover:bg-gray-50'} disabled:cursor-not-allowed disabled:opacity-50"
							>
								{page}
							</button>
						{/if}
					{/each}

					<button
						on:click={() => handlePageChange(currentPage + 1)}
						disabled={currentPage === totalPages || isLoading}
						class="relative inline-flex items-center rounded-r-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
					>
						<span class="sr-only">Siguiente</span>
						<svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
							<path
								fill-rule="evenodd"
								d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
								clip-rule="evenodd"
							/>
						</svg>
					</button>
				</nav>
			</div>
		</div>
	</div>
{/if}
