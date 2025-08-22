import type {
	DTOAlumno,
	DTOPeticionRegistroAlumno,
	DTOActualizacionAlumno,
	DTORespuestaPaginadaDTOAlumno
} from '$lib/generated/api';
import { alumnoApi } from '$lib/api';
import type { PaginatedAlumnosResponse } from '$lib/types/pagination';

// Search parameters interface
export interface AlumnoSearchParams {
	// Campos de texto
	nombre?: string;
	apellidos?: string;
	usuario?: string;
	dni?: string;
	email?: string;
	numeroTelefono?: string;

	// Estados booleanos
	matriculado?: boolean;
	enabled?: boolean;

	// Filtros de fecha
	fechaInscripcionDesde?: Date;
	fechaInscripcionHasta?: Date;

	// Búsqueda general
	busquedaGeneral?: string;
}

// Statistics interfaces
export interface AlumnoStatistics {
	total: number;
	matriculados: number;
	noMatriculados: number;
}

export class AlumnoService {
	// ==================== PAGINATED OPERATIONS ====================

	/**
	 * Get students with pagination and filters (unified method)
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
			// Use the pre-configured API (handles auth automatically)
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

	// ==================== CRUD Operations ====================

	/**
	 * Create a new student (Admin only)
	 */
	static async createAlumno(alumnoData: DTOPeticionRegistroAlumno): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.crearAlumno({
				dTOPeticionRegistroAlumno: alumnoData
			});
			return response;
		} catch (error) {
			console.error('Error creating alumno:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get all students with optional filters (Admin/Professor)
	 */
	static async getAlumnos(filters: AlumnoSearchParams = {}): Promise<DTOAlumno[]> {
		try {
			const response = await alumnoApi.obtenerAlumnos(filters);
			return response;
		} catch (error) {
			console.error('Error fetching alumnos:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get all filtered students for CSV export (uses same filters as pagination)
	 */
	static async getAllFilteredAlumnos(filters: {
		nombre?: string;
		apellidos?: string;
		dni?: string;
		email?: string;
		matriculado?: boolean;
	}): Promise<DTOAlumno[]> {
		try {
			// Use the obtenerAlumnos API which returns all students matching the filters
			const response = await alumnoApi.obtenerAlumnos(filters);
			return response;
		} catch (error) {
			console.error('Error fetching filtered alumnos for export:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get student by ID
	 */
	static async getAlumnoById(id: number): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.obtenerAlumnoPorId({ id });
			return response;
		} catch (error) {
			console.error('Error fetching alumno by ID:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get student by username
	 */
	static async getAlumnoByUsuario(usuario: string): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.obtenerAlumnoPorUsuario({ usuario });
			return response;
		} catch (error) {
			console.error('Error fetching alumno by usuario:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get student by email (Admin only)
	 */
	static async getAlumnoByEmail(email: string): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.obtenerAlumnoPorEmail({ email });
			return response;
		} catch (error) {
			console.error('Error fetching alumno by email:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get student by DNI (Admin only)
	 */
	static async getAlumnoByDni(dni: string): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.obtenerAlumnoPorDni({ dni });
			return response;
		} catch (error) {
			console.error('Error fetching alumno by DNI:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Update student profile
	 */
	static async updateAlumno(id: number, alumnoData: DTOActualizacionAlumno): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.actualizarAlumno({
				id,
				dTOActualizacionAlumno: alumnoData
			});
			return response;
		} catch (error) {
			console.error('Error updating alumno:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Delete student (Admin only)
	 */
	static async deleteAlumno(id: number): Promise<void> {
		try {
			await alumnoApi.borrarAlumnoPorId({ id });
		} catch (error) {
			console.error('Error deleting alumno:', error);
			throw this.handleApiError(error);
		}
	}

	// ==================== Status Management ====================

	/**
	 * Get enrolled students (Admin/Professor)
	 */
	static async getMatriculados(): Promise<DTOAlumno[]> {
		try {
			const response = await alumnoApi.obtenerAlumnosMatriculados();
			return response;
		} catch (error) {
			console.error('Error fetching matriculados:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get non-enrolled students (Admin only)
	 */
	static async getNoMatriculados(): Promise<DTOAlumno[]> {
		try {
			const response = await alumnoApi.obtenerAlumnosNoMatriculados();
			return response;
		} catch (error) {
			console.error('Error fetching no matriculados:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Change enrollment status (Admin only)
	 */
	static async changeEnrollmentStatus(id: number, matriculado: boolean): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.cambiarEstadoMatricula({
				id,
				requestBody: { matriculado }
			});
			return response;
		} catch (error) {
			console.error('Error changing enrollment status:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Enable/disable student account (Admin only)
	 */
	static async toggleAccountStatus(id: number, enabled: boolean): Promise<DTOAlumno> {
		try {
			const response = await alumnoApi.habilitarDeshabilitarAlumno({
				id,
				requestBody: { habilitar: enabled }
			});
			return response;
		} catch (error) {
			console.error('Error toggling account status:', error);
			throw this.handleApiError(error);
		}
	}

	// ==================== Statistics ====================

	/**
	 * Get total student count
	 */
	static async getTotalAlumnos(): Promise<number> {
		try {
			const response = await alumnoApi.obtenerTotalAlumnos();
			// Backend returns {"total": 123}
			if (typeof response === 'object' && response !== null) {
				return (response as any).total || 0;
			}
			return typeof response === 'number' ? response : 0;
		} catch (error) {
			console.error('Error fetching total alumnos:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get enrollment statistics
	 */
	static async getEstadisticasMatriculas(): Promise<{
		matriculados: number;
		noMatriculados: number;
	}> {
		try {
			const response = await alumnoApi.obtenerEstadisticasMatriculas();
			// Backend returns {"matriculados": 50, "no_matriculados": 30}
			if (typeof response === 'object' && response !== null) {
				const responseObj = response as any;
				return {
					matriculados: responseObj.matriculados || 0,
					noMatriculados: responseObj.no_matriculados || responseObj.noMatriculados || 0
				};
			}
			// Fallback for unexpected response format
			return { matriculados: 0, noMatriculados: 0 };
		} catch (error) {
			console.error('Error fetching matriculas statistics:', error);
			throw this.handleApiError(error);
		}
	}

	/**
	 * Get comprehensive statistics
	 */
	static async getStatistics(): Promise<AlumnoStatistics> {
		try {
			const [total, matriculas] = await Promise.all([
				this.getTotalAlumnos(),
				this.getEstadisticasMatriculas()
			]);

			return {
				total,
				matriculados: matriculas.matriculados,
				noMatriculados: matriculas.noMatriculados
			};
		} catch (error) {
			console.error('Error fetching statistics:', error);
			throw this.handleApiError(error);
		}
	}

	// ==================== Validation Helpers ====================

	/**
	 * Validate student registration data
	 */
	static validateRegistrationData(data: DTOPeticionRegistroAlumno): string[] {
		const errors: string[] = [];

		// Username validation
		if (!data.usuario || data.usuario.length < 3 || data.usuario.length > 50) {
			errors.push('El usuario debe tener entre 3 y 50 caracteres');
		}

		// Password validation
		if (!data.contraseña || data.contraseña.length < 6) {
			errors.push('La contraseña debe tener al menos 6 caracteres');
		}

		// Name validation
		if (!data.nombre || data.nombre.length > 100) {
			errors.push('El nombre es obligatorio y no puede superar 100 caracteres');
		}

		// Last name validation
		if (!data.apellidos || data.apellidos.length > 100) {
			errors.push('Los apellidos son obligatorios y no pueden superar 100 caracteres');
		}

		// DNI validation
		if (!data.dni || data.dni.length > 20) {
			errors.push('El DNI es obligatorio y no puede superar 20 caracteres');
		}

		// Email validation
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!data.email || !emailRegex.test(data.email)) {
			errors.push('El email debe tener un formato válido');
		}

		// Phone validation (optional)
		if (data.telefono && data.telefono.length > 15) {
			errors.push('El número de teléfono no puede superar 15 caracteres');
		}

		// Address validation (optional)
		if (data.direccion && data.direccion.length > 200) {
			errors.push('La dirección no puede superar 200 caracteres');
		}

		return errors;
	}

	// ==================== Error Handling ====================

	/**
	 * Handle API errors and return user-friendly messages
	 */
	private static handleApiError(error: any): Error {
		if (error?.response) {
			// HTTP error with response
			const status = error.response.status;
			const data = error.response.data;

			switch (status) {
				case 400:
					return new Error(data?.message || 'Datos inválidos');
				case 401:
					return new Error('No autorizado. Por favor, inicia sesión');
				case 403:
					return new Error('No tienes permisos para realizar esta acción');
				case 404:
					return new Error('Alumno no encontrado');
				case 409:
					return new Error('El usuario, email o DNI ya existe');
				case 500:
					return new Error('Error interno del servidor');
				default:
					return new Error(data?.message || `Error: ${status}`);
			}
		} else if (error?.message) {
			// Network or other errors
			return new Error(error.message);
		} else {
			// Unknown error
			return new Error('Error desconocido');
		}
	}
}
