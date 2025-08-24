import type {
	DTOClase,
	DTOCurso,
	DTOTaller,
	DTOPeticionCrearCurso,
	DTOPeticionCrearTaller,
	DTORespuestaPaginada,
	DTORespuestaPaginadaDTOClase
} from '$lib/generated/api';
import { claseApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class ClaseService {
	/**
	 * Get all classes (filtered by user role)
	 */
	static async getClases(params: Record<string, unknown> = {}): Promise<DTORespuestaPaginada> {
		try {
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getClases');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a class by ID
	 */
	static async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'getClaseById');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new course
	 */
	static async crearCurso(curso: DTOPeticionCrearCurso): Promise<DTOCurso> {
		try {
			return await claseApi.crearCurso({ dTOPeticionCrearCurso: curso });
		} catch (error) {
			ErrorHandler.logError(error, 'crearCurso');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new workshop
	 */
	static async crearTaller(taller: DTOPeticionCrearTaller): Promise<DTOTaller> {
		try {
			return await claseApi.crearTaller({ dTOPeticionCrearTaller: taller });
		} catch (error) {
			ErrorHandler.logError(error, 'crearTaller');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a class by ID
	 */
	static async borrarClasePorId(id: number): Promise<void> {
		try {
			await claseApi.borrarClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'borrarClasePorId');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get classes with pagination and filters
	 */
	static async buscarClasesConPaginacion(
		page: number = 0,
		size: number = 10,
		sortBy?: string,
		sortDirection?: string,
		filters: Record<string, unknown> = {}
	): Promise<DTORespuestaPaginadaDTOClase> {
		try {
			const params = {
				page,
				size,
				sortBy,
				sortDirection,
				...filters
			};
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'buscarClasesConPaginacion');
			throw await ErrorHandler.parseError(error);
		}
	}
}
