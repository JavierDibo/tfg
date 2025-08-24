import type {
	DTOEstadoInscripcion,
	DTORespuestaEnrollment,
	DTOClaseConDetalles
} from '$lib/generated/api';
import { classManagementApi, userOperationsApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class EnrollmentService {
	/**
	 * Check if the current student is enrolled in a specific class
	 */
	static async checkMyEnrollmentStatus(claseId: number): Promise<DTOEstadoInscripcion> {
		try {
			return await classManagementApi.obtenerMiInscripcion({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'checkMyEnrollmentStatus');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Check if a specific student is enrolled in a class (for admins/professors)
	 */
	static async checkStudentEnrollmentStatus(
		claseId: number,
		alumnoId: number
	): Promise<DTOEstadoInscripcion> {
		try {
			return await classManagementApi.obtenerInscripcionEstudiante({
				claseId,
				studentId: alumnoId
			});
		} catch (error) {
			ErrorHandler.logError(error, 'checkStudentEnrollmentStatus');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Enroll the current student in a class
	 */
	static async enrollInClass(claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			return await classManagementApi.inscribirseEnClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'enrollInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Unenroll the current student from a class
	 */
	static async unenrollFromClass(claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			return await classManagementApi.darseDeBajaDeClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'unenrollFromClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Enroll a student in a class (for admins/professors)
	 */
	static async enrollStudentInClass(
		alumnoId: number,
		claseId: number
	): Promise<DTORespuestaEnrollment> {
		try {
			return await classManagementApi.inscribirAlumnoEnClase({ claseId, studentId: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, 'enrollStudentInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Unenroll a student from a class (for admins/professors)
	 */
	static async unenrollStudentFromClass(
		alumnoId: number,
		claseId: number
	): Promise<DTORespuestaEnrollment> {
		try {
			return await classManagementApi.darDeBajaAlumnoDeClase({ claseId, studentId: alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, 'unenrollStudentFromClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get class details for the current student
	 */
	static async getMyClassDetails(claseId: number): Promise<DTOClaseConDetalles> {
		try {
			return await classManagementApi.obtenerMisDetallesClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'getMyClassDetails');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get class details for a specific student (for admins/professors)
	 */
	static async getStudentClassDetails(
		claseId: number,
		alumnoId: number
	): Promise<DTOClaseConDetalles> {
		try {
			return await classManagementApi.obtenerDetallesClaseParaEstudiante({
				claseId,
				studentId: alumnoId
			});
		} catch (error) {
			ErrorHandler.logError(error, 'getStudentClassDetails');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all students enrolled in a class
	 */
	static async getStudentsInClass(
		claseId: number,
		page: number = 0,
		size: number = 10,
		sortBy?: string,
		sortDirection?: string
	) {
		try {
			return await userOperationsApi.obtenerAlumnosDeClase({
				claseId,
				page,
				size,
				sortBy,
				sortDirection
			});
		} catch (error) {
			ErrorHandler.logError(error, 'getStudentsInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get public information of students in a class
	 */
	static async getPublicStudentsInClass(claseId: number) {
		try {
			return await userOperationsApi.obtenerAlumnosPublicosDeClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'getPublicStudentsInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all classes where the current user is enrolled
	 */
	static async getMyEnrolledClasses() {
		try {
			return await userOperationsApi.obtenerMisClasesInscritas();
		} catch (error) {
			ErrorHandler.logError(error, 'getMyEnrolledClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all classes where the current user is teaching (for professors)
	 */
	static async getMyTeachingClasses() {
		try {
			return await userOperationsApi.obtenerMisClases();
		} catch (error) {
			ErrorHandler.logError(error, 'getMyTeachingClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all classes (for admins) - use the classes API instead
	 */
	static async getAllClasses() {
		try {
			const { ClaseService } = await import('./claseService');
			return await ClaseService.getClases();
		} catch (error) {
			ErrorHandler.logError(error, 'getAllClasses');
			throw await ErrorHandler.parseError(error);
		}
	}
}
