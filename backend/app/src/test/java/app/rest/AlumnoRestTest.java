package app.rest;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPerfilAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOProfesor;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para AlumnoRest")
class AlumnoRestTest {

    @Mock
    private ServicioAlumno servicioAlumno;

    @Mock
    private ServicioClase servicioClase;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AlumnoRest alumnoRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTOAlumno dtoAlumno1;
    private DTOAlumno dtoAlumno2;
    private DTORespuestaPaginada<DTOAlumno> respuestaPaginada;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alumnoRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Crear DTOs de prueba
        dtoAlumno1 = new DTOAlumno(
                1L, "alumno1", "Juan", "Pérez", "12345678Z", 
                "juan@ejemplo.com", "123456789", LocalDateTime.now(), true, 
                true, Arrays.asList("1"), Arrays.asList("1"), Arrays.asList("1"), 
                app.entidades.Usuario.Rol.ALUMNO
        );
        
        dtoAlumno2 = new DTOAlumno(
                2L, "alumno2", "María", "García", "87654321Y", 
                "maria@ejemplo.com", "987654321", LocalDateTime.now(), true, 
                false, Arrays.asList("1"), Arrays.asList("1"), Arrays.asList("1"), 
                app.entidades.Usuario.Rol.ALUMNO
        );

        respuestaPaginada = new DTORespuestaPaginada<DTOAlumno>(
                Arrays.asList(dtoAlumno1, dtoAlumno2),
                0, 20, 2L, 1, true, false, true, "id", "ASC"
        );
    }

    @Test
    @DisplayName("GET /api/alumnos/paged debe retornar alumnos paginados")
    void testObtenerAlumnosPaginados() throws Exception {
        when(servicioAlumno.buscarAlumnosPorParametrosPaginados(any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/alumnos/paged")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.contenido[0].id").value(1))
                .andExpect(jsonPath("$.contenido[1].id").value(2))
                .andExpect(jsonPath("$.totalElementos").value(2));

        verify(servicioAlumno).buscarAlumnosPorParametrosPaginados(any(), eq(0), eq(20), eq("id"), eq("ASC"));
    }

    @Test
    @DisplayName("GET /api/alumnos/paged con filtros debe usar parámetros de búsqueda")
    void testObtenerAlumnosPaginadosConFiltros() throws Exception {
        when(servicioAlumno.buscarAlumnosPorParametrosPaginados(any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/alumnos/paged")
                .param("nombre", "Juan")
                .param("matriculado", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).buscarAlumnosPorParametrosPaginados(any(), eq(0), eq(10), eq("id"), eq("ASC"));
    }

    @Test
    @DisplayName("GET /api/alumnos/disponibles debe retornar alumnos disponibles")
    void testObtenerAlumnosDisponibles() throws Exception {
        when(servicioAlumno.obtenerAlumnosDisponiblesPaginados(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/alumnos/disponibles")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido").isArray());

        verify(servicioAlumno).obtenerAlumnosDisponiblesPaginados(0, 50, "nombre", "ASC");
    }

    @Test
    @DisplayName("GET /api/alumnos debe retornar todos los alumnos (DEPRECATED)")
    void testObtenerAlumnos() throws Exception {
        List<DTOAlumno> alumnos = Arrays.asList(dtoAlumno1, dtoAlumno2);
        when(servicioAlumno.buscarAlumnosPorParametros(any())).thenReturn(alumnos);

        mockMvc.perform(get("/api/alumnos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(servicioAlumno).buscarAlumnosPorParametros(any());
    }

    @Test
    @DisplayName("GET /api/alumnos/{id} debe retornar alumno por ID")
    void testObtenerAlumnoPorId() throws Exception {
        when(servicioAlumno.obtenerAlumnoPorId(1L)).thenReturn(dtoAlumno1);

        mockMvc.perform(get("/api/alumnos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuario").value("alumno1"));

        verify(servicioAlumno).obtenerAlumnoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/alumnos/usuario/{usuario} debe retornar alumno por usuario")
    void testObtenerAlumnoPorUsuario() throws Exception {
        when(servicioAlumno.obtenerAlumnoPorUsuario("alumno1")).thenReturn(dtoAlumno1);

        mockMvc.perform(get("/api/alumnos/usuario/alumno1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuario").value("alumno1"));

        verify(servicioAlumno).obtenerAlumnoPorUsuario("alumno1");
    }

    @Test
    @DisplayName("GET /api/alumnos/email/{email} debe retornar alumno por email")
    void testObtenerAlumnoPorEmail() throws Exception {
        when(servicioAlumno.obtenerAlumnoPorEmail("juan@ejemplo.com")).thenReturn(dtoAlumno1);

        mockMvc.perform(get("/api/alumnos/email/juan@ejemplo.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("juan@ejemplo.com"));

        verify(servicioAlumno).obtenerAlumnoPorEmail("juan@ejemplo.com");
    }

    @Test
    @DisplayName("GET /api/alumnos/dni/{dni} debe retornar alumno por DNI")
    void testObtenerAlumnoPorDni() throws Exception {
        when(servicioAlumno.obtenerAlumnoPorDni("12345678Z")).thenReturn(dtoAlumno1);

        mockMvc.perform(get("/api/alumnos/dni/12345678Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dni").value("12345678Z"));

        verify(servicioAlumno).obtenerAlumnoPorDni("12345678Z");
    }

    @Test
    @DisplayName("GET /api/alumnos/matriculados/paged debe retornar alumnos matriculados")
    void testObtenerAlumnosMatriculadosPaginados() throws Exception {
        when(servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(anyBoolean(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/alumnos/matriculados/paged")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido").isArray());

        verify(servicioAlumno).obtenerAlumnosPorMatriculadoPaginados(true, 0, 20, "id", "ASC");
    }

    @Test
    @DisplayName("GET /api/alumnos/no-matriculados/paged debe retornar alumnos no matriculados")
    void testObtenerAlumnosNoMatriculadosPaginados() throws Exception {
        when(servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(anyBoolean(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/alumnos/no-matriculados/paged")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).obtenerAlumnosPorMatriculadoPaginados(false, 0, 20, "id", "ASC");
    }

    @Test
    @DisplayName("POST /api/alumnos debe crear alumno correctamente")
    void testCrearAlumno() throws Exception {
        DTOPeticionRegistroAlumno peticion = new DTOPeticionRegistroAlumno(
                "nuevo", "password123", "Nuevo", "Alumno", "12345678Z", "nuevo@ejemplo.com", "123456789"
        );

        when(servicioAlumno.crearAlumno(any(DTOPeticionRegistroAlumno.class))).thenReturn(dtoAlumno1);

        mockMvc.perform(post("/api/alumnos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuario").value("alumno1"));

        verify(servicioAlumno).crearAlumno(any(DTOPeticionRegistroAlumno.class));
    }

    @Test
    @DisplayName("PATCH /api/alumnos/{id} debe actualizar alumno")
    void testActualizarAlumno() throws Exception {
        DTOActualizacionAlumno actualizacion = new DTOActualizacionAlumno(
                "Nuevo Nombre", null, null, null, null
        );

        when(servicioAlumno.actualizarAlumno(eq(1L), any(DTOActualizacionAlumno.class))).thenReturn(dtoAlumno1);

        mockMvc.perform(patch("/api/alumnos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(servicioAlumno).actualizarAlumno(eq(1L), any(DTOActualizacionAlumno.class));
    }

    @Test
    @DisplayName("PATCH /api/alumnos/{id}/matricula debe cambiar estado de matrícula")
    void testCambiarEstadoMatricula() throws Exception {
        Map<String, Boolean> request = Map.of("matriculado", false);
        when(servicioAlumno.cambiarEstadoMatricula(1L, false)).thenReturn(dtoAlumno2);

        mockMvc.perform(patch("/api/alumnos/1/matricula")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).cambiarEstadoMatricula(1L, false);
    }

    @Test
    @DisplayName("PATCH /api/alumnos/{id}/matricula debe retornar 400 si falta campo matriculado")
    void testCambiarEstadoMatriculaSinCampo() throws Exception {
        Map<String, String> requestInvalido = Map.of("otroCampo", "valor");

        mockMvc.perform(patch("/api/alumnos/1/matricula")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(servicioAlumno, never()).cambiarEstadoMatricula(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("PATCH /api/alumnos/{id}/habilitar debe habilitar/deshabilitar alumno")
    void testHabilitarDeshabilitarAlumno() throws Exception {
        Map<String, Boolean> request = Map.of("habilitar", true);
        when(servicioAlumno.habilitarDeshabilitarAlumno(1L, true)).thenReturn(dtoAlumno1);

        mockMvc.perform(patch("/api/alumnos/1/habilitar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).habilitarDeshabilitarAlumno(1L, true);
    }

    @Test
    @DisplayName("DELETE /api/alumnos/{id} debe borrar alumno")
    void testBorrarAlumnoPorId() throws Exception {
        when(servicioAlumno.borrarAlumnoPorId(1L)).thenReturn(dtoAlumno1);

        mockMvc.perform(delete("/api/alumnos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(servicioAlumno).borrarAlumnoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/alumnos/estadisticas/total debe retornar total de alumnos")
    void testObtenerTotalAlumnos() throws Exception {
        when(servicioAlumno.contarTotalAlumnos()).thenReturn(10L);

        mockMvc.perform(get("/api/alumnos/estadisticas/total"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(10));

        verify(servicioAlumno).contarTotalAlumnos();
    }

    @Test
    @DisplayName("GET /api/alumnos/estadisticas/matriculas debe retornar estadísticas de matrículas")
    void testObtenerEstadisticasMatriculas() throws Exception {
        when(servicioAlumno.contarAlumnosMatriculados()).thenReturn(7L);
        when(servicioAlumno.contarAlumnosNoMatriculados()).thenReturn(3L);

        mockMvc.perform(get("/api/alumnos/estadisticas/matriculas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.matriculados").value(7))
                .andExpect(jsonPath("$.no_matriculados").value(3));

        verify(servicioAlumno).contarAlumnosMatriculados();
        verify(servicioAlumno).contarAlumnosNoMatriculados();
    }

    @Test
    @DisplayName("GET /api/alumnos/{alumnoId}/clases debe retornar clases inscritas")
    void testObtenerClasesInscritasConDetalles() throws Exception {
        DTOProfesor mockProfesor = new DTOProfesor(
                1L, "profesor1", "Profesor", "Test", "12345678Z", 
                "profesor@test.com", "123456789", app.entidades.Usuario.Rol.PROFESOR,
                true, Arrays.asList("1"), LocalDateTime.now()
        );
        
        DTOClaseInscrita claseInscrita = new DTOClaseInscrita(
                1L, "Clase Test", "Descripción de la clase", 
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", app.entidades.enums.ENivel.INTERMEDIO,
                Arrays.asList("1"), Arrays.asList("1"), Arrays.asList("1"),
                Arrays.asList(), "CURSO", mockProfesor, LocalDateTime.now()
        );
        List<DTOClaseInscrita> clases = Arrays.asList(claseInscrita);
        when(servicioClase.obtenerClasesInscritasConDetalles(1L)).thenReturn(clases);

        mockMvc.perform(get("/api/alumnos/1/clases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("clase1"));

        verify(servicioClase).obtenerClasesInscritasConDetalles(1L);
    }

    @Test
    @DisplayName("GET /api/alumnos/mi-perfil debe retornar perfil del alumno autenticado")
    void testObtenerMiPerfil() throws Exception {
        DTOPerfilAlumno perfil = new DTOPerfilAlumno("alumno1", "Juan", "Pérez", "12345678Z", 
                "juan@ejemplo.com", "123456789", LocalDateTime.now(), true, 
                Arrays.asList(), Arrays.asList(), Arrays.asList(), app.entidades.Usuario.Rol.ALUMNO);
        when(securityUtils.getCurrentUsername()).thenReturn("alumno1");
        when(servicioAlumno.obtenerPerfilAlumnoPorUsuario("alumno1")).thenReturn(perfil);

        mockMvc.perform(get("/api/alumnos/mi-perfil"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuario").value("alumno1"))
                .andExpect(jsonPath("$.nombre").value("Juan"));

        verify(securityUtils).getCurrentUsername();
        verify(servicioAlumno).obtenerPerfilAlumnoPorUsuario("alumno1");
    }
}
