# Frontend Delivery Modification Quick Reference

## 🚀 **New Endpoints for Delivery Modification**

### **1. Modify Delivery**
**URL:** `PATCH /api/entregas/{id}/modify`  
**Access:** Students (own deliveries), Admins/Professors (any delivery)

### **2. Add Files**
**URL:** `POST /api/entregas/{id}/add-files`  
**Access:** Students (own deliveries), Admins/Professors (any delivery)

### **3. Delete All Files**
**URL:** `DELETE /api/entregas/{id}/files`  
**Access:** Students (own deliveries), Admins/Professors (any delivery)

## 📊 **Request/Response Examples**

### **Modify Delivery Request**
```json
{
  "comentarios": "Updated comments for the delivery",
  "operacionesArchivos": [
    {
      "tipo": "ELIMINAR",
      "rutaArchivo": "exercise-deliveries/1/1/document.pdf"
    },
    {
      "tipo": "RENOMBRAR",
      "rutaArchivo": "exercise-deliveries/1/1/old-name.pdf",
      "nuevoNombre": "new-name.pdf"
    }
  ]
}
```

### **Modify Delivery Response**
```json
{
  "entregaId": 101,
  "comentarios": "Updated comments for the delivery",
  "archivosEntregados": [
    "exercise-deliveries/1/1/new-name.pdf",
    "exercise-deliveries/1/1/image.png"
  ],
  "numeroArchivos": 2,
  "fechaModificacion": "2025-08-30T00:15:30",
  "operacionesRealizadas": [
    {
      "tipo": "ELIMINAR",
      "rutaArchivo": "exercise-deliveries/1/1/document.pdf",
      "nuevoNombre": null,
      "exitosa": true,
      "mensaje": "Archivo eliminado exitosamente",
      "error": null
    }
  ],
  "errores": []
}
```

## 🎨 **Quick Implementation**

### **Modify Delivery Function**
```javascript
const modifyDelivery = async (deliveryId, comments, fileOperations) => {
  const response = await fetch(`/api/entregas/${deliveryId}/modify`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      comentarios: comments,
      operacionesArchivos: fileOperations
    })
  });
  
  return await response.json();
};
```

### **Add Files Function**
```javascript
const addFilesToDelivery = async (deliveryId, files) => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  
  const response = await fetch(`/api/entregas/${deliveryId}/add-files`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  return await response.json();
};
```

### **Delete All Files Function**
```javascript
const deleteAllFiles = async (deliveryId) => {
  const response = await fetch(`/api/entregas/${deliveryId}/files`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
};
```

## 🔄 **Usage Examples**

### **Delete a File**
```javascript
const fileOperations = [
  {
    tipo: 'ELIMINAR',
    rutaArchivo: 'exercise-deliveries/1/1/old-file.pdf'
  }
];

const result = await modifyDelivery(101, null, fileOperations);
```

### **Rename a File**
```javascript
const fileOperations = [
  {
    tipo: 'RENOMBRAR',
    rutaArchivo: 'exercise-deliveries/1/1/draft.pdf',
    nuevoNombre: 'final-report.pdf'
  }
];

const result = await modifyDelivery(101, null, fileOperations);
```

### **Update Comments Only**
```javascript
const result = await modifyDelivery(101, 'New comments', []);
```

### **Multiple Operations**
```javascript
const fileOperations = [
  {
    tipo: 'ELIMINAR',
    rutaArchivo: 'exercise-deliveries/1/1/old-file.pdf'
  },
  {
    tipo: 'RENOMBRAR',
    rutaArchivo: 'exercise-deliveries/1/1/draft.pdf',
    nuevoNombre: 'final-report.pdf'
  }
];

const result = await modifyDelivery(101, 'Updated comments', fileOperations);
```

## 🎯 **Operation Types**

### **File Operations**
- `ELIMINAR`: Delete a file
- `RENOMBRAR`: Rename a file

### **Status Validation**
- Only **PENDIENTE** and **ENTREGADO** deliveries can be modified
- **CALIFICADO** deliveries cannot be modified

## 🔒 **Security Notes**

- Students can only modify their own deliveries
- Admins/Professors can modify any delivery
- Requires valid JWT token
- File operations are validated for security

## 🎨 **UI Integration Ideas**

### **File Management Interface**
```jsx
const DeliveryFileManager = ({ delivery }) => {
  const [files, setFiles] = useState(delivery.archivosEntregados);
  const [comments, setComments] = useState(delivery.comentarios);
  
  const handleDeleteFile = async (filePath) => {
    const operations = [{
      tipo: 'ELIMINAR',
      rutaArchivo: filePath
    }];
    
    const result = await modifyDelivery(delivery.id, comments, operations);
    if (result.fueExitosa()) {
      setFiles(result.archivosEntregados);
    }
  };
  
  const handleRenameFile = async (oldPath, newName) => {
    const operations = [{
      tipo: 'RENOMBRAR',
      rutaArchivo: oldPath,
      nuevoNombre: newName
    }];
    
    const result = await modifyDelivery(delivery.id, comments, operations);
    if (result.fueExitosa()) {
      setFiles(result.archivosEntregados);
    }
  };
  
  return (
    <div className="file-manager">
      <textarea 
        value={comments}
        onChange={(e) => setComments(e.target.value)}
        placeholder="Add comments..."
      />
      
      {files.map(file => (
        <div key={file} className="file-item">
          <span>{file}</span>
          <button onClick={() => handleDeleteFile(file)}>Delete</button>
          <button onClick={() => handleRenameFile(file, 'new-name.pdf')}>Rename</button>
        </div>
      ))}
      
      <button onClick={() => addFilesToDelivery(delivery.id, selectedFiles)}>
        Add Files
      </button>
    </div>
  );
};
```

## 🚀 **Key Benefits**

- ✅ **Flexible Operations:** Multiple file operations in single request
- ✅ **Detailed Feedback:** Comprehensive operation results
- ✅ **Error Handling:** Clear error messages for failed operations
- ✅ **Security:** Proper access control and validation
- ✅ **No Breaking Changes:** Extends existing functionality

---

**Ready to implement!** 🚀
