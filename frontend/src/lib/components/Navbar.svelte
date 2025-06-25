<script lang="ts">
	let { children } = $props();
	let itemsClass: string =
		'hover:underline hover:underline-offset-4 transition-all hover:text-pink-700';
	import logo from '$lib/assets/logo-vectorizado.svg';
</script>

<div class="w-full py-3">
	<div class="container mx-auto flex h-[80px] items-center justify-between">
		<div class="flex items-center">
			<img src={logo} alt="Sombrero de Goya" class="h-15 mr-2" />
			<div>
				<div class="montserrat-light pl-10 text-[15px]">Academia de Arte</div>
				<div class="flex items-center -mt-2">
					<div class="h-8 w-8 rounded-full bg-black flex items-center justify-center">
						<h1 class="montserrat-bold text-[20px] text-white">El</h1>
					</div>
					<div class="montserrat-semibold ml-1 text-[23px]">Sombrero de Goya</div>
				</div>
			</div>
		</div>
		<nav class="lato-regular flex items-center gap-[40px] text-[19px]">
			<a href="/" class={itemsClass}>Inicio</a>
			<a href="/cursos" class={itemsClass}>Cursos</a>
			<a href="/videos" class={itemsClass}>Videos</a>
			<a href="/horarios" class={itemsClass}>Horarios</a>
			<a href="/contacto" class={itemsClass}>Contacto</a>
			<a href="/sobre-nosotros" class={itemsClass}>Sobre nosotros</a>
		</nav>
	</div>
</div>

{@render children()}
