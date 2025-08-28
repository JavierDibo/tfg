package app.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.EntregaEjercicio;
import app.entidades.Ejercicio;
import app.entidades.enums.EEstadoEjercicio;
import app.repositorios.RepositorioEntregaEjercicio;
import app.repositorios.RepositorioEjercicio;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import app.entidades.Alumno;
import app.repositorios.RepositorioAlumno;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEntregaEjercicio {

    private final RepositorioEntregaEjercicio repositorioEntregaEjercicio;
    private final RepositorioEjercicio repositorioEjercicio;
    private final RepositorioAlumno repositorioAlumno;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaPorId(Long id) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can see it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver esta entrega");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerTodasLasEntregas() {
        // Security check: Only ADMIN and PROFESOR can see all deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver todas las entregas");
        }
        
        return repositorioEntregaEjercicio.findAll().stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see student's deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's deliveries
            return repositorioEntregaEjercicio.findByAlumnoEntreganteId(Long.parseLong(alumnoId)).stream()
                    .map(DTOEntregaEjercicio::new)
                    .collect(Collectors.toList());
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver las entregas de otros alumnos");
            }
            return repositorioEntregaEjercicio.findByAlumnoEntreganteId(Long.parseLong(alumnoId)).stream()
                    .map(DTOEntregaEjercicio::new)
                    .collect(Collectors.toList());
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can see all deliveries for an exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver todas las entregas de un ejercicio");
        }
        
        return repositorioEntregaEjercicio.findByEjercicioId(Long.parseLong(ejercicioId)).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaPorAlumnoYEjercicio(String alumnoId, String ejercicioId) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio
                .findByAlumnoEntreganteIdAndEjercicioId(Long.parseLong(alumnoId), Long.parseLong(ejercicioId)).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "alumno y ejercicio", alumnoId + " - " + ejercicioId);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see the delivery
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any delivery
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver las entregas de otros alumnos");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorEstado(EEstadoEjercicio estado) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by status
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por estado");
        }
        
        return repositorioEntregaEjercicio.findByEstado(estado).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by date range
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por rango de fechas");
        }
        
        return repositorioEntregaEjercicio.findByFechaEntregaBetween(fechaInicio, fechaFin).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorRangoNotas(BigDecimal notaMin, BigDecimal notaMax) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by grade range
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por rango de notas");
        }
        
        return repositorioEntregaEjercicio.findByNotaBetween(notaMin, notaMax).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasCalificadas() {
        // Security check: Only ADMIN and PROFESOR can see graded deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas calificadas");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.CALIFICADO);
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPendientes() {
        // Security check: Only ADMIN and PROFESOR can see pending deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas pendientes");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasEntregadas() {
        // Security check: Only ADMIN and PROFESOR can see submitted deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas entregadas");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.ENTREGADO);
    }

    public DTOEntregaEjercicio crearEntrega(Long alumnoId, Long ejercicioId, List<String> archivos) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can create deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can create deliveries for any student
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only create deliveries for themselves
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No puedes crear entregas para otros alumnos");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear entregas");
        }
        
        // Validate input
        if (alumnoId == null) {
            ExceptionUtils.throwValidationError("El ID del alumno no puede estar vacío");
        }
        
        if (ejercicioId == null) {
            ExceptionUtils.throwValidationError("El ID del ejercicio no puede estar vacío");
        }

        // Check if exercise exists
        Ejercicio ejercicio = repositorioEjercicio.findById(ejercicioId).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", ejercicioId);

        // Check if student exists
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);

        // Check if exercise is still active
        if (ejercicio.haVencido()) {
            ExceptionUtils.throwValidationError("No se puede entregar un ejercicio que ya ha vencido");
        }

        // Check if student already has a delivery for this exercise
        repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(alumnoId, ejercicioId)
                .ifPresent(existing -> {
                    ExceptionUtils.throwValidationError("El alumno ya tiene una entrega para este ejercicio");
                });

        // Create delivery with JPA relationships
        EntregaEjercicio entrega = new EntregaEjercicio(alumno, ejercicio);
        
        // Add files if provided
        if (archivos != null && !archivos.isEmpty()) {
            archivos.forEach(entrega::agregarArchivo);
        }

        EntregaEjercicio entregaGuardada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaGuardada);
    }

    public DTOEntregaEjercicio calificarEntrega(Long entregaId, BigDecimal nota) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only teachers and admins can grade deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
        }

        // Validate grade
        if (nota == null) {
            ExceptionUtils.throwValidationError("La nota no puede estar vacía");
        }
        
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
        }

        // Grade the delivery
        entrega.calificar(nota);

        EntregaEjercicio entregaCalificada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaCalificada);
    }
    
    public DTOEntregaEjercicio calificarEntrega(Long entregaId, BigDecimal nota, String comentarios) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only teachers and admins can grade deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
        }

        // Validate grade
        if (nota == null) {
            ExceptionUtils.throwValidationError("La nota no puede estar vacía");
        }
        
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
        }

        // Grade the delivery with comments
        entrega.calificar(nota, comentarios);

        EntregaEjercicio entregaCalificada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaCalificada);
    }

    public DTOEntregaEjercicio actualizarEntrega(Long entregaId, List<String> archivos, BigDecimal nota) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can update it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta entrega");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar entregas");
        }

        // Update files if provided
        if (archivos != null) {
            // Check if delivery can be modified (only pending deliveries can have files updated)
            if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE) {
                ExceptionUtils.throwValidationError("Solo se pueden modificar archivos de entregas pendientes");
            }
            entrega.getArchivosEntregados().clear();
            archivos.forEach(entrega::agregarArchivo);
        }

        // Update grade if provided
        if (nota != null) {
            // Security check: Only teachers and admins can grade deliveries
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
            }
            
            // Validate grade
            if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
                ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
            }
            
            // Grade the delivery
            entrega.calificar(nota);
        }

        EntregaEjercicio entregaActualizada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaActualizada);
    }
    
    public DTOEntregaEjercicio actualizarEntrega(Long entregaId, List<String> archivos, BigDecimal nota, String comentarios) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can update it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta entrega");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar entregas");
        }

        // Update files if provided
        if (archivos != null) {
            // Check if delivery can be modified (only pending deliveries can have files updated)
            if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE) {
                ExceptionUtils.throwValidationError("Solo se pueden modificar archivos de entregas pendientes");
            }
            entrega.getArchivosEntregados().clear();
            archivos.forEach(entrega::agregarArchivo);
        }

        // Update grade and comments if provided
        if (nota != null) {
            // Security check: Only teachers and admins can grade deliveries
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
            }
            
            // Validate grade
            if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
                ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
            }
            
            // Grade the delivery with comments
            entrega.calificar(nota, comentarios);
        } else if (comentarios != null) {
            // Security check: Only teachers and admins can add comments
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden agregar comentarios");
            }
            
            // Add comments only
            entrega.agregarComentarios(comentarios);
        }

        EntregaEjercicio entregaActualizada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaActualizada);
    }

    public boolean borrarEntregaPorId(Long id) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);

        // Security check: Only ADMIN and PROFESOR can delete deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar entregas");
        }

        repositorioEntregaEjercicio.delete(entrega);
        return true;
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOEntregaEjercicio> obtenerEntregasPaginadas(
            String alumnoId,
            String ejercicioId,
            String estado,
            BigDecimal notaMin,
            BigDecimal notaMax,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ADMIN, PROFESOR, or students viewing their own deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (alumnoId == null || !alumnoId.equals(currentUserId.toString())) {
                // Force filter to current student's deliveries only
                alumnoId = currentUserId.toString();
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
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
        Page<EntregaEjercicio> entregaPage;
        
        // Prepare filter parameters
        Long alumnoIdFilter = (alumnoId != null && !alumnoId.trim().isEmpty()) ? Long.parseLong(alumnoId.trim()) : null;
        Long ejercicioIdFilter = (ejercicioId != null && !ejercicioId.trim().isEmpty()) ? Long.parseLong(ejercicioId.trim()) : null;
        String estadoFilter = (estado != null && !estado.trim().isEmpty()) ? estado.toUpperCase() : null;
        BigDecimal notaMinFilter = notaMin;
        BigDecimal notaMaxFilter = notaMax;
        
        // Use flexible query that handles all combinations
        entregaPage = repositorioEntregaEjercicio.findByFiltrosFlexibles(
            alumnoIdFilter, ejercicioIdFilter, estadoFilter, notaMinFilter, notaMaxFilter, pageable);

        // Convert to DTOs
        List<DTOEntregaEjercicio> entregas = entregaPage.getContent().stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<>(
            entregas,
            entregaPage.getNumber(),
            entregaPage.getSize(),
            entregaPage.getTotalElements(),
            entregaPage.getTotalPages(),
            entregaPage.isFirst(),
            entregaPage.isLast(),
            entregaPage.hasContent(),
            sortBy,
            sortDirection
        );
    }

    @Transactional(readOnly = true)
    public long contarEntregas() {
        // Security check: Only ADMIN and PROFESOR can count all deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar todas las entregas");
        }
        return repositorioEntregaEjercicio.count();
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can count student's deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can count any student's deliveries
            return repositorioEntregaEjercicio.countByAlumnoEntreganteId(Long.parseLong(alumnoId));
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only count their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para contar las entregas de otros alumnos");
            }
            return repositorioEntregaEjercicio.countByAlumnoEntreganteId(Long.parseLong(alumnoId));
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas");
            return 0; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can count deliveries by exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas por ejercicio");
        }
        return repositorioEntregaEjercicio.countByEjercicioId(Long.parseLong(ejercicioId));
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorEstado(EEstadoEjercicio estado) {
        // Security check: Only ADMIN and PROFESOR can count deliveries by status
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas por estado");
        }
        return repositorioEntregaEjercicio.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerNotaPromedioPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can see average grades by exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver promedios por ejercicio");
        }
        return repositorioEntregaEjercicio.findAverageGradeByEjercicioId(Long.parseLong(ejercicioId));
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerNotaPromedioPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see student's average grade
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's average grade
            return repositorioEntregaEjercicio.findAverageGradeByAlumnoId(Long.parseLong(alumnoId));
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own average grade
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver el promedio de otros alumnos");
            }
            return repositorioEntregaEjercicio.findAverageGradeByAlumnoId(Long.parseLong(alumnoId));
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver promedios");
            return BigDecimal.ZERO; // This line will never be reached due to the exception above
        }
    }
}
