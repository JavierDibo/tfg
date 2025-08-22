import type {
	DTOClase,
	DTOCurso,
	DTOTaller,
	DTOParametrosBusquedaClase,
	DTOParametrosBusquedaClaseNivelEnum,
	DTOParametrosBusquedaClasePresencialidadEnum,
	DTOPeticionCrearCurso,
	DTOPeticionCrearTaller,
	DTORespuestaPaginadaDTOClase
} from '$lib/generated/api';
import { claseApi } from '$lib/api';

export class ClaseService {
	/**
	 * Get all classes (filtered by user role)
	 */
	static async getClases(): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClases();
		} catch (error) {
			console.error('Error fetching classes:', error);
			throw error;
		}
	}

	/**
	 * Get a class by ID
	 */
	static async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			console.error(`Error fetching class with ID ${id}:`, error);
			throw error;
		}
	}

	/**
	 * Get a class by title
	 */
	static async getClaseByTitulo(titulo: string): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorTitulo({ titulo });
		} catch (error) {
			console.error(`Error fetching class with title ${titulo}:`, error);
			throw error;
		}
	}

	/**
	 * Search classes with advanced filters and pagination
	 */
	static async buscarClases(
		params: DTOParametrosBusquedaClase
	): Promise<DTORespuestaPaginadaDTOClase> {
		try {
			return await claseApi.buscarClases({ dTOParametrosBusquedaClase: params });
		} catch (error) {
			console.error('Error searching classes:', error);
			throw error;
		}
	}

	/**
	 * Search classes by title
	 */
	static async buscarClasesPorTitulo(titulo: string): Promise<DTOClase[]> {
		try {
			return await claseApi.buscarClasesPorTitulo({ titulo });
		} catch (error) {
			console.error(`Error searching classes with title ${titulo}:`, error);
			throw error;
		}
	}

	/**
	 * Get classes by student ID
	 */
	static async getClasesPorAlumno(alumnoId: string): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClasesPorAlumno({ alumnoId });
		} catch (error) {
			console.error(`Error fetching classes for student ${alumnoId}:`, error);
			throw error;
		}
	}

	/**
	 * Get classes by professor ID
	 */
	static async getClasesPorProfesor(profesorId: string): Promise<DTOClase[]> {
		try {
			return await claseApi.obtenerClasesPorProfesor({ profesorId });
		} catch (error) {
			console.error(`Error fetching classes for professor ${profesorId}:`, error);
			throw error;
		}
	}

	/**
	 * Get enrolled classes for current student
	 */
	static async getMisClasesInscritas(): Promise<DTOClase[]> {
		try {
			// Import here to avoid circular dependency
			const { EnrollmentService } = await import('./enrollmentService');
			return await EnrollmentService.getMyEnrolledClasses();
		} catch (error) {
			console.error('Error fetching enrolled classes:', error);
			throw error;
		}
	}

	/**
	 * Count students in a class
	 */
	static async contarAlumnosEnClase(claseId: number): Promise<number> {
		try {
			return await claseApi.contarAlumnosEnClase({ claseId });
		} catch (error) {
			console.error(`Error counting students in class ${claseId}:`, error);
			throw error;
		}
	}

	/**
	 * Count professors in a class
	 */
	static async contarProfesoresEnClase(claseId: number): Promise<number> {
		try {
			return await claseApi.contarProfesoresEnClase({ claseId });
		} catch (error) {
			console.error(`Error counting professors in class ${claseId}:`, error);
			throw error;
		}
	}

	/**
	 * Create a new course
	 */
	static async crearCurso(dtoPeticion: DTOPeticionCrearCurso): Promise<DTOCurso> {
		try {
			return await claseApi.crearCurso({ dTOPeticionCrearCurso: dtoPeticion });
		} catch (error) {
			console.error('Error creating course:', error);
			throw error;
		}
	}

	/**
	 * Create a new workshop
	 */
	static async crearTaller(dtoPeticion: DTOPeticionCrearTaller): Promise<DTOTaller> {
		try {
			return await claseApi.crearTaller({ dTOPeticionCrearTaller: dtoPeticion });
		} catch (error) {
			console.error('Error creating workshop:', error);
			throw error;
		}
	}

	/**
	 * Delete a class by ID
	 */
	static async borrarClasePorId(id: number): Promise<void> {
		try {
			await claseApi.borrarClasePorId({ id });
		} catch (error) {
			console.error(`Error deleting class with ID ${id}:`, error);
			throw error;
		}
	}

	/**
	 * Delete a class by title
	 */
	static async borrarClasePorTitulo(titulo: string): Promise<void> {
		try {
			await claseApi.borrarClasePorTitulo({ titulo });
		} catch (error) {
			console.error(`Error deleting class with title ${titulo}:`, error);
			throw error;
		}
	}

	/**
	 * Get paginated classes with search and filters
	 */
	static async getPaginatedClases(params: {
		page?: number;
		size?: number;
		titulo?: string;
		nivel?: DTOParametrosBusquedaClaseNivelEnum;
		presencialidad?: DTOParametrosBusquedaClasePresencialidadEnum;
		precioMin?: number;
		precioMax?: number;
		sortBy?: string;
		sortDirection?: 'ASC' | 'DESC';
	}): Promise<DTORespuestaPaginadaDTOClase> {
		try {
			const searchParams: DTOParametrosBusquedaClase = {
				pagina: params.page || 0,
				tamanoPagina: params.size || 20,
				ordenCampo: params.sortBy || 'titulo',
				ordenDireccion: params.sortDirection || 'ASC'
			};

			// Add filters if provided
			if (params.titulo) searchParams.titulo = params.titulo;
			if (params.nivel) searchParams.nivel = params.nivel;
			if (params.presencialidad) searchParams.presencialidad = params.presencialidad;
			if (params.precioMin !== undefined) searchParams.precioMinimo = params.precioMin;
			if (params.precioMax !== undefined) searchParams.precioMaximo = params.precioMax;

			return await this.buscarClases(searchParams);
		} catch (error) {
			console.error('Error fetching paginated classes:', error);
			throw error;
		}
	}
}
