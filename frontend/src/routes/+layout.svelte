<script lang="ts">
	import '../app.css';
	import { authStore } from '$lib/stores/authStore.svelte';

	let { children } = $props();

	const languages = [
		{ code: 'es', name: 'Español', flag: '🇪🇸' },
		{ code: 'en', name: 'English', flag: '🇺🇸' },
		{ code: 'it', name: 'Italiano', flag: '🇮🇹' },
		{ code: 'de', name: 'Deutsch', flag: '🇩🇪' },
		{ code: 'fr', name: 'Français', flag: '🇫🇷' },
		{ code: 'pt', name: 'Português', flag: '🇵🇹' }
	] as const;

	type LanguageCode = (typeof languages)[number]['code'];

	let currentLanguage: LanguageCode = $state('es');
	let dropdownOpen = $state(false);

	const currentLanguageFlag = $derived(languages.find((l) => l.code === currentLanguage)?.flag);

	function changeLanguage(langCode: LanguageCode) {
		currentLanguage = langCode;
	}
</script>

<nav class="bg-gray-800">
	<div class="mx-auto max-w-7xl px-2 sm:px-6 lg:px-8">
		<div class="relative flex h-16 items-center justify-between">
			<div class="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
				<div class="flex flex-shrink-0 items-center">
					<span class="text-white font-bold">Academia</span>
				</div>
				<div class="hidden sm:ml-6 sm:block">
					<div class="flex space-x-4">
						<a
							href="/entidades"
							class="rounded-md bg-gray-900 px-3 py-2 text-sm font-medium text-white"
							aria-current="page">Entidades</a
						>
					</div>
				</div>
			</div>
			<div
				class="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0"
			>
				{#if authStore.isAuthenticated}
					<span class="text-gray-400 mr-4">Welcome, {authStore.user?.sub}</span>
					<button
						onclick={authStore.logout}
						class="rounded-full bg-gray-800 p-1 text-gray-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
					>
						Logout
					</button>
				{:else}
					<a
						href="/auth"
						class="rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
						>Login</a
					>
				{/if}
			</div>
		</div>
	</div>
</nav>

<main>
	{@render children()}
</main>