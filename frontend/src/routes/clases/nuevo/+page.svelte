<script lang="ts">
	import { goto } from '$app/navigation';
	import { ClaseRestApi } from '$lib/generated/api/apis/ClaseRestApi';
	import { authStore } from '$lib/stores/authStore.svelte';
	import type { DTOPeticionCrearCurso } from '$lib/generated/api/models/DTOPeticionCrearCurso';
	import type { DTOPeticionCrearTaller } from '$lib/generated/api/models/DTOPeticionCrearTaller';

	// No props needed

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let success = $state(false);
	let tipoClase = $state<'CURSO' | 'TALLER'>('CURSO');

	let formData = $state({
		titulo: '',
		descripcion: '',
		precio: 0,
		presencialidad: 'ONLINE' as 'ONLINE' | 'PRESENCIAL',
		nivel: 'PRINCIPIANTE' as 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO',
		imagenPortada: '',
		// Course specific fields
		fechaInicio: new Date(),
		fechaFin: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
		// Workshop specific fields
		duracionHoras: 2,
		fechaRealizacion: new Date(),
		horaComienzo: '10:00'
	});

	// API instance
	const claseApi = new ClaseRestApi();

	// Check if user can create classes
	if (!authStore.isProfesor && !authStore.isAdmin) {
		goto('/clases');
	}

	async function handleSubmit() {
		if (!formData.titulo.trim()) {
			error = 'El título es obligatorio';
			return;
		}

		try {
			loading = true;
			error = null;

			if (tipoClase === 'CURSO') {
				const cursoData: DTOPeticionCrearCurso = {
					titulo: formData.titulo.trim(),
					descripcion: formData.descripcion.trim(),
					precio: formData.precio,
					presencialidad: formData.presencialidad,
					nivel: formData.nivel,
					imagenPortada: formData.imagenPortada.trim() || undefined,
					fechaInicio: formData.fechaInicio,
					fechaFin: formData.fechaFin
				};

				await claseApi.crearCurso({ dTOPeticionCrearCurso: cursoData });
			} else {
				const tallerData: DTOPeticionCrearTaller = {
					titulo: formData.titulo.trim(),
					descripcion: formData.descripcion.trim(),
					precio: formData.precio,
					presencialidad: formData.presencialidad,
					nivel: formData.nivel,
					imagenPortada: formData.imagenPortada.trim() || undefined,
					duracionHoras: formData.duracionHoras,
					fechaRealizacion: formData.fechaRealizacion,
					horaComienzo: formData.horaComienzo
				};

				await claseApi.crearTaller({ dTOPeticionCrearTaller: tallerData });
			}

			success = true;
			setTimeout(() => {
				goto('/clases');
			}, 2000);
		} catch (err) {
			console.error('Error creating class:', err);
			error = 'Error al crear la clase';
		} finally {
			loading = false;
		}
	}

	function handleCancel() {
		goto('/clases');
	}
</script>

<div class="container mx-auto px-4 py-8">
	<div class="mx-auto max-w-2xl">
		<div class="mb-8">
			<button
				onclick={handleCancel}
				class="inline-flex items-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm leading-4 font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
			>
				<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M15 19l-7-7 7-7"
					/>
				</svg>
				Volver a Clases
			</button>
		</div>

		<div class="rounded-lg bg-white p-6 shadow-md">
			<h1 class="mb-6 text-2xl font-bold text-gray-900">Crear Nueva Clase</h1>

			{#if error}
				<div class="mb-4 rounded-md border border-red-200 bg-red-50 p-4">
					<div class="flex">
						<div class="flex-shrink-0">
							<svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
								<path
									fill-rule="evenodd"
									d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
									clip-rule="evenodd"
								/>
							</svg>
						</div>
						<div class="ml-3">
							<h3 class="text-sm font-medium text-red-800">Error</h3>
							<div class="mt-2 text-sm text-red-700">{error}</div>
						</div>
					</div>
				</div>
			{/if}

			{#if success}
				<div class="mb-4 rounded-md border border-green-200 bg-green-50 p-4">
					<div class="flex">
						<div class="flex-shrink-0">
							<svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
								<path
									fill-rule="evenodd"
									d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
									clip-rule="evenodd"
								/>
							</svg>
						</div>
						<div class="ml-3">
							<h3 class="text-sm font-medium text-green-800">Éxito</h3>
							<div class="mt-2 text-sm text-green-700">
								Clase creada exitosamente. Redirigiendo...
							</div>
						</div>
					</div>
				</div>
			{/if}

			<form class="space-y-6">
				<div>
					<label for="tipo-clase" class="mb-2 block text-sm font-medium text-gray-700"
						>Tipo de Clase</label
					>
					<div
						class="flex space-x-4"
						id="tipo-clase"
						role="radiogroup"
						aria-labelledby="tipo-clase"
					>
						<label class="flex items-center">
							<input
								type="radio"
								bind:group={tipoClase}
								value="CURSO"
								class="h-4 w-4 border-gray-300 text-blue-600 focus:ring-blue-500"
							/>
							<span class="ml-2 text-sm text-gray-700">Curso</span>
						</label>
						<label class="flex items-center">
							<input
								type="radio"
								bind:group={tipoClase}
								value="TALLER"
								class="h-4 w-4 border-gray-300 text-blue-600 focus:ring-blue-500"
							/>
							<span class="ml-2 text-sm text-gray-700">Taller</span>
						</label>
					</div>
				</div>

				<div>
					<label for="titulo" class="block text-sm font-medium text-gray-700">Título *</label>
					<input
						type="text"
						id="titulo"
						bind:value={formData.titulo}
						required
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						placeholder="Ej: Introducción a la Programación"
					/>
				</div>

				<div>
					<label for="descripcion" class="block text-sm font-medium text-gray-700"
						>Descripción</label
					>
					<textarea
						id="descripcion"
						bind:value={formData.descripcion}
						rows="4"
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						placeholder="Describe el contenido y objetivos de la clase..."
					></textarea>
				</div>

				<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
					<div>
						<label for="precio" class="block text-sm font-medium text-gray-700">Precio (€)</label>
						<input
							type="number"
							id="precio"
							bind:value={formData.precio}
							min="0"
							step="0.01"
							class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						/>
					</div>

					<div>
						<label for="presencialidad" class="block text-sm font-medium text-gray-700"
							>Presencialidad</label
						>
						<select
							id="presencialidad"
							bind:value={formData.presencialidad}
							class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						>
							<option value="ONLINE">Online</option>
							<option value="PRESENCIAL">Presencial</option>
						</select>
					</div>

					<div>
						<label for="nivel" class="block text-sm font-medium text-gray-700">Nivel</label>
						<select
							id="nivel"
							bind:value={formData.nivel}
							class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						>
							<option value="PRINCIPIANTE">Principiante</option>
							<option value="INTERMEDIO">Intermedio</option>
							<option value="AVANZADO">Avanzado</option>
						</select>
					</div>
				</div>

				<!-- Course specific fields -->
				{#if tipoClase === 'CURSO'}
					<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
						<div>
							<label for="fechaInicio" class="block text-sm font-medium text-gray-700"
								>Fecha de Inicio</label
							>
							<input
								type="date"
								id="fechaInicio"
								value={formData.fechaInicio.toISOString().split('T')[0]}
								onchange={(e) =>
									(formData.fechaInicio = new Date((e.target as HTMLInputElement).value))}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
							/>
						</div>
						<div>
							<label for="fechaFin" class="block text-sm font-medium text-gray-700"
								>Fecha de Fin</label
							>
							<input
								type="date"
								id="fechaFin"
								value={formData.fechaFin.toISOString().split('T')[0]}
								onchange={(e) =>
									(formData.fechaFin = new Date((e.target as HTMLInputElement).value))}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
							/>
						</div>
					</div>
				{/if}

				<!-- Workshop specific fields -->
				{#if tipoClase === 'TALLER'}
					<div class="grid grid-cols-1 gap-4 md:grid-cols-3">
						<div>
							<label for="duracionHoras" class="block text-sm font-medium text-gray-700"
								>Duración (horas)</label
							>
							<input
								type="number"
								id="duracionHoras"
								bind:value={formData.duracionHoras}
								min="1"
								max="24"
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
							/>
						</div>
						<div>
							<label for="fechaRealizacion" class="block text-sm font-medium text-gray-700"
								>Fecha de Realización</label
							>
							<input
								type="date"
								id="fechaRealizacion"
								value={formData.fechaRealizacion.toISOString().split('T')[0]}
								onchange={(e) =>
									(formData.fechaRealizacion = new Date((e.target as HTMLInputElement).value))}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
							/>
						</div>
						<div>
							<label for="horaComienzo" class="block text-sm font-medium text-gray-700"
								>Hora de Comienzo</label
							>
							<input
								type="time"
								id="horaComienzo"
								bind:value={formData.horaComienzo}
								class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
							/>
						</div>
					</div>
				{/if}

				<div>
					<label for="imagenPortada" class="block text-sm font-medium text-gray-700"
						>URL de la imagen de portada</label
					>
					<input
						type="url"
						id="imagenPortada"
						bind:value={formData.imagenPortada}
						class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
						placeholder="https://ejemplo.com/imagen.jpg"
					/>
					<p class="mt-1 text-xs text-gray-500">
						Opcional. URL de una imagen para la portada de la clase.
					</p>
				</div>

				<div class="flex justify-end space-x-3 pt-6">
					<button
						type="button"
						onclick={handleCancel}
						class="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
					>
						Cancelar
					</button>
					<button
						type="button"
						onclick={handleSubmit}
						disabled={loading || !formData.titulo.trim()}
						class="inline-flex justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none disabled:cursor-not-allowed disabled:opacity-50"
					>
						{#if loading}
							<svg
								class="mr-2 -ml-1 h-4 w-4 animate-spin text-white"
								fill="none"
								viewBox="0 0 24 24"
							>
								<circle
									class="opacity-25"
									cx="12"
									cy="12"
									r="10"
									stroke="currentColor"
									stroke-width="4"
								></circle>
								<path
									class="opacity-75"
									fill="currentColor"
									d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
								></path>
							</svg>
							Creando...
						{:else}
							Crear Clase
						{/if}
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
