<script lang="ts">
	import '$lib/../app.css';

	import tren from '$lib/assets/tren.jpg';
	import joker from '$lib/assets/joker.jpg';
	import turbante from '$lib/assets/turbante.png';
	import instagram from '$lib/assets/instagram.svg';
	import casamx from '$lib/assets/casa-mexico.png';
</script>

<div class="lato-regular grid grid-cols-4 grid-rows-2">
	<!-- AMARILLO -->
	<a href="/horarios" class="contents" target="_blank" rel="noopener noreferrer">
		<div
			class="cursor-default text-black-900 bg-linear-270 z-50 flex aspect-square flex-col items-center
			justify-center overflow-hidden from-[#f9b10f] to-[#fbb747] transition-all duration-300 hover:scale-105 hover:shadow-lg"
		>
			<h1 class="text-3xl">
				Nuevos talleres<br />de verano
			</h1>

			<button class="cursor-pointer px-15 mt-10 bg-white py-2 text-lg"> Ver Horarios </button>
		</div>
	</a>

	<!-- TREN -->
	<div class="aspect-2-1 relative row-span-2 overflow-hidden">
		<img
			src={tren}
			alt="Train"
			class="scale-104 absolute inset-0 h-full w-full object-cover object-[65%_0%]"
		/>
	</div>

	<!-- JOKER -->
	<div class="relative aspect-square overflow-hidden">
		<img src={joker} alt="Joker" class="scale-130 absolute w-full object-cover object-[0%-47%]" />
	</div>

	<!-- TURBANTE -->
	<div class="aspect-2-1 relative z-10 row-span-2 overflow-hidden">
		<img
			src={turbante}
			alt="Turbante"
			class="scale-117 absolute inset-0 h-full w-full object-cover object-[30%-0%]"
		/>
		<a
			href="https://www.casademexico.es/actividades-academicas/verano-2025-taller-de-acuarela-retratos-y-paisajes/"
			class="contents"
			target="_blank" 
			rel="noopener noreferrer"
		>
			<div
				class="cursor-pointer relative m-5 bg-[#f5f5f5] p-3 transition-all duration-300 hover:z-50 hover:scale-105 hover:shadow-lg hover:shadow-black"
			>
				<div class="relative text-[22px] text-black"> Talleres de Acuarela en </div>
				<img src={casamx} alt="Casa de México logo" class="" />
			</div>
		</a>
	</div>

	<!-- GRIS -->
	<div
		class="bg-linear-90 relative flex aspect-square flex-col items-center justify-center overflow-hidden from-[#eeeae9] to-[#e9e4e2] p-10"
	>
		<h1
			class="text-black-900 hover:scale-101 text-2xl underline underline-offset-4 transition-all duration-300 hover:text-pink-700"
		>
			<a
				href="https://www.instagram.com/elsombrerodegoya/"
				target="_blank"
				rel="noopener noreferrer"
				class="inline-block"
			>
				Échale un vistazo a nuestras redes sociales ➡️
				<img src={instagram} alt="Instagram" class="-ml-1 inline-block h-7 w-7 align-text-bottom" />
			</a>
		</h1>
	</div>

	<!-- NEGRO -->
	<a href="https://cobaltoacademia.com/categoria-producto/tienda" class="contents" target="_blank" rel="noopener noreferrer">
		<div
			class="cursor-default lato-regular z-10 flex aspect-square flex-col items-center justify-center overflow-hidden bg-[#010504] p-10 transition-all duration-300 hover:z-50 hover:scale-105 hover:shadow-lg"
		>
			<h1 class="text-3xl text-white">Clases de dibujo y pintura de Lunes a Sábado</h1>

			<button class="p-15 mt-10 cursor-pointer bg-white py-2 text-lg"> Apuntarse </button>
		</div>
	</a>
</div>
