package app.servicios;

import app.dtos.*;
import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Material;
import app.entidades.Taller;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioClase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de clases
 * Implementa la lógica de negocio según el UML
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioClase {

    private final RepositorioClase repositorioClase;

    /**
     * Obtiene todas las clases
     * @return Lista de DTOClase
     */
    public List<DTOClase> obtenerClases() {
        return repositorioClase.findAllOrderedById().stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una clase por su ID
     * @param id ID de la clase
     * @return DTOClase
     * @throws EntidadNoEncontradaException si no se encuentra la clase
     */
    public DTOClase obtenerClasePorId(Long id) {
        return repositorioClase.findById(id)
                .map(DTOClase::new)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + id + " no encontrada."));
    }

    /**
     * Obtiene una clase por su título
     * @param titulo Título de la clase
     * @return DTOClase
     * @throws EntidadNoEncontradaException si no se encuentra la clase
     */
    public DTOClase obtenerClasePorTitulo(String titulo) {
        return repositorioClase.findByTitulo(titulo)
                .map(DTOClase::new)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con título '" + titulo + "' no encontrada."));
    }

    /**
     * Crea una nueva clase de tipo Curso
     * @param peticion Datos para crear el curso
     * @param fechaInicio Fecha de inicio del curso
     * @param fechaFin Fecha de fin del curso
     * @return DTOCurso con los datos del curso creado
     */
    public DTOCurso crearCurso(DTOPeticionCrearClase peticion, LocalDate fechaInicio, LocalDate fechaFin) {
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
        Optional<Clase> claseOpt = repositorioClase.findByTitulo(titulo);
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
    public DTORespuestaPaginada<DTOClase> buscarClases(DTOParametrosBusquedaClase parametros) {
        // Configurar paginación
        Pageable pageable = PageRequest.of(
                parametros.pagina(),
                parametros.tamanoPagina(),
                Sort.by(Sort.Direction.fromString(parametros.ordenDireccion()), parametros.ordenCampo())
        );
        
        // Realizar búsqueda según criterios proporcionados
        Page<Clase> resultado;
        
        if (parametros.titulo() != null && !parametros.titulo().isEmpty()) {
            // Aplicar paginación manualmente ya que el repositorio no tiene sobrecarga con Pageable
            List<Clase> clases = repositorioClase.findByTituloContainingIgnoreCase(parametros.titulo());
            resultado = convertirListaAPagina(clases, pageable);
        } else if (parametros.descripcion() != null && !parametros.descripcion().isEmpty()) {
            List<Clase> clases = repositorioClase.findByDescripcionContainingIgnoreCase(parametros.descripcion());
            resultado = convertirListaAPagina(clases, pageable);
        } else if (parametros.presencialidad() != null) {
            List<Clase> clases = repositorioClase.findByPresencialidad(parametros.presencialidad());
            resultado = convertirListaAPagina(clases, pageable);
        } else if (parametros.nivel() != null) {
            List<Clase> clases = repositorioClase.findByNivel(parametros.nivel());
            resultado = convertirListaAPagina(clases, pageable);
        } else if (parametros.precioMaximo() != null && parametros.precioMinimo() == null) {
            List<Clase> clases = repositorioClase.findByPrecioLessThanEqual(parametros.precioMaximo());
            resultado = convertirListaAPagina(clases, pageable);
        } else if (parametros.precioMinimo() != null && parametros.precioMaximo() != null) {
            List<Clase> clases = repositorioClase.findByPrecioBetween(parametros.precioMinimo(), parametros.precioMaximo());
            resultado = convertirListaAPagina(clases, pageable);
        } else {
            resultado = repositorioClase.findAll(pageable);
        }
        
        // Construir respuesta paginada usando el constructor que acepta un Page
        return new DTORespuestaPaginada<>(resultado.map(clase -> new DTOClase(clase)));
    }

    /**
     * Agrega un alumno a una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOClase agregarAlumno(Long claseId, String alumnoId) {
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
        // Verificar si el alumno ya está en la clase para evitar duplicados
        if (clase.getAlumnosId().contains(alumnoId)) {
            // El alumno ya está en la clase, devolver la clase sin cambios
            return new DTOClase(clase);
        }
        
        clase.agregarAlumno(alumnoId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Remueve un alumno de una clase
     * @param claseId ID de la clase
     * @param alumnoId ID del alumno
     * @return DTOClase actualizada
     */
    public DTOClase removerAlumno(Long claseId, String alumnoId) {
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
        clase.removerAlumno(alumnoId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Agrega un profesor a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DTOClase agregarProfesor(Long claseId, String profesorId) {
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
        // Verificar si el profesor ya está en la clase para evitar duplicados
        if (clase.getProfesoresId().contains(profesorId)) {
            // El profesor ya está en la clase, devolver la clase sin cambios
            return new DTOClase(clase);
        }
        
        clase.agregarProfesor(profesorId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Remueve un profesor de una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @return DTOClase actualizada
     */
    public DTOClase removerProfesor(Long claseId, String profesorId) {
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
        clase.removerProfesor(profesorId);
        Clase claseActualizada = repositorioClase.save(clase);
        return new DTOClase(claseActualizada);
    }

    /**
     * Agrega un ejercicio a una clase
     * @param claseId ID de la clase
     * @param ejercicioId ID del ejercicio
     * @return DTOClase actualizada
     */
    public DTOClase agregarEjercicio(Long claseId, String ejercicioId) {
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
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
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
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
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
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
        Clase clase = repositorioClase.findById(claseId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Clase con ID " + claseId + " no encontrada."));
        
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
        return repositorioClase.countAlumnosByClaseId(claseId);
    }

    /**
     * Cuenta el número de profesores en una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    public Integer contarProfesoresEnClase(Long claseId) {
        return repositorioClase.countProfesoresByClaseId(claseId);
    }
}