<script lang="ts">
	let { children = undefined } = $props();
	let itemsClass: string =
		'block py-2 px-3 text-gray-900 rounded-sm hover:bg-gray-100 md:hover:bg-transparent md:border-0 md:hover:text-pink-700 md:p-0 transition-all';
	import logo from '$lib/assets/logo-vectorizado.svg';
</script>

<nav class="w-full border-gray-200 bg-white py-3">
	<div class="container mx-auto flex h-[80px] flex-wrap items-center justify-between px-4">
		<!-- Logo Section -->
		<a href="/" class="flex items-center">
			<img src={logo} alt="Sombrero de Goya" class="h-15 mr-2" />
			<div>
				<div class="montserrat-light pl-10 text-[15px]">Academia de Arte</div>
				<div class="-mt-2 flex items-center">
					<div class="flex h-8 w-8 items-center justify-center rounded-full bg-black">
						<h1 class="montserrat-bold text-[20px] text-white">El</h1>
					</div>
					<div class="montserrat-semibold ml-1 text-[23px]">Sombrero de Goya</div>
				</div>
			</div>
		</a>
	</div>

	<!-- Hamburger Button -->
	<button
		data-collapse-toggle="navbar-default"
		type="button"
		class="inline-flex h-10 w-10 items-center justify-center rounded-lg p-2 text-sm text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200"
		aria-controls="navbar-default"
		aria-expanded="false"
	>
		<span class="sr-only">Open main menu</span>
		<svg
			class="h-5 w-5"
			aria-hidden="true"
			xmlns="http://www.w3.org/2000/svg"
			fill="none"
			viewBox="0 0 17 14"
		>
			<path
				stroke="currentColor"
				stroke-linecap="round"
				stroke-linejoin="round"
				stroke-width="2"
				d="M1 1h15M1 7h15M1 13h15"
			/>
		</svg>
	</button>

	<!-- Navigation Menu -->
	<div class="hidden w-full md:block md:w-auto" id="navbar-default">
		<ul
			class="lato-regular mt-4 flex flex-col rounded-lg border border-gray-100 bg-gray-50 p-4 text-[19px] font-medium md:mt-0 md:flex-row md:space-x-8 md:border-0 md:bg-white md:p-0 rtl:space-x-reverse"
		>
			<li>
				<a
					href="/"
					class="{itemsClass} bg-gray-100 text-pink-700 md:bg-transparent md:text-pink-700"
					aria-current="page">Inicio</a
				>
			</li>
			<li>
				<a href="/cursos" class={itemsClass}>Cursos</a>
			</li>
			<li>
				<a href="/videos" class={itemsClass}>Videos</a>
			</li>
			<li>
				<a href="/horarios" class={itemsClass}>Horarios</a>
			</li>
			<li>
				<a href="/contacto" class={itemsClass}>Contacto</a>
			</li>
			<li>
				<a href="/sobre-nosotros" class={itemsClass}>Sobre nosotros</a>
			</li>
		</ul>
	</div>
</nav>

{#if children}
	{@render children()}
{/if}
