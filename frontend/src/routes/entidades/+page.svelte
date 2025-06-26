<script lang="ts">
	import type { PageData } from './$types';
	import { entidadService, type Entidad } from '$lib/services/entidadService';

	let { data }: { data: PageData } = $props();

	// Reactive state for entities
	let entities = $state<Entidad[]>(data.entidades || []);
	let newEntityInfo = $state('');
	let deleteId = $state('');
	let isLoading = $state(false);
	let message = $state('');

	// Auto-clear message after 5 seconds
	$effect(() => {
		if (message) {
			const timer = setTimeout(() => {
				message = '';
			}, 5000);
			return () => clearTimeout(timer);
		}
	});

	async function refreshEntities() {
		try {
			const refreshedEntities = await entidadService.getAllEntities();
			entities = refreshedEntities;
		} catch (error) {
			console.error('Error refreshing entities:', error);
		}
	}

	async function createEntity() {
		if (!newEntityInfo.trim()) return;

		isLoading = true;
		message = '';

		const result = await entidadService.createEntity(newEntityInfo);

		if (result.success) {
			message = result.message;
			newEntityInfo = '';
			// Refresh entities list without page reload
			await refreshEntities();
		} else {
			message = result.message;
		}

		isLoading = false;
	}

	async function deleteEntity(id?: number) {
		const entityId = id || parseInt(deleteId);
		if (!entityId) return;

		isLoading = true;
		message = '';

		// Optimistic update - remove from UI immediately
		const originalEntities = entities;
		entities = entities.filter((e) => e.id !== entityId);

		const result = await entidadService.deleteEntity(entityId);

		if (result.success) {
			message = result.message;
			deleteId = '';
			// Keep the optimistic update
		} else {
			message = result.message;
			// Revert optimistic update on failure
			entities = originalEntities;
		}

		isLoading = false;
	}

	async function deleteAllEntities() {
		if (!confirm('Are you sure you want to delete ALL entities? This cannot be undone.')) {
			return;
		}

		isLoading = true;
		message = '';

		// Optimistic update - clear UI immediately
		const originalEntities = entities;
		entities = [];

		const result = await entidadService.deleteAllEntities();

		if (result.success) {
			message = result.message;
			// Keep the optimistic update
		} else {
			message = result.message;
			// Revert optimistic update on failure
			entities = originalEntities;
		}

		isLoading = false;
	}

	async function manualRefresh() {
		isLoading = true;
		await refreshEntities();
		message = 'Data refreshed!';
		isLoading = false;
	}
</script>

<div class="container mx-auto max-w-4xl p-6">
	<h1 class="mb-8 text-center text-4xl font-bold text-gray-800">Entidades Management</h1>

	<!-- Create Entity Form -->
	<div class="mb-8 rounded-lg bg-white p-6 shadow-lg">
		<h2 class="mb-4 text-2xl font-semibold text-gray-700">Create New Entity</h2>
		<div class="flex gap-4">
			<input
				bind:value={newEntityInfo}
				placeholder="Enter entity info..."
				class="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:ring-2 focus:ring-blue-500"
				disabled={isLoading}
				onkeydown={(e) => e.key === 'Enter' && createEntity()}
			/>
			<button
				onclick={createEntity}
				disabled={isLoading || !newEntityInfo.trim()}
				class="rounded-lg bg-green-600 px-6 py-2 text-white transition-colors hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				{isLoading ? 'Creating...' : 'Create'}
			</button>
		</div>
	</div>

	<!-- Delete Actions -->
	<div class="mb-8 rounded-lg bg-white p-6 shadow-lg">
		<h2 class="mb-4 text-2xl font-semibold text-gray-700">Delete Actions</h2>

		<!-- Delete by ID -->
		<div class="mb-4 flex gap-4">
			<input
				bind:value={deleteId}
				placeholder="Enter entity ID to delete..."
				type="number"
				class="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:ring-2 focus:ring-red-500"
				disabled={isLoading}
				onkeydown={(e) => e.key === 'Enter' && deleteEntity()}
			/>
			<button
				onclick={() => deleteEntity()}
				disabled={isLoading || !deleteId}
				class="rounded-lg bg-red-600 px-6 py-2 text-white transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				{isLoading ? 'Deleting...' : 'Delete by ID'}
			</button>
		</div>

		<!-- Delete All -->
		<button
			onclick={deleteAllEntities}
			disabled={isLoading}
			class="w-full rounded-lg bg-red-800 px-6 py-2 text-white transition-colors hover:bg-red-900 disabled:cursor-not-allowed disabled:bg-gray-400"
		>
			{isLoading ? 'Deleting...' : 'Delete ALL Entities'}
		</button>
	</div>

	<!-- Entities Display -->
	<div class="rounded-lg bg-white p-6 shadow-lg">
		<div class="mb-6 flex items-center justify-between">
			<h2 class="text-2xl font-semibold text-gray-700">Current Entities</h2>
			<button
				onclick={manualRefresh}
				disabled={isLoading}
				class="rounded-lg bg-blue-600 px-4 py-2 text-white transition-colors hover:bg-blue-700 disabled:bg-gray-400"
			>
				{isLoading ? 'Refreshing...' : 'Refresh'}
			</button>
		</div>

		{#if entities && entities.length > 0}
			<div class="grid gap-4">
				{#each entities as entidad (entidad.id)}
					<div class="rounded-lg border border-gray-200 p-4 transition-shadow hover:shadow-md">
						<div class="flex items-center justify-between">
							<div>
								<span class="text-lg font-semibold text-gray-800">ID: {entidad.id}</span>
								<p class="mt-1 text-gray-600">{entidad.info}</p>
							</div>
							<button
								onclick={() => deleteEntity(entidad.id)}
								disabled={isLoading}
								class="rounded bg-red-500 px-3 py-1 text-sm text-white transition-colors hover:bg-red-600 disabled:bg-gray-400"
							>
								Delete
							</button>
						</div>
					</div>
				{/each}
			</div>

			<div class="mt-6 text-center text-gray-600">
				Total entities: {entities.length}
			</div>
		{:else if !data.error}
			<div class="py-8 text-center">
				<p class="text-lg text-gray-500">No entities found.</p>
				<p class="mt-2 text-sm text-gray-400">Create your first entity using the form above!</p>
			</div>
		{/if}
	</div>

	<!-- Message Display -->
	{#if message}
		<div
			class="mb-6 flex items-center justify-between rounded border border-blue-400 bg-blue-100 px-4 py-3 text-blue-700"
		>
			<span>{message}</span>
			<button onclick={() => (message = '')} class="text-blue-700 hover:text-blue-900">✕</button>
		</div>
	{/if}

	<!-- Error Display -->
	{#if data.error}
		<div class="mb-6 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			<strong>Error:</strong>
			{data.error}
		</div>
	{/if}
</div>
