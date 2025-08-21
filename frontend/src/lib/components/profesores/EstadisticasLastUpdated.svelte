<script lang="ts">
	// Get current date and time in local format
	const now = new Date();
	const formattedDate = now.toLocaleDateString('es-ES', {
		day: 'numeric',
		month: 'long',
		year: 'numeric'
	});
	const formattedTime = now.toLocaleTimeString('es-ES', {
		hour: '2-digit',
		minute: '2-digit'
	});
</script>

<div class="text-center">
	<p class="text-sm text-gray-500">
		Última actualización: {formattedDate} a las {formattedTime}
	</p>
</div>
