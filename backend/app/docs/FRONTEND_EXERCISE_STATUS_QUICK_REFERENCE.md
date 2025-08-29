# Frontend Exercise Status Quick Reference

## 🚀 **New Endpoint for Students**

**URL:** `GET /api/ejercicios/with-delivery`

**Access:** Students only (`ALUMNO` role)

**Parameters:** Same as `/api/ejercicios` (q, name, statement, classId, status, page, size, sortBy, sortDirection)

## 📊 **Response Structure**

Each exercise now includes delivery status:

```json
{
  "id": 1,
  "name": "Ejercicio de Programación",
  "statement": "Crear una aplicación...",
  "startDate": "2025-08-01T10:00:00",
  "endDate": "2025-08-15T23:59:59",
  
  // NEW: Delivery information
  "tieneEntrega": true,
  "entregaId": 101,
  "estadoEntrega": "CALIFICADO",
  "notaEntrega": 8.5,
  "fechaEntrega": "2025-08-10T14:30:00",
  "notaFormateada": "8,50",
  "estadoEntregaDescriptivo": "Calificado"
}
```

## 🎨 **Status Display Options**

### **Status Types:**
- `PENDIENTE` → ⏳ "Pendiente de inicio"
- `DISPONIBLE` → 📝 "Disponible para entrega"
- `VENCIDO_SIN_ENTREGA` → ❌ "Vencido sin entrega"
- `ENTREGADO_PENDIENTE` → 📤 "Entregado - Pendiente de calificación"
- `CALIFICADO` → ✅ "Calificado"

### **Quick Implementation:**

```javascript
// Replace your existing exercise fetch
const getExercises = async () => {
  const response = await fetch('/api/ejercicios/with-delivery', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return await response.json();
};

// Display status in your exercise cards
const ExerciseCard = ({ exercise }) => (
  <div className="exercise-card">
    <h3>{exercise.name}</h3>
    
    {/* Status indicator */}
    <div className="status">
      <span className="icon">{getStatusIcon(exercise)}</span>
      <span className="text">{getStatusText(exercise)}</span>
    </div>
    
    {/* Grade if available */}
    {exercise.tieneEntrega && exercise.estaCalificado() && (
      <div className="grade">Nota: {exercise.notaFormateada}</div>
    )}
    
    {/* Action button */}
    {exercise.tieneEntrega ? (
      <button onClick={() => viewDelivery(exercise.entregaId)}>
        Ver entrega
      </button>
    ) : (
      <button 
        disabled={!exercise.puedeEntregar()}
        onClick={() => submitDelivery(exercise.id)}
      >
        {exercise.puedeEntregar() ? 'Entregar' : 'Vencido'}
      </button>
    )}
  </div>
);

// Helper functions
const getStatusIcon = (exercise) => {
  if (!exercise.tieneEntrega) {
    if (exercise.noHaComenzado()) return '⏳';
    if (exercise.haVencido()) return '❌';
    return '📝';
  }
  
  switch (exercise.estadoEntrega) {
    case 'CALIFICADO': return '✅';
    default: return '📤';
  }
};

const getStatusText = (exercise) => {
  if (!exercise.tieneEntrega) {
    if (exercise.noHaComenzado()) return 'Pendiente de inicio';
    if (exercise.haVencido()) return 'Vencido sin entrega';
    return 'Disponible para entrega';
  }
  
  return exercise.estadoEntregaDescriptivo;
};
```

## 🔄 **Migration Steps**

1. **Update API call:**
   ```javascript
   // OLD
   fetch('/api/ejercicios')
   
   // NEW
   fetch('/api/ejercicios/with-delivery')
   ```

2. **Add status display to exercise cards**

3. **Update action buttons based on delivery status**

4. **Add grade display for completed exercises**

## 🎯 **Key Benefits**

- ✅ **Single API call** for all exercise + delivery data
- ✅ **Visual status indicators** with icons and colors
- ✅ **Grade visibility** for completed exercises
- ✅ **Smart action buttons** (submit vs view delivery)
- ✅ **No breaking changes** to existing functionality

## 🔒 **Security Notes**

- Only students can access this endpoint
- Each student only sees their own delivery information
- Requires valid JWT token

---

**Ready to implement!** 🚀
