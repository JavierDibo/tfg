import { pagoApi } from '$lib/api';
import type { DTOPago, DTOPeticionCrearPago } from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class PagoService {
	// ==================== BASIC CRUD OPERATIONS ====================

	/**
	 * Create a new payment
	 */
	static async createPayment(paymentData: DTOPeticionCrearPago): Promise<DTOPago> {
		try {
			console.log('=== PAGO SERVICE: Creating Payment ===');
			console.log('Request data:', paymentData);

			const response = await pagoApi.crearPago({
				dTOPeticionCrearPago: paymentData
			});

			console.log('=== PAGO SERVICE: Response Received ===');
			console.log('Full API response:', response);
			console.log('Client Secret:', response.clientSecret);
			console.log('Payment Intent ID:', response.stripePaymentIntentId);
			console.log('Payment ID:', response.id);
			console.log('=====================================');

			return response;
		} catch (error) {
			console.error('=== PAGO SERVICE: Error ===');
			console.error('Error creating payment:', error);
			console.error('==========================');
			ErrorHandler.logError(error, 'createPayment');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a payment by ID
	 */
	static async getPayment(id: number): Promise<DTOPago> {
		try {
			return await pagoApi.obtenerPagoPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getPayment(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get all payments with pagination and sorting
	 */
	static async getPayments(params?: {
		page?: number;
		size?: number;
		sortBy?: string;
		sortDirection?: string;
	}): Promise<{ content: DTOPago[]; totalElements: number; totalPages: number }> {
		try {
			const response = await pagoApi.obtenerPagos(params || {});
			return {
				content: response.content || [],
				totalElements: response.totalElements || 0,
				totalPages: response.totalPages || 0
			};
		} catch (error) {
			ErrorHandler.logError(error, 'getPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== PAYMENT STATUS OPERATIONS ====================

	/**
	 * Check payment status
	 */
	static async checkPaymentStatus(id: number): Promise<{ isSuccessful: boolean; status: string }> {
		try {
			const response = await pagoApi.checkPaymentStatus({ id });
			return {
				isSuccessful: response.isSuccessful || false,
				status: response.status || 'UNKNOWN'
			};
		} catch (error) {
			ErrorHandler.logError(error, `checkPaymentStatus(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== RECENT PAYMENTS ====================

	/**
	 * Get recent payments (Admin/Professor only)
	 */
	static async getRecentPayments(limit: number = 10): Promise<DTOPago[]> {
		try {
			const response = await pagoApi.getRecentPayments({ limit });
			return response || [];
		} catch (error) {
			ErrorHandler.logError(error, 'getRecentPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get my recent payments (Student only)
	 */
	static async getMyRecentPayments(limit: number = 10): Promise<DTOPago[]> {
		try {
			const response = await pagoApi.getMyRecentPayments({ limit });
			return response || [];
		} catch (error) {
			ErrorHandler.logError(error, 'getMyRecentPayments');
			throw await ErrorHandler.parseError(error);
		}
	}

	// ==================== WEBHOOK PROCESSING ====================

	/**
	 * Process Stripe webhook
	 */
	static async processStripeWebhook(
		stripeSignature: string,
		body: string,
		stripeEventId?: string
	): Promise<void> {
		try {
			await pagoApi.procesarWebhookStripe({
				stripeSignature,
				body,
				stripeEventId
			});
		} catch (error) {
			ErrorHandler.logError(error, 'processStripeWebhook');
			throw await ErrorHandler.parseError(error);
		}
	}
}
