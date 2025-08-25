import {
	type DTOProfesor,
	type DTOPeticionRegistroProfesor,
	type DTOActualizacionProfesor,
	type DTORespuestaPaginadaDTOProfesor
} from '$lib/generated/api';
import { profesorApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

// Define the ProfesorStatistics type used in the estadisticas page
export interface ProfesorStatistics {
	totalCount: number;
	activeCount: number;
	inactiveCount: number;
	newThisMonth: number;
	newThisYear: number;
	bySubject?: Record<string, number>;
	byExperience?: Record<string, number>;
}

// Import pagination types
import type { SortDirection } from '$lib/types/pagination';

export const ProfesorService = {
	async getAllProfesores(filters: {
		firstName?: string;
		lastName?: string;
		email?: string;
		enabled?: boolean;
	}): Promise<DTOProfesor[]> {
		try {
			const response = await profesorApi.obtenerProfesores(filters);
			return response.content as DTOProfesor[];
		} catch (error) {
			ErrorHandler.logError(error, 'getAllProfesores');
			throw await ErrorHandler.parseError(error);
		}
	},

	async getProfesoresPaginados(
		filters: {
			q?: string; // General search parameter
			firstName?: string;
			lastName?: string;
			email?: string;
			username?: string;
			dni?: string;
			enabled?: boolean;
			claseId?: string;
			sinClases?: boolean;
		},
		pagination: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: SortDirection;
		}
	): Promise<DTORespuestaPaginadaDTOProfesor> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const response = await profesorApi.obtenerProfesores({
				...filters,
				page,
				size,
				sortBy,
				sortDirection
			});

			return response as DTORespuestaPaginadaDTOProfesor;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPaginados');
			throw await ErrorHandler.parseError(error);
		}
	},

	async getProfesorById(id: number): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getProfesorById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	async createProfesor(profesorData: DTOPeticionRegistroProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.crearProfesor({
				dTOPeticionRegistroProfesor: profesorData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createProfesor');
			throw await ErrorHandler.parseError(error);
		}
	},

	async updateProfesor(id: number, updateData: DTOActualizacionProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesor({
				id,
				dTOActualizacionProfesor: updateData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	async deleteProfesor(id: number): Promise<void> {
		try {
			await profesorApi.borrarProfesorPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	async toggleAccountStatus(id: number, habilitado: boolean): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesor({
				id,
				dTOActualizacionProfesor: { enabled: habilitado }
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `toggleAccountStatus(${id}, ${habilitado})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	async getStatistics(): Promise<ProfesorStatistics> {
		try {
			let totalCount = 0;
			let activeCount = 0;
			let inactiveCount = 0;

			// Get statistics by fetching all professors and counting
			try {
				const allProfesores = await this.getAllProfesores({});
				totalCount = allProfesores.length;
				activeCount = allProfesores.filter((p) => p.enabled).length;
				inactiveCount = allProfesores.filter((p) => !p.enabled).length;
			} catch (error) {
				console.error('Error fetching professor statistics:', error);
				totalCount = 0;
				activeCount = 0;
				inactiveCount = 0;
			}

			return {
				totalCount,
				activeCount,
				inactiveCount,
				newThisMonth: 0, // TODO: Implement when API provides this data
				newThisYear: 0 // TODO: Implement when API provides this data
			};
		} catch (error) {
			console.error('Error fetching professor statistics:', error);
			throw error;
		}
	},

	validateRegistrationData(data: DTOPeticionRegistroProfesor): string[] {
		const errors: string[] = [];

		// Basic validation rules
		if (!data.username || data.username.length < 3) {
			errors.push('El nombre de usuario debe tener al menos 3 caracteres');
		}

		if (!data.password || data.password.length < 6) {
			errors.push('La contraseña debe tener al menos 6 caracteres');
		}

		if (!data.firstName) {
			errors.push('El nombre es obligatorio');
		}

		if (!data.lastName) {
			errors.push('Los apellidos son obligatorios');
		}

		if (!data.dni) {
			errors.push('El DNI es obligatorio');
		}

		if (!data.email) {
			errors.push('El email es obligatorio');
		} else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
			errors.push('El formato del email no es válido');
		}

		return errors;
	}
};
