<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ClaseService } from '$lib/services/claseService';
	import { MaterialService } from '$lib/services/materialService';
	import type { DTOClase } from '$lib/generated/api';
	import type { Material } from '$lib/generated/api/models/Material';
	import type { DTOPeticionCrearClase } from '$lib/generated/api';
	import { FormatterUtils } from '$lib/utils/formatters';

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let myClasses = $state<DTOClase[]>([]);
	let selectedClass = $state<DTOClase | null>(null);
	let classMaterials = $state<Material[]>([]);
	let availableMaterials = $state<Material[]>([]);

	// Edit mode state
	let editMode = $state(false);
	let editForm = $state<DTOPeticionCrearClase>({
		titulo: '',
		descripcion: '',
		precio: 0,
		presencialidad: 'PRESENCIAL' as const,
		nivel: 'PRINCIPIANTE' as const
	});

	// Material management state
	let showMaterialModal = $state(false);
	let newMaterial = $state({
		name: '',
		url: ''
	});
	let selectedMaterialToAdd: Material | null = $state(null);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	onMount(() => {
		loadMyClasses();
	});

	async function loadMyClasses() {
		loading = true;
		error = null;

		try {
			myClasses = await ClaseService.getMyClasses();
		} catch (err) {
			error = `Error al cargar mis clases: ${err}`;
			console.error('Error loading my classes:', err);
		} finally {
			loading = false;
		}
	}

	async function loadClassMaterials(claseId: number) {
		try {
			classMaterials = await ClaseService.getClassMaterials(claseId);
		} catch (err) {
			console.error('Error loading class materials:', err);
		}
	}

	async function loadAvailableMaterials() {
		try {
			const response = await MaterialService.getMaterials({ page: 0, size: 100 });
			availableMaterials = response.content || [];
		} catch (err) {
			console.error('Error loading available materials:', err);
		}
	}

	function selectClass(clase: DTOClase) {
		selectedClass = clase;
		editForm = {
			titulo: clase.titulo || '',
			descripcion: clase.descripcion || '',
			precio: clase.precio || 0,
			presencialidad: clase.presencialidad || 'PRESENCIAL',
			nivel: clase.nivel || 'PRINCIPIANTE'
		};
		loadClassMaterials(clase.id!);
	}

	function startEdit() {
		editMode = true;
	}

	function cancelEdit() {
		editMode = false;
		editForm = {
			titulo: selectedClass?.titulo || '',
			descripcion: selectedClass?.descripcion || '',
			precio: selectedClass?.precio || 0,
			presencialidad: selectedClass?.presencialidad || 'PRESENCIAL',
			nivel: selectedClass?.nivel || 'PRINCIPIANTE'
		};
	}

	async function saveChanges() {
		if (!selectedClass) return;

		try {
			await ClaseService.updateClassParameters(selectedClass.id!, editForm);
			// Create a new object with the updated properties, preserving the original structure
			selectedClass = {
				...selectedClass,
				titulo: editForm.titulo,
				descripcion: editForm.descripcion,
				precio: editForm.precio,
				presencialidad: editForm.presencialidad,
				nivel: editForm.nivel
			};
			editMode = false;

			// Reload classes to get updated data
			await loadMyClasses();
		} catch (err) {
			error = `Error al actualizar la clase: ${err}`;
			console.error('Error updating class:', err);
		}
	}

	function openMaterialModal() {
		showMaterialModal = true;
		loadAvailableMaterials();
	}

	function closeMaterialModal() {
		showMaterialModal = false;
		selectedMaterialToAdd = null;
		newMaterial = { name: '', url: '' };
	}

	async function createNewMaterial() {
		if (!selectedClass || !newMaterial.name || !newMaterial.url) return;

		try {
			const material = await MaterialService.createMaterial(newMaterial.name, newMaterial.url);
			// Convert DTOMaterial to Material type for the addMaterialToClass method
			const materialForClass: Material = {
				id: material.id,
				name: material.name || '',
				url: material.url || ''
			};
			await ClaseService.addMaterialToClass(selectedClass.id!, materialForClass);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
			closeMaterialModal();
		} catch (err) {
			error = `Error al crear el material: ${err}`;
			console.error('Error creating material:', err);
		}
	}

	async function addExistingMaterial(material: Material) {
		if (!selectedClass) return;

		try {
			await ClaseService.addMaterialToClass(selectedClass.id!, material);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
			closeMaterialModal();
		} catch (err) {
			error = `Error al agregar el material: ${err}`;
			console.error('Error adding material:', err);
		}
	}

	async function removeMaterial(materialId: number) {
		if (!selectedClass) return;

		if (!confirm('¿Estás seguro de que quieres quitar este material de la clase?')) {
			return;
		}

		try {
			await ClaseService.removeMaterialFromClass(selectedClass.id!, materialId);

			// Reload materials
			await loadClassMaterials(selectedClass.id!);
		} catch (err) {
			error = `Error al quitar el material: ${err}`;
			console.error('Error removing material:', err);
		}
	}

	function goToClassDetail(claseId: number) {
		goto(`/clases/${claseId}`);
	}

	function goToNewClass() {
		goto('/clases/nuevo');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-gray-900">Mis Clases</h1>
		<p class="mt-2 text-gray-600">Gestiona las clases que impartes</p>
	</div>

	{#if error}
		<div class="mb-6 rounded-lg border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{/if}

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-12 w-12 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else}
		<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
			<!-- Classes List -->
			<div class="lg:col-span-1">
				<div class="mb-4 flex items-center justify-between">
					<h2 class="text-xl font-semibold text-gray-900">Lista de Clases</h2>
					<button
						onclick={goToNewClass}
						class="inline-flex items-center rounded-lg bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
					>
						➕ Nueva
					</button>
				</div>

				{#if myClasses.length === 0}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
						<p class="text-gray-500">No tienes clases asignadas aún.</p>
						<button
							onclick={goToNewClass}
							class="mt-4 inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
						>
							Crear mi primera clase
						</button>
					</div>
				{:else}
					<div class="space-y-3">
						{#each myClasses as clase (clase.id)}
							<button
								onclick={() => selectClass(clase)}
								class="w-full rounded-lg border border-gray-200 bg-white p-4 text-left transition-colors hover:border-blue-300 hover:bg-blue-50 {selectedClass?.id ===
								clase.id
									? 'border-blue-500 bg-blue-50'
									: ''}"
							>
								<h3 class="font-medium text-gray-900">{clase.titulo}</h3>
								<p class="mt-1 line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>
								<div class="mt-2 flex items-center justify-between text-xs text-gray-500">
									<span>{clase.numeroAlumnos || 0} alumnos</span>
									<span>{FormatterUtils.formatPrice(clase.precio || 0)}</span>
								</div>
							</button>
						{/each}
					</div>
				{/if}
			</div>

			<!-- Class Details and Management -->
			<div class="lg:col-span-2">
				{#if selectedClass}
					<div class="rounded-lg border border-gray-200 bg-white p-6">
						<!-- Class Header -->
						<div class="mb-6 flex items-start justify-between">
							<div>
								<h2 class="text-2xl font-bold text-gray-900">{selectedClass.titulo}</h2>
								<p class="mt-1 text-gray-600">{selectedClass.descripcion}</p>
							</div>
							<div class="flex space-x-2">
								{#if editMode}
									<button
										onclick={saveChanges}
										class="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
									>
										💾 Guardar
									</button>
									<button
										onclick={cancelEdit}
										class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
									>
										❌ Cancelar
									</button>
								{:else}
									<button
										onclick={startEdit}
										class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
									>
										✏️ Editar
									</button>
									<button
										onclick={() => selectedClass && goToClassDetail(selectedClass.id!)}
										class="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
									>
										👁️ Ver Detalle
									</button>
								{/if}
							</div>
						</div>

						<!-- Class Information -->
						<div class="mb-6">
							<h3 class="mb-4 text-lg font-semibold text-gray-900">Información de la Clase</h3>

							{#if editMode}
								<div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
									<div>
										<label for="titulo" class="block text-sm font-medium text-gray-700"
											>Título</label
										>
										<input
											id="titulo"
											type="text"
											bind:value={editForm.titulo}
											class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
										/>
									</div>
									<div>
										<label for="precio" class="block text-sm font-medium text-gray-700"
											>Precio</label
										>
										<input
											id="precio"
											type="number"
											step="0.01"
											bind:value={editForm.precio}
											class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
										/>
									</div>
									<div>
										<label for="presencialidad" class="block text-sm font-medium text-gray-700"
											>Modalidad</label
										>
										<select
											id="presencialidad"
											bind:value={editForm.presencialidad}
											class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
										>
											<option value="PRESENCIAL">Presencial</option>
											<option value="ONLINE">Online</option>
											<option value="HIBRIDO">Híbrido</option>
										</select>
									</div>
									<div>
										<label for="nivel" class="block text-sm font-medium text-gray-700">Nivel</label>
										<select
											id="nivel"
											bind:value={editForm.nivel}
											class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
										>
											<option value="PRINCIPIANTE">Principiante</option>
											<option value="INTERMEDIO">Intermedio</option>
											<option value="AVANZADO">Avanzado</option>
										</select>
									</div>
									<div class="sm:col-span-2">
										<label for="descripcion" class="block text-sm font-medium text-gray-700"
											>Descripción</label
										>
										<textarea
											id="descripcion"
											bind:value={editForm.descripcion}
											rows="3"
											class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
										></textarea>
									</div>
								</div>
							{:else}
								<div class="grid grid-cols-2 gap-4 text-sm">
									<div>
										<span class="font-medium text-gray-500">Precio:</span>
										<p class="text-gray-900">
											{FormatterUtils.formatPrice(selectedClass.precio || 0)}
										</p>
									</div>
									<div>
										<span class="font-medium text-gray-500">Modalidad:</span>
										<p class="text-gray-900">{selectedClass.presencialidad}</p>
									</div>
									<div>
										<span class="font-medium text-gray-500">Nivel:</span>
										<p class="text-gray-900">{selectedClass.nivel}</p>
									</div>
									<div>
										<span class="font-medium text-gray-500">Alumnos:</span>
										<p class="text-gray-900">{selectedClass.numeroAlumnos || 0}</p>
									</div>
								</div>
							{/if}
						</div>

						<!-- Materials Management -->
						<div class="mb-6">
							<div class="mb-4 flex items-center justify-between">
								<h3 class="text-lg font-semibold text-gray-900">Materiales de la Clase</h3>
								<button
									onclick={openMaterialModal}
									class="inline-flex items-center rounded-lg bg-purple-600 px-3 py-2 text-sm font-medium text-white hover:bg-purple-700"
								>
									➕ Agregar Material
								</button>
							</div>

							{#if classMaterials.length === 0}
								<div class="rounded-lg border border-gray-200 bg-gray-50 p-4 text-center">
									<p class="text-gray-500">No hay materiales en esta clase.</p>
								</div>
							{:else}
								<div class="space-y-3">
									{#each classMaterials as material (material.id)}
										<div
											class="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4"
										>
											<div class="flex items-center space-x-3">
												<span class="text-2xl">{MaterialService.getMaterialIcon(material)}</span>
												<div>
													<h4 class="font-medium text-gray-900">{material.name}</h4>
													<p class="text-sm text-gray-500">{material.url}</p>
												</div>
											</div>
											<button
												onclick={() => removeMaterial(material.id!)}
												class="rounded-lg bg-red-100 px-3 py-1 text-sm font-medium text-red-700 hover:bg-red-200"
											>
												❌ Quitar
											</button>
										</div>
									{/each}
								</div>
							{/if}
						</div>

						<!-- Exercises Management -->
						<div class="mb-6">
							<div class="mb-4 flex items-center justify-between">
								<h3 class="text-lg font-semibold text-gray-900">Ejercicios de la Clase</h3>
								<button
									onclick={() => goto('/ejercicios/nuevo')}
									class="inline-flex items-center rounded-lg bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700"
								>
									➕ Nuevo Ejercicio
								</button>
							</div>

							<div class="rounded-lg border border-gray-200 bg-gray-50 p-4 text-center">
								<p class="text-gray-500">Funcionalidad de ejercicios en desarrollo.</p>
								<p class="mt-1 text-sm text-gray-400">
									Próximamente podrás ver y gestionar los ejercicios de esta clase.
								</p>
							</div>
						</div>
					</div>
				{:else}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-12 text-center">
						<p class="text-gray-500">Selecciona una clase para ver sus detalles y gestionarla.</p>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</div>

<!-- Material Modal -->
{#if showMaterialModal}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl">
			<div class="mb-6 flex items-center justify-between">
				<h3 class="text-lg font-semibold text-gray-900">Agregar Material a la Clase</h3>
				<button onclick={closeMaterialModal} class="text-gray-400 hover:text-gray-600"> ✕ </button>
			</div>

			<!-- Tabs -->
			<div class="mb-6 border-b border-gray-200">
				<div class="flex space-x-8">
					<button
						onclick={() => (selectedMaterialToAdd = null)}
						class="border-b-2 px-1 py-2 text-sm font-medium {selectedMaterialToAdd === null
							? 'border-blue-500 text-blue-600'
							: 'border-transparent text-gray-500 hover:text-gray-700'}"
					>
						Crear Nuevo Material
					</button>
					<button
						onclick={() => (selectedMaterialToAdd = {} as Material)}
						class="border-b-2 px-1 py-2 text-sm font-medium {selectedMaterialToAdd !== null
							? 'border-blue-500 text-blue-600'
							: 'border-transparent text-gray-500 hover:text-gray-700'}"
					>
						Usar Material Existente
					</button>
				</div>
			</div>

			{#if selectedMaterialToAdd === null}
				<!-- Create New Material -->
				<div class="space-y-4">
					<div>
						<label for="material-name" class="block text-sm font-medium text-gray-700"
							>Nombre del Material</label
						>
						<input
							id="material-name"
							type="text"
							bind:value={newMaterial.name}
							placeholder="Ej: Apuntes de la lección 1"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
						/>
					</div>
					<div>
						<label for="material-url" class="block text-sm font-medium text-gray-700"
							>URL del Material</label
						>
						<input
							id="material-url"
							type="url"
							bind:value={newMaterial.url}
							placeholder="https://ejemplo.com/material.pdf"
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
						/>
					</div>
					<div class="flex justify-end space-x-3">
						<button
							onclick={closeMaterialModal}
							class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
						>
							Cancelar
						</button>
						<button
							onclick={createNewMaterial}
							disabled={!newMaterial.name || !newMaterial.url}
							class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-400"
						>
							Crear y Agregar
						</button>
					</div>
				</div>
			{:else}
				<!-- Use Existing Material -->
				<div class="space-y-4">
					<div>
						<label for="existing-material" class="block text-sm font-medium text-gray-700"
							>Seleccionar Material</label
						>
						<select
							id="existing-material"
							bind:value={selectedMaterialToAdd}
							class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
						>
							<option value={null}>Selecciona un material...</option>
							{#each availableMaterials as material (material.id)}
								<option value={material}>{material.name}</option>
							{/each}
						</select>
					</div>
					<div class="flex justify-end space-x-3">
						<button
							onclick={closeMaterialModal}
							class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
						>
							Cancelar
						</button>
						<button
							onclick={() => selectedMaterialToAdd && addExistingMaterial(selectedMaterialToAdd)}
							disabled={!selectedMaterialToAdd}
							class="rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-400"
						>
							Agregar a la Clase
						</button>
					</div>
				</div>
			{/if}
		</div>
	</div>
{/if}
