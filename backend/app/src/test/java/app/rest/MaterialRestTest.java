package app.rest;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioMaterial;

@ExtendWith(MockitoExtension.class)
public class MaterialRestTest {

    private MockMvc mockMvc;

    @Mock
    private ServicioMaterial servicioMaterial;

    @InjectMocks
    private MaterialRest materialRest;

    @BeforeEach
    void setUp() {
        // Setup lenient stubbing for common method calls to avoid strict stubbing issues
        lenient().when(servicioMaterial.obtenerMaterialesPaginados(
            anyString(), anyString(), anyString(), anyString(), 
            anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(DTORespuestaPaginada.of(List.of(), 0, 20, 0, "id", "ASC"));
        
        mockMvc = MockMvcBuilders.standaloneSetup(materialRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
    }

    // ===== GET /api/material - Get paginated materials =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_Success() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
            new DTOMaterial("1", "Material 1", "https://example.com/material1.pdf"),
            new DTOMaterial("2", "Material 2", "https://example.com/material2.jpg")
        );
        
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            mockMateriales, 0, 20, 2, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("Material 1"))
                .andExpect(jsonPath("$.content[0].url").value("https://example.com/material1.pdf"))
                .andExpect(jsonPath("$.content[1].id").value("2"))
                .andExpect(jsonPath("$.content[1].name").value("Material 2"))
                .andExpect(jsonPath("$.content[1].url").value("https://example.com/material2.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_WithFilters() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
            new DTOMaterial("1", "Material 1", "https://example.com/material1.pdf")
        );
        
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            mockMateriales, 0, 20, 1, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            eq("material"), eq("Material 1"), eq("https://example.com"), eq("DOCUMENT"), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("q", "material")
                .param("name", "Material 1")
                .param("url", "https://example.com")
                .param("type", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_InvalidPaginationParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_EmptyResult() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ===== GET /api/material/{id} - Get material by ID =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial("1", "Material 1", "https://example.com/material1.pdf");
        
        when(servicioMaterial.obtenerMaterialPorId("1")).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(get("/api/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Material 1"))
                .andExpect(jsonPath("$.url").value("https://example.com/material1.pdf"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_NotFound() throws Exception {
        // Given
        when(servicioMaterial.obtenerMaterialPorId("999"))
            .thenThrow(new app.excepciones.ResourceNotFoundException("Material", "ID", "999"));

        // When & Then
        mockMvc.perform(get("/api/material/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material/0"))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/material - Create new material =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearMaterial_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial("1", "Nuevo Material", "https://example.com/nuevo.pdf");
        
        when(servicioMaterial.crearMaterial(
            eq("Nuevo Material"), eq("https://example.com/nuevo.pdf")
        )).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "Nuevo Material")
                .param("url", "https://example.com/nuevo.pdf"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Nuevo Material"))
                .andExpect(jsonPath("$.url").value("https://example.com/nuevo.pdf"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearMaterial_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "")
                .param("url", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testCrearMaterial_UnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "Material")
                .param("url", "https://example.com/material.pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearMaterial_InvalidURL() throws Exception {
        // Given
        when(servicioMaterial.crearMaterial(
            anyString(), anyString()
        )).thenThrow(new app.excepciones.ValidationException("La URL del material no es válida"));

        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "Material")
                .param("url", "invalid-url"))
                .andExpect(status().isBadRequest());
    }

    // ===== PUT /api/material/{id} - Update material =====

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testActualizarMaterial_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial("1", "Material Actualizado", "https://example.com/actualizado.pdf");
        
        when(servicioMaterial.actualizarMaterial(
            eq("1"), eq("Material Actualizado"), eq("https://example.com/actualizado.pdf")
        )).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "Material Actualizado")
                .param("url", "https://example.com/actualizado.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Material Actualizado"))
                .andExpect(jsonPath("$.url").value("https://example.com/actualizado.pdf"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testActualizarMaterial_UnauthorizedRole() throws Exception {
        // When & Then - ALUMNO role should be forbidden from updating materials
        mockMvc.perform(put("/api/material/1")
                .param("name", "Material")
                .param("url", "https://example.com/material.pdf"))
                .andExpect(status().isForbidden());
        
        // Verify that the service method is never called due to authorization failure
        verify(servicioMaterial, never()).actualizarMaterial(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testActualizarMaterial_NotFound() throws Exception {
        // Given
        when(servicioMaterial.actualizarMaterial(
            eq("999"), anyString(), anyString()
        )).thenThrow(new app.excepciones.ResourceNotFoundException("Material", "ID", "999"));

        // When & Then
        mockMvc.perform(put("/api/material/999")
                .param("name", "Material")
                .param("url", "https://example.com/material.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testActualizarMaterial_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "")
                .param("url", ""))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE /api/material/{id} - Delete material =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_Success() throws Exception {
        // Given
        when(servicioMaterial.borrarMaterialPorId("1")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/material/1"))
                .andExpect(status().isNoContent());
        
        verify(servicioMaterial).borrarMaterialPorId("1");
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testEliminarMaterial_UnauthorizedRole() throws Exception {
        // When & Then - ALUMNO role should be forbidden from deleting materials
        mockMvc.perform(delete("/api/material/1"))
                .andExpect(status().isForbidden());
        
        // Verify that the service method is never called due to authorization failure
        verify(servicioMaterial, never()).borrarMaterialPorId(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_NotFound() throws Exception {
        // Given
        when(servicioMaterial.borrarMaterialPorId("999"))
            .thenThrow(new app.excepciones.ResourceNotFoundException("Material", "ID", "999"));

        // When & Then
        mockMvc.perform(delete("/api/material/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/material/0"))
                .andExpect(status().isBadRequest());
    }

    // ===== Security Tests =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_AdminAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testObtenerMateriales_ProfesorAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testObtenerMateriales_AlumnoAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    // ===== Edge Cases =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_MaxPageSize() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 100, 0, "id", "ASC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(100), eq("id"), eq("ASC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_ExceedMaxPageSize() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_InvalidSortDirection() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("sortDirection", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_ValidSortDirection() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
            List.of(), 0, 20, 0, "id", "DESC"
        );
        
        when(servicioMaterial.obtenerMaterialesPaginados(
            isNull(), isNull(), isNull(), isNull(), 
            eq(0), eq(20), eq("id"), eq("DESC")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
    }
}
