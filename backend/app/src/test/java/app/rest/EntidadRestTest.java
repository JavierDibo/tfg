package app.rest;

import app.dtos.DTOEntidad;
import app.dtos.DTOParametrosBusqueda;
import app.servicios.ServicioEntidad;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para EntidadRest")
class EntidadRestTest {

    @Mock
    private ServicioEntidad servicioEntidad;

    @InjectMocks
    private EntidadRest entidadRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTOEntidad dtoEntidad1;
    private DTOEntidad dtoEntidad2;
    private DTOEntidad dtoEntidad3;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(entidadRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Crear DTOs de prueba
        dtoEntidad1 = new DTOEntidad(1, "Entidad 1", "Información 1");
        dtoEntidad2 = new DTOEntidad(2, "Entidad 2", "Información 2");
        dtoEntidad3 = new DTOEntidad(3, "Entidad 3", "Información 3");
    }

    @Test
    @DisplayName("GET /api/entidades debe retornar todas las entidades")
    void testObtenerEntidades() throws Exception {
        List<DTOEntidad> entidades = Arrays.asList(dtoEntidad1, dtoEntidad2, dtoEntidad3);
        when(servicioEntidad.obtenerEntidadesPorParametros(any(DTOParametrosBusqueda.class)))
                .thenReturn(entidades);

        mockMvc.perform(get("/api/entidades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].info").value("Entidad 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));

        verify(servicioEntidad).obtenerEntidadesPorParametros(any(DTOParametrosBusqueda.class));
    }

    @Test
    @DisplayName("GET /api/entidades con parámetros debe filtrar entidades")
    void testObtenerEntidadesConParametros() throws Exception {
        List<DTOEntidad> entidadesFiltradas = Arrays.asList(dtoEntidad1);
        when(servicioEntidad.obtenerEntidadesPorParametros(any(DTOParametrosBusqueda.class)))
                .thenReturn(entidadesFiltradas);

        mockMvc.perform(get("/api/entidades")
                .param("info", "Entidad 1")
                .param("otraInfo", "Información adicional"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].info").value("Entidad 1"));

        verify(servicioEntidad).obtenerEntidadesPorParametros(any(DTOParametrosBusqueda.class));
    }

    @Test
    @DisplayName("GET /api/entidades/{id} debe retornar entidad por ID")
    void testObtenerEntidadPorId() throws Exception {
        when(servicioEntidad.obtenerEntidadPorId(1)).thenReturn(dtoEntidad1);

        mockMvc.perform(get("/api/entidades/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.info").value("Entidad 1"))
                .andExpect(jsonPath("$.otraInfo").value("Información 1"));

        verify(servicioEntidad).obtenerEntidadPorId(1);
    }

    @Test
    @DisplayName("GET /api/entidades/{id} debe retornar 404 cuando entidad no existe")
    void testObtenerEntidadPorIdNoExiste() throws Exception {
        when(servicioEntidad.obtenerEntidadPorId(999)).thenReturn(null);

        mockMvc.perform(get("/api/entidades/999"))
                .andExpect(status().isNotFound());

        verify(servicioEntidad).obtenerEntidadPorId(999);
    }

    @Test
    @DisplayName("GET /api/entidades/{id} debe validar ID mínimo")
    void testObtenerEntidadPorIdInvalido() throws Exception {
        mockMvc.perform(get("/api/entidades/0"))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).obtenerEntidadPorId(anyInt());
    }

    @Test
    @DisplayName("POST /api/entidades debe crear entidad correctamente")
    void testCrearEntidad() throws Exception {
        DTOEntidad nuevaEntidad = new DTOEntidad(0, "Nueva Entidad", "Nueva Información");
        when(servicioEntidad.crearEntidad(any(DTOEntidad.class))).thenReturn(dtoEntidad1);

        mockMvc.perform(post("/api/entidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaEntidad)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.info").value("Entidad 1"));

        verify(servicioEntidad).crearEntidad(any(DTOEntidad.class));
    }

    @Test
    @DisplayName("POST /api/entidades debe validar datos requeridos")
    void testCrearEntidadValidacion() throws Exception {
        DTOEntidad entidadInvalida = new DTOEntidad(0, "", "");

        mockMvc.perform(post("/api/entidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entidadInvalida)))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).crearEntidad(any());
    }

    @Test
    @DisplayName("PATCH /api/entidades/{id} debe actualizar entidad")
    void testActualizarEntidad() throws Exception {
        DTOEntidad entidadActualizada = new DTOEntidad(1, "Entidad Actualizada", "Información Actualizada");
        when(servicioEntidad.actualizarEntidad(eq(1), any(DTOEntidad.class))).thenReturn(entidadActualizada);

        mockMvc.perform(patch("/api/entidades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entidadActualizada)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.info").value("Entidad Actualizada"));

        verify(servicioEntidad).actualizarEntidad(eq(1), any(DTOEntidad.class));
    }

    @Test
    @DisplayName("PATCH /api/entidades/{id} debe validar ID mínimo")
    void testActualizarEntidadIdInvalido() throws Exception {
        DTOEntidad entidadActualizada = new DTOEntidad(1, "Entidad Actualizada", "Información Actualizada");

        mockMvc.perform(patch("/api/entidades/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entidadActualizada)))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).actualizarEntidad(anyInt(), any());
    }

    @Test
    @DisplayName("DELETE /api/entidades/{id} debe borrar entidad por ID")
    void testBorrarEntidadPorId() throws Exception {
        when(servicioEntidad.borrarEntidadPorId(1)).thenReturn(dtoEntidad1);

        mockMvc.perform(delete("/api/entidades/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.info").value("Entidad 1"));

        verify(servicioEntidad).borrarEntidadPorId(1);
    }

    @Test
    @DisplayName("DELETE /api/entidades/{id} debe validar ID mínimo")
    void testBorrarEntidadPorIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/entidades/0"))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).borrarEntidadPorId(anyInt());
    }

    @Test
    @DisplayName("DELETE /api/entidades debe borrar todas las entidades")
    void testBorrarTodasLasEntidades() throws Exception {
        List<DTOEntidad> entidadesBorradas = Arrays.asList(dtoEntidad1, dtoEntidad2, dtoEntidad3);
        when(servicioEntidad.borrarTodasLasEntidades()).thenReturn(entidadesBorradas);

        mockMvc.perform(delete("/api/entidades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));

        verify(servicioEntidad).borrarTodasLasEntidades();
    }

    @Test
    @DisplayName("POST /api/entidades con JSON malformado debe retornar 400")
    void testCrearEntidadJsonMalformado() throws Exception {
        String jsonMalformado = "{\"id\": 1, \"info\": \"test\", \"otraInfo\":}";

        mockMvc.perform(post("/api/entidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).crearEntidad(any());
    }

    @Test
    @DisplayName("PATCH /api/entidades/{id} con JSON malformado debe retornar 400")
    void testActualizarEntidadJsonMalformado() throws Exception {
        String jsonMalformado = "{\"id\": 1, \"info\":, \"otraInfo\": \"test\"}";

        mockMvc.perform(patch("/api/entidades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).actualizarEntidad(anyInt(), any());
    }

    @Test
    @DisplayName("POST /api/entidades sin Content-Type debe retornar 415")
    void testCrearEntidadSinContentType() throws Exception {
        DTOEntidad nuevaEntidad = new DTOEntidad(0, "Nueva Entidad", "Nueva Información");

        mockMvc.perform(post("/api/entidades")
                .content(objectMapper.writeValueAsString(nuevaEntidad)))
                .andExpect(status().isUnsupportedMediaType());

        verify(servicioEntidad, never()).crearEntidad(any());
    }

    @Test
    @DisplayName("PATCH /api/entidades/{id} sin Content-Type debe retornar 415")
    void testActualizarEntidadSinContentType() throws Exception {
        DTOEntidad entidadActualizada = new DTOEntidad(1, "Entidad Actualizada", "Información Actualizada");

        mockMvc.perform(patch("/api/entidades/1")
                .content(objectMapper.writeValueAsString(entidadActualizada)))
                .andExpect(status().isUnsupportedMediaType());

        verify(servicioEntidad, never()).actualizarEntidad(anyInt(), any());
    }

    @Test
    @DisplayName("GET /api/entidades con parámetros muy largos debe validar tamaño")
    void testObtenerEntidadesParametrosLargos() throws Exception {
        String parametroMuyLargo = "a".repeat(101); // Excede el límite de 100 caracteres

        mockMvc.perform(get("/api/entidades")
                .param("info", parametroMuyLargo))
                .andExpect(status().isBadRequest());

        verify(servicioEntidad, never()).obtenerEntidadesPorParametros(any());
    }
}

