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
import app.repositorios.RepositorioEjercicio;
import app.util.ExceptionUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEjercicio {

    private final RepositorioEjercicio repositorioEjercicio;

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorId(Long id) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerTodosLosEjercicios() {
        return repositorioEjercicio.findAll().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorNombre(String nombre) {
        return repositorioEjercicio.findByNameContainingIgnoreCase(nombre).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorEnunciado(String enunciado) {
        return repositorioEjercicio.findByStatementContainingIgnoreCase(enunciado).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorClase(String claseId) {
        return repositorioEjercicio.findByClassId(claseId).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return repositorioEjercicio.findByStartDateBetween(fechaInicio, fechaFin).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosActivos() {
        return repositorioEjercicio.findEjerciciosEnPlazo(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosVencidos() {
        return repositorioEjercicio.findEjerciciosVencidos(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosFuturos() {
        return repositorioEjercicio.findEjerciciosFuturos(LocalDateTime.now()).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosProximosAVencer(int horasLimite) {
        LocalDateTime limite = LocalDateTime.now().plusHours(horasLimite);
        return repositorioEjercicio.findEjerciciosProximosAVencer(limite).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosConEntregas() {
        return repositorioEjercicio.findEjerciciosConEntregas().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosSinEntregas() {
        return repositorioEjercicio.findEjerciciosSinEntregas().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    public DTOEjercicio crearEjercicio(String nombre, String enunciado, LocalDateTime fechaInicioPlazo, 
                                      LocalDateTime fechaFinalPlazo, String claseId) {
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
        return new DTOEjercicio(ejercicioGuardado);
    }

    public DTOEjercicio actualizarEjercicio(Long id, String nombre, String enunciado, 
                                           LocalDateTime fechaInicioPlazo, LocalDateTime fechaFinalPlazo) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);

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

        repositorioEjercicio.delete(ejercicio);
        return true;
    }

    public boolean borrarEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);

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

        // Build query based on filters
        Page<Ejercicio> ejercicioPage;
        
        if (q != null && !q.trim().isEmpty()) {
            // General search
            ejercicioPage = repositorioEjercicio.findByNameContainingIgnoreCaseOrStatementContainingIgnoreCase(
                q.trim(), q.trim(), pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            // Name filter
            ejercicioPage = repositorioEjercicio.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (statement != null && !statement.trim().isEmpty()) {
            // Statement filter
            ejercicioPage = repositorioEjercicio.findByStatementContainingIgnoreCase(statement.trim(), pageable);
        } else if (classId != null && !classId.trim().isEmpty()) {
            // Class filter
            ejercicioPage = repositorioEjercicio.findByClassId(classId.trim(), pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            // Status filter
            LocalDateTime ahora = LocalDateTime.now();
            switch (status.toUpperCase()) {
                case "ACTIVE":
                    ejercicioPage = repositorioEjercicio.findEjerciciosEnPlazo(ahora, pageable);
                    break;
                case "EXPIRED":
                    ejercicioPage = repositorioEjercicio.findEjerciciosVencidos(ahora, pageable);
                    break;
                case "FUTURE":
                    ejercicioPage = repositorioEjercicio.findEjerciciosFuturos(ahora, pageable);
                    break;
                case "WITH_DELIVERIES":
                    ejercicioPage = repositorioEjercicio.findEjerciciosConEntregas(pageable);
                    break;
                case "WITHOUT_DELIVERIES":
                    ejercicioPage = repositorioEjercicio.findEjerciciosSinEntregas(pageable);
                    break;
                default:
                    ejercicioPage = repositorioEjercicio.findAll(pageable);
            }
        } else {
            // No filters
            ejercicioPage = repositorioEjercicio.findAll(pageable);
        }

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
        return repositorioEjercicio.count();
    }

    @Transactional(readOnly = true)
    public long contarEjerciciosPorClase(String claseId) {
        return repositorioEjercicio.countByClaseId(claseId);
    }

    @Transactional(readOnly = true)
    public int contarEntregasPorEjercicio(Long ejercicioId) {
        return repositorioEjercicio.countEntregasByEjercicioId(ejercicioId);
    }
}
