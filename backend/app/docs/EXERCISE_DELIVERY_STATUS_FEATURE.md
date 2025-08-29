# Exercise Delivery Status Feature

## Overview

The Exercise Delivery Status feature enhances the exercise listing API to show students which exercises they have already completed, providing a clear view of their progress and delivery status.

## What Was Implemented

### 🎯 **New DTO: `DTOEjercicioConEntrega`**

**Location:** `src/main/java/app/dtos/DTOEjercicioConEntrega.java`

A new DTO that extends `DTOEjercicio` to include delivery information for the current user:

#### **Core Fields:**
- All standard exercise fields (id, name, statement, dates, etc.)
- `tieneEntrega`: Boolean indicating if the user has a delivery
- `entregaId`: ID of the user's delivery (if exists)
- `estadoEntrega`: Delivery status (PENDIENTE, ENTREGADO, CALIFICADO)
- `notaEntrega`: Grade received (if calificada)
- `fechaEntrega`: When the delivery was submitted
- `notaFormateada`: Formatted grade string
- `estadoEntregaDescriptivo`: Human-readable status description

#### **Helper Methods:**
- `getEstadoEntregaUsuario()`: Returns delivery status for frontend display
- `getEstadoEntregaIcono()`: Returns appropriate icon (⏳📝❌📤✅)
- `getEstadoEntregaTexto()`: Returns descriptive text
- `puedeEntregar()`: Checks if user can still submit
- `puedeVerEntrega()`: Checks if user can view their delivery
- `estaCalificado()`: Checks if exercise is graded

### 🔗 **New REST Endpoint**

**Endpoint:** `GET /api/ejercicios/with-delivery`

**Security:** Only accessible by students (`ALUMNO` role)

**Parameters:** Same as the standard exercise listing endpoint
- `q`: Search query
- `name`: Exercise name filter
- `statement`: Exercise statement filter
- `classId`: Class ID filter
- `status`: Exercise status filter
- `page`: Page number (0-indexed)
- `size`: Page size (1-100)
- `sortBy`: Sort field
- `sortDirection`: Sort direction (ASC/DESC)

### 🔧 **Enhanced Service Method**

**Location:** `src/main/java/app/servicios/ServicioEjercicio.java`

**Method:** `obtenerEjerciciosConEntregaPaginados()`

**Features:**
- Security validation (ALUMNO role only)
- Current user ID extraction
- Exercise filtering and pagination
- Delivery status lookup for each exercise
- DTO conversion with delivery information

## 🎨 **Frontend Integration Guide**

### **API Call Example**

```javascript
// Get exercises with delivery status
const getExercisesWithDelivery = async (params = {}) => {
  const response = await fetch('/api/ejercicios/with-delivery?' + new URLSearchParams(params), {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return await response.json();
};

// Usage
const exercises = await getExercisesWithDelivery({
  page: 0,
  size: 20,
  sortBy: 'startDate',
  sortDirection: 'DESC'
});
```

### **Response Structure**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Ejercicio de Programación",
      "statement": "Crear una aplicación...",
      "startDate": "2025-08-01T10:00:00",
      "endDate": "2025-08-15T23:59:59",
      "classId": "1",
      "numeroEntregas": 15,
      "entregasCalificadas": 12,
      
      // Delivery information
      "tieneEntrega": true,
      "entregaId": 101,
      "estadoEntrega": "CALIFICADO",
      "notaEntrega": 8.5,
      "fechaEntrega": "2025-08-10T14:30:00",
      "notaFormateada": "8,50",
      "estadoEntregaDescriptivo": "Calificado"
    },
    {
      "id": 2,
      "name": "Ejercicio de Matemáticas",
      "statement": "Resolver ecuaciones...",
      "startDate": "2025-08-20T10:00:00",
      "endDate": "2025-08-30T23:59:59",
      "classId": "1",
      "numeroEntregas": 0,
      "entregasCalificadas": 0,
      
      // No delivery
      "tieneEntrega": false,
      "entregaId": null,
      "estadoEntrega": null,
      "notaEntrega": null,
      "fechaEntrega": null,
      "notaFormateada": null,
      "estadoEntregaDescriptivo": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 25,
  "totalPages": 2,
  "first": true,
  "last": false,
  "hasContent": true,
  "sortBy": "startDate",
  "sortDirection": "DESC"
}
```

### **Frontend Display Examples**

#### **Exercise Card with Status**

```jsx
const ExerciseCard = ({ exercise }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDIENTE': return 'text-gray-500';
      case 'DISPONIBLE': return 'text-blue-600';
      case 'VENCIDO_SIN_ENTREGA': return 'text-red-600';
      case 'ENTREGADO_PENDIENTE': return 'text-yellow-600';
      case 'CALIFICADO': return 'text-green-600';
      default: return 'text-gray-500';
    }
  };

  return (
    <div className="border rounded-lg p-4">
      <div className="flex justify-between items-start">
        <h3 className="text-lg font-semibold">{exercise.name}</h3>
        <div className="flex items-center gap-2">
          <span className="text-2xl">{exercise.getEstadoEntregaIcono()}</span>
          <span className={`text-sm font-medium ${getStatusColor(exercise.getEstadoEntregaUsuario())}`}>
            {exercise.getEstadoEntregaTexto()}
          </span>
        </div>
      </div>
      
      <p className="text-gray-600 mt-2">{exercise.statement}</p>
      
      <div className="mt-4 flex justify-between items-center">
        <div className="text-sm text-gray-500">
          Plazo: {new Date(exercise.endDate).toLocaleDateString()}
        </div>
        
        {exercise.tieneEntrega ? (
          <div className="flex items-center gap-4">
            {exercise.estaCalificado() && (
              <span className="text-green-600 font-semibold">
                Nota: {exercise.notaFormateada}
              </span>
            )}
            <button 
              onClick={() => viewDelivery(exercise.entregaId)}
              className="text-blue-600 hover:underline"
            >
              Ver entrega
            </button>
          </div>
        ) : (
          <button 
            onClick={() => submitDelivery(exercise.id)}
            disabled={!exercise.puedeEntregar()}
            className={`px-4 py-2 rounded ${
              exercise.puedeEntregar() 
                ? 'bg-blue-600 text-white hover:bg-blue-700' 
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {exercise.puedeEntregar() ? 'Entregar' : 'Vencido'}
          </button>
        )}
      </div>
    </div>
  );
};
```

#### **Status Filter Component**

```jsx
const StatusFilter = ({ onFilterChange }) => {
  const statusOptions = [
    { value: '', label: 'Todos' },
    { value: 'DISPONIBLE', label: 'Disponibles para entrega' },
    { value: 'ENTREGADO_PENDIENTE', label: 'Entregados pendientes' },
    { value: 'CALIFICADO', label: 'Calificados' },
    { value: 'VENCIDO_SIN_ENTREGA', label: 'Vencidos sin entrega' }
  ];

  return (
    <select 
      onChange={(e) => onFilterChange(e.target.value)}
      className="border rounded px-3 py-2"
    >
      {statusOptions.map(option => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
};
```

## 🚀 **Benefits**

### **For Students:**
- **Clear Progress View**: See which exercises are completed, pending, or available
- **Status Indicators**: Visual icons and colors for quick status recognition
- **Grade Visibility**: See grades immediately when available
- **Action Guidance**: Clear buttons for submitting or viewing deliveries

### **For Frontend:**
- **Rich Data**: All necessary information in a single API call
- **Flexible Display**: Multiple ways to show status (icons, colors, text)
- **Filtering Support**: Can filter by delivery status
- **No Additional Calls**: No need for separate API calls per exercise

### **For Backend:**
- **Security**: Proper role-based access control
- **Performance**: Efficient database queries with pagination
- **Consistency**: Same filtering and sorting as standard exercise listing
- **Extensibility**: Easy to add more delivery-related fields

## 🔒 **Security Features**

- **Role-Based Access**: Only students can access the delivery status endpoint
- **User Isolation**: Students can only see their own delivery information
- **Authentication Required**: JWT token validation
- **No Data Leakage**: Delivery information is properly scoped to current user

## 📋 **Migration Guide**

### **For Existing Frontend Code:**

1. **Replace Exercise Listing Call:**
   ```javascript
   // OLD
   const exercises = await getExercises();
   
   // NEW (for students)
   const exercises = await getExercisesWithDelivery();
   ```

2. **Update Exercise Display:**
   ```javascript
   // OLD
   exercises.map(exercise => (
     <ExerciseCard key={exercise.id} exercise={exercise} />
   ));
   
   // NEW
   exercises.content.map(exercise => (
     <ExerciseCard key={exercise.id} exercise={exercise} />
   ));
   ```

3. **Add Status Display:**
   ```javascript
   // Add status indicators to exercise cards
   <div className="status-indicator">
     {exercise.getEstadoEntregaIcono()} {exercise.getEstadoEntregaTexto()}
   </div>
   ```

## 🧪 **Testing**

### **API Testing:**
```bash
# Test with student token
curl -X GET "http://localhost:8080/api/ejercicios/with-delivery" \
  -H "Authorization: Bearer {student_token}" \
  -H "Content-Type: application/json"

# Test with professor token (should fail)
curl -X GET "http://localhost:8080/api/ejercicios/with-delivery" \
  -H "Authorization: Bearer {professor_token}" \
  -H "Content-Type: application/json"
```

### **Expected Responses:**
- **Student with deliveries**: Exercises with delivery information
- **Student without deliveries**: Exercises with `tieneEntrega: false`
- **Professor/Admin**: 403 Forbidden
- **Unauthenticated**: 401 Unauthorized

## 📖 **Related Documentation**

- [File Viewing and Download Guide](./FILE_VIEWING_AND_DOWNLOAD_GUIDE.md)
- [Exercise Delivery System](./EXERCISE_DELIVERY_FILE_UPLOAD_GUIDE.md)
- [REST API Standardization Guide](./REST_API_Standardization_Guide.md)

---

**Status**: ✅ Feature implemented | 🔒 Security validated | 📚 Documentation complete
