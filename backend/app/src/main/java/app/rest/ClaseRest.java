package app.rest;

import app.dtos.*;
import app.entidades.Material;
import app.excepciones.ResourceNotFoundException;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para operaciones CRUD básicas de clases (Cursos y Talleres)
 * Maneja la creación, lectura, actualización y eliminación de clases
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
@Tag(name = "Clases", description = "API para gestión básica de clases, cursos y talleres")
public class ClaseRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== OPERACIONES DE LECTURA =====

    /**
     * Obtiene clases según el rol del usuario autenticado:
     * - ADMIN: todas las clases
     * - PROFESOR: solo las clases que imparte
     * - ALUMNO: todas las clases (catálogo)
     */
    @GetMapping
    @Operation(summary = "Obtener clases", description = "Obtiene clases filtradas según el rol del usuario")
    public ResponseEntity<List<DTOClase>> obtenerClases() {
        return ResponseEntity.ok(servicioClase.obtenerClasesSegunRol());
    }

    /**
     * Obtiene una clase por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener clase por ID", description = "Obtiene una clase específica por su ID")
    public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
        DTOClase clase = servicioClase.obtenerClasePorId(id);
        if (clase == null) {
            throw new ResourceNotFoundException("Clase", "ID", id);
        }
        return ResponseEntity.ok(clase);
    }

    /**
     * Obtiene una clase por su título
     */
    @GetMapping("/titulo/{titulo}")
    @Operation(summary = "Obtener clase por título", description = "Obtiene una clase específica por su título")
    public ResponseEntity<DTOClase> obtenerClasePorTitulo(@PathVariable String titulo) {
        return ResponseEntity.ok(servicioClase.obtenerClasePorTitulo(titulo));
    }

    /**
     * Busca clases por título según el rol del usuario autenticado
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar clases por título", description = "Busca clases por título filtradas según el rol")
    public ResponseEntity<List<DTOClase>> buscarClasesPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(servicioClase.buscarClasesPorTituloSegunRol(titulo));
    }

    /**
     * Busca clases con paginación y filtros según el rol del usuario autenticado
     */
    @PostMapping("/buscar")
    @Operation(summary = "Buscar clases con filtros", description = "Busca clases con paginación y filtros avanzados")
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> buscarClases(
            @Valid @RequestBody DTOParametrosBusquedaClase parametros) {
        return ResponseEntity.ok(servicioClase.buscarClasesSegunRol(parametros));
    }

    // ===== OPERACIONES DE CREACIÓN =====

    /**
     * Crea un nuevo curso
     */
    @PostMapping("/cursos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Crear curso", description = "Crea un nuevo curso")
    public ResponseEntity<DTOCurso> crearCurso(@Valid @RequestBody DTOPeticionCrearCurso peticion) {
        DTOCurso curso = servicioClase.crearCurso(
                new DTOPeticionCrearClase(
                        peticion.titulo(),
                        peticion.descripcion(),
                        peticion.precio(),
                        peticion.presencialidad(),
                        peticion.imagenPortada(),
                        peticion.nivel(),
                        peticion.profesoresId(),
                        peticion.material()
                ),
                peticion.fechaInicio(),
                peticion.fechaFin()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(curso);
    }

    /**
     * Crea un nuevo taller
     */
    @PostMapping("/talleres")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Crear taller", description = "Crea un nuevo taller")
    public ResponseEntity<DTOTaller> crearTaller(@Valid @RequestBody DTOPeticionCrearTaller peticion) {
        DTOTaller taller = servicioClase.crearTaller(
                new DTOPeticionCrearClase(
                        peticion.titulo(),
                        peticion.descripcion(),
                        peticion.precio(),
                        peticion.presencialidad(),
                        peticion.imagenPortada(),
                        peticion.nivel(),
                        peticion.profesoresId(),
                        peticion.material()
                ),
                peticion.duracionHoras(),
                peticion.fechaRealizacion(),
                peticion.horaComienzo()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taller);
    }

    // ===== OPERACIONES DE ELIMINACIÓN =====

    /**
     * Borra una clase por su ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar clase por ID", description = "Elimina una clase por su ID")
    public ResponseEntity<Void> borrarClasePorId(@PathVariable Long id) {
        boolean resultado = servicioClase.borrarClasePorId(id);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Borra una clase por su título
     */
    @DeleteMapping("/titulo/{titulo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar clase por título", description = "Elimina una clase por su título")
    public ResponseEntity<Void> borrarClasePorTitulo(@PathVariable String titulo) {
        boolean resultado = servicioClase.borrarClasePorTitulo(titulo);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ===== OPERACIONES DE CONSULTA ESTADÍSTICA =====

    /**
     * Obtiene el número de alumnos en una clase
     */
    @GetMapping("/{claseId}/alumnos/contar")
    @Operation(summary = "Contar alumnos en clase", description = "Obtiene el número de alumnos inscritos en una clase")
    public ResponseEntity<Integer> contarAlumnosEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarAlumnosEnClase(claseId));
    }

    /**
     * Obtiene el número de profesores en una clase
     */
    @GetMapping("/{claseId}/profesores/contar")
    @Operation(summary = "Contar profesores en clase", description = "Obtiene el número de profesores de una clase")
    public ResponseEntity<Integer> contarProfesoresEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarProfesoresEnClase(claseId));
    }

    /**
     * Obtiene las clases por alumno
     */
    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener clases por alumno", description = "Obtiene todas las clases de un alumno específico")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorAlumno(@PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorAlumno(alumnoId));
    }

    /**
     * Obtiene las clases por profesor
     */
    @GetMapping("/profesor/{profesorId}")
    @Operation(summary = "Obtener clases por profesor", description = "Obtiene todas las clases de un profesor específico")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorProfesor(@PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }
}
