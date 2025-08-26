package app.rest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;

import app.dtos.DTOAlumno;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.entidades.Usuario;
import app.repositorios.RepositorioAlumno;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioJwt;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AlumnoRestTest {

    private MockMvc mockMvc;

    @Mock
    private ServicioAlumno servicioAlumno;
    
    @Mock
    private RepositorioAlumno repositorioAlumno;
    
    @Mock
    private ServicioJwt servicioJwt;

    @InjectMocks
    private AlumnoRest alumnoRest;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alumnoRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }
    
    // Utility method to generate valid DNIs using the same algorithm as the system
    private String generateValidDNI(int index) {
        StringBuilder dni = new StringBuilder();
        String indexStr = String.valueOf(index);
        
        // Fill with random digits to make it 8 digits
        while (dni.length() + indexStr.length() < 8) {
            dni.append((int)(Math.random() * 10));
        }
        dni.append(indexStr);
        
        // Ensure it's exactly 8 digits
        if (dni.length() > 8) {
            dni.setLength(8);
        }
        
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        int numero = Integer.parseInt(dni.toString());
        dni.append(letras[numero % 23]);
        return dni.toString();
    }

    @Test
    public void testObtenerAlumnosPaginadosConParametroQ() throws Exception {
        // Given
        String searchTerm = "john";
        List<DTOAlumno> mockAlumnos = Arrays.asList(
            new DTOAlumno(1L, "alumno1", "John", "Doe", generateValidDNI(1), "john.doe@email.com", 
                         "+34612345678", LocalDateTime.now(), true, true, 
                         Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Role.ALUMNO),
            new DTOAlumno(2L, "alumno2", "Johnny", "Smith", generateValidDNI(2), "johnny.smith@email.com", 
                         "+34687654321", LocalDateTime.now(), true, true, 
                         Arrays.asList("1"), Arrays.asList("2"), Arrays.asList("2"), Usuario.Role.ALUMNO)
        );
        
        DTORespuestaPaginada<DTOAlumno> mockResponse = DTORespuestaPaginada.of(
            mockAlumnos, 0, 20, 2L, "id", "ASC"
        );
        
        when(servicioAlumno.buscarAlumnosPorParametrosPaginados(
            any(DTOParametrosBusquedaAlumno.class), anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(get("/api/alumnos")
                .param("q", searchTerm)
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(1));
        
        // Verify that the service was called with the correct parameters
        verify(servicioAlumno).buscarAlumnosPorParametrosPaginados(
            argThat(parametros -> parametros.hasGeneralSearch() && parametros.q().equals(searchTerm)),
            eq(0), eq(20), eq("id"), eq("ASC")
        );
    }
    
    @Test
    public void testObtenerAlumnosPaginadosConParametroQYFiltrosEspecificos() throws Exception {
        // Given
        String searchTerm = "john";
        String nombre = "John";
        Boolean matriculado = true;
        
        List<DTOAlumno> mockAlumnos = Arrays.asList(
            new DTOAlumno(1L, "alumno1", "John", "Doe", generateValidDNI(1), "john.doe@email.com", 
                         "+34612345678", LocalDateTime.now(), true, true, 
                         Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Role.ALUMNO)
        );
        
        DTORespuestaPaginada<DTOAlumno> mockResponse = DTORespuestaPaginada.of(
            mockAlumnos, 0, 20, 1L, "id", "ASC"
        );
        
        when(servicioAlumno.buscarAlumnosPorParametrosPaginados(
            any(DTOParametrosBusquedaAlumno.class), anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(get("/api/alumnos")
                .param("q", searchTerm)
                .param("nombre", nombre)
                .param("matriculado", matriculado.toString())
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
        
        // Verify that the service was called with the correct parameters
        verify(servicioAlumno).buscarAlumnosPorParametrosPaginados(
            argThat(parametros -> 
                parametros.hasGeneralSearch() && 
                parametros.q().equals(searchTerm) &&
                parametros.hasSpecificFilters() &&
                parametros.firstName().equals(nombre) &&
                parametros.enrolled().equals(matriculado)
            ),
            eq(0), eq(20), eq("id"), eq("ASC")
        );
    }
    
    @Test
    public void testObtenerAlumnosPaginadosSinParametroQ() throws Exception {
        // Given
        String nombre = "John";
        
        List<DTOAlumno> mockAlumnos = Arrays.asList(
            new DTOAlumno(1L, "alumno1", "John", "Doe", generateValidDNI(1), "john.doe@email.com", 
                         "+34612345678", LocalDateTime.now(), true, true, 
                         Arrays.asList("1", "2"), Arrays.asList("1"), Arrays.asList("1"), Usuario.Role.ALUMNO)
        );
        
        DTORespuestaPaginada<DTOAlumno> mockResponse = DTORespuestaPaginada.of(
            mockAlumnos, 0, 20, 1L, "id", "ASC"
        );
        
        when(servicioAlumno.buscarAlumnosPorParametrosPaginados(
            any(DTOParametrosBusquedaAlumno.class), anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(get("/api/alumnos")
                .param("nombre", nombre)
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
        
        // Verify that the service was called with the correct parameters
        verify(servicioAlumno).buscarAlumnosPorParametrosPaginados(
            argThat(parametros -> 
                !parametros.hasGeneralSearch() && 
                parametros.hasSpecificFilters() &&
                parametros.firstName().equals(nombre)
            ),
            eq(0), eq(20), eq("id"), eq("ASC")
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActualizarAlumnoConEstadoMatriculaYHabilitado() throws Exception {
        // Given
        Long alumnoId = 1L;
        DTOActualizacionAlumno dtoActualizacion = new DTOActualizacionAlumno(
            null, null, null, null, null, true, false
        );
        
        Alumno alumno = new Alumno("testuser", "password", "Test", "User", "12345678A", "test@test.com", "123456789");
        alumno.setId(alumnoId);
        alumno.setEnrolled(true);  // Updated to true
        alumno.setEnabled(false);  // Updated to false
        
        DTOAlumno dtoAlumno = new DTOAlumno(alumno);
        
        when(servicioAlumno.actualizarAlumno(eq(alumnoId), eq(dtoActualizacion))).thenReturn(dtoAlumno);
        
        // When & Then
        mockMvc.perform(patch("/api/alumnos/{id}", alumnoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoActualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolled").value(true))
                .andExpect(jsonPath("$.enabled").value(false));
        
        // Verify that the service method was called with the correct parameters
        verify(servicioAlumno).actualizarAlumno(eq(alumnoId), eq(dtoActualizacion));
    }
}



