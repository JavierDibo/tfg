package app.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOClase;
import app.dtos.DTOCurso;
import app.dtos.DTOParametrosBusquedaClase;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOPeticionCrearCurso;
import app.dtos.DTOPeticionCrearTaller;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOTaller;
import app.excepciones.ResourceNotFoundException;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for basic CRUD operations of classes (Courses and Workshops)
 * Handles creation, reading, updating and deletion of classes
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "API for basic management of classes, courses and workshops")
public class ClaseRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== READ OPERATIONS =====

    /**
     * Gets classes according to the authenticated user's role:
     * - ADMIN: all classes
     * - PROFESSOR: only the classes they teach
     * - STUDENT: all classes (catalog)
     */
    @GetMapping
    @Operation(summary = "Get classes", description = "Gets classes filtered according to the user's role")
    public ResponseEntity<List<DTOClase>> obtenerClases() {
        return ResponseEntity.ok(servicioClase.obtenerClasesSegunRol());
    }

    /**
     * Gets a class by its ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID", description = "Gets a specific class by its ID")
    public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
        DTOClase clase = servicioClase.obtenerClasePorId(id);
        if (clase == null) {
            throw new ResourceNotFoundException("Class", "ID", id);
        }
        return ResponseEntity.ok(clase);
    }

    /**
     * Gets a class by its title
     */
    @GetMapping("/titulo/{titulo}")
    @Operation(summary = "Get class by title", description = "Gets a specific class by its title")
    public ResponseEntity<DTOClase> obtenerClasePorTitulo(@PathVariable String titulo) {
        return ResponseEntity.ok(servicioClase.obtenerClasePorTitulo(titulo));
    }

    /**
     * Searches for classes by title according to the authenticated user's role
     */
    @GetMapping("/buscar")
    @Operation(summary = "Search classes by title", description = "Searches for classes by title filtered according to the role")
    public ResponseEntity<List<DTOClase>> buscarClasesPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(servicioClase.buscarClasesPorTituloSegunRol(titulo));
    }

    /**
     * Searches for classes with general term according to the authenticated user's role
     */
    @GetMapping("/buscar/general")
    @Operation(summary = "Search classes with general term", description = "Searches for classes with general term in title and description")
    public ResponseEntity<List<DTOClase>> buscarClasesPorTerminoGeneral(@RequestParam String q) {
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase();
        // Create a new instance with the general search term
        parametros = new DTOParametrosBusquedaClase(q, null, null, null, null, null, null, null, 0, 100, "id", "ASC");
        return ResponseEntity.ok(servicioClase.buscarClasesSegunRol(parametros).content());
    }

    /**
     * Searches for classes with pagination and filters according to the authenticated user's role
     */
    @PostMapping("/buscar")
    @Operation(summary = "Search classes with filters", description = "Searches for classes with pagination and advanced filters")
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> buscarClases(
            @Valid @RequestBody DTOParametrosBusquedaClase parametros) {
        return ResponseEntity.ok(servicioClase.buscarClasesSegunRol(parametros));
    }

    // ===== CREATION OPERATIONS =====

    /**
     * Creates a new course
     */
    @PostMapping("/cursos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create course", description = "Creates a new course. Requires ADMIN or PROFESOR permissions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course created successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
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
     * Creates a new workshop
     */
    @PostMapping("/talleres")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create workshop", description = "Creates a new workshop")
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

    // ===== DELETION OPERATIONS =====

    /**
     * Deletes a class by its ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class by ID", description = "Deletes a class by its ID")
    public ResponseEntity<Void> borrarClasePorId(@PathVariable Long id) {
        boolean resultado = servicioClase.borrarClasePorId(id);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Deletes a class by its title
     */
    @DeleteMapping("/titulo/{titulo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class by title", description = "Deletes a class by its title")
    public ResponseEntity<Void> borrarClasePorTitulo(@PathVariable String titulo) {
        boolean resultado = servicioClase.borrarClasePorTitulo(titulo);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ===== STATISTICAL CONSULTATION OPERATIONS =====

    /**
     * Gets the number of students in a class
     */
    @GetMapping("/{claseId}/alumnos/contar")
    @Operation(summary = "Count students in class", description = "Gets the number of students enrolled in a class")
    public ResponseEntity<Integer> contarAlumnosEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarAlumnosEnClase(claseId));
    }

    /**
     * Gets the number of teachers in a class
     */
    @GetMapping("/{claseId}/profesores/contar")
    @Operation(summary = "Count teachers in class", description = "Gets the number of teachers in a class")
    public ResponseEntity<Integer> contarProfesoresEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarProfesoresEnClase(claseId));
    }

    /**
     * Gets classes by student
     */
    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Get classes by student", description = "Gets all classes for a specific student")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorAlumno(@PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorAlumno(alumnoId));
    }

    /**
     * Gets classes by teacher
     */
    @GetMapping("/profesor/{profesorId}")
    @Operation(summary = "Get classes by teacher", description = "Gets all classes for a specific teacher")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorProfesor(@PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }
}
