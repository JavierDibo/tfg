import { redirect } from '@sveltejs/kit';
import type { PageLoad } from './$types';
import { createValidPaginationParams } from '$lib/types/pagination';

export const load: PageLoad = async ({ url, depends }) => {
	depends('alumnos:paginated'); // For selective invalidation

	// Parse and validate all parameters
	const params = createValidPaginationParams(url.searchParams);

	// Check if URL has invalid parameters and redirect if needed
	const currentUrl = new URL(url);
	let needsRedirect = false;

	// Validate page is not negative
	if (parseInt(url.searchParams.get('page') || '0') < 0) {
		currentUrl.searchParams.set('page', '0');
		needsRedirect = true;
	}

	// Validate size is within bounds
	const rawSize = parseInt(url.searchParams.get('size') || '20');
	if (rawSize < 1 || rawSize > 100) {
		currentUrl.searchParams.set('size', '20');
		needsRedirect = true;
	}

	if (needsRedirect) {
		throw redirect(302, currentUrl.pathname + currentUrl.search);
	}

	// Return parsed parameters and filters for component
	return {
		pagination: {
			page: params.page,
			size: params.size,
			sortBy: params.sortBy,
			sortDirection: params.sortDirection
		},
		filters: {
			nombre: params.nombre || '',
			apellidos: params.apellidos || '',
			dni: params.dni || '',
			email: params.email || '',
			matriculado: params.matriculado,
			busquedaGeneral: url.searchParams.get('busquedaGeneral') || '',
			searchMode: url.searchParams.get('searchMode') || 'simple'
		}
	};
};
