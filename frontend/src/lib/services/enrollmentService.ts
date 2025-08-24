import type {
	DTOClase,
	DTOEstadoInscripcion,
	DTORespuestaEnrollment,
	DTOPeticionEnrollment
} from '$lib/generated/api';
import { enrollmentApi, userOperationsApi, alumnoApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { authStore } from '$lib/stores/authStore.svelte';

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
	static async enrollInClass(claseId: number): Promise<DTORespuestaEnrollment> {
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
	static async unenrollFromClass(claseId: number): Promise<DTORespuestaEnrollment> {
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
				studentId: alumnoId,
				classId: claseId
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
				studentId: alumnoId,
				classId: claseId
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
			return enrolledClasses.map((claseInscrita: any) => ({
				id: claseInscrita.id,
				titulo: claseInscrita.titulo,
				descripcion: claseInscrita.descripcion,
				precio: claseInscrita.precio,
				nivel: claseInscrita.nivel,
				presencialidad: claseInscrita.presencialidad,
				tipoClase: claseInscrita.tipoClase,
				imagenPortada: claseInscrita.imagenPortada,
				material: claseInscrita.material,
				numeroMateriales: claseInscrita.material?.length || 0,
				numeroEjercicios: claseInscrita.ejerciciosId?.length || 0,
				numeroProfesores: claseInscrita.numeroProfesores,
				numeroAlumnos: claseInscrita.numeroAlumnos
				// Map other fields as needed
			})) as DTOClase[];
		} catch (error) {
			ErrorHandler.logError(error, 'getMyEnrolledClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get enrolled classes for a specific student (for admins/professors)
	 * Uses the generated API with proper array handling
	 */
	static async getStudentEnrolledClasses(alumnoId: number): Promise<DTOClase[]> {
		try {
			// Use the generated API - it should now work correctly with the backend fixes
			const response = await alumnoApi.obtenerClasesInscritas({ id: alumnoId });
			
			console.log('Raw response from obtenerClasesInscritas:', response);
			
			// Handle the response as an array (which is what the backend returns)
			const enrolledClasses = Array.isArray(response) ? response : [response];
			console.log('Processed enrolledClasses:', enrolledClasses);
			
			// Convert DTOClaseInscrita to DTOClase
			return enrolledClasses
				.filter((clase) => clase && clase.id) // Filter out null/undefined entries
				.map((claseInscrita: any) => ({
					id: claseInscrita.id,
					titulo: claseInscrita.titulo,
					descripcion: claseInscrita.descripcion,
					precio: claseInscrita.precio,
					nivel: claseInscrita.nivel,
					presencialidad: claseInscrita.presencialidad,
					tipoClase: claseInscrita.tipoClase,
					imagenPortada: claseInscrita.imagenPortada,
					material: claseInscrita.material,
					numeroMateriales: claseInscrita.material?.length || 0,
					numeroEjercicios: claseInscrita.ejerciciosId?.length || 0,
					numeroProfesores: claseInscrita.numeroProfesores,
					numeroAlumnos: claseInscrita.numeroAlumnos
					// Map other fields as needed
				})) as DTOClase[];
		} catch (error) {
			ErrorHandler.logError(error, 'getStudentEnrolledClasses');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Toggle enrollment status for the current student
	 * Returns the new enrollment status
	 */
	static async toggleEnrollment(
		claseId: number
	): Promise<{ isEnrolled: boolean; response: DTORespuestaEnrollment }> {
		try {
			// Check current enrollment status
			const currentStatus = await this.checkMyEnrollmentStatus(claseId);

			if (currentStatus.isEnrolled) {
				// Unenroll
				const response = await this.unenrollFromClass(claseId);
				return { isEnrolled: false, response };
			} else {
				// Enroll
				const response = await this.enrollInClass(claseId);
				return { isEnrolled: true, response };
			}
		} catch (error) {
			ErrorHandler.logError(error, 'toggleEnrollment');
			throw await ErrorHandler.parseError(error);
		}
	}
}
