package app.rest;

import app.dtos.*;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.ENivel;
import app.repositorios.RepositorioClase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración para ClaseRest")
class ClaseRestIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RepositorioClase repositorioClase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTOPeticionCrearCurso peticionCrearCurso;
    private DTOPeticionCrearTaller peticionCrearTaller;
    private Material material;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Crear material de prueba
        material = new Material("mat1", "Apuntes de Java", "https://ejemplo.com/apuntes.pdf");

        // Crear peticiones de prueba
        peticionCrearCurso = new DTOPeticionCrearCurso(
                "Curso de Java Avanzado", "Aprende Java avanzado", new BigDecimal("129.99"),
                EPresencialidad.ONLINE, "imagen-java.jpg", ENivel.AVANZADO,
                LocalDate.now().plusDays(15), LocalDate.now().plusDays(45),
                Arrays.asList("profesor1"), Arrays.asList(material));

        peticionCrearTaller = new DTOPeticionCrearTaller(
                "Taller de Spring Boot", "Taller intensivo de Spring Boot", new BigDecimal("79.99"),
                EPresencialidad.PRESENCIAL, "imagen-spring.jpg", ENivel.INTERMEDIO,
                8, LocalDate.now().plusDays(7), LocalTime.of(16, 0),
                Arrays.asList("profesor2"), Arrays.asList());
    }

    @Test
    @DisplayName("GET /api/clases debe retornar lista vacía cuando no hay clases")
    void testObtenerClasesListaVacia() throws Exception {
        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/clases/{id} debe retornar 404 cuando la clase no existe")
    void testObtenerClasePorIdNoExiste() throws Exception {
        mockMvc.perform(get("/api/clases/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/clases/titulo/{titulo} debe retornar 404 cuando la clase no existe")
    void testObtenerClasePorTituloNoExiste() throws Exception {
        mockMvc.perform(get("/api/clases/titulo/Clase Inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/cursos debe crear un curso con rol ADMIN")
    void testCrearCursoConRolAdmin() throws Exception {
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"))
                .andExpect(jsonPath("$.descripcion").value("Aprende Java avanzado"))
                .andExpect(jsonPath("$.precio").value(129.99))
                .andExpect(jsonPath("$.presencialidad").value("ONLINE"))
                .andExpect(jsonPath("$.nivel").value("AVANZADO"))
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaFin").exists());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("POST /api/clases/cursos debe crear un curso con rol PROFESOR")
    void testCrearCursoConRolProfesor() throws Exception {
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    @DisplayName("POST /api/clases/cursos debe denegar acceso con rol ALUMNO")
    void testCrearCursoConRolAlumno() throws Exception {
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/talleres debe crear un taller con rol ADMIN")
    void testCrearTallerConRolAdmin() throws Exception {
        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearTaller)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo").value("Taller de Spring Boot"))
                .andExpect(jsonPath("$.descripcion").value("Taller intensivo de Spring Boot"))
                .andExpect(jsonPath("$.precio").value(79.99))
                .andExpect(jsonPath("$.presencialidad").value("PRESENCIAL"))
                .andExpect(jsonPath("$.nivel").value("INTERMEDIO"))
                .andExpect(jsonPath("$.duracionHoras").value(8))
                .andExpect(jsonPath("$.fechaRealizacion").exists())
                .andExpect(jsonPath("$.horaComienzo").exists());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("POST /api/clases/talleres debe crear un taller con rol PROFESOR")
    void testCrearTallerConRolProfesor() throws Exception {
        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearTaller)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo").value("Taller de Spring Boot"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/clases/{id} debe borrar clase con rol ADMIN")
    void testBorrarClasePorIdConRolAdmin() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extraer el ID de la respuesta
        String id = objectMapper.readTree(response).get("id").asText();

        // Luego borrarla
        mockMvc.perform(delete("/api/clases/" + id))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/api/clases/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    @DisplayName("DELETE /api/clases/{id} debe denegar acceso con rol PROFESOR")
    void testBorrarClasePorIdConRolProfesor() throws Exception {
        mockMvc.perform(delete("/api/clases/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/buscar debe buscar clases con parámetros válidos")
    void testBuscarClasesConParametros() throws Exception {
        // Primero crear algunas clases
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearTaller)))
                .andExpect(status().isCreated());

        // Buscar clases
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
                null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");

        mockMvc.perform(post("/api/clases/buscar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/{claseId}/alumnos/{alumnoId} debe agregar alumno a clase")
    void testAgregarAlumnoAClase() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Agregar alumno
        mockMvc.perform(post("/api/clases/" + id + "/alumnos/alumno123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnosId").isArray())
                .andExpect(jsonPath("$.alumnosId").value("alumno123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/clases/{claseId}/alumnos/{alumnoId} debe remover alumno de clase")
    void testRemoverAlumnoDeClase() throws Exception {
        // Primero crear una clase y agregar un alumno
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/clases/" + id + "/alumnos/alumno123"))
                .andExpect(status().isOk());

        // Remover alumno
        mockMvc.perform(delete("/api/clases/" + id + "/alumnos/alumno123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnosId").isArray())
                .andExpect(jsonPath("$.alumnosId").isEmpty());
    }

    @Test
    @WithMockUser(username = "alumno123", roles = "ALUMNO")
    @DisplayName("POST /api/clases/{claseId}/inscribirse debe permitir inscribirse en clase")
    void testInscribirseEnClase() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Inscribirse en la clase
        mockMvc.perform(post("/api/clases/" + id + "/inscribirse"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnosId").isArray())
                .andExpect(jsonPath("$.alumnosId").value("alumno123"));
    }

    @Test
    @WithMockUser(username = "alumno123", roles = "ALUMNO")
    @DisplayName("DELETE /api/clases/{claseId}/darse-baja debe permitir darse de baja de clase")
    void testDarseDeBajaDeClase() throws Exception {
        // Primero crear una clase e inscribirse
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/clases/" + id + "/inscribirse"))
                .andExpect(status().isOk());

        // Darse de baja
        mockMvc.perform(delete("/api/clases/" + id + "/darse-baja"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.alumnosId").isArray())
                .andExpect(jsonPath("$.alumnosId").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/{claseId}/profesores/{profesorId} debe agregar profesor a clase")
    void testAgregarProfesorAClase() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Agregar profesor
        mockMvc.perform(post("/api/clases/" + id + "/profesores/profesor456"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profesoresId").isArray())
                .andExpect(jsonPath("$.profesoresId").value("profesor456"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/{claseId}/material debe agregar material a clase")
    void testAgregarMaterialAClase() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Agregar material
        Material nuevoMaterial = new Material("mat2", "Ejercicios prácticos", "https://ejemplo.com/ejercicios.pdf");
        
        mockMvc.perform(post("/api/clases/" + id + "/material")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMaterial)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.material").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clases/{claseId}/alumnos/contar debe contar alumnos en clase")
    void testContarAlumnosEnClase() throws Exception {
        // Primero crear una clase y agregar alumnos
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/clases/" + id + "/alumnos/alumno1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/clases/" + id + "/alumnos/alumno2"))
                .andExpect(status().isOk());

        // Contar alumnos
        mockMvc.perform(get("/api/clases/" + id + "/alumnos/contar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clases/{claseId}/profesores/contar debe contar profesores en clase")
    void testContarProfesoresEnClase() throws Exception {
        // Primero crear una clase y agregar profesores
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/clases/" + id + "/profesores/profesor1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/clases/" + id + "/profesores/profesor2"))
                .andExpect(status().isOk());

        // Contar profesores
        mockMvc.perform(get("/api/clases/" + id + "/profesores/contar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @DisplayName("Validación de petición de crear curso con datos inválidos")
    void testCrearCursoDatosInvalidos() throws Exception {
        DTOPeticionCrearCurso peticionInvalida = new DTOPeticionCrearCurso(
                "", // título vacío
                "Descripción", 
                new BigDecimal("-10"), // precio negativo
                EPresencialidad.ONLINE, 
                "imagen.jpg", 
                ENivel.PRINCIPIANTE,
                LocalDate.now().minusDays(1), // fecha en el pasado
                LocalDate.now().minusDays(2), // fecha fin antes que inicio
                Arrays.asList(), 
                Arrays.asList());

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validación de petición de crear taller con datos inválidos")
    void testCrearTallerDatosInvalidos() throws Exception {
        DTOPeticionCrearTaller peticionInvalida = new DTOPeticionCrearTaller(
                "", // título vacío
                "Descripción", 
                new BigDecimal("-5"), // precio negativo
                EPresencialidad.PRESENCIAL, 
                "imagen.jpg", 
                ENivel.INTERMEDIO,
                0, // duración inválida
                LocalDate.now().minusDays(1), // fecha en el pasado
                LocalTime.of(10, 0),
                Arrays.asList(), 
                Arrays.asList());

        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());
    }
}
