# Frontend File Viewing Migration Guide

## Overview

The backend has been updated with a new file viewing and download system. This guide explains how to migrate from the old file access methods to the new secure file viewing endpoints.

## What Changed

### Old System (Deprecated)
- ❌ Direct file path access
- ❌ No security validation
- ❌ Limited file type support
- ❌ No file metadata

### New System (Current)
- ✅ Secure file access with authentication
- ✅ File ownership validation
- ✅ Comprehensive file metadata
- ✅ Inline viewing for supported file types
- ✅ Download support for all file types

## Migration Steps

### 1. Update File Download Function

**Old Code:**
```javascript
// ❌ OLD - Direct file path access
const downloadFile = async (filePath) => {
  const url = `/api/entregas/download-file?filePath=${encodeURIComponent(filePath)}`;
  
  try {
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
      link.download = getFileName(filePath);
      link.click();
      window.URL.revokeObjectURL(downloadUrl);
    }
  } catch (error) {
    console.error('Error downloading file:', error);
  }
};
```

**New Code:**
```javascript
// ✅ NEW - Secure file download with delivery validation
const downloadFile = async (filePath, deliveryId, fileName) => {
  const url = `/api/files/download?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
  
  try {
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
      link.download = fileName || getFileName(filePath);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);
    } else {
      throw new Error(`Download failed: ${response.status}`);
    }
  } catch (error) {
    console.error('Error downloading file:', error);
    throw new Error('Error al descargar el archivo');
  }
};
```

### 2. Add File Viewing Function

**New Code:**
```javascript
// ✅ NEW - Inline file viewing
const viewFile = async (filePath, deliveryId) => {
  const url = `/api/files/view?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
  
  // For images and PDFs, open in new window/tab
  if (isViewableFile(filePath)) {
    window.open(url, '_blank');
  } else {
    // For other files, trigger download
    await downloadFile(filePath, deliveryId);
  }
};

// Helper function to check if file can be viewed inline
const isViewableFile = (filePath) => {
  const extension = getFileExtension(filePath).toLowerCase();
  return ['pdf', 'png', 'jpg', 'jpeg', 'gif'].includes(extension);
};

// Helper function to get file extension
const getFileExtension = (filePath) => {
  return filePath.split('.').pop() || '';
};
```

### 3. Add File Information Retrieval

**New Code:**
```javascript
// ✅ NEW - Get detailed file information
const getFileInfo = async (deliveryId) => {
  const url = `/api/files/info/${deliveryId}`;
  
  try {
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.ok) {
      return await response.json();
    } else {
      throw new Error(`Failed to get file info: ${response.status}`);
    }
  } catch (error) {
    console.error('Error getting file info:', error);
    throw new Error('Error al obtener información del archivo');
  }
};
```

### 4. Update File List Component

**Old Code:**
```javascript
// ❌ OLD - Simple file list
const renderFileList = (files) => {
  return files.map(filePath => (
    <div key={filePath}>
      <span>{getFileName(filePath)}</span>
      <button onClick={() => downloadFile(filePath)}>
        Descargar
      </button>
    </div>
  ));
};
```

**New Code:**
```javascript
// ✅ NEW - Enhanced file list with metadata
const [fileInfos, setFileInfos] = useState([]);
const [loading, setLoading] = useState(true);

useEffect(() => {
  const loadFileInfo = async () => {
    try {
      setLoading(true);
      const info = await getFileInfo(deliveryId);
      setFileInfos(info);
    } catch (error) {
      console.error('Error loading file info:', error);
    } finally {
      setLoading(false);
    }
  };
  
  if (deliveryId) {
    loadFileInfo();
  }
}, [deliveryId]);

const renderFileList = () => {
  if (loading) return <div>Cargando archivos...</div>;
  
  return fileInfos.map(fileInfo => (
    <div key={fileInfo.filePath} className="file-item">
      <div className="file-info">
        <span className="file-icon">{getFileIcon(fileInfo)}</span>
        <div className="file-details">
          <div className="file-name">{fileInfo.fileName}</div>
          <div className="file-meta">
            {getFileTypeLabel(fileInfo)} • {fileInfo.formattedSize}
          </div>
        </div>
      </div>
      <div className="file-actions">
        {fileInfo.isImage || fileInfo.isPdf ? (
          <button onClick={() => viewFile(fileInfo.filePath, deliveryId)}>
            Ver
          </button>
        ) : null}
        <button onClick={() => downloadFile(fileInfo.filePath, deliveryId, fileInfo.fileName)}>
          Descargar
        </button>
      </div>
    </div>
  ));
};

const getFileIcon = (fileInfo) => {
  if (fileInfo.isImage) return '🖼️';
  if (fileInfo.isPdf) return '📄';
  return '📎';
};

const getFileTypeLabel = (fileInfo) => {
  if (fileInfo.isImage) return 'Imagen';
  if (fileInfo.isPdf) return 'PDF';
  return 'Documento';
};
```

### 5. Add File Viewer Modal

**New Code:**
```javascript
// ✅ NEW - Modal file viewer
const [selectedFile, setSelectedFile] = useState(null);
const [showModal, setShowModal] = useState(false);

const openFileViewer = (fileInfo) => {
  setSelectedFile(fileInfo);
  setShowModal(true);
};

const FileViewerModal = () => {
  if (!showModal || !selectedFile) return null;
  
  return (
    <div className="modal-overlay" onClick={() => setShowModal(false)}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{selectedFile.fileName}</h3>
          <button onClick={() => setShowModal(false)}>×</button>
        </div>
        <div className="modal-body">
          {selectedFile.isImage ? (
            <img 
              src={`/api/files/view?path=${encodeURIComponent(selectedFile.filePath)}&deliveryId=${deliveryId}`}
              alt={selectedFile.fileName}
              className="file-preview"
            />
          ) : selectedFile.isPdf ? (
            <iframe 
              src={`/api/files/view?path=${encodeURIComponent(selectedFile.filePath)}&deliveryId=${deliveryId}`}
              className="file-preview"
              title={selectedFile.fileName}
            />
          ) : (
            <div className="no-preview">
              <p>Vista previa no disponible para este tipo de archivo.</p>
              <button onClick={() => downloadFile(selectedFile.filePath, deliveryId, selectedFile.fileName)}>
                Descargar archivo
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
```

## Svelte Component Example

Here's a complete Svelte component example:

```svelte
<script>
  import { onMount } from 'svelte';
  
  export let deliveryId;
  export let delivery;
  
  let fileInfos = [];
  let selectedFile = null;
  let showModal = false;
  let loading = true;
  
  onMount(async () => {
    if (deliveryId) {
      await loadFileInfo();
    }
  });
  
  async function loadFileInfo() {
    try {
      loading = true;
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
    } finally {
      loading = false;
    }
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
  
  function viewFile(fileInfo) {
    selectedFile = fileInfo;
    showModal = true;
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
  
  function getToken() {
    // Implement your token retrieval logic
    return localStorage.getItem('token') || '';
  }
</script>

<!-- File List -->
{#if loading}
  <div class="loading">Cargando archivos...</div>
{:else}
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
{/if}

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
  
  .loading {
    text-align: center;
    padding: 2rem;
    color: #666;
  }
</style>
```

## API Endpoints Reference

### 1. Download File
```
GET /api/files/download?path={filePath}&deliveryId={deliveryId}
```

**Parameters:**
- `path` (String): File path relative to uploads directory
- `deliveryId` (Long): ID of the delivery containing the file

**Headers:**
- `Authorization: Bearer {token}`

### 2. View File (Inline)
```
GET /api/files/view?path={filePath}&deliveryId={deliveryId}
```

**Parameters:**
- `path` (String): File path relative to uploads directory
- `deliveryId` (Long): ID of the delivery containing the file

**Headers:**
- `Authorization: Bearer {token}`

### 3. Get File Information
```
GET /api/files/info/{deliveryId}
```

**Parameters:**
- `deliveryId` (Long): ID of the delivery

**Headers:**
- `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "fileName": "document.pdf",
    "filePath": "exercise-deliveries/12/10/delivery_20250829_232451_93b76706.pdf",
    "extension": "pdf",
    "contentType": "application/pdf",
    "fileSize": 544450,
    "formattedSize": "531.7 KB",
    "isImage": false,
    "isPdf": true
  }
]
```

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "timestamp": "2025-08-29T23:25:08.6929345",
  "status": 400,
  "error": "Bad Request",
  "message": "File path is required",
  "path": "/api/files/download"
}
```

**403 Forbidden:**
```json
{
  "timestamp": "2025-08-29T23:25:08.6929345",
  "status": 403,
  "error": "Forbidden",
  "message": "File does not belong to delivery",
  "path": "/api/files/download"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2025-08-29T23:25:08.6929345",
  "status": 404,
  "error": "Not Found",
  "message": "File not found or not readable",
  "path": "/api/files/download"
}
```

### Error Handling Example

```javascript
const downloadFile = async (filePath, deliveryId, fileName) => {
  const url = `/api/files/download?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
  
  try {
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
    } else {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Error al descargar el archivo');
    }
  } catch (error) {
    console.error('Error downloading file:', error);
    // Show user-friendly error message
    showError(error.message);
  }
};
```

## Migration Checklist

- [ ] Update all `downloadFile` function calls to include `deliveryId` parameter
- [ ] Replace `/api/entregas/download-file` URLs with `/api/files/download`
- [ ] Add file information retrieval using `/api/files/info/{deliveryId}`
- [ ] Implement file viewing functionality for images and PDFs
- [ ] Add proper error handling for all file operations
- [ ] Update file list components to show file metadata
- [ ] Add file viewer modal for inline viewing
- [ ] Test all file operations with different file types
- [ ] Verify security by testing with unauthorized access attempts

## Benefits of the New System

1. **Enhanced Security**: File access is validated against delivery ownership
2. **Better UX**: Inline viewing for supported file types
3. **Rich Metadata**: File size, type, and format information
4. **Proper Error Handling**: Clear error messages for different scenarios
5. **Future-Proof**: Extensible system for additional file types

## Support

If you encounter any issues during migration, please refer to:
- [File Viewing and Download Guide](../docs/FILE_VIEWING_AND_DOWNLOAD_GUIDE.md)
- [File Viewing Implementation Summary](../docs/FILE_VIEWING_IMPLEMENTATION_SUMMARY.md)

For technical questions, contact the backend development team.
