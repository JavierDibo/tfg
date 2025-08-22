<script lang="ts">
	import type { DTOAlumno } from '$lib/generated/api/models/DTOAlumno';
	import type { DTORespuestaPaginadaDTOAlumno } from '$lib/generated/api/models/DTORespuestaPaginadaDTOAlumno';
	import { AlumnoService } from '$lib/services/alumnoService';

	let {
		claseId,
		enrolledStudentIds = [],
		onEnrollStudent,
		onClose
	} = $props<{
		claseId: number;
		enrolledStudentIds: number[];
		onEnrollStudent: (student: DTOAlumno) => void;
		onClose: () => void;
	}>();

	// State
	let availableStudents = $state<DTOAlumno[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);
	let currentPage = $state(1);
	let totalPages = $state(1);
	let totalElements = $state(0);
	let pageSize = $state(20);
	let searchTerm = $state('');

	// Load available students
	async function loadAvailableStudents() {
		if (!claseId) return;

		try {
			loading = true;
			error = null;

			const response: DTORespuestaPaginadaDTOAlumno = await AlumnoService.getAvailableStudents({
				page: currentPage - 1, // API uses 0-based pagination
				size: pageSize,
				sortBy: 'nombre',
				sortDirection: 'ASC'
			});

			// Filter out students already enrolled in this class
			availableStudents = (response.content || []).filter(
				student => !enrolledStudentIds.includes(student.id)
			);
			totalElements = response.page?.totalElements || 0;
			totalPages = response.page?.totalPages || 1;
		} catch (err) {
			console.error('Error loading available students:', err);
			error = 'Error al cargar los alumnos disponibles';
		} finally {
			loading = false;
		}
	}

	// Handle page change
	function handlePageChange(page: number) {
		currentPage = page;
		loadAvailableStudents();
	}

	// Handle student enrollment
	function handleEnrollStudent(student: DTOAlumno) {
		onEnrollStudent(student);
	}

	// Close modal
	function handleClose() {
		onClose();
	}

	// Load students when component mounts or when props change
	$effect(() => {
		if (claseId) {
			loadAvailableStudents();
		}
	});
</script>

<div class="fixed inset-0 z-50 overflow-y-auto">
	<div class="flex min-h-screen items-end justify-center px-4 pt-4 pb-20 text-center sm:block sm:p-0">
		<div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onclick={handleClose}></div>

		<div class="inline-block transform overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-4xl sm:align-middle">
			<div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
				<div class="sm:flex sm:items-start">
					<div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
						<svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
							/>
						</svg>
					</div>
					<div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left w-full">
						<h3 class="text-lg font-medium leading-6 text-gray-900">Alumnos Disponibles para Inscripción</h3>
						<div class="mt-2">
							<p class="text-sm text-gray-500">
								Selecciona los alumnos que deseas inscribir en esta clase.
							</p>
						</div>

						<!-- Error Message -->
						{#if error}
							<div class="mt-4 rounded-md border border-red-200 bg-red-50 p-4">
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
										<p class="text-sm font-medium text-red-800">{error}</p>
									</div>
								</div>
							</div>
						{/if}

						<!-- Students List -->
						<div class="mt-4 max-h-96 overflow-y-auto">
							{#if loading}
								<div class="flex h-32 items-center justify-center">
									<div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
								</div>
							{:else if availableStudents.length > 0}
								<div class="space-y-3">
									{#each availableStudents as student}
										<div class="flex items-center justify-between rounded-lg bg-gray-50 p-3">
											<div class="flex items-center">
												<div class="flex-shrink-0">
													<div class="flex h-8 w-8 items-center justify-center rounded-full bg-green-100">
														<svg
															class="h-4 w-4 text-green-600"
															fill="none"
															stroke="currentColor"
															viewBox="0 0 24 24"
														>
															<path
																stroke-linecap="round"
																stroke-linejoin="round"
																stroke-width="2"
																d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
															/>
														</svg>
													</div>
												</div>
												<div class="ml-3">
													<p class="text-sm font-medium text-gray-900">
														{student.nombre} {student.apellidos}
													</p>
													<p class="text-xs text-gray-500">{student.email}</p>
													<p class="text-xs text-gray-500">DNI: {student.dni}</p>
												</div>
											</div>
											<div class="flex items-center space-x-2">
												<span
													class="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800"
												>
													Disponible
												</span>
												<button
													onclick={() => handleEnrollStudent(student)}
													class="rounded bg-blue-50 p-1 text-blue-600 hover:bg-blue-100"
													title="Inscribir alumno"
												>
													<svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
														<path
															stroke-linecap="round"
															stroke-linejoin="round"
															stroke-width="2"
															d="M12 6v6m0 0v6m0-6h6m-6 0H6"
														/>
													</svg>
												</button>
											</div>
										</div>
									{/each}
								</div>

								<!-- Pagination -->
								{#if totalPages > 1}
									<div class="mt-4 border-t border-gray-200 pt-4">
										<div class="flex items-center justify-between">
											<div class="text-sm text-gray-700">
												Mostrando {((currentPage - 1) * pageSize) + 1} a {Math.min(currentPage * pageSize, totalElements)} de {totalElements} alumnos
											</div>
											<div class="flex space-x-2">
												{#if currentPage > 1}
													<button
														onclick={() => handlePageChange(currentPage - 1)}
														class="rounded bg-gray-50 px-3 py-1 text-sm text-gray-600 hover:bg-gray-100"
													>
														Anterior
													</button>
												{/if}
												{#if currentPage < totalPages}
													<button
														onclick={() => handlePageChange(currentPage + 1)}
														class="rounded bg-gray-50 px-3 py-1 text-sm text-gray-600 hover:bg-gray-100"
													>
														Siguiente
													</button>
												{/if}
											</div>
										</div>
									</div>
								{:else}
									<div class="mt-4 border-t border-gray-200 pt-4">
										<p class="text-sm text-gray-600">
											Total de alumnos disponibles: <span class="font-medium">{totalElements}</span>
										</p>
									</div>
								{/if}
							{:else}
								<div class="py-8 text-center">
									<svg
										class="mx-auto h-12 w-12 text-gray-400"
										fill="none"
										stroke="currentColor"
										viewBox="0 0 24 24"
									>
										<path
											stroke-linecap="round"
											stroke-linejoin="round"
											stroke-width="2"
											d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
										/>
									</svg>
									<h3 class="mt-2 text-sm font-medium text-gray-900">No hay alumnos disponibles</h3>
									<p class="mt-1 text-sm text-gray-500">
										Todos los alumnos disponibles ya están inscritos en esta clase.
									</p>
								</div>
							{/if}
						</div>
					</div>
				</div>
			</div>
			<div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
				<button
					type="button"
					onclick={handleClose}
					class="inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
				>
					Cerrar
				</button>
			</div>
		</div>
	</div>
</div>
