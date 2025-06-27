import {
	Configuration,
	EntidadRestApi,
	AutenticacionRestApi,
	type Middleware
} from './generated/api';
import { authStore } from './stores/authStore.svelte';

export * from './generated/api';

const authMiddleware: Middleware = {
	async pre({ init, url }) {
		const token = authStore.token;
		if (token) {
			// Ensure headers object exists
			if (!init.headers) {
				init.headers = {};
			}
			// Set the Authorization header
			(init.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
		}
		//The return type of pre is Promise<FetchParams | void> 
		//The init property is part of FetchParams so we must return it inside an object
		return { url, init };
	}
};

const config = new Configuration({
	basePath: 'http://localhost:8080',
	middleware: [authMiddleware]
});

// Create pre-configured API clients
export const entidadApi = new EntidadRestApi(config);
export const autenticacionApi = new AutenticacionRestApi(config); 