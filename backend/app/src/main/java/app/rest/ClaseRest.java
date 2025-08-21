package app.rest;

import app.dtos.*;
import app.entidades.Material;
import app.servicios.ServicioClase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de clases (Cursos y Talleres)
 * Implementa la interfaz definida en el UML
 */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseRest {

    private final ServicioClase servicioClase;

    /**
     * Obtiene todas las clases
     * @return Lista de DTOClase
     */
    @GetMapping
    public ResponseEntity<List<DTOClase>> obtenerClases() {
        return ResponseEntity.ok(servicioClase.obtenerClases());
    }

    /**
     * Obtiene una clase por su ID
     * @param id ID de la clase
     * @return DTOClase
     */
    @GetMapping("/{id}")
    public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioClase.obtenerClasePorId(id));
    }

    /**
     * Obtiene una clase por su título
     * @param titulo Título de la clase
     * @return DTOClase
     */
    @GetMapping("/titulo/{titulo}")
    public ResponseEntity<DTOClase> obtenerClasePorTitulo(@PathVariable String titulo) {
        return ResponseEntity.ok(servicioClase.obtenerClasePorTitulo(titulo));
    }

    /**
     * Crea un nuevo curso
     * @param peticion Datos para crear el curso
     * @return DTOCurso con los datos del curso creado
     */
    @PostMapping("/cursos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
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
     * @param peticion Datos para crear el taller
     * @return DTOTaller con los datos del taller creado
     */
    @PostMapping("/talleres")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
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

    /**
     * Borra una clase por su ID
     * @param id ID de la clase
     * @return 204 No Content si se borró correctamente, 404 Not Found si no existe
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> borrarClasePorId(@PathVariable Long id) {
        boolean resultado = servicioClase.borrarClasePorId(id);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Borra una clase por su título
     * @param titulo Título de la clase
     * @return 204 No Content si se borró correctamente, 404 Not Found si no existe
     */
    @DeleteMapping("/titulo/{titulo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> borrarClasePorTitulo(@PathVariable String titulo) {
        boolean resultado = servicioClase.borrarClasePorTitulo(titulo);
        return resultado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Busca clases con paginación y filtros
     * @param parametros Parámetros de búsqueda
     * @return DTORespuestaPaginada con las clases encontradas
     */
    @PostMapping("/buscar")
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> buscarClases(
            @Valid @RequestBody DTOParametrosBusquedaClase parametros) {
        return ResponseEntity.ok(servicioClase.buscarClases(parametros));
    }

    /**
     * Agrega un alumno a una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    @PostMapping("/{claseId}/alumnos/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> agregarAlumno(
            @PathVariable Long claseId,
            @PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.agregarAlumno(claseId, alumnoId));
    }

    /**
     * Remueve un alumno de una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    @DeleteMapping("/{claseId}/alumnos/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> removerAlumno(
            @PathVariable Long claseId,
            @PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.removerAlumno(claseId, alumnoId));
    }

    /**
     * Permite a un alumno inscribirse en una clase
     * @param claseId ID de la clase
     * @return DTOClase actualizada
     */
    @PostMapping("/{claseId}/inscribirse")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DTOClase> inscribirseEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.inscribirseEnClase(claseId));
    }

    /**
     * Permite a un alumno darse de baja de una clase
     * @param claseId ID de la clase
     * @return DTOClase actualizada
     */
    @DeleteMapping("/{claseId}/darse-baja")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DTOClase> darseDeBajaDeClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.darseDeBajaDeClase(claseId));
    }

    /**
     * Agrega un profesor a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    @PostMapping("/{claseId}/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOClase> agregarProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.agregarProfesor(claseId, profesorId));
    }

    /**
     * Remueve un profesor de una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    @DeleteMapping("/{claseId}/profesores/{profesorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOClase> removerProfesor(
            @PathVariable Long claseId,
            @PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.removerProfesor(claseId, profesorId));
    }

    /**
     * Agrega un ejercicio a una clase
     * @param claseId ID de la clase
     * @param ejercicioId ID del ejercicio
     * @return DTOClase actualizada
     */
    @PostMapping("/{claseId}/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> agregarEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.agregarEjercicio(claseId, ejercicioId));
    }

    /**
     * Remueve un ejercicio de una clase
     * @param claseId ID de la clase
     * @param ejercicioId ID del ejercicio
     * @return DTOClase actualizada
     */
    @DeleteMapping("/{claseId}/ejercicios/{ejercicioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> removerEjercicio(
            @PathVariable Long claseId,
            @PathVariable String ejercicioId) {
        return ResponseEntity.ok(servicioClase.removerEjercicio(claseId, ejercicioId));
    }

    /**
     * Agrega material a una clase
     * @param claseId ID de la clase
     * @param material Material a agregar
     * @return DTOClase actualizada
     */
    @PostMapping("/{claseId}/material")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> agregarMaterial(
            @PathVariable Long claseId,
            @Valid @RequestBody Material material) {
        return ResponseEntity.ok(servicioClase.agregarMaterial(claseId, material));
    }

    /**
     * Remueve material de una clase
     * @param claseId ID de la clase
     * @param materialId ID del material
     * @return DTOClase actualizada
     */
    @DeleteMapping("/{claseId}/material/{materialId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOClase> removerMaterial(
            @PathVariable Long claseId,
            @PathVariable String materialId) {
        return ResponseEntity.ok(servicioClase.removerMaterial(claseId, materialId));
    }

    /**
     * Obtiene las clases por alumno
     * @param alumnoId ID del alumno
     * @return Lista de DTOClase
     */
    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorAlumno(@PathVariable String alumnoId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorAlumno(alumnoId));
    }

    /**
     * Obtiene las clases por profesor
     * @param profesorId ID del profesor
     * @return Lista de DTOClase
     */
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<DTOClase>> obtenerClasesPorProfesor(@PathVariable String profesorId) {
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }

    /**
     * Obtiene el número de alumnos en una clase
     * @param claseId ID de la clase
     * @return Número de alumnos
     */
    @GetMapping("/{claseId}/alumnos/contar")
    public ResponseEntity<Integer> contarAlumnosEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarAlumnosEnClase(claseId));
    }

    /**
     * Obtiene el número de profesores en una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    @GetMapping("/{claseId}/profesores/contar")
    public ResponseEntity<Integer> contarProfesoresEnClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(servicioClase.contarProfesoresEnClase(claseId));
    }
}
