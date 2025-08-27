<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { MaterialService } from '$lib/services/materialService';
	import ErrorDisplay from '$lib/components/common/ErrorDisplay.svelte';
	import type { MaterialStats } from '$lib/generated/api/models/MaterialStats';

	let stats: MaterialStats | null = null;
	let loading = true;
	let error: string | null = null;

	async function loadStats() {
		try {
			loading = true;
			error = null;
			stats = await MaterialService.getMaterialStats();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Error loading material statistics';
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		loadStats();
	});
</script>

<svelte:head>
	<title>Material Statistics - Academia App</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-6">
		<button
			on:click={() => goto('/materiales')}
			class="mb-4 inline-flex items-center text-sm text-blue-600 hover:text-blue-800"
		>
			← Back to Materials
		</button>
		<h1 class="text-3xl font-bold text-gray-900">Material Statistics</h1>
		<p class="mt-2 text-gray-600">Overview of educational materials in the system.</p>
	</div>

	{#if error}
		<ErrorDisplay error={{ type: 'error', message: error, title: 'Error' }} />
	{/if}

	{#if loading}
		<div class="py-12 text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
			></div>
			<p class="mt-4 text-gray-600">Loading statistics...</p>
		</div>
	{:else if stats}
		<div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
			<!-- Total Materials -->
			<div class="overflow-hidden rounded-lg bg-white shadow">
				<div class="p-5">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-md bg-blue-500">
								<span class="text-lg text-white">📚</span>
							</div>
						</div>
						<div class="ml-5 w-0 flex-1">
							<dl>
								<dt class="truncate text-sm font-medium text-gray-500">Total Materials</dt>
								<dd class="text-lg font-medium text-gray-900">
									{stats.totalMaterials || 0}
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>

			<!-- Documents -->
			<div class="overflow-hidden rounded-lg bg-white shadow">
				<div class="p-5">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-md bg-green-500">
								<span class="text-lg text-white">📄</span>
							</div>
						</div>
						<div class="ml-5 w-0 flex-1">
							<dl>
								<dt class="truncate text-sm font-medium text-gray-500">Documents</dt>
								<dd class="text-lg font-medium text-gray-900">
									{stats.totalDocuments || 0}
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>

			<!-- Images -->
			<div class="overflow-hidden rounded-lg bg-white shadow">
				<div class="p-5">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-md bg-purple-500">
								<span class="text-lg text-white">🖼️</span>
							</div>
						</div>
						<div class="ml-5 w-0 flex-1">
							<dl>
								<dt class="truncate text-sm font-medium text-gray-500">Images</dt>
								<dd class="text-lg font-medium text-gray-900">
									{stats.totalImages || 0}
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>

			<!-- Videos -->
			<div class="overflow-hidden rounded-lg bg-white shadow">
				<div class="p-5">
					<div class="flex items-center">
						<div class="flex-shrink-0">
							<div class="flex h-8 w-8 items-center justify-center rounded-md bg-red-500">
								<span class="text-lg text-white">🎥</span>
							</div>
						</div>
						<div class="ml-5 w-0 flex-1">
							<dl>
								<dt class="truncate text-sm font-medium text-gray-500">Videos</dt>
								<dd class="text-lg font-medium text-gray-900">
									{stats.totalVideos || 0}
								</dd>
							</dl>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Chart Section -->
		<div class="mt-8 rounded-lg bg-white shadow">
			<div class="border-b border-gray-200 px-6 py-4">
				<h3 class="text-lg font-medium text-gray-900">Material Distribution</h3>
			</div>
			<div class="p-6">
				{#if stats.totalMaterials && stats.totalMaterials > 0}
					<div class="space-y-4">
						<!-- Documents -->
						<div class="flex items-center">
							<div class="w-16 flex-shrink-0">
								<span class="text-sm font-medium text-gray-900">Documents</span>
							</div>
							<div class="ml-4 flex-1">
								<div class="h-2 rounded-full bg-gray-200">
									<div
										class="h-2 rounded-full bg-green-500"
										style="width: {((stats.totalDocuments || 0) / stats.totalMaterials) * 100}%"
									></div>
								</div>
							</div>
							<div class="ml-4 w-16 text-right">
								<span class="text-sm font-medium text-gray-900">
									{stats.totalDocuments || 0}
								</span>
								<span class="text-sm text-gray-500">
									({(((stats.totalDocuments || 0) / stats.totalMaterials) * 100).toFixed(1)}%)
								</span>
							</div>
						</div>

						<!-- Images -->
						<div class="flex items-center">
							<div class="w-16 flex-shrink-0">
								<span class="text-sm font-medium text-gray-900">Images</span>
							</div>
							<div class="ml-4 flex-1">
								<div class="h-2 rounded-full bg-gray-200">
									<div
										class="h-2 rounded-full bg-purple-500"
										style="width: {((stats.totalImages || 0) / stats.totalMaterials) * 100}%"
									></div>
								</div>
							</div>
							<div class="ml-4 w-16 text-right">
								<span class="text-sm font-medium text-gray-900">
									{stats.totalImages || 0}
								</span>
								<span class="text-sm text-gray-500">
									({(((stats.totalImages || 0) / stats.totalMaterials) * 100).toFixed(1)}%)
								</span>
							</div>
						</div>

						<!-- Videos -->
						<div class="flex items-center">
							<div class="w-16 flex-shrink-0">
								<span class="text-sm font-medium text-gray-900">Videos</span>
							</div>
							<div class="ml-4 flex-1">
								<div class="h-2 rounded-full bg-gray-200">
									<div
										class="h-2 rounded-full bg-red-500"
										style="width: {((stats.totalVideos || 0) / stats.totalMaterials) * 100}%"
									></div>
								</div>
							</div>
							<div class="ml-4 w-16 text-right">
								<span class="text-sm font-medium text-gray-900">
									{stats.totalVideos || 0}
								</span>
								<span class="text-sm text-gray-500">
									({(((stats.totalVideos || 0) / stats.totalMaterials) * 100).toFixed(1)}%)
								</span>
							</div>
						</div>
					</div>
				{:else}
					<div class="py-8 text-center">
						<div class="mb-4 text-4xl text-gray-400">📊</div>
						<h3 class="mb-2 text-lg font-medium text-gray-900">No Materials Available</h3>
						<p class="text-gray-600">There are no materials in the system yet.</p>
						<button
							on:click={() => goto('/materiales/nuevo')}
							class="mt-4 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
						>
							Add First Material
						</button>
					</div>
				{/if}
			</div>
		</div>

		<!-- Quick Actions -->
		<div class="mt-8 flex justify-center space-x-4">
			<button
				on:click={() => goto('/materiales')}
				class="rounded-md bg-gray-600 px-4 py-2 text-white hover:bg-gray-700"
			>
				View All Materials
			</button>
			<button
				on:click={() => goto('/materiales/nuevo')}
				class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
			>
				Add New Material
			</button>
		</div>
	{:else}
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📊</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">No Statistics Available</h3>
			<p class="text-gray-600">Unable to load material statistics.</p>
		</div>
	{/if}
</div>
