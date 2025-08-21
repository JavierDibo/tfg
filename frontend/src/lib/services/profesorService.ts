import { type DTOProfesor, type DTOPeticionRegistroProfesor } from '$lib/generated/api';
import { profesorApi } from '$lib/api';

export const ProfesorService = {
	async getAllProfesores(filters: {
		nombre?: string;
		apellidos?: string;
		email?: string;
		habilitado?: boolean;
	}): Promise<DTOProfesor[]> {
		try {
			const response = await profesorApi.obtenerProfesores(filters);
			return response;
		} catch (error) {
			console.error('Error fetching professors:', error);
			throw error;
		}
	},

	async getProfesorById(id: number): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorId({ id });
			return response;
		} catch (error) {
			console.error(`Error fetching professor with id ${id}:`, error);
			throw error;
		}
	},

	async createProfesor(profesorData: DTOPeticionRegistroProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.crearProfesor({
				dTOPeticionRegistroProfesor: profesorData
			});
			return response;
		} catch (error) {
			console.error('Error creating professor:', error);
			throw error;
		}
	},

	async deleteProfesor(id: number): Promise<void> {
		try {
			await profesorApi.borrarProfesorPorId({ id });
		} catch (error) {
			console.error(`Error deleting professor with id ${id}:`, error);
			throw error;
		}
	},

	async toggleAccountStatus(id: number, habilitado: boolean): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.cambiarEstadoProfesor({
				id,
				requestBody: { habilitado }
			});
			return response;
		} catch (error) {
			console.error(`Error changing account status for professor with id ${id}:`, error);
			throw error;
		}
	}
};
