import { entregaApi } from '$lib/api';

class EntregaServiceClass {
	private api = entregaApi;

	async getEntregas(params?: {
		alumnoId?: string;
		ejercicioId?: string;
		estado?: string;
		notaMin?: number;
		notaMax?: number;
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		try {
			const response = await this.api.obtenerEntregas(params);
			return response;
		} catch (error) {
			console.error('Error fetching deliveries:', error);
			throw error;
		}
	}

	async getEntregaById(id: number) {
		try {
			const response = await this.api.obtenerEntregaPorId({ id });
			return response;
		} catch (error) {
			console.error('Error fetching delivery:', error);
			throw error;
		}
	}

	async createEntrega(entrega: {
		ejercicioId: string;
		alumnoEntreganteId: string;
		archivosEntregados?: string[];
	}) {
		try {
			const response = await this.api.crearEntrega({
				dTOPeticionCrearEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			console.error('Error creating delivery:', error);
			throw error;
		}
	}

	async updateEntrega(
		id: number,
		entrega: {
			nota?: number;
			archivosEntregados?: string[];
			comentarios?: string;
		}
	) {
		try {
			const response = await this.api.actualizarEntregaParcial({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			console.error('Error updating delivery:', error);
			throw error;
		}
	}

	async replaceEntrega(
		id: number,
		entrega: {
			nota?: number;
			archivosEntregados?: string[];
			comentarios?: string;
		}
	) {
		try {
			const response = await this.api.reemplazarEntrega({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			console.error('Error replacing delivery:', error);
			throw error;
		}
	}

	async deleteEntrega(id: number) {
		try {
			await this.api.eliminarEntrega({ id });
		} catch (error) {
			console.error('Error deleting delivery:', error);
			throw error;
		}
	}

	async getEstadisticas() {
		try {
			const response = await this.api.obtenerEstadisticas1();
			return response;
		} catch (error) {
			console.error('Error fetching delivery statistics:', error);
			throw error;
		}
	}

	// Helper methods for common use cases
	async getEntregasByEjercicio(
		ejercicioId: string,
		params?: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: string;
		}
	) {
		return this.getEntregas({
			ejercicioId,
			...params
		});
	}

	async getEntregasByAlumno(
		alumnoId: string,
		params?: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: string;
		}
	) {
		return this.getEntregas({
			alumnoId,
			...params
		});
	}

	async getEntregasCalificadas(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		return this.getEntregas({
			estado: 'CALIFICADO',
			...params
		});
	}

	async getEntregasPendientes(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		return this.getEntregas({
			estado: 'PENDIENTE',
			...params
		});
	}

	async getEntregasEntregadas(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		return this.getEntregas({
			estado: 'ENTREGADO',
			...params
		});
	}
}

export const EntregaService = new EntregaServiceClass();
