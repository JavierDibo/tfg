import type {
	DTOClase,
	DTOEstadoInscripcion,
	DTORespuestaEnrollment,
	DTOPeticionEnrollment
} from '$lib/generated/api';
import { enrollmentApi, userOperationsApi, alumnoApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class EnrollmentService {
	/**
	 * Check if the current student is enrolled in a specific class
	 */
	static async checkMyEnrollmentStatus(claseId: number): Promise<DTOEstadoInscripcion> {
		try {
			return await enrollmentApi.verificarMiEstadoInscripcion({ claseId });
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
			return await enrollmentApi.verificarEstadoInscripcion({ claseId, alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, 'checkStudentEnrollmentStatus');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Enroll the current student in a class
	 */
	static async enrollInClass(claseId: number): Promise<DTOClase> {
		try {
			return await enrollmentApi.inscribirseEnClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, 'enrollInClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Unenroll the current student from a class
	 */
	static async unenrollFromClass(claseId: number): Promise<DTOClase> {
		try {
			return await enrollmentApi.darseDeBajaDeClase({ claseId });
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
			const enrollmentRequest: DTOPeticionEnrollment = {
				alumnoId,
				claseId
			};
			return await enrollmentApi.inscribirAlumnoEnClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
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
			const enrollmentRequest: DTOPeticionEnrollment = {
				alumnoId,
				claseId
			};
			return await enrollmentApi.darDeBajaAlumnoDeClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
		} catch (error) {
			ErrorHandler.logError(error, 'unenrollStudentFromClass');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get enrolled classes for the current student
	 * Uses the dedicated API endpoint for efficiency
	 */
	static async getMyEnrolledClasses(): Promise<DTOClase[]> {
		try {
			// Use the dedicated endpoint for enrolled classes
			const enrolledClasses = await userOperationsApi.obtenerMisClasesInscritas();
			// Convert DTOClaseInscrita to DTOClase if needed
			return enrolledClasses.map(claseInscrita => ({
				id: claseInscrita.id,
				titulo: claseInscrita.titulo,
				descripcion: claseInscrita.descripcion,
				precio: claseInscrita.precio,
				nivel: claseInscrita.nivel,
				presencialidad: claseInscrita.presencialidad,
				tipoClase: claseInscrita.tipoClase,
				imagenPortada: claseInscrita.imagenPortada,
				material: claseInscrita.material,
				numeroMateriales: claseInscrita.numeroMateriales,
				numeroEjercicios: claseInscrita.numeroEjercicios,
				// Map other fields as needed
			})) as DTOClase[];
		} catch (error) {
			ErrorHandler.logError(error, 'getMyEnrolledClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get enrolled classes for a specific student (for admins/professors)
	 * Uses the dedicated API endpoint for efficiency
	 */
	static async getStudentEnrolledClasses(alumnoId: number): Promise<DTOClase[]> {
		try {
			// Use the dedicated endpoint for admin to get student's enrolled classes
			const enrolledClasses = await alumnoApi.obtenerClasesInscritasConDetalles({ alumnoId });
			// Convert DTOClaseInscrita to DTOClase if needed
			return enrolledClasses.map(claseInscrita => ({
				id: claseInscrita.id,
				titulo: claseInscrita.titulo,
				descripcion: claseInscrita.descripcion,
				precio: claseInscrita.precio,
				nivel: claseInscrita.nivel,
				presencialidad: claseInscrita.presencialidad,
				tipoClase: claseInscrita.tipoClase,
				imagenPortada: claseInscrita.imagenPortada,
				material: claseInscrita.material,
				numeroMateriales: claseInscrita.numeroMateriales,
				numeroEjercicios: claseInscrita.numeroEjercicios,
				// Map other fields as needed
			})) as DTOClase[];
		} catch (error) {
			ErrorHandler.logError(error, 'getStudentEnrolledClases');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Toggle enrollment status for the current student
	 * Returns the new enrollment status
	 */
	static async toggleEnrollment(claseId: number): Promise<{ isEnrolled: boolean; clase: DTOClase }> {
		try {
			// Check current enrollment status
			const currentStatus = await this.checkMyEnrollmentStatus(claseId);
			
			if (currentStatus.isEnrolled) {
				// Unenroll
				const clase = await this.unenrollFromClass(claseId);
				return { isEnrolled: false, clase };
			} else {
				// Enroll
				const clase = await this.enrollInClass(claseId);
				return { isEnrolled: true, clase };
			}
		} catch (error) {
			ErrorHandler.logError(error, 'toggleEnrollment');
			throw await ErrorHandler.parseError(error);
		}
	}
}
