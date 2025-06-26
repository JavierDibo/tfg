<script lang="ts">
	import type { PageData } from './$types';
	import EntityManager from '$lib/components/EntityManager.svelte';

	let { data }: { data: PageData } = $props();
</script>

<EntityManager initialEntities={data.entidades} error={data.error} />
