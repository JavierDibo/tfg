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
	export let paginatedData: PaginatedEntities<any> | null = null;
	export let currentPagination: EntityPagination;
	export const authStore: AuthStoreType = undefined as unknown as AuthStoreType;
	export let columns: EntityColumn<any>[] = [];
	export const entityName: string = 'elemento';
	export let entityNamePlural: string = 'elementos';
	export let actions: EntityAction<any>[] = [];
	export let showMobileView: boolean = true;

	function formatValue(entity: any, column: EntityColumn<any>): string {
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

	function formatDate(date: Date | string | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES');
	}

	function handleSort(field: string) {
		dispatch('changeSorting', field);
	}

	function triggerAction(action: EntityAction<any>, entity: any) {
		action.action(entity);
	}

	// Generate skeleton rows based on current page size
	$: skeletonCount = currentPagination?.size || 5;
</script>

<div class="overflow-hidden rounded-lg bg-white shadow-md">
	{#if loading && !paginatedData?.content}
		<!-- Initial loading state -->
		<div class="py-8 text-center">
			<div class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-500"></div>
			<p class="mt-2 text-gray-600">Cargando {entityNamePlural}...</p>
		</div>
	{:else if !paginatedData?.content || paginatedData.content.length === 0}
		<div class="py-8 text-center">
			<p class="text-gray-500">
				No se encontraron {entityNamePlural} que coincidan con los filtros.
			</p>
		</div>
	{:else}
		<!-- Desktop Table -->
		<div class="hidden overflow-x-auto lg:block">
			<table class="min-w-full divide-y divide-gray-200">
				<thead class="bg-gray-50">
					<tr>
						{#each columns as column (column.key)}
							<th
								class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase {column.class ||
									''}"
								style={column.width ? `width: ${column.width}` : ''}
							>
								{#if column.sortable}
									<button
										onclick={() => handleSort(column.key.toString())}
										class="flex items-center hover:text-gray-700"
									>
										{column.header}
										{#if currentPagination.sortBy === column.key}
											<span class="ml-1"
												>{currentPagination.sortDirection === 'ASC' ? '↑' : '↓'}</span
											>
										{/if}
									</button>
								{:else}
									{column.header}
								{/if}
							</th>
						{/each}
						{#if actions.length > 0}
							<th
								class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
							>
								Acciones
							</th>
						{/if}
					</tr>
				</thead>
				<tbody class="divide-y divide-gray-200 bg-white">
					<!-- Skeleton rows during loading actions -->
					{#if loading && paginatedData?.content}
						{#each Array(skeletonCount) as _, i (i)}
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
												<div class="h-4 w-16 rounded bg-gray-200"></div>
											{/each}
										</div>
									</td>
								{/if}
							</tr>
						{/each}
					{:else}
						{#each paginatedData.content as entity (entity.id)}
							<tr class="hover:bg-gray-50">
								{#each columns as column (column.key)}
									<td class="px-6 py-4 whitespace-nowrap {column.class || ''}">
										<span class="text-sm text-gray-900">
											{formatValue(entity, column)}
										</span>
									</td>
								{/each}
								{#if actions.length > 0}
									<td class="px-6 py-4 text-sm whitespace-nowrap text-gray-500">
										<div class="flex space-x-2">
											{#each actions as action (action.label)}
												{#if !action.condition || action.condition(entity)}
													<button
														onclick={(e) => {
															e.preventDefault();
															e.currentTarget.blur();
															triggerAction(action, entity);
														}}
														class="text-{action.color}-600 hover:text-{action.hoverColor}-900"
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
					{#each Array(skeletonCount) as _, i (i)}
						<div class="animate-pulse border-b border-gray-200 p-4">
							<div class="mb-2 flex items-start justify-between">
								<div class="space-y-2">
									<div class="h-5 w-32 rounded bg-gray-200"></div>
									<div class="h-4 w-20 rounded bg-gray-200"></div>
								</div>
								<div class="h-6 w-24 rounded-full bg-gray-200"></div>
							</div>
							<div class="space-y-2">
								<div class="h-4 w-full rounded bg-gray-200"></div>
								<div class="h-4 w-3/4 rounded bg-gray-200"></div>
								<div class="h-4 w-1/2 rounded bg-gray-200"></div>
							</div>
							<div class="mt-3 flex space-x-2">
								{#each actions as action (action.label)}
									<div class="h-4 w-16 rounded bg-gray-200"></div>
								{/each}
							</div>
						</div>
					{/each}
				{:else}
					{#each paginatedData.content as entity (entity.id)}
						<div class="border-b border-gray-200 p-4">
							<div class="mb-3">
								{#each columns as column (column.key)}
									{#if column.key === columns[0].key}
										<h3 class="font-medium text-gray-900">{formatValue(entity, column)}</h3>
									{/if}
								{/each}
							</div>
							<div class="space-y-2 text-sm text-gray-600">
								{#each columns as column, i (column.key)}
									{#if i !== 0}
										<p><strong>{column.header}:</strong> {formatValue(entity, column)}</p>
									{/if}
								{/each}
							</div>
							{#if actions.length > 0}
								<div class="mt-3 flex flex-wrap gap-2">
									{#each actions as action (action.label)}
										{#if !action.condition || action.condition(entity)}
											<button
												onclick={(e) => {
													e.preventDefault();
													e.currentTarget.blur();
													triggerAction(action, entity);
												}}
												class="text-sm text-{action.color}-600 hover:text-{action.hoverColor}-900"
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
