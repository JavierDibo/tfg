package app.servicios;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioAlumno;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioAlumno {

    private final RepositorioAlumno repositorioAlumno;
    private final PasswordEncoder passwordEncoder;

    public List<DTOAlumno> obtenerAlumnos() {
        return repositorioAlumno.findAllOrderedById()
                .stream()
                .map(DTOAlumno::new)
                .toList();
    }

    public DTOAlumno obtenerAlumnoPorId(Long id) {
        Alumno alumno = repositorioAlumno.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con ID " + id + " no encontrado."));
        return new DTOAlumno(alumno);
    }

    public DTOAlumno obtenerAlumnoPorEmail(String email) {
        Alumno alumno = repositorioAlumno.findByEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con email " + email + " no encontrado."));
        return new DTOAlumno(alumno);
    }

    public DTOAlumno obtenerAlumnoPorUsuario(String usuario) {
        Alumno alumno = repositorioAlumno.findByUsuario(usuario)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con usuario " + usuario + " no encontrado."));
        return new DTOAlumno(alumno);
    }

    public DTOAlumno obtenerAlumnoPorDni(String dni) {
        Alumno alumno = repositorioAlumno.findByDni(dni)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con DNI " + dni + " no encontrado."));
        return new DTOAlumno(alumno);
    }

    public List<DTOAlumno> buscarAlumnosPorParametros(DTOParametrosBusquedaAlumno parametros) {
        // Si todos los parámetros son nulos, devolver todos los alumnos
        if (parametros.nombre() == null && parametros.apellidos() == null && 
            parametros.dni() == null && parametros.email() == null && parametros.matriculado() == null) {
            return obtenerAlumnos();
        }
        
        return repositorioAlumno.findByFiltros(
                parametros.nombre(), 
                parametros.apellidos(),
                parametros.dni(),
                parametros.email(),
                parametros.matriculado())
                .stream()
                .map(DTOAlumno::new)
                .toList();
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
            throw new IllegalArgumentException("Ya existe un alumno con el usuario: " + peticion.usuario());
        }
        
        if (repositorioAlumno.existsByEmail(peticion.email())) {
            throw new IllegalArgumentException("Ya existe un alumno con el email: " + peticion.email());
        }
        
        if (repositorioAlumno.existsByDni(peticion.dni())) {
            throw new IllegalArgumentException("Ya existe un alumno con el DNI: " + peticion.dni());
        }

        // Crear el alumno
        Alumno alumno = new Alumno(
            peticion.usuario(),
            passwordEncoder.encode(peticion.contraseña()),
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
        Alumno alumno = repositorioAlumno.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con ID " + id + " no encontrado."));

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
                throw new IllegalArgumentException("Ya existe un alumno con el email: " + dtoParcial.email());
            }
            alumno.setEmail(dtoParcial.email());
        }
        
        if (dtoParcial.dni() != null) {
            // Verificar que no exista otro alumno con ese DNI
            if (!alumno.getDni().equals(dtoParcial.dni()) && 
                repositorioAlumno.existsByDni(dtoParcial.dni())) {
                throw new IllegalArgumentException("Ya existe un alumno con el DNI: " + dtoParcial.dni());
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
        Alumno alumno = repositorioAlumno.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con ID " + id + " no encontrado."));
        
        alumno.setMatriculado(matriculado);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno habilitarDeshabilitarAlumno(Long id, boolean habilitar) {
        Alumno alumno = repositorioAlumno.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con ID " + id + " no encontrado."));
        
        alumno.setEnabled(habilitar);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno borrarAlumnoPorId(Long id) {
        Alumno alumno = repositorioAlumno.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alumno con ID " + id + " no encontrado."));
        
        repositorioAlumno.deleteById(id);
        return new DTOAlumno(alumno);
    }

    // Métodos de estadísticas útiles para administradores
    public long contarAlumnosMatriculados() {
        return repositorioAlumno.countByMatriculado(true);
    }

    public long contarAlumnosNoMatriculados() {
        return repositorioAlumno.countByMatriculado(false);
    }

    public long contarTotalAlumnos() {
        return repositorioAlumno.count();
    }

    // Métodos con paginación
    
    /**
     * Obtiene todos los alumnos con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los alumnos y metadatos de paginación
     */
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
        
        return new DTORespuestaPaginada<>(pageDTOs);
    }
    
    /**
     * Busca alumnos por parámetros con paginación
     */
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
        
        // Si todos los parámetros son nulos, usar findAllPaged
        if (parametros.nombre() == null && parametros.apellidos() == null && 
            parametros.dni() == null && parametros.email() == null && parametros.matriculado() == null) {
            pageAlumnos = repositorioAlumno.findAllPaged(pageable);
        } else {
            pageAlumnos = repositorioAlumno.findByFiltrosPaged(
                parametros.nombre(), 
                parametros.apellidos(),
                parametros.dni(),
                parametros.email(),
                parametros.matriculado(),
                pageable
            );
        }
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return new DTORespuestaPaginada<>(pageDTOs);
    }
    
    /**
     * Obtiene alumnos matriculados/no matriculados con paginación
     */
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
        
        return new DTORespuestaPaginada<>(pageDTOs);
    }
}
