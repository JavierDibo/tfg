import type { PaginationMetaData } from '$lib/types/pagination';

/**
 * Represents the base entity that all other entities should extend
 */
export interface BaseEntity {
	id?: string | number;
	[key: string]: any;
}

/**
 * Configuration for entity columns in a data table
 */
export interface EntityColumn<T = any> {
	key: keyof T | string;
	header: string;
	sortable?: boolean;
	formatter?: (value: any, entity: T) => string | null;
	cell?: (entity: T) => string; // For custom cell rendering
	width?: string;
	class?: string;
}

/**
 * Standard filter interface used across all entity search components
 */
export interface EntityFilters {
	searchMode: 'simple' | 'advanced' | string;
	busquedaGeneral?: string;
	[key: string]: any;
}

/**
 * Standard pagination interface used across all entity components
 */
export interface EntityPagination {
	page: number;
	size: number;
	sortBy: string;
	sortDirection: 'ASC' | 'DESC';
}

/**
 * Common action configuration for entity tables
 */
export interface EntityAction<T = any> {
	label: string;
	dynamicLabel?: (entity: T) => string;
	icon?: string;
	color: string;
	hoverColor: string;
	condition?: (entity: T) => boolean;
	action: (entity: T) => void;
}

/**
 * Generic entity collection with pagination metadata
 */
export interface PaginatedEntities<T = any> {
	content?: T[];
	page?: PaginationMetaData;
}

/**
 * Authentication store type with common properties
 */
export interface AuthStoreType {
	isAdmin: boolean;
	isAuthenticated: boolean;
	isProfesor?: boolean;
	[key: string]: any;
}
