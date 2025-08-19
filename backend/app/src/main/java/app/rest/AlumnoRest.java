package app.rest;

import app.dtos.DTOAlumno;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.servicios.ServicioAlumno;
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

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
public class AlumnoRest {

    private final ServicioAlumno servicioAlumno;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnos(
            @RequestParam(required = false) @Size(max = 100) String nombre,
            @RequestParam(required = false) @Size(max = 100) String apellidos,
            @RequestParam(required = false) @Size(max = 20) String dni,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean matriculado) {
        
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
            nombre, apellidos, dni, email, matriculado);
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.buscarAlumnosPorParametros(parametros);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuario}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or (hasRole('ALUMNO') and #usuario == authentication.principal.username)")
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorUsuario(
            @PathVariable @Size(max = 50) String usuario) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorUsuario(usuario);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorEmail(
            @PathVariable String email) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorEmail(email);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorDni(
            @PathVariable @Size(max = 20) String dni) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorDni(dni);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    @GetMapping("/matriculados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnosMatriculados() {
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.obtenerAlumnosPorMatriculado(true);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @GetMapping("/no-matriculados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DTOAlumno>> obtenerAlumnosNoMatriculados() {
        
        List<DTOAlumno> alumnosDTO = servicioAlumno.obtenerAlumnosPorMatriculado(false);
        return new ResponseEntity<>(alumnosDTO, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> crearAlumno(
            @Valid @RequestBody DTOPeticionRegistroAlumno peticion) {
        
        DTOAlumno dtoAlumnoNuevo = servicioAlumno.crearAlumno(peticion);
        return new ResponseEntity<>(dtoAlumnoNuevo, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ALUMNO') and #id == authentication.principal.id)")
    public ResponseEntity<DTOAlumno> actualizarAlumno(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id, 
            @Valid @RequestBody DTOAlumno dtoParcial) {
        
        DTOAlumno dtoActualizado = servicioAlumno.actualizarAlumno(id, dtoParcial);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @PatchMapping("/{id}/matricula")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> cambiarEstadoMatricula(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean matriculado = request.get("matriculado");
        if (matriculado == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOAlumno dtoActualizado = servicioAlumno.cambiarEstadoMatricula(id, matriculado);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @PatchMapping("/{id}/habilitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> habilitarDeshabilitarAlumno(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean habilitar = request.get("habilitar");
        if (habilitar == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOAlumno dtoActualizado = servicioAlumno.habilitarDeshabilitarAlumno(id, habilitar);
        return new ResponseEntity<>(dtoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTOAlumno> borrarAlumnoPorId(
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.borrarAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    // Endpoints de estadísticas para administradores
    @GetMapping("/estadisticas/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerTotalAlumnos() {
        long total = servicioAlumno.contarTotalAlumnos();
        return new ResponseEntity<>(Map.of("total", total), HttpStatus.OK);
    }

    @GetMapping("/estadisticas/matriculas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasMatriculas() {
        Map<String, Long> estadisticas = Map.of(
            "matriculados", servicioAlumno.contarAlumnosMatriculados(),
            "no_matriculados", servicioAlumno.contarAlumnosNoMatriculados()
        );
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }
}
