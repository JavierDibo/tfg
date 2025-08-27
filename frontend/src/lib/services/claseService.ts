import type {
	DTOClase,
	DTOCurso,
	DTOTaller,
	DTOPeticionCrearCurso,
	DTOPeticionCrearTaller,
	DTORespuestaPaginada,
	DTORespuestaPaginadaDTOClase,
	DTOProfesor
} from '$lib/generated/api';
import { claseApi, profesorApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';
import { FormatterUtils } from '$lib/utils/formatters';

export class ClaseService {
	/**
	 * Get all classes (filtered by user role)
	 */
	static async getClases(params: Record<string, unknown> = {}): Promise<DTORespuestaPaginada> {
		try {
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getClases');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a class by ID
	 */
	static async getClaseById(id: number): Promise<DTOClase> {
		try {
			return await claseApi.obtenerClasePorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'getClaseById');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get professors for a specific class
	 */
	static async getProfesoresPorClase(claseId: number): Promise<DTOProfesor[]> {
		try {
			// First, get the class details which include the profesoresId array
			const clase = await claseApi.obtenerClasePorId({ id: claseId });

			// If no professors are assigned, return empty array
			if (!clase.profesoresId || clase.profesoresId.length === 0) {
				return [];
			}

			// Fetch each professor by their ID
			const profesores: DTOProfesor[] = [];
			for (const profesorId of clase.profesoresId) {
				try {
					const profesor = await profesorApi.obtenerProfesorPorId({ id: parseInt(profesorId) });
					profesores.push(profesor);
				} catch (error) {
					console.warn(`Failed to fetch professor with ID ${profesorId}:`, error);
					// Continue with other professors even if one fails
				}
			}

			return profesores;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPorClase');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new course
	 */
	static async crearCurso(curso: DTOPeticionCrearCurso): Promise<DTOCurso> {
		try {
			return await claseApi.crearCurso({ dTOPeticionCrearCurso: curso });
		} catch (error) {
			ErrorHandler.logError(error, 'crearCurso');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new workshop
	 */
	static async crearTaller(taller: DTOPeticionCrearTaller): Promise<DTOTaller> {
		try {
			return await claseApi.crearTaller({ dTOPeticionCrearTaller: taller });
		} catch (error) {
			ErrorHandler.logError(error, 'crearTaller');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a class by ID
	 */
	static async borrarClasePorId(id: number): Promise<void> {
		try {
			await claseApi.eliminarClase({ id });
		} catch (error) {
			ErrorHandler.logError(error, 'borrarClasePorId');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get classes with pagination and filters
	 */
	static async buscarClasesConPaginacion(
		page: number = 0,
		size: number = 10,
		sortBy?: string,
		sortDirection?: string,
		filters: Record<string, unknown> = {}
	): Promise<DTORespuestaPaginadaDTOClase> {
		try {
			const params = {
				page,
				size,
				sortBy,
				sortDirection,
				...filters
			};
			return await claseApi.obtenerClases(params);
		} catch (error) {
			ErrorHandler.logError(error, 'buscarClasesConPaginacion');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle student enrollment with validation and business logic
	 */
	static async handleStudentEnrollment(
		claseId: number,
		alumnoId: number
	): Promise<{
		success: boolean;
		message: string;
		enrollmentData?: { claseId: number; alumnoId: number; enrollmentDate: string };
	}> {
		try {
			// Validate input
			if (!claseId || claseId <= 0) {
				return {
					success: false,
					message: 'ID de clase inválido'
				};
			}

			if (!alumnoId || alumnoId <= 0) {
				return {
					success: false,
					message: 'ID de alumno inválido'
				};
			}

			// Check if class exists
			const clase = await this.getClaseById(claseId);
			if (!clase) {
				return {
					success: false,
					message: 'Clase no encontrada'
				};
			}

			// Check if class has available spots (if capacity is defined)
			// Note: DTOClase doesn't have capacity/enrolledStudents properties
			// This would need to be implemented when the API supports it
			if (clase.numeroAlumnos && clase.alumnosId && clase.alumnosId.length >= clase.numeroAlumnos) {
				return {
					success: false,
					message: 'La clase ha alcanzado su capacidad máxima'
				};
			}

			// TODO: Add enrollment API call when available
			// For now, return success with placeholder data
			return {
				success: true,
				message: 'Inscripción realizada correctamente',
				enrollmentData: {
					claseId,
					alumnoId,
					enrollmentDate: FormatterUtils.formatDate(new Date(), { includeTime: true })
				}
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error en la inscripción'
			};
		}
	}

	/**
	 * Handle student unenrollment with validation and business logic
	 */
	static async handleStudentUnenrollment(
		claseId: number,
		alumnoId: number
	): Promise<{ success: boolean; message: string }> {
		try {
			// Validate input
			if (!claseId || claseId <= 0) {
				return {
					success: false,
					message: 'ID de clase inválido'
				};
			}

			if (!alumnoId || alumnoId <= 0) {
				return {
					success: false,
					message: 'ID de alumno inválido'
				};
			}

			// Check if class exists
			const clase = await this.getClaseById(claseId);
			if (!clase) {
				return {
					success: false,
					message: 'Clase no encontrada'
				};
			}

			// TODO: Add unenrollment API call when available
			// For now, return success
			return {
				success: true,
				message: 'Baja realizada correctamente'
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error en la baja'
			};
		}
	}

	/**
	 * Validate class creation data with business rules using utility functions
	 */
	static validateClassCreationData(data: DTOPeticionCrearCurso | DTOPeticionCrearTaller): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Common validation for both courses and workshops
		if (!data.titulo || data.titulo.trim().length === 0) {
			errors.push('El título de la clase es obligatorio');
		} else if (data.titulo.length < 3) {
			errors.push('El título debe tener al menos 3 caracteres');
		}

		if (!data.descripcion || data.descripcion.trim().length === 0) {
			errors.push('La descripción es obligatoria');
		} else if (data.descripcion.length < 10) {
			errors.push('La descripción debe tener al menos 10 caracteres');
		}

		// Use utility function for price validation
		const priceValidation = ValidationUtils.validatePrice(data.precio);
		if (!priceValidation.isValid) {
			errors.push(priceValidation.message);
		}

		// Course-specific validation
		if ('fechaInicio' in data && 'fechaFin' in data) {
			const startDate = new Date(data.fechaInicio);
			const endDate = new Date(data.fechaFin);
			const now = new Date();

			if (startDate <= now) {
				errors.push('La fecha de inicio debe ser futura');
			}

			if (endDate <= startDate) {
				errors.push('La fecha de fin debe ser posterior a la fecha de inicio');
			}
		}

		// Workshop-specific validation
		if ('duracionHoras' in data) {
			if (!data.duracionHoras || data.duracionHoras <= 0) {
				errors.push('La duración debe ser mayor que 0');
			}
		}

		if ('fechaRealizacion' in data) {
			if (!data.fechaRealizacion) {
				errors.push('La fecha del taller es obligatoria');
			} else {
				const workshopDate = new Date(data.fechaRealizacion);
				const now = new Date();
				if (workshopDate <= now) {
					errors.push('La fecha del taller debe ser futura');
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Format class information using utility functions
	 */
	static formatClassInfo(clase: DTOClase): {
		formattedPrice: string;
		formattedLevel: string;
		levelColor: string;
		studentCount: string;
		professorCount: string;
	} {
		return {
			formattedPrice: FormatterUtils.formatPrice(clase.precio),
			formattedLevel: clase.nivel || 'N/A',
			levelColor: FormatterUtils.getNivelColor(clase.nivel),
			studentCount: `${clase.numeroAlumnos || 0} alumnos`,
			professorCount: `${clase.numeroProfesores || 0} profesores`
		};
	}
}
