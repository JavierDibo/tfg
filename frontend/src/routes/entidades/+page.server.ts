import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        // Basic auth credentials for Spring Security
        const username = 'admin';
        const password = 'admin';
        
        // Create base64 encoded credentials
        const credentials = Buffer.from(`${username}:${password}`).toString('base64');
        
        console.log('Making request to:', '/api/entidades/obtener');
        console.log('Authorization header:', `Basic ${credentials}`);
        
        const response = await fetch('/api/entidades/obtener', {
            method: 'GET',
            headers: {
                'Authorization': `Basic ${credentials}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        console.log('Response status:', response.status);
        console.log('Response headers:', Object.fromEntries(response.headers.entries()));
        
        if (response.ok) {
            const entidades = await response.json();
            console.log('Successfully fetched entidades:', entidades.length, 'items');
            
            return {
                entidades
            };
        } else if (response.status === 404) {
            // Spring Boot returns 404 when no entities found, which is expected
            console.log('No entities found (404 - expected behavior)');
            return {
                entidades: []
            };
        } else {
            const errorText = await response.text();
            console.error('Response error:', errorText);
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
    } catch (error) {
        console.error('Error fetching entidades:', error);
        
        const errorMessage = error instanceof Error ? error.message : 'Unknown error';
        return {
            entidades: [],
            error: `Failed to load entidades: ${errorMessage}`
        };
    }
}; 