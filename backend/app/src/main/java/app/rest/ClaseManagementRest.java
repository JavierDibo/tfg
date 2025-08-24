package app.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOClase;
import app.entidades.Material;
import app.servicios.ServicioClase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para gestión de elementos de clases
 * Maneja operaciones de gestión como agregar/remover profesores, ejercicios y materiales
 */
@RestController
@RequestMapping("/api/clases/{claseId}")
@RequiredArgsConstructor
@Tag(name = "Gestión de Clases", description = "API para gestión de elementos de clases")
public class ClaseManagementRest {

    private final ServicioClase servicioClase;

    // ===== GESTIÓN DE PROFESORES =====

    /**
     * Agrega un profesor a una clase
     */
    @PostMapping("/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Agregar profesor a clase", description = "Agrega un profesor a una clase específica")
    public ResponseEntity<DTOClase> agregarProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.agregarProfesor(claseId, profesorId));
    }

    /**
     * Remueve un profesor de una clase
     */
    @DeleteMapping("/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover profesor de clase", description = "Remueve un profesor de una clase específica")
    public ResponseEntity<DTOClase> removerProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.removerProfesor(claseId, profesorId));
    }

    // ===== GESTIÓN DE EJERCICIOS =====

    /**
     * Agrega un ejercicio a una clase
     */
    @PostMapping("/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Agregar ejercicio a clase", description = "Agrega un ejercicio a una clase específica")
    public ResponseEntity<DTOClase> agregarEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.agregarEjercicio(claseId, ejercicioId));
    }

    /**
     * Remueve un ejercicio de una clase
     */
    @DeleteMapping("/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Remover ejercicio de clase", description = "Remueve un ejercicio de una clase específica")
    public ResponseEntity<DTOClase> removerEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.removerEjercicio(claseId, ejercicioId));
    }

    // ===== GESTIÓN DE MATERIALES =====

    /**
     * Agrega material a una clase
     */
    @PostMapping("/material")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Agregar material a clase", description = "Agrega material a una clase específica")
    public ResponseEntity<DTOClase> agregarMaterial(
            @PathVariable Long claseId,
            @Valid @RequestBody Material material) {
        return ResponseEntity.ok(servicioClase.agregarMaterial(claseId, material));
    }

    /**
     * Remueve material de una clase
     */
    @DeleteMapping("/material/{materialId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Remover material de clase", description = "Remueve material de una clase específica")
    public ResponseEntity<DTOClase> removerMaterial(
            @PathVariable Long claseId,
            @PathVariable String materialId) {
        return ResponseEntity.ok(servicioClase.removerMaterial(claseId, materialId));
    }

    // ===== GESTIÓN DE ALUMNOS (OPERACIONES DIRECTAS) =====

    /**
     * Agrega un alumno a una clase (operación directa)
     */
    @PostMapping("/alumnos/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Agregar alumno a clase", description = "Agrega un alumno directamente a una clase")
    public ResponseEntity<DTOClase> agregarAlumno(
            @PathVariable Long claseId,
            @PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.agregarAlumno(claseId, alumnoId));
    }

    /**
     * Remueve un alumno de una clase (operación directa)
     */
    @DeleteMapping("/alumnos/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Remover alumno de clase", description = "Remueve un alumno directamente de una clase")
    public ResponseEntity<DTOClase> removerAlumno(
            @PathVariable Long claseId,
            @PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.removerAlumno(claseId, alumnoId));
    }
}
