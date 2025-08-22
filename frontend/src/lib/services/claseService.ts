import type {
	DTOClase,
	DTOClaseConDetalles,
	DTOClaseInscrita,
	DTORespuestaEnrollment,
	DTORespuestaAlumnosClase,
	DTOPeticionEnrollment,
	Material
} from '$lib/generated/api';
import { claseApi } from '$lib/api';

export const ClaseService = {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Get all classes
	 */
	async getAllClases(): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClases();
		} catch (error) {
			console.error('Error fetching all classes:', error);
			throw error;
		}
	},

	/**
	 * Get class by ID
	 */
	async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			console.error(`Error fetching class ${id}:`, error);
			throw error;
		}
	},

	/**
	 * Get class details for a specific student
	 */
	async getClaseDetailsForStudent(claseId: number, alumnoId: number): Promise<DTOClaseConDetalles> {
		try {
			return await claseApi.obtenerClaseConDetallesParaEstudiante({
				claseId,
				alumnoId
			});
		} catch (error) {
			console.error(
				`Error fetching class details for student ${alumnoId} in class ${claseId}:`,
				error
			);
			throw error;
		}
	},

	/**
	 * Get class details for the authenticated student
	 */
	async getClaseDetailsForMe(claseId: number): Promise<DTOClaseConDetalles> {
		try {
			return await claseApi.obtenerClaseConDetallesParaMi({ claseId });
		} catch (error) {
			console.error(`Error fetching class details for me in class ${claseId}:`, error);
			throw error;
		}
	},

	/**
	 * Get my enrolled classes (for authenticated student)
	 */
	async getMisClasesInscritas(): Promise<DTOClaseInscrita[]> {
		try {
			return await claseApi.obtenerMisClasesInscritas();
		} catch (error) {
			console.error('Error fetching my enrolled classes:', error);
			throw error;
		}
	},

	/**
	 * Get my classes (for authenticated teacher)
	 */
	async getMisClases(): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerMisClases();
		} catch (error) {
			console.error('Error fetching my classes:', error);
			throw error;
		}
	},

	/**
	 * Get classes by student ID
	 */
	async getClasesByAlumno(alumnoId: string): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClasesPorAlumno({ alumnoId });
		} catch (error) {
			console.error(`Error fetching classes for student ${alumnoId}:`, error);
			throw error;
		}
	},

	/**
	 * Search classes by title
	 */
	async searchClasesByTitle(titulo: string): Promise<DTOClase[]> {
		try {
			return await claseApi.buscarClasesPorTitulo({ titulo });
		} catch (error) {
			console.error(`Error searching classes by title "${titulo}":`, error);
			throw error;
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
			return await claseApi.inscribirAlumnoEnClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
		} catch (error) {
			console.error(`Error enrolling student ${alumnoId} in class ${claseId}:`, error);
			throw error;
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
			return await claseApi.darDeBajaAlumnoDeClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
		} catch (error) {
			console.error(`Error unenrolling student ${alumnoId} from class ${claseId}:`, error);
			throw error;
		}
	},

	/**
	 * Self-enrollment (Student enrolls themselves)
	 */
	async enrollInClase(claseId: number): Promise<DTOClase> {
		try {
			return await claseApi.inscribirseEnClase({ claseId });
		} catch (error) {
			console.error(`Error self-enrolling in class ${claseId}:`, error);
			throw error;
		}
	},

	/**
	 * Self-unenrollment (Student unenrolls themselves)
	 */
	async unenrollFromClase(claseId: number): Promise<DTOClase> {
		try {
			return await claseApi.darseDeBajaDeClase({ claseId });
		} catch (error) {
			console.error(`Error self-unenrolling from class ${claseId}:`, error);
			throw error;
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
			return await claseApi.obtenerAlumnosDeClase({
				claseId,
				...params
			});
		} catch (error) {
			console.error(`Error fetching students for class ${claseId}:`, error);
			throw error;
		}
	},

	/**
	 * Count students in a class
	 */
	async contarAlumnosEnClase(claseId: number): Promise<number> {
		try {
			return await claseApi.contarAlumnosEnClase({ claseId });
		} catch (error) {
			console.error(`Error counting students for class ${claseId}:`, error);
			throw error;
		}
	},

	// ==================== MATERIAL MANAGEMENT ====================

	/**
	 * Add material to a class
	 */
	async addMaterialToClase(claseId: number, material: Material): Promise<DTOClase> {
		try {
			return await claseApi.agregarMaterial({ claseId, material });
		} catch (error) {
			console.error(`Error adding material to class ${claseId}:`, error);
			throw error;
		}
	},

	/**
	 * Remove material from a class
	 */
	async removeMaterialFromClase(claseId: number, materialId: string): Promise<DTOClase> {
		try {
			return await claseApi.removerMaterial({ claseId, materialId });
		} catch (error) {
			console.error(`Error removing material ${materialId} from class ${claseId}:`, error);
			throw error;
		}
	}
};
