import { entregaApi } from '$lib/api';
import type { DTOEntregaEjercicio, DTORespuestaPaginada } from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { FormatterUtils } from '$lib/utils/formatters';
import { PermissionUtils } from '$lib/utils/permissions';
import { ValidationUtils } from '$lib/utils/validators';
import type { DTOAlumno, DTOProfesor } from '$lib/generated/api';

// Type definitions for better type safety
export interface EntregaFilters {
	readonly alumnoId?: string;
	readonly ejercicioId?: string;
	readonly estado?: string;
	readonly notaMin?: number;
	readonly notaMax?: number;
	readonly page?: number;
	readonly size?: number;
	readonly sortBy?: string;
	readonly sortDirection?: string;
}

export interface CreateEntregaData {
	readonly ejercicioId: string;
	readonly alumnoEntreganteId: string;
	readonly archivosEntregados?: string[];
}

export interface UpdateEntregaData {
	readonly nota?: number;
	readonly archivosEntregados?: string[];
	readonly comentarios?: string;
}

export interface GradeData {
	readonly nota: number;
	readonly comentarios?: string;
}

export interface ServiceResult<T = void> {
	readonly success: boolean;
	readonly message: string;
	readonly data?: T;
	readonly error?: Error;
}

export interface PaginatedResult<T> {
	readonly content: readonly T[];
	readonly totalElements: number;
	readonly totalPages: number;
	readonly currentPage: number;
	readonly pageSize: number;
}

export class EntregaService {
	private static readonly api = entregaApi;

	// ==================== VALIDATION METHODS ====================

	/**
	 * Validate delivery creation data
	 */
	static validateCreateData(data: Readonly<CreateEntregaData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate ejercicioId
		if (!data.ejercicioId || data.ejercicioId.trim().length === 0) {
			errors.push('El ID del ejercicio es obligatorio');
		}

		// Validate alumnoEntreganteId
		if (!data.alumnoEntreganteId || data.alumnoEntreganteId.trim().length === 0) {
			errors.push('El ID del alumno es obligatorio');
		}

		// Validate archivosEntregados if provided
		if (data.archivosEntregados && data.archivosEntregados.length > 0) {
			for (const archivo of data.archivosEntregados) {
				if (!archivo || archivo.trim().length === 0) {
					errors.push('Los archivos no pueden estar vacíos');
					break;
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Validate grade data using ValidationUtils
	 */
	static validateGradeData(data: Readonly<GradeData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate nota using ValidationUtils
		const gradeValidation = ValidationUtils.validateGrade(data.nota);
		if (!gradeValidation.isValid) {
			errors.push(gradeValidation.message);
		}

		// Validate comentarios length if provided
		if (data.comentarios && data.comentarios.length > 500) {
			errors.push('Los comentarios no pueden superar 500 caracteres');
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	/**
	 * Validate delivery update data
	 */
	static validateUpdateData(data: Readonly<UpdateEntregaData>): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Validate nota if provided
		if (data.nota !== undefined && data.nota !== null) {
			const gradeValidation = ValidationUtils.validateGrade(data.nota);
			if (!gradeValidation.isValid) {
				errors.push(gradeValidation.message);
			}
		}

		// Validate comentarios length if provided
		if (data.comentarios && data.comentarios.length > 500) {
			errors.push('Los comentarios no pueden superar 500 caracteres');
		}

		// Validate archivosEntregados if provided
		if (data.archivosEntregados && data.archivosEntregados.length > 0) {
			for (const archivo of data.archivosEntregados) {
				if (!archivo || archivo.trim().length === 0) {
					errors.push('Los archivos no pueden estar vacíos');
					break;
				}
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}

	// ==================== PERMISSION METHODS ====================

	/**
	 * Check if user can grade deliveries
	 */
	static canGradeDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canGradeDelivery(user);
	}

	/**
	 * Check if user can view all deliveries
	 */
	static canViewAllDeliveries(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canViewAllDeliveries(user);
	}

	/**
	 * Check if user can edit delivery
	 */
	static canEditDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canEditEntity('entregas', user);
	}

	/**
	 * Check if user can delete delivery
	 */
	static canDeleteDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return PermissionUtils.canDeleteEntity('entregas', user);
	}

	// ==================== FORMATTING METHODS ====================

	/**
	 * Format delivery status for display
	 */
	static formatDeliveryStatus(status: string | undefined | null): string {
		return FormatterUtils.formatStatus(status, 'delivery');
	}

	/**
	 * Get delivery status color for styling
	 */
	static getDeliveryStatusColor(status: string | undefined | null): string {
		return FormatterUtils.getStatusColor(status, 'delivery');
	}

	/**
	 * Format grade for display
	 */
	static formatGrade(nota: number | undefined | null): string {
		return FormatterUtils.formatGrade(nota);
	}

	/**
	 * Get grade color for styling
	 */
	static getGradeColor(nota: number | undefined | null): string {
		return FormatterUtils.getGradeColor(nota);
	}

	/**
	 * Format delivery date
	 */
	static formatDeliveryDate(date: Date | string | undefined | null): string {
		return FormatterUtils.formatDate(date, { includeTime: true });
	}

	// ==================== BASIC CRUD OPERATIONS ====================

	static async getEntregas(params?: Readonly<EntregaFilters>): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas(params);
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregas');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregaById(id: number): Promise<DTOEntregaEjercicio> {
		try {
			const response = await EntregaService.api.obtenerEntregaPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregaById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async createEntrega(entrega: Readonly<CreateEntregaData>): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before creating
			const validation = this.validateCreateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.crearEntrega({
				dTOPeticionCrearEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createEntrega');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async updateEntrega(
		id: number,
		entrega: Readonly<UpdateEntregaData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before updating
			const validation = this.validateUpdateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.actualizarEntregaParcial({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async replaceEntrega(
		id: number,
		entrega: Readonly<UpdateEntregaData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate data before replacing
			const validation = this.validateUpdateData(entrega);
			if (!validation.isValid) {
				throw new Error(`Datos de entrega inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.reemplazarEntrega({
				id,
				dTOPeticionActualizarEntregaEjercicio: entrega
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `replaceEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async deleteEntrega(id: number): Promise<void> {
		try {
			await EntregaService.api.eliminarEntrega({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== GRADING OPERATIONS ====================

	static async gradeEntrega(
		id: number,
		gradeData: Readonly<GradeData>
	): Promise<DTOEntregaEjercicio> {
		try {
			// Validate grade data before grading
			const validation = this.validateGradeData(gradeData);
			if (!validation.isValid) {
				throw new Error(`Datos de calificación inválidos: ${validation.errors.join(', ')}`);
			}

			const response = await EntregaService.api.actualizarEntregaParcial({
				id,
				dTOPeticionActualizarEntregaEjercicio: gradeData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `gradeEntrega(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== FILTERED QUERY OPERATIONS ====================

	static async getEntregasByEjercicio(ejercicioId: number): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({
				ejercicioId: ejercicioId.toString()
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregasByEjercicio(${ejercicioId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasByAlumno(alumnoId: number): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ alumnoId: alumnoId.toString() });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getEntregasByAlumno(${alumnoId})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasPendientes(): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ estado: 'PENDIENTE' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregasPendientes');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getEntregasCalificadas(): Promise<DTORespuestaPaginada> {
		try {
			const response = await EntregaService.api.obtenerEntregas({ estado: 'CALIFICADO' });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'getEntregasCalificadas');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle delivery grading with validation and business logic
	 */
	static async handleDeliveryGrading(
		deliveryId: number,
		gradeData: Readonly<GradeData>,
		user: DTOAlumno | DTOProfesor
	): Promise<{ success: boolean; message: string; updatedDelivery?: DTOEntregaEjercicio }> {
		try {
			// Check permissions
			if (!this.canGradeDelivery(user)) {
				return {
					success: false,
					message: 'No tienes permisos para calificar entregas'
				};
			}

			// Validate input
			if (!deliveryId || deliveryId <= 0) {
				return {
					success: false,
					message: 'ID de entrega inválido'
				};
			}

			// Validate grade data
			const validation = this.validateGradeData(gradeData);
			if (!validation.isValid) {
				return {
					success: false,
					message: `Datos de calificación inválidos: ${validation.errors.join(', ')}`
				};
			}

			// Check if delivery exists
			const existingDelivery = await this.getEntregaById(deliveryId);
			if (!existingDelivery) {
				return {
					success: false,
					message: 'Entrega no encontrada'
				};
			}

			// Check if delivery is already graded
			if (existingDelivery.estado === 'CALIFICADO') {
				return {
					success: false,
					message: 'La entrega ya está calificada'
				};
			}

			// Perform the grading
			const updatedDelivery = await this.gradeEntrega(deliveryId, gradeData);

			return {
				success: true,
				message: 'Entrega calificada correctamente',
				updatedDelivery
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al calificar la entrega'
			};
		}
	}

	/**
	 * Get delivery statistics with formatted data
	 */
	static async getEstadisticas(): Promise<{
		totalEntregas: number;
		pendingEntregas: number;
		gradedEntregas: number;
		averageGrade: number;
		formattedStats: {
			totalFormatted: string;
			pendingFormatted: string;
			gradedFormatted: string;
			averageFormatted: string;
		};
	}> {
		try {
			// This would need to be implemented in the API
			// For now, we'll return a placeholder with formatted data
			const stats = {
				totalEntregas: 0,
				pendingEntregas: 0,
				gradedEntregas: 0,
				averageGrade: 0
			};

			return {
				...stats,
				formattedStats: {
					totalFormatted: stats.totalEntregas.toString(),
					pendingFormatted: stats.pendingEntregas.toString(),
					gradedFormatted: stats.gradedEntregas.toString(),
					averageFormatted: this.formatGrade(stats.averageGrade)
				}
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getEstadisticas');
			throw await ErrorHandler.parseError(error);
		}
	}
}
