import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        // Basic auth credentials
        const username = 'admin';
        const password = 'admin';
        
        // Create base64 encoded credentials
        const credentials = Buffer.from(`${username}:${password}`).toString('base64');
        
        console.log('Making request to:', '/api/entidades/lista');
        console.log('Authorization header:', `Basic ${credentials}`);
        
        const response = await fetch('/api/entidades/lista', {
            method: 'GET',
            headers: {
                'Authorization': `Basic ${credentials}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        console.log('Response status:', response.status);
        console.log('Response headers:', Object.fromEntries(response.headers.entries()));
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Response error:', errorText);
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        
        const entidades = await response.json();
        console.log('Successfully fetched entidades:', entidades.length, 'items');
        
        return {
            entidades
        };
    } catch (error) {
        console.error('Error fetching entidades:', error);
        
        const errorMessage = error instanceof Error ? error.message : 'Unknown error';
        return {
            entidades: [],
            error: `Failed to load entidades: ${errorMessage}`
        };
    }
}; 