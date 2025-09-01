# Profile Page Hierarchy Standards

## 📋 Overview

This document defines the consistent hierarchy standards for all profile pages in the Academia App, ensuring a unified user experience across student and professor profiles.

## 🎯 Design Principles

- **Consistency**: All profile pages follow the same visual hierarchy and component patterns
- **Accessibility**: Proper contrast ratios, focus states, and semantic HTML
- **Responsiveness**: Mobile-first design with progressive enhancement
- **Performance**: Optimized components with minimal re-renders

---

## 🏗️ Page Structure

### Main Container
```svelte
<!-- Main Container with Gradient Background -->
<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
    <div class="mx-auto max-w-4xl">
      <!-- Content goes here -->
    </div>
  </div>
</div>
```

### Standard Layout
1. **Header Section** - Page title, subtitle, and navigation
2. **Messages Section** - Success/error notifications
3. **Loading State** - While data is being fetched
4. **Content Sections** - Profile information, actions, and data
5. **Empty State** - When no data is available

---

## 🧩 Reusable Components

### ProfileHeader
Consistent header component for all profile pages.

```svelte
<script>
  import { ProfileHeader } from '$lib/components/common';
</script>

<ProfileHeader
  title="Perfil de Alumno"
  subtitle="Información personal, clases inscritas y entregas"
  backUrl="/alumnos"
  backText="Volver a Alumnos"
  actions={[
    {
      label: "Editar",
      onClick: () => startEdit(),
      variant: "primary",
      icon: "✏️"
    }
  ]}
/>
```

**Props:**
- `title: string` - Main page title
- `subtitle: string` - Page description
- `backUrl: string` - URL for back navigation
- `backText: string` - Back button text (default: "Volver")
- `showBackButton: boolean` - Show/hide back button (default: true)
- `actions: Array` - Action buttons array

### ProfileCard
Consistent card component for profile sections.

```svelte
<script>
  import { ProfileCard } from '$lib/components/common';
</script>

<ProfileCard
  title="Información Personal"
  subtitle="Datos básicos del usuario"
  loading={loading}
  empty={!hasData}
  emptyIcon="👤"
  emptyTitle="No hay información disponible"
  emptyDescription="No se encontraron datos para mostrar."
  actions={[
    {
      label: "Editar",
      onClick: () => startEdit(),
      variant: "primary"
    }
  ]}
>
  <!-- Card content goes here -->
</ProfileCard>
```

**Props:**
- `title: string` - Card title
- `subtitle: string` - Card subtitle (optional)
- `showHeader: boolean` - Show/hide header (default: true)
- `actions: Array` - Action buttons
- `loading: boolean` - Show loading state
- `empty: boolean` - Show empty state
- `emptyIcon: string` - Empty state icon
- `emptyTitle: string` - Empty state title
- `emptyDescription: string` - Empty state description
- `emptyAction: Object` - Empty state action button

### ProfileInfoGrid
Grid component for displaying information consistently.

```svelte
<script>
  import { ProfileInfoGrid } from '$lib/components/common';
</script>

<ProfileInfoGrid
  title="Información Personal"
  columns={2}
  items={[
    {
      label: "Nombre Completo",
      value: `${user.firstName} ${user.lastName}`,
      type: "text"
    },
    {
      label: "Email",
      value: user.email,
      type: "email"
    },
    {
      label: "Estado",
      value: user.enabled,
      type: "status",
      status: user.enabled ? "success" : "error"
    },
    {
      label: "Fecha de Registro",
      value: user.createdAt,
      type: "date"
    }
  ]}
/>
```

**Props:**
- `title: string` - Section title (optional)
- `columns: 1 | 2` - Number of columns (default: 2)
- `items: Array` - Information items array

**Item Types:**
- `text` - Plain text
- `email` - Clickable email link
- `phone` - Clickable phone link
- `date` - Formatted date
- `status` - Status badge
- `badge` - Custom badge

### ProfileForm
Consistent form component for profile editing.

```svelte
<script>
  import { ProfileForm } from '$lib/components/common';
</script>

<ProfileForm
  onSubmit={saveChanges}
  onCancel={cancelEdit}
  loading={saving}
  valid={isFormValid()}
  submitText="Guardar Cambios"
  cancelText="Cancelar"
>
  <!-- Form fields go here -->
</ProfileForm>
```

**Props:**
- `onSubmit: Function` - Form submission handler
- `onCancel: Function` - Cancel action handler
- `loading: boolean` - Show loading state
- `valid: boolean` - Form validation state
- `submitText: string` - Submit button text
- `cancelText: string` - Cancel button text
- `showCancel: boolean` - Show/hide cancel button

### ProfileInput
Consistent input component with validation.

```svelte
<script>
  import { ProfileInput } from '$lib/components/common';
</script>

<ProfileInput
  id="firstName"
  label="Nombre"
  bind:value={editForm.firstName}
  type="text"
  placeholder="Ej: Juan Carlos"
  required={true}
  maxlength={100}
  validation={validateName(editForm.firstName)}
  helpText="Solo letras, acentos y espacios. Máximo 100 caracteres."
/>
```

**Props:**
- `id: string` - Input ID
- `label: string` - Field label
- `value: string` - Input value
- `type: string` - Input type (text, email, tel, etc.)
- `placeholder: string` - Placeholder text
- `required: boolean` - Required field
- `disabled: boolean` - Disabled state
- `maxlength: number` - Maximum length
- `validation: Object` - Validation result
- `helpText: string` - Help text
- `showValidation: boolean` - Show validation feedback

---

## 🎨 Typography Hierarchy

### Page Titles
```css
text-2xl sm:text-3xl lg:text-4xl font-bold montserrat-bold
bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent
```

### Section Titles
```css
text-xl font-semibold montserrat-semibold text-gray-900
```

### Card Titles
```css
text-lg font-semibold montserrat-semibold text-gray-900
```

### Field Labels
```css
text-sm font-medium text-gray-700
```

### Field Values
```css
text-gray-900 (primary)
text-gray-600 (secondary)
```

---

## 🎨 Color System

### Status Colors
- **Success**: `bg-green-100 text-green-800`
- **Warning**: `bg-yellow-100 text-yellow-800`
- **Error**: `bg-red-100 text-red-800`
- **Info**: `bg-blue-100 text-blue-800`

### Interactive Elements
- **Primary**: `bg-gradient-to-r from-blue-600 to-indigo-600`
- **Secondary**: `bg-gray-100 text-gray-700`
- **Danger**: `bg-gradient-to-r from-red-600 to-red-700`

---

## 📱 Responsive Design

### Mobile First Approach
```css
/* Mobile (320px - 639px) */
text-sm, p-4, space-y-4

/* Tablet (640px+) */
sm:text-base, sm:p-6, sm:space-y-6

/* Desktop (768px+) */
md:text-lg, md:p-8, md:space-y-8

/* Large Desktop (1024px+) */
lg:text-xl, lg:p-12, lg:space-y-12
```

### Grid Layouts
```css
/* Information Grid */
grid grid-cols-1 gap-6 md:grid-cols-2

/* Card Grid */
grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3
```

---

## 🔄 State Management

### Loading States
```svelte
{#if loading}
  <div class="py-12 text-center">
    <div class="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600"></div>
    <p class="mt-4 text-lg font-medium text-gray-600">Cargando...</p>
  </div>
{/if}
```

### Empty States
```svelte
{#if empty}
  <div class="rounded-xl border border-gray-200 bg-gray-50 p-8 text-center">
    <div class="mb-4 text-6xl">{icon}</div>
    <h3 class="mb-2 text-lg font-medium text-gray-900">{title}</h3>
    <p class="mb-4 text-gray-600">{description}</p>
    {#if action}
      <button onclick={action.onClick} class="btn-primary">{action.label}</button>
    {/if}
  </div>
{/if}
```

### Error States
```svelte
{#if error}
  <div class="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
    <div class="flex items-center">
      <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
        <!-- Error icon -->
      </svg>
      {error}
    </div>
  </div>
{/if}
```

---

## 🎯 Implementation Checklist

### For New Profile Pages
1. ✅ Use `ProfileHeader` component for consistent headers
2. ✅ Use `ProfileCard` component for content sections
3. ✅ Use `ProfileInfoGrid` for information display
4. ✅ Use `ProfileForm` and `ProfileInput` for editing
5. ✅ Follow typography hierarchy standards
6. ✅ Implement proper loading and empty states
7. ✅ Use consistent color system
8. ✅ Ensure responsive design
9. ✅ Add proper accessibility attributes
10. ✅ Test on different screen sizes

### For Existing Profile Pages
1. ✅ Update to use new reusable components
2. ✅ Apply consistent typography hierarchy
3. ✅ Standardize color usage
4. ✅ Improve responsive behavior
5. ✅ Enhance accessibility
6. ✅ Add proper loading states
7. ✅ Implement empty states
8. ✅ Test cross-browser compatibility

---

## 📚 Examples

### Student Profile Page
```svelte
<script>
  import { ProfileHeader, ProfileCard, ProfileInfoGrid } from '$lib/components/common';
</script>

<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
    <div class="mx-auto max-w-4xl">
      <ProfileHeader
        title="Perfil de Alumno"
        subtitle="Información personal, clases inscritas y entregas"
        backUrl="/alumnos"
        actions={[
          {
            label: "Editar",
            onClick: startEdit,
            variant: "primary"
          }
        ]}
      />

      <div class="space-y-8">
        <ProfileCard title="Información Personal">
          <ProfileInfoGrid
            items={personalInfoItems}
            columns={2}
          />
        </ProfileCard>

        <ProfileCard 
          title="Clases Inscritas"
          loading={loadingClasses}
          empty={enrolledClasses.length === 0}
          emptyIcon="📚"
          emptyTitle="No tienes clases inscritas"
          emptyDescription="Explora nuestras clases disponibles y comienza tu aprendizaje."
          emptyAction={{
            label: "Explorar Clases",
            onClick: () => goto('/clases')
          }}
        >
          <!-- Classes grid content -->
        </ProfileCard>
      </div>
    </div>
  </div>
</div>
```

### Professor Profile Page
```svelte
<script>
  import { ProfileHeader, ProfileCard, ProfileInfoGrid } from '$lib/components/common';
</script>

<div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50">
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
    <div class="mx-auto max-w-4xl">
      <ProfileHeader
        title="Perfil de Profesor"
        subtitle="Gestiona tu información personal y configuración de cuenta"
        backUrl="/profesores"
        actions={[
          {
            label: "Editar",
            onClick: startEdit,
            variant: "primary"
          }
        ]}
      />

      <div class="space-y-8">
        <ProfileCard title="Información Personal">
          <ProfileInfoGrid
            items={personalInfoItems}
            columns={2}
          />
        </ProfileCard>

        <ProfileCard 
          title="Acciones de Administrador"
          showHeader={canChangeStatus()}
        >
          <!-- Admin actions -->
        </ProfileCard>
      </div>
    </div>
  </div>
</div>
```

---

## 🔧 Maintenance

### Updating Components
1. **Backward Compatibility**: Ensure changes don't break existing implementations
2. **Documentation**: Update this guide when components change
3. **Testing**: Test components across different use cases
4. **Performance**: Monitor component performance and optimize as needed

### Best Practices
1. **Consistency**: Always use the established patterns
2. **Accessibility**: Maintain WCAG compliance
3. **Performance**: Optimize for fast loading
4. **Maintainability**: Keep components simple and focused
5. **Reusability**: Design components for multiple use cases

---

*This document should be updated whenever new patterns emerge or existing components are modified.*

