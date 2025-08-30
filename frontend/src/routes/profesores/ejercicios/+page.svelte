<script lang="ts">
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { ClaseService } from '$lib/services/claseService';
	import { EjercicioService } from '$lib/services/ejercicioService';
	import { EntregaService } from '$lib/services/entregaService';
	import type { DTOClase, DTOEjercicio, DTOEntregaEjercicio } from '$lib/generated/api';
	import { FormatterUtils } from '$lib/utils/formatters';

	// State
	let loading = $state(true);
	let error = $state<string | null>(null);
	let myClasses = $state<DTOClase[]>([]);
	let selectedClass = $state<DTOClase | null>(null);
	let classExercises = $state<DTOEjercicio[]>([]);
	let selectedExercise = $state<DTOEjercicio | null>(null);
	let exerciseDeliveries = $state<DTOEntregaEjercicio[]>([]);

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

	// Load data when authenticated
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadMyClasses();
		}
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
		await loadClassExercises(clase.id!);
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
	<title>Gestión de Ejercicios - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<h1 class="text-3xl font-bold text-gray-900">Gestión de Ejercicios</h1>
		<p class="mt-2 text-gray-600">Gestiona los ejercicios de tus clases y califica las entregas</p>
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
				</div>

				{#if myClasses.length === 0}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
						<p class="text-gray-500">No tienes clases asignadas aún.</p>
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
								</div>
							</button>
						{/each}
					</div>
				{/if}
			</div>

			<!-- Exercises List -->
			<div class="lg:col-span-1">
				{#if selectedClass}
					<div class="mb-4 flex items-center justify-between">
						<h2 class="text-xl font-semibold text-gray-900">Ejercicios</h2>
						<button
							onclick={createNewExercise}
							class="inline-flex items-center rounded-lg bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700"
						>
							➕ Nuevo
						</button>
					</div>

					{#if classExercises.length === 0}
						<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
							<p class="text-gray-500">No hay ejercicios en esta clase.</p>
							<button
								onclick={createNewExercise}
								class="mt-4 inline-flex items-center rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
							>
								Crear primer ejercicio
							</button>
						</div>
					{:else}
						<div class="space-y-3">
							{#each classExercises as ejercicio (ejercicio.id)}
								<button
									onclick={() => selectExercise(ejercicio)}
									class="w-full rounded-lg border border-gray-200 bg-white p-4 text-left transition-colors hover:border-blue-300 hover:bg-blue-50 {selectedExercise?.id ===
									ejercicio.id
										? 'border-blue-500 bg-blue-50'
										: ''}"
								>
									<h3 class="font-medium text-gray-900">{ejercicio.name}</h3>
									<p class="mt-1 line-clamp-2 text-sm text-gray-600">{ejercicio.statement}</p>
									<div class="mt-2 flex items-center justify-between text-xs text-gray-500">
										<span>{ejercicio.numeroEntregas || 0} entregas</span>
										<span>{ejercicio.porcentajeEntregasCalificadas || 0}% calificadas</span>
									</div>
								</button>
							{/each}
						</div>
					{/if}
				{:else}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
						<p class="text-gray-500">Selecciona una clase para ver sus ejercicios.</p>
					</div>
				{/if}
			</div>

			<!-- Deliveries and Grading -->
			<div class="lg:col-span-2">
				{#if selectedExercise}
					<div class="rounded-lg border border-gray-200 bg-white p-6">
						<!-- Exercise Header -->
						<div class="mb-6">
							<h2 class="text-2xl font-bold text-gray-900">{selectedExercise.name}</h2>
							<p class="mt-2 text-gray-600">{selectedExercise.statement}</p>
							<div class="mt-4 grid grid-cols-2 gap-4 text-sm">
								<div>
									<span class="font-medium text-gray-500">Fecha inicio:</span>
									<p class="text-gray-900">
										{FormatterUtils.formatDate(selectedExercise.startDate, { includeTime: true })}
									</p>
								</div>
								<div>
									<span class="font-medium text-gray-500">Fecha fin:</span>
									<p class="text-gray-900">
										{FormatterUtils.formatDate(selectedExercise.endDate, { includeTime: true })}
									</p>
								</div>
								<div>
									<span class="font-medium text-gray-500">Total entregas:</span>
									<p class="text-gray-900">{selectedExercise.numeroEntregas || 0}</p>
								</div>
								<div>
									<span class="font-medium text-gray-500">Calificadas:</span>
									<p class="text-gray-900">{selectedExercise.entregasCalificadas || 0}</p>
								</div>
							</div>
						</div>

						<!-- Deliveries Table -->
						<div class="mb-6">
							<h3 class="mb-4 text-lg font-semibold text-gray-900">Entregas de Estudiantes</h3>

							{#if exerciseDeliveries.length === 0}
								<div class="rounded-lg border border-gray-200 bg-gray-50 p-6 text-center">
									<p class="text-gray-500">No hay entregas para este ejercicio aún.</p>
								</div>
							{:else}
								<div class="overflow-x-auto">
									<table class="min-w-full divide-y divide-gray-200">
										<thead class="bg-gray-50">
											<tr>
												<th
													class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Alumno ID
												</th>
												<th
													class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Fecha Entrega
												</th>
												<th
													class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Estado
												</th>
												<th
													class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Nota
												</th>
												<th
													class="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Archivos
												</th>
												<th
													class="px-6 py-3 text-right text-xs font-medium tracking-wider text-gray-500 uppercase"
												>
													Acciones
												</th>
											</tr>
										</thead>
										<tbody class="divide-y divide-gray-200 bg-white">
											{#each exerciseDeliveries as entrega (entrega.id)}
												<tr class="hover:bg-gray-50">
													<td class="px-6 py-4">
														<div class="text-sm font-medium text-gray-900">
															{entrega.alumnoId || 'N/A'}
														</div>
													</td>
													<td class="px-6 py-4 text-sm text-gray-900">
														{FormatterUtils.formatDate(entrega.fechaEntrega, { includeTime: true })}
													</td>
													<td class="px-6 py-4">
														<span
															class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {getStatusColor(
																entrega.estado
															)}"
														>
															{formatStatus(entrega.estado)}
														</span>
													</td>
													<td class="px-6 py-4">
														<span class="text-sm font-medium {getGradeColor(entrega.nota)}">
															{formatGrade(entrega.nota)}
														</span>
													</td>
													<td class="px-6 py-4 text-sm text-gray-900">
														{entrega.numeroArchivos || 0} archivos
													</td>
													<td class="px-6 py-4 text-right text-sm font-medium">
														<div class="flex space-x-2">
															<button
																onclick={() => goto(`/entregas/${entrega.id}`)}
																class="text-blue-600 hover:text-blue-900"
															>
																Ver
															</button>
															{#if entrega.estado === 'ENTREGADO'}
																<button
																	onclick={() => openGradingModal(entrega)}
																	class="text-green-600 hover:text-green-900"
																>
																	Calificar
																</button>
															{:else if entrega.estado === 'CALIFICADO'}
																<button
																	onclick={() => openGradingModal(entrega)}
																	class="text-yellow-600 hover:text-yellow-900"
																>
																	Editar
																</button>
															{/if}
														</div>
													</td>
												</tr>
											{/each}
										</tbody>
									</table>
								</div>
							{/if}
						</div>
					</div>
				{:else if selectedClass}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-12 text-center">
						<p class="text-gray-500">
							Selecciona un ejercicio para ver las entregas y calificarlas.
						</p>
					</div>
				{:else}
					<div class="rounded-lg border border-gray-200 bg-gray-50 p-12 text-center">
						<p class="text-gray-500">Selecciona una clase para comenzar a gestionar ejercicios.</p>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</div>

<!-- Grading Modal -->
{#if showGradingModal && selectedDelivery}
	<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
		<div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
			<div class="mb-6">
				<h3 class="text-lg font-semibold text-gray-900">
					{#if selectedDelivery.estado === 'CALIFICADO'}
						Editar Calificación
					{:else}
						Calificar Entrega
					{/if}
				</h3>
				<p class="mt-1 text-sm text-gray-600">
					Alumno ID: {selectedDelivery.alumnoId}
				</p>
			</div>

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
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
						required
					/>
				</div>

				<div>
					<label for="comentarios" class="block text-sm font-medium text-gray-700"
						>Comentarios</label
					>
					<textarea
						id="comentarios"
						bind:value={gradingForm.comentarios}
						rows="3"
						placeholder="Comentarios sobre la entrega..."
						class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 focus:outline-none"
					></textarea>
				</div>

				<div class="flex justify-end space-x-3">
					<button
						type="button"
						onclick={closeGradingModal}
						class="rounded-lg bg-gray-600 px-4 py-2 text-sm font-medium text-white hover:bg-gray-700"
					>
						Cancelar
					</button>
					<button
						type="submit"
						class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
					>
						{#if selectedDelivery.estado === 'CALIFICADO'}
							Actualizar
						{:else}
							Calificar
						{/if}
					</button>
				</div>
			</form>
		</div>
	</div>
{/if}
