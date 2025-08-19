<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import type { DTOAlumno } from '$lib/generated/api';
  import { AlumnoService, type AlumnoSearchParams } from '$lib/services/alumnoService';
  import { authStore } from '$lib/stores/authStore.svelte';

  // State management
  let alumnos: DTOAlumno[] = $state([]);
  let loading = $state(false);
  let error = $state<string | null>(null);
  let successMessage = $state<string | null>(null);

  // Search and filter state
  let searchParams: AlumnoSearchParams = $state({
    // Campos de texto
    nombre: '',
    apellidos: '',
    usuario: '',
    dni: '',
    email: '',
    numeroTelefono: '',
    
    // Estados booleanos
    matriculado: undefined,
    enabled: undefined,
    
    // Filtros de fecha
    fechaInscripcionDesde: undefined,
    fechaInscripcionHasta: undefined,
    
    // Búsqueda general
    busquedaGeneral: ''
  });

  // UI state for search
  let showAdvancedSearch = $state(false);
  let searchMode = $state<'simple' | 'advanced'>('simple');

  // Pagination state
  let currentPage = $state(1);
  let itemsPerPage = $state(10);
  
  // Modal state
  let showDeleteModal = $state(false);
  let alumnoToDelete: DTOAlumno | null = $state(null);

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
    loadAlumnos();
  });

  // ==================== SEARCH FUNCTIONS ====================
  
  /**
   * Búsqueda general en todos los campos de texto
   */
  function matchesGeneralSearch(alumno: DTOAlumno, searchTerm: string): boolean {
    if (!searchTerm) return true;
    
    const term = searchTerm.toLowerCase();
    const searchableFields = [
      alumno.nombre,
      alumno.apellidos,
      alumno.usuario,
      alumno.dni,
      alumno.email,
      alumno.numeroTelefono || ''
    ];
    
    return searchableFields.some(field => 
      field.toLowerCase().includes(term)
    );
  }

  /**
   * Verificar si una fecha está en el rango especificado
   */
  function isDateInRange(date: Date | undefined, desde: Date | undefined, hasta: Date | undefined): boolean {
    if (!date) return false;
    if (!desde && !hasta) return true;
    
    const targetDate = new Date(date);
    
    if (desde && targetDate < desde) return false;
    if (hasta && targetDate > hasta) return false;
    
    return true;
  }

  // Computed properties
  const filteredAlumnos = $derived(() => {
    const filtered = alumnos.filter(alumno => {
      // Determinar si usa búsqueda general o filtros específicos
      let matchesTextFields = false;

      if (searchParams.busquedaGeneral && searchParams.busquedaGeneral.trim()) {
        // Modo búsqueda general
        matchesTextFields = matchesGeneralSearch(alumno, searchParams.busquedaGeneral);
      } else {
        // Modo filtros específicos de campos de texto
        const matchesNombre = !searchParams.nombre || 
          alumno.nombre.toLowerCase().includes(searchParams.nombre.toLowerCase());
        
        const matchesApellidos = !searchParams.apellidos || 
          alumno.apellidos.toLowerCase().includes(searchParams.apellidos.toLowerCase());
        
        const matchesUsuario = !searchParams.usuario || 
          alumno.usuario.toLowerCase().includes(searchParams.usuario.toLowerCase());
        
        const matchesDni = !searchParams.dni || 
          alumno.dni.toLowerCase().includes(searchParams.dni.toLowerCase());
        
        const matchesEmail = !searchParams.email || 
          alumno.email.toLowerCase().includes(searchParams.email.toLowerCase());
        
        const matchesTelefono = !searchParams.numeroTelefono || 
          (alumno.numeroTelefono && alumno.numeroTelefono.includes(searchParams.numeroTelefono));

        matchesTextFields = Boolean(matchesNombre && matchesApellidos && matchesUsuario && 
                                   matchesDni && matchesEmail && matchesTelefono);
      }

      // Filtros de estado (SIEMPRE se aplican, independientemente del modo de búsqueda)
      const matchesMatriculado = searchParams.matriculado === undefined || 
        alumno.matriculado === searchParams.matriculado;
      
      const matchesEnabled = searchParams.enabled === undefined || 
        alumno.enabled === searchParams.enabled;

      // Filtro de fecha de inscripción (SIEMPRE se aplica)
      const matchesFechaInscripcion = isDateInRange(
        alumno.fechaInscripcion,
        searchParams.fechaInscripcionDesde,
        searchParams.fechaInscripcionHasta
      );

      // Combinar TODOS los filtros
      return matchesTextFields && matchesMatriculado && matchesEnabled && matchesFechaInscripcion;
    });
    
    return filtered;
  });

  const paginatedAlumnos = $derived(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginated = filteredAlumnos().slice(startIndex, endIndex);
    console.log('Pagination - currentPage:', currentPage, 'itemsPerPage:', itemsPerPage);
    console.log('Pagination - startIndex:', startIndex, 'endIndex:', endIndex);
    console.log('Paginated alumnos:', paginated);
    console.log('Paginated count:', paginated.length);
    return paginated;
  });

  const totalPages = $derived(() => {
    return Math.ceil(filteredAlumnos().length / itemsPerPage);
  });

  // Functions
  async function loadAlumnos() {
    loading = true;
    error = null;
    try {
      alumnos = await AlumnoService.getAlumnos();
      console.log('Loaded alumnos:', alumnos);
      console.log('Alumnos count:', alumnos.length);
    } catch (err) {
      error = `Error al cargar alumnos: ${err}`;
      console.error('Error loading alumnos:', err);
    } finally {
      loading = false;
    }
  }

  async function toggleEnrollmentStatus(alumno: DTOAlumno) {
    if (!authStore.isAdmin) {
      error = 'No tienes permisos para cambiar el estado de matrícula';
      return;
    }

    try {
      const updatedAlumno = await AlumnoService.changeEnrollmentStatus(
        alumno.id!, 
        !alumno.matriculado
      );
      
      // Update the local state
      const index = alumnos.findIndex(a => a.id === alumno.id);
      if (index !== -1) {
        alumnos[index] = updatedAlumno;
      }
      
      successMessage = `Estado de matrícula ${updatedAlumno.matriculado ? 'activado' : 'desactivado'} correctamente`;
      setTimeout(() => successMessage = null, 3000);
    } catch (err) {
      error = `Error al cambiar estado de matrícula: ${err}`;
    }
  }

  async function toggleAccountStatus(alumno: DTOAlumno) {
    if (!authStore.isAdmin) {
      error = 'No tienes permisos para habilitar/deshabilitar cuentas';
      return;
    }

    try {
      const updatedAlumno = await AlumnoService.toggleAccountStatus(
        alumno.id!, 
        !alumno.enabled
      );
      
      // Update the local state
      const index = alumnos.findIndex(a => a.id === alumno.id);
      if (index !== -1) {
        alumnos[index] = updatedAlumno;
      }
      
      successMessage = `Cuenta ${updatedAlumno.enabled ? 'habilitada' : 'deshabilitada'} correctamente`;
      setTimeout(() => successMessage = null, 3000);
    } catch (err) {
      error = `Error al cambiar estado de cuenta: ${err}`;
    }
  }

  function confirmDelete(alumno: DTOAlumno) {
    alumnoToDelete = alumno;
    showDeleteModal = true;
  }

  async function deleteAlumno() {
    if (!alumnoToDelete || !authStore.isAdmin) return;

    try {
      await AlumnoService.deleteAlumno(alumnoToDelete.id!);
      
      // Remove from local state
      alumnos = alumnos.filter(a => a.id !== alumnoToDelete!.id);
      
      successMessage = 'Alumno eliminado correctamente';
      setTimeout(() => successMessage = null, 3000);
      
      showDeleteModal = false;
      alumnoToDelete = null;
    } catch (err) {
      error = `Error al eliminar alumno: ${err}`;
      showDeleteModal = false;
      alumnoToDelete = null;
    }
  }

  function clearSearch() {
    searchParams = {
      nombre: '',
      apellidos: '',
      usuario: '',
      dni: '',
      email: '',
      numeroTelefono: '',
      matriculado: undefined,
      enabled: undefined,
      fechaInscripcionDesde: undefined,
      fechaInscripcionHasta: undefined,
      busquedaGeneral: ''
    };
    currentPage = 1;
  }

  function switchSearchMode(mode: 'simple' | 'advanced') {
    searchMode = mode;
    if (mode === 'simple') {
      // Limpiar filtros avanzados pero conservar búsqueda general
      const generalSearch = searchParams.busquedaGeneral;
      clearSearch();
      searchParams.busquedaGeneral = generalSearch;
    } else {
      // Limpiar búsqueda general al cambiar a avanzada
      searchParams.busquedaGeneral = '';
    }
  }

  function exportResults() {
    const csvContent = generateCSV(filteredAlumnos());
    downloadCSV(csvContent, 'alumnos-filtered.csv');
  }

  function generateCSV(data: DTOAlumno[]): string {
    const headers = ['ID', 'Usuario', 'Nombre', 'Apellidos', 'DNI', 'Email', 'Teléfono', 'Fecha Inscripción', 'Matriculado', 'Habilitado'];
    const rows = data.map(alumno => [
      alumno.id || '',
      alumno.usuario,
      alumno.nombre,
      alumno.apellidos,
      alumno.dni,
      alumno.email,
      alumno.numeroTelefono || '',
      alumno.fechaInscripcion ? formatDate(alumno.fechaInscripcion) : '',
      alumno.matriculado ? 'Sí' : 'No',
      alumno.enabled ? 'Sí' : 'No'
    ]);
    
    return [headers, ...rows].map(row => 
      row.map(field => `"${field}"`).join(',')
    ).join('\n');
  }

  function downloadCSV(content: string, filename: string) {
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  function formatDateForInput(date: Date | undefined): string {
    if (!date) return '';
    return new Date(date).toISOString().split('T')[0];
  }

  function goToPage(page: number) {
    currentPage = page;
  }

  function formatDate(date: Date | undefined): string {
    if (!date) return '-';
    return new Date(date).toLocaleDateString('es-ES');
  }
</script>

<div class="container mx-auto px-4 py-8">
  <!-- Header -->
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-3xl font-bold text-gray-900">Gestión de Alumnos</h1>
    {#if authStore.isAdmin}
      <button
        onclick={() => goto('/alumnos/nuevo')}
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        Nuevo Alumno
      </button>
    {/if}
  </div>

  <!-- Success/Error Messages -->
  {#if successMessage}
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
      {successMessage}
    </div>
  {/if}

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

  <!-- Enhanced Search and Filter Section -->
  <div class="bg-white rounded-lg shadow-md p-6 mb-6">
    <!-- Search Mode Toggle -->
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-lg font-semibold">Búsqueda de Alumnos</h2>
      <div class="flex space-x-2">
        <button
          onclick={() => switchSearchMode('simple')}
          class="px-3 py-1 text-sm rounded-md transition-colors {searchMode === 'simple' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
        >
          🔍 Búsqueda Simple
        </button>
        <button
          onclick={() => switchSearchMode('advanced')}
          class="px-3 py-1 text-sm rounded-md transition-colors {searchMode === 'advanced' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
        >
          ⚙️ Búsqueda Avanzada
        </button>
      </div>
    </div>

    {#if searchMode === 'simple'}
      <!-- Simple Search Mode -->
      <div class="space-y-4">
        <div>
          <label for="busquedaGeneral" class="block text-sm font-medium text-gray-700 mb-2">
            Búsqueda General
          </label>
          <div class="relative">
            <input
              id="busquedaGeneral"
              type="text"
              bind:value={searchParams.busquedaGeneral}
              class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Buscar por nombre, apellidos, usuario, DNI, email o teléfono..."
            />
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>
          <p class="text-xs text-gray-500 mt-1">
            Busca en todos los campos de texto: nombre, apellidos, usuario, DNI, email y teléfono.
            <br><strong>Los filtros de estado y fecha se aplicarán adicionalmente.</strong>
          </p>
        </div>

        <!-- Quick State Filters for Simple Mode -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="matriculadoSimple" class="block text-sm font-medium text-gray-700 mb-1">Estado de Matrícula</label>
            <select
              id="matriculadoSimple"
              bind:value={searchParams.matriculado}
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value={undefined}>Todos</option>
              <option value={true}>✅ Matriculados</option>
              <option value={false}>❌ No Matriculados</option>
            </select>
          </div>
          <div>
            <label for="enabledSimple" class="block text-sm font-medium text-gray-700 mb-1">Estado de Cuenta</label>
            <select
              id="enabledSimple"
              bind:value={searchParams.enabled}
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value={undefined}>Todos</option>
              <option value={true}>🟢 Habilitadas</option>
              <option value={false}>🔴 Deshabilitadas</option>
            </select>
          </div>
        </div>
      </div>
    {:else}
      <!-- Advanced Search Mode -->
      <div class="space-y-6">
        <!-- Campos de Texto -->
        <div>
          <h3 class="text-md font-medium text-gray-800 mb-3">📝 Campos de Texto</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
              <input
                id="nombre"
                type="text"
                bind:value={searchParams.nombre}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: Juan"
              />
            </div>
            <div>
              <label for="apellidos" class="block text-sm font-medium text-gray-700 mb-1">Apellidos</label>
              <input
                id="apellidos"
                type="text"
                bind:value={searchParams.apellidos}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: García López"
              />
            </div>
            <div>
              <label for="usuario" class="block text-sm font-medium text-gray-700 mb-1">Usuario</label>
              <input
                id="usuario"
                type="text"
                bind:value={searchParams.usuario}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: juan123"
              />
            </div>
            <div>
              <label for="dniAdvanced" class="block text-sm font-medium text-gray-700 mb-1">DNI</label>
              <input
                id="dniAdvanced"
                type="text"
                bind:value={searchParams.dni}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: 12345678Z"
              />
            </div>
            <div>
              <label for="emailAdvanced" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                id="emailAdvanced"
                type="email"
                bind:value={searchParams.email}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: juan@universidad.es"
              />
            </div>
            <div>
              <label for="telefono" class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
              <input
                id="telefono"
                type="tel"
                bind:value={searchParams.numeroTelefono}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ej: 123456789"
              />
            </div>
          </div>
        </div>

        <!-- Estados -->
        <div>
          <h3 class="text-md font-medium text-gray-800 mb-3">⚙️ Estados</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label for="matriculadoAdvanced" class="block text-sm font-medium text-gray-700 mb-1">Estado de Matrícula</label>
              <select
                id="matriculadoAdvanced"
                bind:value={searchParams.matriculado}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={undefined}>Todos</option>
                <option value={true}>✅ Matriculados</option>
                <option value={false}>❌ No Matriculados</option>
              </select>
            </div>
            <div>
              <label for="enabledAdvanced" class="block text-sm font-medium text-gray-700 mb-1">Estado de Cuenta</label>
              <select
                id="enabledAdvanced"
                bind:value={searchParams.enabled}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={undefined}>Todos</option>
                <option value={true}>🟢 Habilitadas</option>
                <option value={false}>🔴 Deshabilitadas</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Filtros de Fecha -->
        <div>
          <h3 class="text-md font-medium text-gray-800 mb-3">📅 Rango de Fecha de Inscripción</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label for="fechaDesde" class="block text-sm font-medium text-gray-700 mb-1">Desde</label>
              <input
                id="fechaDesde"
                type="date"
                value={formatDateForInput(searchParams.fechaInscripcionDesde)}
                onchange={(e) => {
                  const target = e.target as HTMLInputElement;
                  searchParams.fechaInscripcionDesde = target.value ? new Date(target.value) : undefined;
                }}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label for="fechaHasta" class="block text-sm font-medium text-gray-700 mb-1">Hasta</label>
              <input
                id="fechaHasta"
                type="date"
                value={formatDateForInput(searchParams.fechaInscripcionHasta)}
                onchange={(e) => {
                  const target = e.target as HTMLInputElement;
                  searchParams.fechaInscripcionHasta = target.value ? new Date(target.value) : undefined;
                }}
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          <p class="text-xs text-gray-500 mt-1">
            Filtra por estudiantes inscritos en un rango de fechas específico
          </p>
        </div>
      </div>
    {/if}
    
    <!-- Action Buttons -->
    <div class="flex flex-wrap gap-3 mt-6 pt-4 border-t">
      <button
        onclick={clearSearch}
        class="bg-gray-500 text-white px-4 py-2 rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500 transition-colors"
      >
        🗑️ Limpiar Filtros
      </button>
      <button
        onclick={loadAlumnos}
        class="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
      >
        🔄 Actualizar
      </button>
      {#if filteredAlumnos().length > 0}
        <button
          onclick={exportResults}
          class="bg-green-500 text-white px-4 py-2 rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 transition-colors"
        >
          📥 Exportar CSV ({filteredAlumnos().length})
        </button>
      {/if}
    </div>

    <!-- Search Results Summary -->
    {#if searchParams.busquedaGeneral || searchParams.nombre || searchParams.apellidos || searchParams.usuario || searchParams.dni || searchParams.email || searchParams.numeroTelefono || searchParams.matriculado !== undefined || searchParams.enabled !== undefined || searchParams.fechaInscripcionDesde || searchParams.fechaInscripcionHasta}
      <div class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
        <h4 class="text-sm font-medium text-blue-800 mb-2">🔍 Filtros Activos:</h4>
        <div class="flex flex-wrap gap-2">
          {#if searchParams.busquedaGeneral}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              General: "{searchParams.busquedaGeneral}"
            </span>
          {/if}
          {#if searchParams.nombre}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              Nombre: "{searchParams.nombre}"
            </span>
          {/if}
          {#if searchParams.apellidos}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              Apellidos: "{searchParams.apellidos}"
            </span>
          {/if}
          {#if searchParams.usuario}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              Usuario: "{searchParams.usuario}"
            </span>
          {/if}
          {#if searchParams.dni}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              DNI: "{searchParams.dni}"
            </span>
          {/if}
          {#if searchParams.email}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              Email: "{searchParams.email}"
            </span>
          {/if}
          {#if searchParams.numeroTelefono}
            <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
              Teléfono: "{searchParams.numeroTelefono}"
            </span>
          {/if}
          {#if searchParams.matriculado !== undefined}
            <span class="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full">
              {searchParams.matriculado ? '✅ Matriculados' : '❌ No Matriculados'}
            </span>
          {/if}
          {#if searchParams.enabled !== undefined}
            <span class="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs rounded-full">
              {searchParams.enabled ? '🟢 Habilitados' : '🔴 Deshabilitados'}
            </span>
          {/if}
          {#if searchParams.fechaInscripcionDesde}
            <span class="px-2 py-1 bg-purple-100 text-purple-800 text-xs rounded-full">
              Desde: {formatDate(searchParams.fechaInscripcionDesde)}
            </span>
          {/if}
          {#if searchParams.fechaInscripcionHasta}
            <span class="px-2 py-1 bg-purple-100 text-purple-800 text-xs rounded-full">
              Hasta: {formatDate(searchParams.fechaInscripcionHasta)}
            </span>
          {/if}
        </div>
      </div>
    {/if}
  </div>

  <!-- Students Table -->
  <div class="bg-white rounded-lg shadow-md overflow-hidden">
    {#if loading}
      <div class="text-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
        <p class="mt-2 text-gray-600">Cargando alumnos...</p>
      </div>
    {:else if filteredAlumnos().length === 0}
      <div class="text-center py-8">
        <p class="text-gray-500">No se encontraron alumnos que coincidan con los filtros.</p>
      </div>
    {:else}
      <!-- Desktop Table -->
      <div class="hidden lg:block overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Alumno</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">DNI</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Inscripción</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each paginatedAlumnos() as alumno (alumno.id)}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div class="text-sm font-medium text-gray-900">
                      {alumno.nombre} {alumno.apellidos}
                    </div>
                    <div class="text-sm text-gray-500">@{alumno.usuario}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {alumno.dni}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {alumno.email}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {alumno.numeroTelefono || '-'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {formatDate(alumno.fechaInscripcion)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="space-y-1">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {alumno.matriculado ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}">
                      {alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
                    </span>
                    <br>
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {alumno.enabled ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'}">
                      {alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div class="flex space-x-2">
                    <button
                      onclick={() => goto(`/alumnos/${alumno.id}`)}
                      class="text-blue-600 hover:text-blue-900"
                      title="Ver/Editar"
                    >
                      Ver
                    </button>
                    {#if authStore.isAdmin}
                      <button
                        onclick={() => toggleEnrollmentStatus(alumno)}
                        class="text-green-600 hover:text-green-900"
                        title={alumno.matriculado ? 'Desmatricular' : 'Matricular'}
                      >
                        {alumno.matriculado ? 'Desmatricular' : 'Matricular'}
                      </button>
                      <button
                        onclick={() => toggleAccountStatus(alumno)}
                        class="text-yellow-600 hover:text-yellow-900"
                        title={alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
                      >
                        {alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
                      </button>
                      <button
                        onclick={() => confirmDelete(alumno)}
                        class="text-red-600 hover:text-red-900"
                        title="Eliminar"
                      >
                        Eliminar
                      </button>
                    {/if}
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>

      <!-- Mobile Cards -->
      <div class="lg:hidden">
        {#each paginatedAlumnos() as alumno (alumno.id)}
          <div class="border-b border-gray-200 p-4">
            <div class="flex justify-between items-start mb-2">
              <div>
                <h3 class="font-medium text-gray-900">{alumno.nombre} {alumno.apellidos}</h3>
                <p class="text-sm text-gray-500">@{alumno.usuario}</p>
              </div>
              <div class="text-right">
                <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {alumno.matriculado ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}">
                  {alumno.matriculado ? 'Matriculado' : 'No Matriculado'}
                </span>
              </div>
            </div>
            <div class="space-y-1 text-sm text-gray-600">
              <p><strong>DNI:</strong> {alumno.dni}</p>
              <p><strong>Email:</strong> {alumno.email}</p>
              {#if alumno.numeroTelefono}
                <p><strong>Teléfono:</strong> {alumno.numeroTelefono}</p>
              {/if}
              <p><strong>Inscripción:</strong> {formatDate(alumno.fechaInscripcion)}</p>
              <p><strong>Estado:</strong> 
                <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {alumno.enabled ? 'bg-blue-100 text-blue-800' : 'bg-red-100 text-red-800'}">
                  {alumno.enabled ? 'Habilitado' : 'Deshabilitado'}
                </span>
              </p>
            </div>
            <div class="flex space-x-2 mt-3">
              <button
                onclick={() => goto(`/alumnos/${alumno.id}`)}
                class="text-blue-600 hover:text-blue-900 text-sm"
              >
                Ver/Editar
              </button>
              {#if authStore.isAdmin}
                <button
                  onclick={() => toggleEnrollmentStatus(alumno)}
                  class="text-green-600 hover:text-green-900 text-sm"
                >
                  {alumno.matriculado ? 'Desmatricular' : 'Matricular'}
                </button>
                <button
                  onclick={() => toggleAccountStatus(alumno)}
                  class="text-yellow-600 hover:text-yellow-900 text-sm"
                >
                  {alumno.enabled ? 'Deshabilitar' : 'Habilitar'}
                </button>
                <button
                  onclick={() => confirmDelete(alumno)}
                  class="text-red-600 hover:text-red-900 text-sm"
                >
                  Eliminar
                </button>
              {/if}
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>

  <!-- Pagination -->
  {#if totalPages() > 1}
    <div class="flex justify-center items-center space-x-2 mt-6">
      <button
        onclick={() => goToPage(currentPage - 1)}
        disabled={currentPage === 1}
        class="px-3 py-2 text-sm border rounded-md {currentPage === 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'}"
      >
        Anterior
      </button>
      
      {#each Array.from({length: totalPages()}, (_, i) => i + 1) as page}
        <button
          onclick={() => goToPage(page)}
          class="px-3 py-2 text-sm border rounded-md {page === currentPage ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'}"
        >
          {page}
        </button>
      {/each}
      
      <button
        onclick={() => goToPage(currentPage + 1)}
        disabled={currentPage === totalPages()}
        class="px-3 py-2 text-sm border rounded-md {currentPage === totalPages() ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'}"
      >
        Siguiente
      </button>
    </div>
  {/if}

  <!-- Statistics Summary -->
  <div class="mt-6 text-center text-sm text-gray-600">
    Mostrando {paginatedAlumnos().length} de {filteredAlumnos().length} alumnos
    {#if filteredAlumnos().length !== alumnos.length}
      (filtrados de {alumnos.length} total)
    {/if}
  </div>
</div>

<!-- Delete Confirmation Modal -->
{#if showDeleteModal && alumnoToDelete}
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
      <div class="mt-3 text-center">
        <h3 class="text-lg font-medium text-gray-900">Confirmar Eliminación</h3>
        <div class="mt-2 px-7 py-3">
          <p class="text-sm text-gray-500">
            ¿Estás seguro de que quieres eliminar al alumno 
            <strong>{alumnoToDelete.nombre} {alumnoToDelete.apellidos}</strong>?
            Esta acción no se puede deshacer.
          </p>
        </div>
        <div class="flex justify-center space-x-4 px-4 py-3">
          <button
            onclick={() => { showDeleteModal = false; alumnoToDelete = null; }}
            class="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md shadow-sm hover:bg-gray-600"
          >
            Cancelar
          </button>
          <button
            onclick={deleteAlumno}
            class="px-4 py-2 bg-red-600 text-white text-base font-medium rounded-md shadow-sm hover:bg-red-700"
          >
            Eliminar
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}
