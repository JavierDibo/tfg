<script lang="ts">
	import type { PageData } from './$types';
	import '../app.css';
	import Grid from '$lib/components/Grid.svelte';
	import GridMobile from '$lib/components/GridMobile.svelte';
	import Navbar from '$lib/components/Navbar.svelte';
	import NavbarMobile from '$lib/components/NavbarMobile.svelte';
</script>

<div class="mx-auto max-w-[min(80vw,1300px)] min-w-[1000px]">
	<GridMobile />
</div>