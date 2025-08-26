# Exercise Delivery Comments Guide

This guide explains how to use the RESTful API endpoints to grade exercise deliveries and add teacher comments.

## Overview

The exercise delivery system now supports teacher comments when grading. All functionality is available through standard REST endpoints without using verbs in the URL.

## Available Endpoints

### 1. Grade a Delivery with Comments
**Endpoint:** `PATCH /api/entregas/{id}`  
**Method:** PATCH  
**Role Required:** ADMIN or PROFESOR  

**Request Body:**
```json
{
  "nota": 8.5,
  "comentarios": "Excelente trabajo. Has demostrado un dominio completo del tema. La implementación es elegante y eficiente."
}
```

**Response:**
```json
{
  "id": 123,
  "nota": 8.50,
  "fechaEntrega": "2024-08-25T10:30:00",
  "estado": "CALIFICADO",
  "archivosEntregados": ["tarea_1.pdf", "codigo.java"],
  "alumnoEntreganteId": "456",
  "ejercicioId": "789",
  "numeroArchivos": 2,
  "comentarios": "Excelente trabajo. Has demostrado un dominio completo del tema. La implementación es elegante y eficiente."
}
```

### 2. Add Comments Only (without grading)
**Endpoint:** `PATCH /api/entregas/{id}`  
**Method:** PATCH  
**Role Required:** ADMIN or PROFESOR  

**Request Body:**
```json
{
  "comentarios": "Buen trabajo en general, pero considera revisar la documentación del código."
}
```

### 3. Grade Only (without comments)
**Endpoint:** `PATCH /api/entregas/{id}`  
**Method:** PATCH  
**Role Required:** ADMIN or PROFESOR  

**Request Body:**
```json
{
  "nota": 7.0
}
```

### 4. Update Files (Students only)
**Endpoint:** `PATCH /api/entregas/{id}`  
**Method:** PATCH  
**Role Required:** ALUMNO (own delivery only)  

**Request Body:**
```json
{
  "archivosEntregados": ["nueva_version.pdf", "codigo_actualizado.java"]
}
```

## DTO Structure

### DTOPeticionActualizarEntregaEjercicio
```json
{
  "alumnoEntreganteId": "string (optional)",
  "ejercicioId": "string (optional)",
  "archivosEntregados": ["string"] (optional),
  "nota": "number (0.0-10.0)" (optional),
  "comentarios": "string (max 1000 chars)" (optional)
}
```

## Response DTO Structure

### DTOEntregaEjercicio
```json
{
  "id": "number",
  "nota": "number (0.0-10.0)",
  "fechaEntrega": "datetime",
  "estado": "PENDIENTE|ENTREGADO|CALIFICADO",
  "archivosEntregados": ["string"],
  "alumnoEntreganteId": "string",
  "ejercicioId": "string",
  "numeroArchivos": "number",
  "comentarios": "string"
}
```

## Helper Methods in DTO

The `DTOEntregaEjercicio` provides several helper methods:

- `tieneComentarios()`: Returns true if the delivery has comments
- `getComentariosFormateados()`: Returns formatted comments or "Sin comentarios"
- `getResumenComentarios()`: Returns first 100 characters of comments with "..." if truncated
- `estaCalificada()`: Returns true if the delivery is graded
- `getNotaFormateada()`: Returns formatted grade or "Sin calificar"
- `getCalificacionCualitativa()`: Returns qualitative grade (Excelente, Notable, Aprobado, Suspenso)

## Examples

### Example 1: Grade with Excellent Comments
```bash
curl -X PATCH "http://localhost:8080/api/entregas/123" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "nota": 9.5,
    "comentarios": "Trabajo sobresaliente. Has superado las expectativas y demostrado una comprensión excepcional del tema."
  }'
```

### Example 2: Add Comments to Existing Delivery
```bash
curl -X PATCH "http://localhost:8080/api/entregas/123" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "comentarios": "La solución es correcta pero considera optimizar el rendimiento en la función principal."
  }'
```

### Example 3: Update Grade Only
```bash
curl -X PATCH "http://localhost:8080/api/entregas/123" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "nota": 6.5
  }'
```

## Security Notes

- Only ADMIN and PROFESOR roles can grade deliveries and add comments
- Students can only update their own deliveries (files only)
- Comments are limited to 1000 characters
- Grades must be between 0.0 and 10.0
- All endpoints follow the established security patterns with `@PreAuthorize` annotations

## Data Initialization

The system includes sample comments for different grade ranges:
- **Excellent (9.0-10.0):** "Excelente trabajo. Has demostrado un dominio completo del tema."
- **Good (7.0-8.9):** "Buen trabajo. La solución es correcta con algunos detalles menores."
- **Average (5.0-6.9):** "Trabajo aceptable. Necesitas revisar algunos conceptos."
- **Poor (0.0-4.9):** "Necesitas revisar los conceptos fundamentales."

These comments are automatically assigned during data initialization when grading sample deliveries.
