 to c# Exercise Delivery File Upload Guide

This guide explains how to use the new file upload functionality for exercise deliveries, which supports PNG and PDF files.

## Overview

The exercise delivery system now supports real file uploads with the following features:

- **Supported Formats**: PNG and PDF files only
- **File Size Limit**: Maximum 10MB per file
- **Security**: Students can only upload files for themselves
- **Validation**: File type validation using both extension and MIME type
- **Storage**: Files are stored in a structured directory system
- **Unique Naming**: Files are renamed with timestamps and UUIDs for security

## File Upload Workflow

### 1. Upload Files First
Before creating a delivery, students must upload their files using the upload endpoints.

### 2. Create Delivery with File Paths
After uploading files, students create the delivery by providing the file paths returned from the upload.

### 3. Manage Files
Students can delete files from their deliveries if they are still pending or delivered (not graded).

## Available Endpoints

### 1. Upload Single File
**Endpoint:** `POST /api/entregas/upload-file`  
**Method:** POST  
**Content-Type:** `multipart/form-data`  
**Role Required:** ALUMNO  

**Request Parameters:**
- `file` (MultipartFile): The file to upload (PNG or PDF, max 10MB)
- `ejercicioId` (Long): The ID of the exercise

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/entregas/upload-file" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/document.pdf" \
  -F "ejercicioId=123"
```

**Response:**
```json
{
  "nombreOriginal": "document.pdf",
  "nombreGuardado": "delivery_20240825_143022_a1b2c3d4.pdf",
  "rutaRelativa": "exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf",
  "tipoMime": "application/pdf",
  "tamanoBytes": 2048576,
  "tamanoFormateado": "2.0 MB",
  "fechaSubida": "2024-08-25T14:30:22",
  "extension": "pdf",
  "esImagen": false,
  "esPdf": true
}
```

### 2. Upload Multiple Files
**Endpoint:** `POST /api/entregas/upload-files`  
**Method:** POST  
**Content-Type:** `multipart/form-data`  
**Role Required:** ALUMNO  

**Request Parameters:**
- `files` (List<MultipartFile>): The files to upload (PNG or PDF, max 10MB each)
- `ejercicioId` (Long): The ID of the exercise

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/entregas/upload-files" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "files=@/path/to/document1.pdf" \
  -F "files=@/path/to/image1.png" \
  -F "ejercicioId=123"
```

**Response:**
```json
[
  {
    "nombreOriginal": "document1.pdf",
    "nombreGuardado": "delivery_20240825_143022_a1b2c3d4.pdf",
    "rutaRelativa": "exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf",
    "tipoMime": "application/pdf",
    "tamanoBytes": 2048576,
    "tamanoFormateado": "2.0 MB",
    "fechaSubida": "2024-08-25T14:30:22",
    "extension": "pdf",
    "esImagen": false,
    "esPdf": true
  },
  {
    "nombreOriginal": "image1.png",
    "nombreGuardado": "delivery_20240825_143023_e5f6g7h8.png",
    "rutaRelativa": "exercise-deliveries/456/123/delivery_20240825_143023_e5f6g7h8.png",
    "tipoMime": "image/png",
    "tamanoBytes": 512000,
    "tamanoFormateado": "500.0 KB",
    "fechaSubida": "2024-08-25T14:30:23",
    "extension": "png",
    "esImagen": true,
    "esPdf": false
  }
]
```

### 3. Create Delivery with Uploaded Files
**Endpoint:** `POST /api/entregas/create-with-files`  
**Method:** POST  
**Content-Type:** `application/json`  
**Role Required:** ALUMNO  

**Request Body:**
```json
{
  "ejercicioId": 123,
  "archivosRutas": [
    "exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf",
    "exercise-deliveries/456/123/delivery_20240825_143023_e5f6g7h8.png"
  ]
}
```

**Response:**
```json
{
  "id": 789,
  "nota": null,
  "fechaEntrega": "2024-08-25T14:30:25",
  "estado": "ENTREGADO",
  "archivosEntregados": [
    "exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf",
    "exercise-deliveries/456/123/delivery_20240825_143023_e5f6g7h8.png"
  ],
  "alumnoEntreganteId": "456",
  "ejercicioId": "123",
  "numeroArchivos": 2,
  "comentarios": null
}
```

### 4. Delete File from Delivery
**Endpoint:** `DELETE /api/entregas/{id}/files/{rutaArchivo}`  
**Method:** DELETE  
**Role Required:** ALUMNO (own delivery), ADMIN, PROFESOR  

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/entregas/789/files/exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Archivo eliminado correctamente"
}
```

## File Storage Structure

Files are stored in the following directory structure:

```
uploads/
└── exercise-deliveries/
    └── {studentId}/
        └── {exerciseId}/
            ├── delivery_20240825_143022_a1b2c3d4.pdf
            ├── delivery_20240825_143023_e5f6g7h8.png
            └── ...
```

### File Naming Convention
- Format: `delivery_{timestamp}_{uuid}.{extension}`
- Example: `delivery_20240825_143022_a1b2c3d4.pdf`
- Timestamp: `yyyyMMdd_HHmmss`
- UUID: First 8 characters of a UUID for uniqueness

## File Validation

### Supported File Types
- **PNG**: Image files with `.png` extension and `image/png` MIME type
- **PDF**: Document files with `.pdf` extension and `application/pdf` MIME type

### Validation Checks
1. **File Size**: Maximum 10MB per file
2. **File Extension**: Must be `.png` or `.pdf`
3. **MIME Type**: Must match the expected content type
4. **Magic Bytes**: Additional validation for PNG and PDF files
5. **File Content**: Validates actual file content, not just extension

### Error Messages
- `"El archivo no puede estar vacío"` - Empty file
- `"El archivo es demasiado grande. Tamaño máximo: 10MB"` - File too large
- `"Tipo de archivo no permitido. Solo se permiten: pdf, png"` - Invalid file type
- `"Tipo de contenido no permitido"` - Invalid MIME type
- `"El archivo no es un PNG válido"` - Invalid PNG content
- `"El archivo no es un PDF válido"` - Invalid PDF content

## Security Features

### Access Control
- Only students can upload files for themselves
- Students can only delete files from their own deliveries
- Admins and professors can delete files from any delivery

### File Security
- Files are renamed with timestamps and UUIDs to prevent conflicts
- Original filenames are not preserved in the file system
- Directory structure prevents unauthorized access
- File validation prevents malicious file uploads

### Exercise Validation
- Files can only be uploaded for active exercises
- Cannot upload files for expired exercises
- Cannot create deliveries for exercises that have already been delivered

## Frontend Integration

### File Upload Component Example
```javascript
// Upload single file
const uploadFile = async (file, exerciseId) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('ejercicioId', exerciseId);
  
  const response = await fetch('/api/entregas/upload-file', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  return await response.json();
};

// Upload multiple files
const uploadFiles = async (files, exerciseId) => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  formData.append('ejercicioId', exerciseId);
  
  const response = await fetch('/api/entregas/upload-files', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  return await response.json();
};

// Create delivery with uploaded files
const createDeliveryWithFiles = async (exerciseId, filePaths) => {
  const response = await fetch('/api/entregas/create-with-files', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      ejercicioId: exerciseId,
      archivosRutas: filePaths
    })
  });
  
  return await response.json();
};
```

### File Validation Example
```javascript
const validateFile = (file) => {
  const allowedTypes = ['image/png', 'application/pdf'];
  const maxSize = 10 * 1024 * 1024; // 10MB
  
  if (!allowedTypes.includes(file.type)) {
    throw new Error('Solo se permiten archivos PNG y PDF');
  }
  
  if (file.size > maxSize) {
    throw new Error('El archivo es demasiado grande. Máximo 10MB');
  }
  
  return true;
};
```

## Error Handling

### Common Error Scenarios
1. **File too large**: Returns 400 with size limit message
2. **Invalid file type**: Returns 400 with supported types message
3. **Exercise not found**: Returns 404 with exercise not found message
4. **Exercise expired**: Returns 400 with expiration message
5. **Unauthorized access**: Returns 403 with access denied message
6. **File not found**: Returns 404 when trying to delete non-existent file

### Error Response Format
```json
{
  "timestamp": "2024-08-25T14:30:22",
  "status": 400,
  "error": "Bad Request",
  "message": "El archivo es demasiado grande. Tamaño máximo: 10MB",
  "errorCode": "FILE_TOO_LARGE",
  "path": "/api/entregas/upload-file"
}
```

## Configuration

### Application Properties
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB
```

### File Upload Utils Configuration
- **Base Directory**: `uploads/`
- **Exercise Deliveries Directory**: `exercise-deliveries/`
- **Maximum File Size**: 10MB
- **Allowed Extensions**: `pdf`, `png`
- **Allowed MIME Types**: `application/pdf`, `image/png`

## Best Practices

### For Students
1. Upload files before creating the delivery
2. Validate file types and sizes before uploading
3. Keep file names descriptive for easier identification
4. Delete unwanted files before creating the delivery

### For Developers
1. Always validate files on both frontend and backend
2. Use proper error handling for file operations
3. Implement file size limits to prevent abuse
4. Store file metadata for better management
5. Implement proper cleanup for deleted files

### For Administrators
1. Monitor file storage usage
2. Implement backup strategies for uploaded files
3. Set up proper file permissions
4. Consider implementing file retention policies
