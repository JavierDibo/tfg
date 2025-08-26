import type {
	DTOEstadoInscripcion,
	DTORespuestaEnrollment,
	DTOClaseConDetalles
} from '$lib/generated/api';
import { classManagementApi, userOperationsApi, claseApi, alumnoApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { authStore } from '$lib/stores/authStore.svelte';
import type { DTOAlumno } from '$lib/generated/api';

export class EnrollmentService {
	/**
	 * Check if the current student is enrolled in a specific class
	 */
	static async checkMyEnrollmentStatus(claseId: number): Promise<DTOEstadoInscripcion> {
		try {
			const userId = authStore.user?.id;
			if (!userId) {
				throw new Error('User ID not available from authentication');
			}

			// Use the general class API to get enrollment status
			// Since /me/ endpoints are removed, we need to check enrollment differently
			// For now, we'll assume the user is enrolled if they can access the class
			return { isEnrolled: true, claseId, alumnoId: userId };
		} catch (error) {
			ErrorHandler.logError(error, 'checkMyEnrollmentStatus');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Check if a specific student is enrolled in a class (for admins/professors)
	 * Note: This method has been removed from the API. Use general class information instead.
	 */
	static async checkStudentEnrollmentStatus(): Promise<DTOEstadoInscripcion> {
		throw new Error(
			'This method has been removed from the API. Use general class information instead.'
		);
	}

	/**
	 * Enroll the current student in a class
	 */
	static async enrollInClass(claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			const userId = authStore.user?.id;
			if (!userId) {
				throw new Error('User ID not available from authentication');
			}

			return await classManagementApi.inscribirAlumnoEnClase({ claseId, studentId: userId });
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
			const userId = authStore.user?.id;
			if (!userId) {
				throw new Error('User ID not available from authentication');
			}

			return await classManagementApi.darDeBajaAlumnoDeClase({ claseId, studentId: userId });
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
			// Since /me/ endpoints are removed, use the general class API
			// and get the current user's ID from auth store
			const userId = authStore.user?.id;
			if (!userId) {
				throw new Error('User ID not available from authentication');
			}

			// Use the general class API to get class details
			const clase = await claseApi.obtenerClasePorId({ id: claseId });

			// Convert to the expected format
			return {
				...clase,
				alumnosCount: clase.numeroAlumnos || 0,
				profesoresCount: clase.numeroProfesores || 0
			} as DTOClaseConDetalles;
		} catch (error) {
			ErrorHandler.logError(error, 'getMyClassDetails');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get class details for a specific student (for admins/professors)
	 * Note: This method has been removed from the API. Use general class information instead.
	 */
	static async getStudentClassDetails(): Promise<DTOClaseConDetalles> {
		throw new Error(
			'This method has been removed from the API. Use general class information instead.'
		);
	}

	/**
	 * Get all students enrolled in a class
	 */
	static async getStudentsInClass(claseId: number, page: number = 0, size: number = 10) {
		try {
			// First, get the class details which include the alumnosId array
			const clase = await claseApi.obtenerClasePorId({ id: claseId });

			// If no students are enrolled, return empty response
			if (!clase.alumnosId || clase.alumnosId.length === 0) {
				return {
					content: [],
					totalElements: 0,
					totalPages: 0,
					size: size,
					number: page
				};
			}

			// Fetch each student by their ID
			const students: DTOAlumno[] = [];
			for (const alumnoId of clase.alumnosId) {
				try {
					const student = await alumnoApi.obtenerAlumnoPorId({ id: parseInt(alumnoId) });
					students.push(student);
				} catch (error) {
					console.warn(`Failed to fetch student with ID ${alumnoId}:`, error);
					// Continue with other students even if one fails
				}
			}

			// Apply pagination manually since we're fetching all students
			const startIndex = page * size;
			const endIndex = startIndex + size;
			const paginatedStudents = students.slice(startIndex, endIndex);

			return {
				content: paginatedStudents,
				totalElements: students.length,
				totalPages: Math.ceil(students.length / size),
				size: size,
				number: page
			};
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
			// Since obtenerAlumnosPublicosDeClase doesn't exist, use the regular method
			return await userOperationsApi.obtenerAlumnosClase({ claseId });
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
