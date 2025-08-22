import { EntidadesApi, type DTOEntidad } from '../generated/api';
import { Configuration } from '../generated/api/runtime';

export interface ApiResponse {
	success: boolean;
	message: string;
	data?: unknown;
}

class EntidadService {
	private api: EntidadesApi;

	constructor() {
		// Configure the API with base path and authentication
		const config = new Configuration({
			basePath: 'http://localhost:8080',
			username: 'admin',
			password: 'admin'
		});
		this.api = new EntidadesApi(config);
	}

	async createEntity(info: string, otraInfo?: string): Promise<ApiResponse> {
		try {
			if (!info.trim()) {
				return {
					success: false,
					message: 'Info cannot be empty'
				};
			}

			const entity: DTOEntidad = {
				info: info.trim(),
				otraInfo: otraInfo?.trim() || ''
			};

			const result = await this.api.crearEntidad({ dTOEntidad: entity });

			return {
				success: true,
				message: 'Entity created successfully!',
				data: result
			};
		} catch (error) {
			return {
				success: false,
				message: `Error creating entity: ${error instanceof Error ? error.message : 'Unknown error'}`
			};
		}
	}

	async updateEntity(id: number, info: string, otraInfo?: string): Promise<ApiResponse> {
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

			const entity: DTOEntidad = {
				id,
				info: info.trim(),
				otraInfo: otraInfo?.trim() || ''
			};

			const result = await this.api.actualizarEntidad({ id, dTOEntidad: entity });

			return {
				success: true,
				message: `Entity ${id} updated successfully!`,
				data: result
			};
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

			const result = await this.api.borrarEntidadPorId({ id });

			return {
				success: true,
				message: `Entity ${id} deleted successfully!`,
				data: result
			};
		} catch (error) {
			return {
				success: false,
				message: `Error deleting entity: ${error instanceof Error ? error.message : 'Unknown error'}`
			};
		}
	}

	async deleteAllEntities(): Promise<ApiResponse> {
		try {
			const result = await this.api.borrarTodasLasEntidades();

			return {
				success: true,
				message: 'All entities deleted successfully!',
				data: result
			};
		} catch (error) {
			return {
				success: false,
				message: `Error deleting all entities: ${error instanceof Error ? error.message : 'Unknown error'}`
			};
		}
	}

	async getAllEntities(): Promise<DTOEntidad[]> {
		try {
			// Since obtenerEntidades returns a single entity, we need to handle this differently
			// For now, return an empty array as this API seems to be for single entity operations
			console.warn('obtenerEntidades returns single entity, not array. Returning empty array.');
			return [];
		} catch (error) {
			console.error('Error fetching entities:', error);
			return [];
		}
	}

	async getEntitiesByInfo(): Promise<DTOEntidad[]> {
		try {
			// Since obtenerEntidades returns a single entity, we need to handle this differently
			// For now, return an empty array as this API seems to be for single entity operations
			console.warn('obtenerEntidades returns single entity, not array. Returning empty array.');
			return [];
		} catch (error) {
			console.error('Error fetching entities by info:', error);
			return [];
		}
	}

	async getEntityById(id: number): Promise<DTOEntidad | null> {
		try {
			return await this.api.obtenerEntidadPorId({ id });
		} catch (error) {
			console.error('Error fetching entity by ID:', error);
			return null;
		}
	}
}

export const entidadService = new EntidadService();
