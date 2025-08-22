<script lang="ts">
	import { goto } from '$app/navigation';
	import type {
		DTOPeticionCrearCurso,
		DTOPeticionCrearCursoNivelEnum,
		DTOPeticionCrearCursoPresencialidadEnum,
		DTOPeticionCrearTaller,
		DTOPeticionCrearTallerNivelEnum,
		DTOPeticionCrearTallerPresencialidadEnum,
		Material
	} from '$lib/generated/api';
	import { ClaseService } from '$lib/services/claseService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);

	// Form data
	let tipoClase = $state<'curso' | 'taller'>('curso');
	let titulo = $state('');
	let descripcion = $state('');
	let precio = $state<number>(0);
	let presencialidad = $state<
		DTOPeticionCrearCursoPresencialidadEnum | DTOPeticionCrearTallerPresencialidadEnum
	>('ONLINE');
	let nivel = $state<DTOPeticionCrearCursoNivelEnum | DTOPeticionCrearTallerNivelEnum>(
		'PRINCIPIANTE'
	);
	let imagenPortada = $state('');
	let fechaInicio = $state('');
	let fechaFin = $state('');
	let duracionHoras = $state<number>(2);
	let fechaRealizacion = $state('');
	let horaComienzo = $state('');
	let profesoresId = $state<string[]>([]);
	let material = $state<Material[]>([]);

	// Material form
	let nuevoMaterial = $state({ nombre: '', url: '' });

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}

		if (!authStore.isAdmin && !authStore.isProfesor) {
			goto('/');
			return;
		}
	});

	// Add material
	function agregarMaterial() {
		if (nuevoMaterial.nombre.trim() && nuevoMaterial.url.trim()) {
			material = [
				...material,
				{
					id: Date.now().toString(), // Temporary ID
					nombre: nuevoMaterial.nombre.trim(),
					url: nuevoMaterial.url.trim()
				}
			];
			nuevoMaterial = { nombre: '', url: '' };
		}
	}

	// Remove material
	function removerMaterial(index: number) {
		material = material.filter((_, i) => i !== index);
	}

	// Add professor ID
	function agregarProfesor() {
		const profesorId = prompt('Ingresa el ID del profesor:');
		if (profesorId && profesorId.trim()) {
			profesoresId = [...profesoresId, profesorId.trim()];
		}
	}

	// Remove professor ID
	function removerProfesor(index: number) {
		profesoresId = profesoresId.filter((_, i) => i !== index);
	}

	// Form validation
	function validarFormulario(): boolean {
		if (!titulo.trim()) {
			error = 'El título es obligatorio';
			return false;
		}
		if (precio < 0) {
			error = 'El precio no puede ser negativo';
			return false;
		}

		if (tipoClase === 'curso') {
			if (!fechaInicio) {
				error = 'La fecha de inicio es obligatoria';
				return false;
			}
			if (!fechaFin) {
				error = 'La fecha de fin es obligatoria';
				return false;
			}
			if (new Date(fechaInicio) >= new Date(fechaFin)) {
				error = 'La fecha de fin debe ser posterior a la fecha de inicio';
				return false;
			}
		} else {
			if (!fechaRealizacion) {
				error = 'La fecha de realización es obligatoria';
				return false;
			}
			if (!horaComienzo) {
				error = 'La hora de comienzo es obligatoria';
				return false;
			}
			if (duracionHoras <= 0) {
				error = 'La duración debe ser mayor a 0';
				return false;
			}
		}
		return true;
	}

	// Submit form
	async function submitForm() {
		if (!validarFormulario()) return;

		loading = true;
		error = null;

		try {
			if (tipoClase === 'curso') {
				const cursoData: DTOPeticionCrearCurso = {
					titulo: titulo.trim(),
					descripcion: descripcion.trim() || undefined,
					precio,
					presencialidad: presencialidad as DTOPeticionCrearCursoPresencialidadEnum,
					nivel: nivel as DTOPeticionCrearCursoNivelEnum,
					fechaInicio: new Date(fechaInicio),
					fechaFin: new Date(fechaFin),
					imagenPortada: imagenPortada.trim() || undefined,
					profesoresId: profesoresId.length > 0 ? profesoresId : undefined,
					material: material.length > 0 ? material : undefined
				};

				await ClaseService.crearCurso(cursoData);
			} else {
				const tallerData: DTOPeticionCrearTaller = {
					titulo: titulo.trim(),
					descripcion: descripcion.trim() || undefined,
					precio,
					presencialidad: presencialidad as DTOPeticionCrearTallerPresencialidadEnum,
					nivel: nivel as DTOPeticionCrearTallerNivelEnum,
					duracionHoras,
					fechaRealizacion: new Date(fechaRealizacion),
					horaComienzo: horaComienzo,
					imagenPortada: imagenPortada.trim() || undefined,
					profesoresId: profesoresId.length > 0 ? profesoresId : undefined,
					material: material.length > 0 ? material : undefined
				};

				await ClaseService.crearTaller(tallerData);
			}

			successMessage = `${tipoClase === 'curso' ? 'Curso' : 'Taller'} creado correctamente`;
			setTimeout(() => {
				goto('/clases');
			}, 2000);
		} catch (err) {
			error = `Error al crear ${tipoClase}: ${err}`;
			console.error('Error creating class:', err);
		} finally {
			loading = false;
		}
	}

	// Reset form
	function resetForm() {
		titulo = '';
		descripcion = '';
		precio = 0;
		presencialidad = 'ONLINE';
		nivel = 'PRINCIPIANTE';
		imagenPortada = '';
		fechaInicio = '';
		fechaFin = '';
		duracionHoras = 2;
		fechaRealizacion = '';
		horaComienzo = '';
		profesoresId = [];
		material = [];
		error = null;
		successMessage = null;
	}
</script>

<svelte:head>
	<title>Nueva Clase - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="mx-auto max-w-4xl">
		<!-- Header -->
		<div class="mb-8">
			<div class="mb-6 flex items-center justify-between">
				<h1 class="text-3xl font-bold text-gray-900">Crear Nueva Clase</h1>
				<button onclick={() => goto('/clases')} class="text-blue-600 hover:text-blue-800">
					← Volver a Clases
				</button>
			</div>
		</div>

		<!-- Messages -->
		{#if error}
			<div class="mb-6 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
				{error}
			</div>
		{/if}

		{#if successMessage}
			<div class="mb-6 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700">
				{successMessage}
			</div>
		{/if}

		<!-- Form -->
		<form
			onsubmit={(e) => {
				e.preventDefault();
				submitForm();
			}}
			class="rounded-lg bg-white p-6 shadow-lg"
		>
			<!-- Class Type Selection -->
			<div class="mb-6">
				<fieldset>
					<legend class="mb-2 block text-sm font-medium text-gray-700">Tipo de Clase</legend>
					<div class="flex space-x-4">
						<label class="flex items-center">
							<input type="radio" bind:group={tipoClase} value="curso" class="mr-2" />
							Curso
						</label>
						<label class="flex items-center">
							<input type="radio" bind:group={tipoClase} value="taller" class="mr-2" />
							Taller
						</label>
					</div>
				</fieldset>
			</div>

			<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
				<!-- Basic Information -->
				<div class="space-y-6">
					<div>
						<label for="titulo" class="mb-1 block text-sm font-medium text-gray-700">
							Título *
						</label>
						<input
							id="titulo"
							type="text"
							bind:value={titulo}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Título de la clase"
						/>
					</div>

					<div>
						<label for="descripcion" class="mb-1 block text-sm font-medium text-gray-700">
							Descripción
						</label>
						<textarea
							id="descripcion"
							bind:value={descripcion}
							rows="4"
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="Descripción de la clase"
						></textarea>
					</div>

					<div>
						<label for="precio" class="mb-1 block text-sm font-medium text-gray-700">
							Precio (€) *
						</label>
						<input
							id="precio"
							type="number"
							bind:value={precio}
							min="0"
							step="0.01"
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="0.00"
						/>
					</div>

					<div>
						<label for="imagenPortada" class="mb-1 block text-sm font-medium text-gray-700">
							URL de Imagen de Portada
						</label>
						<input
							id="imagenPortada"
							type="url"
							bind:value={imagenPortada}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							placeholder="https://ejemplo.com/imagen.jpg"
						/>
					</div>
				</div>

				<!-- Class Settings -->
				<div class="space-y-6">
					<div>
						<label for="nivel" class="mb-1 block text-sm font-medium text-gray-700">
							Nivel *
						</label>
						<select
							id="nivel"
							bind:value={nivel}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						>
							<option value="PRINCIPIANTE">Principiante</option>
							<option value="INTERMEDIO">Intermedio</option>
							<option value="AVANZADO">Avanzado</option>
						</select>
					</div>

					<div>
						<label for="presencialidad" class="mb-1 block text-sm font-medium text-gray-700">
							Presencialidad *
						</label>
						<select
							id="presencialidad"
							bind:value={presencialidad}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						>
							<option value="ONLINE">Online</option>
							<option value="PRESENCIAL">Presencial</option>
						</select>
					</div>

					<div>
						<label for="fechaInicio" class="mb-1 block text-sm font-medium text-gray-700">
							Fecha de Inicio *
						</label>
						<input
							id="fechaInicio"
							type="date"
							bind:value={fechaInicio}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
					</div>

					<div>
						<label for="fechaFin" class="mb-1 block text-sm font-medium text-gray-700">
							Fecha de Fin *
						</label>
						<input
							id="fechaFin"
							type="date"
							bind:value={fechaFin}
							required={tipoClase === 'curso'}
							disabled={tipoClase === 'taller'}
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none disabled:bg-gray-100"
						/>
					</div>
				</div>
			</div>

			<!-- Workshop-specific fields -->
			{#if tipoClase === 'taller'}
				<div class="mt-6 grid grid-cols-1 gap-6 md:grid-cols-3">
					<div>
						<label for="fechaRealizacion" class="mb-1 block text-sm font-medium text-gray-700">
							Fecha de Realización *
						</label>
						<input
							id="fechaRealizacion"
							type="date"
							bind:value={fechaRealizacion}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
					</div>

					<div>
						<label for="horaComienzo" class="mb-1 block text-sm font-medium text-gray-700">
							Hora de Comienzo *
						</label>
						<input
							id="horaComienzo"
							type="time"
							bind:value={horaComienzo}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
					</div>

					<div>
						<label for="duracionHoras" class="mb-1 block text-sm font-medium text-gray-700">
							Duración (horas) *
						</label>
						<input
							id="duracionHoras"
							type="number"
							bind:value={duracionHoras}
							min="1"
							max="24"
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
					</div>
				</div>
			{/if}

			<!-- Professors Section -->
			<div class="mt-8">
				<h3 class="mb-4 text-lg font-medium text-gray-900">Profesores</h3>
				<div class="space-y-4">
					<button
						type="button"
						onclick={agregarProfesor}
						class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
					>
						Agregar Profesor
					</button>
					{#if profesoresId.length > 0}
						<div class="space-y-2">
							{#each profesoresId as profesorId, index (index)}
								<div class="flex items-center space-x-2">
									<span class="flex-1 rounded-md bg-gray-100 px-3 py-2">{profesorId}</span>
									<button
										type="button"
										onclick={() => removerProfesor(index)}
										class="text-red-600 hover:text-red-800"
									>
										Eliminar
									</button>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>

			<!-- Materials Section -->
			<div class="mt-8">
				<h3 class="mb-4 text-lg font-medium text-gray-900">Materiales</h3>
				<div class="space-y-4">
					<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
						<input
							type="text"
							bind:value={nuevoMaterial.nombre}
							placeholder="Nombre del material"
							class="rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
						<input
							type="url"
							bind:value={nuevoMaterial.url}
							placeholder="URL del material"
							class="rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						/>
					</div>
					<button
						type="button"
						onclick={agregarMaterial}
						class="rounded-md bg-green-600 px-4 py-2 text-white hover:bg-green-700"
					>
						Agregar Material
					</button>
					{#if material.length > 0}
						<div class="space-y-2">
							{#each material as materialItem, index (index)}
								<div class="flex items-center space-x-2 rounded-md bg-gray-50 p-3">
									<div class="flex-1">
										<div class="font-medium">{materialItem.nombre}</div>
										<div class="text-sm text-gray-600">{materialItem.url}</div>
									</div>
									<button
										type="button"
										onclick={() => removerMaterial(index)}
										class="text-red-600 hover:text-red-800"
									>
										Eliminar
									</button>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>

			<!-- Form Actions -->
			<div class="mt-8 flex justify-end space-x-4 border-t border-gray-200 pt-6">
				<button
					type="button"
					onclick={resetForm}
					class="rounded-md border border-gray-300 px-6 py-2 text-gray-700 hover:bg-gray-50"
				>
					Limpiar
				</button>
				<button
					type="submit"
					disabled={loading}
					class="rounded-md bg-blue-600 px-6 py-2 text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
				>
					{loading ? 'Creando...' : `Crear ${tipoClase === 'curso' ? 'Curso' : 'Taller'}`}
				</button>
			</div>
		</form>
	</div>
</div>
