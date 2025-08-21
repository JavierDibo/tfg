<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOPeticionRegistroAlumno } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { authStore } from '$lib/stores/authStore.svelte';

	// Form state
	let formData: DTOPeticionRegistroAlumno = $state({
		usuario: '',
		contraseña: '',
		nombre: '',
		apellidos: '',
		dni: '',
		email: '',
		telefono: '',
		direccion: '',
		fechaNacimiento: ''
	});

	// UI state
	let loading = $state(false);
	let errors: string[] = $state([]);
	let fieldErrors: Record<string, string> = $state({});
	let showPassword = $state(false);

	// Check authentication and permissions
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isAdmin) {
			goto('/alumnos');
			return;
		}
	});

	// Form validation
	function validateForm(): boolean {
		errors = [];
		fieldErrors = {};

		// Client-side validation using the service
		const validationErrors = AlumnoService.validateRegistrationData(formData);

		if (validationErrors.length > 0) {
			errors = validationErrors;
			return false;
		}

		// Additional custom validations
		if (formData.telefono && !/^\+?[\d\s-()]+$/.test(formData.telefono)) {
			fieldErrors.telefono = 'El formato del teléfono no es válido';
			return false;
		}

		return true;
	}

	async function handleSubmit() {
		if (!validateForm()) {
			return;
		}

		loading = true;
		errors = [];
		fieldErrors = {};

		try {
			const newAlumno = await AlumnoService.createAlumno(formData);

			// Redirect to the new student's profile
			goto(`/alumnos/${newAlumno.id}?created=true`);
		} catch (error: any) {
			loading = false;

			// Handle specific field errors from backend
			if (error.message.includes('usuario')) {
				fieldErrors.usuario = 'El nombre de usuario ya existe';
			} else if (error.message.includes('email')) {
				fieldErrors.email = 'El email ya está registrado';
			} else if (error.message.includes('dni')) {
				fieldErrors.dni = 'El DNI ya está registrado';
			} else {
				errors = [error.message];
			}
		}
	}

	function resetForm() {
		formData = {
			usuario: '',
			contraseña: '',
			nombre: '',
			apellidos: '',
			dni: '',
			email: '',
			telefono: '',
			direccion: '',
			fechaNacimiento: ''
		};
		errors = [];
		fieldErrors = {};
	}

	// Real-time field validation
	function validateField(field: keyof DTOPeticionRegistroAlumno, value: string) {
		delete fieldErrors[field];

		switch (field) {
			case 'usuario':
				if (value && (value.length < 3 || value.length > 50)) {
					fieldErrors.usuario = 'El usuario debe tener entre 3 y 50 caracteres';
				}
				break;
			case 'contraseña':
				if (value && value.length < 6) {
					fieldErrors.contraseña = 'La contraseña debe tener al menos 6 caracteres';
				}
				break;
			case 'email':
				if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
					fieldErrors.email = 'El formato del email no es válido';
				}
				break;
			case 'dni':
				if (value && value.length > 20) {
					fieldErrors.dni = 'El DNI no puede superar 20 caracteres';
				}
				break;
			case 'nombre':
				if (value && value.length > 100) {
					fieldErrors.nombre = 'El nombre no puede superar 100 caracteres';
				}
				break;
			case 'apellidos':
				if (value && value.length > 100) {
					fieldErrors.apellidos = 'Los apellidos no pueden superar 100 caracteres';
				}
				break;
			case 'telefono':
				if (value && value.length > 15) {
					fieldErrors.telefono = 'El teléfono no puede superar 15 caracteres';
				}
				break;
			case 'direccion':
				if (value && value.length > 200) {
					fieldErrors.direccion = 'La dirección no puede superar 200 caracteres';
				}
				break;
		}
	}
</script>

<div class="container mx-auto max-w-2xl px-4 py-8">
	<!-- Header -->
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Nuevo Alumno</h1>
		<button onclick={() => goto('/alumnos')} class="text-gray-600 hover:text-gray-800">
			← Volver a Alumnos
		</button>
	</div>

	<!-- Error Messages -->
	{#if errors.length > 0}
		<div class="mb-6 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			<ul class="list-inside list-disc">
				{#each errors as error}
					<li>{error}</li>
				{/each}
			</ul>
		</div>
	{/if}

	<!-- Registration Form -->
	<form
		onsubmit={(e) => {
			e.preventDefault();
			handleSubmit();
		}}
		class="rounded-lg bg-white p-6 shadow-md"
	>
		<!-- Account Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de Cuenta</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- Username -->
				<div>
					<label for="usuario" class="mb-1 block text-sm font-medium text-gray-700">
						Usuario *
					</label>
					<input
						id="usuario"
						type="text"
						bind:value={formData.usuario}
						oninput={(e) => validateField('usuario', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.usuario
							? 'border-red-500'
							: ''}"
						placeholder="Nombre de usuario único"
					/>
					{#if fieldErrors.usuario}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.usuario}</p>
					{/if}
				</div>

				<!-- Password -->
				<div>
					<label for="contraseña" class="mb-1 block text-sm font-medium text-gray-700">
						Contraseña *
					</label>
					<div class="relative">
						<input
							id="contraseña"
							type={showPassword ? 'text' : 'password'}
							bind:value={formData.contraseña}
							oninput={(e) => validateField('contraseña', (e.target as HTMLInputElement).value)}
							required
							class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.contraseña
								? 'border-red-500'
								: ''}"
							placeholder="Mínimo 6 caracteres"
						/>
						<button
							type="button"
							onclick={() => (showPassword = !showPassword)}
							class="absolute inset-y-0 right-0 flex items-center pr-3 text-sm leading-5"
						>
							{showPassword ? '🙈' : '👁️'}
						</button>
					</div>
					{#if fieldErrors.contraseña}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.contraseña}</p>
					{/if}
				</div>
			</div>
		</div>

		<!-- Personal Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información Personal</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- First Name -->
				<div>
					<label for="nombre" class="mb-1 block text-sm font-medium text-gray-700">
						Nombre *
					</label>
					<input
						id="nombre"
						type="text"
						bind:value={formData.nombre}
						oninput={(e) => validateField('nombre', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.nombre
							? 'border-red-500'
							: ''}"
						placeholder="Nombre del alumno"
					/>
					{#if fieldErrors.nombre}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.nombre}</p>
					{/if}
				</div>

				<!-- Last Name -->
				<div>
					<label for="apellidos" class="mb-1 block text-sm font-medium text-gray-700">
						Apellidos *
					</label>
					<input
						id="apellidos"
						type="text"
						bind:value={formData.apellidos}
						oninput={(e) => validateField('apellidos', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.apellidos
							? 'border-red-500'
							: ''}"
						placeholder="Apellidos del alumno"
					/>
					{#if fieldErrors.apellidos}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.apellidos}</p>
					{/if}
				</div>

				<!-- DNI -->
				<div>
					<label for="dni" class="mb-1 block text-sm font-medium text-gray-700"> DNI * </label>
					<input
						id="dni"
						type="text"
						bind:value={formData.dni}
						oninput={(e) => validateField('dni', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.dni
							? 'border-red-500'
							: ''}"
						placeholder="Documento de identidad"
					/>
					{#if fieldErrors.dni}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.dni}</p>
					{/if}
				</div>

				<!-- Email -->
				<div>
					<label for="email" class="mb-1 block text-sm font-medium text-gray-700"> Email * </label>
					<input
						id="email"
						type="email"
						bind:value={formData.email}
						oninput={(e) => validateField('email', (e.target as HTMLInputElement).value)}
						required
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.email
							? 'border-red-500'
							: ''}"
						placeholder="correo@ejemplo.com"
					/>
					{#if fieldErrors.email}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.email}</p>
					{/if}
				</div>
			</div>
		</div>

		<!-- Contact Information -->
		<div class="mb-6">
			<h2 class="mb-4 text-xl font-semibold text-gray-900">Información de Contacto</h2>

			<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
				<!-- Phone -->
				<div>
					<label for="telefono" class="mb-1 block text-sm font-medium text-gray-700">
						Número de Teléfono
					</label>
					<input
						id="telefono"
						type="tel"
						bind:value={formData.telefono}
						oninput={(e) => validateField('telefono', (e.target as HTMLInputElement).value)}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.telefono
							? 'border-red-500'
							: ''}"
						placeholder="+34 XXX XXX XXX (opcional)"
					/>
					{#if fieldErrors.telefono}
						<p class="mt-1 text-xs text-red-500">{fieldErrors.telefono}</p>
					{/if}
					<p class="mt-1 text-xs text-gray-500">Campo opcional</p>
				</div>

				<!-- Birth Date -->
				<div>
					<label for="fechaNacimiento" class="mb-1 block text-sm font-medium text-gray-700">
						Fecha de Nacimiento
					</label>
					<input
						id="fechaNacimiento"
						type="date"
						bind:value={formData.fechaNacimiento}
						class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
						placeholder="YYYY-MM-DD (opcional)"
					/>
					<p class="mt-1 text-xs text-gray-500">Campo opcional</p>
				</div>
			</div>

			<!-- Address -->
			<div class="mt-4">
				<label for="direccion" class="mb-1 block text-sm font-medium text-gray-700">
					Dirección
				</label>
				<textarea
					id="direccion"
					bind:value={formData.direccion}
					oninput={(e) => validateField('direccion', (e.target as HTMLTextAreaElement).value)}
					rows="3"
					class="w-full rounded-md border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none {fieldErrors.direccion
						? 'border-red-500'
						: ''}"
					placeholder="Dirección completa del alumno (opcional)"
				></textarea>
				{#if fieldErrors.direccion}
					<p class="mt-1 text-xs text-red-500">{fieldErrors.direccion}</p>
				{/if}
				<p class="mt-1 text-xs text-gray-500">Campo opcional</p>
			</div>
		</div>

		<!-- Form Actions -->
		<div class="flex justify-between space-x-4 border-t border-gray-200 pt-6">
			<div class="flex space-x-2">
				<button
					type="button"
					onclick={resetForm}
					class="rounded-md bg-gray-500 px-4 py-2 text-white hover:bg-gray-600 focus:ring-2 focus:ring-gray-500 focus:outline-none"
				>
					Limpiar Formulario
				</button>
				<button
					type="button"
					onclick={() => goto('/alumnos')}
					class="rounded-md bg-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-400 focus:ring-2 focus:ring-gray-500 focus:outline-none"
				>
					Cancelar
				</button>
			</div>

			<button
				type="submit"
				disabled={loading || Object.keys(fieldErrors).length > 0}
				class="rounded-md bg-blue-600 px-6 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none disabled:cursor-not-allowed disabled:bg-gray-400"
			>
				{#if loading}
					<span class="flex items-center">
						<svg
							class="mr-3 -ml-1 h-5 w-5 animate-spin text-white"
							xmlns="http://www.w3.org/2000/svg"
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
						Creando Alumno...
					</span>
				{:else}
					Crear Alumno
				{/if}
			</button>
		</div>
	</form>

	<!-- Form Help -->
	<div class="mt-6 rounded-lg border border-blue-200 bg-blue-50 p-4">
		<h3 class="mb-2 text-sm font-medium text-blue-900">Información sobre el formulario:</h3>
		<ul class="space-y-1 text-sm text-blue-700">
			<li>• Los campos marcados con (*) son obligatorios</li>
			<li>• El usuario debe ser único en el sistema</li>
			<li>• El email y DNI también deben ser únicos</li>
			<li>• La contraseña debe tener al menos 6 caracteres</li>
			<li>• El alumno se creará con estado habilitado y no matriculado por defecto</li>
		</ul>
	</div>
</div>
