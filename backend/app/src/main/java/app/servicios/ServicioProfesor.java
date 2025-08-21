package app.servicios;

import app.dtos.*;
import app.entidades.Profesor;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioProfesor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para la gestión de profesores
 * Implementa la lógica de negocio según el UML
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioProfesor {

    private final RepositorioProfesor repositorioProfesor;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los profesores
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> obtenerProfesores() {
        return repositorioProfesor.findAllOrderedById()
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Obtiene un profesor por su ID
     * @param id ID del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor obtenerProfesorPorId(Long id) {
        Profesor profesor = repositorioProfesor.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con ID " + id + " no encontrado."));
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su email
     * @param email Email del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor obtenerProfesorPorEmail(String email) {
        Profesor profesor = repositorioProfesor.findByEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con email " + email + " no encontrado."));
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su usuario
     * @param usuario Nombre de usuario del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor obtenerProfesorPorUsuario(String usuario) {
        Profesor profesor = repositorioProfesor.findByUsuario(usuario)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con usuario " + usuario + " no encontrado."));
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su DNI
     * @param dni DNI del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor obtenerProfesorPorDni(String dni) {
        Profesor profesor = repositorioProfesor.findByDni(dni)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con DNI " + dni + " no encontrado."));
        return new DTOProfesor(profesor);
    }

    /**
     * Busca profesores por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> buscarProfesoresPorNombre(String nombre) {
        return repositorioProfesor.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Busca profesores por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> buscarProfesoresPorApellidos(String apellidos) {
        return repositorioProfesor.findByApellidosContainingIgnoreCase(apellidos)
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Busca profesores por parámetros múltiples usando consultas de base de datos optimizadas
     * @param parametros Parámetros de búsqueda
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> buscarProfesoresPorParametros(DTOParametrosBusquedaProfesor parametros) {
        // Si no hay parámetros, retornar todos
        if (parametros.estaVacio()) {
            return obtenerProfesores();
        }

        return repositorioProfesor.findByFiltros(
                parametros.nombre(),
                parametros.apellidos(),
                parametros.email(),
                parametros.usuario(),
                parametros.dni(),
                parametros.habilitado(),
                parametros.claseId(),
                parametros.sinClases())
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Busca profesores por parámetros con paginación
     * @param parametros Parámetros de búsqueda
     * @param page Número de página (0-indexed)
     * @param size Tamaño de la página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return Respuesta paginada con profesores
     */
    public DTORespuestaPaginada<DTOProfesor> buscarProfesoresPorParametrosPaginados(
            DTOParametrosBusquedaProfesor parametros, int page, int size, String sortBy, String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DTOProfesor> profesoresPage;
        
        if (parametros.estaVacio()) {
            profesoresPage = repositorioProfesor.findAllOrderedById(pageable)
                    .map(DTOProfesor::new);
        } else {
            profesoresPage = repositorioProfesor.findByFiltrosPaginados(
                    parametros.nombre(),
                    parametros.apellidos(),
                    parametros.email(),
                    parametros.usuario(),
                    parametros.dni(),
                    parametros.habilitado(),
                    parametros.claseId(),
                    parametros.sinClases(),
                    pageable)
                    .map(DTOProfesor::new);
        }
        
        return new DTORespuestaPaginada<>(profesoresPage);
    }

    /**
     * Obtiene profesores habilitados
     * @return Lista de DTOProfesor habilitados
     */
    public List<DTOProfesor> obtenerProfesoresHabilitados() {
        return repositorioProfesor.findByEnabledTrue()
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Obtiene profesores habilitados con paginación
     * @param page Número de página (0-indexed)
     * @param size Tamaño de la página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return Respuesta paginada con profesores habilitados
     */
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresHabilitadosPaginados(
            int page, int size, String sortBy, String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DTOProfesor> profesoresPage = repositorioProfesor.findByEnabledTrue(pageable)
                .map(DTOProfesor::new);
        
        return new DTORespuestaPaginada<>(profesoresPage);
    }

    /**
     * Busca profesores que tienen una clase específica asignada
     * @param claseId ID de la clase
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> obtenerProfesoresPorClase(String claseId) {
        return repositorioProfesor.findByClaseId(claseId)
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Obtiene profesores que no tienen clases asignadas
     * @return Lista de DTOProfesor sin clases
     */
    public List<DTOProfesor> obtenerProfesoresSinClases() {
        return repositorioProfesor.findProfesoresSinClases()
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Crea un nuevo profesor según el UML
     * @param peticion Datos del profesor a crear
     * @return DTOProfesor del profesor creado
     */
    public DTOProfesor crearProfesor(DTOPeticionRegistroProfesor peticion) {
        // Validar que no existe un profesor con el mismo usuario, email o DNI
        if (repositorioProfesor.findByUsuario(peticion.usuario()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el usuario: " + peticion.usuario());
        }
        
        if (repositorioProfesor.findByEmail(peticion.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el email: " + peticion.email());
        }
        
        if (repositorioProfesor.findByDni(peticion.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el DNI: " + peticion.dni());
        }

        // Crear el profesor
        Profesor profesor = new Profesor(
                peticion.usuario(),
                passwordEncoder.encode(peticion.password()),
                peticion.nombre(),
                peticion.apellidos(),
                peticion.dni(),
                peticion.email(),
                peticion.numeroTelefono()
        );

        // Asignar clases si se proporcionaron
        if (peticion.clasesId() != null && !peticion.clasesId().isEmpty()) {
            peticion.clasesId().forEach(profesor::agregarClase);
        }

        Profesor profesorGuardado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorGuardado);
    }

    /**
     * Borra un profesor por su ID según el UML
     * @param id ID del profesor a borrar
     * @return true si se borró exitosamente
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public boolean borrarProfesorPorId(Long id) {
        if (!repositorioProfesor.existsById(id)) {
            throw new EntidadNoEncontradaException("Profesor con ID " + id + " no encontrado.");
        }

        repositorioProfesor.deleteById(id);
        return true;
    }

    /**
     * Asigna una clase a un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @return DTOProfesor actualizado
     */
    public DTOProfesor asignarClase(Long profesorId, String claseId) {
        Profesor profesor = repositorioProfesor.findById(profesorId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con ID " + profesorId + " no encontrado."));

        profesor.agregarClase(claseId);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Remueve una clase de un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @return DTOProfesor actualizado
     */
    public DTOProfesor removerClase(Long profesorId, String claseId) {
        Profesor profesor = repositorioProfesor.findById(profesorId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con ID " + profesorId + " no encontrado."));

        profesor.removerClase(claseId);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Cuenta el número de clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Número de clases
     */
    public Integer contarClasesProfesor(Long profesorId) {
        return repositorioProfesor.countClasesByProfesorId(profesorId);
    }

    /**
     * Habilita o deshabilita un profesor
     * @param profesorId ID del profesor
     * @param habilitado true para habilitar, false para deshabilitar
     * @return DTOProfesor actualizado
     */
    public DTOProfesor cambiarEstadoProfesor(Long profesorId, boolean habilitado) {
        Profesor profesor = repositorioProfesor.findById(profesorId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con ID " + profesorId + " no encontrado."));

        profesor.setEnabled(habilitado);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Actualiza parcialmente los datos de un profesor
     * @param profesorId ID del profesor a actualizar
     * @param dtoParcial DTO con los campos a actualizar
     * @return DTOProfesor actualizado
     * @throws EntidadNoEncontradaException si el profesor no existe
     * @throws IllegalArgumentException si el dto está vacío o hay campos inválidos
     */
    public DTOProfesor actualizarProfesor(Long profesorId, DTOActualizacionProfesor dtoParcial) {
        if (dtoParcial == null || dtoParcial.estaVacio()) {
            throw new IllegalArgumentException("No se proporcionaron datos para actualizar");
        }

        Profesor profesor = repositorioProfesor.findById(profesorId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Profesor con ID " + profesorId + " no encontrado"));

        // Actualizar solo los campos no nulos
        if (dtoParcial.nombre() != null) {
            profesor.setNombre(dtoParcial.nombre());
        }

        if (dtoParcial.apellidos() != null) {
            profesor.setApellidos(dtoParcial.apellidos());
        }

        if (dtoParcial.dni() != null) {
            // Verificar que no existe otro profesor con el mismo DNI
            repositorioProfesor.findByDni(dtoParcial.dni())
                    .ifPresent(existente -> {
                        if (!existente.getId().equals(profesorId)) {
                            throw new IllegalArgumentException("Ya existe otro profesor con el DNI: " + dtoParcial.dni());
                        }
                    });
            profesor.setDni(dtoParcial.dni());
        }

        if (dtoParcial.email() != null) {
            // Verificar que no existe otro profesor con el mismo email
            repositorioProfesor.findByEmail(dtoParcial.email())
                    .ifPresent(existente -> {
                        if (!existente.getId().equals(profesorId)) {
                            throw new IllegalArgumentException("Ya existe otro profesor con el email: " + dtoParcial.email());
                        }
                    });
            profesor.setEmail(dtoParcial.email());
        }

        if (dtoParcial.numeroTelefono() != null) {
            profesor.setNumeroTelefono(dtoParcial.numeroTelefono());
        }

        if (dtoParcial.clasesId() != null) {
            // Actualizar clases (reemplazar todas)
            profesor.getClasesId().clear();
            dtoParcial.clasesId().forEach(profesor::agregarClase);
        }

        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Obtiene estadísticas del total de profesores
     * @return total de profesores
     */
    public long contarTotalProfesores() {
        return repositorioProfesor.count();
    }
    
    /**
     * Cuenta profesores habilitados
     * @return número de profesores habilitados
     */
    public long contarProfesoresHabilitados() {
        return repositorioProfesor.countByEnabledTrue();
    }
    
    /**
     * Cuenta profesores deshabilitados
     * @return número de profesores deshabilitados
     */
    public long contarProfesoresDeshabilitados() {
        return repositorioProfesor.countByEnabledFalse();
    }
}
