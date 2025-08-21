<script lang="ts">
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';

	let { clase } = $props<{
		clase: DTOClase;
	}>();

	function getPresencialidadLabel(presencialidad?: string): string {
		switch (presencialidad) {
			case 'ONLINE':
				return 'Online';
			case 'PRESENCIAL':
				return 'Presencial';
			default:
				return 'No especificado';
		}
	}

	function getNivelLabel(nivel?: string): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'Principiante';
			case 'INTERMEDIO':
				return 'Intermedio';
			case 'AVANZADO':
				return 'Avanzado';
			default:
				return 'No especificado';
		}
	}

	function getNivelColor(nivel?: string): string {
		switch (nivel) {
			case 'PRINCIPIANTE':
				return 'bg-green-100 text-green-800';
			case 'INTERMEDIO':
				return 'bg-yellow-100 text-yellow-800';
			case 'AVANZADO':
				return 'bg-red-100 text-red-800';
			default:
				return 'bg-gray-100 text-gray-800';
		}
	}
</script>

<div class="rounded-lg bg-white p-6 shadow-md">
	<div class="flex flex-col gap-6 lg:flex-row">
		<!-- Class Image -->
		<div class="flex-shrink-0">
			{#if clase.imagenPortada}
				<img
					src={clase.imagenPortada}
					alt={clase.titulo}
					class="h-48 w-64 rounded-lg object-cover shadow-md"
				/>
			{:else}
				<div class="flex h-48 w-64 items-center justify-center rounded-lg bg-gray-200 shadow-md">
					<svg
						class="h-16 w-16 text-gray-400"
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
						/>
					</svg>
				</div>
			{/if}
		</div>

		<!-- Class Information -->
		<div class="flex-1">
			<div class="mb-4">
				<h1 class="mb-2 text-3xl font-bold text-gray-900">{clase.titulo}</h1>
				<div class="mb-3 flex flex-wrap gap-2">
					<span
						class="inline-flex rounded-full px-3 py-1 text-sm font-semibold {getNivelColor(
							clase.nivel
						)}"
					>
						{getNivelLabel(clase.nivel)}
					</span>
					<span
						class="inline-flex rounded-full bg-blue-100 px-3 py-1 text-sm font-semibold text-blue-800"
					>
						{getPresencialidadLabel(clase.presencialidad)}
					</span>
					<span
						class="inline-flex rounded-full bg-purple-100 px-3 py-1 text-sm font-semibold text-purple-800"
					>
						{clase.tipoClase || 'Clase'}
					</span>
				</div>
			</div>

			{#if clase.descripcion}
				<div class="mb-6">
					<h3 class="mb-2 text-lg font-medium text-gray-900">Descripción</h3>
					<p class="leading-relaxed text-gray-700">{clase.descripcion}</p>
				</div>
			{/if}

			<!-- Class Stats -->
			<div class="grid grid-cols-2 gap-4 md:grid-cols-4">
				<div class="text-center">
					<div class="text-2xl font-bold text-blue-600">{clase.numeroAlumnos || 0}</div>
					<div class="text-sm text-gray-500">Alumnos</div>
				</div>
				<div class="text-center">
					<div class="text-2xl font-bold text-green-600">{clase.numeroProfesores || 0}</div>
					<div class="text-sm text-gray-500">Profesores</div>
				</div>
				<div class="text-center">
					<div class="text-2xl font-bold text-purple-600">{clase.numeroMateriales || 0}</div>
					<div class="text-sm text-gray-500">Materiales</div>
				</div>
				<div class="text-center">
					<div class="text-2xl font-bold text-orange-600">{clase.numeroEjercicios || 0}</div>
					<div class="text-sm text-gray-500">Ejercicios</div>
				</div>
			</div>

			<!-- Price -->
			{#if clase.precio !== undefined && clase.precio !== null}
				<div class="mt-6">
					<div class="text-3xl font-bold text-gray-900">
						{clase.precio === 0 ? 'Gratis' : `€${clase.precio}`}
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
