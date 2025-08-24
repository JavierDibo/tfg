import { alumnoApi } from '$lib/api';
import type {
	DTOAlumno,
	DTOActualizacionAlumno,
	DTOClaseInscrita,
	DTOPeticionRegistroAlumno
} from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class AlumnoService {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Get all students with pagination and filters
	 */
	static async getAlumnos(params?: {
		q?: string;
		firstName?: string;
		lastName?: string;
		dni?: string;
		email?: string;
		enrolled?: boolean;
		enabled?: boolean;
		available?: boolean;
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}): Promise<{ content: DTOAlumno[]; totalElements: number; totalPages: number }> {
		try {
			const response = await alumnoApi.obtenerAlumnos(params || {});
			return {
				content: response.content || [],
				totalElements: response.totalElements || 0,
				totalPages: response.totalPages || 0
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getAlumnos');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a student by ID
	 */
	static async getAlumno(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new student
	 */
	static async createAlumno(alumnoData: DTOPeticionRegistroAlumno): Promise<DTOAlumno> {
		try {
			return await alumnoApi.crearAlumno({
				dTOPeticionRegistroAlumno: alumnoData
			});
		} catch (error) {
			ErrorHandler.logError(error, 'createAlumno');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Update a student
	 */
	static async updateAlumno(id: number, alumnoData: DTOActualizacionAlumno): Promise<DTOAlumno> {
		try {
			return await alumnoApi.actualizarAlumno({
				id,
				dTOActualizacionAlumno: alumnoData
			});
		} catch (error) {
			ErrorHandler.logError(error, `updateAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a student
	 */
	static async deleteAlumno(id: number): Promise<void> {
		try {
			await alumnoApi.borrarAlumnoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteAlumno(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== STUDENT PROFILE OPERATIONS ====================

	/**
	 * Get enrolled classes for a student
	 */
	static async getClasesInscritas(alumnoId: number): Promise<DTOClaseInscrita> {
		try {
			return await alumnoApi.obtenerClasesInscritas({ id: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, `getClasesInscritas(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== STATUS UPDATE OPERATIONS ====================

	/**
	 * Change enrollment status for a student
	 * Uses the proper generated API with the updated DTO
	 */
	static async changeEnrollmentStatus(id: number, enrolled: boolean): Promise<DTOAlumno> {
		try {
			const updateData: DTOActualizacionAlumno = {
				enrolled: enrolled
			};

			console.log('Sending enrollment status change:', {
				id,
				updateData
			});

			const result = await alumnoApi.actualizarAlumno({
				id,
				dTOActualizacionAlumno: updateData
			});

			console.log('Response from enrollment status change:', result);
			return result;
		} catch (error) {
			ErrorHandler.logError(error, `changeEnrollmentStatus(${id}, ${enrolled})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Toggle enabled status for a student
	 * Uses the proper generated API with the updated DTO
	 */
	static async toggleEnabled(id: number, enabled: boolean): Promise<DTOAlumno> {
		try {
			const updateData: DTOActualizacionAlumno = {
				enabled: enabled
			};

			console.log('Sending enabled status change:', {
				id,
				updateData
			});

			const result = await alumnoApi.actualizarAlumno({
				id,
				dTOActualizacionAlumno: updateData
			});

			console.log('Response from enabled status change:', result);
			return result;
		} catch (error) {
			ErrorHandler.logError(error, `toggleEnabled(${id}, ${enabled})`);
			throw await ErrorHandler.parseError(error);
		}
	}
}
