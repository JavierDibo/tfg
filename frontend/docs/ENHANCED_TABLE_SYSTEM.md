# Enhanced Table System

This document describes the enhanced table system that provides beautiful, consistent styling across all entity tables in the application.

## Overview

The enhanced table system consists of:

1. **EnhancedDataTable** - A beautiful, flexible table component with multiple themes
2. **tableUtils** - Utility functions for creating consistent column formatters and badges
3. **Common column configurations** - Pre-built column setups for different entity types

## Components

### EnhancedDataTable

The main table component with enhanced styling and features.

```svelte
<script>
import { EnhancedDataTable, createColumns, commonColumns } from '$lib/components/common';

// Create columns using utilities
const tableColumns = createColumns({
    id: commonColumns.student.id,
    name: commonColumns.student.name,
    email: commonColumns.student.email,
    enabled: commonColumns.student.enabled
});

// Define actions
const tableActions = [
    {
        label: 'Ver',
        color: 'blue',
        hoverColor: 'blue',
        action: (entity) => goto(`/entity/${entity.id}`),
        condition: () => true
    }
];
</script>

<EnhancedDataTable
    {loading}
    {paginatedData}
    {currentPagination}
    {authStore}
    {columns}
    {actions}
    entityName="student"
    entityNamePlural="students"
    theme="modern"
    showRowNumbers={true}
    stripedRows={true}
    hoverEffects={true}
/>
```

#### Props

- `loading` - Boolean indicating if data is loading
- `paginatedData` - Paginated data object with content array
- `currentPagination` - Current pagination state
- `authStore` - Authentication store
- `columns` - Array of column configurations
- `actions` - Array of action configurations
- `entityName` - Singular entity name for messages
- `entityNamePlural` - Plural entity name for messages
- `theme` - Theme: 'default', 'modern', or 'minimal'
- `showRowNumbers` - Show row numbers (default: false)
- `stripedRows` - Alternate row colors (default: true)
- `hoverEffects` - Enable hover effects (default: true)
- `compactMode` - Use compact spacing (default: false)
- `showMobileView` - Show mobile card view (default: true)

#### Themes

1. **Default** - Clean, professional styling with subtle gradients
2. **Modern** - Dark header with enhanced hover effects and scaling
3. **Minimal** - Simple, clean design with minimal styling

## Table Utilities

### Column Formatters

The `tableUtils` module provides formatters for common data types:

```typescript
import { 
    createEnabledBadge,
    createMatriculationBadge,
    createPriceFormatter,
    createDateFormatter,
    createNameFormatter,
    createTruncatedTextFormatter,
    createPhoneFormatter,
    createEmailFormatter,
    createDNIFormatter,
    createLevelBadge,
    createPresencialityBadge
} from '$lib/components/common/tableUtils';

// Status badges
const enabledColumn = {
    key: 'enabled',
    header: 'Estado',
    sortable: true,
    html: true,
    formatter: createEnabledBadge
};

// Price formatting
const priceColumn = {
    key: 'precio',
    header: 'Precio',
    sortable: true,
    formatter: createPriceFormatter('€')
};

// Date formatting
const dateColumn = {
    key: 'fechaCreacion',
    header: 'Fecha de Creación',
    sortable: true,
    formatter: createDateFormatter({
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
};

// Name combination
const nameColumn = {
    key: 'nombreCompleto',
    header: 'Nombre Completo',
    sortable: true,
    formatter: createNameFormatter('nombre', 'apellidos')
};
```

### Common Column Configurations

Pre-built column configurations for different entity types:

```typescript
import { commonColumns } from '$lib/components/common/tableUtils';

// Student columns
const studentColumns = createColumns({
    id: commonColumns.student.id,
    name: commonColumns.student.name,        // Uses 'nombre' field, displays full name
    email: commonColumns.student.email,
    dni: commonColumns.student.dni,
    enrollment: commonColumns.student.enrollment,
    enabled: commonColumns.student.enabled
});

// Teacher columns
const teacherColumns = createColumns({
    id: commonColumns.teacher.id,
    name: commonColumns.teacher.name,        // Uses 'nombre' field, displays full name
    email: commonColumns.teacher.email,
    dni: commonColumns.teacher.dni,
    enabled: commonColumns.teacher.enabled,
    createdAt: commonColumns.teacher.createdAt
});

// Class columns
const classColumns = createColumns({
    id: commonColumns.class.id,
    title: commonColumns.class.title,
    description: commonColumns.class.description,
    level: commonColumns.class.level,        // Uses enum: PRINCIPIANTE, INTERMEDIO, AVANZADO
    presenciality: commonColumns.class.presenciality, // Uses enum: PRESENCIAL, ONLINE
    price: commonColumns.class.price
});
```

## Action Configuration

Actions are configured with color schemes and conditions:

```typescript
const tableActions = [
    {
        label: 'Ver',
        color: 'blue',
        hoverColor: 'blue',
        action: (entity) => goto(`/entity/${entity.id}`),
        condition: () => true
    },
    {
        label: 'Editar',
        color: 'yellow',
        hoverColor: 'yellow',
        action: (entity) => goto(`/entity/${entity.id}/edit`),
        condition: (entity) => authStore.isAdmin
    },
    {
        label: 'Eliminar',
        color: 'red',
        hoverColor: 'red',
        action: (entity) => deleteEntity(entity),
        condition: (entity) => authStore.isAdmin && !entity.enabled
    }
];
```

### Dynamic Labels

Actions can have dynamic labels based on entity state:

```typescript
{
    label: 'Toggle Status',
    dynamicLabel: (entity) => entity.enabled ? 'Deshabilitar' : 'Habilitar',
    color: 'yellow',
    hoverColor: 'yellow',
    action: toggleStatus,
    condition: () => authStore.isAdmin
}
```

## Creating Custom Formatters

You can create custom formatters for specific needs:

```typescript
// Custom status badge
function createCustomStatusBadge(value: unknown): string {
    const status = String(value).toLowerCase();
    const statusMap = {
        'active': { label: 'Activo', color: 'green', bgColor: 'green' },
        'inactive': { label: 'Inactivo', color: 'red', bgColor: 'red' },
        'pending': { label: 'Pendiente', color: 'yellow', bgColor: 'yellow' }
    };
    
    return createStatusBadge(value, {}, statusMap);
}

// Custom column with formatter
const customColumn = {
    key: 'status',
    header: 'Estado',
    sortable: true,
    html: true,
    formatter: createCustomStatusBadge
};
```

## Migration Guide

### From EntityDataTable to EnhancedDataTable

1. **Import the new component:**
   ```typescript
   // Old
   import { EntityDataTable } from '$lib/components/common';
   
   // New
   import { EnhancedDataTable } from '$lib/components/common';
   ```

2. **Update the component usage:**
   ```svelte
   <!-- Old -->
   <EntityDataTable
       {loading}
       {paginatedData}
       {currentPagination}
       {authStore}
       {columns}
       {actions}
       entityName="student"
       entityNamePlural="students"
   />
   
   <!-- New -->
   <EnhancedDataTable
       {loading}
       {paginatedData}
       {currentPagination}
       {authStore}
       {columns}
       {actions}
       entityName="student"
       entityNamePlural="students"
       theme="modern"
       showRowNumbers={true}
   />
   ```

3. **Use table utilities for consistent styling:**
   ```typescript
   // Old - manual column definition
   const columns = [
       { key: 'nombre', header: 'Nombre', sortable: true },
       { key: 'enabled', header: 'Estado', sortable: true }
   ];
   
   // New - using utilities
   const columns = createColumns({
       name: commonColumns.student.name,
       enabled: commonColumns.student.enabled
   });
   ```

## Best Practices

1. **Use common column configurations** when possible for consistency
2. **Choose appropriate themes** based on the context (modern for admin, minimal for public)
3. **Use color-coded actions** for better UX (blue for view, yellow for edit, red for delete)
4. **Implement proper conditions** for actions based on user permissions
5. **Use dynamic labels** when action text depends on entity state
6. **Enable row numbers** for better data tracking in large tables
7. **Use appropriate formatters** for data types (dates, prices, status badges)

## Examples

### Complete Student Table Implementation

```svelte
<script lang="ts">
import { EnhancedDataTable, createColumns, commonColumns } from '$lib/components/common';

const tableColumns = createColumns({
    id: commonColumns.student.id,
    name: commonColumns.student.name,
    email: commonColumns.student.email,
    dni: commonColumns.student.dni,
    enrollment: commonColumns.student.enrollment,
    enabled: commonColumns.student.enabled
});

const tableActions = [
    {
        label: 'Ver',
        color: 'blue',
        hoverColor: 'blue',
        action: (student) => goto(`/alumnos/${student.id}`),
        condition: () => true
    },
    {
        label: 'Matricular',
        color: 'green',
        hoverColor: 'green',
        action: toggleEnrollment,
        condition: (student) => !student.matriculado && authStore.isAdmin
    },
    {
        label: 'Eliminar',
        color: 'red',
        hoverColor: 'red',
        action: deleteStudent,
        condition: () => authStore.isAdmin
    }
];
</script>

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
/>
```

This enhanced table system provides a beautiful, consistent, and maintainable way to display data across your application while supporting all the features you need for different entity types.
