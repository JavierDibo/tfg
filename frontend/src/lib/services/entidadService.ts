export interface Entidad {
    id: number;
    info: string;
}

export interface ApiResponse {
    success: boolean;
    message: string;
    data?: any;
}

class EntidadService {
    // Restore auth headers since Spring Security requires authentication
    private getHeaders() {
        const credentials = btoa('admin:admin');
        return {
            'Authorization': `Basic ${credentials}`,
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    async createEntity(info: string): Promise<ApiResponse> {
        try {
            if (!info.trim()) {
                return {
                    success: false,
                    message: 'Info cannot be empty'
                };
            }

            const response = await fetch(`/api/entidades/crear?info=${encodeURIComponent(info)}`, {
                method: 'POST',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return {
                    success: true,
                    message: 'Entity created successfully!'
                };
            } else {
                return {
                    success: false,
                    message: `Error creating entity: ${response.status} ${response.statusText}`
                };
            }
        } catch (error) {
            return {
                success: false,
                message: `Error creating entity: ${error instanceof Error ? error.message : 'Unknown error'}`
            };
        }
    }

    async updateEntity(id: number, info: string): Promise<ApiResponse> {
        try {
            if (!id || id <= 0) {
                return {
                    success: false,
                    message: 'Invalid entity ID'
                };
            }

            if (!info.trim()) {
                return {
                    success: false,
                    message: 'Info cannot be empty'
                };
            }

            const response = await fetch(`/api/entidades/actualizar/${id}?info=${encodeURIComponent(info)}`, {
                method: 'PATCH',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return {
                    success: true,
                    message: `Entity ${id} updated successfully!`
                };
            } else if (response.status === 404) {
                return {
                    success: false,
                    message: `Entity with ID ${id} not found`
                };
            } else {
                return {
                    success: false,
                    message: `Error updating entity: ${response.status} ${response.statusText}`
                };
            }
        } catch (error) {
            return {
                success: false,
                message: `Error updating entity: ${error instanceof Error ? error.message : 'Unknown error'}`
            };
        }
    }

    async deleteEntity(id: number): Promise<ApiResponse> {
        try {
            if (!id || id <= 0) {
                return {
                    success: false,
                    message: 'Invalid entity ID'
                };
            }

            const response = await fetch(`/api/entidades/borrar/${id}`, {
                method: 'DELETE',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return {
                    success: true,
                    message: `Entity ${id} deleted successfully!`
                };
            } else if (response.status === 404) {
                return {
                    success: false,
                    message: `Entity with ID ${id} not found`
                };
            } else {
                return {
                    success: false,
                    message: `Error deleting entity: ${response.status} ${response.statusText}`
                };
            }
        } catch (error) {
            return {
                success: false,
                message: `Error deleting entity: ${error instanceof Error ? error.message : 'Unknown error'}`
            };
        }
    }

    async deleteAllEntities(): Promise<ApiResponse> {
        try {
            const response = await fetch('/api/entidades/borrar', {
                method: 'DELETE',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return {
                    success: true,
                    message: 'All entities deleted successfully!'
                };
            } else {
                return {
                    success: false,
                    message: `Error deleting all entities: ${response.status} ${response.statusText}`
                };
            }
        } catch (error) {
            return {
                success: false,
                message: `Error deleting all entities: ${error instanceof Error ? error.message : 'Unknown error'}`
            };
        }
    }

    async getAllEntities(): Promise<Entidad[]> {
        try {
            const response = await fetch('/api/entidades/obtener', {
                method: 'GET',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return await response.json();
            } else if (response.status === 404) {
                // Spring Boot returns 404 when no entities found, which is expected
                return [];
            } else {
                console.error('Error fetching entities:', response.status, response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error fetching entities:', error);
            return [];
        }
    }

    async getEntitiesByInfo(info: string): Promise<Entidad[]> {
        try {
            const response = await fetch(`/api/entidades/obtener?info=${encodeURIComponent(info)}`, {
                method: 'GET',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return await response.json();
            } else if (response.status === 404) {
                // No entities found with that info
                return [];
            } else {
                console.error('Error fetching entities by info:', response.status, response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error fetching entities by info:', error);
            return [];
        }
    }

    async getEntityById(id: number): Promise<Entidad | null> {
        try {
            if (!id || id <= 0) {
                return null;
            }

            const response = await fetch(`/api/entidades/obtener/${id}`, {
                method: 'GET',
                headers: this.getHeaders()
            });

            if (response.ok) {
                return await response.json();
            } else if (response.status === 404) {
                return null;
            } else {
                console.error('Error fetching entity by ID:', response.status, response.statusText);
                return null;
            }
        } catch (error) {
            console.error('Error fetching entity by ID:', error);
            return null;
        }
    }
}

// Export a singleton instance
export const entidadService = new EntidadService(); 