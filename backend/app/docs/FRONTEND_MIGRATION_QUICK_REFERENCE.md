# Frontend Migration Quick Reference

## 🚨 Critical Changes Required

### 1. Update Download Function Call

**OLD:**
```javascript
downloadFile(filePath)
```

**NEW:**
```javascript
downloadFile(filePath, deliveryId, fileName)
```

### 2. Update API Endpoint

**OLD:**
```javascript
const url = `/api/entregas/download-file?filePath=${encodeURIComponent(filePath)}`;
```

**NEW:**
```javascript
const url = `/api/files/download?path=${encodeURIComponent(filePath)}&deliveryId=${deliveryId}`;
```

### 3. Add File Information Retrieval

**NEW:**
```javascript
const getFileInfo = async (deliveryId) => {
  const response = await fetch(`/api/files/info/${deliveryId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return await response.json();
};
```

## 📋 Migration Checklist

- [ ] **Replace download endpoint**: `/api/entregas/download-file` → `/api/files/download`
- [ ] **Add deliveryId parameter**: All download calls need delivery ID
- [ ] **Add file info endpoint**: Use `/api/files/info/{deliveryId}` for metadata
- [ ] **Update error handling**: Handle new error responses
- [ ] **Test file viewing**: Add inline viewing for images/PDFs

## 🔗 New API Endpoints

| Purpose | Endpoint | Parameters |
|---------|----------|------------|
| Download | `GET /api/files/download` | `path`, `deliveryId` |
| View | `GET /api/files/view` | `path`, `deliveryId` |
| Info | `GET /api/files/info/{deliveryId}` | `deliveryId` (path) |

## 📖 Full Documentation

See the complete migration guide: [Frontend File Viewing Migration Guide](./FRONTEND_FILE_VIEWING_MIGRATION_GUIDE.md)

## ⚠️ Breaking Changes

- ❌ `/api/entregas/download-file` no longer exists
- ❌ `downloadFile(filePath)` signature changed
- ❌ Direct file path access no longer works

## ✅ Benefits

- 🔒 **Enhanced Security**: File ownership validation
- 📊 **Rich Metadata**: File size, type, format info
- 👁️ **Inline Viewing**: View images/PDFs in browser
- 🛡️ **Better Error Handling**: Clear error messages
