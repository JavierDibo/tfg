import type {
	DTOAlumno,
	DTOPeticionRegistroAlumno,
	DTOActualizacionAlumno,
	DTORespuestaPaginadaDTOAlumno,
	DTOPerfilAlumno,
	DTOClaseInscrita
} from '$lib/generated/api';
import { alumnoApi } from '$lib/api';

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
			console.error('Error fetching paginated alumnos:', error);
			throw this.handleApiError(error);
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
			console.error('Error fetching available students:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get student by ID
	 */
	static async getAlumnoById(id: number): Promise<DTOAlumno> {
		try {
			return await alumnoApi.obtenerAlumnoPorId({ id });
		} catch (error) {
			console.error('Error fetching alumno by ID:', error);
			throw this.handleApiError(error);
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
			console.error('Error creating alumno:', error);
			throw this.handleApiError(error);
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
			console.error('Error updating alumno:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Delete a student
	 */
	static async deleteAlumno(id: number): Promise<void> {
		try {
			await alumnoApi.borrarAlumnoPorId({ id });
		} catch (error) {
			console.error('Error deleting alumno:', error);
			throw this.handleApiError(error);
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
			console.error('Error changing enrollment status:', error);
			throw this.handleApiError(error);
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
			console.error('Error toggling student enabled status:', error);
			throw this.handleApiError(error);
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
			console.error('Error fetching my profile:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get enrolled classes for a student with details
	 */
	static async getClasesInscritasConDetalles(alumnoId: number): Promise<DTOClaseInscrita[]> {
		try {
			return await alumnoApi.obtenerClasesInscritasConDetalles({ alumnoId });
		} catch (error) {
			console.error('Error fetching enrolled classes with details:', error);
			throw this.handleApiError(error);
		}
	}

	// ==================== UTILITY METHODS ====================

	/**
	 * Handle API errors consistently
	 */
	private static handleApiError(error: unknown): Error {
		if (error && typeof error === 'object' && 'response' in error) {
			const response = (error as { response: { data?: { message?: string } } }).response;
			if (response?.data?.message) {
				return new Error(response.data.message);
			}
		}
		if (error && typeof error === 'object' && 'message' in error) {
			return new Error((error as { message: string }).message);
		}
		return new Error('An unexpected error occurred');
	}
}
