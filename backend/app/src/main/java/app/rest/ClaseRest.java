package app.rest;

import app.dtos.*;
import app.dtos.DTOAlumno;
import app.dtos.DTOClaseConDetalles;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOEstadoInscripcion;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTORespuestaEnrollment;
import app.entidades.Material;
import app.servicios.ServicioClase;
import app.servicios.ServicioAlumno;
import app.util.SecurityUtils;
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
    private final ServicioAlumno servicioAlumno;
    private final SecurityUtils securityUtils;

    /**
     * Obtiene clases según el rol del usuario autenticado:
     * - ADMIN: todas las clases
     * - PROFESOR: solo las clases que imparte
     * - ALUMNO: todas las clases (catálogo)
     * @return Lista de DTOClase filtrada según el rol
     */
    @GetMapping
    public ResponseEntity<List<DTOClase>> obtenerClases() {
        return ResponseEntity.ok(servicioClase.obtenerClasesSegunRol());
    }

    /**
     * Busca clases por título según el rol del usuario autenticado
     * @param titulo Título de la clase a buscar
     * @return Lista de DTOClase filtrada según el rol
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<DTOClase>> buscarClasesPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(servicioClase.buscarClasesPorTituloSegunRol(titulo));
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
     * Obtiene una clase por su ID
     * @param id ID de la clase
     * @return DTOClase
     */
    @GetMapping("/{id}")
    public ResponseEntity<DTOClase> obtenerClasePorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioClase.obtenerClasePorId(id));
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
     * Busca clases con paginación y filtros según el rol del usuario autenticado
     * @param parametros Parámetros de búsqueda
     * @return DTORespuestaPaginada con las clases encontradas filtradas según el rol
     */
    @PostMapping("/buscar")
    public ResponseEntity<DTORespuestaPaginada<DTOClase>> buscarClases(
            @Valid @RequestBody DTOParametrosBusquedaClase parametros) {
        return ResponseEntity.ok(servicioClase.buscarClasesSegunRol(parametros));
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
     * Endpoint específico para que los profesores inscriban alumnos en sus clases
     * @param peticion DTO con los datos de la inscripción
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @PostMapping("/enrollment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnClase(
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        DTORespuestaEnrollment respuesta = servicioClase.inscribirAlumnoEnClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
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
     * Endpoint específico para que los profesores den de baja alumnos de sus clases
     * @param peticion DTO con los datos de la baja
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @DeleteMapping("/enrollment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeClase(
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
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
     * Obtiene las clases del profesor autenticado
     * @return Lista de DTOClase del profesor actual
     */
    @GetMapping("/mis-clases")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<DTOClase>> obtenerMisClases() {
        String profesorId = securityUtils.getCurrentUserId().toString();
        return ResponseEntity.ok(servicioClase.obtenerClasesPorProfesor(profesorId));
    }

    /**
     * Obtiene las clases en las que está inscrito el estudiante autenticado
     * @return Lista de DTOClaseInscrita del estudiante actual
     */
    @GetMapping("/mis-clases-inscritas")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<DTOClaseInscrita>> obtenerMisClasesInscritas() {
        Long alumnoId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(servicioClase.obtenerClasesInscritasConDetalles(alumnoId));
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
     * Obtiene la lista de alumnos inscritos en una clase con paginación
     * @param claseId ID de la clase
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return DTORespuestaPaginada con los alumnos de la clase
     */
    @GetMapping("/{claseId}/alumnos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    public ResponseEntity<DTORespuestaPaginada<?>> obtenerAlumnosDeClase(
            @PathVariable Long claseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        // Obtener información del usuario autenticado
        String userRole = securityUtils.getCurrentUser().getRol().name();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        return ResponseEntity.ok(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(
            claseId, page, size, sortBy, sortDirection, userRole, currentUserId));
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

    // ===== NUEVOS ENDPOINTS PARA ESTUDIANTES =====

    /**
     * Verifica si un estudiante está inscrito en una clase específica
     * GET /api/clases/{claseId}/enrollment-status/{alumnoId}
     * @param claseId ID de la clase
     * @param alumnoId ID del estudiante
     * @return DTOEstadoInscripcion con el estado de inscripción
     */
    @GetMapping("/{claseId}/enrollment-status/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #alumnoId == authentication.principal.id)")
    public ResponseEntity<DTOEstadoInscripcion> verificarEstadoInscripcion(
            @PathVariable Long claseId,
            @PathVariable Long alumnoId) {
        
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(alumnoId, claseId);
        return ResponseEntity.ok(estado);
    }

    /**
     * Verifica si el estudiante autenticado está inscrito en una clase específica
     * GET /api/clases/{claseId}/enrollment-status
     * @param claseId ID de la clase
     * @return DTOEstadoInscripcion con el estado de inscripción
     */
    @GetMapping("/{claseId}/enrollment-status")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DTOEstadoInscripcion> verificarMiEstadoInscripcion(
            @PathVariable Long claseId) {
        
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOEstadoInscripcion estado = servicioClase.verificarEstadoInscripcion(alumnoId, claseId);
        return ResponseEntity.ok(estado);
    }

    /**
     * Obtiene información detallada de una clase para un estudiante específico
     * GET /api/clases/{claseId}/details-for-student/{alumnoId}
     * @param claseId ID de la clase
     * @param alumnoId ID del estudiante
     * @return DTOClaseConDetalles con información completa
     */
    @GetMapping("/{claseId}/details-for-student/{alumnoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #alumnoId == authentication.principal.id)")
    public ResponseEntity<DTOClaseConDetalles> obtenerClaseConDetallesParaEstudiante(
            @PathVariable Long claseId,
            @PathVariable Long alumnoId) {
        
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, alumnoId);
        return ResponseEntity.ok(detalles);
    }

    /**
     * Obtiene información detallada de una clase para el estudiante autenticado
     * GET /api/clases/{claseId}/details-for-me
     * @param claseId ID de la clase
     * @return DTOClaseConDetalles con información completa
     */
    @GetMapping("/{claseId}/details-for-me")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DTOClaseConDetalles> obtenerClaseConDetallesParaMi(
            @PathVariable Long claseId) {
        
        Long alumnoId = securityUtils.getCurrentUserId();
        DTOClaseConDetalles detalles = servicioClase.obtenerClaseConDetallesParaEstudiante(claseId, alumnoId);
        return ResponseEntity.ok(detalles);
    }
}
