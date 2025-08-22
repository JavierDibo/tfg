<script lang="ts">
	import '../app.css';
	import { authStore } from '$lib/stores/authStore.svelte';

	let { children } = $props();
</script>

<nav class="bg-gray-800">
	<div class="mx-auto max-w-7xl px-2 sm:px-6 lg:px-8">
		<div class="relative flex h-16 items-center justify-between">
			<div class="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
				<div class="flex flex-shrink-0 items-center">
					<span class="font-bold text-white">Academia</span>
				</div>
				<div class="hidden sm:ml-6 sm:block">
					<div class="flex space-x-4">
						{#if authStore.isAuthenticated}
							<!-- Admin Navigation -->
							{#if authStore.isAdmin}
								<a
									href="/clases"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Clases
								</a>
								<a
									href="/alumnos"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Gestión de Alumnos
								</a>
								<a
									href="/alumnos/estadisticas"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Estadísticas Alumnos
								</a>
								<a
									href="/profesores"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Profesores
								</a>
								<a
									href="/profesores/estadisticas"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Estadísticas Profesores
								</a>
								<a
									href="/entidades"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Entidades
								</a>
								<!-- Professor Navigation -->
							{:else if authStore.isProfesor}
								<a
									href="/clases"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Mis Clases
								</a>
								<a
									href="/alumnos"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Mis Alumnos
								</a>
								<a
									href="/profesores/estadisticas"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Estadísticas
								</a>
								<a
									href="/profesores/perfil"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Mi Perfil
								</a>
								<!-- Student Navigation -->
							{:else if authStore.isAlumno}
								<a
									href="/clases"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Clases
								</a>
								<a
									href="/alumnos/perfil"
									class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
								>
									Mi Perfil
								</a>
							{/if}
						{:else}
							<!-- Public Navigation -->
							<a
								href="/"
								class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
							>
								Inicio
							</a>
							<a
								href="/clases"
								class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
							>
								Clases
							</a>
						{/if}
					</div>
				</div>
			</div>
			<div
				class="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0"
			>
				{#if authStore.isAuthenticated}
					<span class="mr-4 text-gray-400">Bienvenido/a, {authStore.user?.sub}</span>
					<button
						onclick={authStore.logout}
						class="rounded-full bg-gray-800 p-1 text-gray-400 hover:text-white focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800 focus:outline-none"
					>
						Cerrar sesión
					</button>
				{:else}
					<a
						href="/auth"
						class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
						>Iniciar sesión</a
					>
				{/if}
			</div>
		</div>
	</div>
</nav>

<main>
	{@render children()}
</main>
