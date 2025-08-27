import { ejercicioApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';
import { FormatterUtils } from '$lib/utils/formatters';
import { PermissionUtils } from '$lib/utils/permissions';
import { NavigationUtils } from '$lib/utils/navigation';
import type { DTOPeticionCrearEjercicio, DTOAlumno, DTOProfesor } from '$lib/generated/api';

export class EjercicioService {
	private static api = ejercicioApi;

	static async getEjercicios(params?: {
		classId?: string;
		q?: string;
		name?: string;
		statement?: string;
		status?: string;
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}) {
		try {
			const response = await EjercicioService.api.obtenerEjercicios(params);
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEjercicios');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEjercicioById(id: number) {
		try {
			const response = await EjercicioService.api.obtenerEjercicioPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEjercicioById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async createEjercicio(ejercicio: {
		nombre: string;
		enunciado: string;
		fechaInicioPlazo: Date;
		fechaFinalPlazo: Date;
		claseId: string;
		duracionEnHoras?: number;
	}) {
		try {
			const ejercicioData: DTOPeticionCrearEjercicio = {
				nombre: ejercicio.nombre,
				enunciado: ejercicio.enunciado,
				fechaInicioPlazo: ejercicio.fechaInicioPlazo,
				fechaFinalPlazo: ejercicio.fechaFinalPlazo,
				claseId: ejercicio.claseId,
				duracionEnHoras: ejercicio.duracionEnHoras
			};

			const response = await EjercicioService.api.crearEjercicio({
				dTOPeticionCrearEjercicio: ejercicioData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createEjercicio');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async updateEjercicio(
		id: number,
		ejercicio:
			| Partial<{
					nombre?: string;
					enunciado?: string;
					fechaInicioPlazo?: Date;
					fechaFinalPlazo?: Date;
					claseId?: string;
					duracionEnHoras?: number;
			  }>
			| {
					name?: string;
					statement?: string;
					startDate?: Date;
					endDate?: Date;
					classId?: string;
			  }
	) {
		try {
			// Handle both Spanish and English field names for backward compatibility
			const isSpanishFormat = 'nombre' in ejercicio;
			const ejercicioData: DTOPeticionCrearEjercicio = {
				nombre: isSpanishFormat
					? (ejercicio as { nombre?: string }).nombre || ''
					: (ejercicio as { name?: string }).name || '',
				enunciado: isSpanishFormat
					? (ejercicio as { enunciado?: string }).enunciado || ''
					: (ejercicio as { statement?: string }).statement || '',
				fechaInicioPlazo: isSpanishFormat
					? (ejercicio as { fechaInicioPlazo?: Date }).fechaInicioPlazo || new Date()
					: (ejercicio as { startDate?: Date }).startDate || new Date(),
				fechaFinalPlazo: isSpanishFormat
					? (ejercicio as { fechaFinalPlazo?: Date }).fechaFinalPlazo || new Date()
					: (ejercicio as { endDate?: Date }).endDate || new Date(),
				claseId: isSpanishFormat
					? (ejercicio as { claseId?: string }).claseId || ''
					: (ejercicio as { classId?: string }).classId || '',
				duracionEnHoras: isSpanishFormat
					? (ejercicio as { duracionEnHoras?: number }).duracionEnHoras
					: undefined
			};

			const response = await EjercicioService.api.actualizarEjercicioParcial({
				id,
				dTOPeticionCrearEjercicio: ejercicioData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateEjercicio(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async replaceEjercicio(
		id: number,
		ejercicio: {
			nombre: string;
			enunciado: string;
			fechaInicioPlazo: Date;
			fechaFinalPlazo: Date;
			claseId: string;
			duracionEnHoras?: number;
		}
	) {
		try {
			const ejercicioData: DTOPeticionCrearEjercicio = {
				nombre: ejercicio.nombre,
				enunciado: ejercicio.enunciado,
				fechaInicioPlazo: ejercicio.fechaInicioPlazo,
				fechaFinalPlazo: ejercicio.fechaFinalPlazo,
				claseId: ejercicio.claseId,
				duracionEnHoras: ejercicio.duracionEnHoras
			};

			const response = await EjercicioService.api.reemplazarEjercicio({
				id,
				dTOPeticionCrearEjercicio: ejercicioData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `replaceEjercicio(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async deleteEjercicio(id: number) {
		try {
			await EjercicioService.api.eliminarEjercicio({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteEjercicio(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEjerciciosActivos() {
		try {
			// Use the general obtenerEjercicios method with status filter
			const response = await EjercicioService.api.obtenerEjercicios({ status: 'ACTIVE' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEjerciciosActivos');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEjerciciosVencidos() {
		try {
			// Use the general obtenerEjercicios method with status filter
			const response = await EjercicioService.api.obtenerEjercicios({ status: 'EXPIRED' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEjerciciosVencidos');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEjerciciosFuturos() {
		try {
			// Use the general obtenerEjercicios method with status filter
			const response = await EjercicioService.api.obtenerEjercicios({ status: 'FUTURE' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEjerciciosFuturos');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEjerciciosUrgentes() {
		try {
			// Use the general obtenerEjercicios method with status filter
			const response = await EjercicioService.api.obtenerEjercicios({ status: 'URGENT' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEjerciciosUrgentes');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEstadisticas() {
		try {
			// Use the obtenerEstadisticasGenerales method
			const response = await EjercicioService.api.obtenerEstadisticasGenerales();
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEstadisticas');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEstadisticasEjercicio(ejercicioId: number) {
		try {
			const response = await EjercicioService.api.obtenerEstadisticasEjercicio({ id: ejercicioId });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEstadisticasEjercicio(${ejercicioId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Validate exercise creation data with business rules
	 */
	static validateExerciseData(data: {
		nombre: string;
		enunciado: string;
		fechaInicioPlazo: Date;
		fechaFinalPlazo: Date;
		claseId: string;
		duracionEnHoras?: number;
	}): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate name
		const nameValidation = ValidationUtils.validateName(data.nombre);
		if (!nameValidation.isValid) {
			errors.push(nameValidation.message);
		}

		// Validate statement (enunciado)
		if (!data.enunciado || data.enunciado.trim().length === 0) {
			errors.push('El enunciado es obligatorio');
		} else if (data.enunciado.length > 1000) {
			errors.push('El enunciado no puede superar 1000 caracteres');
		}

		// Validate dates
		const now = new Date();
		if (data.fechaInicioPlazo < now) {
			errors.push('La fecha de inicio no puede ser anterior a la fecha actual');
		}

		if (data.fechaFinalPlazo <= data.fechaInicioPlazo) {
			errors.push('La fecha final debe ser posterior a la fecha de inicio');
		}

		// Validate class ID
		if (!data.claseId || data.claseId.trim().length === 0) {
			errors.push('La clase es obligatoria');
		}

		// Validate duration if provided
		if (data.duracionEnHoras !== undefined && data.duracionEnHoras <= 0) {
			errors.push('La duración debe ser mayor a 0 horas');
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Format exercise status for display
	 */
	static formatExerciseStatus(status: string | undefined | null): string {
		return FormatterUtils.formatStatus(status, 'exercise');
	}

	/**
	 * Get exercise status color for UI
	 */
	static getExerciseStatusColor(status: string | undefined | null): string {
		return FormatterUtils.getStatusColor(status, 'exercise');
	}

	/**
	 * Format exercise dates for display
	 */
	static formatExerciseDate(date: Date | string | undefined | null, includeTime = true): string {
		return FormatterUtils.formatDate(date, { includeTime });
	}

	/**
	 * Check if user can create exercises
	 */
	static canCreateExercise(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canCreateExercises(user);
	}

	/**
	 * Check if user can edit exercises
	 */
	static canEditExercise(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canEditEntity('ejercicios', user);
	}

	/**
	 * Check if user can delete exercises
	 */
	static canDeleteExercise(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canDeleteEntity('ejercicios', user);
	}

	/**
	 * Navigate to exercise view
	 */
	static navigateToExercise(exerciseId: number): void {
		NavigationUtils.goToEntity('ejercicios', exerciseId);
	}

	/**
	 * Navigate to exercise edit
	 */
	static navigateToExerciseEdit(exerciseId: number): void {
		NavigationUtils.navigateToEditExercise(exerciseId);
	}

	/**
	 * Navigate to exercise submission
	 */
	static navigateToExerciseSubmission(exerciseId: number): void {
		NavigationUtils.navigateToSubmitExercise(exerciseId);
	}

	/**
	 * Navigate to exercise list
	 */
	static navigateToExerciseList(): void {
		NavigationUtils.goToEntityList('ejercicios');
	}

	/**
	 * Navigate to create new exercise
	 */
	static navigateToCreateExercise(): void {
		NavigationUtils.goToEntityCreate('ejercicios');
	}
}
