import type {
	DTOClase,
	DTOClaseConDetalles,
	DTOClaseInscrita,
	DTORespuestaEnrollment,
	DTORespuestaAlumnosClase,
	DTOPeticionEnrollment,
	Material,
	DTOEstadoInscripcion
} from '$lib/generated/api';
import { claseApi, enrollmentApi, userOperationsApi, claseManagementApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export const ClaseService = {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Get all classes
	 */
	async getAllClases(): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClases();
		} catch (error) {
			ErrorHandler.logError(error, 'getAllClases');
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get class by ID
	 */
	async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getClaseById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get class details for a specific student
	 */
	async getClaseDetailsForStudent(claseId: number, alumnoId: number): Promise<DTOClaseConDetalles> {
		try {
			return await enrollmentApi.obtenerClaseConDetallesParaEstudiante({
				claseId,
				alumnoId
			});
		} catch (error) {
			ErrorHandler.logError(error, `getClaseDetailsForStudent(${claseId}, ${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get class details for the authenticated student
	 */
	async getClaseDetailsForMe(claseId: number): Promise<DTOClaseConDetalles> {
		try {
			return await enrollmentApi.obtenerClaseConDetallesParaMi({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, `getClaseDetailsForMe(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get my enrolled classes (for authenticated student)
	 */
	async getMisClasesInscritas(): Promise<DTOClaseInscrita[]> {
		try {
			return await userOperationsApi.obtenerMisClasesInscritas();
		} catch (error) {
			ErrorHandler.logError(error, 'getMisClasesInscritas');
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get my classes (for authenticated teacher)
	 */
	async getMisClases(): Promise<DTOClase[]> {
		try {
			return await userOperationsApi.obtenerMisClases();
		} catch (error) {
			ErrorHandler.logError(error, 'getMisClases');
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Get classes by student ID
	 */
	async getClasesByAlumno(alumnoId: string): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClasesPorAlumno({ alumnoId });
		} catch (error) {
			ErrorHandler.logError(error, `getClasesByAlumno(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Search classes by title
	 */
	async searchClasesByTitle(titulo: string): Promise<DTOClase[]> {
		try {
			return await claseApi.buscarClasesPorTitulo({ titulo });
		} catch (error) {
			ErrorHandler.logError(error, `searchClasesByTitle("${titulo}")`);
			throw await ErrorHandler.parseError(error);
		}
	},

	// ==================== ENROLLMENT MANAGEMENT ====================

	/**
	 * Enroll a student in a class (Admin or Professor)
	 */
	async enrollStudentInClass(alumnoId: number, claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			const enrollmentRequest: DTOPeticionEnrollment = {
				alumnoId,
				claseId
			};
			return await enrollmentApi.inscribirAlumnoEnClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
		} catch (error) {
			ErrorHandler.logError(error, `enrollStudentInClass(${alumnoId}, ${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Unenroll a student from a class (Admin or Professor)
	 */
	async unenrollStudentFromClass(
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
			ErrorHandler.logError(error, `unenrollStudentFromClass(${alumnoId}, ${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Self-enrollment (Student enrolls themselves)
	 */
	async enrollInClase(claseId: number): Promise<DTOClase> {
		try {
			return await enrollmentApi.inscribirseEnClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, `enrollInClase(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Self-unenrollment (Student unenrolls themselves)
	 */
	async unenrollFromClase(claseId: number): Promise<DTOClase> {
		try {
			console.log('ClaseService.unenrollFromClase called with claseId:', claseId);
			const result = await enrollmentApi.darseDeBajaDeClase({ claseId });
			console.log('ClaseService.unenrollFromClase result:', result);
			return result;
		} catch (error) {
			console.error('ClaseService.unenrollFromClase error:', error);
			ErrorHandler.logError(error, `unenrollFromClase(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Check enrollment status for the authenticated student
	 */
	async checkMyEnrollmentStatus(claseId: number): Promise<DTOEstadoInscripcion> {
		try {
			return await enrollmentApi.verificarMiEstadoInscripcion({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, `checkMyEnrollmentStatus(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	// ==================== STUDENT MANAGEMENT ====================

	/**
	 * Get students in a class with pagination
	 */
	async getAlumnosDeClase(
		claseId: number,
		params: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: 'ASC' | 'DESC';
		} = {}
	): Promise<DTORespuestaAlumnosClase> {
		try {
			return await userOperationsApi.obtenerAlumnosDeClase({
				claseId,
				...params
			});
		} catch (error) {
			ErrorHandler.logError(error, `getAlumnosDeClase(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Count students in a class
	 */
	async contarAlumnosEnClase(claseId: number): Promise<number> {
		try {
			return await claseApi.contarAlumnosEnClase({ claseId });
		} catch (error) {
			ErrorHandler.logError(error, `contarAlumnosEnClase(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	// ==================== MATERIAL MANAGEMENT ====================

	/**
	 * Add material to a class
	 */
	async addMaterialToClase(claseId: number, material: Material): Promise<DTOClase> {
		try {
			return await claseManagementApi.agregarMaterial({ claseId, material });
		} catch (error) {
			ErrorHandler.logError(error, `addMaterialToClase(${claseId})`);
			throw await ErrorHandler.parseError(error);
		}
	},

	/**
	 * Remove material from a class
	 */
	async removeMaterialFromClase(claseId: number, materialId: string): Promise<DTOClase> {
		try {
			return await claseManagementApi.removerMaterial({ claseId, materialId });
		} catch (error) {
			ErrorHandler.logError(error, `removeMaterialFromClase(${claseId}, ${materialId})`);
			throw await ErrorHandler.parseError(error);
		}
	}
};
