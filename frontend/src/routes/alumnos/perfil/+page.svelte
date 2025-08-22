<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { AlumnoService } from '$lib/services/alumnoService';

	let loading = $state(true);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated || !authStore.isAlumno) {
			goto('/');
			return;
		}
	});

	// Monitor auth store changes
	$effect(() => {
		console.log('Auth store changed:', {
			isAuthenticated: authStore.isAuthenticated,
			isAlumno: authStore.isAlumno,
			user: authStore.user,
			userId: authStore.user?.id
		});
	});

	onMount(async () => {
		console.log('Component mounted');
		console.log('Initial auth state:', {
			isAuthenticated: authStore.isAuthenticated,
			isAlumno: authStore.isAlumno,
			user: authStore.user,
			userId: authStore.user?.id
		});

		if (!authStore.user?.usuario && !authStore.user?.sub) {
			console.log('No user found, redirecting to home');
			goto('/');
			return;
		}

		console.log('Auth store user:', authStore.user);
		console.log('User ID:', authStore.user?.id);

		try {
			// Use the user ID from the auth store
			if (!authStore.user?.id) {
				console.error('No user ID available');
				console.log('Available user data:', authStore.user);
				goto('/');
				return;
			}

			const alumno = await AlumnoService.getAlumnoById(authStore.user.id);

			// Redirect to the student's profile page
			goto(`/alumnos/${alumno.id}`);
		} catch (error) {
			console.error('Error finding student profile:', error);
			goto('/');
		}
	});
</script>

{#if loading}
	<div class="container mx-auto px-4 py-8 text-center">
		<div class="mx-auto h-12 w-12 animate-spin rounded-full border-b-2 border-blue-500"></div>
		<p class="mt-4 text-gray-600">Cargando tu perfil...</p>
	</div>
{/if}
