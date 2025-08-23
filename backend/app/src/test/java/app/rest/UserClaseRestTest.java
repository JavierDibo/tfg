package app.rest;

import app.dtos.*;
import app.entidades.Usuario;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para UserClaseRest")
class UserClaseRestTest {

    @Mock
    private ServicioClase servicioClase;

    @Mock
    private ServicioAlumno servicioAlumno;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserClaseRest userClaseRest;

    private MockMvc mockMvc;

    private DTOClase dtoClase;
    private DTOClaseInscrita claseInscrita;
    private DTORespuestaAlumnosClase respuestaAlumnosClase;
    private DTOAlumnoPublico alumnoPublico;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userClaseRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();

        // Crear DTOs de prueba
        dtoClase = new DTOClase(
                1L, "Matemáticas I", "Descripción de la clase",
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", app.entidades.enums.ENivel.INTERMEDIO,
                Arrays.asList("alumno1", "alumno2"), Arrays.asList("profesor1"), Arrays.asList("ejercicio1"),
                Arrays.asList(), "CURSO"
        );

        DTOProfesor mockProfesor = new DTOProfesor(
                1L, "profesor1", "Juan", "Profesor", "12345678Z", 
                "juan.profesor@ejemplo.com", "123456789", app.entidades.Usuario.Rol.PROFESOR,
                true, Arrays.asList("1"), LocalDateTime.now()
        );

        claseInscrita = new DTOClaseInscrita(
                1L, "Matemáticas I", "Descripción de la clase", 
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", app.entidades.enums.ENivel.INTERMEDIO,
                Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"),
                Arrays.asList(), "CURSO", mockProfesor, LocalDateTime.now()
        );

        DTOAlumno dtoAlumno1 = new DTOAlumno(
                1L, "alumno1", "Juan", "Pérez", "12345678Z", 
                "juan@ejemplo.com", "123456789", LocalDateTime.now(), true, true,
                Arrays.asList("1"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Rol.ALUMNO
        );
        
        DTOAlumno dtoAlumno2 = new DTOAlumno(
                2L, "alumno2", "María", "García", "87654321Y", 
                "maria@ejemplo.com", "987654321", LocalDateTime.now(), true, true,
                Arrays.asList("1"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Rol.ALUMNO
        );

        respuestaAlumnosClase = new DTORespuestaAlumnosClase(
                Arrays.asList(dtoAlumno1, dtoAlumno2), 
                new DTORespuestaAlumnosClase.DTOMetadatosPaginacion(0, 20, 2, 1, true, false, false, true),
                "COMPLETA"
        );

        alumnoPublico = new DTOAlumnoPublico("Juan", "Pérez");

        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setRol(Usuario.Rol.PROFESOR);
    }

    // ===== TESTS PARA OPERACIONES DE PROFESORES =====

    @Test
    @DisplayName("GET /api/my/classes debe retornar clases del profesor autenticado")
    void testObtenerMisClases() throws Exception {
        List<DTOClase> clases = Arrays.asList(dtoClase);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClasesPorProfesor("1")).thenReturn(clases);

        mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Matemáticas I"));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesPorProfesor("1");
    }

    // ===== TESTS PARA OPERACIONES DE ALUMNOS =====

    @Test
    @DisplayName("GET /api/my/enrolled-classes debe retornar clases inscritas del alumno")
    void testObtenerMisClasesInscritas() throws Exception {
        List<DTOClaseInscrita> clases = Arrays.asList(claseInscrita);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClasesInscritasConDetalles(1L)).thenReturn(clases);

        mockMvc.perform(get("/api/my/enrolled-classes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Matemáticas I"));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesInscritasConDetalles(1L);
    }

    // ===== TESTS PARA CONSULTA DE ALUMNOS =====

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students debe retornar alumnos de la clase")
    void testObtenerAlumnosDeClase() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(usuarioMock);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(respuestaAlumnosClase);

        mockMvc.perform(get("/api/my/classes/1/students")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.claseId").value("1"))
                .andExpect(jsonPath("$.claseNombre").value("Matemáticas I"))
                .andExpect(jsonPath("$.alumnos").isArray())
                .andExpect(jsonPath("$.totalElementos").value(2));

        verify(securityUtils).getCurrentUser();
        verify(securityUtils).getCurrentUserId();
        verify(servicioAlumno).obtenerAlumnosPorClaseConNivelAcceso(1L, 0, 20, "id", "ASC", "PROFESOR", 1L);
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students con parámetros por defecto")
    void testObtenerAlumnosDeClaseParametrosDefecto() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(usuarioMock);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(respuestaAlumnosClase);

        mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.claseId").value("1"));

        verify(servicioAlumno).obtenerAlumnosPorClaseConNivelAcceso(1L, 0, 20, "id", "ASC", "PROFESOR", 1L);
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students/public debe retornar alumnos públicos")
    void testObtenerAlumnosPublicosDeClase() throws Exception {
        List<DTOAlumnoPublico> alumnosPublicos = Arrays.asList(alumnoPublico);
        when(servicioClase.obtenerAlumnosPublicosDeClase(1L)).thenReturn(alumnosPublicos);

        mockMvc.perform(get("/api/my/classes/1/students/public"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].apellidos").value("Pérez"));

        verify(servicioClase).obtenerAlumnosPublicosDeClase(1L);
    }

    // ===== TESTS DE VALIDACIÓN Y ERRORES =====

    @Test
    @DisplayName("GET /api/my/classes debe manejar error cuando no hay usuario autenticado")
    void testObtenerMisClasesErrorUsuario() throws Exception {
        when(securityUtils.getCurrentUserId()).thenThrow(new RuntimeException("Usuario no autenticado"));

        mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isInternalServerError());

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase, never()).obtenerClasesPorProfesor(anyString());
    }

    @Test
    @DisplayName("GET /api/my/enrolled-classes debe manejar error del servicio")
    void testObtenerMisClasesInscritasErrorServicio() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClasesInscritasConDetalles(1L))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(get("/api/my/enrolled-classes"))
                .andExpect(status().isInternalServerError());

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesInscritasConDetalles(1L);
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students debe manejar error del servicio")
    void testObtenerAlumnosDeClaseErrorServicio() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(usuarioMock);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isInternalServerError());

        verify(securityUtils).getCurrentUser();
        verify(securityUtils).getCurrentUserId();
        verify(servicioAlumno).obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students/public debe manejar error del servicio")
    void testObtenerAlumnosPublicosDeClaseErrorServicio() throws Exception {
        when(servicioClase.obtenerAlumnosPublicosDeClase(1L))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(get("/api/my/classes/1/students/public"))
                .andExpect(status().isInternalServerError());

        verify(servicioClase).obtenerAlumnosPublicosDeClase(1L);
    }

    @Test
    @DisplayName("GET /api/my/classes con usuario ADMIN")
    void testObtenerMisClasesConUsuarioAdmin() throws Exception {
        Usuario adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRol(Usuario.Rol.ADMIN);
        
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(respuestaAlumnosClase);

        mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).obtenerAlumnosPorClaseConNivelAcceso(1L, 0, 20, "id", "ASC", "ADMIN", 1L);
    }

    @Test
    @DisplayName("GET /api/my/classes con usuario ALUMNO")
    void testObtenerMisClasesConUsuarioAlumno() throws Exception {
        Usuario alumnoUser = new Usuario();
        alumnoUser.setId(1L);
        alumnoUser.setRol(Usuario.Rol.ALUMNO);
        
        when(securityUtils.getCurrentUser()).thenReturn(alumnoUser);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioAlumno.obtenerAlumnosPorClaseConNivelAcceso(anyLong(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(respuestaAlumnosClase);

        mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(servicioAlumno).obtenerAlumnosPorClaseConNivelAcceso(1L, 0, 20, "id", "ASC", "ALUMNO", 1L);
    }
}
