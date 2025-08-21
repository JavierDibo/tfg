import {
	type DTOProfesor,
	type DTOPeticionRegistroProfesor,
	type DTOActualizacionProfesor,
	type DTORespuestaPaginadaDTOProfesor
} from '$lib/generated/api';
import { profesorApi } from '$lib/api';

// Define the ProfesorStatistics type used in the estadisticas page
export interface ProfesorStatistics {
	totalCount: number;
	activeCount: number;
	inactiveCount: number;
	bySubject?: Record<string, number>;
	byExperience?: Record<string, number>;
}

// Import pagination types
import type { SortDirection, PageMetadata, PaginatedData } from '$lib/types/pagination';

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
			console.error('Error fetching professors:', error);
			throw error;
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
			console.error('Error fetching paginated professors:', error);
			throw error;
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
			console.error('Error fetching paginated enabled professors:', error);
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

	async getProfesorByUsuario(usuario: string): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorUsuario({ usuario });
			return response;
		} catch (error) {
			console.error(`Error fetching professor with username ${usuario}:`, error);
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

	async updateProfesor(id: number, updateData: DTOActualizacionProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesor({
				id,
				dTOActualizacionProfesor: updateData
			});
			return response;
		} catch (error) {
			console.error(`Error updating professor with id ${id}:`, error);
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
	},

	async getStatistics(): Promise<ProfesorStatistics> {
		try {
			// Get total counts from the API endpoints
			const totalStats = await profesorApi.obtenerTotalProfesores();
			const habilitacionStats = await profesorApi.obtenerEstadisticasHabilitacion();

			return {
				totalCount: totalStats['total'] || 0,
				activeCount: habilitacionStats['habilitados'] || 0,
				inactiveCount: habilitacionStats['noHabilitados'] || 0
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
