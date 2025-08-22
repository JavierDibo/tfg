<script lang="ts">
	import { goto } from '$app/navigation';
	import type { DTOClaseConDetalles } from '$lib/generated/api/models/DTOClaseConDetalles';
	import type { DTOProfesor } from '$lib/generated/api/models/DTOProfesor';
	import { ProfesorService } from '$lib/services/profesorService';

	let { clase } = $props<{
		clase: DTOClaseConDetalles;
	}>();

	let teachers = $state<DTOProfesor[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);

	// Load teacher information when component mounts
	$effect(() => {
		if (clase?.profesoresId && clase.profesoresId.length > 0) {
			loadTeachers();
		}
	});

	async function loadTeachers() {
		if (!clase?.profesoresId) return;

		try {
			loading = true;
			error = null;

			const teacherPromises = clase.profesoresId.map(async (teacherId: string) => {
				try {
					return await ProfesorService.getProfesorById(parseInt(teacherId));
				} catch (err) {
					console.error(`Error loading teacher ${teacherId}:`, err);
					return null;
				}
			});

			const results = await Promise.all(teacherPromises);
			teachers = results.filter((teacher): teacher is DTOProfesor => teacher !== null);
		} catch (err) {
			console.error('Error loading teachers:', err);
			error = 'Error al cargar información de profesores';
		} finally {
			loading = false;
		}
	}

	function goToTeacherProfile(teacherId: number) {
		goto(`/profesores/${teacherId}`);
	}
</script>

<div class="rounded-lg bg-white p-6 shadow-md">
	<h3 class="mb-4 text-lg font-medium text-gray-900">Profesores</h3>

	{#if loading}
		<div class="py-4 text-center">
			<div class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-500"></div>
			<p class="mt-2 text-sm text-gray-600">Cargando profesores...</p>
		</div>
	{:else if error}
		<div class="rounded-md border border-red-200 bg-red-50 p-3">
			<p class="text-sm text-red-700">{error}</p>
		</div>
	{:else if teachers.length === 0}
		<div class="py-4 text-center">
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
					d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
				/>
			</svg>
			<p class="mt-2 text-sm text-gray-500">No hay profesores asignados</p>
		</div>
	{:else}
		<div class="space-y-3">
			{#each teachers as teacher (teacher.id)}
				<div class="flex items-center justify-between rounded-lg border p-3 hover:bg-gray-50">
					<div class="flex items-center space-x-3">
						<div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
							<span class="text-sm font-medium text-blue-600">
								{teacher.nombre?.charAt(0)}{teacher.apellidos?.charAt(0)}
							</span>
						</div>
						<div>
							<h4 class="font-medium text-gray-900">
								{teacher.nombre}
								{teacher.apellidos}
							</h4>
							<p class="text-sm text-gray-600">{teacher.email}</p>
						</div>
					</div>
					<button
						onclick={() => goToTeacherProfile(teacher.id!)}
						class="rounded-md bg-blue-50 px-3 py-1 text-sm text-blue-600 hover:bg-blue-100 hover:text-blue-900"
					>
						Ver Perfil
					</button>
				</div>
			{/each}
		</div>
	{/if}
</div>
