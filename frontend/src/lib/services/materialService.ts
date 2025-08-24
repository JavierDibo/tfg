import { materialApi } from '$lib/api';
import type {
	DTOMaterial,
	MaterialStats,
	DTORespuestaPaginadaDTOMaterial
} from '$lib/generated/api';
import { ErrorHandler } from '$lib/utils/errorHandler';

export class MaterialService {
	/**
	 * Get paginated materials with optional filters
	 */
	static async getMaterials(
		params: {
			q?: string;
			name?: string;
			url?: string;
			type?: string;
			page?: number;
			size?: number;
			sortBy?: string;
			sortDirection?: string;
		} = {}
	): Promise<DTORespuestaPaginadaDTOMaterial> {
		try {
			return await materialApi.obtenerMateriales(params);
		} catch (error) {
			ErrorHandler.logError(error, 'getMaterials');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get a specific material by ID
	 */
	static async getMaterialById(id: string): Promise<DTOMaterial> {
		try {
			return await materialApi.obtenerMaterialPorId({ id });
		} catch (error) {
			ErrorHandler.logError(error, `getMaterialById(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Create a new material
	 */
	static async createMaterial(name: string, url: string): Promise<DTOMaterial> {
		try {
			return await materialApi.crearMaterial({ name, url });
		} catch (error) {
			ErrorHandler.logError(error, 'createMaterial');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Update an existing material
	 */
	static async updateMaterial(id: string, name: string, url: string): Promise<DTOMaterial> {
		try {
			return await materialApi.actualizarMaterial({ id, name, url });
		} catch (error) {
			ErrorHandler.logError(error, `updateMaterial(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Delete a material by ID
	 */
	static async deleteMaterial(id: string): Promise<void> {
		try {
			await materialApi.borrarMaterial({ id });
		} catch (error) {
			ErrorHandler.logError(error, `deleteMaterial(${id})`);
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get material statistics
	 */
	static async getMaterialStats(): Promise<MaterialStats> {
		try {
			return await materialApi.obtenerEstadisticas();
		} catch (error) {
			ErrorHandler.logError(error, 'getMaterialStats');
			throw await ErrorHandler.parseError(error);
		}
	}

	/**
	 * Get material type from URL
	 */
	static getMaterialType(url: string): 'DOCUMENT' | 'IMAGE' | 'VIDEO' {
		const extension = url.split('.').pop()?.toLowerCase();

		if (!extension) return 'DOCUMENT';

		const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
		const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv'];

		if (imageExtensions.includes(extension)) {
			return 'IMAGE';
		} else if (videoExtensions.includes(extension)) {
			return 'VIDEO';
		} else {
			return 'DOCUMENT';
		}
	}

	/**
	 * Get material icon based on type
	 */
	static getMaterialIcon(material: DTOMaterial): string {
		if (material.video) return '🎥';
		if (material.image) return '🖼️';
		if (material.document) return '📄';

		// Fallback based on URL
		const type = MaterialService.getMaterialType(material.url || '');
		switch (type) {
			case 'VIDEO':
				return '🎥';
			case 'IMAGE':
				return '🖼️';
			default:
				return '📄';
		}
	}

	/**
	 * Get material type label
	 */
	static getMaterialTypeLabel(material: DTOMaterial): string {
		if (material.video) return 'Video';
		if (material.image) return 'Image';
		if (material.document) return 'Document';

		// Fallback based on URL
		const type = MaterialService.getMaterialType(material.url || '');
		switch (type) {
			case 'VIDEO':
				return 'Video';
			case 'IMAGE':
				return 'Image';
			default:
				return 'Document';
		}
	}
}
