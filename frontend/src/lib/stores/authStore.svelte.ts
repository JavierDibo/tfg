import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
	sub: string;
	roles: string[];
}

// State
let token = $state<string | null>(null);
let user = $state<DecodedToken | null>(null);
let isAuthenticated = $derived(!!token);
let isAdmin = $derived(user?.roles?.includes('ROLE_ADMIN') ?? false);

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
function login(jwt: string) {
	updateUserFromToken(jwt);
	goto('/entidades');
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
	login,
	logout
}; 