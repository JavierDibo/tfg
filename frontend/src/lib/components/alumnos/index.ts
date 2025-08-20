// Main Alumnos Management Components
// =====================================

// Header component with title and "New Student" button for the main alumnos page
export { default as AlumnosHeader } from './AlumnosHeader.svelte';

// Statistics summary cards showing quick stats (total, enrolled, etc.) - used in main alumnos view
export { default as AlumnosStats } from './AlumnosStats.svelte';

// Filter controls for advanced search functionality (name, apellidos, DNI, email filters)
export { default as AlumnosFilters } from './AlumnosFilters.svelte';

// Basic table component for displaying student data in tabular format
export { default as AlumnosTable } from './AlumnosTable.svelte';

// Pagination component for navigating through student records
export { default as AlumnosPagination } from './AlumnosPagination.svelte';

// Combined search section with simple/advanced search modes, filters, and export functionality
export { default as AlumnosSearchSection } from './AlumnosSearchSection.svelte';

// Enhanced pagination controls with page size selection, sorting options, and navigation
export { default as AlumnosPaginationControls } from './AlumnosPaginationControls.svelte';

// Complete data table with loading states, sorting, and student action buttons (view, edit, delete)
export { default as AlumnosDataTable } from './AlumnosDataTable.svelte';

// Confirmation modal for deleting student records with safety prompts
export { default as AlumnosDeleteModal } from './AlumnosDeleteModal.svelte';

// Reusable message component for displaying success and error notifications
export { default as AlumnosMessages } from './AlumnosMessages.svelte';

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
