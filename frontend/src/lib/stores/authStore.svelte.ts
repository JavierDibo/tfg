import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
	sub: string;
	usuario?: string;
	id?: number;
	roles?: string[];
}

interface LoginResponse {
	token?: string;
	type?: string;
	id?: number;
	username?: string;
	email?: string;
	nombre?: string;
	apellidos?: string;
	rol?: string;
}

// State
let token = $state<string | null>(null);
let user = $state<DecodedToken | null>(null);
let isAuthenticated = $derived(!!token);
let isAdmin = $derived(user?.roles?.includes('ROLE_ADMIN') ?? false);
let isProfesor = $derived(user?.roles?.includes('ROLE_PROFESOR') ?? false);
let isAlumno = $derived(user?.roles?.includes('ROLE_ALUMNO') ?? false);

// Helper to decode and set user from token
function updateUserFromToken(jwt: string) {
	try {
		const decoded = jwtDecode<DecodedToken>(jwt);
		user = decoded;
		token = jwt;
		if (browser) {
			localStorage.setItem('jwt_token', jwt);
		}
	} catch (error) {
		console.error('Invalid token:', error);
		logout();
	}
}

// Actions
function login(jwtOrResponse: string | LoginResponse) {
	if (typeof jwtOrResponse === 'string') {
		// Handle JWT token
		updateUserFromToken(jwtOrResponse);
	} else {
		// Handle full login response
		const response = jwtOrResponse;
		const roleMapping = {
			'ADMIN': 'ROLE_ADMIN',
			'PROFESOR': 'ROLE_PROFESOR',
			'ALUMNO': 'ROLE_ALUMNO'
		};

		// Validate required fields
		if (!response.token || !response.username || !response.rol) {
			console.error('Invalid login response:', response);
			throw new Error('Invalid login response from server');
		}

		// Create a user object with the response data and proper role format
		user = {
			sub: response.username,
			usuario: response.username,
			id: response.id,
			roles: [roleMapping[response.rol as keyof typeof roleMapping] || `ROLE_${response.rol}`]
		};
		
		token = response.token;
		
		if (browser) {
			localStorage.setItem('jwt_token', token);
		}
	}
	
	// Redirect based on role
	if (user?.roles?.includes('ROLE_ADMIN')) {
		goto('/alumnos');
	} else if (user?.roles?.includes('ROLE_PROFESOR')) {
		goto('/alumnos');
	} else if (user?.roles?.includes('ROLE_ALUMNO')) {
		goto('/alumnos/perfil');
	} else {
		goto('/');
	}
}

function logout() {
	token = null;
	user = null;
	if (browser) {
		localStorage.removeItem('jwt_token');
	}
	goto('/auth');
}

// Initialization (run once on client)
if (browser) {
	const storedToken = localStorage.getItem('jwt_token');
	if (storedToken && !token) {
		updateUserFromToken(storedToken);
	}
}

// Export the reactive store
export const authStore = {
	get token() {
		return token;
	},
	get user() {
		return user;
	},
	get isAuthenticated() {
		return isAuthenticated;
	},
	get isAdmin() {
		return isAdmin;
	},
	get isProfesor() {
		return isProfesor;
	},
	get isAlumno() {
		return isAlumno;
	},
	login: login as (jwtOrResponse: string | LoginResponse) => void,
	logout
}; 