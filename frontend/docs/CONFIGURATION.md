# Configuration Management

This document explains how the centralized configuration system works in the Academia App frontend.

## Overview

The application uses a centralized configuration system to manage API endpoints and other configuration values. This makes it easy to change settings across the entire application by updating a single file.

## Configuration Files

### Main Configuration File
- **Location**: `src/lib/config.ts`
- **Purpose**: Central repository for all API configuration
- **Key Values**:
  - `basePath`: The base URL for all API endpoints
  - `getApiBasePath()`: Function that returns the current base path (supports future environment variable integration)

### API Configuration File
- **Location**: `src/lib/api.ts`
- **Purpose**: Creates and exports all API instances using the centralized configuration
- **Features**:
  - Uses `getApiBasePath()` from the config
  - Includes authentication middleware
  - Exports all API instances for use throughout the application

## How to Change the API Base Path

### For Development
1. Edit `src/lib/config.ts`
2. Update the `basePath` value in `API_CONFIG`:
   ```typescript
   export const API_CONFIG = {
       basePath: 'http://your-new-api-url:port'
   } as const;
   ```

### For Production (Environment Variables)
1. Uncomment the environment variable usage in `getApiBasePath()`:
   ```typescript
   export const getApiBasePath = (): string => {
       return import.meta.env.VITE_API_BASE_URL || API_CONFIG.basePath;
   };
   ```
2. Set the `VITE_API_BASE_URL` environment variable in your deployment environment

## Files That Use the Centralized Configuration

### Service Files
All service files import API instances from `src/lib/api.ts`:
- `src/lib/services/alumnoService.ts` → uses `alumnoApi`
- `src/lib/services/profesorService.ts` → uses `profesorApi`
- `src/lib/services/claseService.ts` → uses `claseApi`
- `src/lib/services/materialService.ts` → uses `materialApi`
- `src/lib/services/ejercicioService.ts` → uses `ejercicioApi`
- `src/lib/services/entregaService.ts` → uses `entregaApi`
- `src/lib/services/enrollmentService.ts` → uses `classManagementApi` and `userOperationsApi`

### Build Configuration
- **Vite Config** (`vite.config.ts`): Uses `getApiBasePath()` for development proxy configuration
- **API Generation** (`scripts/generate-api.js`): Reads the config file to generate API client code
- **OpenAPI Tools** (`scripts/update-openapitools.js`): Updates OpenAPI configuration with the centralized base path

## Scripts

### Available NPM Scripts
- `npm run api`: Generates API client code using the centralized configuration
- `npm run update-config`: Updates OpenAPI tools configuration with the current base path
- `npm run postinstall`: Automatically runs configuration updates and API generation

### How Scripts Work
1. **API Generation Script** (`scripts/generate-api.js`):
   - Reads the base path from `src/lib/config.ts`
   - Generates TypeScript API client code
   - Uses the centralized configuration for the OpenAPI spec URL

2. **Configuration Update Script** (`scripts/update-openapitools.js`):
   - Reads the base path from `src/lib/config.ts`
   - Updates `openapitools.json` with the current configuration
   - Ensures build tools use the centralized configuration

## Benefits

✅ **Single Source of Truth**: All API configuration is managed in one place
✅ **Easy Maintenance**: Change the API URL once, updates everywhere
✅ **Environment Support**: Ready for environment variable integration
✅ **Consistent Configuration**: All tools and services use the same settings
✅ **Automated Updates**: Scripts automatically keep configuration in sync

## Migration Notes

If you're updating from the old system:
1. All service files now import from `src/lib/api.ts` instead of creating their own configurations
2. The vite proxy configuration now uses the centralized config
3. API generation scripts now read from the centralized config
4. No more hardcoded URLs scattered throughout the codebase

## Troubleshooting

### Common Issues
1. **API Generation Fails**: Ensure the backend is running and accessible at the configured base path
2. **Proxy Not Working**: Check that the vite config is using `getApiBasePath()` correctly
3. **Services Not Working**: Verify that services are importing from `src/lib/api.ts` and not creating their own configurations

### Debugging
- Check the console for API generation script output
- Verify the base path in `src/lib/config.ts` is correct
- Ensure all service files are using the centralized API instances
