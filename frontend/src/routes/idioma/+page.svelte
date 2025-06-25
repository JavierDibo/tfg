<script lang="ts">
	import * as m from '$lib/paraglide/messages';
	
	const languages = [
		{ code: 'es', name: 'Español', flag: '🇪🇸' },
		{ code: 'en', name: 'English', flag: '🇺🇸' },
		{ code: 'it', name: 'Italiano', flag: '🇮🇹' },
		{ code: 'de', name: 'Deutsch', flag: '🇩🇪' },
		{ code: 'fr', name: 'Français', flag: '🇫🇷' },
		{ code: 'pt', name: 'Português', flag: '🇵🇹' }
	] as const;
	
	let currentLanguage: 'es' | 'en' | 'it' | 'de' | 'fr' | 'pt' = 'es';
	
	$: greeting = m.bienvenida({ name: `Javi` }, { locale: currentLanguage });
	
	function changeLanguage(langCode: 'es' | 'en' | 'it' | 'de' | 'fr' | 'pt') {
		currentLanguage = langCode;
	}
</script>

<div class="max-w-2xl p-8 mx-auto my-8 font-sans">
	<h1 class="mb-8 text-4xl font-bold text-center text-gray-800">{greeting}</h1>

	<div class="mt-8 text-center">
		<h2 class="mb-4 text-2xl font-bold text-gray-600">Choose Language:</h2>
		<div class="flex flex-wrap justify-center gap-4 my-4">
			{#each languages as lang}
				<button
					class="cursor-pointer rounded-lg border-2 px-6 py-3 text-base transition-all duration-200 ease-in-out {currentLanguage === lang.code ? 'border-blue-500 bg-blue-500 text-white' : 'border-gray-300 bg-white hover:border-blue-500 hover:bg-gray-50'}"
					on:click={() => changeLanguage(lang.code)}
				>
					{lang.flag} {lang.name}
				</button>
			{/each}
		</div>
		<p class="mt-4 text-lg text-gray-500">
			Current language:
			<strong class="font-bold">{languages.find((l) => l.code === currentLanguage)?.name}</strong>
		</p>
	</div>
</div>