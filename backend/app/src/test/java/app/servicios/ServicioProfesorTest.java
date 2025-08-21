package app.servicios;

import app.dtos.DTOProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.entidades.Profesor;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioProfesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import app.entidades.Usuario;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioProfesor")
class ServicioProfesorTest {

    @Mock
    private RepositorioProfesor repositorioProfesor;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ServicioProfesor servicioProfesor;

    private Profesor profesor1;
    private Profesor profesor2;
    private Profesor profesor3;
    private DTOProfesor dtoProfesor1;
    private DTOProfesor dtoProfesor2;
    private DTOProfesor dtoProfesor3;

    @BeforeEach
    void setUp() {
        profesor1 = new Profesor("profesor1", "password1", "María", "García", "12345678Z", "maria@ejemplo.com", "123456789");
        profesor1.setId(1L);
        profesor1.setEnabled(true);
        profesor1.agregarClase("clase1");
        profesor1.agregarClase("clase2");

        profesor2 = new Profesor("profesor2", "password2", "Juan", "Pérez", "87654321Y", "juan@ejemplo.com", "987654321");
        profesor2.setId(2L);
        profesor2.setEnabled(true);

        profesor3 = new Profesor("profesor3", "password3", "Ana", "López", "11223344X", "ana@ejemplo.com", "555666777");
        profesor3.setId(3L);
        profesor3.setEnabled(false);
        profesor3.agregarClase("clase3");

        // Create DTOs with fixed timestamp to avoid comparison issues
        dtoProfesor1 = new DTOProfesor(profesor1.getId(), profesor1.getUsuario(), profesor1.getNombre(), 
                                      profesor1.getApellidos(), profesor1.getDni(), profesor1.getEmail(), 
                                      profesor1.getNumeroTelefono(), profesor1.getRol(), profesor1.isEnabled(), 
                                      profesor1.getClasesId(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        dtoProfesor2 = new DTOProfesor(profesor2.getId(), profesor2.getUsuario(), profesor2.getNombre(), 
                                      profesor2.getApellidos(), profesor2.getDni(), profesor2.getEmail(), 
                                      profesor2.getNumeroTelefono(), profesor2.getRol(), profesor2.isEnabled(), 
                                      profesor2.getClasesId(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        dtoProfesor3 = new DTOProfesor(profesor3.getId(), profesor3.getUsuario(), profesor3.getNombre(), 
                                      profesor3.getApellidos(), profesor3.getDni(), profesor3.getEmail(), 
                                      profesor3.getNumeroTelefono(), profesor3.getRol(), profesor3.isEnabled(), 
                                      profesor3.getClasesId(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
    }

    @Test
    @DisplayName("obtenerProfesores debe retornar lista de DTOs")
    void testObtenerProfesores() {
        List<Profesor> profesores = Arrays.asList(profesor1, profesor2, profesor3);
        when(repositorioProfesor.findAllOrderedById()).thenReturn(profesores);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesores();

        assertEquals(3, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultado1 = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultado1.id());
        assertEquals(dtoProfesor1.usuario(), resultado1.usuario());
        assertEquals(dtoProfesor1.nombre(), resultado1.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultado1.apellidos());
        assertEquals(dtoProfesor1.dni(), resultado1.dni());
        assertEquals(dtoProfesor1.email(), resultado1.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultado1.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultado1.rol());
        assertEquals(dtoProfesor1.enabled(), resultado1.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultado1.clasesId());
        
        DTOProfesor resultado2 = resultado.get(1);
        assertEquals(dtoProfesor2.id(), resultado2.id());
        assertEquals(dtoProfesor2.usuario(), resultado2.usuario());
        assertEquals(dtoProfesor2.nombre(), resultado2.nombre());
        assertEquals(dtoProfesor2.apellidos(), resultado2.apellidos());
        assertEquals(dtoProfesor2.dni(), resultado2.dni());
        assertEquals(dtoProfesor2.email(), resultado2.email());
        assertEquals(dtoProfesor2.numeroTelefono(), resultado2.numeroTelefono());
        assertEquals(dtoProfesor2.rol(), resultado2.rol());
        assertEquals(dtoProfesor2.enabled(), resultado2.enabled());
        assertEquals(dtoProfesor2.clasesId(), resultado2.clasesId());
        
        DTOProfesor resultado3 = resultado.get(2);
        assertEquals(dtoProfesor3.id(), resultado3.id());
        assertEquals(dtoProfesor3.usuario(), resultado3.usuario());
        assertEquals(dtoProfesor3.nombre(), resultado3.nombre());
        assertEquals(dtoProfesor3.apellidos(), resultado3.apellidos());
        assertEquals(dtoProfesor3.dni(), resultado3.dni());
        assertEquals(dtoProfesor3.email(), resultado3.email());
        assertEquals(dtoProfesor3.numeroTelefono(), resultado3.numeroTelefono());
        assertEquals(dtoProfesor3.rol(), resultado3.rol());
        assertEquals(dtoProfesor3.enabled(), resultado3.enabled());
        assertEquals(dtoProfesor3.clasesId(), resultado3.clasesId());
        
        verify(repositorioProfesor).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerProfesorPorId debe retornar DTO cuando existe")
    void testObtenerProfesorPorIdExiste() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorId(1L);

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.usuario(), resultado.usuario());
        assertEquals(dtoProfesor1.nombre(), resultado.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultado.apellidos());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultado.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultado.rol());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultado.clasesId());
        verify(repositorioProfesor).findById(1L);
    }

    @Test
    @DisplayName("obtenerProfesorPorId debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorIdNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.obtenerProfesorPorId(999L);
        });
        verify(repositorioProfesor).findById(999L);
    }

    @Test
    @DisplayName("obtenerProfesorPorEmail debe retornar DTO cuando existe")
    void testObtenerProfesorPorEmailExiste() {
        when(repositorioProfesor.findByEmail("maria@ejemplo.com")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorEmail("maria@ejemplo.com");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.usuario(), resultado.usuario());
        assertEquals(dtoProfesor1.nombre(), resultado.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultado.apellidos());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultado.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultado.rol());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultado.clasesId());
        verify(repositorioProfesor).findByEmail("maria@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerProfesorPorEmail debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorEmailNoExiste() {
        when(repositorioProfesor.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.obtenerProfesorPorEmail("inexistente@ejemplo.com");
        });
        verify(repositorioProfesor).findByEmail("inexistente@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerProfesorPorUsuario debe retornar DTO cuando existe")
    void testObtenerProfesorPorUsuarioExiste() {
        when(repositorioProfesor.findByUsuario("profesor1")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorUsuario("profesor1");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.usuario(), resultado.usuario());
        assertEquals(dtoProfesor1.nombre(), resultado.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultado.apellidos());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultado.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultado.rol());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultado.clasesId());
        verify(repositorioProfesor).findByUsuario("profesor1");
    }

    @Test
    @DisplayName("obtenerProfesorPorUsuario debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorUsuarioNoExiste() {
        when(repositorioProfesor.findByUsuario("inexistente")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.obtenerProfesorPorUsuario("inexistente");
        });
        verify(repositorioProfesor).findByUsuario("inexistente");
    }

    @Test
    @DisplayName("obtenerProfesorPorDni debe retornar DTO cuando existe")
    void testObtenerProfesorPorDniExiste() {
        when(repositorioProfesor.findByDni("12345678Z")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorDni("12345678Z");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.usuario(), resultado.usuario());
        assertEquals(dtoProfesor1.nombre(), resultado.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultado.apellidos());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultado.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultado.rol());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultado.clasesId());
        verify(repositorioProfesor).findByDni("12345678Z");
    }

    @Test
    @DisplayName("obtenerProfesorPorDni debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorDniNoExiste() {
        when(repositorioProfesor.findByDni("99999999A")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.obtenerProfesorPorDni("99999999A");
        });
        verify(repositorioProfesor).findByDni("99999999A");
    }

    @Test
    @DisplayName("buscarProfesoresPorNombre debe retornar profesores filtrados")
    void testBuscarProfesoresPorNombre() {
        List<Profesor> profesoresFiltrados = Arrays.asList(profesor1);
        when(repositorioProfesor.findByNombreContainingIgnoreCase("María")).thenReturn(profesoresFiltrados);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorNombre("María");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.usuario(), resultadoProfesor.usuario());
        assertEquals(dtoProfesor1.nombre(), resultadoProfesor.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultadoProfesor.apellidos());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultadoProfesor.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultadoProfesor.rol());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultadoProfesor.clasesId());
        verify(repositorioProfesor).findByNombreContainingIgnoreCase("María");
    }

    @Test
    @DisplayName("buscarProfesoresPorApellidos debe retornar profesores filtrados")
    void testBuscarProfesoresPorApellidos() {
        List<Profesor> profesoresFiltrados = Arrays.asList(profesor1);
        when(repositorioProfesor.findByApellidosContainingIgnoreCase("García")).thenReturn(profesoresFiltrados);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorApellidos("García");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.usuario(), resultadoProfesor.usuario());
        assertEquals(dtoProfesor1.nombre(), resultadoProfesor.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultadoProfesor.apellidos());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultadoProfesor.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultadoProfesor.rol());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultadoProfesor.clasesId());
        verify(repositorioProfesor).findByApellidosContainingIgnoreCase("García");
    }

    @Test
    @DisplayName("buscarProfesoresPorParametros debe retornar todos cuando no hay filtros")
    void testBuscarProfesoresPorParametrosSinFiltros() {
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor();
        List<Profesor> todosLosProfesores = Arrays.asList(profesor1, profesor2, profesor3);
        when(repositorioProfesor.findAllOrderedById()).thenReturn(todosLosProfesores);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorParametros(parametros);

        assertEquals(3, resultado.size());
        verify(repositorioProfesor).findAllOrderedById();
        verify(repositorioProfesor, never()).findByFiltros(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("buscarProfesoresPorParametros debe usar filtros cuando se proporcionan")
    void testBuscarProfesoresPorParametrosConFiltros() {
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
                "María", null, null, null, null, null, null, null);
        List<Profesor> profesoresFiltrados = Arrays.asList(profesor1);
        when(repositorioProfesor.findByFiltros("María", null, null, null, null, null, null, null))
                .thenReturn(profesoresFiltrados);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorParametros(parametros);

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.usuario(), resultadoProfesor.usuario());
        assertEquals(dtoProfesor1.nombre(), resultadoProfesor.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultadoProfesor.apellidos());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultadoProfesor.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultadoProfesor.rol());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultadoProfesor.clasesId());
        verify(repositorioProfesor).findByFiltros("María", null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("obtenerProfesoresHabilitados debe retornar profesores habilitados")
    void testObtenerProfesoresHabilitados() {
        List<Profesor> profesoresHabilitados = Arrays.asList(profesor1, profesor2);
        when(repositorioProfesor.findByEnabledTrue()).thenReturn(profesoresHabilitados);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresHabilitados();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(DTOProfesor::enabled));
        verify(repositorioProfesor).findByEnabledTrue();
    }

    @Test
    @DisplayName("obtenerProfesoresPorClase debe retornar profesores de la clase")
    void testObtenerProfesoresPorClase() {
        List<Profesor> profesoresDeClase = Arrays.asList(profesor1);
        when(repositorioProfesor.findByClaseId("clase1")).thenReturn(profesoresDeClase);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresPorClase("clase1");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.usuario(), resultadoProfesor.usuario());
        assertEquals(dtoProfesor1.nombre(), resultadoProfesor.nombre());
        assertEquals(dtoProfesor1.apellidos(), resultadoProfesor.apellidos());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.numeroTelefono(), resultadoProfesor.numeroTelefono());
        assertEquals(dtoProfesor1.rol(), resultadoProfesor.rol());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.clasesId(), resultadoProfesor.clasesId());
        verify(repositorioProfesor).findByClaseId("clase1");
    }

    @Test
    @DisplayName("obtenerProfesoresSinClases debe retornar profesores sin clases")
    void testObtenerProfesoresSinClases() {
        List<Profesor> profesoresSinClases = Arrays.asList(profesor2);
        when(repositorioProfesor.findProfesoresSinClases()).thenReturn(profesoresSinClases);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresSinClases();

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor2.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor2.usuario(), resultadoProfesor.usuario());
        assertEquals(dtoProfesor2.nombre(), resultadoProfesor.nombre());
        assertEquals(dtoProfesor2.apellidos(), resultadoProfesor.apellidos());
        assertEquals(dtoProfesor2.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor2.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor2.numeroTelefono(), resultadoProfesor.numeroTelefono());
        assertEquals(dtoProfesor2.rol(), resultadoProfesor.rol());
        assertEquals(dtoProfesor2.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor2.clasesId(), resultadoProfesor.clasesId());
        verify(repositorioProfesor).findProfesoresSinClases();
    }

    @Test
    @DisplayName("crearProfesor debe crear profesor correctamente")
    void testCrearProfesor() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "98765432X", "nuevo@ejemplo.com", "555123456");
        
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor profesorGuardado = invocation.getArgument(0);
            profesorGuardado.setId(4L);
            return profesorGuardado;
        });

        DTOProfesor resultado = servicioProfesor.crearProfesor(peticion);

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(4L, resultado.id());
        assertEquals("nuevo", resultado.usuario());
        assertEquals("Nuevo", resultado.nombre());
        assertEquals("Profesor", resultado.apellidos());
        assertEquals("98765432X", resultado.dni());
        assertEquals("nuevo@ejemplo.com", resultado.email());
        assertEquals("555123456", resultado.numeroTelefono());
        assertEquals(Usuario.Rol.PROFESOR, resultado.rol());
        assertTrue(resultado.enabled());
        assertNotNull(resultado.clasesId());
        assertTrue(resultado.clasesId().isEmpty());
        
        verify(passwordEncoder).encode("password123");
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción si usuario ya existe")
    void testCrearProfesorUsuarioExiste() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "existente", "password123", "Nuevo", "Profesor", "99999999A", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioProfesor.findByUsuario("existente")).thenReturn(Optional.of(profesor1));

        assertThrows(IllegalArgumentException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });
        verify(repositorioProfesor).findByUsuario("existente");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción si email ya existe")
    void testCrearProfesorEmailExiste() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "99999999A", "existente@ejemplo.com", "111222333"
        );
        
        when(repositorioProfesor.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("existente@ejemplo.com")).thenReturn(Optional.of(profesor1));

        assertThrows(IllegalArgumentException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });
        verify(repositorioProfesor).findByUsuario("nuevo");
        verify(repositorioProfesor).findByEmail("existente@ejemplo.com");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción si DNI ya existe")
    void testCrearProfesorDniExiste() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "12345678Z", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioProfesor.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("nuevo@ejemplo.com")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByDni("12345678Z")).thenReturn(Optional.of(profesor1));

        assertThrows(IllegalArgumentException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });
        verify(repositorioProfesor).findByUsuario("nuevo");
        verify(repositorioProfesor).findByEmail("nuevo@ejemplo.com");
        verify(repositorioProfesor).findByDni("12345678Z");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor con clases debe asignar clases correctamente")
    void testCrearProfesorConClases() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "99999999A", "nuevo@ejemplo.com", "111222333",
                Arrays.asList("clase1", "clase2")
        );
        
        when(repositorioProfesor.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("nuevo@ejemplo.com")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByDni("99999999A")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.crearProfesor(peticion);

        assertNotNull(resultado);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("borrarProfesorPorId debe borrar profesor correctamente")
    void testBorrarProfesorPorId() {
        when(repositorioProfesor.existsById(1L)).thenReturn(true);

        boolean resultado = servicioProfesor.borrarProfesorPorId(1L);

        assertTrue(resultado);
        verify(repositorioProfesor).existsById(1L);
        verify(repositorioProfesor).deleteById(1L);
    }

    @Test
    @DisplayName("borrarProfesorPorId debe lanzar excepción si profesor no existe")
    void testBorrarProfesorPorIdNoExiste() {
        when(repositorioProfesor.existsById(999L)).thenReturn(false);

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.borrarProfesorPorId(999L);
        });
        verify(repositorioProfesor).existsById(999L);
        verify(repositorioProfesor, never()).deleteById(any());
    }

    @Test
    @DisplayName("asignarClase debe asignar clase correctamente")
    void testAsignarClase() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.asignarClase(1L, "clase3");

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("asignarClase debe lanzar excepción si profesor no existe")
    void testAsignarClaseProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.asignarClase(999L, "clase3");
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("removerClase debe remover clase correctamente")
    void testRemoverClase() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.removerClase(1L, "clase1");

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("removerClase debe lanzar excepción si profesor no existe")
    void testRemoverClaseProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.removerClase(999L, "clase1");
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("contarClasesProfesor debe retornar número correcto de clases")
    void testContarClasesProfesor() {
        when(repositorioProfesor.countClasesByProfesorId(1L)).thenReturn(2);

        Integer resultado = servicioProfesor.contarClasesProfesor(1L);

        assertEquals(2, resultado);
        verify(repositorioProfesor).countClasesByProfesorId(1L);
    }

    @Test
    @DisplayName("cambiarEstadoProfesor debe cambiar estado correctamente")
    void testCambiarEstadoProfesor() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.cambiarEstadoProfesor(1L, false);

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("cambiarEstadoProfesor debe lanzar excepción si profesor no existe")
    void testCambiarEstadoProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioProfesor.cambiarEstadoProfesor(999L, false);
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }
}
