package app.servicios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Ejercicio;
import app.entidades.Clase;
import app.repositorios.RepositorioEjercicio;
import app.repositorios.RepositorioClase;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEjercicio {

    private final RepositorioEjercicio repositorioEjercicio;
    private final RepositorioClase repositorioClase;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorId(Long id) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerTodosLosEjercicios() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findAll().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorNombre(String nombre) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByNameContainingIgnoreCase(nombre).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorEnunciado(String enunciado) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByStatementContainingIgnoreCase(enunciado).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorClase(String claseId) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByClassId(claseId).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByStartDateBetween(fechaInicio, fechaFin).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosActivos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findEjerciciosEnPlazo(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosVencidos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findEjerciciosVencidos(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosFuturos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findEjerciciosFuturos(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosProximosAVencer(int horasLimite) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        LocalDateTime limite = LocalDateTime.now().plusHours(horasLimite);
        return repositorioEjercicio.findEjerciciosProximosAVencer(limite).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosConEntregas() {
        // Security check: Only ADMIN and PROFESOR can see exercises with deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver ejercicios con entregas");
        }
        
        return repositorioEjercicio.findEjerciciosConEntregas().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosSinEntregas() {
        // Security check: Only ADMIN and PROFESOR can see exercises without deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver ejercicios sin entregas");
        }
        
        return repositorioEjercicio.findEjerciciosSinEntregas().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    public DTOEjercicio crearEjercicio(String nombre, String enunciado, LocalDateTime fechaInicioPlazo, 
                                      LocalDateTime fechaFinalPlazo, String claseId) {
        // Security check: Only ADMIN and PROFESOR can create exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear ejercicios");
        }
        
        // Validate input
        if (nombre == null || nombre.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del ejercicio no puede estar vacío");
        }
        
        if (enunciado == null || enunciado.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El enunciado del ejercicio no puede estar vacío");
        }
        
        if (fechaInicioPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha de inicio del plazo no puede estar vacía");
        }
        
        if (fechaFinalPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede estar vacía");
        }
        
        if (claseId == null || claseId.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El ID de la clase no puede estar vacío");
        }
        
        // Validate that claseId is a valid number
        try {
            Long.parseLong(claseId.trim());
        } catch (NumberFormatException e) {
            ExceptionUtils.throwValidationError("El ID de la clase debe ser un número válido: " + claseId);
        }

        // Validate date logic
        if (fechaFinalPlazo.isBefore(fechaInicioPlazo)) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }

        // Check for duplicates
        if (repositorioEjercicio.findByName(nombre).isPresent()) {
            ExceptionUtils.throwValidationError("Ya existe un ejercicio con el nombre: " + nombre);
        }

        // Create exercise
        Ejercicio ejercicio = new Ejercicio(
            nombre.trim(),
            enunciado.trim(),
            fechaInicioPlazo,
            fechaFinalPlazo,
            claseId.trim()
        );

        Ejercicio ejercicioGuardado = repositorioEjercicio.save(ejercicio);
        
        // Automatically add the exercise to the class's exercise list
        Long claseIdLong = Long.parseLong(claseId.trim());
        Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        clase.agregarEjercicio(ejercicioGuardado.getId().toString());
        repositorioClase.save(clase);
        
        return new DTOEjercicio(ejercicioGuardado);
    }

    public DTOEjercicio actualizarEjercicio(Long id, String nombre, String enunciado, 
                                           LocalDateTime fechaInicioPlazo, LocalDateTime fechaFinalPlazo) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can update exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para actualizar ejercicios");
        }

        // Validate input
        if (nombre == null || nombre.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del ejercicio no puede estar vacío");
        }
        
        if (enunciado == null || enunciado.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El enunciado del ejercicio no puede estar vacío");
        }
        
        if (fechaInicioPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha de inicio del plazo no puede estar vacía");
        }
        
        if (fechaFinalPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede estar vacía");
        }

        // Validate date logic
        if (fechaFinalPlazo.isBefore(fechaInicioPlazo)) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }

        // Check for duplicates (excluding current exercise)
        repositorioEjercicio.findByName(nombre).ifPresent(existingEjercicio -> {
            if (!existingEjercicio.getId().equals(id)) {
                ExceptionUtils.throwValidationError("Ya existe un ejercicio con el nombre: " + nombre);
            }
        });

        // Update exercise
        ejercicio.setName(nombre.trim());
        ejercicio.setStatement(enunciado.trim());
        ejercicio.setStartDate(fechaInicioPlazo);
        ejercicio.setEndDate(fechaFinalPlazo);

        Ejercicio ejercicioActualizado = repositorioEjercicio.save(ejercicio);
        return new DTOEjercicio(ejercicioActualizado);
    }

    public boolean borrarEjercicioPorId(Long id) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can delete exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar ejercicios");
        }

        // Remove the exercise from the class's exercise list before deleting
        String claseId = ejercicio.getClassId();
        Long claseIdLong = Long.parseLong(claseId);
        Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId + " for exercise " + ejercicio.getId());
        
        clase.removerEjercicio(ejercicio.getId().toString());
        repositorioClase.save(clase);

        repositorioEjercicio.delete(ejercicio);
        return true;
    }

    public boolean borrarEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can delete exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar ejercicios");
        }

        repositorioEjercicio.delete(ejercicio);
        return true;
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOEjercicio> obtenerEjerciciosPaginados(
            String q,
            String name,
            String statement,
            String classId,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }

        // Validate pagination parameters
        if (page < 0) {
            ExceptionUtils.throwValidationError("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            ExceptionUtils.throwValidationError("El tamaño de página debe estar entre 1 y 100");
        }

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build query based on filters using flexible approach
        Page<Ejercicio> ejercicioPage;
        
        // Prepare filter parameters
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String enunciadoFilter = (statement != null && !statement.trim().isEmpty()) ? statement.trim() : null;
        String classIdFilter = (classId != null && !classId.trim().isEmpty()) ? classId.trim() : null;
        String statusFilter = (status != null && !status.trim().isEmpty()) ? status.toUpperCase() : null;
        LocalDateTime now = LocalDateTime.now();
        

        
        // Use flexible query that handles all combinations
        ejercicioPage = repositorioEjercicio.findByGeneralAndSpecificFilters(
            searchTerm, nombreFilter, enunciadoFilter, classIdFilter, statusFilter, now, pageable);

        // Convert to DTOs
        List<DTOEjercicio> ejercicios = ejercicioPage.getContent().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<>(
            ejercicios,
            ejercicioPage.getNumber(),
            ejercicioPage.getSize(),
            ejercicioPage.getTotalElements(),
            ejercicioPage.getTotalPages(),
            ejercicioPage.isFirst(),
            ejercicioPage.isLast(),
            ejercicioPage.hasContent(),
            sortBy,
            sortDirection
        );
    }

    @Transactional(readOnly = true)
    public long contarEjercicios() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        return repositorioEjercicio.count();
    }

    @Transactional(readOnly = true)
    public long contarEjerciciosPorClase(String claseId) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        return repositorioEjercicio.countByClaseId(claseId);
    }

    @Transactional(readOnly = true)
    public int contarEntregasPorEjercicio(Long ejercicioId) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        return repositorioEjercicio.countEntregasByEjercicioId(ejercicioId);
    }
}
