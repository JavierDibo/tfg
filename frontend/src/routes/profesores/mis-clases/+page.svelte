<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ClaseService } from '$lib/services/claseService';
	import { MaterialService } from '$lib/services/materialService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { EntregaService } from '$lib/services/entregaService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import type { DTOClase } from '$lib/generated/api';
	import type { Material } from '$lib/generated/api/models/Material';
	import type {
		DTOPeticionCrearClase,
		DTOEjercicio,
		DTOEntregaEjercicio,
		DTOAlumno
	} from '$lib/generated/api';
	import { FormatterUtils } from '$lib/utils/formatters';

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let myClasses = $state<DTOClase[]>([]);
	let selectedClass = $state<DTOClase | null>(null);
	let classMaterials = $state<Material[]>([]);
	let availableMaterials = $state<Material[]>([]);
	let classExercises = $state<DTOEjercicio[]>([]);
	let selectedExercise = $state<DTOEjercicio | null>(null);
	let exerciseDeliveries = $state<DTOEntregaEjercicio[]>([]);
	let classStudents = $state<DTOAlumno[]>([]);
	let studentsLoading = $state(false);

	// Active tab state
	let activeTab = $state<'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos'>(
		'overview'
	);

	// Function to get initial tab from URL hash
	function getInitialTab(): 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos' {
		if (typeof window !== 'undefined') {
			const hash = window.location.hash.slice(1); // Remove the # symbol
			if (['overview', 'materials', 'exercises', 'grading', 'alumnos'].includes(hash)) {
				return hash as 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos';
			}
		}
		return 'overview';
	}

	// Function to update URL hash when tab changes
	function updateUrlHash(tab: string) {
		if (typeof window !== 'undefined') {
			window.location.hash = tab;
		}
	}

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
	// Removed unused variable

	// Grading state
	let showGradingModal = $state(false);
	let selectedDelivery = $state<DTOEntregaEjercicio | null>(null);
	let gradingForm = $state({
		nota: '',
		comentarios: ''
	});

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	// Reactive effect to handle class changes and reload dependent data
	$effect(() => {
		if (selectedClass && activeTab) {
			// Clear dependent data when class changes
			if (activeTab === 'materials') {
				loadClassMaterials(selectedClass.id!);
			} else if (activeTab === 'exercises' || activeTab === 'grading') {
				loadClassExercises(selectedClass.id!);
			} else if (activeTab === 'alumnos') {
				loadClassStudents(selectedClass.id!);
			}
		}
	});

	onMount(() => {
		// Initialize active tab from URL hash
		activeTab = getInitialTab();

		// Listen for hash changes (browser back/forward buttons)
		const handleHashChange = () => {
			const newTab = getInitialTab();
			if (newTab !== activeTab) {
				activeTab = newTab;
				// Load data for the new tab if a class is selected
				if (selectedClass) {
					handleTabChange(newTab);
				}
			}
		};

		window.addEventListener('hashchange', handleHashChange);

		loadMyClasses();

		// Cleanup event listener
		return () => {
			window.removeEventListener('hashchange', handleHashChange);
		};
	});

	async function loadMyClasses() {
		loading = true;
		error = null;

		try {
			myClasses = await ClaseService.getMyClasses();
			if (myClasses.length > 0) {
				selectClass(myClasses[0]);
			}
		} catch (err) {
			error = `Error al cargar mis clases: ${err}`;
			console.error('Error loading my classes:', err);
		} finally {
			loading = false;
		}
	}

	async function selectClass(clase: DTOClase) {
		selectedClass = clase;
		selectedExercise = null;
		exerciseDeliveries = [];
		// Clear students data when switching classes
		classStudents = [];
		editForm = {
			titulo: clase.titulo || '',
			descripcion: clase.descripcion || '',
			precio: clase.precio || 0,
			presencialidad: clase.presencialidad || 'PRESENCIAL',
			nivel: clase.nivel || 'PRINCIPIANTE'
		};

		// Load data based on active tab
		if (activeTab === 'materials') {
			await loadClassMaterials(clase.id!);
		} else if (activeTab === 'exercises') {
			await loadClassExercises(clase.id!);
		} else if (activeTab === 'grading') {
			await loadClassExercises(clase.id!);
		} else if (activeTab === 'alumnos') {
			await loadClassStudents(clase.id!);
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

	async function loadClassExercises(claseId: number) {
		try {
			const response = await EjercicioService.getEjercicios({ classId: claseId.toString() });
			classExercises = response.content || [];
		} catch (err) {
			console.error('Error loading class exercises:', err);
			classExercises = [];
		}
	}

	async function selectExercise(ejercicio: DTOEjercicio) {
		selectedExercise = ejercicio;
		await loadExerciseDeliveries(ejercicio.id!);
	}

	async function loadExerciseDeliveries(ejercicioId: number) {
		try {
			const response = await EntregaService.getEntregasByEjercicio(ejercicioId);
			exerciseDeliveries = response.content || [];
		} catch (err) {
			console.error('Error loading exercise deliveries:', err);
			exerciseDeliveries = [];
		}
	}

	async function loadClassStudents(claseId: number) {
		studentsLoading = true;
		try {
			const response = await EnrollmentService.getStudentsInClass(claseId, 0, 100);
			classStudents = response.content || [];
		} catch (err) {
			console.error('Error loading class students:', err);
			classStudents = [];
		} finally {
			studentsLoading = false;
		}
	}

	async function handleTabChange(
		tab: 'overview' | 'materials' | 'exercises' | 'grading' | 'alumnos'
	) {
		activeTab = tab;

		// Update URL hash to persist tab state
		updateUrlHash(tab);

		if (selectedClass) {
			if (tab === 'materials') {
				await loadClassMaterials(selectedClass.id!);
			} else if (tab === 'exercises' || tab === 'grading') {
				await loadClassExercises(selectedClass.id!);
			} else if (tab === 'alumnos') {
				await loadClassStudents(selectedClass.id!);
			}
		}
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

	function openGradingModal(delivery: DTOEntregaEjercicio) {
		selectedDelivery = delivery;
		gradingForm = {
			nota: delivery.nota?.toString() || '',
			comentarios: delivery.comentarios || ''
		};
		showGradingModal = true;
	}

	function closeGradingModal() {
		showGradingModal = false;
		selectedDelivery = null;
		gradingForm = { nota: '', comentarios: '' };
	}

	async function handleGradeSubmit(event: Event) {
		event.preventDefault();

		if (!selectedDelivery || !validateGradingForm()) {
			return;
		}

		try {
			const nota = parseFloat(gradingForm.nota);
			const updateData: {
				nota: number;
				comentarios?: string;
			} = { nota };

			if (gradingForm.comentarios && gradingForm.comentarios.trim()) {
				updateData.comentarios = gradingForm.comentarios.trim();
			}

			await EntregaService.updateEntrega(selectedDelivery.id!, updateData);

			// Update local state
			selectedDelivery.nota = nota;
			selectedDelivery.comentarios = updateData.comentarios;
			selectedDelivery.estado = 'CALIFICADO';

			// Reload deliveries to get updated data
			if (selectedExercise) {
				await loadExerciseDeliveries(selectedExercise.id!);
			}

			closeGradingModal();
		} catch (err) {
			error = `Error al calificar la entrega: ${err}`;
			console.error('Error grading delivery:', err);
		}
	}

	function validateGradingForm(): boolean {
		if (!gradingForm.nota || gradingForm.nota.toString().trim() === '') {
			error = 'La nota es obligatoria';
			return false;
		}

		const nota = parseFloat(gradingForm.nota);
		if (isNaN(nota) || nota < 0 || nota > 10) {
			error = 'La nota debe ser un número entre 0 y 10';
			return false;
		}

		if (gradingForm.comentarios && gradingForm.comentarios.trim().length > 1000) {
			error = 'Los comentarios no pueden exceder 1000 caracteres';
			return false;
		}

		return true;
	}

	function createNewExercise() {
		if (selectedClass) {
			goto(`/ejercicios/nuevo?classId=${selectedClass.id}`);
		} else {
			goto('/ejercicios/nuevo');
		}
	}

	function goToClassDetail(claseId: number) {
		goto(`/clases/${claseId}`);
	}

	function goToNewClass() {
		goto('/clases/nuevo');
	}

	function getStatusColor(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'bg-yellow-100 text-yellow-800';
			case 'CALIFICADO':
				return 'bg-green-100 text-green-800';
			case 'ENTREGADO':
				return 'bg-blue-100 text-blue-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}

	function formatStatus(status: string | undefined | null): string {
		switch (status) {
			case 'PENDIENTE':
				return 'Pendiente';
			case 'CALIFICADO':
				return 'Calificado';
			case 'ENTREGADO':
				return 'Entregado';
			default:
				return 'Desconocido';
		}
	}

	function getGradeColor(nota: number | undefined): string {
		if (nota === undefined || nota === null) return 'text-gray-500';
		if (nota >= 9) return 'text-green-600 font-bold';
		if (nota >= 7) return 'text-blue-600 font-semibold';
		if (nota >= 5) return 'text-yellow-600 font-semibold';
		return 'text-red-600 font-semibold';
	}

	function formatGrade(nota: number | undefined): string {
		if (nota === undefined || nota === null) return 'N/A';
		return nota.toFixed(1);
	}
</script>

<svelte:head>
	<title>Gestión de Clases - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Clases</h1>
		<p class="mt-2 text-gray-600">Gestiona tus clases, materiales, ejercicios y calificaciones</p>
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
		<div class="grid grid-cols-1 gap-8 lg:grid-cols-4">
			<!-- Classes List -->
			<div class="lg:col-span-1">
				<div class="mb-4 flex items-center justify-between">
					<h2 class="text-xl font-semibold text-gray-900">Mis Clases</h2>
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
			<div class="lg:col-span-3">
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
										class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
									>
										👁️ Ver Detalle
									</button>
								{/if}
							</div>
						</div>

						<!-- Tab Navigation -->
						<div class="mb-6 border-b border-gray-200">
							<nav class="-mb-px flex space-x-8">
								<button
									onclick={() => handleTabChange('overview')}
									class="border-b-2 px-1 py-2 text-sm font-medium {activeTab === 'overview'
										? 'border-blue-500 text-blue-600'
										: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
								>
									📊 Resumen
								</button>
								<button
									onclick={() => handleTabChange('materials')}
									class="border-b-2 px-1 py-2 text-sm font-medium {activeTab === 'materials'
										? 'border-blue-500 text-blue-600'
										: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
								>
									📚 Materiales
								</button>
								<button
									onclick={() => handleTabChange('exercises')}
									class="border-b-2 px-1 py-2 text-sm font-medium {activeTab === 'exercises'
										? 'border-blue-500 text-blue-600'
										: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
								>
									✍️ Ejercicios
								</button>
								<button
									onclick={() => handleTabChange('grading')}
									class="border-b-2 px-1 py-2 text-sm font-medium {activeTab === 'grading'
										? 'border-blue-500 text-blue-600'
										: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
								>
									📝 Calificaciones
								</button>
								<button
									onclick={() => handleTabChange('alumnos')}
									class="border-b-2 px-1 py-2 text-sm font-medium {activeTab === 'alumnos'
										? 'border-blue-500 text-blue-600'
										: 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'}"
								>
									👥 Alumnos
								</button>
							</nav>
						</div>

						<!-- Tab Content -->
						{#if activeTab === 'overview'}
							<!-- Overview Tab -->
							<div class="space-y-6">
								<!-- Class Information -->
								<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
									{#if editMode}
										<!-- Edit Form -->
										<div class="rounded-lg border border-gray-200 bg-white p-4">
											<h3 class="mb-4 text-lg font-semibold text-gray-900">
												✏️ Editar Información de la Clase
											</h3>
											<div class="space-y-4">
												<div>
													<label for="titulo" class="block text-sm font-medium text-gray-700">
														Título de la Clase
													</label>
													<input
														id="titulo"
														type="text"
														bind:value={editForm.titulo}
														class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
														placeholder="Título de la clase"
														required
													/>
												</div>

												<div>
													<label for="descripcion" class="block text-sm font-medium text-gray-700">
														Descripción
													</label>
													<textarea
														id="descripcion"
														bind:value={editForm.descripcion}
														rows="3"
														class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
														placeholder="Descripción de la clase"
													></textarea>
												</div>

												<div class="grid grid-cols-2 gap-4">
													<div>
														<label for="precio" class="block text-sm font-medium text-gray-700">
															Precio
														</label>
														<input
															id="precio"
															type="number"
															step="0.01"
															min="0"
															bind:value={editForm.precio}
															class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
															placeholder="0.00"
															required
														/>
													</div>

													<div>
														<label
															for="presencialidad"
															class="block text-sm font-medium text-gray-700"
														>
															Modalidad
														</label>
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
												</div>

												<div>
													<label for="nivel" class="block text-sm font-medium text-gray-700">
														Nivel
													</label>
													<select
														id="nivel"
														bind:value={editForm.nivel}
														class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
													>
														<option value="PRINCIPIANTE">Principiante</option>
														<option value="BASICO">Básico</option>
														<option value="INTERMEDIO">Intermedio</option>
														<option value="AVANZADO">Avanzado</option>
													</select>
												</div>
											</div>
										</div>
									{:else}
										<!-- Display Class Information -->
										<div class="rounded-lg border border-gray-200 bg-gray-50 p-4">
											<h3 class="mb-2 text-lg font-semibold text-gray-900">
												Información de la Clase
											</h3>
											<div class="space-y-2 text-sm text-gray-600">
												<p>
													<strong>Título:</strong>
													{selectedClass.titulo}
												</p>
												<p>
													<strong>Precio:</strong>
													{FormatterUtils.formatPrice(selectedClass.precio || 0)}
												</p>
												<p><strong>Presencialidad:</strong> {selectedClass.presencialidad}</p>
												<p><strong>Nivel:</strong> {selectedClass.nivel}</p>
												<p><strong>Alumnos:</strong> {selectedClass.numeroAlumnos || 0}</p>
											</div>
										</div>
									{/if}

									<div class="rounded-lg border border-gray-200 bg-gray-50 p-4">
										<h3 class="mb-2 text-lg font-semibold text-gray-900">Acciones Rápidas</h3>
										<div class="space-y-2">
											<button
												onclick={() => handleTabChange('materials')}
												class="w-full rounded bg-blue-100 px-3 py-2 text-sm text-blue-700 hover:bg-blue-200"
											>
												📚 Gestionar Materiales
											</button>
											<button
												onclick={() => handleTabChange('exercises')}
												class="w-full rounded bg-green-100 px-3 py-2 text-sm text-green-700 hover:bg-green-200"
											>
												✍️ Gestionar Ejercicios
											</button>
											<button
												onclick={() => handleTabChange('grading')}
												class="w-full rounded bg-purple-100 px-3 py-2 text-sm text-purple-700 hover:bg-purple-200"
											>
												📝 Revisar Entregas
											</button>
											<button
												onclick={() => handleTabChange('alumnos')}
												class="w-full rounded bg-orange-100 px-3 py-2 text-sm text-orange-700 hover:bg-orange-200"
											>
												👥 Gestionar Alumnos
											</button>
										</div>
									</div>
								</div>

								<!-- Quick Stats -->
								<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
									<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
										<div class="text-2xl font-bold text-blue-600">
											{selectedClass.numeroAlumnos || 0}
										</div>
										<div class="text-sm text-gray-600">Alumnos Inscritos</div>
									</div>
									<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
										<div class="text-2xl font-bold text-green-600">
											{selectedClass.precio
												? FormatterUtils.formatPrice(selectedClass.precio)
												: 'Gratis'}
										</div>
										<div class="text-sm text-gray-600">Precio por Clase</div>
									</div>
									<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
										<div class="text-2xl font-bold text-purple-600">
											{selectedClass.presencialidad}
										</div>
										<div class="text-sm text-gray-600">Modalidad</div>
									</div>
								</div>
							</div>
						{:else if activeTab === 'materials'}
							<!-- Materials Tab -->
							<div class="space-y-6">
								<div class="flex items-center justify-between">
									<h3 class="text-lg font-semibold text-gray-900">Materiales de la Clase</h3>
									<button
										onclick={openMaterialModal}
										class="inline-flex items-center rounded-lg bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
									>
										➕ Agregar Material
									</button>
								</div>

								{#if classMaterials.length === 0}
									<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
										<p class="text-gray-500">No hay materiales en esta clase.</p>
										<button
											onclick={openMaterialModal}
											class="mt-4 inline-flex items-center rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
										>
											Agregar primer material
										</button>
									</div>
								{:else}
									<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
										{#each classMaterials as material (material.id)}
											<div class="rounded-lg border border-gray-200 bg-white p-4">
												<h4 class="font-medium text-gray-900">{material.name}</h4>
												<a
													href={material.url}
													target="_blank"
													rel="noopener noreferrer"
													class="mt-2 inline-block text-sm text-blue-600 hover:text-blue-800"
												>
													Ver Material →
												</a>
												<button
													onclick={() => removeMaterial(material.id!)}
													class="mt-3 w-full rounded bg-red-100 px-3 py-2 text-sm text-red-700 hover:bg-red-200"
												>
													❌ Quitar
												</button>
											</div>
										{/each}
									</div>
								{/if}
							</div>
						{:else if activeTab === 'exercises'}
							<!-- Exercises Tab -->
							<div class="space-y-6">
								<div class="flex items-center justify-between">
									<h3 class="text-lg font-semibold text-gray-900">Ejercicios de la Clase</h3>
									<button
										onclick={createNewExercise}
										class="inline-flex items-center rounded-lg bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700"
									>
										➕ Nuevo Ejercicio
									</button>
								</div>

								{#if classExercises.length === 0}
									<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
										<p class="text-gray-500">No hay ejercicios en esta clase.</p>
										<button
											onclick={createNewExercise}
											class="mt-4 inline-flex items-center rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
										>
											Crear primer ejercicio
										</button>
									</div>
								{:else}
									<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
										{#each classExercises as ejercicio (ejercicio.id)}
											<div class="rounded-lg border border-gray-200 bg-white p-4">
												<h4 class="font-medium text-gray-900">{ejercicio.name}</h4>
												<p class="mt-2 line-clamp-3 text-sm text-gray-600">{ejercicio.statement}</p>
												<div class="mt-3 flex items-center justify-between">
													<span class="text-xs text-gray-500">
														{ejercicio.startDate
															? FormatterUtils.formatDate(ejercicio.startDate)
															: 'Sin fecha'}
													</span>
													<button
														onclick={() => goto(`/ejercicios/${ejercicio.id}`)}
														class="rounded bg-blue-100 px-3 py-1 text-xs text-blue-700 hover:bg-blue-200"
													>
														Ver Detalles
													</button>
												</div>
											</div>
										{/each}
									</div>
								{/if}
							</div>
						{:else if activeTab === 'grading'}
							<!-- Grading Tab -->
							<div class="space-y-6">
								<div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
									<!-- Exercises List -->
									<div>
										<h3 class="mb-4 text-lg font-semibold text-gray-900">Ejercicios</h3>
										{#if classExercises.length === 0}
											<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
												<p class="text-gray-500">No hay ejercicios en esta clase.</p>
											</div>
										{:else}
											<div class="space-y-3">
												{#each classExercises as ejercicio (ejercicio.id)}
													<div
														class="rounded-lg border border-gray-200 bg-white p-4 transition-colors hover:border-blue-300 hover:bg-blue-50 {selectedExercise?.id ===
														ejercicio.id
															? 'border-blue-500 bg-blue-50'
															: ''}"
													>
														<div class="flex items-center justify-between">
															<button
																onclick={() => selectExercise(ejercicio)}
																class="flex-1 text-left"
															>
																<h4 class="font-medium text-gray-900">{ejercicio.name}</h4>
																<p class="mt-1 line-clamp-2 text-sm text-gray-600">
																	{ejercicio.statement}
																</p>
															</button>
															<button
																onclick={() => goto(`/ejercicios/${ejercicio.id}`)}
																class="ml-3 rounded bg-blue-100 px-3 py-1 text-xs text-blue-700 hover:bg-blue-200"
															>
																Ver Detalles
															</button>
														</div>
													</div>
												{/each}
											</div>
										{/if}
									</div>

									<!-- Deliveries List -->
									<div>
										<h3 class="mb-4 text-lg font-semibold text-gray-900">
											Entregas {selectedExercise ? `- ${selectedExercise.name}` : ''}
										</h3>
										{#if !selectedExercise}
											<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
												<p class="text-gray-500">Selecciona una clase para ver sus ejercicios.</p>
											</div>
										{:else if exerciseDeliveries.length === 0}
											<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
												<p class="text-gray-500">No hay entregas para este ejercicio.</p>
											</div>
										{:else}
											<div class="space-y-3">
												{#each exerciseDeliveries as entrega (entrega.id)}
													<div class="rounded-lg border border-gray-200 bg-white p-4">
														<div class="flex items-center justify-between">
															<div>
																<p class="font-medium text-gray-900">
																	Alumno ID: {entrega.alumnoId}
																</p>
																<p class="text-sm text-gray-600">
																	Estado:
																	<span
																		class="inline-flex rounded-full px-2 text-xs font-semibold {getStatusColor(
																			entrega.estado
																		)}"
																	>
																		{formatStatus(entrega.estado)}
																	</span>
																</p>
																<p class="text-sm text-gray-600">
																	Nota:
																	<span class={getGradeColor(entrega.nota)}>
																		{formatGrade(entrega.nota)}
																	</span>
																</p>
															</div>
															<div class="flex space-x-2">
																<button
																	onclick={() => goto(`/entregas/${entrega.id}`)}
																	class="rounded bg-gray-100 px-3 py-2 text-sm text-gray-700 hover:bg-gray-200"
																>
																	👁️ Ver Detalles
																</button>
																<button
																	onclick={() => openGradingModal(entrega)}
																	class="rounded bg-blue-100 px-3 py-2 text-sm text-blue-700 hover:bg-blue-200"
																>
																	{entrega.nota ? '✏️ Editar' : '📝 Calificar'}
																</button>
															</div>
														</div>
													</div>
												{/each}
											</div>
										{/if}
									</div>
								</div>
							</div>
						{:else if activeTab === 'alumnos'}
							<!-- Alumnos Tab -->
							<div class="space-y-6">
								<div class="flex items-center justify-between">
									<h3 class="text-lg font-semibold text-gray-900">Alumnos Inscritos</h3>
									<div class="text-sm text-gray-600">
										Total: {selectedClass?.numeroAlumnos || 0} alumnos
									</div>
								</div>

								{#if studentsLoading}
									<div class="flex items-center justify-center py-12">
										<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
										<span class="ml-3 text-gray-600">Cargando alumnos...</span>
									</div>
								{:else if classStudents.length === 0}
									<div class="rounded-lg border border-gray-200 bg-gray-50 p-8 text-center">
										<div class="mx-auto mb-4 h-16 w-16 rounded-full bg-gray-100 p-3">
											<span class="text-2xl">👥</span>
										</div>
										<p class="text-gray-500">No hay alumnos inscritos en esta clase aún.</p>
										<p class="mt-2 text-sm text-gray-400">
											Los alumnos aparecerán aquí una vez se inscriban a la clase.
										</p>
									</div>
								{:else}
									<!-- Student List -->
									<div class="rounded-lg border border-gray-200 bg-white p-6">
										<div class="mb-4 flex items-center justify-between">
											<h4 class="text-md font-medium text-gray-900">Lista de Alumnos</h4>
											<span class="text-sm text-gray-500">
												{classStudents.length} inscritos
											</span>
										</div>

										<div class="space-y-3">
											{#each classStudents as student (student.id)}
												<div class="rounded-lg border border-gray-200 bg-white p-4">
													<div class="flex items-center justify-between">
														<div class="flex items-center space-x-3">
															<div
																class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100"
															>
																<span class="text-sm font-medium text-blue-600">
																	{student.firstName?.charAt(0)}{student.lastName?.charAt(0)}
																</span>
															</div>
															<div>
																<h5 class="font-medium text-gray-900">
																	{student.firstName}
																	{student.lastName}
																</h5>
																<p class="text-sm text-gray-600">{student.email}</p>
																{#if student.phoneNumber}
																	<p class="text-xs text-gray-500">📞 {student.phoneNumber}</p>
																{/if}
															</div>
														</div>
														<div class="text-right">
															<div class="text-sm text-gray-600">
																<span
																	class="inline-flex items-center rounded-full px-2 text-xs font-semibold {student.enabled
																		? 'bg-green-100 text-green-800'
																		: 'bg-red-100 text-red-800'}"
																>
																	{student.enabled ? 'Activo' : 'Inactivo'}
																</span>
															</div>
															{#if student.enrollmentDate}
																<p class="mt-1 text-xs text-gray-500">
																	📅 {FormatterUtils.formatDate(student.enrollmentDate)}
																</p>
															{/if}
															<button
																onclick={() => goto(`/alumnos/${student.id}`)}
																class="mt-2 rounded bg-blue-100 px-3 py-1 text-xs text-blue-700 transition-colors hover:bg-blue-200"
															>
																👤 Ver Perfil
															</button>
														</div>
													</div>
												</div>
											{/each}
										</div>
									</div>

									<!-- Quick Stats -->
									<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
										<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
											<div class="text-2xl font-bold text-blue-600">
												{classStudents.length}
											</div>
											<div class="text-sm text-gray-600">Total Inscritos</div>
										</div>
										<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
											<div class="text-2xl font-bold text-green-600">
												{classStudents.filter((s) => s.enabled).length}
											</div>
											<div class="text-sm text-gray-600">Activos</div>
										</div>
										<div class="rounded-lg border border-gray-200 bg-white p-4 text-center">
											<div class="text-2xl font-bold text-purple-600">
												{classStudents.filter((s) => s.submissionIds && s.submissionIds.length > 0)
													.length}
											</div>
											<div class="text-sm text-gray-600">Con Entregas</div>
										</div>
									</div>
								{/if}
							</div>
						{/if}
					</div>
				{:else}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-12 text-center">
						<p class="text-gray-500">Selecciona una clase para comenzar a gestionar.</p>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</div>

<!-- Material Modal -->
{#if showMaterialModal}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="w-full max-w-md rounded-lg bg-white p-6">
			<h3 class="mb-4 text-lg font-semibold text-gray-900">Agregar Material</h3>

			<!-- Create New Material -->
			<div class="mb-6">
				<h4 class="mb-3 font-medium text-gray-900">Crear Nuevo Material</h4>
				<div class="space-y-3">
					<input
						type="text"
						bind:value={newMaterial.name}
						placeholder="Nombre del material"
						class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
					/>
					<input
						type="url"
						bind:value={newMaterial.url}
						placeholder="URL del material"
						class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
					/>
					<button
						onclick={createNewMaterial}
						disabled={!newMaterial.name || !newMaterial.url}
						class="w-full rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:bg-gray-400"
					>
						Crear y Agregar
					</button>
				</div>
			</div>

			<!-- Add Existing Material -->
			<div class="mb-6">
				<h4 class="mb-3 font-medium text-gray-900">Agregar Material Existente</h4>
				{#if availableMaterials.length > 0}
					<div class="max-h-40 space-y-2 overflow-y-auto">
						{#each availableMaterials as material (material.id)}
							<button
								onclick={() => addExistingMaterial(material)}
								class="w-full rounded border border-gray-200 bg-white p-3 text-left hover:bg-gray-50"
							>
								<div class="font-medium text-gray-900">{material.name}</div>
								<div class="text-sm text-gray-600">{material.url}</div>
							</button>
						{/each}
					</div>
				{:else}
					<p class="text-sm text-gray-500">No hay materiales disponibles.</p>
				{/if}
			</div>

			<button
				onclick={closeMaterialModal}
				class="w-full rounded-lg bg-gray-600 px-4 py-2 text-white hover:bg-gray-700"
			>
				Cerrar
			</button>
		</div>
	</div>
{/if}

<!-- Grading Modal -->
{#if showGradingModal}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="w-full max-w-md rounded-lg bg-white p-6">
			<h3 class="mb-4 text-lg font-semibold text-gray-900">Calificar Entrega</h3>

			<form onsubmit={handleGradeSubmit} class="space-y-4">
				<div>
					<label for="nota" class="block text-sm font-medium text-gray-700">Nota (0-10)</label>
					<input
						id="nota"
						type="number"
						min="0"
						max="10"
						step="0.1"
						bind:value={gradingForm.nota}
						class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
						required
					/>
				</div>

				<div>
					<label for="comentarios" class="block text-sm font-medium text-gray-700"
						>Comentarios (opcional)</label
					>
					<textarea
						id="comentarios"
						bind:value={gradingForm.comentarios}
						rows="3"
						maxlength="1000"
						class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
						placeholder="Comentarios sobre la entrega..."
					></textarea>
				</div>

				<div class="flex space-x-3">
					<button
						type="submit"
						class="flex-1 rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
					>
						Guardar Calificación
					</button>
					<button
						type="button"
						onclick={closeGradingModal}
						class="flex-1 rounded-lg bg-gray-600 px-4 py-2 text-white hover:bg-gray-700"
					>
						Cancelar
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
