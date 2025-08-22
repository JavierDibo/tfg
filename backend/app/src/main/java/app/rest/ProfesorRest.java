package app.rest;

import app.dtos.DTOActualizacionProfesor;
import app.dtos.DTOProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTORespuestaEnrollment;
import app.excepciones.ResourceNotFoundException;
import app.servicios.ServicioProfesor;
import app.servicios.ServicioClase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de profesores
 * Implementa los endpoints según el UML: RESTProfesor
 */
@RestController
@RequestMapping("/api/profesores")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
public class ProfesorRest {

    private final ServicioProfesor servicioProfesor;
    private final ServicioClase servicioClase;

    /**
     * Obtiene todos los profesores paginados o busca con parámetros
     * GET /api/profesores/paged
     * @param nombre Nombre a buscar (opcional)
     * @param apellidos Apellidos a buscar (opcional)
     * @param email Email a buscar (opcional)
     * @param usuario Usuario a buscar (opcional)
     * @param dni DNI a buscar (opcional)
     * @param habilitado Estado de habilitación (opcional)
     * @param claseId ID de clase asignada (opcional)
     * @param sinClases Si es true, busca profesores sin clases asignadas (opcional)
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return Respuesta paginada de profesores
     */
    @GetMapping("/paged")
    public ResponseEntity<DTORespuestaPaginada<DTOProfesor>> obtenerProfesoresPaginados(
            @RequestParam(required = false) @Size(max = 100) String nombre,
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @RequestParam(required = false) @Size(max = 100) String email,
            @RequestParam(required = false) @Size(max = 50) String usuario,
            @RequestParam(required = false) @Size(max = 20) String dni,
            @RequestParam(required = false) Boolean habilitado,
            @RequestParam(required = false) String claseId,
            @RequestParam(required = false) Boolean sinClases,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
                nombre, apellidos, email, usuario, dni, habilitado, claseId, sinClases);
        
        System.out.println("Obteniendo profesores paginados con parámetros: " + parametros);
        DTORespuestaPaginada<DTOProfesor> respuesta = servicioProfesor.buscarProfesoresPorParametrosPaginados(
                parametros, page, size, sortBy, sortDirection);
                
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Obtiene todos los profesores o busca con parámetros (sin paginación - DEPRECATED)
     * GET /api/profesores
     * @param nombre Nombre a buscar (opcional)
     * @param apellidos Apellidos a buscar (opcional)
     * @param email Email a buscar (opcional)
     * @param habilitado Estado de habilitación (opcional)
     * @return Lista de profesores
     */
    @GetMapping
    @Deprecated(since = "1.1", forRemoval = true)
    public ResponseEntity<List<DTOProfesor>> obtenerProfesores(
            @RequestParam(required = false) @Size(max = 100) String nombre,
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @RequestParam(required = false) @Size(max = 100) String email,
            @RequestParam(required = false) Boolean habilitado) {
        
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
                nombre, apellidos, email, null, null, habilitado, null, null);
        
        if (parametros.estaVacio()) {
            // System.out.println("Obteniendo todos los profesores");
            List<DTOProfesor> profesores = servicioProfesor.obtenerProfesores();
            return new ResponseEntity<>(profesores, HttpStatus.OK);
        } else {
            // System.out.println("Buscando profesores con parámetros: " + parametros);
            List<DTOProfesor> profesores = servicioProfesor.buscarProfesoresPorParametros(parametros);
            return new ResponseEntity<>(profesores, HttpStatus.OK);
        }
    }

    /**
     * Obtiene un profesor por su ID
     * GET /api/profesores/{id}
     * @param id ID del profesor
     * @return DTOProfesor
     */
    @GetMapping("/{id}")
    public ResponseEntity<DTOProfesor> obtenerProfesorPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        // System.out.println("Obteniendo profesor con ID: " + id);
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorId(id);
        return new ResponseEntity<>(profesor, HttpStatus.OK);
    }

    /**
     * Obtiene un profesor por su email
     * GET /api/profesores/email/{email}
     * @param email Email del profesor
     * @return DTOProfesor
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<DTOProfesor> obtenerProfesorPorEmail(
            @PathVariable @Size(max = 100) String email) {
        System.out.println("Obteniendo profesor con email: " + email);
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorEmail(email);
        return new ResponseEntity<>(profesor, HttpStatus.OK);
    }

    /**
     * Obtiene un profesor por su usuario
     * GET /api/profesores/usuario/{usuario}
     * @param usuario Nombre de usuario del profesor
     * @return DTOProfesor
     */
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<DTOProfesor> obtenerProfesorPorUsuario(
            @PathVariable @Size(max = 50) String usuario) {
        System.out.println("Obteniendo profesor con usuario: " + usuario);
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorUsuario(usuario);
        return new ResponseEntity<>(profesor, HttpStatus.OK);
    }

    /**
     * Obtiene un profesor por su DNI
     * GET /api/profesores/dni/{dni}
     * @param dni DNI del profesor
     * @return DTOProfesor
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<DTOProfesor> obtenerProfesorPorDni(
            @PathVariable @Size(max = 20) String dni) {
        System.out.println("Obteniendo profesor con DNI: " + dni);
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorDni(dni);
        return new ResponseEntity<>(profesor, HttpStatus.OK);
    }

    /**
     * Busca profesores por nombre
     * GET /api/profesores/buscar/nombre?nombre={nombre}
     * @param nombre Nombre a buscar
     * @return Lista de profesores
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<DTOProfesor>> buscarProfesoresPorNombre(
            @RequestParam @Size(max = 100) String nombre) {
        System.out.println("Buscando profesores por nombre: " + nombre);
        List<DTOProfesor> profesores = servicioProfesor.buscarProfesoresPorNombre(nombre);
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    /**
     * Busca profesores por apellidos
     * GET /api/profesores/buscar/apellidos?apellidos={apellidos}
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores
     */
    @GetMapping("/buscar/apellidos")
    public ResponseEntity<List<DTOProfesor>> buscarProfesoresPorApellidos(
            @RequestParam @Size(max = 100) String apellidos) {
        System.out.println("Buscando profesores por apellidos: " + apellidos);
        List<DTOProfesor> profesores = servicioProfesor.buscarProfesoresPorApellidos(apellidos);
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    /**
     * Obtiene profesores habilitados paginados
     * GET /api/profesores/habilitados/paged
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return Respuesta paginada de profesores habilitados
     */
    @GetMapping("/habilitados/paged")
    public ResponseEntity<DTORespuestaPaginada<DTOProfesor>> obtenerProfesoresHabilitadosPaginados(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        System.out.println("Obteniendo profesores habilitados paginados");
        DTORespuestaPaginada<DTOProfesor> respuesta = servicioProfesor.obtenerProfesoresHabilitadosPaginados(
                page, size, sortBy, sortDirection);
                
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Obtiene profesores habilitados (sin paginación - DEPRECATED)
     * GET /api/profesores/habilitados
     * @return Lista de profesores habilitados
     */
    @GetMapping("/habilitados")
    @Deprecated(since = "1.1", forRemoval = true)
    public ResponseEntity<List<DTOProfesor>> obtenerProfesoresHabilitados() {
        System.out.println("Obteniendo profesores habilitados");
        List<DTOProfesor> profesores = servicioProfesor.obtenerProfesoresHabilitados();
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    /**
     * Obtiene profesores por clase
     * GET /api/profesores/clase/{claseId}
     * @param claseId ID de la clase
     * @return Lista de profesores de la clase
     */
    @GetMapping("/clase/{claseId}")
    public ResponseEntity<List<DTOProfesor>> obtenerProfesoresPorClase(
            @PathVariable String claseId) {
        System.out.println("Obteniendo profesores de la clase: " + claseId);
        List<DTOProfesor> profesores = servicioProfesor.obtenerProfesoresPorClase(claseId);
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    /**
     * Obtiene profesores sin clases asignadas
     * GET /api/profesores/sin-clases
     * @return Lista de profesores sin clases
     */
    @GetMapping("/sin-clases")
    public ResponseEntity<List<DTOProfesor>> obtenerProfesoresSinClases() {
        System.out.println("Obteniendo profesores sin clases");
        List<DTOProfesor> profesores = servicioProfesor.obtenerProfesoresSinClases();
        return new ResponseEntity<>(profesores, HttpStatus.OK);
    }

    /**
     * Crea un nuevo profesor según el UML
     * POST /api/profesores
     * @param peticion Datos del profesor a crear
     * @return DTOProfesor del profesor creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOProfesor> crearProfesor(@Valid @RequestBody DTOPeticionRegistroProfesor peticion) {
        System.out.println("Creando nuevo profesor: " + peticion.usuario());
        DTOProfesor profesorCreado = servicioProfesor.crearProfesor(peticion);
        return new ResponseEntity<>(profesorCreado, HttpStatus.CREATED);
    }

    /**
     * Borra un profesor por su ID según el UML
     * DELETE /api/profesores/{id}
     * @param id ID del profesor a borrar
     * @return Confirmación de borrado
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> borrarProfesorPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        System.out.println("Borrando profesor con ID: " + id);
        boolean borrado = servicioProfesor.borrarProfesorPorId(id);
        
        Map<String, Object> response = Map.of(
                "success", borrado,
                "message", "Profesor borrado exitosamente",
                "profesorId", id
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Asigna una clase a un profesor
     * PUT /api/profesores/{id}/clases/{claseId}
     * @param id ID del profesor
     * @param claseId ID de la clase
     * @return DTOProfesor actualizado
     */
    @PutMapping("/{id}/clases/{claseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOProfesor> asignarClase(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @PathVariable String claseId) {
        System.out.println("Asignando clase " + claseId + " al profesor " + id);
        DTOProfesor profesorActualizado = servicioProfesor.asignarClase(id, claseId);
        return new ResponseEntity<>(profesorActualizado, HttpStatus.OK);
    }

    /**
     * Remueve una clase de un profesor
     * DELETE /api/profesores/{id}/clases/{claseId}
     * @param id ID del profesor
     * @param claseId ID de la clase
     * @return DTOProfesor actualizado
     */
    @DeleteMapping("/{id}/clases/{claseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<DTOProfesor> removerClase(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @PathVariable String claseId) {
        System.out.println("Removiendo clase " + claseId + " del profesor " + id);
        DTOProfesor profesorActualizado = servicioProfesor.removerClase(id, claseId);
        return new ResponseEntity<>(profesorActualizado, HttpStatus.OK);
    }

    /**
     * Obtiene el número de clases de un profesor
     * GET /api/profesores/{id}/clases/count
     * @param id ID del profesor
     * @return Número de clases
     */
    @GetMapping("/{id}/clases/count")
    public ResponseEntity<Map<String, Object>> contarClasesProfesor(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        System.out.println("Contando clases del profesor: " + id);
        Integer numeroClases = servicioProfesor.contarClasesProfesor(id);
        
        Map<String, Object> response = Map.of(
                "profesorId", id,
                "numeroClases", numeroClases
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Habilita o deshabilita un profesor
     * PATCH /api/profesores/{id}/estado
     * @param id ID del profesor
     * @param estado Map con el estado (habilitado: true/false)
     * @return DTOProfesor actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOProfesor> cambiarEstadoProfesor(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @RequestBody Map<String, Boolean> estado) {
        
        Boolean habilitado = estado.get("habilitado");
        if (habilitado == null) {
            throw new IllegalArgumentException("Debe proporcionar el campo 'habilitado'");
        }
        
        // System.out.println("Cambiando estado del profesor " + id + " a: " + habilitado);
        DTOProfesor profesorActualizado = servicioProfesor.cambiarEstadoProfesor(id, habilitado);
        return new ResponseEntity<>(profesorActualizado, HttpStatus.OK);
    }

    /**
     * Actualiza parcialmente los datos de un profesor
     * PATCH /api/profesores/{id}
     * @param id ID del profesor a actualizar
     * @param dtoParcial Datos a actualizar
     * @return DTOProfesor con los datos actualizados
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #id == authentication.principal.id)")
    public ResponseEntity<DTOProfesor> actualizarProfesor(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @Valid @RequestBody DTOActualizacionProfesor dtoParcial) {
        
        // System.out.println("Actualizando profesor con ID: " + id);
        DTOProfesor profesorActualizado = servicioProfesor.actualizarProfesor(id, dtoParcial);
        return new ResponseEntity<>(profesorActualizado, HttpStatus.OK);
    }

    /**
     * Endpoint para estadísticas de profesores
     * GET /api/profesores/estadisticas/total
     * @return Mapa con estadísticas
     */
    @GetMapping("/estadisticas/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerTotalProfesores() {
        long total = servicioProfesor.contarTotalProfesores();
        return new ResponseEntity<>(Map.of("total", total), HttpStatus.OK);
    }

    /**
     * Endpoint para estadísticas de habilitación de profesores
     * GET /api/profesores/estadisticas/habilitacion
     * @return Mapa con estadísticas
     */
    @GetMapping("/estadisticas/habilitacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasHabilitacion() {
        Map<String, Long> estadisticas = Map.of(
            "habilitados", servicioProfesor.contarProfesoresHabilitados(),
            "deshabilitados", servicioProfesor.contarProfesoresDeshabilitados()
        );
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    /**
     * Endpoint de prueba para verificar que el controlador funciona
     * GET /api/profesores/test
     * @return Mensaje de confirmación
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controlador ProfesorRest funcionando correctamente");
    }

    // ===== ENDPOINTS PARA GESTIÓN DE ALUMNOS EN CLASES =====

    /**
     * Endpoint para que un profesor inscriba un alumno en una de sus clases
     * POST /api/profesores/{profesorId}/clases/{claseId}/alumnos
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @param peticion DTO con los datos de la inscripción
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @PostMapping("/{profesorId}/clases/{claseId}/alumnos")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #profesorId == authentication.principal.id)")
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnMiClase(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long profesorId,
            @PathVariable String claseId,
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        
        // Verificar que el profesor está intentando inscribir en su propia clase
        if (!peticion.claseId().toString().equals(claseId)) {
            return ResponseEntity.badRequest().body(
                DTORespuestaEnrollment.failure(peticion.alumnoId(), peticion.claseId(), 
                    "El ID de la clase en la URL no coincide con el de la petición", "ENROLLMENT")
            );
        }
        
        DTORespuestaEnrollment respuesta = servicioClase.inscribirAlumnoEnClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Endpoint para que un profesor dé de baja un alumno de una de sus clases
     * DELETE /api/profesores/{profesorId}/clases/{claseId}/alumnos
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @param peticion DTO con los datos de la baja
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @DeleteMapping("/{profesorId}/clases/{claseId}/alumnos")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #profesorId == authentication.principal.id)")
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeMiClase(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long profesorId,
            @PathVariable String claseId,
            @Valid @RequestBody DTOPeticionEnrollment peticion) {
        
        // Verificar que el profesor está intentando dar de baja de su propia clase
        if (!peticion.claseId().toString().equals(claseId)) {
            return ResponseEntity.badRequest().body(
                DTORespuestaEnrollment.failure(peticion.alumnoId(), peticion.claseId(), 
                    "El ID de la clase en la URL no coincide con el de la petición", "UNENROLLMENT")
            );
        }
        
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);
        
        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
}
