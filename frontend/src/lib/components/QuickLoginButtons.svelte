<script lang="ts">
	import { authStore } from '$lib/stores/authStore.svelte';
	import { goto } from '$app/navigation';
	import { autenticacionApi } from '$lib/api';

	let loginLoading = $state(false);
	let message = $state('');

	async function quickLogin(username: string, password: string, role: string, redirectTo?: string) {
		loginLoading = true;
		message = `Logging in as ${role}...`;
		
		try {
			// First logout current user
			authStore.logout();
			
			// Then login with new credentials
			const loginResponse = await autenticacionApi.login({
				dTOPeticionLogin: {
					username,
					password
				}
			});
			
			// Store the authentication data
			authStore.login(loginResponse.token);
			message = `✅ Successfully logged in as ${role}`;
			
			// Redirect if specified
			if (redirectTo) {
				setTimeout(() => {
					goto(redirectTo);
				}, 50);
			}
			
		} catch (error) {
			message = `❌ Login failed for ${role}: ${error}`;
			console.error('Login error:', error);
		} finally {
			loginLoading = false;
		}
	}

	async function loginAsAdmin() {
		await quickLogin('admin', 'admin', 'Admin', '/test');
	}

	async function loginAsStudent() {
		await quickLogin('estudiante', 'password', 'Student', '/test');
	}

	async function loginAsProfessor() {
		await quickLogin('profesor', 'password', 'Professor', '/test');
	}
</script>

<div class="p-4 rounded-lg bg-purple-50 border border-purple-200">
	<h3 class="text-lg font-semibold text-purple-900 mb-3">🚀 Quick Login (for Testing)</h3>
	
	<div class="flex flex-wrap gap-3 mb-3">
		<button
			onclick={loginAsAdmin}
			disabled={loginLoading}
			class="px-4 py-2 bg-red-600 text-white font-semibold rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍💼 Admin'}
		</button>

		<button
			onclick={loginAsStudent}
			disabled={loginLoading}
			class="px-4 py-2 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍🎓 Student'}
		</button>

		<button
			onclick={loginAsProfessor}
			disabled={loginLoading}
			class="px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
		>
			{loginLoading ? '🔄 Logging in...' : '👨‍🏫 Professor'}
		</button>
	</div>
	
	{#if message}
		<div class="text-sm text-purple-700 p-2 bg-white rounded border">
			{message}
		</div>
	{/if}
	
	<p class="text-xs text-purple-600 mt-2">
		Auto-redirects to /test after login
	</p>
</div>
