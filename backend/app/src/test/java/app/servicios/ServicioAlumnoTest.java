package app.servicios;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioAlumno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioAlumno")
class ServicioAlumnoTest {

    @Mock
    private RepositorioAlumno repositorioAlumno;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ServicioAlumno servicioAlumno;

    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;
    private DTOAlumno dtoAlumno1;
    private DTOAlumno dtoAlumno2;
    private DTOAlumno dtoAlumno3;

    @BeforeEach
    void setUp() {
        alumno1 = new Alumno("alumno1", "password1", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "123456789");
        alumno1.setId(1L);
        alumno1.setFechaInscripcion(LocalDateTime.now().minusDays(30));
        alumno1.setMatriculado(true);

        alumno2 = new Alumno("alumno2", "password2", "María", "García", "87654321Y", "maria@ejemplo.com", "987654321");
        alumno2.setId(2L);
        alumno2.setFechaInscripcion(LocalDateTime.now().minusDays(15));
        alumno2.setMatriculado(false);

        alumno3 = new Alumno("alumno3", "password3", "Carlos", "López", "11223344X", "carlos@ejemplo.com", "555666777");
        alumno3.setId(3L);
        alumno3.setFechaInscripcion(LocalDateTime.now());
        alumno3.setMatriculado(true);

        dtoAlumno1 = new DTOAlumno(alumno1);
        dtoAlumno2 = new DTOAlumno(alumno2);
        dtoAlumno3 = new DTOAlumno(alumno3);
    }

    @Test
    @DisplayName("obtenerAlumnos debe retornar lista de DTOs")
    void testObtenerAlumnos() {
        List<Alumno> alumnos = Arrays.asList(alumno1, alumno2, alumno3);
        when(repositorioAlumno.findAllOrderedById()).thenReturn(alumnos);

        List<DTOAlumno> resultado = servicioAlumno.obtenerAlumnos();

        assertEquals(3, resultado.size());
        assertEquals(dtoAlumno1, resultado.get(0));
        assertEquals(dtoAlumno2, resultado.get(1));
        assertEquals(dtoAlumno3, resultado.get(2));
        verify(repositorioAlumno).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerAlumnoPorId debe retornar DTO cuando existe")
    void testObtenerAlumnoPorIdExiste() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorId(1L);

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findById(1L);
    }

    @Test
    @DisplayName("obtenerAlumnoPorId debe lanzar excepción cuando no existe")
    void testObtenerAlumnoPorIdNoExiste() {
        when(repositorioAlumno.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioAlumno.obtenerAlumnoPorId(999L);
        });
        verify(repositorioAlumno).findById(999L);
    }

    @Test
    @DisplayName("obtenerAlumnoPorEmail debe retornar DTO cuando existe")
    void testObtenerAlumnoPorEmailExiste() {
        when(repositorioAlumno.findByEmail("juan@ejemplo.com")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorEmail("juan@ejemplo.com");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByEmail("juan@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerAlumnoPorEmail debe lanzar excepción cuando no existe")
    void testObtenerAlumnoPorEmailNoExiste() {
        when(repositorioAlumno.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioAlumno.obtenerAlumnoPorEmail("inexistente@ejemplo.com");
        });
        verify(repositorioAlumno).findByEmail("inexistente@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerAlumnoPorUsuario debe retornar DTO cuando existe")
    void testObtenerAlumnoPorUsuarioExiste() {
        when(repositorioAlumno.findByUsuario("alumno1")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorUsuario("alumno1");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByUsuario("alumno1");
    }

    @Test
    @DisplayName("obtenerAlumnoPorDni debe retornar DTO cuando existe")
    void testObtenerAlumnoPorDniExiste() {
        when(repositorioAlumno.findByDni("12345678Z")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorDni("12345678Z");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByDni("12345678Z");
    }

    @Test
    @DisplayName("buscarAlumnosPorParametros debe retornar todos cuando no hay filtros")
    void testBuscarAlumnosPorParametrosSinFiltros() {
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(null, null, null, null, null);
        List<Alumno> todosLosAlumnos = Arrays.asList(alumno1, alumno2, alumno3);
        when(repositorioAlumno.findAllOrderedById()).thenReturn(todosLosAlumnos);

        List<DTOAlumno> resultado = servicioAlumno.buscarAlumnosPorParametros(parametros);

        assertEquals(3, resultado.size());
        verify(repositorioAlumno).findAllOrderedById();
        verify(repositorioAlumno, never()).findByFiltros(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("buscarAlumnosPorParametros debe usar filtros cuando se proporcionan")
    void testBuscarAlumnosPorParametrosConFiltros() {
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno("Juan", null, null, null, null);
        List<Alumno> alumnosFiltrados = Arrays.asList(alumno1);
        when(repositorioAlumno.findByFiltros("Juan", null, null, null, null)).thenReturn(alumnosFiltrados);

        List<DTOAlumno> resultado = servicioAlumno.buscarAlumnosPorParametros(parametros);

        assertEquals(1, resultado.size());
        assertEquals(dtoAlumno1, resultado.get(0));
        verify(repositorioAlumno).findByFiltros("Juan", null, null, null, null);
    }

    @Test
    @DisplayName("obtenerAlumnosPorMatriculado debe retornar alumnos matriculados")
    void testObtenerAlumnosPorMatriculado() {
        List<Alumno> alumnosMatriculados = Arrays.asList(alumno1, alumno3);
        when(repositorioAlumno.findByMatriculado(true)).thenReturn(alumnosMatriculados);

        List<DTOAlumno> resultado = servicioAlumno.obtenerAlumnosPorMatriculado(true);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(DTOAlumno::matriculado));
        verify(repositorioAlumno).findByMatriculado(true);
    }

    @Test
    @DisplayName("crearAlumno debe crear alumno correctamente")
    void testCrearAlumno() {
        DTOPeticionRegistroAlumno peticion = new DTOPeticionRegistroAlumno(
            "nuevo", "password123", "Nuevo", "Alumno", "99999999A", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsuario("nuevo")).thenReturn(false);
        when(repositorioAlumno.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
        when(repositorioAlumno.existsByDni("99999999A")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(repositorioAlumno.save(any(Alumno.class))).thenReturn(alumno1);

        DTOAlumno resultado = servicioAlumno.crearAlumno(peticion);

        assertNotNull(resultado);
        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).existsByUsuario("nuevo");
        verify(repositorioAlumno).existsByEmail("nuevo@ejemplo.com");
        verify(repositorioAlumno).existsByDni("99999999A");
        verify(passwordEncoder).encode("password123");
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("crearAlumno debe lanzar excepción si usuario ya existe")
    void testCrearAlumnoUsuarioExiste() {
        DTOPeticionRegistroAlumno peticion = new DTOPeticionRegistroAlumno(
            "existente", "password123", "Nuevo", "Alumno", "99999999A", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsuario("existente")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            servicioAlumno.crearAlumno(peticion);
        });
        verify(repositorioAlumno).existsByUsuario("existente");
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("crearAlumno debe lanzar excepción si email ya existe")
    void testCrearAlumnoEmailExiste() {
        DTOPeticionRegistroAlumno peticion = new DTOPeticionRegistroAlumno(
            "nuevo", "password123", "Nuevo", "Alumno", "99999999A", "existente@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsuario("nuevo")).thenReturn(false);
        when(repositorioAlumno.existsByEmail("existente@ejemplo.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            servicioAlumno.crearAlumno(peticion);
        });
        verify(repositorioAlumno).existsByUsuario("nuevo");
        verify(repositorioAlumno).existsByEmail("existente@ejemplo.com");
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("actualizarAlumno debe actualizar campos no nulos")
    void testActualizarAlumno() {
        DTOActualizacionAlumno dtoParcial = new DTOActualizacionAlumno("Nuevo Nombre", null, null, null, null);
        
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.actualizarAlumno(1L, dtoParcial);

        assertNotNull(resultado);
        assertEquals("Nuevo Nombre", resultado.nombre());
        assertEquals(alumno1.getApellidos(), resultado.apellidos());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("actualizarAlumno debe lanzar excepción si alumno no existe")
    void testActualizarAlumnoNoExiste() {
        DTOActualizacionAlumno dtoParcial = new DTOActualizacionAlumno("Nuevo Nombre", null, null, null, null);
        
        when(repositorioAlumno.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioAlumno.actualizarAlumno(999L, dtoParcial);
        });
        verify(repositorioAlumno).findById(999L);
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstadoMatricula debe cambiar estado correctamente")
    void testCambiarEstadoMatricula() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.cambiarEstadoMatricula(1L, false);

        assertNotNull(resultado);
        assertFalse(resultado.matriculado());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("habilitarDeshabilitarAlumno debe cambiar estado correctamente")
    void testHabilitarDeshabilitarAlumno() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.habilitarDeshabilitarAlumno(1L, false);

        assertNotNull(resultado);
        assertFalse(resultado.enabled());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("borrarAlumnoPorId debe borrar alumno correctamente")
    void testBorrarAlumnoPorId() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.borrarAlumnoPorId(1L);

        assertNotNull(resultado);
        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).deleteById(1L);
    }

    @Test
    @DisplayName("contarAlumnosMatriculados debe retornar conteo correcto")
    void testContarAlumnosMatriculados() {
        when(repositorioAlumno.countByMatriculado(true)).thenReturn(2L);

        long resultado = servicioAlumno.contarAlumnosMatriculados();

        assertEquals(2L, resultado);
        verify(repositorioAlumno).countByMatriculado(true);
    }

    @Test
    @DisplayName("contarAlumnosNoMatriculados debe retornar conteo correcto")
    void testContarAlumnosNoMatriculados() {
        when(repositorioAlumno.countByMatriculado(false)).thenReturn(1L);

        long resultado = servicioAlumno.contarAlumnosNoMatriculados();

        assertEquals(1L, resultado);
        verify(repositorioAlumno).countByMatriculado(false);
    }

    @Test
    @DisplayName("contarTotalAlumnos debe retornar conteo correcto")
    void testContarTotalAlumnos() {
        when(repositorioAlumno.count()).thenReturn(3L);

        long resultado = servicioAlumno.contarTotalAlumnos();

        assertEquals(3L, resultado);
        verify(repositorioAlumno).count();
    }

    @Test
    @DisplayName("obtenerAlumnosPaginados debe retornar respuesta paginada")
    void testObtenerAlumnosPaginados() {
        List<Alumno> alumnos = Arrays.asList(alumno1, alumno2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnos, pageable, 3);
        when(repositorioAlumno.findAllPaged(any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.obtenerAlumnosPaginados(0, 2, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(2, resultado.contenido().size());
        assertEquals(3, resultado.totalElementos());
        assertEquals(0, resultado.numeroPagina());
        assertEquals(2, resultado.tamanoPagina());
        verify(repositorioAlumno).findAllPaged(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarAlumnosPorParametrosPaginados debe retornar respuesta paginada")
    void testBuscarAlumnosPorParametrosPaginados() {
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno("Juan", null, null, null, null);
        List<Alumno> alumnosFiltrados = Arrays.asList(alumno1);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnosFiltrados, pageable, 1);
        when(repositorioAlumno.findByFiltrosPaged(anyString(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.buscarAlumnosPorParametrosPaginados(parametros, 0, 1, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(1, resultado.contenido().size());
        assertEquals(1, resultado.totalElementos());
        verify(repositorioAlumno).findByFiltrosPaged(anyString(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerAlumnosPorMatriculadoPaginados debe retornar respuesta paginada")
    void testObtenerAlumnosPorMatriculadoPaginados() {
        List<Alumno> alumnosMatriculados = Arrays.asList(alumno1, alumno3);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnosMatriculados, pageable, 2);
        when(repositorioAlumno.findByMatriculadoPaged(any(Boolean.class), any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(true, 0, 2, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(2, resultado.contenido().size());
        assertEquals(2, resultado.totalElementos());
        verify(repositorioAlumno).findByMatriculadoPaged(any(Boolean.class), any(Pageable.class));
    }
}
