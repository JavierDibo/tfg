# File Viewing and Download Guide

This guide explains how to use the new file viewing and download functionality for exercise deliveries, which allows users to view and download uploaded files securely.

## Overview

The file viewing system provides secure access to uploaded files with the following features:

- **Secure Access Control**: Users can only access files from deliveries they have permission to view
- **Inline Viewing**: PNG images and PDF documents can be viewed directly in the browser
- **Download Support**: All file types can be downloaded
- **File Information**: Detailed metadata about files including size, type, and format
- **Proper Content Types**: Files are served with appropriate MIME types
- **Security Validation**: File paths are validated against delivery ownership

## Available Endpoints

### 1. View File (Inline)
**Endpoint:** `GET /api/files/view`  
**Method:** GET  
**Role Required:** ALUMNO (own delivery), ADMIN, PROFESOR  

**Query Parameters:**
- `path` (String): File path relative to uploads directory (required)
- `deliveryId` (Long): ID of the delivery containing the file (required)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/files/view?path=exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf&deliveryId=789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
- **200 OK**: File content with appropriate content type headers
- **403 Forbidden**: Access denied or file doesn't belong to delivery
- **404 Not Found**: File or delivery not found

**Headers Set:**
```
Content-Type: application/pdf
Content-Disposition: inline; filename="delivery_20240825_143022_a1b2c3d4.pdf"
```

### 2. Download File
**Endpoint:** `GET /api/files/download`  
**Method:** GET  
**Role Required:** ALUMNO (own delivery), ADMIN, PROFESOR  

**Query Parameters:**
- `path` (String): File path relative to uploads directory (required)
- `deliveryId` (Long): ID of the delivery containing the file (required)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/files/download?path=exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf&deliveryId=789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output downloaded_file.pdf
```

**Response:**
- **200 OK**: File content with download headers
- **403 Forbidden**: Access denied or file doesn't belong to delivery
- **404 Not Found**: File or delivery not found

**Headers Set:**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="delivery_20240825_143022_a1b2c3d4.pdf"
```

### 3. Get File Information
**Endpoint:** `GET /api/files/info/{deliveryId}`  
**Method:** GET  
**Role Required:** ALUMNO (own delivery), ADMIN, PROFESOR  

**Path Parameters:**
- `deliveryId` (Long): ID of the delivery (required)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/files/info/789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "fileName": "delivery_20240825_143022_a1b2c3d4.pdf",
    "filePath": "exercise-deliveries/456/123/delivery_20240825_143022_a1b2c3d4.pdf",
    "extension": "pdf",
    "contentType": "application/pdf",
    "fileSize": 2048576,
    "formattedSize": "2.0 MB",
    "isImage": false,
    "isPdf": true
  },
  {
    "fileName": "delivery_20240825_143023_e5f6g7h8.png",
    "filePath": "exercise-deliveries/456/123/delivery_20240825_143023_e5f6g7h8.png",
    "extension": "png",
    "contentType": "image/png",
    "fileSize": 512000,
    "formattedSize": "500.0 KB",
    "isImage": true,
    "isPdf": false
  }
]
```

## Security Features

### Access Control
- **Student Access**: Students can only access files from their own deliveries
- **Admin/Professor Access**: Admins and professors can access files from any delivery
- **Delivery Validation**: Files are validated against the delivery's file list
- **Path Security**: File paths are validated to prevent directory traversal attacks

### File Validation
- **Existence Check**: Verifies that the file exists on the filesystem
- **Readability Check**: Ensures the file is readable
- **Ownership Check**: Validates that the file belongs to the specified delivery
- **Content Type Detection**: Automatically determines appropriate MIME types

### Error Handling
- **404 Not Found**: When file or delivery doesn't exist
- **403 Forbidden**: When user doesn't have permission or file doesn't belong to delivery
- **400 Bad Request**: When required parameters are missing
- **500 Internal Server Error**: For unexpected server errors

## Frontend Integration

### File Viewing Component Example
```javascript
// View file inline
const viewFile = async (filePath, deliveryId) => {
  const url = `/api/files/view?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
  
  // For images, use in img tag
  if (isImageFile(filePath)) {
    return url;
  }
  
  // For PDFs, use in iframe or new window
  if (isPdfFile(filePath)) {
    window.open(url, '_blank');
  }
  
  // For other files, show download option
  return null;
};

// Download file
const downloadFile = async (filePath, deliveryId, fileName) => {
  const url = `/api/files/download?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
  
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.ok) {
    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }
};

// Get file information
const getFileInfo = async (deliveryId) => {
  const response = await fetch(`/api/files/info/${deliveryId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.ok) {
    return await response.json();
  }
  
  throw new Error('Failed to get file information');
};
```

### Svelte Component Example
```svelte
<script>
  import { onMount } from 'svelte';
  
  export let deliveryId;
  export let files = [];
  
  let fileInfos = [];
  let selectedFile = null;
  let showModal = false;
  
  onMount(async () => {
    try {
      const response = await fetch(`/api/files/info/${deliveryId}`, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });
      
      if (response.ok) {
        fileInfos = await response.json();
      }
    } catch (error) {
      console.error('Error loading file info:', error);
    }
  });
  
  function viewFile(fileInfo) {
    selectedFile = fileInfo;
    showModal = true;
  }
  
  async function downloadFile(fileInfo) {
    const url = `/api/files/download?path=${encodeURIComponent(fileInfo.filePath)}&deliveryId=${deliveryId}`;
    
    try {
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });
      
      if (response.ok) {
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = fileInfo.fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);
      }
    } catch (error) {
      console.error('Error downloading file:', error);
    }
  }
  
  function getFileIcon(fileInfo) {
    if (fileInfo.isImage) return '🖼️';
    if (fileInfo.isPdf) return '📄';
    return '📎';
  }
  
  function getFileTypeLabel(fileInfo) {
    if (fileInfo.isImage) return 'Imagen';
    if (fileInfo.isPdf) return 'PDF';
    return 'Documento';
  }
</script>

<!-- File List -->
<div class="file-list">
  {#each fileInfos as fileInfo}
    <div class="file-item">
      <div class="file-info">
        <span class="file-icon">{getFileIcon(fileInfo)}</span>
        <div class="file-details">
          <div class="file-name">{fileInfo.fileName}</div>
          <div class="file-meta">
            {getFileTypeLabel(fileInfo)} • {fileInfo.formattedSize}
          </div>
        </div>
      </div>
      <div class="file-actions">
        {#if fileInfo.isImage || fileInfo.isPdf}
          <button on:click={() => viewFile(fileInfo)} class="btn-view">
            Ver
          </button>
        {/if}
        <button on:click={() => downloadFile(fileInfo)} class="btn-download">
          Descargar
        </button>
      </div>
    </div>
  {/each}
</div>

<!-- File Viewer Modal -->
{#if showModal && selectedFile}
  <div class="modal-overlay" on:click={() => showModal = false}>
    <div class="modal-content" on:click|stopPropagation>
      <div class="modal-header">
        <h3>{selectedFile.fileName}</h3>
        <button on:click={() => showModal = false} class="btn-close">×</button>
      </div>
      <div class="modal-body">
        {#if selectedFile.isImage}
          <img 
            src="/api/files/view?path={encodeURIComponent(selectedFile.filePath)}&deliveryId={deliveryId}" 
            alt={selectedFile.fileName}
            class="file-preview"
          />
        {:else if selectedFile.isPdf}
          <iframe 
            src="/api/files/view?path={encodeURIComponent(selectedFile.filePath)}&deliveryId={deliveryId}"
            class="file-preview"
            title={selectedFile.fileName}
          ></iframe>
        {:else}
          <div class="no-preview">
            <p>Vista previa no disponible para este tipo de archivo.</p>
            <button on:click={() => downloadFile(selectedFile)} class="btn-download">
              Descargar archivo
            </button>
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}

<style>
  .file-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .file-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background: white;
  }
  
  .file-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .file-icon {
    font-size: 1.5rem;
  }
  
  .file-name {
    font-weight: 500;
    color: #333;
  }
  
  .file-meta {
    font-size: 0.875rem;
    color: #666;
  }
  
  .file-actions {
    display: flex;
    gap: 0.5rem;
  }
  
  .btn-view, .btn-download {
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
  }
  
  .btn-view {
    background: #007bff;
    color: white;
  }
  
  .btn-download {
    background: #28a745;
    color: white;
  }
  
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }
  
  .modal-content {
    background: white;
    border-radius: 8px;
    max-width: 90vw;
    max-height: 90vh;
    overflow: hidden;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .modal-body {
    padding: 1rem;
  }
  
  .file-preview {
    max-width: 100%;
    max-height: 70vh;
    border: none;
  }
  
  .no-preview {
    text-align: center;
    padding: 2rem;
  }
</style>
```

## Content Type Support

### Supported File Types
| Extension | Content Type | View Support | Download Support |
|-----------|--------------|--------------|------------------|
| PDF | `application/pdf` | ✅ Inline | ✅ |
| PNG | `image/png` | ✅ Inline | ✅ |
| JPG/JPEG | `image/jpeg` | ✅ Inline | ✅ |
| GIF | `image/gif` | ✅ Inline | ✅ |
| TXT | `text/plain` | ❌ | ✅ |
| Other | `application/octet-stream` | ❌ | ✅ |

### Content Type Detection
The system automatically detects content types based on file extensions:

```java
private String determineContentType(String filePath) {
    String extension = getFileExtension(getFileName(filePath)).toLowerCase();
    return switch (extension) {
        case "pdf" -> "application/pdf";
        case "png" -> "image/png";
        case "jpg", "jpeg" -> "image/jpeg";
        case "gif" -> "image/gif";
        case "txt" -> "text/plain";
        default -> "application/octet-stream";
    };
}
```

## Error Handling

### Common Error Scenarios
1. **File not found**: Returns 404 when file doesn't exist
2. **Access denied**: Returns 403 when user doesn't have permission
3. **Invalid delivery**: Returns 404 when delivery doesn't exist
4. **File not in delivery**: Returns 403 when file doesn't belong to delivery
5. **Missing parameters**: Returns 400 when required parameters are missing

### Error Response Examples
```json
// 404 - File not found
{
  "timestamp": "2024-08-25T14:30:22",
  "status": 404,
  "error": "Not Found",
  "message": "File not found or not readable",
  "path": "/api/files/view"
}

// 403 - Access denied
{
  "timestamp": "2024-08-25T14:30:22",
  "status": 403,
  "error": "Forbidden",
  "message": "File does not belong to delivery",
  "path": "/api/files/view"
}
```

## Best Practices

### For Frontend Developers
1. **Always validate file types** before attempting to view them
2. **Handle errors gracefully** with user-friendly messages
3. **Use appropriate content types** for different file types
4. **Implement loading states** for file operations
5. **Cache file information** to reduce API calls

### For Backend Developers
1. **Validate file paths** to prevent directory traversal
2. **Check file ownership** before serving files
3. **Set appropriate headers** for content type and disposition
4. **Log file access** for security monitoring
5. **Handle large files** efficiently with streaming

### For Security
1. **Never trust file paths** from user input
2. **Validate delivery ownership** for every request
3. **Use proper content types** to prevent MIME confusion
4. **Implement rate limiting** for file access
5. **Monitor file access patterns** for suspicious activity

## Configuration

### Application Properties
```yaml
# File serving configuration
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
```

### Security Configuration
The file endpoints inherit security from the main application configuration and use the same JWT authentication system.

## Testing

### Manual Testing
```bash
# Test file viewing
curl -X GET "http://localhost:8080/api/files/view?path=exercise-deliveries/1/1/test.pdf&deliveryId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test file download
curl -X GET "http://localhost:8080/api/files/download?path=exercise-deliveries/1/1/test.pdf&deliveryId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output test_download.pdf

# Test file info
curl -X GET "http://localhost:8080/api/files/info/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Unit Testing
Create unit tests for:
- File path validation
- Access control logic
- Content type detection
- Error handling scenarios

## Troubleshooting

### Common Issues
1. **File not found**: Check if the file path is correct and the file exists
2. **Access denied**: Verify user permissions and delivery ownership
3. **Content type issues**: Ensure file extension matches actual content
4. **Large file timeouts**: Consider implementing streaming for large files

### Debug Information
Enable debug logging for file operations:
```yaml
logging:
  level:
    app.rest.FileRest: DEBUG
```

This will provide detailed information about file access attempts and any issues encountered.
