import type { PageLoad } from './$types';

export const load: PageLoad = ({ url }: { url: URL }) => {
	// Pagination
	const page = parseInt(url.searchParams.get('page') ?? '0', 10);
	const size = parseInt(url.searchParams.get('size') ?? '10', 10);
	const sortBy = url.searchParams.get('sortBy') ?? 'id';
	const sortDirection = (url.searchParams.get('sortDirection') ?? 'ASC').toUpperCase() as
		| 'ASC'
		| 'DESC';

	// Filters
	const searchMode = (url.searchParams.get('searchMode') ?? 'simple') as 'simple' | 'advanced';
	const q = url.searchParams.get('q') ?? undefined;
	const nombre = url.searchParams.get('nombre') ?? undefined;
	const apellidos = url.searchParams.get('apellidos') ?? undefined;
	const dni = url.searchParams.get('dni') ?? undefined;
	const email = url.searchParams.get('email') ?? undefined;
	const habilitadoParam = url.searchParams.get('habilitado');
	const habilitado =
		habilitadoParam === 'true' ? true : habilitadoParam === 'false' ? false : undefined;

	return {
		pagination: {
			page,
			size,
			sortBy,
			sortDirection
		},
		filters: {
			searchMode,
			q,
			nombre,
			apellidos,
			dni,
			email,
			habilitado
		}
	};
};
