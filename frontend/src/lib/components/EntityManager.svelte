<script lang="ts">
	import { entidadService, type Entidad } from '$lib/services/entidadService';

	interface Props {
		initialEntities: Entidad[];
		error?: string;
	}

	let { initialEntities, error }: Props = $props();

	// Reactive state for entities
	let entities = $state<Entidad[]>(initialEntities || []);
	let newEntityInfo = $state('');
	let deleteId = $state('');
	let searchInfo = $state('');
	let isLoading = $state(false);
	let message = $state('');

	// Update form state
	let editingEntity = $state<Entidad | null>(null);
	let editInfo = $state('');

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

	async function searchEntities() {
		if (!searchInfo.trim()) {
			await refreshEntities();
			return;
		}

		isLoading = true;
		try {
			const searchResults = await entidadService.getEntitiesByInfo(searchInfo);
			entities = searchResults;
			message = `Found ${searchResults.length} entities matching "${searchInfo}"`;
		} catch (error) {
			console.error('Error searching entities:', error);
			message = 'Error searching entities';
		}
		isLoading = false;
	}

	async function clearSearch() {
		searchInfo = '';
		await refreshEntities();
		message = 'Search cleared';
	}

	async function createEntity() {
		if (!newEntityInfo.trim() || isLoading) return;

		isLoading = true;
		message = '';

		try {
			const result = await entidadService.createEntity(newEntityInfo);

			if (result.success) {
				message = result.message;
				newEntityInfo = '';
				// Refresh entities list without page reload
				await refreshEntities();
			} else {
				message = result.message;
			}
		} finally {
			isLoading = false;
		}
	}

	async function startEdit(entity: Entidad) {
		editingEntity = entity;
		editInfo = entity.info;
	}

	async function cancelEdit() {
		editingEntity = null;
		editInfo = '';
	}

	async function updateEntity() {
		if (!editingEntity || !editInfo.trim() || isLoading) return;

		isLoading = true;
		message = '';

		try {
			const result = await entidadService.updateEntity(editingEntity.id, editInfo);

			if (result.success) {
				message = result.message;
				editingEntity = null;
				editInfo = '';
				// Refresh entities list without page reload
				await refreshEntities();
			} else {
				message = result.message;
			}
		} finally {
			isLoading = false;
		}
	}

	async function deleteEntity(id?: number) {
		const entityId = id || parseInt(deleteId);
		if (!entityId || isLoading) return;

		isLoading = true;
		message = '';

		// Optimistic update - remove from UI immediately
		const originalEntities = entities;
		entities = entities.filter((e) => e.id !== entityId);

		try {
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
		} finally {
			isLoading = false;
		}
	}

	async function deleteAllEntities() {
		if (isLoading || !confirm('Are you sure you want to delete ALL entities? This cannot be undone.')) {
			return;
		}

		isLoading = true;
		message = '';

		// Optimistic update - clear UI immediately
		const originalEntities = entities;
		entities = [];

		try {
			const result = await entidadService.deleteAllEntities();

			if (result.success) {
				message = result.message;
				// Keep the optimistic update
			} else {
				message = result.message;
				// Revert optimistic update on failure
				entities = originalEntities;
			}
		} finally {
			isLoading = false;
		}
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

	<!-- Search/Filter Section -->
	<div class="mb-8 rounded-lg bg-white p-6 shadow-lg">
		<h2 class="mb-4 text-2xl font-semibold text-gray-700">Search Entities</h2>
		<div class="flex gap-4">
			<input
				bind:value={searchInfo}
				placeholder="Search by info..."
				class="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:ring-2 focus:ring-blue-500"
				disabled={isLoading}
				onkeydown={(e) => e.key === 'Enter' && searchEntities()}
			/>
			<button
				onclick={searchEntities}
				disabled={isLoading}
				class="rounded-lg bg-blue-600 px-6 py-2 text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				{isLoading ? 'Searching...' : 'Search'}
			</button>
			<button
				onclick={clearSearch}
				disabled={isLoading}
				class="rounded-lg bg-gray-600 px-6 py-2 text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				Clear
			</button>
		</div>
	</div>

	<!-- Create Entity Form -->
	<div class="mb-8 rounded-lg bg-white p-6 shadow-lg">
		<h2 class="mb-4 text-2xl font-semibold text-gray-700">Create New Entity</h2>
		<div class="flex gap-4">
			<input
				bind:value={newEntityInfo}
				placeholder="Enter entity info..."
				class="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:ring-2 focus:ring-green-500"
				onkeydown={(e) => e.key === 'Enter' && createEntity()}
				disabled={isLoading}
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

	<!-- Update Entity Form (shows when editing) -->
	{#if editingEntity}
		<div class="mb-8 rounded-lg bg-blue-50 p-6 shadow-lg border-2 border-blue-200">
			<h2 class="mb-4 text-2xl font-semibold text-blue-700">Update Entity #{editingEntity.id}</h2>
			<div class="flex gap-4">
				<input
					bind:value={editInfo}
					placeholder="Enter new entity info..."
					class="flex-1 rounded-lg border border-blue-300 px-4 py-2 focus:border-transparent focus:ring-2 focus:ring-blue-500"
					disabled={isLoading}
					onkeydown={(e) => e.key === 'Enter' && updateEntity()}
				/>
				<button
					onclick={updateEntity}
					disabled={isLoading || !editInfo.trim()}
					class="rounded-lg bg-blue-600 px-6 py-2 text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-400"
				>
					{isLoading ? 'Updating...' : 'Update'}
				</button>
				<button
					onclick={cancelEdit}
					disabled={isLoading}
					class="rounded-lg bg-gray-600 px-6 py-2 text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:bg-gray-400"
				>
					Cancel
				</button>
			</div>
		</div>
	{/if}

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
							<div class="flex-1">
								<span class="text-lg font-semibold text-gray-800">ID: {entidad.id}</span>
								<p class="mt-1 text-gray-600">{entidad.info}</p>
							</div>
							<div class="flex gap-2">
								<button
									onclick={() => startEdit(entidad)}
									disabled={isLoading || editingEntity?.id === entidad.id}
									class="rounded bg-blue-500 px-3 py-1 text-sm text-white transition-colors hover:bg-blue-600 disabled:bg-gray-400"
								>
									{editingEntity?.id === entidad.id ? 'Editing...' : 'Edit'}
								</button>
								<button
									onclick={() => deleteEntity(entidad.id)}
									disabled={isLoading}
									class="rounded bg-red-500 px-3 py-1 text-sm text-white transition-colors hover:bg-red-600 disabled:bg-gray-400"
								>
									Delete
								</button>
							</div>
						</div>
					</div>
				{/each}
			</div>

			<div class="mt-6 text-center text-gray-600">
				Total entities: {entities.length}
			</div>
		{:else if !error}
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
	{#if error}
		<div class="mb-6 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			<strong>Error:</strong>
			{error}
		</div>
	{/if}
</div> 