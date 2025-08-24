package app.rest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import app.entidades.Usuario;
import app.entidades.enums.EDificultad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.dtos.DTOClaseConDetalles;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOEstadoInscripcion;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTOProfesor;
import app.dtos.DTORespuestaEnrollment;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para EnrollmentRest")
class EnrollmentRestTest {

    @Mock
    private ServicioClase servicioClase;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private EnrollmentRest enrollmentRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTOPeticionEnrollment peticionEnrollment;
    private DTORespuestaEnrollment respuestaExitosa;
    private DTORespuestaEnrollment respuestaError;
    private DTOEstadoInscripcion estadoInscripcion;
    private DTOClaseInscrita claseInscrita;
    private DTOClaseConDetalles claseConDetalles;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(enrollmentRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Crear DTOs de prueba
        peticionEnrollment = new DTOPeticionEnrollment(1L, 1L);

        respuestaExitosa = DTORespuestaEnrollment.success(1L, 1L, "Juan Pérez", "Matemáticas I", "ENROLLMENT");

        respuestaError = DTORespuestaEnrollment.failure(1L, 1L, "Error en la inscripción: alumno ya inscrito", "ENROLLMENT");

        estadoInscripcion = new DTOEstadoInscripcion(1L, 1L, true, LocalDateTime.now());

        // Create mock objects for testing
        DTOProfesor mockProfesor = new DTOProfesor(
                1L, "profesor1", "Juan", "Profesor", "12345678Z", 
                "juan.profesor@ejemplo.com", "123456789", Usuario.Role.PROFESOR,
                true, Arrays.asList("1"), LocalDateTime.now()
        );
        
        // For DTOClaseInscrita, we need to create a mock Clase first
        // Since we can't easily create a full Clase object, we'll use a simplified approach
        claseInscrita = new DTOClaseInscrita(
                1L, "Matemáticas I", "Descripción de la clase", 
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", EDificultad.INTERMEDIO,
                Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"),
                Arrays.asList(), "CURSO", mockProfesor, LocalDateTime.now()
        );

        claseConDetalles = new DTOClaseConDetalles(
                1L, "Matemáticas I", "Descripción de la clase",
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", EDificultad.INTERMEDIO,
                Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"),
                Arrays.asList(), "CURSO", mockProfesor, true, LocalDateTime.now(), 2, 1
        );
    }

    @Test
    @DisplayName("POST /api/enrollments debe inscribir alumno en clase exitosamente")
    void testInscribirAlumnoEnClaseExitoso() throws Exception {
        when(servicioClase.inscribirAlumnoEnClase(any(DTOPeticionEnrollment.class)))
                .thenReturn(respuestaExitosa);

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEnrollment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operación realizada con éxito"))
                .andExpect(jsonPath("$.alumnoId").value(1))
                .andExpect(jsonPath("$.claseId").value(1));

        verify(servicioClase).inscribirAlumnoEnClase(any(DTOPeticionEnrollment.class));
    }

    @Test
    @DisplayName("POST /api/enrollments debe retornar error cuando falla la inscripción")
    void testInscribirAlumnoEnClaseError() throws Exception {
        when(servicioClase.inscribirAlumnoEnClase(any(DTOPeticionEnrollment.class)))
                .thenReturn(respuestaError);

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEnrollment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error en la inscripción: alumno ya inscrito"));

        verify(servicioClase).inscribirAlumnoEnClase(any(DTOPeticionEnrollment.class));
    }

    @Test
    @DisplayName("POST /api/enrollments debe validar datos requeridos")
    void testInscribirAlumnoValidacion() throws Exception {
        DTOPeticionEnrollment peticionInvalida = new DTOPeticionEnrollment(null, null);

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());

        verify(servicioClase, never()).inscribirAlumnoEnClase(any());
    }

    @Test
    @DisplayName("DELETE /api/enrollments debe dar de baja alumno exitosamente")
    void testDarDeBajaAlumnoDeClaseExitoso() throws Exception {
        when(servicioClase.darDeBajaAlumnoDeClase(any(DTOPeticionEnrollment.class)))
                .thenReturn(respuestaExitosa);

        mockMvc.perform(delete("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEnrollment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        verify(servicioClase).darDeBajaAlumnoDeClase(any(DTOPeticionEnrollment.class));
    }

    @Test
    @DisplayName("DELETE /api/enrollments debe retornar error cuando falla la baja")
    void testDarDeBajaAlumnoDeClaseError() throws Exception {
        when(servicioClase.darDeBajaAlumnoDeClase(any(DTOPeticionEnrollment.class)))
                .thenReturn(respuestaError);

        mockMvc.perform(delete("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionEnrollment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));

        verify(servicioClase).darDeBajaAlumnoDeClase(any(DTOPeticionEnrollment.class));
    }

    @Test
    @DisplayName("POST /api/enrollments/{claseId}/self-enroll debe permitir auto-inscripción")
    void testInscribirseEnClase() throws Exception {
        when(servicioClase.inscribirseEnClase(1L)).thenReturn(respuestaExitosa);

        mockMvc.perform(post("/api/enrollments/1/self-enroll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.claseId").value(1));

        verify(servicioClase).inscribirseEnClase(1L);
    }

    @Test
    @DisplayName("POST /api/enrollments/{claseId}/self-enroll debe retornar error cuando falla")
    void testInscribirseEnClaseError() throws Exception {
        when(servicioClase.inscribirseEnClase(1L)).thenReturn(respuestaError);

        mockMvc.perform(post("/api/enrollments/1/self-enroll"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));

        verify(servicioClase).inscribirseEnClase(1L);
    }

    @Test
    @DisplayName("DELETE /api/enrollments/{claseId}/self-unenroll debe permitir auto-baja")
    void testDarseDeBajaDeClase() throws Exception {
        when(servicioClase.darseDeBajaDeClase(1L)).thenReturn(respuestaExitosa);

        mockMvc.perform(delete("/api/enrollments/1/self-unenroll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.claseId").value(1));

        verify(servicioClase).darseDeBajaDeClase(1L);
    }

    @Test
    @DisplayName("DELETE /api/enrollments/{claseId}/self-unenroll debe retornar error cuando falla")
    void testDarseDeBajaDeClaseError() throws Exception {
        when(servicioClase.darseDeBajaDeClase(1L)).thenReturn(respuestaError);

        mockMvc.perform(delete("/api/enrollments/1/self-unenroll"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));

        verify(servicioClase).darseDeBajaDeClase(1L);
    }

    @Test
    @DisplayName("GET /api/enrollments/{claseId}/status/{alumnoId} debe verificar estado de inscripción")
    void testVerificarEstadoInscripcion() throws Exception {
        when(servicioClase.verificarEstadoInscripcion(1L, 1L)).thenReturn(estadoInscripcion);

        mockMvc.perform(get("/api/enrollments/1/status/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnoId").value(1))
                .andExpect(jsonPath("$.claseId").value(1))
                .andExpect(jsonPath("$.isEnrolled").value(true));

        verify(servicioClase).verificarEstadoInscripcion(1L, 1L);
    }

    @Test
    @DisplayName("GET /api/enrollments/{claseId}/my-status debe verificar mi estado de inscripción")
    void testVerificarMiEstadoInscripcion() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.verificarEstadoInscripcion(1L, 1L)).thenReturn(estadoInscripcion);

        mockMvc.perform(get("/api/enrollments/1/my-status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnoId").value(1))
                .andExpect(jsonPath("$.claseId").value(1))
                .andExpect(jsonPath("$.isEnrolled").value(true));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).verificarEstadoInscripcion(1L, 1L);
    }

    @Test
    @DisplayName("GET /api/enrollments/my-enrolled-classes debe obtener mis clases inscritas")
    void testObtenerMisClasesInscritas() throws Exception {
        List<DTOClaseInscrita> clases = Arrays.asList(claseInscrita);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClasesInscritasConDetalles(1L)).thenReturn(clases);

        mockMvc.perform(get("/api/enrollments/my-enrolled-classes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Matemáticas I"));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesInscritasConDetalles(1L);
    }

    @Test
    @DisplayName("GET /api/enrollments/{claseId}/details-for-student/{alumnoId} debe obtener detalles de clase")
    void testObtenerClaseConDetallesParaEstudiante() throws Exception {
        when(servicioClase.obtenerClaseConDetallesParaEstudiante(1L, 1L)).thenReturn(claseConDetalles);

        mockMvc.perform(get("/api/enrollments/1/details-for-student/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Matemáticas I"))
                .andExpect(jsonPath("$.isEnrolled").value(true));

        verify(servicioClase).obtenerClaseConDetallesParaEstudiante(1L, 1L);
    }

    @Test
    @DisplayName("GET /api/enrollments/{claseId}/details-for-me debe obtener detalles de clase para mí")
    void testObtenerClaseConDetallesParaMi() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(servicioClase.obtenerClaseConDetallesParaEstudiante(1L, 1L)).thenReturn(claseConDetalles);

        mockMvc.perform(get("/api/enrollments/1/details-for-me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Matemáticas I"))
                .andExpect(jsonPath("$.isEnrolled").value(true));

        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClaseConDetallesParaEstudiante(1L, 1L);
    }

    @Test
    @DisplayName("POST /api/enrollments con JSON malformado debe retornar 500")
    void testInscribirAlumnoJsonMalformado() throws Exception {
        String jsonMalformado = "{\"alumnoId\": 1, \"claseId\":}";

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isInternalServerError());

        verify(servicioClase, never()).inscribirAlumnoEnClase(any());
    }

    @Test
    @DisplayName("DELETE /api/enrollments con JSON malformado debe retornar 500")
    void testDarDeBajaAlumnoJsonMalformado() throws Exception {
        String jsonMalformado = "{\"alumnoId\":, \"claseId\": 1}";

        mockMvc.perform(delete("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isInternalServerError());

        verify(servicioClase, never()).darDeBajaAlumnoDeClase(any());
    }
}
