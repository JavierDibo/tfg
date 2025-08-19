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

<div class="container mx-auto px-4 py-8 max-w-2xl">
  <!-- Header -->
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-3xl font-bold text-gray-900">Nuevo Alumno</h1>
    <button
      onclick={() => goto('/alumnos')}
      class="text-gray-600 hover:text-gray-800"
    >
      ← Volver a Alumnos
    </button>
  </div>

  <!-- Error Messages -->
  {#if errors.length > 0}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
      <ul class="list-disc list-inside">
        {#each errors as error}
          <li>{error}</li>
        {/each}
      </ul>
    </div>
  {/if}

  <!-- Registration Form -->
  <form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }} class="bg-white shadow-md rounded-lg p-6">
    <!-- Account Information -->
    <div class="mb-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Información de Cuenta</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Username -->
        <div>
          <label for="usuario" class="block text-sm font-medium text-gray-700 mb-1">
            Usuario *
          </label>
          <input
            id="usuario"
            type="text"
            bind:value={formData.usuario}
            oninput={(e) => validateField('usuario', (e.target as HTMLInputElement).value)}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.usuario ? 'border-red-500' : ''}"
            placeholder="Nombre de usuario único"
          />
          {#if fieldErrors.usuario}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.usuario}</p>
          {/if}
        </div>

        <!-- Password -->
        <div>
          <label for="contraseña" class="block text-sm font-medium text-gray-700 mb-1">
            Contraseña *
          </label>
          <div class="relative">
            <input
              id="contraseña"
              type={showPassword ? 'text' : 'password'}
              bind:value={formData.contraseña}
              oninput={(e) => validateField('contraseña', (e.target as HTMLInputElement).value)}
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.contraseña ? 'border-red-500' : ''}"
              placeholder="Mínimo 6 caracteres"
            />
            <button
              type="button"
              onclick={() => showPassword = !showPassword}
              class="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5"
            >
              {showPassword ? '🙈' : '👁️'}
            </button>
          </div>
          {#if fieldErrors.contraseña}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.contraseña}</p>
          {/if}
        </div>
      </div>
    </div>

    <!-- Personal Information -->
    <div class="mb-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Información Personal</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- First Name -->
        <div>
          <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
            Nombre *
          </label>
          <input
            id="nombre"
            type="text"
            bind:value={formData.nombre}
            oninput={(e) => validateField('nombre', (e.target as HTMLInputElement).value)}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.nombre ? 'border-red-500' : ''}"
            placeholder="Nombre del alumno"
          />
          {#if fieldErrors.nombre}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.nombre}</p>
          {/if}
        </div>

        <!-- Last Name -->
        <div>
          <label for="apellidos" class="block text-sm font-medium text-gray-700 mb-1">
            Apellidos *
          </label>
          <input
            id="apellidos"
            type="text"
            bind:value={formData.apellidos}
            oninput={(e) => validateField('apellidos', (e.target as HTMLInputElement).value)}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.apellidos ? 'border-red-500' : ''}"
            placeholder="Apellidos del alumno"
          />
          {#if fieldErrors.apellidos}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.apellidos}</p>
          {/if}
        </div>

        <!-- DNI -->
        <div>
          <label for="dni" class="block text-sm font-medium text-gray-700 mb-1">
            DNI *
          </label>
          <input
            id="dni"
            type="text"
            bind:value={formData.dni}
            oninput={(e) => validateField('dni', (e.target as HTMLInputElement).value)}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.dni ? 'border-red-500' : ''}"
            placeholder="Documento de identidad"
          />
          {#if fieldErrors.dni}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.dni}</p>
          {/if}
        </div>

        <!-- Email -->
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
            Email *
          </label>
          <input
            id="email"
            type="email"
            bind:value={formData.email}
            oninput={(e) => validateField('email', (e.target as HTMLInputElement).value)}
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.email ? 'border-red-500' : ''}"
            placeholder="correo@ejemplo.com"
          />
          {#if fieldErrors.email}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.email}</p>
          {/if}
        </div>
      </div>
    </div>

    <!-- Contact Information -->
    <div class="mb-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Información de Contacto</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Phone -->
        <div>
          <label for="telefono" class="block text-sm font-medium text-gray-700 mb-1">
            Número de Teléfono
          </label>
          <input
            id="telefono"
            type="tel"
            bind:value={formData.telefono}
            oninput={(e) => validateField('telefono', (e.target as HTMLInputElement).value)}
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.telefono ? 'border-red-500' : ''}"
            placeholder="+34 XXX XXX XXX (opcional)"
          />
          {#if fieldErrors.telefono}
            <p class="text-red-500 text-xs mt-1">{fieldErrors.telefono}</p>
          {/if}
          <p class="text-gray-500 text-xs mt-1">Campo opcional</p>
        </div>

        <!-- Birth Date -->
        <div>
          <label for="fechaNacimiento" class="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Nacimiento
          </label>
          <input
            id="fechaNacimiento"
            type="date"
            bind:value={formData.fechaNacimiento}
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="YYYY-MM-DD (opcional)"
          />
          <p class="text-gray-500 text-xs mt-1">Campo opcional</p>
        </div>
      </div>

      <!-- Address -->
      <div class="mt-4">
        <label for="direccion" class="block text-sm font-medium text-gray-700 mb-1">
          Dirección
        </label>
        <textarea
          id="direccion"
          bind:value={formData.direccion}
          oninput={(e) => validateField('direccion', (e.target as HTMLTextAreaElement).value)}
          rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 {fieldErrors.direccion ? 'border-red-500' : ''}"
          placeholder="Dirección completa del alumno (opcional)"
        ></textarea>
        {#if fieldErrors.direccion}
          <p class="text-red-500 text-xs mt-1">{fieldErrors.direccion}</p>
        {/if}
        <p class="text-gray-500 text-xs mt-1">Campo opcional</p>
      </div>
    </div>

    <!-- Form Actions -->
    <div class="flex justify-between space-x-4 pt-6 border-t border-gray-200">
      <div class="flex space-x-2">
        <button
          type="button"
          onclick={resetForm}
          class="bg-gray-500 text-white px-4 py-2 rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500"
        >
          Limpiar Formulario
        </button>
        <button
          type="button"
          onclick={() => goto('/alumnos')}
          class="bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
        >
          Cancelar
        </button>
      </div>
      
      <button
        type="submit"
        disabled={loading || Object.keys(fieldErrors).length > 0}
        class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        {#if loading}
          <span class="flex items-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
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
  <div class="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <h3 class="text-sm font-medium text-blue-900 mb-2">Información sobre el formulario:</h3>
    <ul class="text-sm text-blue-700 space-y-1">
      <li>• Los campos marcados con (*) son obligatorios</li>
      <li>• El usuario debe ser único en el sistema</li>
      <li>• El email y DNI también deben ser únicos</li>
      <li>• La contraseña debe tener al menos 6 caracteres</li>
      <li>• El alumno se creará con estado habilitado y no matriculado por defecto</li>
    </ul>
  </div>
</div>
