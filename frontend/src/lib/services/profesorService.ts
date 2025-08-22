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
		nombre?: string;
		apellidos?: string;
		email?: string;
		habilitado?: boolean;
	}): Promise<DTOProfesor[]> {
		try {
			// Note: obtenerProfesores is deprecated, but still keeping for compatibility
			const response = await profesorApi.obtenerProfesores(filters);
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getAllProfesores');
			throw await ErrorHandler.parseError(error);
		}
	},

	async getProfesoresPaginados(
		filters: {
			nombre?: string;
			apellidos?: string;
			email?: string;
			usuario?: string;
			dni?: string;
			habilitado?: boolean;
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
			const response = await profesorApi.obtenerProfesoresPaginados({
				...filters,
				page,
				size,
				sortBy,
				sortDirection
			});

			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPaginados');
			throw await ErrorHandler.parseError(error);
		}
	},

	async getProfesoresHabilitadosPaginados(pagination: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: SortDirection;
	}): Promise<DTORespuestaPaginadaDTOProfesor> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const response = await profesorApi.obtenerProfesoresHabilitadosPaginados({
				page,
				size,
				sortBy,
				sortDirection
			});

			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresHabilitadosPaginados');
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

	async getProfesorByUsuario(usuario: string): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorUsuario({ usuario });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getProfesorByUsuario(${usuario})`);
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
			const response = await profesorApi.cambiarEstadoProfesor({
				id,
				requestBody: { habilitado }
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

			// Try to get total count, but handle gracefully if endpoint doesn't exist
			try {
				const totalStats = await profesorApi.obtenerTotalProfesores();
				totalCount = totalStats['total'] || 0;
			} catch (totalError) {
				console.warn('Total professors endpoint not available, using fallback:', totalError);
				// Fallback: get all professors and count them
				try {
					const allProfesores = await this.getAllProfesores({});
					totalCount = allProfesores.length;
				} catch (fallbackError) {
					console.error('Fallback for total count also failed:', fallbackError);
					totalCount = 0;
				}
			}

			// Try to get habilitacion statistics
			try {
				const habilitacionStats = await profesorApi.obtenerEstadisticasHabilitacion();
				activeCount = habilitacionStats['habilitados'] || 0;
				inactiveCount = habilitacionStats['noHabilitados'] || 0;
			} catch (habilitacionError) {
				console.warn(
					'Habilitacion statistics endpoint not available, using fallback:',
					habilitacionError
				);
				// Fallback: count enabled/disabled from all professors
				try {
					const allProfesores = await this.getAllProfesores({});
					activeCount = allProfesores.filter((p) => p.enabled).length;
					inactiveCount = allProfesores.filter((p) => !p.enabled).length;
				} catch (fallbackError) {
					console.error('Fallback for habilitacion count also failed:', fallbackError);
					activeCount = 0;
					inactiveCount = 0;
				}
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
		if (!data.usuario || data.usuario.length < 3) {
			errors.push('El nombre de usuario debe tener al menos 3 caracteres');
		}

		if (!data.password || data.password.length < 6) {
			errors.push('La contraseña debe tener al menos 6 caracteres');
		}

		if (!data.nombre) {
			errors.push('El nombre es obligatorio');
		}

		if (!data.apellidos) {
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
