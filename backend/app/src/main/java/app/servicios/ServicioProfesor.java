package app.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOActualizacionProfesor;
import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOProfesor;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Clase;
import app.entidades.Profesor;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioProfesor;
import app.util.ExceptionUtils;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de profesores
 * Implementa la lógica de negocio según el UML
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioProfesor {

    private final RepositorioProfesor repositorioProfesor;
    private final RepositorioClase repositorioClase;
    // Uncomment if needed in the future
    // private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCachePassword servicioCachePassword;

    /**
     * Obtiene todos los profesores
     * @return Lista de DTOProfesor
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorId(Long id) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su email
     * @param email Email del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorEmail(String email) {
        Profesor profesor = repositorioProfesor.findByEmail(email).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "email", email);
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su nombre de usuario
     * @param usuario Nombre de usuario del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorUsuario(String usuario) {
        Profesor profesor = repositorioProfesor.findByUsuario(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "usuario", usuario);
        return new DTOProfesor(profesor);
    }

    /**
     * Obtiene un profesor por su DNI
     * @param dni DNI del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorDni(String dni) {
        Profesor profesor = repositorioProfesor.findByDni(dni).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "DNI", dni);
        return new DTOProfesor(profesor);
    }
    
    /**
     * Busca profesores por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de profesores que contienen el nombre especificado
     */
    public List<DTOProfesor> buscarProfesoresPorNombre(String nombre) {
        List<Profesor> profesores = repositorioProfesor.findByNombreContainingIgnoreCase(nombre);
        return profesores.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca profesores por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores que contienen los apellidos especificados
     */
    public List<DTOProfesor> buscarProfesoresPorApellidos(String apellidos) {
        List<Profesor> profesores = repositorioProfesor.findByApellidosContainingIgnoreCase(apellidos);
        return profesores.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca profesores según diversos parámetros
     * @param parametros Parámetros de búsqueda
     * @return Lista de DTOProfesor
     */
    public List<DTOProfesor> buscarProfesoresPorParametros(DTOParametrosBusquedaProfesor parametros) {
        // Si todos los parámetros son nulos, devolver todos los profesores
        if (parametros.nombre() == null && parametros.apellidos() == null && 
            parametros.dni() == null && parametros.email() == null) {
            return obtenerProfesores();
        }
        
        // Buscar por filtros básicos
        List<Profesor> profesores = repositorioProfesor.findAll().stream()
                .filter(p -> (parametros.nombre() == null || 
                              p.getNombre().toLowerCase().contains(parametros.nombre().toLowerCase())))
                .filter(p -> (parametros.apellidos() == null || 
                              p.getApellidos().toLowerCase().contains(parametros.apellidos().toLowerCase())))
                .filter(p -> (parametros.dni() == null || 
                              p.getDni().toLowerCase().contains(parametros.dni().toLowerCase())))
                .filter(p -> (parametros.email() == null || 
                              p.getEmail().toLowerCase().contains(parametros.email().toLowerCase())))
                .collect(Collectors.toList());
        
        return profesores.stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Crea un nuevo profesor
     * @param peticion Datos del profesor a crear
     * @return DTOProfesor con los datos del profesor creado
     * @throws IllegalArgumentException si ya existe un profesor con el mismo usuario, email o DNI
     */
    public DTOProfesor crearProfesor(DTOPeticionRegistroProfesor peticion) {
        // Validar que no existan duplicados
        // Verificar usuario duplicado
        if (repositorioProfesor.findByUsuario(peticion.usuario()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el usuario: " + peticion.usuario());
        }
        
        // Verificar email duplicado
        if (repositorioProfesor.findByEmail(peticion.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el email: " + peticion.email());
        }
        
        // Verificar DNI duplicado
        if (repositorioProfesor.findByDni(peticion.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un profesor con el DNI: " + peticion.dni());
        }

        // Crear el profesor
        Profesor profesor = new Profesor(
            peticion.usuario(),
            servicioCachePassword.encodePassword(peticion.password()),
            peticion.nombre(),
            peticion.apellidos(),
            peticion.dni(),
            peticion.email(),
            peticion.numeroTelefono()
        );

        Profesor profesorGuardado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorGuardado);
    }

    /**
     * Actualiza los datos de un profesor
     * @param id ID del profesor a actualizar
     * @param dtoParcial Datos a actualizar
     * @return DTOProfesor con los datos actualizados
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     * @throws IllegalArgumentException si se intenta actualizar a un valor duplicado
     */
    public DTOProfesor actualizarProfesor(Long id, DTOActualizacionProfesor dtoParcial) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);

        // Actualizar campos no nulos
        if (dtoParcial.nombre() != null) {
            profesor.setNombre(dtoParcial.nombre());
        }
        
        if (dtoParcial.apellidos() != null) {
            profesor.setApellidos(dtoParcial.apellidos());
        }
        
        if (dtoParcial.email() != null) {
            // Verificar que no exista otro profesor con ese email
            if (!profesor.getEmail().equals(dtoParcial.email()) && 
                repositorioProfesor.findByEmail(dtoParcial.email()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un profesor con el email: " + dtoParcial.email());
            }
            profesor.setEmail(dtoParcial.email());
        }
        
        if (dtoParcial.dni() != null) {
            // Verificar que no exista otro profesor con ese DNI
            if (!profesor.getDni().equals(dtoParcial.dni()) && 
                repositorioProfesor.findByDni(dtoParcial.dni()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un profesor con el DNI: " + dtoParcial.dni());
            }
            profesor.setDni(dtoParcial.dni());
        }
        
        if (dtoParcial.numeroTelefono() != null) {
            profesor.setNumeroTelefono(dtoParcial.numeroTelefono());
        }

        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Habilita o deshabilita un profesor
     * @param id ID del profesor
     * @param habilitar true para habilitar, false para deshabilitar
     * @return DTOProfesor actualizado
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor habilitarDeshabilitarProfesor(Long id, boolean habilitar) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
        
        profesor.setEnabled(habilitar);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Borra un profesor por su ID
     * @param id ID del profesor
     * @return true si el profesor fue borrado correctamente
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public boolean borrarProfesorPorId(Long id) {
        // Verificar que el profesor existe antes de intentar borrarlo
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);

        repositorioProfesor.deleteById(id);
        return true;
    }

    /**
     * Asigna una clase a un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase (como String)
     * @return DTOProfesor actualizado
     */
    public DTOProfesor asignarClase(Long profesorId, String claseId) {
        try {
            Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
            
            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

            // Verificar si el profesor ya tiene asignada la clase
            if (profesor.getClasesId().contains(claseId)) {
                throw new IllegalArgumentException("El profesor ya tiene asignada esta clase.");
            }
            
            // Asignar la clase al profesor
            profesor.agregarClase(claseId);
            
            // Asignar el profesor a la clase
            clase.agregarProfesor(profesorId.toString());
            
            // Guardar los cambios
            repositorioClase.save(clase);
            Profesor profesorActualizado = repositorioProfesor.save(profesor);
            
            return new DTOProfesor(profesorActualizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }

    /**
     * Remueve una clase de un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase (como String)
     * @return DTOProfesor actualizado
     */
    public DTOProfesor removerClase(Long profesorId, String claseId) {
        try {
            Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
            
            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

            // Verificar si el profesor tiene asignada la clase
            if (!profesor.getClasesId().contains(claseId)) {
                throw new IllegalArgumentException("El profesor no tiene asignada esta clase.");
            }
            
            // Remover la clase del profesor
            profesor.removerClase(claseId);
            
            // Remover el profesor de la clase
            clase.removerProfesor(profesorId.toString());
            
            // Guardar los cambios
            repositorioClase.save(clase);
            Profesor profesorActualizado = repositorioProfesor.save(profesor);
            
            return new DTOProfesor(profesorActualizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }

    /**
     * Cuenta el número de clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Número de clases
     */
    public Integer contarClasesProfesor(Long profesorId) {
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        return profesor.getClasesId().size();
    }
    
    /**
     * Obtiene profesores por clase
     * @param claseId ID de la clase
     * @return Lista de profesores que imparten la clase
     */
    public List<DTOProfesor> obtenerProfesoresPorClase(String claseId) {
        List<Profesor> profesoresDeClase = repositorioProfesor.findByClaseId(claseId);
        return profesoresDeClase.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene profesores sin clases asignadas
     * @return Lista de profesores sin clases
     */
    public List<DTOProfesor> obtenerProfesoresSinClases() {
        List<Profesor> profesoresSinClases = repositorioProfesor.findProfesoresSinClases();
        return profesoresSinClases.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Lista de clases
     */
    public List<DTOClase> obtenerClasesPorProfesor(Long profesorId) {
        // Verificar que el profesor existe
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        // Obtener las clases del profesor
        return repositorioClase.findByProfesorId(profesorId.toString())
                .stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un profesor imparte una clase específica
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClase(Long profesorId, String claseId) {
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        return profesor.imparteClase(claseId);
    }

    /**
     * Habilita o deshabilita un profesor
     * @param profesorId ID del profesor
     * @param habilitado true para habilitar, false para deshabilitar
     * @return DTOProfesor actualizado
     */
    public DTOProfesor cambiarEstadoProfesor(Long profesorId, boolean habilitado) {
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
        
        profesor.setEnabled(habilitado);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    // Métodos con paginación

    /**
     * Obtiene todos los profesores con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los profesores y metadatos de paginación
     */
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresPaginados(
            int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20; // Máximo 100 elementos por página
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Implementar paginación manualmente
        List<Profesor> allProfesores = repositorioProfesor.findAll(Sort.by(direction, sortBy));
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProfesores.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Profesor> pageContent = allProfesores.subList(start, end);
        Page<Profesor> pageProfesores = new PageImpl<>(pageContent, pageable, allProfesores.size());
        
        // Convertir a DTOs
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Busca profesores por parámetros con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> buscarProfesoresPorParametrosPaginados(
            DTOParametrosBusquedaProfesor parametros, int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Filtrar profesores según parámetros
        List<Profesor> profesoresFiltrados;
        
        // Si todos los parámetros son nulos, obtener todos
        if (parametros.nombre() == null && parametros.apellidos() == null && 
            parametros.dni() == null && parametros.email() == null) {
            profesoresFiltrados = repositorioProfesor.findAll(Sort.by(direction, sortBy));
        } else {
            // Filtrar según parámetros
            profesoresFiltrados = repositorioProfesor.findAll(Sort.by(direction, sortBy))
                .stream()
                .filter(p -> (parametros.nombre() == null || 
                            p.getNombre().toLowerCase().contains(parametros.nombre().toLowerCase())))
                .filter(p -> (parametros.apellidos() == null || 
                            p.getApellidos().toLowerCase().contains(parametros.apellidos().toLowerCase())))
                .filter(p -> (parametros.dni() == null || 
                            p.getDni().toLowerCase().contains(parametros.dni().toLowerCase())))
                .filter(p -> (parametros.email() == null || 
                            p.getEmail().toLowerCase().contains(parametros.email().toLowerCase())))
                .collect(Collectors.toList());
        }
        
        // Aplicar paginación
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), profesoresFiltrados.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Profesor> pageContent = profesoresFiltrados.subList(start, end);
        Page<Profesor> pageProfesores = new PageImpl<>(pageContent, pageable, profesoresFiltrados.size());
        
        // Convertir a DTOs
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene profesores que imparten una clase específica con paginación
     * @param claseId ID de la clase (como String)
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar
     * @param sortDirection dirección de ordenamiento (ASC/DESC)
     * @return DTORespuestaPaginada con los profesores que imparten la clase
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresPorClasePaginados(
            String claseId, int page, int size, String sortBy, String sortDirection) {
        
        try {
            // Validar parámetros
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20;
            if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
            
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(sortDirection);
            } catch (Exception e) {
                direction = Sort.Direction.ASC;
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Verificar que la clase existe
            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
            
            // Filtrar profesores por clase manualmente
            List<Profesor> profesoresDeClase = repositorioProfesor.findAll(Sort.by(direction, sortBy))
                .stream()
                .filter(p -> p.getClasesId() != null && p.getClasesId().contains(claseId))
                .collect(Collectors.toList());
            
            // Aplicar paginación
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), profesoresDeClase.size());
            
            if (start > end) {
                start = 0;
                end = 0;
            }
            
            List<Profesor> pageContent = profesoresDeClase.subList(start, end);
            Page<Profesor> pageProfesores = new PageImpl<>(pageContent, pageable, profesoresDeClase.size());
            
            // Convertir a DTOs
            Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
            
            return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los profesores habilitados
     * @return Lista de profesores habilitados
     */
    public List<DTOProfesor> obtenerProfesoresHabilitados() {
        List<Profesor> profesoresHabilitados = repositorioProfesor.findByEnabledTrue();
        return profesoresHabilitados.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los profesores habilitados con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los profesores habilitados y metadatos de paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresHabilitadosPaginados(
            int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Profesor> pageProfesores = repositorioProfesor.findByEnabledTrue(pageable);
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Cuenta profesores habilitados
     * @return Número de profesores habilitados
     */
    public long contarProfesoresHabilitados() {
        return repositorioProfesor.countByEnabledTrue();
    }
    
    /**
     * Cuenta profesores deshabilitados
     * @return Número de profesores deshabilitados
     */
    public long contarProfesoresDeshabilitados() {
        return repositorioProfesor.countByEnabledFalse();
    }
    
    /**
     * Cuenta el número total de profesores
     */
    public long contarTotalProfesores() {
        return repositorioProfesor.count();
    }
}