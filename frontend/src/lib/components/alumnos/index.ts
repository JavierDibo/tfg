// Statistics Page Components
// ===========================

// Header for statistics page with refresh button and navigation back to main alumnos list
export { default as EstadisticasHeader } from './EstadisticasHeader.svelte';

// Statistics cards grid showing total students, enrolled, and non-enrolled with percentages
export { default as EstadisticasStatsGrid } from './EstadisticasStatsGrid.svelte';

// Visual charts section with enrollment status bar chart and executive summary panel
export { default as EstadisticasCharts } from './EstadisticasCharts.svelte';

// Quick action buttons for common tasks (view all, add student, filter by enrollment status)
export { default as EstadisticasQuickActions } from './EstadisticasQuickActions.svelte';

// Loading state component with spinner and loading message for statistics page
export { default as EstadisticasLoadingState } from './EstadisticasLoadingState.svelte';

// Error state component with retry functionality when statistics fail to load
export { default as EstadisticasErrorState } from './EstadisticasErrorState.svelte';

// Simple timestamp component showing when statistics were last updated
export { default as EstadisticasLastUpdated } from './EstadisticasLastUpdated.svelte';
