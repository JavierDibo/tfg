// Simplified pagination types using generated API types
import type { DTOMetadatosPaginacion, DTORespuestaPaginadaDTOAlumno } from '$lib/generated/api';

// Use generated types directly
export type PaginatedAlumnosResponse = DTORespuestaPaginadaDTOAlumno;
export type PageMetadata = DTOMetadatosPaginacion;
export type SortDirection = 'ASC' | 'DESC';

// Centralized parameter validation and creation
export function createValidPaginationParams(searchParams: URLSearchParams) {
  return {
    page: Math.max(0, parseInt(searchParams.get('page') || '0')),
    size: Math.min(100, Math.max(1, parseInt(searchParams.get('size') || '20'))),
    sortBy: searchParams.get('sortBy') || 'id',
    sortDirection: (searchParams.get('sortDirection') || 'ASC') as SortDirection,
    // Filters
    nombre: searchParams.get('nombre') || undefined,
    apellidos: searchParams.get('apellidos') || undefined,
    dni: searchParams.get('dni') || undefined,
    email: searchParams.get('email') || undefined,
    matriculado: searchParams.get('matriculado') ? searchParams.get('matriculado') === 'true' : undefined
  };
}

// Simple UI helper for display info
export function getPageDisplayInfo(metadata: DTOMetadatosPaginacion) {
  const currentPage = (metadata.number ?? 0) + 1; // Convert to 1-indexed
  const size = metadata.size ?? 20;
  const totalElements = metadata.totalElements ?? 0;
  
  return {
    currentPage,
    totalPages: metadata.totalPages ?? 0,
    startItem: Math.min(totalElements, (metadata.number ?? 0) * size + 1),
    endItem: Math.min(totalElements, ((metadata.number ?? 0) + 1) * size),
    totalItems: totalElements,
    hasNext: metadata.hasNext ?? false,
    hasPrevious: metadata.hasPrevious ?? false,
    isFirst: metadata.first ?? true,
    isLast: metadata.last ?? true
  };
}
