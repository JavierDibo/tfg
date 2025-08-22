<script lang="ts">
	import type { ErrorInfo } from '$lib/utils/errorHandler';

	export let error: ErrorInfo;
	export let onRetry: (() => void) | undefined = undefined;
	export let onDismiss: (() => void) | undefined = undefined;
	export let showTitle = true;
	export let compact = false;

	function getIconClass(): string {
		switch (error.type) {
			case 'error':
				return 'text-red-400';
			case 'warning':
				return 'text-yellow-400';
			case 'info':
				return 'text-blue-400';
			default:
				return 'text-red-400';
		}
	}

	function getContainerClass(): string {
		const baseClass = 'rounded-md border p-4';
		const typeClass = error.type === 'error' 
			? 'border-red-200 bg-red-50' 
			: error.type === 'warning'
			? 'border-yellow-200 bg-yellow-50'
			: 'border-blue-200 bg-blue-50';
		
		return `${baseClass} ${typeClass}`;
	}

	function getTitleClass(): string {
		const baseClass = 'text-sm font-medium';
		const typeClass = error.type === 'error'
			? 'text-red-800'
			: error.type === 'warning'
			? 'text-yellow-800'
			: 'text-blue-800';
		
		return `${baseClass} ${typeClass}`;
	}

	function getMessageClass(): string {
		const baseClass = 'text-sm';
		const typeClass = error.type === 'error'
			? 'text-red-700'
			: error.type === 'warning'
			? 'text-yellow-700'
			: 'text-blue-700';
		
		return `${baseClass} ${typeClass}`;
	}

	function handleRetry() {
		if (onRetry) {
			onRetry();
		}
	}

	function handleDismiss() {
		if (onDismiss) {
			onDismiss();
		}
	}
</script>

<div class={getContainerClass()}>
	<div class="flex">
		<div class="flex-shrink-0">
			{#if error.type === 'error'}
				<svg class="h-5 w-5 {getIconClass()}" viewBox="0 0 20 20" fill="currentColor">
					<path
						fill-rule="evenodd"
						d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
						clip-rule="evenodd"
					/>
				</svg>
			{:else if error.type === 'warning'}
				<svg class="h-5 w-5 {getIconClass()}" viewBox="0 0 20 20" fill="currentColor">
					<path
						fill-rule="evenodd"
						d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
						clip-rule="evenodd"
					/>
				</svg>
			{:else}
				<svg class="h-5 w-5 {getIconClass()}" viewBox="0 0 20 20" fill="currentColor">
					<path
						fill-rule="evenodd"
						d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
						clip-rule="evenodd"
					/>
				</svg>
			{/if}
		</div>
		<div class="ml-3 flex-1">
			{#if showTitle && error.title}
				<h3 class={getTitleClass()}>{error.title}</h3>
			{/if}
			<div class={getMessageClass()}>
				<p>{error.message}</p>
				
				{#if error.fieldErrors && Object.keys(error.fieldErrors).length > 0}
					<div class="mt-2">
						<p class="font-medium">Errores de validación:</p>
						<ul class="mt-1 list-disc list-inside space-y-1">
							{#each Object.entries(error.fieldErrors) as [field, errors] (field)}
								<li>
									<span class="font-medium">{field}:</span> {errors.join(', ')}
								</li>
							{/each}
						</ul>
					</div>
				{/if}
			</div>

			{#if !compact && (onRetry || onDismiss)}
				<div class="mt-4 flex space-x-3">
					{#if onRetry && error.canRetry}
						<button
							type="button"
							class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
							on:click={handleRetry}
						>
							Reintentar
						</button>
					{/if}
					{#if onDismiss}
						<button
							type="button"
							class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
							on:click={handleDismiss}
						>
							Cerrar
						</button>
					{/if}
				</div>
			{/if}
		</div>
	</div>
</div>
