<script lang="ts">
	import type { PageData } from './$types';
	import '../app.css';
	import Main from '$lib/components/Main.svelte';
	import Navbar from '$lib/components/Navbar.svelte';
</script>

<Navbar>
	<Main />
</Navbar>