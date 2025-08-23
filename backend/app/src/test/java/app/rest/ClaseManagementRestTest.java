package app.rest;

import app.dtos.DTOClase;
import app.entidades.Material;
import app.servicios.ServicioClase;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ClaseManagementRest")
class ClaseManagementRestTest {

    @Mock
    private ServicioClase servicioClase;

    @InjectMocks
    private ClaseManagementRest claseManagementRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTOClase dtoClase;
    private Material material;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(claseManagementRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Crear DTOs de prueba
        dtoClase = new DTOClase(
                1L, "Matemáticas I", "Descripción de la clase",
                new java.math.BigDecimal("50.00"), app.entidades.enums.EPresencialidad.PRESENCIAL,
                "imagen.jpg", app.entidades.enums.ENivel.INTERMEDIO,
                Arrays.asList("alumno1", "alumno2"), Arrays.asList("profesor1"), Arrays.asList("ejercicio1"),
                Arrays.asList(), "CURSO"
        );

        material = new Material();
        material.setId("material1");
        material.setNombre("Material de prueba");
    }

    // ===== TESTS PARA GESTIÓN DE PROFESORES =====

    @Test
    @DisplayName("POST /api/clases/{claseId}/profesores/{profesorId} debe agregar profesor")
    void testAgregarProfesor() throws Exception {
        when(servicioClase.agregarProfesor(1L, "profesor1")).thenReturn(dtoClase);

        mockMvc.perform(post("/api/clases/1/profesores/profesor1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Matemáticas I"))
                .andExpect(jsonPath("$.profesoresId").isArray());

        verify(servicioClase).agregarProfesor(1L, "profesor1");
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/profesores/{profesorId} debe remover profesor")
    void testRemoverProfesor() throws Exception {
        when(servicioClase.removerProfesor(1L, "profesor1")).thenReturn(dtoClase);

        mockMvc.perform(delete("/api/clases/1/profesores/profesor1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Matemáticas I"));

        verify(servicioClase).removerProfesor(1L, "profesor1");
    }

    // ===== TESTS PARA GESTIÓN DE EJERCICIOS =====

    @Test
    @DisplayName("POST /api/clases/{claseId}/ejercicios/{ejercicioId} debe agregar ejercicio")
    void testAgregarEjercicio() throws Exception {
        when(servicioClase.agregarEjercicio(1L, "ejercicio1")).thenReturn(dtoClase);

        mockMvc.perform(post("/api/clases/1/ejercicios/ejercicio1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ejerciciosId").isArray());

        verify(servicioClase).agregarEjercicio(1L, "ejercicio1");
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/ejercicios/{ejercicioId} debe remover ejercicio")
    void testRemoverEjercicio() throws Exception {
        when(servicioClase.removerEjercicio(1L, "ejercicio1")).thenReturn(dtoClase);

        mockMvc.perform(delete("/api/clases/1/ejercicios/ejercicio1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Matemáticas I"));

        verify(servicioClase).removerEjercicio(1L, "ejercicio1");
    }

    // ===== TESTS PARA GESTIÓN DE MATERIALES =====

    @Test
    @DisplayName("POST /api/clases/{claseId}/material debe agregar material")
    void testAgregarMaterial() throws Exception {
        when(servicioClase.agregarMaterial(eq(1L), any(Material.class))).thenReturn(dtoClase);

        mockMvc.perform(post("/api/clases/1/material")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(material)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"));

        verify(servicioClase).agregarMaterial(eq(1L), any(Material.class));
    }

    @Test
    @DisplayName("POST /api/clases/{claseId}/material debe validar datos requeridos")
    void testAgregarMaterialValidacion() throws Exception {
        Material materialInvalido = new Material();

        mockMvc.perform(post("/api/clases/1/material")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(materialInvalido)))
                .andExpect(status().isBadRequest());

        verify(servicioClase, never()).agregarMaterial(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/material/{materialId} debe remover material")
    void testRemoverMaterial() throws Exception {
        when(servicioClase.removerMaterial(1L, "material1")).thenReturn(dtoClase);

        mockMvc.perform(delete("/api/clases/1/material/material1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"));

        verify(servicioClase).removerMaterial(1L, "material1");
    }

    // ===== TESTS PARA GESTIÓN DE ALUMNOS =====

    @Test
    @DisplayName("POST /api/clases/{claseId}/alumnos/{alumnoId} debe agregar alumno")
    void testAgregarAlumno() throws Exception {
        when(servicioClase.agregarAlumno(1L, "alumno1")).thenReturn(dtoClase);

        mockMvc.perform(post("/api/clases/1/alumnos/alumno1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.alumnosId").isArray());

        verify(servicioClase).agregarAlumno(1L, "alumno1");
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/alumnos/{alumnoId} debe remover alumno")
    void testRemoverAlumno() throws Exception {
        when(servicioClase.removerAlumno(1L, "alumno1")).thenReturn(dtoClase);

        mockMvc.perform(delete("/api/clases/1/alumnos/alumno1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nombre").value("Matemáticas I"));

        verify(servicioClase).removerAlumno(1L, "alumno1");
    }

    // ===== TESTS DE VALIDACIÓN Y ERRORES =====

    @Test
    @DisplayName("POST /api/clases/{claseId}/material con JSON malformado debe retornar 400")
    void testAgregarMaterialJsonMalformado() throws Exception {
        String jsonMalformado = "{\"nombre\": \"test\", \"descripcion\":}";

        mockMvc.perform(post("/api/clases/1/material")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(servicioClase, never()).agregarMaterial(anyLong(), any());
    }

    @Test
    @DisplayName("POST /api/clases/{claseId}/material sin Content-Type debe retornar 415")
    void testAgregarMaterialSinContentType() throws Exception {
        mockMvc.perform(post("/api/clases/1/material")
                .content(objectMapper.writeValueAsString(material)))
                .andExpect(status().isUnsupportedMediaType());

        verify(servicioClase, never()).agregarMaterial(anyLong(), any());
    }

    @Test
    @DisplayName("POST /api/clases/{claseId}/profesores/{profesorId} debe manejar errores del servicio")
    void testAgregarProfesorErrorServicio() throws Exception {
        when(servicioClase.agregarProfesor(1L, "profesor1"))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(post("/api/clases/1/profesores/profesor1"))
                .andExpect(status().isInternalServerError());

        verify(servicioClase).agregarProfesor(1L, "profesor1");
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/ejercicios/{ejercicioId} debe manejar errores del servicio")
    void testRemoverEjercicioErrorServicio() throws Exception {
        when(servicioClase.removerEjercicio(1L, "ejercicio1"))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(delete("/api/clases/1/ejercicios/ejercicio1"))
                .andExpect(status().isInternalServerError());

        verify(servicioClase).removerEjercicio(1L, "ejercicio1");
    }

    @Test
    @DisplayName("DELETE /api/clases/{claseId}/material/{materialId} debe manejar errores del servicio")
    void testRemoverMaterialErrorServicio() throws Exception {
        when(servicioClase.removerMaterial(1L, "material1"))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(delete("/api/clases/1/material/material1"))
                .andExpect(status().isInternalServerError());

        verify(servicioClase).removerMaterial(1L, "material1");
    }

    @Test
    @DisplayName("POST /api/clases/{claseId}/alumnos/{alumnoId} debe manejar errores del servicio")
    void testAgregarAlumnoErrorServicio() throws Exception {
        when(servicioClase.agregarAlumno(1L, "alumno1"))
                .thenThrow(new RuntimeException("Error en el servicio"));

        mockMvc.perform(post("/api/clases/1/alumnos/alumno1"))
                .andExpect(status().isInternalServerError());

        verify(servicioClase).agregarAlumno(1L, "alumno1");
    }
}
