<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import type { DTOEntregaEjercicio } from '$lib/generated/api';
	import { EntregaService } from '$lib/services/entregaService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { FormatterUtils } from '$lib/utils/formatters.js';

	// State
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let entrega = $state<DTOEntregaEjercicio | null>(null);

	// Grading form state
	let gradingMode = $state(false);
	let gradeForm = $state({
		nota: '',
		comentarios: ''
	});

	// Get delivery ID from URL
	const entregaId = $derived(Number($page.params.id));

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Load delivery data
	$effect(() => {
		if (authStore.isAuthenticated && entregaId) {
			loadEntrega();
		}
	});

	async function loadEntrega() {
		loading = true;
		error = null;

		try {
			entrega = await EntregaService.getEntregaById(entregaId);
		} catch (err) {
			console.error('Error loading delivery:', err);
			error = 'Error al cargar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			loading = false;
		}
	}

	function startGrading() {
		gradingMode = true;
		gradeForm = {
			nota: entrega?.nota?.toString() || '',
			comentarios: entrega?.comentarios || ''
		};
	}

	function cancelGrading() {
		gradingMode = false;
		gradeForm = {
			nota: '',
			comentarios: ''
		};
	}

	async function handleGradeSubmit(event: Event) {
		event.preventDefault();

		if (!validateGradeForm()) {
			return;
		}

		saving = true;
		error = null;
		successMessage = null;

		try {
			const nota = parseFloat(gradeForm.nota);
			const updateData: {
				nota: number;
				comentarios?: string;
			} = { nota };

			// Only include comments if they are provided
			if (gradeForm.comentarios && gradeForm.comentarios.trim()) {
				updateData.comentarios = gradeForm.comentarios.trim();
			}

			await EntregaService.updateEntrega(entregaId, updateData);

			successMessage = 'Entrega calificada exitosamente';
			gradingMode = false;

			// Reload delivery data
			await loadEntrega();

			setTimeout(() => {
				successMessage = null;
			}, 3000);
		} catch (err) {
			console.error('Error grading delivery:', err);
			error = 'Error al calificar la entrega. Por favor, inténtalo de nuevo.';
		} finally {
			saving = false;
		}
	}

	function validateGradeForm(): boolean {
		if (!gradeForm.nota || gradeForm.nota.toString().trim() === '') {
			error = 'La nota es obligatoria';
			return false;
		}

		const nota = parseFloat(gradeForm.nota);
		if (isNaN(nota) || nota < 0 || nota > 10) {
			error = 'La nota debe ser un número entre 0 y 10';
			return false;
		}

		if (gradeForm.comentarios && gradeForm.comentarios.trim().length > 1000) {
			error = 'Los comentarios no pueden exceder 1000 caracteres';
			return false;
		}

		return true;
	}

	function handleBack() {
		if (entrega?.ejercicioId) {
			goto(`/ejercicios/${entrega.ejercicioId}`);
		} else {
			goto('/entregas');
		}
	}
</script>

<svelte:head>
	<title>Detalle de Entrega - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mb-8">
		<div class="flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-gray-900">Detalle de Entrega</h1>
				<p class="mt-2 text-gray-600">Información completa de la entrega</p>
			</div>
			<div class="flex space-x-4">
				<button
					onclick={handleBack}
					class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
				>
					Volver
				</button>
				{#if (authStore.isProfesor || authStore.isAdmin) && entrega?.estado === 'ENTREGADO'}
					<button
						onclick={startGrading}
						class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700"
					>
						Calificar Entrega
					</button>
				{/if}
			</div>
		</div>
	</div>

	{#if loading}
		<div class="flex items-center justify-center py-12">
			<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
		</div>
	{:else if error && !entrega}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
		</div>
	{:else if entrega}
		<!-- Success Message -->
		{#if successMessage}
			<div class="mb-4 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		<!-- Error Message -->
		{#if error}
			<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				{error}
			</div>
		{/if}

		<!-- Delivery Information -->
		<div class="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
			<!-- Main Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de la Entrega</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">ID de Entrega</span>
						<p class="text-lg text-gray-900">{entrega.id || 'N/A'}</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Estudiante</span>
						<p class="text-lg text-gray-900">{entrega.alumnoEntreganteId || 'N/A'}</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Ejercicio ID</span>
						<p class="text-lg text-gray-900">{entrega.ejercicioId || 'N/A'}</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Fecha de Entrega</span>
						<p class="text-gray-900">
							{FormatterUtils.formatDate(entrega.fechaEntrega, { includeTime: true })}
						</p>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Estado</span>
						<div class="mt-1">
							<span
								class="inline-flex rounded-full px-2 text-xs leading-5 font-semibold {FormatterUtils.getStatusColor(
									entrega.estado,
									'delivery'
								)}"
							>
								{FormatterUtils.formatStatus(entrega.estado, 'delivery')}
							</span>
						</div>
					</div>

					<div>
						<span class="text-sm font-medium text-gray-500">Número de Archivos</span>
						<p class="text-gray-900">{entrega.numeroArchivos || 0} archivos</p>
					</div>
				</div>
			</div>

			<!-- Grading Information -->
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Calificación</h2>

				<div class="space-y-4">
					<div>
						<span class="text-sm font-medium text-gray-500">Nota</span>
						<p class="text-2xl font-bold {FormatterUtils.getGradeColor(entrega.nota)}">
							{FormatterUtils.formatGrade(entrega.nota)}
						</p>
					</div>

					{#if entrega.comentarios && entrega.comentarios.trim()}
						<div>
							<span class="text-sm font-medium text-gray-500">Comentarios</span>
							<p class="mt-1 whitespace-pre-wrap text-gray-900">
								{entrega.comentariosFormateados || entrega.comentarios}
							</p>
						</div>
					{/if}

					{#if entrega.estadoDescriptivo}
						<div>
							<span class="text-sm font-medium text-gray-500">Estado Descriptivo</span>
							<p class="text-gray-900">{entrega.estadoDescriptivo}</p>
						</div>
					{/if}

					{#if entrega.notaFormateada}
						<div>
							<span class="text-sm font-medium text-gray-500">Nota Formateada</span>
							<p class="text-gray-900">{entrega.notaFormateada}</p>
						</div>
					{/if}
				</div>
			</div>
		</div>

		<!-- Files Section -->
		{#if entrega.archivosEntregados && entrega.archivosEntregados.length > 0}
			<div class="rounded-lg bg-white p-6 shadow-lg">
				<h2 class="mb-4 text-xl font-semibold text-gray-900">Archivos Entregados</h2>
				<div class="space-y-2">
					{#each entrega.archivosEntregados as archivo (archivo)}
						<div class="flex items-center justify-between rounded-lg border border-gray-200 p-3">
							<div class="flex items-center space-x-3">
								<div class="text-blue-600">📎</div>
								<span class="text-gray-900">{archivo}</span>
							</div>
							<a
								href={archivo}
								target="_blank"
								rel="noopener noreferrer"
								class="text-blue-600 hover:text-blue-800"
							>
								Ver archivo
							</a>
						</div>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Grading Form Modal -->
		{#if gradingMode}
			<div class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
				<div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Calificar Entrega</h3>

					<form onsubmit={handleGradeSubmit} class="space-y-4">
						<div>
							<label for="nota" class="block text-sm font-medium text-gray-700">
								Nota (0-10) *
							</label>
							<input
								type="number"
								id="nota"
								bind:value={gradeForm.nota}
								min="0"
								max="10"
								step="0.1"
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="8.5"
								required
							/>
						</div>

						<div>
							<label for="comentarios" class="block text-sm font-medium text-gray-700">
								Comentarios
							</label>
							<textarea
								id="comentarios"
								bind:value={gradeForm.comentarios}
								rows="4"
								class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
								placeholder="Comentarios sobre la entrega..."
							></textarea>
							<p class="mt-1 text-xs text-gray-500">
								{gradeForm.comentarios.length}/1000 caracteres
							</p>
						</div>

						<div class="flex justify-end space-x-3">
							<button
								type="button"
								onclick={cancelGrading}
								class="rounded-lg bg-gray-600 px-4 py-2 font-semibold text-white hover:bg-gray-700"
								disabled={saving}
							>
								Cancelar
							</button>
							<button
								type="submit"
								class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700 disabled:opacity-50"
								disabled={saving}
							>
								{saving ? 'Guardando...' : 'Guardar Calificación'}
							</button>
						</div>
					</form>
				</div>
			</div>
		{/if}
	{:else}
		<!-- Not Found State -->
		<div class="py-12 text-center">
			<div class="mb-4 text-6xl text-gray-400">📝</div>
			<h3 class="mb-2 text-lg font-medium text-gray-900">Entrega no encontrada</h3>
			<p class="mb-4 text-gray-500">La entrega que buscas no existe o ha sido eliminada.</p>
			<button
				onclick={() => goto('/entregas')}
				class="rounded bg-blue-600 px-4 py-2 font-bold text-white hover:bg-blue-700"
			>
				Volver a Entregas
			</button>
		</div>
	{/if}
</div>
