import { ejercicioApi } from '$lib/api';

class EjercicioService {
	private api = ejercicioApi;

	async getEjercicios(params?: {
		classId?: string;
		q?: string;
		name?: string;
		statement?: string;
		status?: string;
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		try {
			const response = await this.api.obtenerEjercicios(params);
			return response;
		} catch (error) {
			console.error('Error fetching exercises:', error);
			throw error;
		}
	}

	async getEjercicioById(id: number) {
		try {
			const response = await this.api.obtenerEjercicioPorId({ id });
			return response;
		} catch (error) {
			console.error('Error fetching exercise:', error);
			throw error;
		}
	}

	async createEjercicio(ejercicio: {
		name: string;
		statement: string;
		startDate: Date;
		endDate: Date;
		classId: string;
	}) {
		try {
			const response = await this.api.crearEjercicio({
				dTOPeticionCrearEjercicio: ejercicio
			});
			return response;
		} catch (error) {
			console.error('Error creating exercise:', error);
			throw error;
		}
	}

	async updateEjercicio(
		id: number,
		ejercicio: {
			name?: string;
			statement?: string;
			startDate?: Date;
			endDate?: Date;
			classId?: string;
		}
	) {
		try {
			const response = await this.api.actualizarEjercicioParcial({
				id,
				dTOPeticionActualizarEjercicio: ejercicio
			});
			return response;
		} catch (error) {
			console.error('Error updating exercise:', error);
			throw error;
		}
	}

	async replaceEjercicio(
		id: number,
		ejercicio: {
			name: string;
			statement: string;
			startDate: Date;
			endDate: Date;
			classId: string;
		}
	) {
		try {
			const response = await this.api.reemplazarEjercicio({
				id,
				dTOPeticionActualizarEjercicio: ejercicio
			});
			return response;
		} catch (error) {
			console.error('Error replacing exercise:', error);
			throw error;
		}
	}

	async deleteEjercicio(id: number) {
		try {
			await this.api.eliminarEjercicio({ id });
		} catch (error) {
			console.error('Error deleting exercise:', error);
			throw error;
		}
	}

	async getEjerciciosActivos() {
		try {
			const response = await this.api.obtenerEjerciciosActivos();
			return response;
		} catch (error) {
			console.error('Error fetching active exercises:', error);
			throw error;
		}
	}

	async getEjerciciosVencidos() {
		try {
			const response = await this.api.obtenerEjerciciosVencidos();
			return response;
		} catch (error) {
			console.error('Error fetching expired exercises:', error);
			throw error;
		}
	}

	async getEjerciciosFuturos() {
		try {
			const response = await this.api.obtenerEjerciciosFuturos();
			return response;
		} catch (error) {
			console.error('Error fetching future exercises:', error);
			throw error;
		}
	}

	async getEjerciciosUrgentes() {
		try {
			const response = await this.api.obtenerEjerciciosUrgentes();
			return response;
		} catch (error) {
			console.error('Error fetching urgent exercises:', error);
			throw error;
		}
	}

	async getEstadisticas() {
		try {
			const response = await this.api.obtenerEstadisticas();
			return response;
		} catch (error) {
			console.error('Error fetching exercise statistics:', error);
			throw error;
		}
	}
}

export const ejercicioService = new EjercicioService();
