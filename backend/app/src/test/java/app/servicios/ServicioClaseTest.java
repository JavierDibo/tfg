package app.servicios;

import app.dtos.*;
import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Material;
import app.entidades.Taller;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import app.excepciones.ResourceNotFoundException;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioProfesor;
import app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import app.dtos.DTOClaseConDetallesPublico;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOProfesorPublico;
import app.entidades.Profesor;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioClase")
class ServicioClaseTest {

    @Mock
    private RepositorioAlumno repositorioAlumno;

    @Mock
    private RepositorioClase repositorioClase;

    @Mock
    private RepositorioProfesor repositorioProfesor;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioClase servicioClase;

    private Curso curso;
    private Taller taller;
    private DTOPeticionCrearClase peticionCrearClase;
    private Material material;
    private Profesor profesor;

    @BeforeEach
    void setUp() {
        // Mock SecurityUtils behavior with lenient stubbing
        lenient().when(securityUtils.isAdmin()).thenReturn(true);
        lenient().when(securityUtils.isProfessor()).thenReturn(false);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(1L);
        lenient().when(securityUtils.hasRole(anyString())).thenReturn(true);
        
        material = new Material("mat1", "Apuntes de Java", "https://ejemplo.com/apuntes.pdf");
        
        curso = new Curso(
                "Curso de Java", "Aprende Java desde cero", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen1.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(30)
        );
        curso.setId(1L);
        curso.agregarAlumno("alumno1");
        curso.agregarProfesor("profesor1");

        taller = new Taller(
                "Taller de Spring", "Taller intensivo de Spring Boot", new BigDecimal("49.99"),
                EPresencialidad.PRESENCIAL, "imagen2.jpg", EDificultad.INTERMEDIO,
                4, LocalDate.now().plusDays(3), LocalTime.of(10, 0)
        );
        taller.setId(2L);
        taller.agregarAlumno("alumno2");
        taller.agregarProfesor("profesor2");

        peticionCrearClase = new DTOPeticionCrearClase(
                "Nuevo Curso", "Descripción del curso", new BigDecimal("89.99"),
                EPresencialidad.ONLINE, "nueva-imagen.jpg", EDificultad.PRINCIPIANTE,
                Arrays.asList("profesor1"), Arrays.asList(material)
        );

        // Crear profesor de prueba
        profesor = new Profesor("prof1", "password", "Luis", "Muñoz López", "12345678A", "prof1@academia.com", "647940540");
        profesor.setId(3L);
        profesor.setEnabled(true);
    }

    @Test
    @DisplayName("obtenerClases debe retornar todas las clases ordenadas")
    void testObtenerClases() {
        List<Clase> clases = Arrays.asList(curso, taller);
        when(repositorioClase.findAllOrderedById()).thenReturn(clases);

        List<DTOClase> resultado = servicioClase.obtenerClases();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).titulo());
        assertEquals("Taller de Spring", resultado.get(1).titulo());
        verify(repositorioClase).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerClasePorId debe retornar la clase cuando existe")
    void testObtenerClasePorIdExiste() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));

        DTOClase resultado = servicioClase.obtenerClasePorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Curso de Java", resultado.titulo());
        verify(repositorioClase).findById(1L);
    }

    @Test
    @DisplayName("obtenerClasePorId debe lanzar excepción cuando no existe")
    void testObtenerClasePorIdNoExiste() {
        when(repositorioClase.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioClase.obtenerClasePorId(999L);
        });

        verify(repositorioClase).findById(999L);
    }

    @Test
    @DisplayName("obtenerClasePorTitulo debe retornar la clase cuando existe")
    void testObtenerClasePorTituloExiste() {
        when(repositorioClase.findByTitle("Curso de Java")).thenReturn(Optional.of(curso));

        DTOClase resultado = servicioClase.obtenerClasePorTitulo("Curso de Java");

        assertNotNull(resultado);
        assertEquals("Curso de Java", resultado.titulo());
        verify(repositorioClase).findByTitle("Curso de Java");
    }

    @Test
    @DisplayName("obtenerClasePorTitulo debe lanzar excepción cuando no existe")
    void testObtenerClasePorTituloNoExiste() {
        when(repositorioClase.findByTitle("Clase Inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioClase.obtenerClasePorTitulo("Clase Inexistente");
        });

        verify(repositorioClase).findByTitle("Clase Inexistente");
    }

    @Test
    @DisplayName("crearCurso debe crear y guardar un curso correctamente")
    void testCrearCurso() {
        LocalDate fechaInicio = LocalDate.now().plusDays(10);
        LocalDate fechaFin = LocalDate.now().plusDays(40);
        
        Curso cursoCreado = new Curso(
                peticionCrearClase.titulo(), peticionCrearClase.descripcion(), peticionCrearClase.precio(),
                peticionCrearClase.presencialidad(), peticionCrearClase.imagenPortada(), peticionCrearClase.nivel(),
                fechaInicio, fechaFin
        );
        cursoCreado.setId(1L);
        
        when(repositorioClase.save(any(Curso.class))).thenReturn(cursoCreado);

        DTOCurso resultado = servicioClase.crearCurso(peticionCrearClase, fechaInicio, fechaFin);

        assertNotNull(resultado);
        assertEquals("Nuevo Curso", resultado.titulo());
        assertEquals(fechaInicio, resultado.fechaInicio());
        assertEquals(fechaFin, resultado.fechaFin());
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("crearTaller debe crear y guardar un taller correctamente")
    void testCrearTaller() {
        Integer duracionHoras = 6;
        LocalDate fechaRealizacion = LocalDate.now().plusDays(5);
        LocalTime horaComienzo = LocalTime.of(14, 0);
        
        Taller tallerCreado = new Taller(
                peticionCrearClase.titulo(), peticionCrearClase.descripcion(), peticionCrearClase.precio(),
                peticionCrearClase.presencialidad(), peticionCrearClase.imagenPortada(), peticionCrearClase.nivel(),
                duracionHoras, fechaRealizacion, horaComienzo
        );
        tallerCreado.setId(2L);
        
        when(repositorioClase.save(any(Taller.class))).thenReturn(tallerCreado);

        DTOTaller resultado = servicioClase.crearTaller(peticionCrearClase, duracionHoras, fechaRealizacion, horaComienzo);

        assertNotNull(resultado);
        assertEquals("Nuevo Curso", resultado.titulo());
        assertEquals(duracionHoras, resultado.duracionHoras());
        assertEquals(fechaRealizacion, resultado.fechaRealizacion());
        assertEquals(horaComienzo, resultado.horaComienzo());
        verify(repositorioClase).save(any(Taller.class));
    }

    @Test
    @DisplayName("borrarClasePorId debe retornar true cuando la clase existe")
    void testBorrarClasePorIdExiste() {
        when(repositorioClase.existsById(1L)).thenReturn(true);

        boolean resultado = servicioClase.borrarClasePorId(1L);

        assertTrue(resultado);
        verify(repositorioClase).existsById(1L);
        verify(repositorioClase).deleteById(1L);
    }

    @Test
    @DisplayName("borrarClasePorId debe retornar false cuando la clase no existe")
    void testBorrarClasePorIdNoExiste() {
        when(repositorioClase.existsById(999L)).thenReturn(false);

        boolean resultado = servicioClase.borrarClasePorId(999L);

        assertFalse(resultado);
        verify(repositorioClase).existsById(999L);
        verify(repositorioClase, never()).deleteById(any());
    }

    @Test
    @DisplayName("borrarClasePorTitulo debe retornar true cuando la clase existe")
    void testBorrarClasePorTituloExiste() {
        when(repositorioClase.findByTitle("Curso de Java")).thenReturn(Optional.of(curso));

        boolean resultado = servicioClase.borrarClasePorTitulo("Curso de Java");

        assertTrue(resultado);
        verify(repositorioClase).findByTitle("Curso de Java");
        verify(repositorioClase).delete(curso);
    }

    @Test
    @DisplayName("borrarClasePorTitulo debe retornar false cuando la clase no existe")
    void testBorrarClasePorTituloNoExiste() {
        when(repositorioClase.findByTitle("Clase Inexistente")).thenReturn(Optional.empty());

        boolean resultado = servicioClase.borrarClasePorTitulo("Clase Inexistente");

        assertFalse(resultado);
        verify(repositorioClase).findByTitle("Clase Inexistente");
        verify(repositorioClase, never()).delete(any());
    }

    @Test
    @DisplayName("buscarClases debe retornar resultados paginados")
    void testBuscarClases() {
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
                "Java", null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        when(repositorioClase.findByGeneralSearch(eq("Java"), any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Curso de Java", resultado.content().get(0).titulo());
        verify(repositorioClase).findByGeneralSearch(eq("Java"), any(Pageable.class));
    }

    @Test
    @DisplayName("agregarAlumno debe agregar alumno a la clase")
    void testAgregarAlumno() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.agregarAlumno(1L, "alumno3");

        assertNotNull(resultado);
        assertTrue(resultado.alumnosId().contains("alumno3"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("agregarAlumno debe lanzar excepción cuando la clase no existe")
    void testAgregarAlumnoClaseNoExiste() {
        when(repositorioClase.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioClase.agregarAlumno(999L, "alumno3");
        });

        verify(repositorioClase).findById(999L);
        verify(repositorioClase, never()).save(any());
    }

    @Test
    @DisplayName("removerAlumno debe remover alumno de la clase")
    void testRemoverAlumno() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.removerAlumno(1L, "alumno1");

        assertNotNull(resultado);
        assertFalse(resultado.alumnosId().contains("alumno1"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("agregarProfesor debe agregar profesor a la clase")
    void testAgregarProfesor() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.agregarProfesor(1L, "profesor3");

        assertNotNull(resultado);
        assertTrue(resultado.profesoresId().contains("profesor3"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("removerProfesor debe remover profesor de la clase")
    void testRemoverProfesor() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.removerProfesor(1L, "profesor1");

        assertNotNull(resultado);
        assertFalse(resultado.profesoresId().contains("profesor1"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("agregarEjercicio debe agregar ejercicio a la clase")
    void testAgregarEjercicio() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.agregarEjercicio(1L, "ejercicio4");

        assertNotNull(resultado);
        assertTrue(resultado.ejerciciosId().contains("ejercicio4"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("removerEjercicio debe remover ejercicio de la clase")
    void testRemoverEjercicio() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.removerEjercicio(1L, "ejercicio1");

        assertNotNull(resultado);
        assertFalse(resultado.ejerciciosId().contains("ejercicio1"));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("agregarMaterial debe agregar material a la clase")
    void testAgregarMaterial() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        Material nuevoMaterial = new Material("mat2", "Ejercicios prácticos", "https://ejemplo.com/ejercicios.pdf");
        DTOClase resultado = servicioClase.agregarMaterial(1L, nuevoMaterial);

        assertNotNull(resultado);
        assertTrue(resultado.material().contains(nuevoMaterial));
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("removerMaterial debe remover material de la clase")
    void testRemoverMaterial() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);

        DTOClase resultado = servicioClase.removerMaterial(1L, "mat1");

        assertNotNull(resultado);
        assertTrue(resultado.material().isEmpty());
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Curso.class));
    }

    @Test
    @DisplayName("obtenerClasesPorAlumno debe retornar clases del alumno")
    void testObtenerClasesPorAlumno() {
        List<Clase> clases = Arrays.asList(curso, taller);
        when(repositorioClase.findByAlumnoId("alumno1")).thenReturn(clases);

        List<DTOClase> resultado = servicioClase.obtenerClasesPorAlumno("alumno1");

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repositorioClase).findByAlumnoId("alumno1");
    }

    @Test
    @DisplayName("obtenerClasesPorProfesor debe retornar clases del profesor")
    void testObtenerClasesPorProfesor() {
        List<Clase> clases = Arrays.asList(curso);
        when(repositorioClase.findByProfesorId("profesor1")).thenReturn(clases);

        List<DTOClase> resultado = servicioClase.obtenerClasesPorProfesor("profesor1");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).titulo());
        verify(repositorioClase).findByProfesorId("profesor1");
    }

    @Test
    @DisplayName("contarAlumnosEnClase debe retornar el número correcto de alumnos")
    void testContarAlumnosEnClase() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.countAlumnosByClaseId(1L)).thenReturn(2);

        Integer resultado = servicioClase.contarAlumnosEnClase(1L);

        assertEquals(2, resultado);
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).countAlumnosByClaseId(1L);
    }

    @Test
    @DisplayName("contarProfesoresEnClase debe retornar el número correcto de profesores")
    void testContarProfesoresEnClase() {
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioClase.countProfesoresByClaseId(1L)).thenReturn(1);

        Integer resultado = servicioClase.contarProfesoresEnClase(1L);

        assertEquals(1, resultado);
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).countProfesoresByClaseId(1L);
    }

    // ===== TESTS PARA BIDIRECTIONAL RELATIONSHIP =====

    @Test
    @DisplayName("Crear curso debe actualizar la lista de clases del profesor")
    void testCrearCursoActualizaProfesorClases() {
        // Configurar mocks
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);
        when(repositorioProfesor.findById(3L)).thenReturn(Optional.of(profesor));
        when(repositorioProfesor.save(any(Profesor.class))).thenReturn(profesor);
        
        // Ejecutar método
        servicioClase.crearCurso(peticionCrearClase, LocalDate.now(), LocalDate.now().plusMonths(3));
        
        // Verificar que se llamó a agregarClase en el profesor
        verify(repositorioProfesor, times(1)).findById(3L);
        verify(repositorioProfesor, times(1)).save(any(Profesor.class));
        
        // Verificar que el profesor tiene la clase en su lista
        assertTrue(profesor.getClasesId().contains("1"));
        assertEquals(1, profesor.getClasesId().size());
    }

    @Test
    @DisplayName("DTOProfesorPublico debe mostrar classCount correcto después de crear curso")
    void testDTOProfesorPublicoClassCountCorrecto() {
        // Configurar mocks
        when(repositorioClase.save(any(Curso.class))).thenReturn(curso);
        when(repositorioProfesor.findById(3L)).thenReturn(Optional.of(profesor));
        when(repositorioProfesor.save(any(Profesor.class))).thenReturn(profesor);
        
        // Ejecutar método
        servicioClase.crearCurso(peticionCrearClase, LocalDate.now(), LocalDate.now().plusMonths(3));
        
        // Crear DTOProfesorPublico y verificar classCount
        DTOProfesorPublico dtoProfesor = new DTOProfesorPublico(profesor);
        assertEquals(1, dtoProfesor.classCount());
        assertTrue(dtoProfesor.hasClasses());
    }

    @Test
    @DisplayName("Agregar profesor a clase debe actualizar la lista de clases del profesor")
    void testAgregarProfesorActualizaProfesorClases() {
        // Configurar mocks
        when(repositorioClase.findById(2L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Clase.class))).thenReturn(curso);
        when(repositorioProfesor.findById(3L)).thenReturn(Optional.of(profesor));
        when(repositorioProfesor.save(any(Profesor.class))).thenReturn(profesor);
        when(securityUtils.isAdmin()).thenReturn(true);
        
        // Ejecutar método
        servicioClase.agregarProfesor(2L, "3");
        
        // Verificar que se llamó a agregarClase en el profesor
        verify(repositorioProfesor, times(1)).findById(3L);
        verify(repositorioProfesor, times(1)).save(any(Profesor.class));
        
        // Verificar que el profesor tiene la clase en su lista
        assertTrue(profesor.getClasesId().contains("2"));
        assertEquals(1, profesor.getClasesId().size());
    }

    @Test
    @DisplayName("Remover profesor de clase debe actualizar la lista de clases del profesor")
    void testRemoverProfesorActualizaProfesorClases() {
        // Agregar clase al profesor primero
        profesor.agregarClase("2");
        
        // Configurar mocks
        when(repositorioClase.findById(2L)).thenReturn(Optional.of(curso));
        when(repositorioClase.save(any(Clase.class))).thenReturn(curso);
        when(repositorioProfesor.findById(3L)).thenReturn(Optional.of(profesor));
        when(repositorioProfesor.save(any(Profesor.class))).thenReturn(profesor);
        when(securityUtils.isAdmin()).thenReturn(true);
        
        // Ejecutar método
        servicioClase.removerProfesor(2L, "3");
        
        // Verificar que se llamó a removerClase en el profesor
        verify(repositorioProfesor, times(1)).findById(3L);
        verify(repositorioProfesor, times(1)).save(any(Profesor.class));
        
        // Verificar que el profesor ya no tiene la clase en su lista
        assertFalse(profesor.getClasesId().contains("2"));
        assertEquals(0, profesor.getClasesId().size());
    }

    // ===== TESTS PARA FILTRADO FLEXIBLE =====

    @Test
    @DisplayName("buscarClases con filtros combinados debe usar findByGeneralAndSpecificFilters")
    void testBuscarClasesConFiltrosCombinados() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", "Programación", "Aprende Java", EPresencialidad.ONLINE, 
            EDificultad.INTERMEDIO, new BigDecimal("50"), new BigDecimal("100"), 
            null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        // Mock the flexible filtering method
        when(repositorioClase.findByGeneralAndSpecificFilters(
            eq("Java"), eq("Programación"), eq("Aprende Java"), 
            eq(EPresencialidad.ONLINE), eq(EDificultad.INTERMEDIO),
            eq(new BigDecimal("50")), eq(new BigDecimal("100")), 
            any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Curso de Java", resultado.content().get(0).titulo());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioClase).findByGeneralAndSpecificFilters(
            eq("Java"), eq("Programación"), eq("Aprende Java"), eq(EPresencialidad.ONLINE), 
            eq(EDificultad.INTERMEDIO), eq(new BigDecimal("50")), eq(new BigDecimal("100")), any(Pageable.class));
    }

    @Test
    @DisplayName("buscarClases solo con búsqueda general debe usar findByGeneralSearch")
    void testBuscarClasesSoloConBusquedaGeneral() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        // Mock the general search method
        when(repositorioClase.findByGeneralSearch(eq("Java"), any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Curso de Java", resultado.content().get(0).titulo());
        
        // Verify that the general search method was called
        verify(repositorioClase).findByGeneralSearch(eq("Java"), any(Pageable.class));
        verify(repositorioClase, never()).findByGeneralAndSpecificFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("buscarClases solo con filtros específicos debe usar findByGeneralAndSpecificFilters")
    void testBuscarClasesSoloConFiltrosEspecificos() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            null, "Programación", "Aprende", EPresencialidad.ONLINE, 
            EDificultad.INTERMEDIO, new BigDecimal("50"), new BigDecimal("100"), 
            null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        // Mock the flexible filtering method
        when(repositorioClase.findByGeneralAndSpecificFilters(
            isNull(), eq("Programación"), eq("Aprende"), 
            eq(EPresencialidad.ONLINE), eq(EDificultad.INTERMEDIO),
            eq(new BigDecimal("50")), eq(new BigDecimal("100")), 
            any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the flexible filtering method was called with null for general search
        verify(repositorioClase).findByGeneralAndSpecificFilters(
            isNull(), eq("Programación"), eq("Aprende"), eq(EPresencialidad.ONLINE), 
            eq(EDificultad.INTERMEDIO), eq(new BigDecimal("50")), eq(new BigDecimal("100")), any(Pageable.class));
    }

    @Test
    @DisplayName("buscarClases sin filtros debe retornar todas las clases")
    void testBuscarClasesSinFiltros() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            null, null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> todasLasClases = Arrays.asList(curso, taller);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(todasLasClases, pageable, 2);
        
        // Mock findAllOrderedById for no filters case
        when(repositorioClase.findAllOrderedById()).thenReturn(todasLasClases);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that findAllOrderedById was called
        verify(repositorioClase).findAllOrderedById();
        verify(repositorioClase, never()).findByGeneralSearch(any(), any());
        verify(repositorioClase, never()).findByGeneralAndSpecificFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("buscarClases con paginación debe aplicar correctamente")
    void testBuscarClasesConPaginacion() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", null, null, null, null, null, null, null, null, 2, 5, "titulo", "DESC");
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(2, 5);
        Page<Clase> page = new PageImpl<>(clases, pageable, 25);
        
        // Mock the general search method
        when(repositorioClase.findByGeneralSearch(eq("Java"), any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.page());
        assertEquals(5, resultado.size());
        assertEquals(25, resultado.totalElements());
        assertEquals(5, resultado.totalPages());
        assertEquals("titulo", resultado.sortBy());
        assertEquals("DESC", resultado.sortDirection());
        
        // Verify that the method was called with correct pagination
        verify(repositorioClase).findByGeneralSearch(eq("Java"), any(Pageable.class));
    }

    @Test
    @DisplayName("buscarClases con filtros vacíos debe tratarlos como null")
    void testBuscarClasesConFiltrosVacios() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "", "", "", null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        List<Clase> todasLasClases = Arrays.asList(curso, taller);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(todasLasClases, pageable, 2);
        
        // Mock findAllOrderedById for no filters case
        when(repositorioClase.findAllOrderedById()).thenReturn(todasLasClases);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClases(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that findAllOrderedById was called (treating empty filters as no filters)
        verify(repositorioClase).findAllOrderedById();
    }

    @Test
    @DisplayName("buscarClasesSegunRol para ADMIN debe usar buscarClases")
    void testBuscarClasesSegunRolParaAdmin() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        when(securityUtils.isAdmin()).thenReturn(true);
        when(securityUtils.isProfessor()).thenReturn(false);
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        when(repositorioClase.findByGeneralSearch(eq("Java"), any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClasesSegunRol(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that buscarClases was called (for admin)
        verify(repositorioClase).findByGeneralSearch(eq("Java"), any(Pageable.class));
    }

    @Test
    @DisplayName("buscarClasesSegunRol para PROFESOR debe filtrar por clases del profesor")
    void testBuscarClasesSegunRolParaProfesor() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.isProfessor()).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        
        // Mock classes for the professor
        when(repositorioClase.findByProfesorId("1")).thenReturn(Arrays.asList(curso));

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClasesSegunRol(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Curso de Java", resultado.content().get(0).titulo());
        
        // Verify that the professor's classes were filtered
        verify(repositorioClase).findByProfesorId("1");
    }

    @Test
    @DisplayName("buscarClasesSegunRol para ALUMNO debe usar buscarClases")
    void testBuscarClasesSegunRolParaAlumno() {
        // Arrange
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Java", null, null, null, null, null, null, null, null, 0, 10, "titulo", "ASC");
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.isProfessor()).thenReturn(false);
        
        List<Clase> clases = Arrays.asList(curso);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Clase> page = new PageImpl<>(clases, pageable, 1);
        
        when(repositorioClase.findByGeneralSearch(eq("Java"), any(Pageable.class))).thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOClase> resultado = servicioClase.buscarClasesSegunRol(parametros);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that buscarClases was called (for student)
        verify(repositorioClase).findByGeneralSearch(eq("Java"), any(Pageable.class));
    }
}
