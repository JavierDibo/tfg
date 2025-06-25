<script lang="ts">
	import '../app.css';
	import Navbar from '$lib/components/Navbar.svelte';

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

{@render children()}