<script lang="ts">
	import type { DTOClase } from '$lib/generated/api/models/DTOClase';

	let {
		clase,
		isEnrolled,
		onEnrollment,
		showEnrollment = false,
		loading = false
	} = $props<{
		clase: DTOClase;
		isEnrolled: boolean;
		onEnrollment: (enroll: boolean) => void;
		showEnrollment?: boolean;
		loading?: boolean;
	}>();
</script>

<div class="rounded-lg bg-white p-6 shadow">
	<h3 class="mb-4 text-lg font-medium text-gray-900">Inscripción</h3>

	<div class="space-y-4">
		<div class="flex items-center justify-between">
			<span class="text-sm text-gray-600">Estado de inscripción:</span>
			<span
				class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium {isEnrolled
					? 'bg-green-100 text-green-800'
					: 'bg-gray-100 text-gray-800'}"
			>
				{isEnrolled ? 'Inscrito' : 'No inscrito'}
			</span>
		</div>

		<div class="flex items-center justify-between">
			<span class="text-sm text-gray-600">Precio:</span>
			<span class="text-sm font-medium text-gray-900">
				{clase.precio === 0 || clase.precio === null ? 'Gratis' : `€${clase.precio}`}
			</span>
		</div>

		<div class="flex items-center justify-between">
			<span class="text-sm text-gray-600">Alumnos inscritos:</span>
			<span class="text-sm font-medium text-gray-900">{clase.numeroAlumnos || 0}</span>
		</div>

		{#if showEnrollment}
			<div class="border-t border-gray-200 pt-4">
				{#if loading}
					<button
						disabled
						class="inline-flex w-full cursor-not-allowed items-center justify-center rounded-md border border-transparent bg-gray-400 px-4 py-2 text-sm font-medium text-white"
					>
						<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
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
						Procesando...
					</button>
				{:else if isEnrolled}
					<button
						onclick={() => onEnrollment(false)}
						class="inline-flex w-full items-center justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:ring-offset-2 focus:outline-none"
					>
						<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
							/>
						</svg>
						Desinscribirse de la Clase
					</button>
				{:else}
					<button
						onclick={() => onEnrollment(true)}
						class="inline-flex w-full items-center justify-center rounded-md border border-transparent bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 focus:ring-2 focus:ring-green-500 focus:ring-offset-2 focus:outline-none"
					>
						<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 6v6m0 0v6m0-6h6m-6 0H6"
							/>
						</svg>
						Inscribirse en la Clase
					</button>
				{/if}
			</div>
		{/if}
	</div>
</div>
