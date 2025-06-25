<script lang="ts">
	import { page } from '$app/state';
	import Navbar from '$lib/components/Navbar.svelte';
</script>

<Navbar>
	<div class="flex flex-col items-center justify-center min-h-[70vh] py-20">
		<div class="text-[120px] montserrat-bold text-gray-800 mb-4">{page.status}</div>
		<div class="montserrat-semibold text-4xl mb-8">
			{#if page.status === 404}
				Página no encontrada
			{:else}
				Ha ocurrido un error
			{/if}
		</div>
		<p class="lato-regular text-xl text-gray-600 max-w-lg text-center mb-10">
			{#if page.status === 404}
				Lo sentimos, la página que estás buscando no existe o ha sido movida.
			{:else}
				Lo sentimos, ha ocurrido un error. Por favor, inténtalo de nuevo más tarde.
			{/if}
		</p>
		<a 
			href="/" 
			class="bg-black text-white px-8 py-3 lato-regular hover:bg-pink-700 transition-colors duration-300"
		>
			Volver al inicio
		</a>
	</div>
</Navbar> 