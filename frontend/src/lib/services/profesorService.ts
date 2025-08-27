import {
	type DTOProfesor,
	type DTOPeticionRegistroProfesor,
	type DTOActualizacionProfesor,
	type DTORespuestaPaginadaDTOProfesor
} from '$lib/generated/api';
import { profesorApi } from '$lib/api';
import { ErrorHandler } from '$lib/utils/errorHandler';
import { ValidationUtils } from '$lib/utils/validators';

// Define the ProfesorStatistics type used in the estadisticas page
export interface ProfesorStatistics {
	totalCount: number;
	activeCount: number;
	inactiveCount: number;
	newThisMonth: number;
	newThisYear: number;
	bySubject?: Record<string, number>;
	byExperience?: Record<string, number>;
}

// Import pagination types
import type { SortDirection } from '$lib/types/pagination';

export class ProfesorService {
	static async getAllProfesores(filters: {
		firstName?: string;
		lastName?: string;
		email?: string;
		enabled?: boolean;
	}): Promise<DTOProfesor[]> {
		try {
			const response = await profesorApi.obtenerProfesores(filters);
			return response.content as DTOProfesor[];
		} catch (error) {
			ErrorHandler.logError(error, 'getAllProfesores');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getProfesoresPaginados(
		filters: {
			q?: string; // General search parameter
			firstName?: string;
			lastName?: string;
			email?: string;
			username?: string;
			dni?: string;
			enabled?: boolean;
			claseId?: string;
			sinClases?: boolean;
		},
		pagination: {
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: SortDirection;
		}
	): Promise<DTORespuestaPaginadaDTOProfesor> {
		try {
			const { page, size, sortBy, sortDirection } = pagination;
			const response = await profesorApi.obtenerProfesores({
				...filters,
				page,
				size,
				sortBy,
				sortDirection
			});

			return response as DTORespuestaPaginadaDTOProfesor;
		} catch (error) {
			ErrorHandler.logError(error, 'getProfesoresPaginados');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getProfesorById(id: number): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.obtenerProfesorPorId({ id });
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `getProfesorById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async createProfesor(profesorData: DTOPeticionRegistroProfesor): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.crearProfesor({
				dTOPeticionRegistroProfesor: profesorData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, 'createProfesor');
			throw await ErrorHandler.parseError(error);
		}
	}

	static async updateProfesor(
		id: number,
		updateData: DTOActualizacionProfesor
	): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesorParcial({
				id,
				dTOActualizacionProfesor: updateData
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `updateProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async deleteProfesor(id: number): Promise<void> {
		try {
			await profesorApi.eliminarProfesor({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteProfesor(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async toggleAccountStatus(id: number, habilitado: boolean): Promise<DTOProfesor> {
		try {
			const response = await profesorApi.actualizarProfesorParcial({
				id,
				dTOActualizacionProfesor: { enabled: habilitado }
			});
			return response;
		} catch (error) {
			ErrorHandler.logError(error, `toggleAccountStatus(${id}, ${habilitado})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	static async getStatistics(): Promise<ProfesorStatistics> {
		try {
			let totalCount = 0;
			let activeCount = 0;
			let inactiveCount = 0;

			// Get statistics by fetching all professors and counting
			try {
				const allProfesores = await this.getAllProfesores({});
				totalCount = allProfesores.length;
				activeCount = allProfesores.filter((p) => p.enabled).length;
				inactiveCount = allProfesores.filter((p) => !p.enabled).length;
			} catch (error) {
				console.error('Error fetching professor statistics:', error);
				totalCount = 0;
				activeCount = 0;
				inactiveCount = 0;
			}

			return {
				totalCount,
				activeCount,
				inactiveCount,
				newThisMonth: 0, // TODO: Implement when API provides this data
				newThisYear: 0 // TODO: Implement when API provides this data
			};
		} catch (error) {
			console.error('Error fetching professor statistics:', error);
			throw error;
		}
	}

	static validateRegistrationData(data: DTOPeticionRegistroProfesor): string[] {
		const errors: string[] = [];

		// Basic validation rules using utility functions
		const usernameValidation = ValidationUtils.validateUsername(data.username);
		if (!usernameValidation.isValid) {
			errors.push(usernameValidation.message);
		}

		const passwordValidation = ValidationUtils.validatePassword(data.password);
		if (!passwordValidation.isValid) {
			errors.push(passwordValidation.message);
		}

		const firstNameValidation = ValidationUtils.validateName(data.firstName);
		if (!firstNameValidation.isValid) {
			errors.push(firstNameValidation.message);
		}

		const lastNameValidation = ValidationUtils.validateName(data.lastName);
		if (!lastNameValidation.isValid) {
			errors.push(lastNameValidation.message);
		}

		const dniValidation = ValidationUtils.validateDNI(data.dni);
		if (!dniValidation.isValid) {
			errors.push(dniValidation.message);
		}

		const emailValidation = ValidationUtils.validateEmail(data.email);
		if (!emailValidation.isValid) {
			errors.push(emailValidation.message);
		}

		// Phone validation using utility function
		if (data.phoneNumber) {
			const phoneValidation = ValidationUtils.validatePhoneNumber(data.phoneNumber);
			if (!phoneValidation.isValid) {
				errors.push(phoneValidation.message);
			}
		}

		return errors;
	}

	// ==================== BUSINESS LOGIC METHODS ====================

	/**
	 * Handle account status change with validation and business logic
	 */
	static async handleAccountStatusChange(
		profesorId: number,
		newStatus: boolean
	): Promise<{ success: boolean; message: string; updatedProfesor?: DTOProfesor }> {
		try {
			// Validate input
			if (!profesorId || profesorId <= 0) {
				return {
					success: false,
					message: 'ID de profesor inválido'
				};
			}

			// Check if professor exists
			const existingProfesor = await this.getProfesorById(profesorId);
			if (!existingProfesor) {
				return {
					success: false,
					message: 'Profesor no encontrado'
				};
			}

			// Check if status is actually changing
			if (existingProfesor.enabled === newStatus) {
				return {
					success: false,
					message: `La cuenta ya está ${newStatus ? 'habilitada' : 'deshabilitada'}`
				};
			}

			// Perform the status change
			const updatedProfesor = await this.toggleAccountStatus(profesorId, newStatus);

			return {
				success: true,
				message: `Cuenta ${newStatus ? 'habilitada' : 'deshabilitada'} correctamente`,
				updatedProfesor
			};
		} catch (error) {
			return {
				success: false,
				message: error instanceof Error ? error.message : 'Error al cambiar estado de cuenta'
			};
		}
	}

	/**
	 * Validate professor registration data with business rules
	 */
	static validateRegistrationDataWithBusinessRules(data: DTOPeticionRegistroProfesor): {
		isValid: boolean;
		errors: string[];
	} {
		const errors: string[] = [];

		// Basic validation rules using utility functions
		const usernameValidation = ValidationUtils.validateUsername(data.username);
		if (!usernameValidation.isValid) {
			errors.push(usernameValidation.message);
		}

		const passwordValidation = ValidationUtils.validatePassword(data.password);
		if (!passwordValidation.isValid) {
			errors.push(passwordValidation.message);
		}

		const firstNameValidation = ValidationUtils.validateName(data.firstName);
		if (!firstNameValidation.isValid) {
			errors.push(firstNameValidation.message);
		}

		const lastNameValidation = ValidationUtils.validateName(data.lastName);
		if (!lastNameValidation.isValid) {
			errors.push(lastNameValidation.message);
		}

		const dniValidation = ValidationUtils.validateDNI(data.dni);
		if (!dniValidation.isValid) {
			errors.push(dniValidation.message);
		}

		const emailValidation = ValidationUtils.validateEmail(data.email);
		if (!emailValidation.isValid) {
			errors.push(emailValidation.message);
		}

		// Phone validation using utility function
		if (data.phoneNumber) {
			const phoneValidation = ValidationUtils.validatePhoneNumber(data.phoneNumber);
			if (!phoneValidation.isValid) {
				errors.push(phoneValidation.message);
			}
		}

		return {
			isValid: errors.length === 0,
			errors
		};
	}
}
