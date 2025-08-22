# Guía de API de Paginación - Academia App

## Introducción

Se ha implementado paginación en la API de alumnos siguiendo las mejores prácticas de Spring Boot y REST. Los endpoints originales se mantienen por compatibilidad (marcados como `@Deprecated`), pero se recomienda usar los nuevos endpoints con paginación.

## Endpoints con Paginación

### 1. Obtener todos los alumnos (con filtros opcionales)

**Endpoint:** `GET /api/alumnos/paged`

**Parámetros de búsqueda (opcionales):**
- `nombre`: Filtrar por nombre (contiene)
- `apellidos`: Filtrar por apellidos (contiene)
- `dni`: Filtrar por DNI (contiene)
- `email`: Filtrar por email (contiene)
- `matriculado`: Filtrar por estado de matrícula (true/false)

**Parámetros de paginación:**
- `page`: Número de página (0-indexed, por defecto: 0)
- `size`: Tamaño de página (1-100, por defecto: 20)
- `sortBy`: Campo para ordenar (por defecto: "id")
- `sortDirection`: Dirección del ordenamiento ("ASC" o "DESC", por defecto: "ASC")

**Ejemplo:**
```
GET /api/alumnos/paged?page=0&size=20&sortBy=nombre&sortDirection=ASC&matriculado=true
```

### 2. Obtener alumnos matriculados

**Endpoint:** `GET /api/alumnos/matriculados/paged`

**Parámetros:**
- `page`, `size`, `sortBy`, `sortDirection` (mismos que arriba)

**Ejemplo:**
```
GET /api/alumnos/matriculados/paged?page=2&size=10
```

### 3. Obtener alumnos no matriculados

**Endpoint:** `GET /api/alumnos/no-matriculados/paged`

**Parámetros:**
- `page`, `size`, `sortBy`, `sortDirection` (mismos que arriba)

## Estructura de Respuesta

Todos los endpoints con paginación devuelven una respuesta con esta estructura:

```json
{
  "content": [
    {
      "id": 1,
      "usuario": "alumno1",
      "nombre": "Juan",
      "apellidos": "Pérez García",
      "dni": "12345678A",
      "email": "juan.perez@email.com",
      "numeroTelefono": "+34123456789",
      "matriculado": true,
      "enabled": true,
      "fechaCreacion": "2024-01-01T10:00:00"
    }
    // ... más alumnos
  ],
  "page": {
    "number": 0,           // Página actual (0-indexed)
    "size": 20,            // Tamaño de página
    "totalElements": 1000, // Total de elementos en todas las páginas
    "totalPages": 50,      // Total de páginas
    "first": true,         // Es la primera página
    "last": false,         // Es la última página
    "hasNext": true,       // Tiene página siguiente
    "hasPrevious": false   // Tiene página anterior
  }
}
```

## Campos Válidos para Ordenamiento

Puedes ordenar por cualquiera de estos campos:
- `id`
- `usuario` 
- `nombre`
- `apellidos`
- `dni`
- `email`
- `matriculado`
- `fechaCreacion`

## Limitaciones

- **Tamaño máximo de página:** 100 elementos
- **Tamaño por defecto:** 20 elementos
- **Ordenamiento por defecto:** Por ID ascendente

## Ejemplos de Uso

### Obtener la primera página con 10 alumnos ordenados por nombre:
```
GET /api/alumnos/paged?page=0&size=10&sortBy=nombre&sortDirection=ASC
```

### Buscar alumnos por nombre "Juan" en la página 2:
```
GET /api/alumnos/paged?nombre=Juan&page=1&size=20
```

### Obtener alumnos matriculados ordenados por fecha de creación (descendente):
```
GET /api/alumnos/matriculados/paged?sortBy=fechaCreacion&sortDirection=DESC
```

## Migración desde Endpoints Sin Paginación

Los endpoints originales sin paginación (`/api/alumnos`, `/api/alumnos/matriculados`, `/api/alumnos/no-matriculados`) siguen funcionando pero están marcados como deprecated. Se recomienda migrar a los nuevos endpoints:

- `GET /api/alumnos` → `GET /api/alumnos/paged`
- `GET /api/alumnos/matriculados` → `GET /api/alumnos/matriculados/paged`
- `GET /api/alumnos/no-matriculados` → `GET /api/alumnos/no-matriculados/paged`

## Beneficios de la Paginación

1. **Rendimiento mejorado:** Reduce la carga del servidor y el tiempo de respuesta
2. **Experiencia de usuario:** Permite navegación más eficiente en grandes conjuntos de datos
3. **Escalabilidad:** La aplicación puede manejar miles de registros sin problemas
4. **Estándares REST:** Sigue las mejores prácticas de la industria

## Autorización

Los endpoints de paginación mantienen la misma autorización que los originales:
- **ADMIN y PROFESOR:** Pueden acceder a todos los endpoints
- **ALUMNO:** Solo puede acceder a sus propios datos (endpoints específicos por ID/usuario)
