# Delivery Modification Feature

## Overview

The Delivery Modification feature allows students to modify their existing exercise deliveries by updating comments, adding new files, deleting files, and renaming files. This provides flexibility for students to improve their submissions before the deadline.

## What Was Implemented

### 🎯 **New DTOs for Modification Operations**

#### **1. `DTOPeticionModificarEntrega`**
**Location:** `src/main/java/app/dtos/DTOPeticionModificarEntrega.java`

Request DTO for delivery modifications:
- `comentarios`: Updated comments (optional)
- `operacionesArchivos`: List of file operations to perform

#### **2. `DTOOperacionArchivo`**
**Location:** `src/main/java/app/dtos/DTOOperacionArchivo.java`

Defines file operations:
- `tipo`: Operation type (ELIMINAR, RENOMBRAR)
- `rutaArchivo`: File path to operate on
- `nuevoNombre`: New name for rename operations

#### **3. `DTORespuestaModificacionEntrega`**
**Location:** `src/main/java/app/dtos/DTORespuestaModificacionEntrega.java`

Response DTO with modification results:
- `entregaId`: ID of the modified delivery
- `comentarios`: Updated comments
- `archivosEntregados`: List of remaining files
- `operacionesRealizadas`: Results of file operations
- `errores`: List of errors encountered

#### **4. `DTOOperacionArchivoResultado`**
**Location:** `src/main/java/app/dtos/DTOOperacionArchivoResultado.java`

Individual operation result:
- `tipo`: Operation type performed
- `rutaArchivo`: File path operated on
- `nuevoNombre`: New name (for rename operations)
- `exitosa`: Whether operation succeeded
- `mensaje`: Success/error message
- `error`: Error details if failed

### 🔧 **Enhanced Service Methods**

#### **1. `modificarEntrega()`**
**Location:** `src/main/java/app/servicios/ServicioEntregaEjercicio.java`

Main modification method that:
- Validates user permissions
- Checks delivery status (only pending/delivered can be modified)
- Updates comments if provided
- Processes file operations
- Returns detailed results

#### **2. `agregarArchivosEntrega()`**
**Location:** `src/main/java/app/servicios/ServicioEntregaEjercicio.java`

Adds new files to existing delivery:
- Validates file types and sizes
- Uploads files to storage
- Updates delivery record
- Returns file information

#### **3. File Operation Processing Methods**
- `procesarOperacionArchivo()`: Routes operations to appropriate handlers
- `procesarEliminacionArchivo()`: Handles file deletion
- `procesarRenombradoArchivo()`: Handles file renaming

### 🔗 **New REST Endpoints**

#### **1. Modify Delivery**
```
PATCH /api/entregas/{id}/modify
```
**Purpose:** Modify comments and perform file operations
**Access:** Students (own deliveries), Admins/Professors (any delivery)

**Request Body:**
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

**Response:**
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
    },
    {
      "tipo": "RENOMBRAR",
      "rutaArchivo": "exercise-deliveries/1/1/old-name.pdf",
      "nuevoNombre": "new-name.pdf",
      "exitosa": true,
      "mensaje": "Archivo renombrado exitosamente",
      "error": null
    }
  ],
  "errores": []
}
```

#### **2. Add Files to Delivery**
```
POST /api/entregas/{id}/add-files
```
**Purpose:** Add new files to existing delivery
**Access:** Students (own deliveries), Admins/Professors (any delivery)

**Request:** Multipart form data with files
**Response:** List of uploaded file information

#### **3. Delete All Files**
```
DELETE /api/entregas/{id}/files
```
**Purpose:** Remove all files from delivery
**Access:** Students (own deliveries), Admins/Professors (any delivery)

**Response:**
```json
{
  "success": true,
  "message": "Todos los archivos eliminados correctamente",
  "archivosEliminados": 3,
  "errores": 0
}
```

### 🔧 **Enhanced FileUploadUtils**

**Location:** `src/main/java/app/util/FileUploadUtils.java`

New methods added:
- `renombrarArchivo()`: Renames files in the file system
- `guardarArchivoEntrega()`: Alias for existing save method
- `getFileExtension()`: Public version of file extension extraction
- `isImageFile()`: Checks if file is an image
- `isPdfFile()`: Checks if file is a PDF
- `getFileTypeLabel()`: Gets human-readable file type

## 🎨 **Frontend Integration Guide**

### **Modify Delivery Example**

```javascript
// Modify delivery with file operations
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

// Usage examples
const fileOperations = [
  // Delete a file
  {
    tipo: 'ELIMINAR',
    rutaArchivo: 'exercise-deliveries/1/1/old-file.pdf'
  },
  // Rename a file
  {
    tipo: 'RENOMBRAR',
    rutaArchivo: 'exercise-deliveries/1/1/document.pdf',
    nuevoNombre: 'final-report.pdf'
  }
];

const result = await modifyDelivery(101, 'Updated comments', fileOperations);
```

### **Add Files Example**

```javascript
// Add files to existing delivery
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

### **Delete All Files Example**

```javascript
// Delete all files from delivery
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

## 🔒 **Security Features**

### **Access Control**
- **Students:** Can only modify their own deliveries
- **Admins/Professors:** Can modify any delivery
- **Authentication:** JWT token required for all operations

### **Status Validation**
- Only **PENDIENTE** and **ENTREGADO** deliveries can be modified
- **CALIFICADO** deliveries cannot be modified (already graded)

### **File Validation**
- File type validation (PNG, PDF only)
- File size limits (10MB per file)
- Path traversal protection
- Ownership verification

## 📋 **Operation Types**

### **File Operations**

#### **1. Delete File (`ELIMINAR`)**
- Removes file from delivery record
- Deletes physical file from storage
- Returns success/failure status

#### **2. Rename File (`RENOMBRAR`)**
- Renames file in file system
- Updates delivery record with new path
- Validates new filename doesn't conflict

### **Comment Updates**
- Updates delivery comments
- Preserves existing comments if not provided
- Validates comment length (max 2000 characters)

## 🚀 **Benefits**

### **For Students:**
- **Flexibility:** Can improve submissions before deadline
- **Error Correction:** Fix mistakes in uploaded files
- **Organization:** Rename files for better clarity
- **Iterative Work:** Add files as work progresses

### **For Frontend:**
- **Rich Operations:** Multiple file operations in single request
- **Detailed Feedback:** Comprehensive operation results
- **Error Handling:** Clear error messages for failed operations
- **Batch Operations:** Process multiple changes efficiently

### **For Backend:**
- **Security:** Proper access control and validation
- **Atomicity:** Operations are processed together
- **Audit Trail:** Detailed operation logging
- **Error Recovery:** Graceful handling of partial failures

## 🧪 **Testing Examples**

### **API Testing**

```bash
# Modify delivery with file operations
curl -X PATCH "http://localhost:8080/api/entregas/101/modify" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "comentarios": "Updated delivery with corrections",
    "operacionesArchivos": [
      {
        "tipo": "RENOMBRAR",
        "rutaArchivo": "exercise-deliveries/1/1/draft.pdf",
        "nuevoNombre": "final-report.pdf"
      }
    ]
  }'

# Add files to delivery
curl -X POST "http://localhost:8080/api/entregas/101/add-files" \
  -H "Authorization: Bearer {token}" \
  -F "files=@new-document.pdf" \
  -F "files=@diagram.png"

# Delete all files
curl -X DELETE "http://localhost:8080/api/entregas/101/files" \
  -H "Authorization: Bearer {token}"
```

## 📖 **Related Features**

This feature complements:
- **File Upload System:** For adding new files
- **File Viewing System:** For viewing modified files
- **Exercise Delivery Status:** For tracking delivery progress
- **Grading System:** For professor evaluation

---

**Status:** ✅ **Feature Complete** | 🔒 **Security Validated** | 📚 **Documentation Ready** | 🚀 **Ready for Frontend Integration**
