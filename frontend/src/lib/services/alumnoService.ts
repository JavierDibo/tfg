import type {
	DTOAlumno,
	DTOPeticionRegistroAlumno,
	DTOActualizacionAlumno,
	DTORespuestaPaginadaDTOAlumno,
	DTOPerfilAlumno,
	DTOClaseInscrita
} from '$lib/generated/api';
import { alumnoApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class AlumnoService {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Get all students with pagination
	 */
	static async getPaginatedAlumnos(
		params: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: 'ASC' | 'DESC';
			nombre?: string;
			apellidos?: string;
			dni?: string;
			email?: string;
			matriculado?: boolean;
		} = {}
	): Promise<DTORespuestaPaginadaDTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnosPaginados(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getPaginatedAlumnos');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get available students for enrollment (enabled and matriculated)
	 */
	static async getAvailableStudents(
		params: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: 'ASC' | 'DESC';
		} = {}
	): Promise<DTORespuestaPaginadaDTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnosDisponibles(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getAvailableStudents');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get student by ID
	 */
	static async getAlumnoById(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getAlumnoById(${id})`);
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

	/**
	 * Change enrollment status
	 */
	static async changeEnrollmentStatus(id: number, matriculado: boolean): Promise<DTOAlumno> {
		try {
			return await alumnoApi.cambiarEstadoMatricula({
				id,
				requestBody: { matriculado }
			});
		} catch (error) {
			ErrorHandler.logError(error, `changeEnrollmentStatus(${id}, ${matriculado})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Enable/disable student
	 */
	static async toggleEnabled(id: number, enabled: boolean): Promise<DTOAlumno> {
		try {
			return await alumnoApi.habilitarDeshabilitarAlumno({
				id,
				requestBody: { enabled }
			});
		} catch (error) {
			ErrorHandler.logError(error, `toggleEnabled(${id}, ${enabled})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== STUDENT PROFILE OPERATIONS ====================

	/**
	 * Get my profile (for authenticated student)
	 */
	static async getMiPerfil(): Promise<DTOPerfilAlumno> {
		try {
			return await alumnoApi.obtenerMiPerfil();
		} catch (error) {
			ErrorHandler.logError(error, 'getMiPerfil');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get enrolled classes for a student with details
	 */
	static async getClasesInscritasConDetalles(alumnoId: number): Promise<DTOClaseInscrita[]> {
		try {
			return await alumnoApi.obtenerClasesInscritasConDetalles({ alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, `getClasesInscritasConDetalles(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	}
}
