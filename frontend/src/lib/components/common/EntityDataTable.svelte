<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import type {
		EntityAction,
		EntityColumn,
		EntityPagination,
		PaginatedEntities,
		AuthStoreType
	} from './types';

	const dispatch = createEventDispatcher();

	export let loading: boolean = false;
	export let paginatedData: PaginatedEntities<Record<string, unknown>> | null = null;
	export let currentPagination: EntityPagination;
	export const authStore: AuthStoreType = undefined as unknown as AuthStoreType;
	export let columns: EntityColumn<Record<string, unknown>>[] = [];
	export const entityName: string = 'elemento';
	export let entityNamePlural: string = 'elementos';
	export let actions: EntityAction<Record<string, unknown>>[] = [];
	export let showMobileView: boolean = true;

	function formatValue(
		entity: Record<string, unknown>,
		column: EntityColumn<Record<string, unknown>>
	): string {
		if (column.formatter) {
			return column.formatter(entity[column.key], entity) || '-';
		}

		if (column.cell) {
			return column.cell(entity);
		}

		const value = entity[column.key];
		if (value === undefined || value === null) return '-';

		if (value instanceof Date) {
			return formatDate(value);
		}

		return String(value);
	}

	function shouldRenderAsHtml(column: EntityColumn<Record<string, unknown>>): boolean {
		return column.html === true && column.formatter !== undefined;
	}

	function formatDate(date: Date | string | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	function handleSort(field: string) {
		dispatch('changeSorting', field);
	}

	function triggerAction(
		action: EntityAction<Record<string, unknown>>,
		entity: Record<string, unknown>
	) {
		action.action(entity);
	}

	// Generate skeleton rows based on current page size
	$: skeletonCount = currentPagination?.size || 5;
</script>

<div class="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-lg">
	{#if loading && !paginatedData?.content}
		<!-- Initial loading state -->
		<div class="py-16 text-center">
			<div
				class="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600"
			></div>
			<p class="mt-4 text-lg font-medium text-gray-700">Cargando {entityNamePlural}...</p>
			<p class="mt-2 text-sm text-gray-500">Por favor espera un momento</p>
		</div>
	{:else if !paginatedData?.content || paginatedData.content.length === 0}
		<div class="py-16 text-center">
			<div class="mx-auto h-24 w-24 text-gray-300">
				<svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="1.5"
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
					/>
				</svg>
			</div>
			<h3 class="mt-4 text-lg font-medium text-gray-900">No se encontraron {entityNamePlural}</h3>
			<p class="mt-2 text-sm text-gray-500">
				No hay {entityNamePlural} que coincidan con los filtros actuales.
			</p>
		</div>
	{:else}
		<!-- Desktop Table -->
		<div class="hidden overflow-x-auto lg:block">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gradient-to-r from-gray-50 to-gray-100">
					<tr>
						{#each columns as column (column.key)}
							<th
								class="px-6 py-4 text-left text-xs font-semibold tracking-wider text-gray-700 uppercase {column.class ||
									''}"
								style={column.width ? `width: ${column.width}` : ''}
							>
								{#if column.sortable}
									<button
										onclick={() => handleSort(column.key.toString())}
										class="flex items-center space-x-1 transition-colors duration-200 hover:text-blue-600"
									>
										<span>{column.header}</span>
										{#if currentPagination.sortBy === column.key}
											<span class="text-blue-600">
												{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}
											</span>
										{:else}
											<span class="text-gray-400">↕</span>
										{/if}
									</button>
								{:else}
									{column.header}
								{/if}
							</th>
						{/each}
						{#if actions.length > 0}
							<th
								class="px-6 py-4 text-left text-xs font-semibold tracking-wider text-gray-700 uppercase"
							>
								Acciones
							</th>
						{/if}
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-100 bg-white">
					<!-- Skeleton rows during loading actions -->
					{#if loading && paginatedData?.content}
						{#each Array(skeletonCount), i (i)}
							<tr class="animate-pulse">
								{#each columns as column (column.key)}
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="h-4 w-24 rounded bg-gray-200"></div>
									</td>
								{/each}
								{#if actions.length > 0}
									<td class="px-6 py-4 whitespace-nowrap">
										<div class="flex space-x-2">
											{#each actions as action (action.label)}
												<div class="h-6 w-16 rounded bg-gray-200"></div>
											{/each}
										</div>
									</td>
								{/if}
							</tr>
						{/each}
					{:else}
						{#each paginatedData.content as entity, index (entity.id)}
							<tr
								class="transition-all duration-200 hover:bg-gradient-to-r hover:from-blue-50 hover:to-indigo-50 {index %
									2 ===
								0
									? 'bg-white'
									: 'bg-gray-50/30'}"
							>
								{#each columns as column (column.key)}
									<td class="px-6 py-4 whitespace-nowrap {column.class || ''}">
										{#if shouldRenderAsHtml(column)}
											<span class="text-sm text-gray-900">
												<!-- eslint-disable-next-line svelte/no-at-html-tags -->
												{@html formatValue(entity, column)}
											</span>
										{:else}
											<span class="text-sm text-gray-900">
												{formatValue(entity, column)}
											</span>
										{/if}
									</td>
								{/each}
								{#if actions.length > 0}
									<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500">
										<div class="flex space-x-3">
											{#each actions as action (action.label)}
												{#if !action.condition || action.condition(entity)}
													<button
														onclick={(e) => {
															e.preventDefault();
															e.currentTarget.blur();
															triggerAction(action, entity);
														}}
														class="inline-flex items-center rounded-md px-3 py-1.5 text-xs font-medium transition-all duration-200 text-{action.color}-700 bg-{action.color}-50 hover:bg-{action.hoverColor}-100 hover:text-{action.hoverColor}-800 focus:ring-2 focus:outline-none focus:ring-{action.color}-500 focus:ring-offset-2"
														title={action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
													>
														{action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
													</button>
												{/if}
											{/each}
										</div>
									</td>
								{/if}
							</tr>
						{/each}
					{/if}
				</tbody>
			</table>
		</div>

		<!-- Mobile View -->
		{#if showMobileView}
			<div class="lg:hidden">
				<!-- Skeleton cards during loading actions -->
				{#if loading && paginatedData?.content}
					{#each Array(skeletonCount), i (i)}
						<!-- eslint-disable-next-line @typescript-eslint/no-unused-vars -->
						<div class="animate-pulse border-b border-gray-200 p-6">
							<div class="mb-4 flex items-start justify-between">
								<div class="space-y-3">
									<div class="h-6 w-40 rounded bg-gray-200"></div>
									<div class="h-4 w-32 rounded bg-gray-200"></div>
								</div>
								<div class="h-8 w-24 rounded-full bg-gray-200"></div>
							</div>
							<div class="space-y-3">
								<div class="h-4 w-full rounded bg-gray-200"></div>
								<div class="h-4 w-3/4 rounded bg-gray-200"></div>
								<div class="h-4 w-1/2 rounded bg-gray-200"></div>
							</div>
							<div class="mt-4 flex space-x-3">
								{#each actions as action (action.label)}
									<div class="h-8 w-20 rounded bg-gray-200"></div>
								{/each}
							</div>
						</div>
					{/each}
				{:else}
					{#each paginatedData.content as entity, index (entity.id)}
						<div
							class="border-b border-gray-100 p-6 transition-colors duration-200 hover:bg-gray-50 {index %
								2 ===
							0
								? 'bg-white'
								: 'bg-gray-50/30'}"
						>
							<div class="mb-4">
								{#each columns as column (column.key)}
									{#if column.key === columns[0].key}
										<h3 class="text-lg font-semibold text-gray-900">
											{formatValue(entity, column)}
										</h3>
									{/if}
								{/each}
							</div>
							<div class="space-y-3 text-sm text-gray-600">
								{#each columns as column, i (column.key)}
									{#if i !== 0}
										<div class="flex items-center justify-between">
											<span class="font-medium text-gray-700">{column.header}:</span>
											<div class="text-right">
												{#if shouldRenderAsHtml(column)}
													<!-- eslint-disable-next-line svelte/no-at-html-tags -->
													{@html formatValue(entity, column)}
												{:else}
													{formatValue(entity, column)}
												{/if}
											</div>
										</div>
									{/if}
								{/each}
							</div>
							{#if actions.length > 0}
								<div class="mt-6 flex flex-wrap gap-2">
									{#each actions as action (action.label)}
										{#if !action.condition || action.condition(entity)}
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													triggerAction(action, entity);
												}}
												class="inline-flex items-center rounded-md px-3 py-2 text-sm font-medium transition-all duration-200 text-{action.color}-700 bg-{action.color}-50 hover:bg-{action.hoverColor}-100 hover:text-{action.hoverColor}-800 focus:ring-2 focus:outline-none focus:ring-{action.color}-500 focus:ring-offset-2"
											>
												{action.dynamicLabel ? action.dynamicLabel(entity) : action.label}
											</button>
										{/if}
									{/each}
								</div>
							{/if}
						</div>
					{/each}
				{/if}
			</div>
		{/if}
	{/if}
</div>
