<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import type { DTOAlumno, DTOActualizacionAlumno, DTOClase } from '$lib/generated/api';
	import { AlumnoService } from '$lib/services/alumnoService';
	import { EnrollmentService } from '$lib/services/enrollmentService';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { alumnoApi } from '$lib/api';

	// Props and derived state
	const studentId = $derived(parseInt($page.params.id));
	const isCreated = $derived($page.url.searchParams.get('created') === 'true');

	// State management
	let alumno: DTOAlumno | null = $state(null);
	let editMode = $state(false);
	let loading = $state(false);
	let saving = $state(false);
	let error = $state<string | null>(null);
	let successMessage = $state<string | null>(null);
	let enrolledClasses = $state<DTOClase[]>([]);
	let loadingClasses = $state(false);

	// Edit form state
	let editForm: DTOActualizacionAlumno = $state({});

	// Permissions
	const canEdit = $derived(() => {
		if (!alumno) return false;

		// Admin can edit anyone
		if (authStore.isAdmin) return true;

		// Student can only edit their own profile
		if (
			authStore.isAlumno &&
			(authStore.user?.usuario === alumno.username || authStore.user?.sub === alumno.username)
		)
			return true;

		return false;
	});

	const canChangeStatus = $derived(() => authStore.isAdmin);

	// Check if current user is viewing their own profile
	const isOwnProfile = $derived(() => {
		if (!alumno || !authStore.user) return false;
		return authStore.user.usuario === alumno.username || authStore.user.sub === alumno.username;
	});

	// Check if user can see sensitive information (admin only)
	const canSeeSensitiveInfo = $derived(() => authStore.isAdmin);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Show success message if just created
	$effect(() => {
		if (isCreated) {
			successMessage = 'Alumno creado exitosamente';
			setTimeout(() => (successMessage = null), 5000);
		}
	});

	onMount(() => {
		loadAlumno();
	});

	// Load enrolled classes if viewing own profile or if admin is viewing any profile
	$effect(() => {
		if (alumno && (isOwnProfile() || authStore.isAdmin)) {
			loadEnrolledClasses();
		}
	});

	async function loadAlumno() {
		if (!studentId || isNaN(studentId)) {
			error = 'ID de alumno inválido';
			return;
		}

		loading = true;
		error = null;

		try {
			// If student is viewing their own profile, get their data by ID
			if (isOwnProfile()) {
				const userId = authStore.user?.id;

				if (!userId) {
					throw new Error('User ID not available from authentication');
				}

				alumno = await AlumnoService.getAlumno(userId);
			} else {
				alumno = await AlumnoService.getAlumno(studentId);

				// Check if current user can access this profile
				if (!authStore.isAdmin && !authStore.isProfesor) {
					if (
						!authStore.isAlumno ||
						(authStore.user?.usuario !== alumno.username && authStore.user?.sub !== alumno.username)
					) {
						error = 'No tienes permisos para ver este perfil';
						return;
					}
				}
			}
		} catch (err) {
			error = `Error al cargar el alumno: ${err}`;
		} finally {
			loading = false;
		}
	}

	async function loadEnrolledClasses() {
		if (!alumno?.id) return;

		try {
			loadingClasses = true;

			// Use different endpoints based on who is viewing the profile
			if (isOwnProfile()) {
				// Student viewing their own profile - use the enrollment service
				enrolledClasses = await EnrollmentService.getMyEnrolledClasses();
			} else if (authStore.isAdmin) {
				// Admin viewing any student's profile - use the student API directly
				enrolledClasses = await alumnoApi.obtenerClasesInscritas({ id: alumno.id });
			}
		} catch (err) {
			console.error('Error loading enrolled classes:', err);
		} finally {
			loadingClasses = false;
		}
	}

	function startEdit() {
		if (!alumno || !canEdit) return;

		editForm = {
			firstName: alumno.firstName || '',
			lastName: alumno.lastName || '',
			dni: alumno.dni || '',
			email: alumno.email || '',
			phoneNumber: alumno.phoneNumber || ''
		};
		editMode = true;
	}

	function cancelEdit() {
		editMode = false;
		editForm = {};
		error = null;
	}

	// ==================== VALIDATION FUNCTIONS ====================

	/**
	 * Valida nombres y apellidos: solo letras, acentos, espacios, máximo 100 caracteres
	 */
	function validateName(name: string): { isValid: boolean; message: string } {
		if (!name || name.trim().length === 0) {
			return { isValid: false, message: 'Este campo es obligatorio' };
		}
		if (name.length > 100) {
			return { isValid: false, message: 'Máximo 100 caracteres' };
		}
		const nameRegex = /^[a-zA-ZáéíóúñÁÉÍÓÚÑ\s]+$/;
		if (!nameRegex.test(name)) {
			return { isValid: false, message: 'Solo se permiten letras, acentos y espacios' };
		}
		return { isValid: true, message: '✓ Válido' };
	}

	/**
	 * Valida DNI español: 8 números + 1 letra calculada
	 */
	function validateDNI(dni: string): { isValid: boolean; message: string } {
		if (!dni || dni.trim().length === 0) {
			return { isValid: false, message: 'El DNI es obligatorio' };
		}

		const dniRegex = /^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$/i;
		if (!dniRegex.test(dni)) {
			return { isValid: false, message: 'Formato: 8 números + 1 letra (ej: 12345678Z)' };
		}

		// Calcular letra correcta
		const numbers = dni.substring(0, 8);
		const letter = dni.substring(8, 9).toUpperCase();
		const correctLetters = 'TRWAGMYFPDXBNJZSQVHLCKE';
		const correctLetter = correctLetters[parseInt(numbers) % 23];

		if (letter !== correctLetter) {
			return { isValid: false, message: `La letra debería ser ${correctLetter}` };
		}

		return { isValid: true, message: '✓ DNI válido' };
	}

	/**
	 * Valida email con límites específicos
	 */
	function validateEmail(email: string): { isValid: boolean; message: string } {
		if (!email || email.trim().length === 0) {
			return { isValid: false, message: 'El email es obligatorio' };
		}

		if (email.length > 254) {
			return { isValid: false, message: 'Máximo 254 caracteres' };
		}

		const [localPart] = email.split('@');
		if (localPart && localPart.length > 64) {
			return { isValid: false, message: 'La parte local no puede superar 64 caracteres' };
		}

		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			return { isValid: false, message: 'Formato de email inválido' };
		}

		// Verificar puntos consecutivos
		if (email.includes('..')) {
			return { isValid: false, message: 'No se permiten puntos consecutivos' };
		}

		return { isValid: true, message: '✓ Email válido' };
	}

	/**
	 * Valida teléfono: 6-14 dígitos, prefijos internacionales, caracteres permitidos
	 */
	function validatePhoneNumber(phone: string): { isValid: boolean; message: string } {
		if (!phone || phone.trim().length === 0) {
			return { isValid: true, message: 'Campo opcional' }; // Es opcional
		}

		// Caracteres permitidos: números, espacios, guiones, puntos, paréntesis, +
		const allowedCharsRegex = /^[0-9+\-\s().]+$/;
		if (!allowedCharsRegex.test(phone)) {
			return { isValid: false, message: 'Solo números, espacios, guiones, puntos, paréntesis y +' };
		}

		// Extraer solo los dígitos
		const digits = phone.replace(/[^0-9]/g, '');

		if (digits.length < 6) {
			return { isValid: false, message: 'Mínimo 6 dígitos' };
		}

		if (digits.length > 14) {
			return { isValid: false, message: 'Máximo 14 dígitos' };
		}

		return { isValid: true, message: '✓ Teléfono válido' };
	}

	/**
	 * Verifica si un campo debe enviarse en el PATCH (no vacío)
	 */
	function shouldIncludeField(value: string | undefined): boolean {
		return value !== undefined && value !== null && value.trim() !== '';
	}

	/**
	 * Verifica si el formulario completo es válido
	 */
	function isFormValid(): boolean {
		// Verificar que todos los campos que tienen contenido sean válidos
		if (editForm.firstName && !validateName(editForm.firstName).isValid) return false;
		if (editForm.lastName && !validateName(editForm.lastName).isValid) return false;
		if (editForm.dni && !validateDNI(editForm.dni).isValid) return false;
		if (editForm.email && !validateEmail(editForm.email).isValid) return false;
		if (editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid) return false;

		// Verificar que al menos un campo tenga contenido para enviar
		return (
			shouldIncludeField(editForm.firstName) ||
			shouldIncludeField(editForm.lastName) ||
			shouldIncludeField(editForm.dni) ||
			shouldIncludeField(editForm.email) ||
			shouldIncludeField(editForm.phoneNumber)
		);
	}

	/**
	 * Verifica si hay errores de validación en el formulario
	 */
	function hasFormErrors(): boolean {
		return Boolean(
			(editForm.firstName && !validateName(editForm.firstName).isValid) ||
				(editForm.lastName && !validateName(editForm.lastName).isValid) ||
				(editForm.dni && !validateDNI(editForm.dni).isValid) ||
				(editForm.email && !validateEmail(editForm.email).isValid) ||
				(editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid)
		);
	}

	async function saveChanges() {
		if (!alumno) return;

		saving = true;
		error = null;

		try {
			// Validar todos los campos que van a enviarse
			const validationErrors: string[] = [];

			// Solo incluir campos que no estén vacíos (PATCH parcial)
			const updateData: DTOActualizacionAlumno = {};

			// Validar y incluir nombre si está presente
			if (shouldIncludeField(editForm.firstName)) {
				const nameValidation = validateName(editForm.firstName!);
				if (!nameValidation.isValid) {
					validationErrors.push(`Nombre: ${nameValidation.message}`);
				} else {
					updateData.firstName = editForm.firstName;
				}
			}

			// Validar y incluir apellidos si está presente
			if (shouldIncludeField(editForm.lastName)) {
				const apellidosValidation = validateName(editForm.lastName!);
				if (!apellidosValidation.isValid) {
					validationErrors.push(`Apellidos: ${apellidosValidation.message}`);
				} else {
					updateData.lastName = editForm.lastName;
				}
			}

			// Validar y incluir DNI si está presente
			if (shouldIncludeField(editForm.dni)) {
				const dniValidation = validateDNI(editForm.dni!);
				if (!dniValidation.isValid) {
					validationErrors.push(`DNI: ${dniValidation.message}`);
				} else {
					updateData.dni = editForm.dni;
				}
			}

			// Validar y incluir email si está presente
			if (shouldIncludeField(editForm.email)) {
				const emailValidation = validateEmail(editForm.email!);
				if (!emailValidation.isValid) {
					validationErrors.push(`Email: ${emailValidation.message}`);
				} else {
					updateData.email = editForm.email;
				}
			}

			// Validar y incluir teléfono si está presente
			if (shouldIncludeField(editForm.phoneNumber)) {
				const phoneValidation = validatePhoneNumber(editForm.phoneNumber!);
				if (!phoneValidation.isValid) {
					validationErrors.push(`Teléfono: ${phoneValidation.message}`);
				} else {
					updateData.phoneNumber = editForm.phoneNumber;
				}
			}

			// Si hay errores de validación, mostrarlos
			if (validationErrors.length > 0) {
				error = validationErrors.join('. ');
				return;
			}

			// Si no hay datos para actualizar
			if (Object.keys(updateData).length === 0) {
				error = 'No hay cambios para guardar';
				return;
			}

			const updatedAlumno = await AlumnoService.updateAlumno(alumno.id!, updateData);
			alumno = updatedAlumno;
			editMode = false;
			editForm = {};
			successMessage = 'Perfil actualizado correctamente';
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al actualizar el perfil: ${err}`;
		} finally {
			saving = false;
		}
	}

	async function toggleEnrollmentStatus() {
		if (!alumno || !canChangeStatus) return;

		try {
			console.log('Before toggle - enrolled:', alumno.enrolled);
			const updatedAlumno = await AlumnoService.changeEnrollmentStatus(
				alumno.id!,
				!alumno.enrolled
			);
			console.log('After toggle - updatedAlumno:', updatedAlumno);
			alumno = updatedAlumno;
			console.log('After assignment - alumno.enrolled:', alumno.enrolled);

			// Force a reload of the student data to ensure UI is updated
			await loadAlumno();

			successMessage = `Estado de matrícula ${updatedAlumno.enrolled ? 'activado' : 'desactivado'} correctamente`;
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al cambiar estado de matrícula: ${err}`;
		}
	}

	async function toggleAccountStatus() {
		if (!alumno || !canChangeStatus) return;

		try {
			console.log('Before toggle - enabled:', alumno.enabled);
			const updatedAlumno = await AlumnoService.toggleEnabled(alumno.id!, !alumno.enabled);
			console.log('After toggle - updatedAlumno:', updatedAlumno);
			alumno = updatedAlumno;
			console.log('After assignment - alumno.enabled:', alumno.enabled);

			// Force a reload of the student data to ensure UI is updated
			await loadAlumno();

			successMessage = `Cuenta ${updatedAlumno.enabled ? 'habilitada' : 'deshabilitada'} correctamente`;
			setTimeout(() => (successMessage = null), 3000);
		} catch (err) {
			error = `Error al cambiar estado de cuenta: ${err}`;
		}
	}

	function formatDate(date: Date | undefined): string {
		if (!date) return '-';
		return new Date(date).toLocaleDateString('es-ES', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
	}
</script>

<div class="container mx-auto max-w-4xl px-4 py-8">
	<!-- Header -->
	<div class="mb-6 flex items-center justify-between">
		<h1 class="text-3xl font-bold text-gray-900">Perfil de Alumno</h1>
		<button onclick={() => goto('/alumnos')} class="text-gray-600 hover:text-gray-800">
			← Volver a Alumnos
		</button>
	</div>

	<!-- Success/Error Messages -->
	{#if successMessage}
		<div class="mb-4 rounded border border-green-400 bg-green-100 px-4 py-3 text-green-700">
			{successMessage}
		</div>
	{/if}

	{#if error}
		<div class="mb-4 rounded border border-red-400 bg-red-100 px-4 py-3 text-red-700">
			{error}
			<button onclick={() => (error = null)} class="float-right text-red-500 hover:text-red-700">
				×
			</button>
		</div>
	{/if}

	<!-- Loading State -->
	{#if loading}
		<div class="py-12 text-center">
			<div class="mx-auto h-12 w-12 animate-spin rounded-full border-b-2 border-blue-500"></div>
			<p class="mt-4 text-gray-600">Cargando perfil del alumno...</p>
		</div>
	{:else if alumno}
		<div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
			<!-- Main Profile Info -->
			<div class="lg:col-span-2">
				<div class="rounded-lg bg-white p-6 shadow-md">
					<div class="mb-6 flex items-start justify-between">
						<div>
							<h2 class="text-2xl font-bold text-gray-900">
								{alumno.firstName}
								{alumno.lastName}
							</h2>
							<p class="text-gray-600">@{alumno.username}</p>
						</div>

						{#if canEdit() && !editMode}
							<button
								onclick={startEdit}
								class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:outline-none"
							>
								Editar Datos
							</button>
						{/if}
					</div>

					{#if editMode}
						<!-- Edit Form -->
						<form
							onsubmit={(e) => {
								e.preventDefault();
								saveChanges();
							}}
							class="space-y-4"
						>
							<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
								<!-- NOMBRE -->
								<div>
									<label for="nombre" class="mb-1 block text-sm font-medium text-gray-700">
										Nombre <span class="text-red-500">*</span>
									</label>
									<div class="relative">
										<input
											id="firstName"
											type="text"
											bind:value={editForm.firstName}
											maxlength="100"
											class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.firstName
												? validateName(editForm.firstName).isValid
													? 'border-green-500 bg-green-50'
													: 'border-red-500 bg-red-50'
												: 'border-gray-300'}"
											placeholder="Ej: Juan Carlos"
										/>
										{#if editForm.firstName}
											<div class="absolute inset-y-0 right-0 flex items-center pr-3">
												{#if validateName(editForm.firstName).isValid}
													<span class="text-green-500">✓</span>
												{:else}
													<span class="text-red-500">✗</span>
												{/if}
											</div>
										{/if}
									</div>
									{#if editForm.firstName}
										<p
											class="mt-1 text-xs {validateName(editForm.firstName).isValid
												? 'text-green-600'
												: 'text-red-600'}"
										>
											{validateName(editForm.firstName).message}
										</p>
									{/if}
									<p class="mt-1 text-xs text-gray-500">
										Solo letras, acentos y espacios. Máximo 100 caracteres.
									</p>
								</div>

								<!-- APELLIDOS -->
								<div>
									<label for="apellidos" class="mb-1 block text-sm font-medium text-gray-700">
										Apellidos <span class="text-red-500">*</span>
									</label>
									<div class="relative">
										<input
											id="lastName"
											type="text"
											bind:value={editForm.lastName}
											maxlength="100"
											class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.lastName
												? validateName(editForm.lastName).isValid
													? 'border-green-500 bg-green-50'
													: 'border-red-500 bg-red-50'
												: 'border-gray-300'}"
											placeholder="Ej: García López"
										/>
										{#if editForm.lastName}
											<div class="absolute inset-y-0 right-0 flex items-center pr-3">
												{#if validateName(editForm.lastName).isValid}
													<span class="text-green-500">✓</span>
												{:else}
													<span class="text-red-500">✗</span>
												{/if}
											</div>
										{/if}
									</div>
									{#if editForm.lastName}
										<p
											class="mt-1 text-xs {validateName(editForm.lastName).isValid
												? 'text-green-600'
												: 'text-red-600'}"
										>
											{validateName(editForm.lastName).message}
										</p>
									{/if}
									<p class="mt-1 text-xs text-gray-500">
										Solo letras, acentos y espacios. Máximo 100 caracteres.
									</p>
								</div>

								<!-- DNI -->
								<div>
									<label for="dni" class="mb-1 block text-sm font-medium text-gray-700">
										DNI <span class="text-red-500">*</span>
									</label>
									<div class="relative">
										<input
											id="dni"
											type="text"
											bind:value={editForm.dni}
											maxlength="9"
											class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.dni
												? validateDNI(editForm.dni).isValid
													? 'border-green-500 bg-green-50'
													: 'border-red-500 bg-red-50'
												: 'border-gray-300'}"
											placeholder="12345678Z"
											style="text-transform: uppercase;"
										/>
										{#if editForm.dni}
											<div class="absolute inset-y-0 right-0 flex items-center pr-3">
												{#if validateDNI(editForm.dni).isValid}
													<span class="text-green-500">✓</span>
												{:else}
													<span class="text-red-500">✗</span>
												{/if}
											</div>
										{/if}
									</div>
									{#if editForm.dni}
										<p
											class="mt-1 text-xs {validateDNI(editForm.dni).isValid
												? 'text-green-600'
												: 'text-red-600'}"
										>
											{validateDNI(editForm.dni).message}
										</p>
									{/if}
									<p class="mt-1 text-xs text-gray-500">
										8 números seguidos de 1 letra. Ej: 12345678Z
									</p>
								</div>

								<!-- EMAIL -->
								<div>
									<label for="email" class="mb-1 block text-sm font-medium text-gray-700">
										Email <span class="text-red-500">*</span>
									</label>
									<div class="relative">
										<input
											id="email"
											type="email"
											bind:value={editForm.email}
											maxlength="254"
											class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.email
												? validateEmail(editForm.email).isValid
													? 'border-green-500 bg-green-50'
													: 'border-red-500 bg-red-50'
												: 'border-gray-300'}"
											placeholder="usuario@universidad.es"
										/>
										{#if editForm.email}
											<div class="absolute inset-y-0 right-0 flex items-center pr-3">
												{#if validateEmail(editForm.email).isValid}
													<span class="text-green-500">✓</span>
												{:else}
													<span class="text-red-500">✗</span>
												{/if}
											</div>
										{/if}
									</div>
									{#if editForm.email}
										<p
											class="mt-1 text-xs {validateEmail(editForm.email).isValid
												? 'text-green-600'
												: 'text-red-600'}"
										>
											{validateEmail(editForm.email).message}
										</p>
									{/if}
									<p class="mt-1 text-xs text-gray-500">
										Máximo 254 caracteres. Parte local máximo 64 caracteres.
									</p>
								</div>

								<!-- TELÉFONO -->
								<div class="md:col-span-2">
									<label for="phoneNumber" class="mb-1 block text-sm font-medium text-gray-700">
										Teléfono <span class="text-gray-400">(Opcional)</span>
									</label>
									<div class="relative">
										<input
											id="phoneNumber"
											type="tel"
											bind:value={editForm.phoneNumber}
											class="w-full rounded-md border px-3 py-2 pr-10 focus:ring-2 focus:ring-blue-500 focus:outline-none {editForm.phoneNumber
												? validatePhoneNumber(editForm.phoneNumber).isValid
													? 'border-green-500 bg-green-50'
													: 'border-red-500 bg-red-50'
												: 'border-gray-300'}"
											placeholder="Ej: +34 123 456 789, (555) 123-4567, 123456789"
										/>
										{#if editForm.phoneNumber}
											<div class="absolute inset-y-0 right-0 flex items-center pr-3">
												{#if validatePhoneNumber(editForm.phoneNumber).isValid}
													<span class="text-green-500">✓</span>
												{:else}
													<span class="text-red-500">✗</span>
												{/if}
											</div>
										{/if}
									</div>
									{#if editForm.phoneNumber}
										<p
											class="mt-1 text-xs {validatePhoneNumber(editForm.phoneNumber).isValid
												? 'text-green-600'
												: 'text-red-600'}"
										>
											{validatePhoneNumber(editForm.phoneNumber).message}
										</p>
									{/if}
									<p class="mt-1 text-xs text-gray-500">
										6-14 dígitos. Permitidos: números, espacios, guiones, puntos, paréntesis y +
									</p>
								</div>
							</div>

							<div class="flex justify-end space-x-3 border-t pt-6">
								<button
									type="button"
									onclick={cancelEdit}
									class="rounded-md bg-gray-300 px-6 py-2 text-gray-700 transition-colors hover:bg-gray-400"
									disabled={saving}
								>
									Cancelar
								</button>
								<button
									type="submit"
									disabled={saving || !isFormValid()}
									class="rounded-md px-6 py-2 font-medium transition-colors {isFormValid() &&
									!saving
										? 'bg-blue-600 text-white hover:bg-blue-700'
										: 'cursor-not-allowed bg-gray-400 text-gray-200'}"
								>
									{saving
										? '🔄 Guardando...'
										: isFormValid()
											? '✓ Guardar Cambios'
											: '⚠️ Corregir Errores'}
								</button>
							</div>

							<!-- Form Validation Summary -->
							{#if hasFormErrors()}
								<div class="mt-4 rounded-md border border-yellow-200 bg-yellow-50 p-3">
									<h4 class="mb-2 text-sm font-medium text-yellow-800">⚠️ Campos con errores:</h4>
									<ul class="space-y-1 text-xs text-yellow-700">
										{#if editForm.firstName && !validateName(editForm.firstName).isValid}
											<li>• Nombre: {validateName(editForm.firstName).message}</li>
										{/if}
										{#if editForm.lastName && !validateName(editForm.lastName).isValid}
											<li>• Apellidos: {validateName(editForm.lastName).message}</li>
										{/if}
										{#if editForm.dni && !validateDNI(editForm.dni).isValid}
											<li>• DNI: {validateDNI(editForm.dni).message}</li>
										{/if}
										{#if editForm.email && !validateEmail(editForm.email).isValid}
											<li>• Email: {validateEmail(editForm.email).message}</li>
										{/if}
										{#if editForm.phoneNumber && !validatePhoneNumber(editForm.phoneNumber).isValid}
											<li>• Teléfono: {validatePhoneNumber(editForm.phoneNumber).message}</li>
										{/if}
									</ul>
								</div>
							{/if}
						</form>
					{:else}
						<!-- View Mode -->
						<div class="space-y-4">
							<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
								<div>
									<h3 class="mb-2 text-sm font-medium tracking-wide text-gray-500 uppercase">
										Información Personal
									</h3>
									<dl class="space-y-2">
										<div>
											<dt class="text-sm font-medium text-gray-900">Nombre Completo</dt>
											<dd class="text-sm text-gray-600">{alumno.firstName} {alumno.lastName}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">DNI</dt>
											<dd class="text-sm text-gray-600">{alumno.dni}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">Email</dt>
											<dd class="text-sm text-gray-600">{alumno.email}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">Teléfono</dt>
											<dd class="text-sm text-gray-600">
												{alumno.phoneNumber || 'No especificado'}
											</dd>
										</div>
									</dl>
								</div>

								<div>
									<h3 class="mb-2 text-sm font-medium tracking-wide text-gray-500 uppercase">
										Información Académica
									</h3>
									<dl class="space-y-2">
										<div>
											<dt class="text-sm font-medium text-gray-900">Usuario</dt>
											<dd class="text-sm text-gray-600">@{alumno.username}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">Fecha de Inscripción</dt>
											<dd class="text-sm text-gray-600">{formatDate(alumno.enrollmentDate)}</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">Estado de Matrícula</dt>
											<dd class="text-sm">
												<span
													class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enrolled
														? 'bg-green-100 text-green-800'
														: 'bg-yellow-100 text-yellow-800'}"
												>
													{alumno.enrolled ? 'Matriculado' : 'No Matriculado'}
												</span>
											</dd>
										</div>
										<div>
											<dt class="text-sm font-medium text-gray-900">Estado de Cuenta</dt>
											<dd class="text-sm">
												<span
													class="inline-flex rounded-full px-2 py-1 text-xs font-semibold {alumno.enabled
														? 'bg-blue-100 text-blue-800'
														: 'bg-red-100 text-red-800'}"
												>
													{alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
												</span>
											</dd>
										</div>
									</dl>
								</div>
							</div>
						</div>
					{/if}
				</div>

				<!-- Enrolled Classes Section (for own profile or admin viewing any profile) -->
				{#if isOwnProfile() || authStore.isAdmin}
					<div class="rounded-lg bg-white p-6 shadow-md">
						<h3 class="mb-4 text-lg font-semibold text-gray-900">
							{#if isOwnProfile()}
								Mis Clases Inscritas
							{:else}
								Clases Inscritas
							{/if}
						</h3>

						{#if loadingClasses}
							<div class="py-4 text-center">
								<div
									class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-500"
								></div>
								<p class="mt-2 text-sm text-gray-600">Cargando clases...</p>
							</div>
						{:else if enrolledClasses.length === 0}
							<div class="py-4 text-center">
								<p class="text-gray-500">
									{#if isOwnProfile()}
										No estás inscrito en ninguna clase
									{:else}
										El alumno no está inscrito en ninguna clase
									{/if}
								</p>
								{#if isOwnProfile()}
									<button
										onclick={() => goto('/clases')}
										class="mt-2 rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
									>
										Explorar Clases
									</button>
								{/if}
							</div>
						{:else}
							<div class="space-y-3">
								{#each enrolledClasses as clase (clase.id)}
									<div
										class="flex items-center justify-between rounded-lg border p-3 hover:bg-gray-50"
									>
										<div class="flex-1">
											<h4 class="font-medium text-gray-900">{clase.titulo}</h4>
											<p class="text-sm text-gray-600">{clase.descripcion}</p>
											<div class="mt-1 flex items-center gap-4 text-xs text-gray-500">
												<span class="flex items-center">
													<span class="mr-1">💰</span>
													{clase.precio}€
												</span>
												<span class="flex items-center">
													<span class="mr-1">📚</span>
													{clase.nivel}
												</span>
												<span class="flex items-center">
													<span class="mr-1">📍</span>
													{clase.presencialidad}
												</span>
											</div>
										</div>
										<button
											onclick={() => goto(`/clases/${clase.id}`)}
											class="rounded-md bg-blue-600 px-3 py-1 text-sm text-white hover:bg-blue-700"
										>
											Ver Detalles
										</button>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				{/if}
			</div>

			<!-- Action Panel -->
			<div class="space-y-6">
				{#if canChangeStatus()}
					<!-- Admin Actions -->
					<div class="rounded-lg bg-white p-6 shadow-md">
						<h3 class="mb-4 text-lg font-semibold text-gray-900">Acciones de Administrador</h3>

						<div class="space-y-3">
							<button
								onclick={toggleEnrollmentStatus}
								class="w-full rounded-md bg-green-600 px-4 py-2 text-white hover:bg-green-700 focus:ring-2 focus:ring-green-500 focus:outline-none"
							>
								{alumno.enrolled ? 'Desmatricular' : 'Matricular'} Alumno
							</button>

							<button
								onclick={toggleAccountStatus}
								class="w-full {alumno.enabled
									? 'bg-red-600 hover:bg-red-700 focus:ring-red-500'
									: 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'} rounded-md px-4 py-2 text-white focus:ring-2 focus:outline-none"
							>
								{alumno.enabled ? 'Deshabilitar' : 'Habilitar'} Cuenta
							</button>
						</div>
					</div>
				{/if}

				<!-- Quick Stats -->
				<div class="rounded-lg bg-white p-6 shadow-md">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Información Rápida</h3>

					<div class="space-y-3">
						{#if canSeeSensitiveInfo()}
							<div class="flex justify-between">
								<span class="text-sm text-gray-600">ID del Alumno:</span>
								<span class="text-sm font-medium text-gray-900">#{alumno.id}</span>
							</div>

							<div class="flex justify-between">
								<span class="text-sm text-gray-600">Estado:</span>
								<span
									class="text-sm font-medium {alumno.enabled ? 'text-green-600' : 'text-red-600'}"
								>
									{alumno.enabled ? 'Activo' : 'Inactivo'}
								</span>
							</div>
						{/if}

						<div class="flex justify-between">
							<span class="text-sm text-gray-600">Estado de Matrícula:</span>
							<span
								class="text-sm font-medium {alumno.enrolled ? 'text-green-600' : 'text-yellow-600'}"
							>
								{alumno.enrolled ? 'Matriculado' : 'No Matriculado'}
							</span>
						</div>

						{#if isOwnProfile()}
							<div class="flex justify-between">
								<span class="text-sm text-gray-600">Clases Inscritas:</span>
								<span class="text-sm font-medium text-blue-600">
									{alumno.classIds?.length || 0} clases
								</span>
							</div>
						{/if}
					</div>
				</div>

				<!-- Navigation -->
				<div class="rounded-lg bg-white p-6 shadow-md">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Navegación</h3>

					<div class="space-y-2">
						<button
							onclick={() => goto('/alumnos')}
							class="w-full text-left text-sm text-blue-600 hover:text-blue-800"
						>
							← Volver a Lista de Alumnos
						</button>

						{#if authStore.isAdmin}
							<button
								onclick={() => goto('/alumnos/nuevo')}
								class="w-full text-left text-sm text-blue-600 hover:text-blue-800"
							>
								+ Crear Nuevo Alumno
							</button>
						{/if}
					</div>
				</div>
			</div>
		</div>
	{:else if !loading}
		<div class="py-12 text-center">
			<p class="text-lg text-gray-500">Alumno no encontrado</p>
			<button
				onclick={() => goto('/alumnos')}
				class="mt-4 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
			>
				Volver a Alumnos
			</button>
		</div>
	{/if}
</div>
