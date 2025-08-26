package app.rest;

import app.dtos.DTOAlumno;
import app.dtos.DTOAlumnoPublico;
import app.dtos.DTOClase;
import app.dtos.DTORespuestaAlumnosClase;
import app.entidades.Usuario;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserClaseRest.class)
@Import(BaseRestTestConfig.class)
class UserClaseRestTest {

    @MockBean
    private ServicioClase servicioClase;

    @MockBean
    private ServicioAlumno servicioAlumno;

    @MockBean
    private SecurityUtils securityUtils;

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Usuario usuarioMock;
    private DTOClase dtoClase;
    private DTOAlumno dtoAlumno;
    private DTOAlumnoPublico alumnoPublico;
    private DTORespuestaAlumnosClase respuestaAlumnosClase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        usuarioMock = new Usuario("testuser", "password", "Test", "User", "12345678Z", "test@example.com", "123456789");
        usuarioMock.setId(1L);
        usuarioMock.setRole(Usuario.Role.PROFESOR);

        dtoClase = new DTOClase(
            1L, "Test Class", "Test Description", 
            BigDecimal.valueOf(100), 
            app.entidades.enums.EPresencialidad.PRESENCIAL,
            "test-image.jpg", app.entidades.enums.EDificultad.INTERMEDIO,
            Arrays.asList("alum1"), Arrays.asList("prof1"), 
            Arrays.asList("ej1"), Arrays.asList(), "CURSO"
        );

        dtoAlumno = new DTOAlumno(
            1L, "alumno1", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", 
            "+34612345678", LocalDateTime.now(), true, true, 
            Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Role.ALUMNO
        );

        alumnoPublico = new DTOAlumnoPublico("Juan", "Pérez");

        respuestaAlumnosClase = new DTORespuestaAlumnosClase(
            Arrays.asList(dtoAlumno), 
            new DTORespuestaAlumnosClase.DTOMetadatosPaginacion(0, 20, 1, 1, true, true, false, false),
            "COMPLETE"
        );
    }

    @Test
    @DisplayName("GET /api/my/classes debe obtener clases del profesor")
    void testObtenerMisClasesConUsuarioProfesor() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClasesPorProfesor("1")).thenReturn(Arrays.asList(dtoClase));

        mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Test Class"));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesPorProfesor("1");
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students debe obtener alumnos de clase")
    void testObtenerAlumnosDeClase() throws Exception {
        mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").isEmpty())
                .andExpect(jsonPath("$.informationType").value("PUBLIC"));
    }

    @Test
    @DisplayName("GET /api/my/classes debe manejar error cuando getCurrentUserId falla por falta de autenticación")
    void testObtenerMisClasesErrorUsuario() throws Exception {
        when(securityUtils.getCurrentUserId()).thenThrow(new RuntimeException("No user is currently authenticated"));

        mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isInternalServerError());

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase, never()).obtenerClasesPorProfesor(anyString());
    }

    @Test
    @DisplayName("GET /api/my/classes debe manejar error cuando getCurrentUserId falla")
    void testObtenerMisClasesErrorGetCurrentUserId() throws Exception {
        when(securityUtils.getCurrentUserId()).thenThrow(new RuntimeException("Error obteniendo ID de usuario"));

        mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isInternalServerError());

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase, never()).obtenerClasesPorProfesor(anyString());
    }




}
