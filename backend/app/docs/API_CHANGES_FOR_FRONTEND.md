# API Changes for Frontend Integration

## Overview

This document outlines all the API changes made during the refactoring process. These changes improve the RESTful design, eliminate redundancy, and provide a cleaner, more maintainable API structure.

## Summary of Changes

### ✅ **Removed Redundant Endpoints**
- Eliminated duplicate email/DNI endpoints in ProfesorRest
- Removed redundant enabled status endpoint in ProfesorRest
- Consolidated student enrollment information endpoints

### ✅ **Fixed Resource Hierarchy**
- Moved professor-by-class endpoint to correct location
- Improved RESTful URL structure

### ✅ **Simplified Complex Endpoints**
- Removed overly complex student-class information endpoint
- Used existing general endpoints for enrollment status

## Detailed Changes

### 1. ProfesorRest.java Changes

#### **Removed Endpoints:**

**❌ Before (Removed):**
```
GET /api/profesores/email/{email}
GET /api/profesores/dni/{dni}
PATCH /api/profesores/{id}/estado
GET /api/profesores/clase/{claseId}
```

**✅ After (Use These Instead):**
```
GET /api/profesores?email={email}
GET /api/profesores?dni={dni}
PATCH /api/profesores/{id}
GET /api/clases/{claseId}/profesores
```

#### **Frontend Impact:**

**1. Professor Search by Email/DNI:**
```javascript
// ❌ OLD WAY
const response = await fetch(`/api/profesores/email/${email}`);

// ✅ NEW WAY
const response = await fetch(`/api/profesores?email=${email}`);
```

**2. Professor Status Update:**
```javascript
// ❌ OLD WAY
const response = await fetch(`/api/profesores/${id}/estado`, {
  method: 'PATCH',
  body: JSON.stringify({ enabled: false })
});

// ✅ NEW WAY
const response = await fetch(`/api/profesores/${id}`, {
  method: 'PATCH',
  body: JSON.stringify({ enabled: false })
});
```

**3. Get Professors by Class:**
```javascript
// ❌ OLD WAY
const response = await fetch(`/api/profesores/clase/${claseId}`);

// ✅ NEW WAY
const response = await fetch(`/api/clases/${claseId}/profesores`);
```

### 2. ClaseRest.java Changes

#### **Added Endpoint:**

**✅ New Endpoint:**
```
GET /api/clases/{claseId}/profesores
```

#### **Frontend Impact:**

**Get Professors for a Class:**
```javascript
// ✅ NEW WAY
const response = await fetch(`/api/clases/${claseId}/profesores`);
const profesores = await response.json();
```

### 3. ClaseManagementRest.java Changes

#### **Removed Complex Endpoint:**

**❌ Before (Removed):**
```
GET /api/clases/{claseId}/students/{studentId}
GET /api/clases/{claseId}/students/{studentId}/details
```

**✅ After (Use These Instead):**
```
GET /api/clases/{claseId}/students/me
GET /api/clases/{claseId}/students/me/details
GET /api/clases/{claseId}
GET /api/alumnos/{studentId}/clases
```

#### **Frontend Impact:**

**1. Student Enrollment Status:**
```javascript
// ❌ OLD WAY (for specific student)
const response = await fetch(`/api/clases/${claseId}/students/${studentId}`);

// ✅ NEW WAY (for authenticated student)
const response = await fetch(`/api/clases/${claseId}/students/me`);
```

**2. Student Class Details:**
```javascript
// ❌ OLD WAY (for specific student)
const response = await fetch(`/api/clases/${claseId}/students/${studentId}/details`);

// ✅ NEW WAY (for authenticated student)
const response = await fetch(`/api/clases/${claseId}/students/me/details`);
```

**3. General Class Information:**
```javascript
// ✅ NEW WAY
const response = await fetch(`/api/clases/${claseId}`);
const clase = await response.json();
```

**4. Student's Enrolled Classes:**
```javascript
// ✅ NEW WAY
const response = await fetch(`/api/alumnos/${studentId}/clases`);
const clasesInscritas = await response.json();
```

## Migration Guide

### Step 1: Update Professor Search

**Find and Replace:**
```javascript
// Search for patterns like:
fetch(`/api/profesores/email/`)
fetch(`/api/profesores/dni/`)

// Replace with:
fetch(`/api/profesores?email=`)
fetch(`/api/profesores?dni=`)
```

### Step 2: Update Professor Status Management

**Find and Replace:**
```javascript
// Search for patterns like:
fetch(`/api/profesores/${id}/estado`, { method: 'PATCH' })

// Replace with:
fetch(`/api/profesores/${id}`, { method: 'PATCH' })
```

### Step 3: Update Professor-Class Relationships

**Find and Replace:**
```javascript
// Search for patterns like:
fetch(`/api/profesores/clase/${claseId}`)

// Replace with:
fetch(`/api/clases/${claseId}/profesores`)
```

### Step 4: Update Student Enrollment Queries

**Find and Replace:**
```javascript
// Search for patterns like:
fetch(`/api/clases/${claseId}/students/${studentId}`)
fetch(`/api/clases/${claseId}/students/${studentId}/details`)

// Replace with appropriate endpoint based on context:
// For authenticated student:
fetch(`/api/clases/${claseId}/students/me`)
fetch(`/api/clases/${claseId}/students/me/details`)

// For general class info:
fetch(`/api/clases/${claseId}`)

// For student's enrolled classes:
fetch(`/api/alumnos/${studentId}/clases`)
```

## Response Format Changes

### Professor Search Response

**Before (Single Professor):**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellidos": "Pérez",
  "email": "juan@example.com"
}
```

**After (Paginated Response):**
```json
{
  "content": [
    {
      "id": 1,
      "nombre": "Juan",
      "apellidos": "Pérez",
      "email": "juan@example.com"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Professor Status Update

**No Change in Response Format** - Both endpoints return the same `DTOProfesor` object.

### Professors by Class

**No Change in Response Format** - Both endpoints return the same `List<DTOProfesor>`.

## Testing Checklist

### Frontend Testing

1. **Professor Search:**
   - [ ] Search by email works with new endpoint
   - [ ] Search by DNI works with new endpoint
   - [ ] Pagination works correctly

2. **Professor Management:**
   - [ ] Enable/disable professor works with new endpoint
   - [ ] Professor updates work correctly

3. **Class-Professor Relationships:**
   - [ ] Get professors for a class works
   - [ ] Add/remove professors from class works

4. **Student Enrollment:**
   - [ ] Check own enrollment status works
   - [ ] Get own class details works
   - [ ] Get general class information works
   - [ ] Get enrolled classes works

### API Testing

1. **Verify Removed Endpoints Return 404:**
   - [ ] `/api/profesores/email/{email}`
   - [ ] `/api/profesores/dni/{dni}`
   - [ ] `/api/profesores/{id}/estado`
   - [ ] `/api/profesores/clase/{claseId}`

2. **Verify New Endpoints Work:**
   - [ ] `/api/profesores?email={email}`
   - [ ] `/api/profesores?dni={dni}`
   - [ ] `/api/profesores/{id}` (PATCH)
   - [ ] `/api/clases/{claseId}/profesores`

## Benefits of Changes

### 1. **Improved RESTful Design**
- URLs now follow proper resource hierarchy
- No verbs in URLs
- Consistent patterns across all endpoints

### 2. **Reduced Complexity**
- Fewer endpoints to maintain
- Clearer API structure
- Better separation of concerns

### 3. **Enhanced Flexibility**
- Query parameters for filtering
- Pagination support
- Consistent response formats

### 4. **Better Security**
- Proper role-based access control
- Consistent authentication patterns
- Clear permission boundaries

## Rollback Plan

If issues arise, you can temporarily revert to the old endpoints by:

1. **Restoring removed endpoints** in the backend
2. **Reverting frontend changes** to use old URLs
3. **Testing thoroughly** before re-implementing changes

## Support

For questions or issues with these changes:

1. Check the API documentation in Swagger UI
2. Review the backend logs for detailed error messages
3. Test endpoints individually to isolate issues
4. Contact the backend team for clarification

---

**Last Updated:** [Current Date]
**Version:** 1.0
**Status:** Ready for Frontend Implementation
