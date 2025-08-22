<script lang="ts">
	import { onMount } from 'svelte';
	import { type DTOEntidad, entidadApi } from '@/api';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';

	// Check authentication and permissions on component mount
	if (browser && !authStore.isAuthenticated) {
		goto('/auth');
	}

	if (browser && !authStore.isAdmin) {
		goto('/');
	}

	// State variables
	let entidades = $state<DTOEntidad[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let message = $state<string | null>(null);

	// Form state for creating/editing entities
	let showCreateForm = $state(false);
	let showEditForm = $state(false);
	let currentEntity = $state<DTOEntidad>({ info: '', otraInfo: '' });
	let editingId = $state<number | null>(null);

	// Search filters
	let searchInfo = $state('');
	let searchOtraInfo = $state('');

	// Load all entities on component mount
	onMount(() => {
		if (authStore.isAuthenticated && authStore.isAdmin) {
			loadEntities();
		}
	});

	// Load entities with optional filters
	async function loadEntities() {
		loading = true;
		error = null;
		try {
			// Use the generated API client to fetch entities with optional filters
			entidades = await entidadApi.obtenerEntidades({
				info: searchInfo,
				otraInfo: searchOtraInfo
			});
		} catch (err: unknown) {
			error = `Error loading entities: ${(err as Error).message}`;
			console.error('Error loading entities:', err);
		} finally {
			loading = false;
		}
	}

	// Create a new entity
	async function createEntity() {
		loading = true;
		error = null;
		try {
			const newEntity = await entidadApi.crearEntidad({
				dTOEntidad: currentEntity
			});
			entidades.push(newEntity);
			entidades = entidades;
			message = 'Entity created successfully!';
			resetForm();
		} catch (err: unknown) {
			error = `Error creating entity: ${(err as Error).message}`;
			console.error('Error creating entity:', err);
		} finally {
			loading = false;
		}
	}

	// Update an existing entity
	async function updateEntity() {
		if (editingId === null) return;
		loading = true;
		error = null;
		try {
			const updatedEntity = await entidadApi.actualizarEntidad({
				id: editingId,
				dTOEntidad: currentEntity
			});
			entidades = entidades.map((e) => (e.id === editingId ? updatedEntity : e));
			message = 'Entity updated successfully!';
			resetForm();
		} catch (err: unknown) {
			error = `Error updating entity: ${(err as Error).message}`;
			console.error('Error updating entity:', err);
		} finally {
			loading = false;
		}
	}

	// Delete a specific entity
	async function deleteEntity(id: number) {
		if (!confirm('Are you sure you want to delete this entity?')) return;
		loading = true;
		error = null;
		try {
			await entidadApi.borrarEntidadPorId({ id });
			entidades = entidades.filter((e) => e.id !== id);
			message = 'Entity deleted successfully!';
		} catch (err: unknown) {
			error = `Error deleting entity: ${(err as Error).message}`;
			console.error('Error deleting entity:', err);
		} finally {
			loading = false;
		}
	}

	// Delete all entities
	async function deleteAllEntities() {
		if (!confirm('Are you sure you want to delete ALL entities? This cannot be undone!')) return;
		loading = true;
		error = null;
		try {
			await entidadApi.borrarTodasLasEntidades();
			entidades = [];
			message = 'All entities deleted successfully!';
		} catch (err: unknown) {
			error = `Error deleting all entities: ${(err as Error).message}`;
			console.error('Error deleting all entities:', err);
		} finally {
			loading = false;
		}
	}

	// Get a specific entity by ID
	async function getEntityById(id: number) {
		loading = true;
		error = null;
		try {
			const entity = await entidadApi.obtenerEntidadPorId({ id });
			const entityElement = document.getElementById(`entity-${id}`);
			if (entityElement) {
				entityElement.scrollIntoView({ behavior: 'smooth' });
				entityElement.classList.add('highlight');
				setTimeout(() => entityElement.classList.remove('highlight'), 2000);
			}
			message = `Found entity: ${entity.info || 'N/A'}`;
		} catch (err: unknown) {
			error = `Error getting entity: ${(err as Error).message}`;
			console.error('Error getting entity:', err);
		} finally {
			loading = false;
		}
	}

	// Form helpers
	function showCreateEntityForm() {
		currentEntity = { info: '', otraInfo: '' };
		showCreateForm = true;
		showEditForm = false;
		editingId = null;
	}

	function showEditEntityForm(entity: DTOEntidad) {
		currentEntity = { ...entity };
		showEditForm = true;
		showCreateForm = false;
		editingId = entity.id || null;
	}

	function resetForm() {
		currentEntity = { info: '', otraInfo: '' };
		showCreateForm = false;
		showEditForm = false;
		editingId = null;
	}

	function clearMessages() {
		error = null;
		message = null;
	}

	// Handle search
	function handleSearch() {
		clearMessages();
		loadEntities();
	}

	function clearSearch() {
		searchInfo = '';
		searchOtraInfo = '';
		clearMessages();
		loadEntities();
	}
</script>

{#if authStore.isAuthenticated}
	<div class="container">
		<h1>Entity Management</h1>

		<!-- Messages -->
		{#if error}
			<div class="alert alert-error">
				{error}
				<button onclick={clearMessages} class="close-btn">×</button>
			</div>
		{/if}

		{#if message}
			<div class="alert alert-success">
				{message}
				<button onclick={clearMessages} class="close-btn">×</button>
			</div>
		{/if}

		<!-- Search Section -->
		<div class="search-section">
			<h2>Search Entities</h2>
			<div class="search-form">
				<div class="form-group">
					<label for="search-info">Info:</label>
					<input
						id="search-info"
						type="text"
						bind:value={searchInfo}
						placeholder="Search by info..."
						class="form-control"
					/>
				</div>
				<div class="form-group">
					<label for="search-otra-info">Otra Info:</label>
					<input
						id="search-otra-info"
						type="text"
						bind:value={searchOtraInfo}
						placeholder="Search by otra info..."
						class="form-control"
					/>
				</div>
				<div class="button-group">
					<button onclick={handleSearch} class="btn btn-primary" disabled={loading}>
						{loading ? 'Searching...' : 'Search'}
					</button>
					<button onclick={clearSearch} class="btn btn-secondary"> Clear Filters </button>
				</div>
			</div>
		</div>

		<!-- Actions Section -->
		<div class="actions-section">
			<h2>Actions</h2>
			<div class="button-group">
				<button onclick={showCreateEntityForm} class="btn btn-success"> Create New Entity </button>
				<button onclick={loadEntities} class="btn btn-primary" disabled={loading}>
					{loading ? 'Loading...' : 'Refresh All'}
				</button>
				{#if authStore.isAdmin}
					<button
						onclick={deleteAllEntities}
						class="btn btn-danger"
						disabled={loading || entidades.length === 0}
					>
						Delete All Entities
					</button>
				{/if}
			</div>
		</div>

		<!-- Create/Edit Form -->
		{#if showCreateForm || showEditForm}
			<div class="form-section">
				<h2>{showCreateForm ? 'Create New Entity' : 'Edit Entity'}</h2>
				<form onsubmit={showCreateForm ? createEntity : updateEntity}>
					<div class="form-group">
						<label for="entity-info">Info:</label>
						<input
							id="entity-info"
							type="text"
							bind:value={currentEntity.info}
							placeholder="Enter info..."
							class="form-control"
							maxlength="100"
						/>
					</div>
					<div class="form-group">
						<label for="entity-otra-info">Otra Info:</label>
						<input
							id="entity-otra-info"
							type="text"
							bind:value={currentEntity.otraInfo}
							placeholder="Enter otra info..."
							class="form-control"
							maxlength="100"
						/>
					</div>
					<div class="button-group">
						<button type="submit" class="btn btn-primary" disabled={loading}>
							{loading ? 'Saving...' : showCreateForm ? 'Create' : 'Update'}
						</button>
						<button type="button" onclick={resetForm} class="btn btn-secondary"> Cancel </button>
					</div>
				</form>
			</div>
		{/if}

		<!-- Entities List -->
		<div class="entities-section">
			<h2>Entities ({entidades.length})</h2>

			{#if loading}
				<div class="loading">Loading entities...</div>
			{:else if entidades.length === 0}
				<div class="empty-state">
					No entities found.
					{searchInfo || searchOtraInfo
						? 'Try adjusting your search filters.'
						: 'Create your first entity!'}
				</div>
			{:else}
				<div class="entities-grid">
					{#each entidades as entity (entity.id)}
						<div class="entity-card" id="entity-{entity.id}">
							<div class="entity-header">
								<h3>Entity #{entity.id}</h3>
								<div class="entity-actions">
									<button onclick={() => getEntityById(entity.id || 0)} class="btn btn-sm btn-info">
										Refresh
									</button>
									<button onclick={() => showEditEntityForm(entity)} class="btn btn-sm btn-warning">
										Edit
									</button>
									<button
										onclick={() => deleteEntity(entity.id || 0)}
										class="btn btn-sm btn-danger"
									>
										Delete
									</button>
								</div>
							</div>
							<div class="entity-content">
								<div class="entity-field">
									<strong>Info:</strong>
									{entity.info || 'N/A'}
								</div>
								<div class="entity-field">
									<strong>Otra Info:</strong>
									{entity.otraInfo || 'N/A'}
								</div>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
{:else}
	<div class="container py-10 text-center">
		<p>Redirecting to login...</p>
	</div>
{/if}

<style>
	/* svelte-ignore css_unused_selector */
	.container {
		max-width: 1200px;
		margin: 0 auto;
		padding: 20px;
		font-family:
			system-ui,
			-apple-system,
			sans-serif;
	}

	h1 {
		color: #333;
		text-align: center;
		margin-bottom: 30px;
	}

	h2 {
		color: #555;
		margin-bottom: 15px;
		border-bottom: 2px solid #eee;
		padding-bottom: 5px;
	}

	.alert {
		padding: 12px;
		border-radius: 4px;
		margin-bottom: 20px;
		position: relative;
	}

	.alert-error {
		background-color: #fee;
		border: 1px solid #fcc;
		color: #c33;
	}

	.alert-success {
		background-color: #efe;
		border: 1px solid #cfc;
		color: #363;
	}

	.close-btn {
		position: absolute;
		right: 10px;
		top: 50%;
		transform: translateY(-50%);
		background: none;
		border: none;
		font-size: 18px;
		cursor: pointer;
		color: inherit;
	}

	.search-section,
	.actions-section,
	.form-section,
	.entities-section {
		background: #f9f9f9;
		padding: 20px;
		border-radius: 8px;
		margin-bottom: 20px;
	}

	.search-form {
		display: grid;
		grid-template-columns: 1fr 1fr auto;
		gap: 15px;
		align-items: end;
	}

	.form-group {
		display: flex;
		flex-direction: column;
	}

	.form-group label {
		margin-bottom: 5px;
		font-weight: 500;
		color: #555;
	}

	.form-control {
		padding: 8px 12px;
		border: 1px solid #ccc;
		border-radius: 4px;
		font-size: 14px;
	}

	.form-control:focus {
		outline: none;
		border-color: #007bff;
		box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
	}

	.button-group {
		display: flex;
		gap: 10px;
		flex-wrap: wrap;
	}

	.btn {
		padding: 8px 16px;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 14px;
		transition: background-color 0.2s;
	}

	.btn:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.btn-primary {
		background-color: #007bff;
		color: white;
	}

	.btn-primary:hover:not(:disabled) {
		background-color: #0056b3;
	}

	.btn-secondary {
		background-color: #6c757d;
		color: white;
	}

	.btn-secondary:hover:not(:disabled) {
		background-color: #545b62;
	}

	.btn-success {
		background-color: #28a745;
		color: white;
	}

	.btn-success:hover:not(:disabled) {
		background-color: #1e7e34;
	}

	.btn-warning {
		background-color: #ffc107;
		color: #212529;
	}

	.btn-warning:hover:not(:disabled) {
		background-color: #e0a800;
	}

	.btn-danger {
		background-color: #dc3545;
		color: white;
	}

	.btn-danger:hover:not(:disabled) {
		background-color: #c82333;
	}

	.btn-info {
		background-color: #17a2b8;
		color: white;
	}

	.btn-info:hover:not(:disabled) {
		background-color: #138496;
	}

	.btn-sm {
		padding: 4px 8px;
		font-size: 12px;
	}

	.loading,
	.empty-state {
		text-align: center;
		padding: 40px;
		color: #666;
		font-style: italic;
	}

	.entities-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
		gap: 20px;
	}

	.entity-card {
		background: white;
		border: 1px solid #ddd;
		border-radius: 8px;
		padding: 15px;
		transition:
			box-shadow 0.2s,
			transform 0.2s;
	}

	.entity-card:hover {
		box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
		transform: translateY(-2px);
	}

	.entity-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 10px;
	}

	.entity-header h3 {
		margin: 0;
		color: #333;
		font-size: 16px;
	}

	.entity-actions {
		display: flex;
		gap: 5px;
	}

	.entity-content {
		display: flex;
		flex-direction: column;
		gap: 8px;
	}

	.entity-field {
		font-size: 14px;
		line-height: 1.4;
	}

	.entity-field strong {
		color: #555;
	}

	@media (max-width: 768px) {
		.search-form {
			grid-template-columns: 1fr;
		}

		.entities-grid {
			grid-template-columns: 1fr;
		}

		.entity-header {
			flex-direction: column;
			align-items: stretch;
			gap: 10px;
		}

		.entity-actions {
			justify-content: center;
		}
	}
</style>
