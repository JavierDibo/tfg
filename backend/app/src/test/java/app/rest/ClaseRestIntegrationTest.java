package app.rest;

import app.dtos.*;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

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
                EPresencialidad.ONLINE, "imagen-java.jpg", EDificultad.AVANZADO,
                LocalDate.now().plusDays(15), LocalDate.now().plusDays(45),
                Arrays.asList("profesor1"), Arrays.asList(material));

        peticionCrearTaller = new DTOPeticionCrearTaller(
                "Taller de Spring Boot", "Taller intensivo de Spring Boot", new BigDecimal("79.99"),
                EPresencialidad.PRESENCIAL, "imagen-spring.jpg", EDificultad.INTERMEDIO,
                8, LocalDate.now().plusDays(7), LocalTime.of(16, 0),
                Arrays.asList("profesor2"), Arrays.asList());
    }

    @Test
    @DisplayName("GET /api/clases debe retornar lista de clases")
    void testObtenerClases() throws Exception {
        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"))
                .andExpect(jsonPath("$.descripcion").value("Aprende Java avanzado"))
                .andExpect(jsonPath("$.precio").value(129.99))
                .andExpect(jsonPath("$.presencialidad").value("ONLINE"))
                .andExpect(jsonPath("$.nivel").value("AVANZADO"))
                .andExpect(jsonPath("$.profesoresId").isArray())
                .andExpect(jsonPath("$.alumnosId").isEmpty());
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"))
                .andExpect(jsonPath("$.descripcion").value("Aprende Java avanzado"))
                .andExpect(jsonPath("$.precio").value(129.99))
                .andExpect(jsonPath("$.presencialidad").value("ONLINE"))
                .andExpect(jsonPath("$.nivel").value("AVANZADO"));
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
    @DisplayName("POST /api/clases/talleres debe crear un taller")
    void testCrearTaller() throws Exception {
        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearTaller)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Taller de Spring Boot"))
                .andExpect(jsonPath("$.descripcion").value("Taller intensivo de Spring Boot"))
                .andExpect(jsonPath("$.precio").value(79.99))
                .andExpect(jsonPath("$.presencialidad").value("PRESENCIAL"))
                .andExpect(jsonPath("$.nivel").value("INTERMEDIO"))
                .andExpect(jsonPath("$.duracionHoras").value(8))
                .andExpect(jsonPath("$.profesoresId").isArray())
                .andExpect(jsonPath("$.alumnosId").isEmpty());
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
        // Primero crear una clase con rol ADMIN
        String response = mockMvc.perform(post("/api/clases/cursos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Intentar borrar la clase con rol PROFESOR (debe ser denegado)
        mockMvc.perform(delete("/api/clases/" + id))
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

        // Buscar clases con parámetros específicos
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
                "Java", null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");

        mockMvc.perform(post("/api/clases/buscar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parametros)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.numeroPagina").exists())
                .andExpect(jsonPath("$.tamanoPagina").exists())
                .andExpect(jsonPath("$.totalElementos").exists());
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
    @DisplayName("POST /api/enrollments/{claseId}/self-enroll debe permitir a un alumno inscribirse en una clase")
    void testInscribirseEnClase() throws Exception {
        // Primero crear una clase para asegurar que existe
        String response = mockMvc.perform(post("/api/clases/cursos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String claseId = objectMapper.readTree(response).get("id").asText();

        // Inscribirse en la clase
        mockMvc.perform(post("/api/enrollments/" + claseId + "/self-enroll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "alumno123", roles = "ALUMNO")
    @DisplayName("DELETE /api/enrollments/{claseId}/self-unenroll debe permitir a un alumno darse de baja de una clase")
    void testDarseDeBajaDeClase() throws Exception {
        // Primero crear una clase y inscribirse
        String response = mockMvc.perform(post("/api/clases/cursos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String claseId = objectMapper.readTree(response).get("id").asText();

        // Inscribirse primero
        mockMvc.perform(post("/api/enrollments/" + claseId + "/self-enroll"))
                .andExpect(status().isOk());

        // Darse de baja
        mockMvc.perform(delete("/api/enrollments/" + claseId + "/self-unenroll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists());
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

        // Agregar profesor usando el endpoint correcto
        mockMvc.perform(post("/api/clases/" + id + "/profesores/profesor456"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profesoresId").isArray())
                .andExpect(jsonPath("$.profesoresId").value(org.hamcrest.Matchers.hasItem("profesor456")));
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
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Contar alumnos (debe ser 0 inicialmente)
        mockMvc.perform(get("/api/clases/" + id + "/alumnos/contar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(0));

        // Agregar un alumno y verificar que el conteo cambia
        mockMvc.perform(post("/api/clases/" + id + "/alumnos/alumno123"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clases/" + id + "/alumnos/contar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clases/{claseId}/profesores/contar debe contar profesores en clase")
    void testContarProfesoresEnClase() throws Exception {
        // Primero crear una clase
        String response = mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Contar profesores (debe ser 1 inicialmente por el profesor en la petición)
        mockMvc.perform(get("/api/clases/" + id + "/profesores/contar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(1));

        // Agregar otro profesor y verificar que el conteo cambia
        mockMvc.perform(post("/api/clases/" + id + "/profesores/profesor456"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clases/" + id + "/profesores/contar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @DisplayName("GET /api/clases/alumno/{alumnoId} debe obtener clases por alumno")
    void testObtenerClasesPorAlumno() throws Exception {
        // Primero crear una clase y agregar un alumno
        String response = mockMvc.perform(post("/api/clases/cursos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String claseId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/clases/" + claseId + "/alumnos/alumno123")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        // Obtener clases del alumno
        mockMvc.perform(get("/api/clases/alumno/alumno123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(claseId));
    }

    @Test
    @DisplayName("GET /api/clases/profesor/{profesorId} debe obtener clases por profesor")
    void testObtenerClasesPorProfesor() throws Exception {
        // Primero crear una clase con un profesor específico
        DTOPeticionCrearCurso cursoConProfesor = new DTOPeticionCrearCurso(
                "Curso de Python", "Aprende Python", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen-python.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(40),
                Arrays.asList("profesorEspecifico"), Arrays.asList());

        String response = mockMvc.perform(post("/api/clases/cursos")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cursoConProfesor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String claseId = objectMapper.readTree(response).get("id").asText();

        // Obtener clases del profesor
        mockMvc.perform(get("/api/clases/profesor/profesorEspecifico"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(claseId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/cursos debe crear múltiples cursos")
    void testCrearMultiplesCursos() throws Exception {
        // Crear primer curso
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated());

        // Crear segundo curso
        DTOPeticionCrearCurso segundoCurso = new DTOPeticionCrearCurso(
                "Curso de Python", "Aprende Python desde cero", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen-python.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(40),
                Arrays.asList("profesor2"), Arrays.asList());

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundoCurso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Curso de Python"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clases/cursos debe validar datos inválidos")
    void testCrearCursoConDatosInvalidos() throws Exception {
        DTOPeticionCrearCurso peticionInvalida = new DTOPeticionCrearCurso(
                "Curso Inválido", "Curso con datos inválidos", new BigDecimal("-10"),
                EPresencialidad.ONLINE, "imagen.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(2),
                Arrays.asList("profesor1"), Arrays.asList());

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());
    }
}
