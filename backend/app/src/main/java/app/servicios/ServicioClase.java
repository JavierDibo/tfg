// LLM_EDIT_TIMESTAMP: 25 ago. 14:00
package app.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOAlumnoPublico;
import app.dtos.DTOClase;
import app.dtos.DTOClaseConDetalles;
import app.dtos.DTOClaseConDetallesPublico;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOCurso;
import app.dtos.DTOEstadoInscripcion;
import app.dtos.DTOParametrosBusquedaClase;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTOProfesor;
import app.dtos.DTOProfesorPublico;
import app.dtos.DTORespuestaEnrollment;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOTaller;
import app.entidades.Alumno;
import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Material;
import app.entidades.Profesor;
import app.entidades.Taller;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioProfesor;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de clases
 * Implementa la lógica de negocio según el UML
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioClase {

    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioClase repositorioClase;
    private final RepositorioProfesor repositorioProfesor;
    private final SecurityUtils securityUtils;

    /**
     * Obtiene todas las clases
     * @return Lista de DTOClase
     */
    @Transactional(readOnly = true)
    public List<DTOClase> obtenerClases() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access classes
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a clases");
        }
        
        return repositorioClase.findAllOrderedById().stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene clases según el rol del usuario autenticado:
     * - ADMIN: todas las clases
     * - PROFESOR: solo las clases que imparte o ha creado
     * - ALUMNO: todas las clases (para ver el catálogo)
     * @return Lista de DTOClase filtrada según el rol
     */
    @Transactional(readOnly = true)
    public List<DTOClase> obtenerClasesSegunRol() {
        if (securityUtils.isAdmin()) {
            // Los administradores pueden ver todas las clases
            return obtenerClases();
        } else if (securityUtils.isProfessor()) {
            // Los profesores solo pueden ver las clases que imparten
            String profesorId = securityUtils.getCurrentUserId().toString();
            return obtenerClasesPorProfesor(profesorId);
        } else {
            // Los alumnos pueden ver todas las clases (catálogo)
            return obtenerClases();
        }
    }

    /**
     * Busca clases por título según el rol del usuario autenticado
     * @param titulo Título de la clase a buscar
     * @return Lista de DTOClase filtrada según el rol
     */
    public List<DTOClase> buscarClasesPorTituloSegunRol(String titulo) {
        if (securityUtils.isAdmin()) {
            // Los administradores pueden buscar en todas las clases
            return buscarClasesPorTitulo(titulo);
        } else if (securityUtils.isProfessor()) {
            // Los profesores solo pueden buscar en las clases que imparten
            String profesorId = securityUtils.getCurrentUserId().toString();
            List<DTOClase> clasesDelProfesor = obtenerClasesPorProfesor(profesorId);
            return clasesDelProfesor.stream()
                    .filter(clase -> clase.titulo().toLowerCase().contains(titulo.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // Los alumnos pueden buscar en todas las clases
            return buscarClasesPorTitulo(titulo);
        }
    }

    /**
     * Busca clases con paginación según el rol del usuario autenticado
     * @param parametros Parámetros de búsqueda
     * @return DTORespuestaPaginada con las clases encontradas filtradas según el rol
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOClase> buscarClasesSegunRol(DTOParametrosBusquedaClase parametros) {
        if (securityUtils.isAdmin()) {
            // Los administradores pueden buscar en todas las clases
            return buscarClases(parametros);
        } else if (securityUtils.isProfessor()) {
            // Los profesores solo pueden buscar en las clases que imparten
            String profesorId = securityUtils.getCurrentUserId().toString();
            List<DTOClase> clasesDelProfesor = obtenerClasesPorProfesor(profesorId);
            
            // Filtrar por los parámetros de búsqueda
            List<DTOClase> clasesFiltradas = clasesDelProfesor.stream()
                    .filter(clase -> {
                        boolean cumpleCriterios = true;
                        
                        if (parametros.titulo() != null && !parametros.titulo().isEmpty()) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.titulo().toLowerCase().contains(parametros.titulo().toLowerCase());
                        }
                        
                        if (parametros.descripcion() != null && !parametros.descripcion().isEmpty()) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.descripcion().toLowerCase().contains(parametros.descripcion().toLowerCase());
                        }
                        
                        if (parametros.presencialidad() != null) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.presencialidad() == parametros.presencialidad();
                        }
                        
                        if (parametros.nivel() != null) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.nivel() == parametros.nivel();
                        }
                        
                        if (parametros.precioMinimo() != null && parametros.precioMaximo() != null) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.precio().compareTo(parametros.precioMinimo()) >= 0 &&
                                clase.precio().compareTo(parametros.precioMaximo()) <= 0;
                        } else if (parametros.precioMaximo() != null) {
                            cumpleCriterios = cumpleCriterios && 
                                clase.precio().compareTo(parametros.precioMaximo()) <= 0;
                        }
                        
                        return cumpleCriterios;
                    })
                    .collect(Collectors.toList());
            
            // Aplicar paginación manual
            Pageable pageable = PageRequest.of(
                    parametros.page(),
                    parametros.size(),
                    Sort.by(Sort.Direction.fromString(parametros.sortDirection()), parametros.sortBy())
            );
            
            Page<DTOClase> page = convertirListaAPagina(clasesFiltradas, pageable);
            return DTORespuestaPaginada.fromPage(page, parametros.sortBy(), parametros.sortDirection());
        } else {
            // Los alumnos pueden buscar en todas las clases
            return buscarClases(parametros);
        }
    }

    /**
     * Obtiene una clase por su ID según el rol del usuario autenticado
     * @param id ID de la clase
     * @return DTOClase
     * @throws EntidadNoEncontradaException si no se encuentra la clase o no tiene acceso
     */
    @Transactional(readOnly = true)
    public DTOClase obtenerClasePorId(Long id) {
        Clase clase = repositorioClase.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", id);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase");
        }
        
        return new DTOClase(clase);
    }

    /**
     * Verifica si el usuario actual puede acceder a una clase específica
     * @param clase La clase a verificar
     * @return true si puede acceder, false en caso contrario
     */
    private boolean puedeAccederAClase(Clase clase) {
        if (securityUtils.isAdmin()) {
            return true; // Los administradores pueden acceder a todas las clases
        } else if (securityUtils.isProfessor()) {
            // Los profesores solo pueden acceder a las clases que imparten
            String profesorId = securityUtils.getCurrentUserId().toString();
            return clase.getTeacherIds().contains(profesorId);
        } else {
            // Los alumnos pueden acceder a todas las clases (para ver el catálogo)
            return true;
        }
    }

    /**
     * Obtiene una clase por su título según el rol del usuario autenticado
     * @param titulo Título de la clase
     * @return DTOClase
     * @throws EntidadNoEncontradaException si no se encuentra la clase o no tiene acceso
     */
    @Transactional(readOnly = true)
    public DTOClase obtenerClasePorTitulo(String titulo) {
        Clase clase = repositorioClase.findByTitle(titulo).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "título", titulo);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase");
        }
        
        return new DTOClase(clase);
    }

    /**
     * Crea una nueva clase de tipo Curso
     * @param peticion Datos para crear el curso
     * @param fechaInicio Fecha de inicio del curso
     * @param fechaFin Fecha de fin del curso
     * @return DTOCurso con los datos del curso creado
     */
    public DTOCurso crearCurso(DTOPeticionCrearClase peticion, LocalDate fechaInicio, LocalDate fechaFin) {
        // Security check: Only ADMIN and PROFESOR can create courses
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear cursos");
        }
        
        Curso curso = new Curso(
                peticion.titulo(),
                peticion.descripcion(),
                peticion.precio(),
                peticion.presencialidad(),
                peticion.imagenPortada(),
                peticion.nivel(),
                fechaInicio,
                fechaFin
        );
        
        // Agregar profesores si se proporcionaron
        if (peticion.profesoresId() != null) {
            for (String profesorId : peticion.profesoresId()) {
                curso.agregarProfesor(profesorId);
            }
        }
        
        // Agregar material si se proporcionó
        if (peticion.material() != null) {
            for (Material material : peticion.material()) {
                curso.agregarMaterial(material);
            }
        }
        
        Curso cursoGuardado = (Curso) repositorioClase.save(curso);
        
        // Actualizar la lista de clases de los profesores para mantener consistencia bidireccional
        if (peticion.profesoresId() != null) {
            for (String profesorId : peticion.profesoresId()) {
                try {
                    Long profesorIdLong = Long.parseLong(profesorId);
                    Profesor profesor = repositorioProfesor.findById(profesorIdLong).orElse(null);
                    if (profesor != null) {
                        profesor.agregarClase(cursoGuardado.getId().toString());
                        repositorioProfesor.save(profesor);
                    }
                } catch (NumberFormatException e) {
                    // Si no se puede parsear el ID, continuar con el siguiente profesor
                }
            }
        }
        
        return new DTOCurso(cursoGuardado);
    }

    /**
     * Crea un nuevo taller
     * @param peticion Datos para crear el taller
     * @param duracionHoras Duración del taller en horas
     * @param fechaRealizacion Fecha de realización del taller
     * @param horaComienzo Hora de comienzo del taller
     * @return DTOTaller con los datos del taller creado
     */
    public DTOTaller crearTaller(DTOPeticionCrearClase peticion, Integer duracionHoras, 
                                LocalDate fechaRealizacion, LocalTime horaComienzo) {
        // Security check: Only ADMIN and PROFESOR can create workshops
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear talleres");
        }
        
        Taller taller = new Taller(
                peticion.titulo(),
                peticion.descripcion(),
                peticion.precio(),
                peticion.presencialidad(),
                peticion.imagenPortada(),
                peticion.nivel(),
                duracionHoras,
                fechaRealizacion,
                horaComienzo
        );
        
        // Agregar profesores si se proporcionaron
        if (peticion.profesoresId() != null) {
            for (String profesorId : peticion.profesoresId()) {
                taller.agregarProfesor(profesorId);
            }
        }
        
        // Agregar material si se proporcionó
        if (peticion.material() != null) {
            for (Material material : peticion.material()) {
                taller.agregarMaterial(material);
            }
        }
        
        Taller tallerGuardado = (Taller) repositorioClase.save(taller);
        
        // Actualizar la lista de clases de los profesores para mantener consistencia bidireccional
        if (peticion.profesoresId() != null) {
            for (String profesorId : peticion.profesoresId()) {
                try {
                    Long profesorIdLong = Long.parseLong(profesorId);
                    Profesor profesor = repositorioProfesor.findById(profesorIdLong).orElse(null);
                    if (profesor != null) {
                        profesor.agregarClase(tallerGuardado.getId().toString());
                        repositorioProfesor.save(profesor);
                    }
                } catch (NumberFormatException e) {
                    // Si no se puede parsear el ID, continuar con el siguiente profesor
                }
            }
        }
        
        return new DTOTaller(tallerGuardado);
    }

    /**
     * Crea varias clases a partir de una lista
     * @param clases Lista de clases a crear
     * @return Lista de DTOClase con los datos de las clases creadas
     */
    public List<DTOClase> crearClaseDesdeLista(List<Clase> clases) {
        List<Clase> clasesGuardadas = repositorioClase.saveAll(clases);
        return clasesGuardadas.stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Borra una clase por su ID
     * @param id ID de la clase
     * @return true si se borró correctamente, false en caso contrario
     */
    public boolean borrarClasePorId(Long id) {
        if (!repositorioClase.existsById(id)) {
            return false;
        }
        
        repositorioClase.deleteById(id);
        return true;
    }

    /**
     * Borra una clase por su título
     * @param titulo Título de la clase
     * @return true si se borró correctamente, false en caso contrario
     */
    public boolean borrarClasePorTitulo(String titulo) {
        Optional<Clase> claseOpt = repositorioClase.findByTitle(titulo);
        if (claseOpt.isEmpty()) {
            return false;
        }
        
        repositorioClase.delete(claseOpt.get());
        return true;
    }
    
    /**
     * Convierte una lista de entidades a una página
     * @param lista Lista a convertir
     * @param pageable Información de paginación
     * @return Página con los elementos de la lista
     */
    private <T> Page<T> convertirListaAPagina(List<T> lista, Pageable pageable) {
        if (lista == null || lista.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        int inicio = (int) pageable.getOffset();
        int fin = Math.min((inicio + pageable.getPageSize()), lista.size());
        
        if (inicio > fin) {
            return new PageImpl<>(Collections.emptyList(), pageable, lista.size());
        }
        
        return new PageImpl<>(lista.subList(inicio, fin), pageable, lista.size());
    }

    /**
     * Busca clases con paginación
     * @param parametros Parámetros de búsqueda
     * @return DTORespuestaPaginada con las clases encontradas
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOClase> buscarClases(DTOParametrosBusquedaClase parametros) {
        // Configurar paginación
        Pageable pageable = PageRequest.of(
                parametros.page(),
                parametros.size(),
                Sort.by(Sort.Direction.fromString(parametros.sortDirection()), parametros.sortBy())
        );
        
        // Enhanced search logic with "q" parameter support
        Page<Clase> resultado;
        
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters)
                resultado = repositorioClase.findByGeneralAndSpecificFilters(
                    parametros.q(),
                    parametros.titulo(),
                    parametros.descripcion(),
                    parametros.presencialidad(),
                    parametros.nivel(),
                    parametros.precioMinimo(),
                    parametros.precioMaximo(),
                    pageable
                );
            } else {
                // Use only general search
                resultado = repositorioClase.findByGeneralSearch(parametros.q(), pageable);
            }
        } else {
            // Use existing specific search logic with flexible approach
            if (parametros.hasSpecificFilters()) {
                // Use flexible query that handles all combinations
                resultado = repositorioClase.findByGeneralAndSpecificFilters(
                    null, // No general search term
                    parametros.titulo(),
                    parametros.descripcion(),
                    parametros.presencialidad(),
                    parametros.nivel(),
                    parametros.precioMinimo(),
                    parametros.precioMaximo(),
                    pageable
                );
            } else {
                // Si no hay criterios específicos, obtener todas las clases
                List<Clase> todasLasClases = repositorioClase.findAllOrderedById();
                resultado = convertirListaAPagina(todasLasClases, pageable);
            }
        }
        
        // Ensure resultado is not null before converting to DTOs
        if (resultado == null) {
            resultado = new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        // Convertir a DTOs
        Page<DTOClase> pageDTOs = resultado.map(DTOClase::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, parametros.sortBy(), parametros.sortDirection());
    }

    /**
     * Busca clases por título
     * @param titulo Título de la clase a buscar
     * @return Lista de DTOClase que coinciden con el título
     */
    public List<DTOClase> buscarClasesPorTitulo(String titulo) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can search classes
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para buscar clases");
        }
        
        return repositorioClase.findByTitleContainingIgnoreCase(titulo)
                .stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Agrega un alumno a una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOClase agregarAlumno(Long claseId, String alumnoId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        // Verificar si el alumno ya está en la clase para evitar duplicados
        if (clase.getStudentIds().contains(alumnoId)) {
            // El alumno ya está en la clase, devolver la clase sin cambios
            return new DTOClase(clase);
        }
        
        clase.agregarAlumno(alumnoId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Método específico para que los profesores inscriban alumnos en sus clases
     * @param peticion DTO con los datos de la inscripción
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTORespuestaEnrollment inscribirAlumnoEnClase(DTOPeticionEnrollment peticion) {
        try {
            Clase clase = repositorioClase.findById(peticion.classId()).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", peticion.classId());
            
            // Verify that the current professor can modify this class
            if (!puedeAccederAClase(clase)) {
                ExceptionUtils.throwAccessDenied("You don't have permission to modify this class");
            }
            
            // Check if the student is already in the class
            if (clase.getStudentIds().contains(peticion.studentId().toString())) {
                return DTORespuestaEnrollment.failure(
                    peticion.studentId(), 
                    peticion.classId(), 
                    "The student is already enrolled in this class", 
                    "ENROLLMENT"
                );
            }
            
            // Add the student to the class
            clase.agregarAlumno(peticion.studentId().toString());
            Clase claseActualizada = repositorioClase.save(clase);
            
            return DTORespuestaEnrollment.success(
                peticion.studentId(),
                peticion.classId(),
                "Student enrolled", // TODO: Get real student name
                claseActualizada.getTitle(),
                "ENROLLMENT"
            );
            
        } catch (Exception e) {
            return DTORespuestaEnrollment.failure(
                peticion.studentId(), 
                peticion.classId(), 
                "Error enrolling student: " + e.getMessage(), 
                "ENROLLMENT"
            );
        }
    }

    /**
     * Remueve un alumno de una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    public DTOClase removerAlumno(Long claseId, String alumnoId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.removerAlumno(alumnoId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Método específico para que los profesores den de baja alumnos de sus clases
     * @param peticion DTO con los datos de la baja
     * @return DTORespuestaEnrollment con el resultado de la operación
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTORespuestaEnrollment darDeBajaAlumnoDeClase(DTOPeticionEnrollment peticion) {
        try {
            Clase clase = repositorioClase.findById(peticion.classId()).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", peticion.classId());
            
            // Verify that the current professor can modify this class
            if (!puedeAccederAClase(clase)) {
                ExceptionUtils.throwAccessDenied("You don't have permission to modify this class");
            }
            
            // Check if the student is in the class
            if (!clase.getStudentIds().contains(peticion.studentId().toString())) {
                return DTORespuestaEnrollment.failure(
                    peticion.studentId(), 
                    peticion.classId(), 
                    "The student is not enrolled in this class", 
                    "UNENROLLMENT"
                );
            }
            
            // Remove the student from the class
            clase.removerAlumno(peticion.studentId().toString());
            Clase claseActualizada = repositorioClase.save(clase);
            
            return DTORespuestaEnrollment.success(
                peticion.studentId(),
                peticion.classId(),
                "Student unenrolled", // TODO: Get real student name
                claseActualizada.getTitle(),
                "UNENROLLMENT"
            );
            
        } catch (Exception e) {
            return DTORespuestaEnrollment.failure(
                peticion.studentId(), 
                peticion.classId(), 
                "Error unenrolling student: " + e.getMessage(), 
                "UNENROLLMENT"
            );
        }
    }

    /**
     * Agrega un profesor a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOClase agregarProfesor(Long claseId, String profesorId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        // Verificar si el profesor ya está en la clase para evitar duplicados
        if (clase.getTeacherIds().contains(profesorId)) {
            // El profesor ya está en la clase, devolver la clase sin cambios
            return new DTOClase(clase);
        }
        
        clase.agregarProfesor(profesorId);
        Clase claseActualizada = repositorioClase.save(clase);
        
        // Actualizar la lista de clases del profesor para mantener consistencia bidireccional
        try {
            Long profesorIdLong = Long.parseLong(profesorId);
            Profesor profesor = repositorioProfesor.findById(profesorIdLong).orElse(null);
            if (profesor != null) {
                profesor.agregarClase(claseId.toString());
                repositorioProfesor.save(profesor);
            }
        } catch (NumberFormatException e) {
            // Si no se puede parsear el ID, continuar sin actualizar el profesor
        }
        
        return new DTOClase(claseActualizada);
    }

    /**
     * Remueve un profesor de una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    public DTOClase removerProfesor(Long claseId, String profesorId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.removerProfesor(profesorId);
        Clase claseActualizada = repositorioClase.save(clase);
        
        // Actualizar la lista de clases del profesor para mantener consistencia bidireccional
        try {
            Long profesorIdLong = Long.parseLong(profesorId);
            Profesor profesor = repositorioProfesor.findById(profesorIdLong).orElse(null);
            if (profesor != null) {
                profesor.removerClase(claseId.toString());
                repositorioProfesor.save(profesor);
            }
        } catch (NumberFormatException e) {
            // Si no se puede parsear el ID, continuar sin actualizar el profesor
        }
        
        return new DTOClase(claseActualizada);
    }

    /**
     * Agrega un ejercicio a una clase
     * @param claseId ID de la clase
     * @param ejercicioId ID del ejercicio
     * @return DTOClase actualizada
     */
    public DTOClase agregarEjercicio(Long claseId, String ejercicioId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.agregarEjercicio(ejercicioId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Remueve un ejercicio de una clase
     * @param claseId ID de la clase
     * @param ejercicioId ID del ejercicio
     * @return DTOClase actualizada
     */
    public DTOClase removerEjercicio(Long claseId, String ejercicioId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.removerEjercicio(ejercicioId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Agrega material a una clase
     * @param claseId ID de la clase
     * @param material Material a agregar
     * @return DTOClase actualizada
     */
    public DTOClase agregarMaterial(Long claseId, Material material) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.agregarMaterial(material);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Remueve material de una clase
     * @param claseId ID de la clase
     * @param materialId ID del material
     * @return DTOClase actualizada
     */
    public DTOClase removerMaterial(Long claseId, String materialId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta clase");
        }
        
        clase.removerMaterial(materialId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Obtiene todas las clases donde un alumno está inscrito
     * @param alumnoId ID del alumno
     * @return Lista de DTOClase
     */
    public List<DTOClase> obtenerClasesPorAlumno(String alumnoId) {
        return repositorioClase.findByAlumnoId(alumnoId).stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las clases donde un profesor está asignado
     * @param profesorId ID del profesor
     * @return Lista de DTOClase
     */
    public List<DTOClase> obtenerClasesPorProfesor(String profesorId) {
        return repositorioClase.findByProfesorId(profesorId).stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta el número de alumnos en una clase
     * @param claseId ID de la clase
     * @return Número de alumnos
     */
    public Integer contarAlumnosEnClase(Long claseId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase");
        }
        
        return repositorioClase.countAlumnosByClaseId(claseId);
    }

    /**
     * Cuenta el número de profesores en una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    public Integer contarProfesoresEnClase(Long claseId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if (!puedeAccederAClase(clase)) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase");
        }
        
        return repositorioClase.countProfesoresByClaseId(claseId);
    }

    /**
     * Permite a un alumno inscribirse en una clase.
     * @param claseId ID de la clase a inscribirse.
     * @return DTORespuestaEnrollment con el resultado de la operación.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTORespuestaEnrollment inscribirseEnClase(Long claseId) {
        String alumnoId = securityUtils.getCurrentUserId().toString(); // Obtiene el ID del alumno autenticado
        Long alumnoIdLong = Long.parseLong(alumnoId);

        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

        // Verificar si el alumno ya está inscrito
        if (clase.getStudentIds().contains(alumnoId)) {
            return DTORespuestaEnrollment.failure(alumnoIdLong, claseId, 
                "El alumno ya está inscrito en esta clase", "ENROLLMENT");
        }

        clase.agregarAlumno(alumnoId);
        
        // También agregar la clase al alumno (actualizar la relación bidireccional)
        try {
            Alumno alumno = repositorioAlumno.findById(alumnoIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
            
            alumno.agregarClase(claseId.toString());
            repositorioAlumno.save(alumno);
        } catch (Exception e) {
            // Log the error but continue with the class update
        }
        
        Clase claseActualizada = repositorioClase.save(clase);
        
        // Obtener información del alumno para la respuesta
        Alumno alumno = repositorioAlumno.findById(alumnoIdLong).orElse(null);
        String nombreAlumno = alumno != null ? alumno.getFirstName() : "Alumno";
        
        return DTORespuestaEnrollment.success(alumnoIdLong, claseId, 
            nombreAlumno, claseActualizada.getTitle(), "ENROLLMENT");
    }

    /**
     * Permite a un alumno darse de baja de una clase.
     * @param claseId ID de la clase a darse de baja.
     * @return DTORespuestaEnrollment con el resultado de la operación.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DTORespuestaEnrollment darseDeBajaDeClase(Long claseId) {
        String alumnoId = securityUtils.getCurrentUserId().toString(); // Obtiene el ID del alumno autenticado
        Long alumnoIdLong = Long.parseLong(alumnoId);

        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

        // Verificar si el alumno está inscrito en la clase
        if (!clase.getStudentIds().contains(alumnoId)) {
            return DTORespuestaEnrollment.failure(alumnoIdLong, claseId, 
                "El alumno no está inscrito en esta clase", "UNENROLLMENT");
        }

        // Remover el alumno de la clase
        clase.removerAlumno(alumnoId);
        
        // También remover la clase del alumno (actualizar la relación bidireccional)
        try {
            Alumno alumno = repositorioAlumno.findById(alumnoIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
            
            alumno.removerClase(claseId.toString());
            repositorioAlumno.save(alumno);
        } catch (Exception e) {
            // Log the error but continue with the class update
        }
        
        Clase claseActualizada = repositorioClase.save(clase);
        
        // Obtener información del alumno para la respuesta
        Alumno alumno = repositorioAlumno.findById(alumnoIdLong).orElse(null);
        String nombreAlumno = alumno != null ? alumno.getFirstName() : "Alumno";
        
        return DTORespuestaEnrollment.success(alumnoIdLong, claseId, 
            nombreAlumno, claseActualizada.getTitle(), "UNENROLLMENT");
    }

    // ===== NUEVOS MÉTODOS PARA ESTUDIANTES =====

    /**
     * Obtiene las clases en las que está inscrito un estudiante con información detallada del profesor
     * @param alumnoId ID del estudiante
     * @return Lista de DTOClaseInscrita con información del profesor
     */
    public List<DTOClaseInscrita> obtenerClasesInscritasConDetalles(Long alumnoId) {
        List<Clase> clases = repositorioClase.findByAlumnoId(alumnoId.toString());
        
        return clases.stream()
                .map(clase -> {
                    // Obtener el primer profesor de la clase (profesor principal)
                    DTOProfesor profesor = null;
                    if (!clase.getTeacherIds().isEmpty()) {
                        try {
                            Long profesorId = Long.parseLong(clase.getTeacherIds().get(0));
                            Profesor profesorEntity = repositorioProfesor.findById(profesorId)
                                    .orElse(null);
                            if (profesorEntity != null) {
                                profesor = new DTOProfesor(profesorEntity);
                            }
                        } catch (NumberFormatException e) {
                            // Si no se puede parsear el ID, continuar sin profesor
                        }
                    }
                    
                    // Por ahora, usamos la fecha actual como fecha de inscripción
                    // En una implementación real, esto debería venir de una tabla de enrollments
                    LocalDateTime fechaInscripcion = LocalDateTime.now();
                    
                    return new DTOClaseInscrita(clase, profesor, fechaInscripcion);
                })
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un estudiante está inscrito en una clase específica
     * @param alumnoId ID del estudiante
     * @param claseId ID de la clase
     * @return DTOEstadoInscripcion con el estado de inscripción
     */
    public DTOEstadoInscripcion verificarEstadoInscripcion(Long alumnoId, Long claseId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        boolean isEnrolled = clase.getStudentIds().contains(alumnoId.toString());
        
        if (isEnrolled) {
            // Por ahora, usamos la fecha actual como fecha de inscripción
            // En una implementación real, esto debería venir de una tabla de enrollments
            LocalDateTime fechaInscripcion = LocalDateTime.now();
            return DTOEstadoInscripcion.enrolled(alumnoId, claseId, fechaInscripcion);
        } else {
            return DTOEstadoInscripcion.notEnrolled(alumnoId, claseId);
        }
    }

    /**
     * Obtiene información detallada de una clase para un estudiante específico
     * @param claseId ID de la clase
     * @param alumnoId ID del estudiante
     * @return DTOClaseConDetalles con información completa
     */
    public DTOClaseConDetalles obtenerClaseConDetallesParaEstudiante(Long claseId, Long alumnoId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Obtener el primer profesor de la clase (profesor principal)
        DTOProfesor profesor = null;
        if (!clase.getTeacherIds().isEmpty()) {
            try {
                Long profesorId = Long.parseLong(clase.getTeacherIds().get(0));
                Profesor profesorEntity = repositorioProfesor.findById(profesorId)
                        .orElse(null);
                if (profesorEntity != null) {
                    profesor = new DTOProfesor(profesorEntity);
                }
            } catch (NumberFormatException e) {
                // Si no se puede parsear el ID, continuar sin profesor
            }
        }
        
        // Verificar si el estudiante está inscrito
        boolean isEnrolled = clase.getStudentIds().contains(alumnoId.toString());
        LocalDateTime fechaInscripcion = isEnrolled ? LocalDateTime.now() : null;
        
        // Contar alumnos y profesores
        int alumnosCount = clase.getStudentIds().size();
        int profesoresCount = clase.getTeacherIds().size();
        
        return new DTOClaseConDetalles(clase, profesor, isEnrolled, fechaInscripcion, alumnosCount, profesoresCount);
    }

    /**
     * Obtiene información detallada de una clase para un estudiante con información pública del profesor
     * @param claseId ID de la clase
     * @param alumnoId ID del estudiante
     * @return DTOClaseConDetallesPublico con información pública del profesor
     */
    public DTOClaseConDetallesPublico obtenerClaseConDetallesPublicoParaEstudiante(Long claseId, Long alumnoId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Obtener el primer profesor de la clase (profesor principal) con información pública
        DTOProfesorPublico profesor = null;
        if (!clase.getTeacherIds().isEmpty()) {
            try {
                Long profesorId = Long.parseLong(clase.getTeacherIds().get(0));
                Profesor profesorEntity = repositorioProfesor.findById(profesorId)
                        .orElse(null);
                if (profesorEntity != null) {
                    profesor = new DTOProfesorPublico(profesorEntity);
                }
            } catch (NumberFormatException e) {
                // Si no se puede parsear el ID, continuar sin profesor
            }
        }
        
        // Verificar si el estudiante está inscrito
        boolean isEnrolled = clase.getStudentIds().contains(alumnoId.toString());
        LocalDateTime fechaInscripcion = isEnrolled ? LocalDateTime.now() : null;
        
        // Contar alumnos y profesores
        int alumnosCount = clase.getStudentIds().size();
        int profesoresCount = clase.getTeacherIds().size();
        
        return new DTOClaseConDetallesPublico(clase, profesor, isEnrolled, fechaInscripcion, alumnosCount, profesoresCount);
    }

    /**
     * Obtiene información pública de los alumnos de una clase
     * @param claseId ID de la clase
     * @return Lista de DTOAlumnoPublico con información pública de los alumnos
     */
    public List<DTOAlumnoPublico> obtenerAlumnosPublicosDeClase(Long claseId) {
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        return clase.getStudentIds().stream()
                .map(alumnoId -> {
                    try {
                        Long id = Long.parseLong(alumnoId);
                        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
                        return alumno != null ? new DTOAlumnoPublico(alumno) : null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
}
