import {
	type DTOClase,
	type DTOParametrosBusquedaClase,
	type DTORespuestaPaginadaDTOClase,
	type DTORespuestaPaginadaDTOAlumno,
	type Material,
	type DTOPeticionEnrollment,
	type DTORespuestaEnrollment
} from '$lib/generated/api';
import { claseApi } from '$lib/api';

// Import pagination types
import type { SortDirection } from '$lib/types/pagination';

export const ClaseService = {
	async getAllClases(): Promise<DTOClase[]> {
		try {
			const response = await claseApi.obtenerClases();
			return response;
		} catch (error) {
			console.error('Error fetching classes:', error);
			throw error;
		}
	},

	async getClaseById(id: number): Promise<DTOClase> {
		try {
			const response = await claseApi.obtenerClasePorId({ id });
			return response;
		} catch (error) {
			console.error(`Error fetching class with id ${id}:`, error);
			throw error;
		}
	},

	async getClasesPaginados(
		filters: {
			titulo?: string;
			descripcion?: string;
			profesorId?: string;
			alumnoId?: string;
		},
		pagination: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: SortDirection;
		}
	): Promise<DTORespuestaPaginadaDTOClase> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const searchParams: DTOParametrosBusquedaClase = {
				...filters,
				pagina: page || 0,
				tamanoPagina: size || 10,
				ordenCampo: sortBy || 'id',
				ordenDireccion: sortDirection || 'ASC'
			};

			const response = await claseApi.buscarClases({
				dTOParametrosBusquedaClase: searchParams
			});

			return response;
		} catch (error) {
			console.error('Error fetching paginated classes:', error);
			throw error;
		}
	},

	// Get classes by student (enrolled classes)
	async getClasesByAlumno(alumnoId: string): Promise<DTOClase[]> {
		try {
			const response = await claseApi.obtenerClasesPorAlumno({ alumnoId });
			return response;
		} catch (error) {
			console.error(`Error fetching classes for student ${alumnoId}:`, error);
			throw error;
		}
	},

	// Get classes by teacher (classes they teach)
	async getClasesByProfesor(profesorId: string): Promise<DTOClase[]> {
		try {
			const response = await claseApi.obtenerClasesPorProfesor({ profesorId });
			return response;
		} catch (error) {
			console.error(`Error fetching classes for teacher ${profesorId}:`, error);
			throw error;
		}
	},

	// Get teacher's classes using the new mis-clases endpoint
	async getMisClases(): Promise<DTOClase[]> {
		try {
			const response = await claseApi.obtenerMisClases();
			return response;
		} catch (error) {
			console.error('Error fetching my classes:', error);
			throw error;
		}
	},

	// Get students in a class with pagination
	async getAlumnosDeClase(
		claseId: number,
		pagination: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: SortDirection;
		} = {}
	): Promise<DTORespuestaPaginadaDTOAlumno> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const response = await claseApi.obtenerAlumnosDeClase({
				claseId,
				page: page || 0,
				size: size || 20,
				sortBy: sortBy || 'id',
				sortDirection: sortDirection || 'ASC'
			});
			return response;
		} catch (error) {
			console.error(`Error fetching students for class ${claseId}:`, error);
			throw error;
		}
	},

	// Count students in a class
	async contarAlumnosEnClase(claseId: number): Promise<number> {
		try {
			const response = await claseApi.contarAlumnosEnClase({ claseId });
			return response;
		} catch (error) {
			console.error(`Error counting students for class ${claseId}:`, error);
			throw error;
		}
	},

	// ==================== ENROLLMENT MANAGEMENT ====================

	// General enrollment (Admin or Professor who teaches the class)
	async enrollStudentInClass(alumnoId: number, claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			const enrollmentRequest: DTOPeticionEnrollment = {
				alumnoId,
				claseId
			};
			const response = await claseApi.inscribirAlumnoEnClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
			return response;
		} catch (error) {
			console.error(`Error enrolling student ${alumnoId} in class ${claseId}:`, error);
			throw error;
		}
	},

	// General unenrollment (Admin or Professor who teaches the class)
	async unenrollStudentFromClass(alumnoId: number, claseId: number): Promise<DTORespuestaEnrollment> {
		try {
			const enrollmentRequest: DTOPeticionEnrollment = {
				alumnoId,
				claseId
			};
			const response = await claseApi.darDeBajaAlumnoDeClase({
				dTOPeticionEnrollment: enrollmentRequest
			});
			return response;
		} catch (error) {
			console.error(`Error unenrolling student ${alumnoId} from class ${claseId}:`, error);
			throw error;
		}
	},

	// ==================== EXISTING METHODS ====================

	async addAlumnoToClase(claseId: number, alumnoId: string): Promise<void> {
		try {
			await claseApi.agregarAlumno({
				claseId,
				alumnoId
			});
		} catch (error) {
			console.error(`Error adding student ${alumnoId} to class ${claseId}:`, error);
			throw error;
		}
	},

	async removeAlumnoFromClase(claseId: number, alumnoId: string): Promise<void> {
		try {
			await claseApi.removerAlumno({
				claseId,
				alumnoId
			});
		} catch (error) {
			console.error(`Error removing student ${alumnoId} from class ${claseId}:`, error);
			throw error;
		}
	},

	async addProfesorToClase(claseId: number, profesorId: string): Promise<void> {
		try {
			await claseApi.agregarProfesor({
				claseId,
				profesorId
			});
		} catch (error) {
			console.error(`Error adding professor ${profesorId} to class ${claseId}:`, error);
			throw error;
		}
	},

	async removeProfesorFromClase(claseId: number, profesorId: string): Promise<void> {
		try {
			await claseApi.removerProfesor({
				claseId,
				profesorId
			});
		} catch (error) {
			console.error(`Error removing professor ${profesorId} from class ${claseId}:`, error);
			throw error;
		}
	},

	async addMaterialToClase(claseId: number, material: Material): Promise<void> {
		try {
			await claseApi.agregarMaterial({
				claseId,
				material
			});
		} catch (error) {
			console.error(`Error adding material to class ${claseId}:`, error);
			throw error;
		}
	},

	async removeMaterialFromClase(claseId: number, materialId: string): Promise<void> {
		try {
			await claseApi.removerMaterial({
				claseId,
				materialId
			});
		} catch (error) {
			console.error(`Error removing material ${materialId} from class ${claseId}:`, error);
			throw error;
		}
	},

	async deleteClase(id: number): Promise<void> {
		try {
			await claseApi.borrarClasePorId({ id });
		} catch (error) {
			console.error(`Error deleting class ${id}:`, error);
			throw error;
		}
	},

	// Student self-enrollment methods
	async enrollInClase(claseId: number): Promise<DTOClase> {
		try {
			const response = await claseApi.inscribirseEnClase({ claseId });
			return response;
		} catch (error) {
			console.error(`Error enrolling in class ${claseId}:`, error);
			throw error;
		}
	},

	async unenrollFromClase(claseId: number): Promise<DTOClase> {
		try {
			const response = await claseApi.darseDeBajaDeClase({ claseId });
			return response;
		} catch (error) {
			console.error(`Error unenrolling from class ${claseId}:`, error);
			throw error;
		}
	},

	// Simple search by title
	async searchClasesByTitle(titulo: string): Promise<DTOClase[]> {
		try {
			const response = await claseApi.buscarClasesPorTitulo({ titulo });
			return response;
		} catch (error) {
			console.error(`Error searching classes by title "${titulo}":`, error);
			throw error;
		}
	}
};
