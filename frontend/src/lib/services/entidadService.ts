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
    private getAuthHeaders() {
        const credentials = btoa('admin:admin');
        return {
            'Authorization': `Basic ${credentials}`
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
                headers: this.getAuthHeaders()
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
                headers: this.getAuthHeaders()
            });

            if (response.ok) {
                return {
                    success: true,
                    message: `Entity ${id} deleted successfully!`
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
                headers: this.getAuthHeaders()
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
            const response = await fetch('/api/entidades/lista', {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            if (response.ok) {
                return await response.json();
            } else {
                console.error('Error fetching entities:', response.status, response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error fetching entities:', error);
            return [];
        }
    }
}

// Export a singleton instance
export const entidadService = new EntidadService(); 