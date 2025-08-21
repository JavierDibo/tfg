import { deLocalizeUrl } from '$lib/paraglide/runtime';

export const reroute = (request) => deLocalizeUrl(request.url).pathname;

// For SvelteKit's internal client-side requests
export const transport = ({ request, fetch }) => {
	return fetch(request);
};
