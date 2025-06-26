# Services Architecture

This directory contains all the service layer files that handle API communication and business logic.

## Structure

```
src/lib/services/
├── entidadService.ts     # Entity management service
└── README.md            # This file
```

## Service Pattern

Our services follow a consistent pattern:

### 1. Interface Definitions
- Define TypeScript interfaces for data models
- Define response types for API operations

### 2. Service Class
- Encapsulate all API calls related to a specific domain
- Handle authentication (if needed)
- Provide consistent error handling
- Return standardized response formats

### 3. Singleton Export
- Export a single instance of the service
- This provides a consistent API across the application

## Example: EntidadService

```typescript
// 1. Define interfaces
export interface Entidad {
    id: number;
    info: string;
}

export interface ApiResponse {
    success: boolean;
    message: string;
    data?: any;
}

// 2. Service class with encapsulated logic
class EntidadService {
    private getHeaders() {
        // Centralized auth logic
    }

    async createEntity(info: string): Promise<ApiResponse> {
        // Implementation with error handling
    }
    
    // ... other methods
}

// 3. Singleton export
export const entidadService = new EntidadService();
```

## Best Practices

### 1. Separation of Concerns
- **Services**: Handle API communication and data transformation
- **Components**: Handle UI logic and state management
- **Server files**: Handle SSR data loading

### 2. Error Handling
- Services should catch and transform errors into user-friendly messages
- Return consistent response objects with success/error states
- Log detailed errors for debugging while showing simple messages to users

### 3. Type Safety
- Always define TypeScript interfaces for your data models
- Use strict typing for all service methods
- Export types for use in components

### 4. Authentication
- Centralize authentication logic in service base methods
- Keep credentials and tokens out of components
- Handle auth errors consistently across all services

### 5. Caching and State
- Services should be stateless
- Let components manage local state
- Consider using stores for global state if needed

## Adding New Services

When creating a new service:

1. Create a new file: `src/lib/services/yourDomainService.ts`
2. Follow the pattern shown in `entidadService.ts`
3. Export the service and types in `src/lib/index.ts`
4. Create components that use the service without knowing about HTTP details

## Usage in Components

```typescript
// In your Svelte component
import { yourService, type YourType } from '$lib/services/yourDomainService';

// Or via the barrel export
import { yourService, type YourType } from '$lib';
```

## Testing Services

Services should be unit tested independently:
- Mock HTTP responses
- Test error conditions
- Verify correct data transformation
- Test authentication flows 