<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { AlumnoService, type AlumnoStatistics } from '$lib/services/alumnoService';
  import { authStore } from '$lib/stores/authStore.svelte';

  // State management
  let statistics: AlumnoStatistics | null = $state(null);
  let loading = $state(false);
  let error = $state<string | null>(null);

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

  onMount(() => {
    loadStatistics();
  });

  async function loadStatistics() {
    loading = true;
    error = null;
    
    try {
      statistics = await AlumnoService.getStatistics();
    } catch (err) {
      error = `Error al cargar estadísticas: ${err}`;
    } finally {
      loading = false;
    }
  }

  // Computed properties  
  const enrollmentPercentage = $derived(() => {
    if (!statistics || statistics.total === 0) return 0;
    return Math.round((statistics.matriculados / statistics.total) * 100);
  });

  const nonEnrollmentPercentage = $derived(() => {
    if (!statistics || statistics.total === 0) return 0;
    return Math.round((statistics.noMatriculados / statistics.total) * 100);
  });

  // Helper to display statistics values properly with Svelte 5
  const displayStats = $derived(() => statistics);
</script>

<div class="container mx-auto px-4 py-8">
  <!-- Header -->
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-3xl font-bold text-gray-900">Estadísticas de Alumnos</h1>
    <div class="space-x-2">
      <button
        onclick={loadStatistics}
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        Actualizar
      </button>
      <button
        onclick={() => goto('/alumnos')}
        class="text-gray-600 hover:text-gray-800"
      >
        ← Volver a Alumnos
      </button>
    </div>
  </div>

  <!-- Error Message -->
  {#if error}
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      {error}
      <button 
        onclick={() => error = null}
        class="float-right text-red-500 hover:text-red-700"
      >
        ×
      </button>
    </div>
  {/if}

  <!-- Loading State -->
  {#if loading}
    <div class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-gray-600">Cargando estadísticas...</p>
    </div>
  {:else if statistics}
    <!-- Statistics Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <!-- Total Students -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">Total de Alumnos</p>
            <p class="text-2xl font-bold text-gray-900">{displayStats()?.total || 0}</p>
          </div>
        </div>
      </div>

      <!-- Enrolled Students -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">Alumnos Matriculados</p>
            <p class="text-2xl font-bold text-gray-900">{displayStats()?.matriculados || 0}</p>
            <p class="text-sm text-green-600">{enrollmentPercentage()}% del total</p>
          </div>
        </div>
      </div>

      <!-- Non-enrolled Students -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">No Matriculados</p>
            <p class="text-2xl font-bold text-gray-900">{displayStats()?.noMatriculados || 0}</p>
            <p class="text-sm text-yellow-600">{nonEnrollmentPercentage()}% del total</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Visual Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <!-- Enrollment Status Chart -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Estado de Matrícula</h3>
        
        {#if displayStats()?.total && displayStats()!.total > 0}
          <!-- Simple Bar Chart -->
          <div class="space-y-4">
            <div>
              <div class="flex justify-between items-center mb-1">
                <span class="text-sm font-medium text-green-700">Matriculados</span>
                <span class="text-sm text-gray-500">{displayStats()?.matriculados || 0} ({enrollmentPercentage()}%)</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-3">
                <div 
                  class="bg-green-500 h-3 rounded-full transition-all duration-1000 ease-out"
                  style="width: {enrollmentPercentage()}%"
                ></div>
              </div>
            </div>
            
            <div>
              <div class="flex justify-between items-center mb-1">
                <span class="text-sm font-medium text-yellow-700">No Matriculados</span>
                <span class="text-sm text-gray-500">{displayStats()?.noMatriculados || 0} ({nonEnrollmentPercentage()}%)</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-3">
                <div 
                  class="bg-yellow-500 h-3 rounded-full transition-all duration-1000 ease-out"
                  style="width: {nonEnrollmentPercentage()}%"
                ></div>
              </div>
            </div>
          </div>
        {:else}
          <p class="text-gray-500 text-center py-8">No hay datos para mostrar</p>
        {/if}
      </div>

      <!-- Summary Information -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Resumen Ejecutivo</h3>
        
        <div class="space-y-4">
          <div class="border-l-4 border-blue-500 pl-4">
            <h4 class="font-medium text-gray-900">Total de Estudiantes</h4>
            <p class="text-sm text-gray-600">
              Actualmente hay <strong>{displayStats()?.total || 0}</strong> estudiantes registrados en el sistema.
            </p>
          </div>
          
          <div class="border-l-4 border-green-500 pl-4">
            <h4 class="font-medium text-gray-900">Estudiantes Activos</h4>
            <p class="text-sm text-gray-600">
              <strong>{displayStats()?.matriculados || 0}</strong> estudiantes están matriculados y pueden acceder a los cursos.
            </p>
          </div>
          
          <div class="border-l-4 border-yellow-500 pl-4">
            <h4 class="font-medium text-gray-900">Estudiantes Pendientes</h4>
            <p class="text-sm text-gray-600">
              <strong>{displayStats()?.noMatriculados || 0}</strong> estudiantes están registrados pero aún no matriculados.
            </p>
          </div>
          
          {#if displayStats()?.noMatriculados && displayStats()!.noMatriculados > 0}
            <div class="bg-yellow-50 border border-yellow-200 rounded-md p-3">
              <p class="text-sm text-yellow-800">
                <strong>Acción recomendada:</strong> Considera revisar los estudiantes no matriculados para determinar si necesitan ser matriculados o dados de baja.
              </p>
            </div>
          {:else}
            <div class="bg-green-50 border border-green-200 rounded-md p-3">
              <p class="text-sm text-green-800">
                <strong>¡Excelente!</strong> Todos los estudiantes registrados están matriculados.
              </p>
            </div>
          {/if}
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-lg shadow-md p-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Acciones Rápidas</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <button
          onclick={() => goto('/alumnos')}
          class="flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div class="text-center">
            <svg class="w-8 h-8 text-blue-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p class="text-sm font-medium text-gray-900">Ver Todos</p>
            <p class="text-xs text-gray-500">Lista completa</p>
          </div>
        </button>

        {#if authStore.isAdmin}
          <button
            onclick={() => goto('/alumnos/nuevo')}
            class="flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="text-center">
              <svg class="w-8 h-8 text-green-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              <p class="text-sm font-medium text-gray-900">Nuevo Alumno</p>
              <p class="text-xs text-gray-500">Registrar estudiante</p>
            </div>
          </button>

          <button
            onclick={() => goto('/alumnos?matriculado=false')}
            class="flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="text-center">
              <svg class="w-8 h-8 text-yellow-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.732 15.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
              <p class="text-sm font-medium text-gray-900">No Matriculados</p>
              <p class="text-xs text-gray-500">{displayStats()?.noMatriculados || 0} estudiantes</p>
            </div>
          </button>
        {/if}

        <button
          onclick={() => goto('/alumnos?matriculado=true')}
          class="flex items-center justify-center p-4 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div class="text-center">
            <svg class="w-8 h-8 text-green-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-sm font-medium text-gray-900">Matriculados</p>
            <p class="text-xs text-gray-500">{displayStats()?.matriculados || 0} estudiantes</p>
          </div>
        </button>
      </div>
    </div>

    <!-- Last Updated -->
    <div class="text-center text-sm text-gray-500 mt-6">
      Estadísticas actualizadas: {new Date().toLocaleString('es-ES')}
    </div>
  {:else if !loading}
    <div class="text-center py-12">
      <p class="text-gray-500 text-lg">No se pudieron cargar las estadísticas</p>
      <button
        onclick={loadStatistics}
        class="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
      >
        Intentar de Nuevo
      </button>
    </div>
  {/if}
</div>
