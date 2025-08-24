<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { MaterialService } from '$lib/services/materialService';
	import { getSearchConfig } from '$lib/components/common/searchConfigs';
	import EntityDataTable from '$lib/components/common/EntityDataTable.svelte';
	import EntitySearchSection from '$lib/components/common/EntitySearchSection.svelte';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import type { DTOMaterial } from '$lib/generated/api';
	import type { EntityColumn, EntityAction } from '$lib/components/common/types';

	let materials: DTOMaterial[] = [];
	let loading = true;
	let error: string | null = null;
	let totalElements = 0;
	let currentPage = 0;
	let pageSize = 20;
	let sortBy = 'id';
	let sortDirection = 'ASC';

	// Search parameters
	let searchParams = {
		searchMode: 'simple' as const,
		q: '',
		name: '',
		url: '',
		type: ''
	};

	// Table configuration
	const materialSearchConfig = getSearchConfig('materiales');

	const columns: EntityColumn<DTOMaterial>[] = [
		{
			key: 'name',
			header: 'Name',
			sortable: true,
			formatter: (value) => value as string
		},
		{
			key: 'url',
			header: 'URL',
			sortable: true,
			formatter: (value) => {
				const url = value as string;
				return url.length > 50 ? url.substring(0, 50) + '...' : url;
			}
		},
		{
			key: 'materialType',
			header: 'Type',
			sortable: true,
			formatter: (value, entity) => {
				return MaterialService.getMaterialTypeLabel(entity);
			}
		},
		{
			key: 'id',
			header: 'Actions',
			sortable: false,
			cell: (entity) => {
				const icon = MaterialService.getMaterialIcon(entity);
				return `<span class="text-2xl">${icon}</span>`;
			},
			html: true
		}
	];

	const actions: EntityAction<DTOMaterial>[] = [
		{
			label: 'Edit',
			icon: '✏️',
			color: 'blue',
			hoverColor: 'indigo',
			action: handleEdit
		},
		{
			label: 'Delete',
			icon: '🗑️',
			color: 'red',
			hoverColor: 'rose',
			action: handleDelete
		}
	];

	$: urlParams = $page.url.searchParams;
	$: currentPage = parseInt(urlParams.get('page') || '0');
	$: pageSize = parseInt(urlParams.get('size') || '20');
	$: sortBy = urlParams.get('sortBy') || 'id';
	$: sortDirection = urlParams.get('sortDirection') || 'ASC';
	$: searchParams.q = urlParams.get('q') || '';
	$: searchParams.name = urlParams.get('name') || '';
	$: searchParams.url = urlParams.get('url') || '';
	$: searchParams.type = urlParams.get('type') || '';

	async function loadMaterials() {
		try {
			loading = true;
			error = null;

			const response = await MaterialService.getMaterials({
				...searchParams,
				page: currentPage,
				size: pageSize,
				sortBy,
				sortDirection
			});

			materials = response.content || [];
			totalElements = response.totalElements || 0;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading materials';
		} finally {
			loading = false;
		}
	}

	function handleSearch(newParams: typeof searchParams) {
		searchParams = { ...newParams };
		currentPage = 0;
		updateUrl();
	}

	function handleSort(field: string, direction: string) {
		sortBy = field;
		sortDirection = direction;
		updateUrl();
	}

	function updateUrl() {
		const params = new URLSearchParams();

		if (currentPage > 0) params.set('page', currentPage.toString());
		if (pageSize !== 20) params.set('size', pageSize.toString());
		if (sortBy !== 'id') params.set('sortBy', sortBy);
		if (sortDirection !== 'ASC') params.set('sortDirection', sortDirection);

		if (searchParams.q) params.set('q', searchParams.q);
		if (searchParams.name) params.set('name', searchParams.name);
		if (searchParams.url) params.set('url', searchParams.url);
		if (searchParams.type) params.set('type', searchParams.type);

		const queryString = params.toString();
		goto(`/materiales${queryString ? `?${queryString}` : ''}`);
	}

	function handleEdit(material: DTOMaterial) {
		goto(`/materiales/${material.id}`);
	}

	function handleDelete(material: DTOMaterial) {
		// This will be handled by the EntityDataTable component
		return MaterialService.deleteMaterial(material.id!);
	}

	function handleCreate() {
		goto('/materiales/nuevo');
	}

	$: if ($page.url.searchParams.toString() !== '') {
		loadMaterials();
	}

	onMount(() => {
		loadMaterials();
	});
</script>

<svelte:head>
	<title>Materials - Academia App</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Materials</h1>
		<button
			on:click={handleCreate}
			class="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-white transition-colors hover:bg-blue-700"
		>
			<span>➕</span>
			Add Material
		</button>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	<EntitySearchSection
		currentFilters={searchParams}
		entityNamePlural="materiales"
		advancedFields={materialSearchConfig.advancedFields}
		entityType="materiales"
		on:updateFilters={(event) => handleSearch(event.detail)}
		on:clearFilters={() =>
			handleSearch({ searchMode: 'simple', q: '', name: '', url: '', type: '' })}
	/>

	<EntityDataTable
		{loading}
		paginatedData={{
			content: materials as Record<string, unknown>[],
			page: {
				number: currentPage,
				size: pageSize,
				totalElements,
				totalPages: Math.ceil(totalElements / pageSize),
				first: currentPage === 0,
				last: currentPage >= Math.ceil(totalElements / pageSize) - 1,
				hasNext: currentPage < Math.ceil(totalElements / pageSize) - 1,
				hasPrevious: currentPage > 0
			}
		}}
		currentPagination={{
			page: currentPage,
			size: pageSize,
			sortBy,
			sortDirection: sortDirection as 'ASC' | 'DESC'
		}}
		{columns}
		entityName="material"
		entityNamePlural="materiales"
		{actions}
		on:changeSorting={(event) => handleSort(event.detail, sortDirection === 'ASC' ? 'DESC' : 'ASC')}
	/>
</div>
