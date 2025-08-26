package app.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.dtos.DTOEjercicio;
import app.dtos.DTOPeticionCrearEjercicio;
import app.dtos.DTOParametrosBusquedaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.enums.EEstadoEjercicio;
import app.servicios.ServicioEjercicio;
import com.fasterxml.jackson.databind.ObjectMapper;



@WebMvcTest(EjercicioRest.class)
@Import(BaseRestTestConfig.class)
public class EjercicioRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioEjercicio servicioEjercicio;
    
    @Autowired
    private ObjectMapper objectMapper;

    // ===== GET /api/ejercicios - Get paginated exercises =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicios_Success() throws Exception {
        // Given
        List<DTOEjercicio> mockEjercicios = Arrays.asList(
            new DTOEjercicio(1L, "Ejercicio 1", "Descripción del ejercicio 1", 
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 5, 3),
            new DTOEjercicio(2L, "Ejercicio 2", "Descripción del ejercicio 2", 
                LocalDateTime.now(), LocalDateTime.now().plusDays(14), "1", 3, 1)
        );
        
        DTORespuestaPaginada<DTOEjercicio> mockResponse = DTORespuestaPaginada.of(
            mockEjercicios, 0, 20, 2, "id", "ASC"
        );
        
        when(servicioEjercicio.obtenerEjerciciosPaginados(
            any(), any(), any(), any(), any(), 
            anyInt(), anyInt(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/ejercicios")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ejercicio 1"))
                .andExpect(jsonPath("$.content[0].statement").value("Descripción del ejercicio 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Ejercicio 2"))
                .andExpect(jsonPath("$.content[1].statement").value("Descripción del ejercicio 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicios_WithFilters() throws Exception {
        // Given
        List<DTOEjercicio> mockEjercicios = Arrays.asList(
            new DTOEjercicio(1L, "Ejercicio 1", "Descripción del ejercicio 1", 
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 5, 3)
        );
        
        DTORespuestaPaginada<DTOEjercicio> mockResponse = DTORespuestaPaginada.of(
            mockEjercicios, 0, 20, 1, "id", "ASC"
        );
        
        when(servicioEjercicio.obtenerEjerciciosPaginados(
            eq("ejercicio"), eq("Ejercicio 1"), eq("Descripción"), eq("1"), eq("ACTIVO"), 
            anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/ejercicios")
                .param("q", "ejercicio")
                .param("name", "Ejercicio 1")
                .param("statement", "Descripción")
                .param("classId", "1")
                .param("status", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicios_InvalidPaginationParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ejercicios")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/ejercicios/{id} - Get exercise by ID =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicioPorId_Success() throws Exception {
        // Given
        DTOEjercicio mockEjercicio = new DTOEjercicio(
            1L, "Ejercicio 1", "Descripción del ejercicio 1", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 5, 3
        );
        
        when(servicioEjercicio.obtenerEjercicioPorId(1L)).thenReturn(mockEjercicio);

        // When & Then
        mockMvc.perform(get("/api/ejercicios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ejercicio 1"))
                .andExpect(jsonPath("$.statement").value("Descripción del ejercicio 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicioPorId_NotFound() throws Exception {
        // Given
        when(servicioEjercicio.obtenerEjercicioPorId(999L))
            .thenThrow(new app.excepciones.ResourceNotFoundException("Ejercicio", "ID", 999L));

        // When & Then
        mockMvc.perform(get("/api/ejercicios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicioPorId_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ejercicios/0"))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/ejercicios - Create new exercise =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearEjercicio_Success() throws Exception {
        // Given
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "Ejercicio Nuevo", "Descripción del nuevo ejercicio", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1"
        );
        
        DTOEjercicio mockEjercicio = new DTOEjercicio(
            1L, "Ejercicio Nuevo", "Descripción del nuevo ejercicio", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 0, 0
        );
        
        when(servicioEjercicio.crearEjercicio(
            eq("Ejercicio Nuevo"), eq("Descripción del nuevo ejercicio"), 
            any(LocalDateTime.class), any(LocalDateTime.class), eq("1")
        )).thenReturn(mockEjercicio);

        // When & Then
        mockMvc.perform(post("/api/ejercicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ejercicio Nuevo"))
                .andExpect(jsonPath("$.statement").value("Descripción del nuevo ejercicio"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearEjercicio_InvalidInput() throws Exception {
        // Given - Create an invalid request that should trigger validation
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "", "", LocalDateTime.now(), LocalDateTime.now().minusDays(1), "" // Invalid: empty fields and end date before start date
        );

        // When & Then
        mockMvc.perform(post("/api/ejercicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testCrearEjercicio_UnauthorizedRole() throws Exception {
        // Given
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "Ejercicio Nuevo", "Descripción del nuevo ejercicio", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1"
        );

        // When & Then
        mockMvc.perform(post("/api/ejercicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isForbidden());
    }

    // ===== PUT /api/ejercicios/{id} - Replace exercise =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testReemplazarEjercicio_Success() throws Exception {
        // Given
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "Ejercicio Actualizado", "Descripción actualizada", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1"
        );
        
        DTOEjercicio mockEjercicio = new DTOEjercicio(
            1L, "Ejercicio Actualizado", "Descripción actualizada", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 5, 3
        );
        
        when(servicioEjercicio.actualizarEjercicio(
            eq(1L), eq("Ejercicio Actualizado"), eq("Descripción actualizada"), 
            any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(mockEjercicio);

        // When & Then
        mockMvc.perform(put("/api/ejercicios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ejercicio Actualizado"))
                .andExpect(jsonPath("$.statement").value("Descripción actualizada"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testReemplazarEjercicio_UnauthorizedRole() throws Exception {
        // Given
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "Ejercicio Actualizado", "Descripción actualizada", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1"
        );

        // When & Then
        mockMvc.perform(put("/api/ejercicios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isForbidden());
    }

    // ===== PATCH /api/ejercicios/{id} - Update exercise partially =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testActualizarEjercicioParcial_Success() throws Exception {
        // Given
        DTOPeticionCrearEjercicio peticion = new DTOPeticionCrearEjercicio(
            "Ejercicio Parcial", "Descripción parcial", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1"
        );
        
        DTOEjercicio mockEjercicio = new DTOEjercicio(
            1L, "Ejercicio Parcial", "Descripción parcial", 
            LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1", 5, 3
        );
        
        when(servicioEjercicio.actualizarEjercicio(
            eq(1L), eq("Ejercicio Parcial"), eq("Descripción parcial"), 
            any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(mockEjercicio);

        // When & Then
        mockMvc.perform(patch("/api/ejercicios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ejercicio Parcial"))
                .andExpect(jsonPath("$.statement").value("Descripción parcial"));
    }

    // ===== DELETE /api/ejercicios/{id} - Delete exercise =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testEliminarEjercicio_Success() throws Exception {
        // Given
        when(servicioEjercicio.borrarEjercicioPorId(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/ejercicios/1"))
                .andExpect(status().isNoContent());
        
        verify(servicioEjercicio).borrarEjercicioPorId(1L);
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testEliminarEjercicio_UnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/ejercicios/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarEjercicio_NotFound() throws Exception {
        // Given
        when(servicioEjercicio.borrarEjercicioPorId(999L))
            .thenThrow(new app.excepciones.ResourceNotFoundException("Ejercicio", "ID", 999L));

        // When & Then
        mockMvc.perform(delete("/api/ejercicios/999"))
                .andExpect(status().isNotFound());
    }

    // ===== Edge Cases and Error Handling =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerEjercicios_EmptyResult() throws Exception {
        // Given
        DTORespuestaPaginada<DTOEjercicio> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "ASC"
        );
        
        when(servicioEjercicio.obtenerEjerciciosPaginados(
            any(), any(), any(), any(), any(), 
            anyInt(), anyInt(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testObtenerEjercicios_InvalidPaginationParameters_Profesor() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ejercicios")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
