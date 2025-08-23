package app.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOAlumnoPublico;
import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPerfilAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.dtos.DTORespuestaAlumnosClase;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.entidades.Clase;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioClase;
import app.util.ExceptionUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioAlumno {

    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioClase repositorioClase;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCachePassword servicioCachePassword;

    @Transactional(readOnly = true)
    public List<DTOAlumno> obtenerAlumnos() {
        return repositorioAlumno.findAllOrderedById()
                .stream()
                .map(DTOAlumno::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorId(Long id) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        return new DTOAlumno(alumno);
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorEmail(String email) {
        Alumno alumno = repositorioAlumno.findByEmail(email).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "email", email);
        return new DTOAlumno(alumno);
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorUsuario(String usuario) {
        Alumno alumno = repositorioAlumno.findByUsuario(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "usuario", usuario);
        return new DTOAlumno(alumno);
    }

    /**
     * Obtiene el perfil del alumno (sin información sensible)
     * @param usuario Usuario del alumno
     * @return DTOPerfilAlumno
     */
    @Transactional(readOnly = true)
    public DTOPerfilAlumno obtenerPerfilAlumnoPorUsuario(String usuario) {
        Alumno alumno = repositorioAlumno.findByUsuario(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "usuario", usuario);
        return new DTOPerfilAlumno(alumno);
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorDni(String dni) {
        Alumno alumno = repositorioAlumno.findByDni(dni).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "DNI", dni);
        return new DTOAlumno(alumno);
    }

    @Transactional(readOnly = true)
    public List<DTOAlumno> buscarAlumnosPorParametros(DTOParametrosBusquedaAlumno parametros) {
        // Enhanced search logic with "q" parameter support
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters) without pagination
                List<Alumno> alumnos = repositorioAlumno.findAll().stream()
                    .filter(a -> {
                        String searchTerm = parametros.q().toLowerCase();
                        boolean generalMatch = a.getNombre().toLowerCase().contains(searchTerm) ||
                                             a.getApellidos().toLowerCase().contains(searchTerm) ||
                                             a.getDni().toLowerCase().contains(searchTerm) ||
                                             a.getEmail().toLowerCase().contains(searchTerm);
                        
                        boolean specificMatch = (parametros.nombre() == null || 
                                               a.getNombre().toLowerCase().contains(parametros.nombre().toLowerCase())) &&
                                              (parametros.apellidos() == null || 
                                               a.getApellidos().toLowerCase().contains(parametros.apellidos().toLowerCase())) &&
                                              (parametros.dni() == null || 
                                               a.getDni().toLowerCase().contains(parametros.dni().toLowerCase())) &&
                                              (parametros.email() == null || 
                                               a.getEmail().toLowerCase().contains(parametros.email().toLowerCase())) &&
                                              (parametros.matriculado() == null || 
                                               a.isMatriculado() == parametros.matriculado());
                        
                        return generalMatch && specificMatch;
                    })
                    .collect(Collectors.toList());
                
                return alumnos.stream().map(DTOAlumno::new).toList();
            } else {
                // Use only general search without pagination
                List<Alumno> alumnos = repositorioAlumno.findAll().stream()
                    .filter(a -> {
                        String searchTerm = parametros.q().toLowerCase();
                        return a.getNombre().toLowerCase().contains(searchTerm) ||
                               a.getApellidos().toLowerCase().contains(searchTerm) ||
                               a.getDni().toLowerCase().contains(searchTerm) ||
                               a.getEmail().toLowerCase().contains(searchTerm);
                    })
                    .collect(Collectors.toList());
                
                return alumnos.stream().map(DTOAlumno::new).toList();
            }
        } else {
            // Use existing specific search logic
            if (parametros.hasSpecificFilters()) {
                return repositorioAlumno.findByFiltros(
                        parametros.nombre(), 
                        parametros.apellidos(),
                        parametros.dni(),
                        parametros.email(),
                        parametros.matriculado())
                        .stream()
                        .map(DTOAlumno::new)
                        .toList();
            } else {
                return obtenerAlumnos();
            }
        }
    }

    public List<DTOAlumno> obtenerAlumnosPorMatriculado(boolean matriculado) {
        return repositorioAlumno.findByMatriculado(matriculado)
                .stream()
                .map(DTOAlumno::new)
                .toList();
    }

    public DTOAlumno crearAlumno(DTOPeticionRegistroAlumno peticion) {
        // Validar que no existan duplicados
        if (repositorioAlumno.existsByUsuario(peticion.usuario())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el usuario: " + peticion.usuario());
        }
        
        if (repositorioAlumno.existsByEmail(peticion.email())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el email: " + peticion.email());
        }
        
        if (repositorioAlumno.existsByDni(peticion.dni())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el DNI: " + peticion.dni());
        }

        // Crear el alumno
        Alumno alumno = new Alumno(
            peticion.usuario(),
            servicioCachePassword.encodePassword(peticion.password()),
            peticion.nombre(),
            peticion.apellidos(),
            peticion.dni(),
            peticion.email(),
            peticion.numeroTelefono()
        );

        Alumno alumnoGuardado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoGuardado);
    }

    public DTOAlumno actualizarAlumno(Long id, DTOActualizacionAlumno dtoParcial) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);

        // Actualizar campos no nulos
        if (dtoParcial.nombre() != null) {
            alumno.setNombre(dtoParcial.nombre());
        }
        
        if (dtoParcial.apellidos() != null) {
            alumno.setApellidos(dtoParcial.apellidos());
        }
        
        if (dtoParcial.email() != null) {
            // Verificar que no exista otro alumno con ese email
            if (!alumno.getEmail().equals(dtoParcial.email()) && 
                repositorioAlumno.existsByEmail(dtoParcial.email())) {
                ExceptionUtils.throwValidationError("Ya existe un alumno con el email: " + dtoParcial.email());
            }
            alumno.setEmail(dtoParcial.email());
        }
        
        if (dtoParcial.dni() != null) {
            // Verificar que no exista otro alumno con ese DNI
            if (!alumno.getDni().equals(dtoParcial.dni()) && 
                repositorioAlumno.existsByDni(dtoParcial.dni())) {
                ExceptionUtils.throwValidationError("Ya existe un alumno con el DNI: " + dtoParcial.dni());
            }
            alumno.setDni(dtoParcial.dni());
        }
        
        if (dtoParcial.numeroTelefono() != null) {
            alumno.setNumeroTelefono(dtoParcial.numeroTelefono());
        }

        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno cambiarEstadoMatricula(Long id, boolean matriculado) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        alumno.setMatriculado(matriculado);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno habilitarDeshabilitarAlumno(Long id, boolean habilitar) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        alumno.setEnabled(habilitar);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno borrarAlumnoPorId(Long id) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        repositorioAlumno.deleteById(id);
        return new DTOAlumno(alumno);
    }

    // metodos de estadísticas útiles para administradores
    public long contarAlumnosMatriculados() {
        return repositorioAlumno.countByMatriculado(true);
    }

    public long contarAlumnosNoMatriculados() {
        return repositorioAlumno.countByMatriculado(false);
    }

    public long contarTotalAlumnos() {
        return repositorioAlumno.count();
    }

    // métodos para gestionar relaciones con clases
    
    /**
     * Inscribir un alumno en una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return DTOAlumno actualizado
     */
    public DTOAlumno inscribirEnClase(Long alumnoId, Long claseId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar que el alumno no esté ya inscrito en la clase
        if (alumno.getClasesId().contains(claseId.toString())) {
            throw new IllegalArgumentException("El alumno ya está inscrito en esta clase.");
        }
        
        // Agregar la clase al alumno
        alumno.agregarClase(claseId.toString());
        
        // Agregar el alumno a la clase
        clase.agregarAlumno(alumnoId.toString());
        
        // Guardar cambios
        repositorioClase.save(clase);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        
        return new DTOAlumno(alumnoActualizado);
    }
    
    /**
     * Dar de baja a un alumno de una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return DTOAlumno actualizado
     */
    public DTOAlumno darDeBajaDeClase(Long alumnoId, Long claseId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar que el alumno esté inscrito en la clase
        if (!alumno.getClasesId().contains(claseId.toString())) {
            throw new IllegalArgumentException("El alumno no está inscrito en esta clase.");
        }
        
        // Remover la clase del alumno
        alumno.removerClase(claseId.toString());
        
        // Remover el alumno de la clase
        clase.removerAlumno(alumnoId.toString());
        
        // Guardar cambios
        repositorioClase.save(clase);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        
        return new DTOAlumno(alumnoActualizado);
    }
    
    /**
     * Obtener todas las clases en las que está inscrito un alumno
     * @param alumnoId ID del alumno
     * @return Lista de clases
     */
    public List<DTOClase> obtenerClasesPorAlumno(Long alumnoId) {
        // Primero verificamos que el alumno existe
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
        
        // Obtenemos las clases del alumno usando el repositorio de clase
        return repositorioClase.findByAlumnoId(alumnoId.toString())
                .stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Verificar si un alumno está inscrito en una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return true si está inscrito, false en caso contrario
     */
    public boolean estaInscritoEnClase(Long alumnoId, Long claseId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        return alumno.estaInscritoEnClase(claseId.toString());
    }
    
    /**
     * Contar el número de clases en las que está inscrito un alumno
     * @param alumnoId ID del alumno
     * @return Número de clases
     */
    public int contarClasesPorAlumno(Long alumnoId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        return alumno.getClasesId().size();
    }

    // metodos con paginación
    
    /**
     * Obtiene todos los alumnos con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los alumnos y metadatos de paginación
     */
    @Transactional
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPaginados(
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
        Page<Alumno> pageAlumnos = repositorioAlumno.findAllPaged(pageable);
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Busca alumnos por parámetros con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> buscarAlumnosPorParametrosPaginados(
            DTOParametrosBusquedaAlumno parametros, int page, int size, String sortBy, String sortDirection) {
        
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
        Page<Alumno> pageAlumnos;
        
        // Enhanced search logic with "q" parameter support
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters)
                pageAlumnos = repositorioAlumno.findByGeneralAndSpecificFilters(
                    parametros.q(),
                    parametros.nombre(),
                    parametros.apellidos(),
                    parametros.dni(),
                    parametros.email(),
                    parametros.matriculado(),
                    pageable
                );
            } else {
                // Use only general search
                pageAlumnos = repositorioAlumno.findByGeneralSearch(parametros.q(), pageable);
            }
        } else {
            // Use existing specific search logic
            if (parametros.hasSpecificFilters()) {
                pageAlumnos = repositorioAlumno.findByFiltrosPaged(
                    parametros.nombre(),
                    parametros.apellidos(),
                    parametros.dni(),
                    parametros.email(),
                    parametros.matriculado(),
                    pageable
                );
            } else {
                pageAlumnos = repositorioAlumno.findAllPaged(pageable);
            }
        }
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene alumnos matriculados/no matriculados con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPorMatriculadoPaginados(
            boolean matriculado, int page, int size, String sortBy, String sortDirection) {
        
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
        Page<Alumno> pageAlumnos = repositorioAlumno.findByMatriculadoPaged(matriculado, pageable);
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene alumnos inscritos en una clase específica con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPorClasePaginados(
            Long claseId, int page, int size, String sortBy, String sortDirection) {
        
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
        
        // Primero verificamos que la clase existe
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Obtenemos los alumnos de la clase usando filtrado en memoria
        // Nota: En un escenario real se implementaría una consulta específica
        List<Alumno> alumnosDeClase = repositorioAlumno.findAll().stream()
                .filter(alumno -> alumno.getClasesId() != null && 
                                 alumno.getClasesId().contains(claseId.toString()))
                .collect(Collectors.toList());
        
        // Aplicamos paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDeClase.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDeClase.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDeClase.size());
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }

    /**
     * Obtiene alumnos disponibles (habilitados y matriculados) con paginación
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return DTORespuestaPaginada con los alumnos disponibles
     */
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosDisponiblesPaginados(
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
        
        // Obtener solo alumnos habilitados y matriculados
        // Nota: En un escenario real se implementaría una consulta específica en el repositorio
        List<Alumno> alumnosDisponibles = repositorioAlumno.findAll().stream()
                .filter(alumno -> alumno.isEnabled() && alumno.isMatriculado())
                .collect(Collectors.toList());
        
        // Aplicar paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDisponibles.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDisponibles.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDisponibles.size());
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }

    /**
     * Obtiene alumnos inscritos en una clase específica con diferentes niveles de acceso
     * según el rol del usuario autenticado
     * @param claseId ID de la clase
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @param userRole Rol del usuario autenticado
     * @param currentUserId ID del usuario autenticado (para verificar si es profesor de la clase)
     * @return DTORespuestaAlumnosClase con los alumnos de la clase según el nivel de acceso
     */
    public DTORespuestaAlumnosClase obtenerAlumnosPorClaseConNivelAcceso(
            Long claseId, int page, int size, String sortBy, String sortDirection, 
            String userRole, Long currentUserId) {
        
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
        
        // Primero verificamos que la clase existe
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if ("ALUMNO".equals(userRole)) {
            // Para estudiantes, verificar que estén inscritos en la clase
            Alumno alumno = repositorioAlumno.findById(currentUserId).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", currentUserId);
            
            if (!alumno.estaInscritoEnClase(claseId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase. Debes estar inscrito.");
            }
        }
        
        // Obtenemos los alumnos de la clase usando filtrado en memoria
        List<Alumno> alumnosDeClase = repositorioAlumno.findAll().stream()
                .filter(alumno -> alumno.getClasesId() != null && 
                                 alumno.getClasesId().contains(claseId.toString()))
                .collect(Collectors.toList());
        
        // Aplicamos paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDeClase.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDeClase.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDeClase.size());
        
        // Convertir a DTOs según el nivel de acceso
        Page<?> pageDTOs;
        String tipoInformacion;
        
        if ("ADMIN".equals(userRole)) {
            // Admin: ve toda la información
            pageDTOs = pageAlumnos.map(DTOAlumno::new);
            tipoInformacion = DTORespuestaAlumnosClase.TIPO_COMPLETA;
        } else if ("PROFESOR".equals(userRole)) {
            // Profesor: verificar si es profesor de esta clase específica
            // La clase ya fue verificada arriba, podemos usarla directamente
            
            if (clase.getProfesoresId().contains(currentUserId.toString())) {
                // Es profesor de esta clase: ve toda la información
                pageDTOs = pageAlumnos.map(DTOAlumno::new);
                tipoInformacion = DTORespuestaAlumnosClase.TIPO_COMPLETA;
            } else {
                // No es profesor de esta clase: solo información pública
                pageDTOs = pageAlumnos.map(DTOAlumnoPublico::new);
                tipoInformacion = DTORespuestaAlumnosClase.TIPO_PUBLICA;
            }
        } else {
            // Alumno: solo información pública
            pageDTOs = pageAlumnos.map(DTOAlumnoPublico::new);
            tipoInformacion = DTORespuestaAlumnosClase.TIPO_PUBLICA;
        }
        
        return new DTORespuestaAlumnosClase(pageDTOs, tipoInformacion);
    }
}