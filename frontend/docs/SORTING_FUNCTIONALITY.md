# Sorting Functionality Implementation

This document describes the sorting functionality implementation across all entity tables in the application.

## Overview

The sorting system uses API query parameters to handle sorting server-side, ensuring consistent and efficient data retrieval. All tables now support:

- **Server-side sorting** via API query parameters
- **Default sorting by ID** for consistent data ordering
- **Toggle sorting direction** (ASC/DESC) when clicking the same column
- **Reset to first page** when changing sort criteria
- **URL-based state management** for bookmarkable and shareable URLs

## API Query Parameters

The sorting system uses these standard query parameters:

```
?page=0&size=20&sortBy=id&sortDirection=ASC
```

- `page` - Current page number (0-based)
- `size` - Number of items per page
- `sortBy` - Field to sort by (e.g., 'id', 'nombre', 'email')
- `sortDirection` - Sort direction ('ASC' or 'DESC')

## Implementation Patterns

### 1. State Management

Each page maintains sorting state:

```typescript
// Sorting state
let sortBy = $state('id'); // Default sort field
let sortDirection = $state<'ASC' | 'DESC'>('ASC'); // Default direction
```

### 2. API Integration

Sorting parameters are passed to the API:

```typescript
async function loadData() {
    const params: Record<string, unknown> = {
        page: currentPage,
        size: pageSize,
        sortBy: sortBy,
        sortDirection: sortDirection
    };
    
    const response = await Service.getPaginatedData(params);
    // Process response...
}
```

### 3. Sorting Function

The `changeSorting` function handles sort changes:

```typescript
function changeSorting(field: string | { value: string; direction: 'ASC' | 'DESC' }) {
    if (typeof field === 'string') {
        // Toggle direction if same field, otherwise set to ASC
        if (sortBy === field) {
            sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
        } else {
            sortBy = field;
            sortDirection = 'ASC';
        }
    } else {
        sortBy = field.value;
        sortDirection = field.direction;
    }
    
    currentPage = 0; // Reset to first page when sorting
    loadData(); // Reload data with new sorting
}
```

## Page Implementations

### Alumnos Page

- **Default sort**: `id` ASC
- **Sortable fields**: ID, Nombre, Apellidos, DNI, Email, Estado de Matrícula, Estado
- **State management**: Local state with reactive loading

### Clases Page

- **Default sort**: `id` ASC
- **Sortable fields**: ID, Título, Descripción, Nivel, Presencialidad, Precio
- **State management**: Local state with reactive loading

### Profesores Page

- **Default sort**: `id` ASC
- **Sortable fields**: ID, Usuario, Nombre, Apellidos, DNI, Email, Habilitado, Fecha Creación
- **State management**: URL-based state management for bookmarkable URLs

## Table Column Configuration

Sortable columns are configured with the `sortable: true` property:

```typescript
const tableColumns = createColumns({
    id: commonColumns.student.id,        // sortable: true
    name: commonColumns.student.name,    // sortable: true (uses 'nombre' field)
    email: commonColumns.student.email,  // sortable: true
    dni: commonColumns.student.dni,      // sortable: true
    enrollment: commonColumns.student.enrollment, // sortable: true
    enabled: commonColumns.student.enabled        // sortable: true
});
```

## Available Sort Fields by Entity

### Alumnos (Students)
- **Database fields**: `id`, `nombre`, `apellidos`, `dni`, `email`, `matriculado`, `enabled`
- **Sortable columns**: ID, Nombre, Apellidos, DNI, Email, Estado de Matrícula, Estado

### Clases (Classes)
- **Database fields**: `id`, `titulo`, `descripcion`, `nivel`, `presencialidad`, `precio`
- **Sortable columns**: ID, Título, Descripción, Nivel, Presencialidad, Precio
- **Enum values**:
  - Nivel: `PRINCIPIANTE`, `INTERMEDIO`, `AVANZADO`
  - Presencialidad: `PRESENCIAL`, `ONLINE`

### Profesores (Teachers)
- **Database fields**: `id`, `usuario`, `nombre`, `apellidos`, `dni`, `email`, `enabled`, `fechaCreacion`
- **Sortable columns**: ID, Usuario, Nombre, Apellidos, DNI, Email, Habilitado, Fecha Creación

## EnhancedDataTable Integration

The `EnhancedDataTable` component automatically handles:

- **Sort button rendering** for sortable columns
- **Sort direction indicators** (↑/↓ arrows)
- **Click handling** for sort changes
- **Visual feedback** for current sort state

```svelte
<EnhancedDataTable
    {loading}
    {paginatedData}
    {currentPagination}
    {authStore}
    {columns}
    {actions}
    entityName="alumno"
    entityNamePlural="alumnos"
    theme="modern"
    showRowNumbers={true}
    on:changeSorting={(e) => changeSorting(e.detail)}
/>
```

## URL-Based State Management (Profesores)

The profesores page uses URL-based state management for advanced features:

### +page.ts Load Function

```typescript
export const load: PageLoad = ({ url }: { url: URL }) => {
    const sortBy = url.searchParams.get('sortBy') ?? 'id';
    const sortDirection = (url.searchParams.get('sortDirection') ?? 'ASC').toUpperCase() as 'ASC' | 'DESC';
    
    return {
        pagination: {
            page,
            size,
            sortBy,
            sortDirection
        },
        // ... other data
    };
};
```

### URL Update Functions

```typescript
function updateUrl(params: Record<string, string | number | boolean | undefined>) {
    const url = new URL(currentUrl);
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        } else {
            url.searchParams.delete(key);
        }
    });
    goto(url.toString());
}

function changeSorting(newSortBy: string | { value: string; direction: 'ASC' | 'DESC' }) {
    if (typeof newSortBy === 'string') {
        const newDirection = currentPagination.sortBy === newSortBy
            ? currentPagination.sortDirection === 'ASC' ? 'DESC' : 'ASC'
            : 'ASC';
        updateUrl({ sortBy: newSortBy, sortDirection: newDirection, page: 0 });
    } else {
        updateUrl({ sortBy: newSortBy.value, sortDirection: newSortBy.direction, page: 0 });
    }
}
```

## Benefits

1. **Server-side efficiency**: Sorting is handled by the database, not the client
2. **Consistent data**: All users see the same sorted data
3. **Performance**: Only requested data is transferred
4. **Scalability**: Works efficiently with large datasets
5. **Bookmarkable URLs**: Sort state can be shared and bookmarked
6. **Default ordering**: ID-based sorting ensures consistent initial state

## Best Practices

1. **Always start with ID sorting**: Provides consistent initial ordering
2. **Reset to page 0**: When changing sort criteria, reset to first page
3. **Use appropriate field names**: Match API field names exactly
4. **Handle loading states**: Show loading indicators during sort changes
5. **Provide visual feedback**: Use arrows and styling to indicate sort state
6. **Support both patterns**: String-based toggle and object-based explicit direction

## Migration Guide

### From Client-side to Server-side Sorting

1. **Add sorting state**:
   ```typescript
   let sortBy = $state('id');
   let sortDirection = $state<'ASC' | 'DESC'>('ASC');
   ```

2. **Update API calls**:
   ```typescript
   const params = {
       page: currentPage,
       size: pageSize,
       sortBy: sortBy,
       sortDirection: sortDirection
   };
   ```

3. **Implement changeSorting function**:
   ```typescript
   function changeSorting(field: string | { value: string; direction: 'ASC' | 'DESC' }) {
       // Implementation as shown above
   }
   ```

4. **Update column configurations**:
   ```typescript
   const tableColumns = createColumns({
       id: { ...commonColumns.entity.id, sortable: true },
       // ... other columns
   });
   ```

This sorting system provides a robust, scalable, and user-friendly way to handle data sorting across all entity tables in the application.
