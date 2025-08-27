# Separation of Concerns Optimization Plan

## 📋 Overview

This document outlines the comprehensive plan to optimize separation of concerns in the frontend application by moving business logic from components to appropriate service layers and utility classes.

## 🎯 Goals

- **Reduce code duplication** by ~650 lines across the application
- **Improve maintainability** by centralizing business logic
- **Enhance testability** by isolating logic from UI components
- **Ensure consistency** in formatting, validation, and error handling
- **Follow single responsibility principle** - components focus on UI, services handle business logic

---

## 📊 Current Issues Analysis

### 🔴 Critical Issues (Priority 1)

#### 1. Formatting Functions Duplication
**Impact:** ~200 lines of duplicate code across 8+ files

**Files Affected:**
- `routes/entregas/+page.svelte`
- `routes/ejercicios/[id]/+page.svelte`
- `routes/clases/[id]/+page.svelte`
- `routes/pagos/admin/+page.svelte`
- `routes/pagos/my-payments/+page.svelte`
- `routes/pagos/[id]/+page.svelte`
- `routes/alumnos/perfil/+page.svelte`
- `routes/entregas/[id]/+page.svelte`

**Duplicated Functions:**
- `formatDate()` - 6+ variations
- `formatStatus()` - 4+ variations
- `getStatusColor()` - 4+ variations
- `formatGrade()` - 3+ variations
- `getGradeColor()` - 3+ variations
- `formatPrice()` - 2+ variations
- `formatAmount()` - 2+ variations

#### 2. Validation Logic Duplication
**Impact:** ~150 lines of duplicate code across 4 files

**Files Affected:**
- `routes/alumnos/[id]/+page.svelte`
- `routes/profesores/[id]/+page.svelte`
- `routes/alumnos/nuevo/+page.svelte`
- `routes/profesores/nuevo/+page.svelte`

**Duplicated Functions:**
- `validateName()` - 50+ lines each
- `validateDNI()` - 30+ lines with Spanish DNI algorithm
- `validateEmail()` - 25+ lines with RFC compliance
- `validatePhoneNumber()` - 20+ lines with international format support

#### 3. Business Logic in Components
**Impact:** ~100 lines of business logic mixed with UI

**Files Affected:**
- `routes/entregas/[id]/+page.svelte` - `handleGradeSubmit()`
- `routes/clases/[id]/+page.svelte` - `handleUnenrollStudent()`
- `routes/alumnos/[id]/+page.svelte` - `toggleAccountStatus()`
- `routes/profesores/+page.svelte` - `toggleAccountStatus()`

---

## 🛠️ Implementation Plan

### Phase 1: Critical Optimizations (Week 1)

#### Step 1.1: Create Formatter Utilities

**File:** `frontend/src/lib/utils/formatters.ts`

```typescript
export interface DateFormatOptions {
  includeTime?: boolean;
  includeSeconds?: boolean;
  format?: 'short' | 'long' | 'full';
}

export interface StatusConfig {
  delivery: Record<string, { text: string; color: string }>;
  exercise: Record<string, { text: string; color: string }>;
  payment: Record<string, { text: string; color: string }>;
}

export class FormatterUtils {
  private static readonly STATUS_CONFIG: StatusConfig = {
    delivery: {
      PENDIENTE: { text: 'Pendiente', color: 'bg-yellow-100 text-yellow-800' },
      ENTREGADO: { text: 'Entregado', color: 'bg-blue-100 text-blue-800' },
      CALIFICADO: { text: 'Calificado', color: 'bg-green-100 text-green-800' }
    },
    exercise: {
      ACTIVE: { text: 'Activo', color: 'bg-green-100 text-green-800' },
      EXPIRED: { text: 'Vencido', color: 'bg-red-100 text-red-800' },
      FUTURE: { text: 'Futuro', color: 'bg-blue-100 text-blue-800' },
      WITH_DELIVERIES: { text: 'Con entregas', color: 'bg-purple-100 text-purple-800' },
      WITHOUT_DELIVERIES: { text: 'Sin entregas', color: 'bg-yellow-100 text-yellow-800' }
    },
    payment: {
      EXITO: { text: 'Success', color: 'bg-green-100 text-green-800' },
      PENDIENTE: { text: 'Pending', color: 'bg-yellow-100 text-yellow-800' },
      PROCESANDO: { text: 'Processing', color: 'bg-blue-100 text-blue-800' },
      ERROR: { text: 'Failed', color: 'bg-red-100 text-red-800' },
      REEMBOLSADO: { text: 'Refunded', color: 'bg-purple-100 text-purple-800' }
    }
  };

  static formatDate(date: Date | string | undefined, options: DateFormatOptions = {}): string {
    if (!date) return 'N/A';
    
    const dateObj = new Date(date);
    const { includeTime = false, includeSeconds = false, format = 'short' } = options;
    
    const dateOptions: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: format === 'short' ? 'short' : 'long',
      day: 'numeric'
    };

    if (includeTime) {
      dateOptions.hour = '2-digit';
      dateOptions.minute = '2-digit';
      if (includeSeconds) {
        dateOptions.second = '2-digit';
      }
    }

    return dateObj.toLocaleDateString('es-ES', dateOptions);
  }

  static formatStatus(status: string | undefined, type: keyof StatusConfig): string {
    if (!status) return 'N/A';
    return this.STATUS_CONFIG[type][status]?.text || status;
  }

  static getStatusColor(status: string | undefined, type: keyof StatusConfig): string {
    if (!status) return 'bg-gray-100 text-gray-800';
    return this.STATUS_CONFIG[type][status]?.color || 'bg-gray-100 text-gray-800';
  }

  static formatGrade(nota: number | undefined): string {
    if (nota === undefined || nota === null) return 'N/A';
    return nota.toFixed(1);
  }

  static getGradeColor(nota: number | undefined): string {
    if (nota === undefined || nota === null) return 'text-gray-500';
    if (nota >= 9) return 'text-green-600 font-bold';
    if (nota >= 7) return 'text-blue-600 font-semibold';
    if (nota >= 5) return 'text-yellow-600 font-semibold';
    return 'text-red-600 font-semibold';
  }

  static formatPrice(precio: number | undefined): string {
    if (precio === undefined || precio === null) return 'N/A';
    return `€${precio.toFixed(2)}`;
  }

  static formatAmount(amount: number | undefined): string {
    if (amount === undefined || amount === null) return '€0.00';
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  }

  static getNivelColor(nivel: string | undefined): string {
    switch (nivel) {
      case 'PRINCIPIANTE': return 'bg-green-100 text-green-800';
      case 'INTERMEDIO': return 'bg-yellow-100 text-yellow-800';
      case 'AVANZADO': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  static getNivelText(nivel: string | undefined): string {
    switch (nivel) {
      case 'PRINCIPIANTE': return 'Principiante';
      case 'INTERMEDIO': return 'Intermedio';
      case 'AVANZADO': return 'Avanzado';
      default: return 'N/A';
    }
  }

  static getPresencialidadColor(presencialidad: string | undefined): string {
    switch (presencialidad) {
      case 'ONLINE': return 'bg-blue-100 text-blue-800';
      case 'PRESENCIAL': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  static getPresencialidadText(presencialidad: string | undefined): string {
    switch (presencialidad) {
      case 'ONLINE': return 'Online';
      case 'PRESENCIAL': return 'Presencial';
      default: return 'N/A';
    }
  }
}
```

#### Step 1.2: Create Validation Utilities

**File:** `frontend/src/lib/utils/validators.ts`

```typescript
export interface ValidationResult {
  isValid: boolean;
  message: string;
}

export interface ValidationSchema {
  [key: string]: {
    required?: boolean;
    minLength?: number;
    maxLength?: number;
    pattern?: RegExp;
    custom?: (value: any) => ValidationResult;
  };
}

export class ValidationUtils {
  static validateName(name: string): ValidationResult {
    if (!name || name.trim().length === 0) {
      return { isValid: false, message: 'Este campo es obligatorio' };
    }
    if (name.length > 100) {
      return { isValid: false, message: 'Máximo 100 caracteres' };
    }
    const nameRegex = /^[a-zA-ZáéíóúñÁÉÍÓÚÑ\s]+$/;
    if (!nameRegex.test(name)) {
      return { isValid: false, message: 'Solo se permiten letras, acentos y espacios' };
    }
    return { isValid: true, message: '✓ Válido' };
  }

  static validateDNI(dni: string): ValidationResult {
    if (!dni || dni.trim().length === 0) {
      return { isValid: false, message: 'El DNI es obligatorio' };
    }

    const dniRegex = /^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$/i;
    if (!dniRegex.test(dni)) {
      return { isValid: false, message: 'Formato: 8 números + 1 letra (ej: 12345678Z)' };
    }

    // Calcular letra correcta
    const numbers = dni.substring(0, 8);
    const letter = dni.substring(8, 9).toUpperCase();
    const correctLetters = 'TRWAGMYFPDXBNJZSQVHLCKE';
    const correctLetter = correctLetters[parseInt(numbers) % 23];

    if (letter !== correctLetter) {
      return { isValid: false, message: `La letra debería ser ${correctLetter}` };
    }

    return { isValid: true, message: '✓ DNI válido' };
  }

  static validateEmail(email: string): ValidationResult {
    if (!email || email.trim().length === 0) {
      return { isValid: false, message: 'El email es obligatorio' };
    }

    if (email.length > 254) {
      return { isValid: false, message: 'Máximo 254 caracteres' };
    }

    const [localPart] = email.split('@');
    if (localPart && localPart.length > 64) {
      return { isValid: false, message: 'La parte local no puede superar 64 caracteres' };
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return { isValid: false, message: 'Formato de email inválido' };
    }

    if (email.includes('..')) {
      return { isValid: false, message: 'No se permiten puntos consecutivos' };
    }

    return { isValid: true, message: '✓ Email válido' };
  }

  static validatePhoneNumber(phone: string): ValidationResult {
    if (!phone || phone.trim().length === 0) {
      return { isValid: true, message: 'Campo opcional' };
    }

    const allowedCharsRegex = /^[0-9+\-\s().]+$/;
    if (!allowedCharsRegex.test(phone)) {
      return { isValid: false, message: 'Solo números, espacios, guiones, puntos, paréntesis y +' };
    }

    const digits = phone.replace(/[^0-9]/g, '');

    if (digits.length < 6) {
      return { isValid: false, message: 'Mínimo 6 dígitos' };
    }

    if (digits.length > 14) {
      return { isValid: false, message: 'Máximo 14 dígitos' };
    }

    return { isValid: true, message: '✓ Teléfono válido' };
  }

  static validateUsername(username: string): ValidationResult {
    if (!username || username.trim().length === 0) {
      return { isValid: false, message: 'El usuario es obligatorio' };
    }
    if (username.length < 3) {
      return { isValid: false, message: 'Mínimo 3 caracteres' };
    }
    if (username.length > 50) {
      return { isValid: false, message: 'Máximo 50 caracteres' };
    }
    return { isValid: true, message: '✓ Usuario válido' };
  }

  static validatePassword(password: string): ValidationResult {
    if (!password || password.trim().length === 0) {
      return { isValid: false, message: 'La contraseña es obligatoria' };
    }
    if (password.length < 6) {
      return { isValid: false, message: 'Mínimo 6 caracteres' };
    }
    return { isValid: true, message: '✓ Contraseña válida' };
  }

  static validateFormData(data: any, schema: ValidationSchema): ValidationResult[] {
    const results: ValidationResult[] = [];
    
    for (const [field, rules] of Object.entries(schema)) {
      const value = data[field];
      
      if (rules.required && (!value || value.trim().length === 0)) {
        results.push({ isValid: false, message: `${field} es obligatorio` });
        continue;
      }
      
      if (value && rules.minLength && value.length < rules.minLength) {
        results.push({ isValid: false, message: `${field} debe tener al menos ${rules.minLength} caracteres` });
      }
      
      if (value && rules.maxLength && value.length > rules.maxLength) {
        results.push({ isValid: false, message: `${field} no puede superar ${rules.maxLength} caracteres` });
      }
      
      if (value && rules.pattern && !rules.pattern.test(value)) {
        results.push({ isValid: false, message: `${field} tiene un formato inválido` });
      }
      
      if (value && rules.custom) {
        const customResult = rules.custom(value);
        if (!customResult.isValid) {
          results.push(customResult);
        }
      }
    }
    
    return results;
  }
}
```

#### Step 1.3: Update Components to Use Formatters

**Example Migration for `routes/entregas/+page.svelte`:**

```typescript
// Before
function formatDate(date: Date | string | undefined): string {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function formatStatus(status: string | undefined): string {
  if (!status) return 'N/A';
  const statusMap: Record<string, string> = {
    PENDIENTE: 'Pendiente',
    ENTREGADO: 'Entregado',
    CALIFICADO: 'Calificado'
  };
  return statusMap[status] || status;
}

// After
import { FormatterUtils } from '$lib/utils/formatters';

// In template:
{FormatterUtils.formatDate(entrega.fechaEntrega, { includeTime: true })}
{FormatterUtils.formatStatus(entrega.estado, 'delivery')}
{FormatterUtils.getStatusColor(entrega.estado, 'delivery')}
```

### Phase 2: High Priority Optimizations (Week 2)

#### Step 2.1: Extend Services with Business Logic

**File:** `frontend/src/lib/services/entregaService.ts` (extend existing)

```typescript
export class EntregaService {
  // ... existing methods ...

  static async handleGrading(
    entregaId: number, 
    gradeData: { nota: number; comentarios?: string }
  ): Promise<{ success: boolean; message: string }> {
    try {
      // Validate grade data
      if (gradeData.nota < 0 || gradeData.nota > 10) {
        throw new Error('La nota debe estar entre 0 y 10');
      }

      if (gradeData.comentarios && gradeData.comentarios.length > 1000) {
        throw new Error('Los comentarios no pueden exceder 1000 caracteres');
      }

      // Update delivery
      await this.updateEntrega(entregaId, gradeData);

      return {
        success: true,
        message: 'Entrega calificada exitosamente'
      };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : 'Error al calificar la entrega'
      };
    }
  }
}
```

**File:** `frontend/src/lib/services/alumnoService.ts` (extend existing)

```typescript
export class AlumnoService {
  // ... existing methods ...

  static async handleAccountStatusChange(
    alumnoId: number, 
    newStatus: boolean
  ): Promise<{ success: boolean; message: string; updatedAlumno?: DTOAlumno }> {
    try {
      const updatedAlumno = await this.toggleEnabled(alumnoId, newStatus);
      
      return {
        success: true,
        message: `Cuenta ${newStatus ? 'habilitada' : 'deshabilitada'} correctamente`,
        updatedAlumno
      };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : 'Error al cambiar estado de cuenta'
      };
    }
  }
}
```

#### Step 2.2: Create Navigation Utilities

**File:** `frontend/src/lib/utils/navigation.ts`

```typescript
import { goto } from '$app/navigation';

export class NavigationUtils {
  static goToEntity(entityType: string, id: number): void {
    goto(`/${entityType}/${id}`);
  }

  static goToEntityEdit(entityType: string, id: number): void {
    goto(`/${entityType}/${id}/editar`);
  }

  static goToPayment(classId: number, amount: number, description: string): void {
    const url = `/payment?classId=${classId}&amount=${amount}&description=${encodeURIComponent(description)}`;
    goto(url);
  }

  static goToPaymentSuccess(paymentId: number, classId?: number): void {
    let url = `/payment-success?payment_id=${paymentId}`;
    if (classId) {
      url += `&classId=${classId}`;
    }
    goto(url);
  }

  static goBack(fallbackUrl: string): void {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      goto(fallbackUrl);
    }
  }

  static goToEntityList(entityType: string): void {
    goto(`/${entityType}`);
  }

  static goToEntityCreate(entityType: string): void {
    goto(`/${entityType}/nuevo`);
  }
}
```

### Phase 3: Medium Priority Optimizations (Week 3)

#### Step 3.1: Create Error Handling Utilities

**File:** `frontend/src/lib/utils/errorHandler.ts`

```typescript
export interface ErrorContext {
  operation: string;
  entity?: string;
  id?: number | string;
}

export class ErrorHandler {
  static formatErrorMessage(error: unknown, context: ErrorContext): string {
    const { operation, entity, id } = context;
    
    if (error instanceof Error) {
      return `Error al ${operation}${entity ? ` ${entity}` : ''}${id ? ` (ID: ${id})` : ''}: ${error.message}`;
    }
    
    return `Error inesperado al ${operation}${entity ? ` ${entity}` : ''}`;
  }

  static handleApiError(error: unknown, fallbackMessage: string): string {
    if (error instanceof Error) {
      return error.message;
    }
    
    if (typeof error === 'string') {
      return error;
    }
    
    return fallbackMessage;
  }

  static isNetworkError(error: unknown): boolean {
    if (error instanceof Error) {
      return error.message.includes('fetch') || 
             error.message.includes('network') || 
             error.message.includes('connection');
    }
    return false;
  }

  static isValidationError(error: unknown): boolean {
    if (error instanceof Error) {
      return error.message.includes('validation') || 
             error.message.includes('invalid') ||
             error.message.includes('required');
    }
    return false;
  }

  static isAuthError(error: unknown): boolean {
    if (error instanceof Error) {
      return error.message.includes('unauthorized') || 
             error.message.includes('forbidden') ||
             error.message.includes('401') ||
             error.message.includes('403');
    }
    return false;
  }
}
```

#### Step 3.2: Create Permission Utilities

**File:** `frontend/src/lib/utils/permissions.ts`

```typescript
import type { User } from '$lib/generated/api';

export interface Action {
  id: string;
  label: string;
  icon?: string;
  color?: string;
  condition: (user: User, entity?: any) => boolean;
}

export class PermissionUtils {
  static canEditEntity(entityType: string, user: User): boolean {
    return user.roles.includes('ADMIN') || 
           (entityType === 'profesores' && user.roles.includes('PROFESOR'));
  }

  static canDeleteEntity(entityType: string, user: User): boolean {
    return user.roles.includes('ADMIN');
  }

  static canGradeDelivery(user: User): boolean {
    return user.roles.includes('ADMIN') || user.roles.includes('PROFESOR');
  }

  static canEnrollInClass(user: User): boolean {
    return user.roles.includes('ALUMNO');
  }

  static canManagePayments(user: User): boolean {
    return user.roles.includes('ADMIN');
  }

  static getVisibleActions(entityType: string, user: User, entity?: any): Action[] {
    const actions: Action[] = [
      {
        id: 'view',
        label: 'Ver',
        icon: '👁️',
        color: 'blue',
        condition: () => true
      }
    ];

    if (this.canEditEntity(entityType, user)) {
      actions.push({
        id: 'edit',
        label: 'Editar',
        icon: '✏️',
        color: 'green',
        condition: () => true
      });
    }

    if (this.canDeleteEntity(entityType, user)) {
      actions.push({
        id: 'delete',
        label: 'Eliminar',
        icon: '🗑️',
        color: 'red',
        condition: () => true
      });
    }

    return actions.filter(action => action.condition(user, entity));
  }

  static canAccessRoute(route: string, user: User): boolean {
    const routePermissions: Record<string, string[]> = {
      '/admin': ['ADMIN'],
      '/profesores': ['ADMIN', 'PROFESOR'],
      '/alumnos': ['ADMIN', 'PROFESOR'],
      '/pagos/admin': ['ADMIN'],
      '/clases/nuevo': ['ADMIN', 'PROFESOR']
    };

    const requiredRoles = routePermissions[route];
    if (!requiredRoles) return true;

    return requiredRoles.some(role => user.roles.includes(role));
  }
}
```

---

## 📋 Migration Checklist

### Phase 1 Checklist
- [ ] Create `FormatterUtils` class
- [ ] Create `ValidationUtils` class
- [ ] Update `routes/entregas/+page.svelte`
- [ ] Update `routes/ejercicios/[id]/+page.svelte`
- [ ] Update `routes/clases/[id]/+page.svelte`
- [ ] Update `routes/pagos/admin/+page.svelte`
- [ ] Update `routes/pagos/my-payments/+page.svelte`
- [ ] Update `routes/pagos/[id]/+page.svelte`
- [ ] Update `routes/alumnos/perfil/+page.svelte`
- [ ] Update `routes/entregas/[id]/+page.svelte`
- [ ] Update `routes/alumnos/[id]/+page.svelte`
- [ ] Update `routes/profesores/[id]/+page.svelte`
- [ ] Update `routes/alumnos/nuevo/+page.svelte`
- [ ] Update `routes/profesores/nuevo/+page.svelte`

### Phase 2 Checklist
- [ ] Extend `EntregaService` with `handleGrading`
- [ ] Extend `AlumnoService` with `handleAccountStatusChange`
- [ ] Extend `ProfesorService` with `handleAccountStatusChange`
- [ ] Create `NavigationUtils` class
- [ ] Update all navigation calls in components
- [ ] Create `PaginationStore` for list pages
- [ ] Update list pages to use pagination store

### Phase 3 Checklist
- [ ] Create `ErrorHandler` utilities
- [ ] Create `PermissionUtils` class
- [ ] Update error handling across all components
- [ ] Update permission checks across all components
- [ ] Final testing and cleanup
- [ ] Update documentation

---

## 🧪 Testing Strategy

### Unit Tests
- Test all utility functions independently
- Test service business logic methods
- Test validation rules thoroughly

### Integration Tests
- Test component integration with utilities
- Test service integration with API calls
- Test navigation flows

### Manual Testing
- Verify all formatting displays correctly
- Verify validation works as expected
- Verify business logic flows work properly

---

## 📈 Success Metrics

### Code Quality Metrics
- **Lines of Code Reduced:** Target: 650+ lines
- **Code Duplication:** Target: 0% for formatting/validation
- **Cyclomatic Complexity:** Target: < 10 for all functions
- **Test Coverage:** Target: > 90% for utilities

### Maintainability Metrics
- **Time to Add New Formatter:** Target: < 30 minutes
- **Time to Add New Validation:** Target: < 1 hour
- **Time to Fix Formatting Bug:** Target: < 15 minutes
- **Number of Files to Change for Format Update:** Target: 1

---

## 🚀 Implementation Timeline

| Week | Phase | Tasks | Estimated Hours |
|------|-------|-------|-----------------|
| 1 | Critical | Formatters + Validators | 8-10 hours |
| 2 | High Priority | Business Logic + Navigation | 6-8 hours |
| 3 | Medium Priority | Error Handling + Permissions | 4-6 hours |
| 4 | Testing | Comprehensive testing | 4-6 hours |

**Total Estimated Time:** 22-30 hours

---

## 🔄 Maintenance Plan

### Regular Reviews
- Monthly review of utility functions for consistency
- Quarterly review of validation rules
- Bi-annual review of business logic placement

### Update Procedures
- All formatting changes go through `FormatterUtils`
- All validation changes go through `ValidationUtils`
- All business logic changes go through appropriate services

### Documentation Updates
- Keep this document updated with new patterns
- Maintain API documentation for utilities
- Update component migration guides