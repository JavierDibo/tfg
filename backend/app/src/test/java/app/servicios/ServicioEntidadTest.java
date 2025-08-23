package app.servicios;

import app.dtos.DTOEntidad;
import app.dtos.DTOParametrosBusqueda;
import app.entidades.Entidad;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioEntidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioEntidad")
class ServicioEntidadTest {

    @Mock
    private RepositorioEntidad repositorioEntidad;

    @InjectMocks
    private ServicioEntidad servicioEntidad;

    private Entidad entidad1;
    private Entidad entidad2;
    private Entidad entidad3;
    private DTOEntidad dtoEntidad1;
    private DTOEntidad dtoEntidad2;
    private DTOEntidad dtoEntidad3;

    @BeforeEach
    void setUp() {
        entidad1 = new Entidad("Información 1", "Otra información 1");
        entidad1.setId(1);
        entidad2 = new Entidad("Información 2", "Otra información 2");
        entidad2.setId(2);
        entidad3 = new Entidad("Información 3", "Otra información 3");
        entidad3.setId(3);

        dtoEntidad1 = new DTOEntidad(entidad1);
        dtoEntidad2 = new DTOEntidad(entidad2);
        dtoEntidad3 = new DTOEntidad(entidad3);
    }

    @Test
    @DisplayName("obtenerTodasLasEntidades debe retornar lista de DTOs")
    void testObtenerTodasLasEntidades() {
        // Arrange
        List<Entidad> entidades = Arrays.asList(entidad1, entidad2, entidad3);
        when(repositorioEntidad.findAllOrderedById()).thenReturn(entidades);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerTodasLasEntidades();

        // Assert
        assertEquals(3, resultado.size());
        assertEquals(dtoEntidad1.id(), resultado.get(0).id());
        assertEquals(dtoEntidad1.info(), resultado.get(0).info());
        assertEquals(dtoEntidad2.id(), resultado.get(1).id());
        assertEquals(dtoEntidad3.id(), resultado.get(2).id());
        verify(repositorioEntidad).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerTodasLasEntidades debe retornar lista vacía cuando no hay entidades")
    void testObtenerTodasLasEntidadesVacia() {
        // Arrange
        when(repositorioEntidad.findAllOrderedById()).thenReturn(Collections.emptyList());

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerTodasLasEntidades();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repositorioEntidad).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerEntidadPorId debe retornar DTO cuando existe")
    void testObtenerEntidadPorIdExiste() {
        // Arrange
        when(repositorioEntidad.findById(1)).thenReturn(Optional.of(entidad1));

        // Act
        DTOEntidad resultado = servicioEntidad.obtenerEntidadPorId(1);

        // Assert
        assertEquals(dtoEntidad1.id(), resultado.id());
        assertEquals(dtoEntidad1.info(), resultado.info());
        assertEquals(dtoEntidad1.otraInfo(), resultado.otraInfo());
        verify(repositorioEntidad).findById(1);
    }

    @Test
    @DisplayName("obtenerEntidadPorId debe lanzar excepción cuando no existe")
    void testObtenerEntidadPorIdNoExiste() {
        // Arrange
        when(repositorioEntidad.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioEntidad.obtenerEntidadPorId(999);
        });
        verify(repositorioEntidad).findById(999);
    }

    @Test
    @DisplayName("obtenerEntidadesPorInfo debe filtrar por información")
    void testObtenerEntidadesPorInfo() {
        // Arrange
        List<Entidad> entidadesFiltradas = Arrays.asList(entidad1);
        when(repositorioEntidad.findByInfoContainingIgnoreCase("Información 1")).thenReturn(entidadesFiltradas);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorInfo("Información 1");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(dtoEntidad1.info(), resultado.get(0).info());
        verify(repositorioEntidad).findByInfoContainingIgnoreCase("Información 1");
    }

    @Test
    @DisplayName("obtenerEntidadesPorOtraInfo debe filtrar por otra información")
    void testObtenerEntidadesPorOtraInfo() {
        // Arrange
        List<Entidad> entidadesFiltradas = Arrays.asList(entidad2);
        when(repositorioEntidad.findByOtraInfoContainingIgnoreCase("Otra información 2")).thenReturn(entidadesFiltradas);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorOtraInfo("Otra información 2");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(dtoEntidad2.otraInfo(), resultado.get(0).otraInfo());
        verify(repositorioEntidad).findByOtraInfoContainingIgnoreCase("Otra información 2");
    }

    @Test
    @DisplayName("obtenerEntidadesPorParametros debe retornar todas cuando ambos parámetros son nulos")
    void testObtenerEntidadesPorParametrosSinFiltros() {
        // Arrange
        DTOParametrosBusqueda parametros = new DTOParametrosBusqueda(null, null);
        List<Entidad> todasLasEntidades = Arrays.asList(entidad1, entidad2, entidad3);
        when(repositorioEntidad.findAllOrderedById()).thenReturn(todasLasEntidades);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorParametros(parametros);

        // Assert
        assertEquals(3, resultado.size());
        verify(repositorioEntidad).findAllOrderedById();
        verify(repositorioEntidad, never()).findByInfoAndOtraInfoContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    @DisplayName("obtenerEntidadesPorParametros debe usar filtros cuando se proporcionan")
    void testObtenerEntidadesPorParametrosConFiltros() {
        // Arrange
        DTOParametrosBusqueda parametros = new DTOParametrosBusqueda("info", "otraInfo");
        List<Entidad> entidadesFiltradas = Arrays.asList(entidad1);
        when(repositorioEntidad.findByInfoAndOtraInfoContainingIgnoreCase("info", "otraInfo")).thenReturn(entidadesFiltradas);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorParametros(parametros);

        // Assert
        assertEquals(1, resultado.size());
        verify(repositorioEntidad).findByInfoAndOtraInfoContainingIgnoreCase("info", "otraInfo");
        verify(repositorioEntidad, never()).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerEntidadesPorParametros debe usar filtros cuando solo uno es nulo")
    void testObtenerEntidadesPorParametrosUnParametroNulo() {
        // Arrange
        DTOParametrosBusqueda parametros = new DTOParametrosBusqueda("info", null);
        List<Entidad> entidadesFiltradas = Arrays.asList(entidad1);
        when(repositorioEntidad.findByInfoAndOtraInfoContainingIgnoreCase("info", null)).thenReturn(entidadesFiltradas);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorParametros(parametros);

        // Assert
        assertEquals(1, resultado.size());
        verify(repositorioEntidad).findByInfoAndOtraInfoContainingIgnoreCase("info", null);
    }

    @Test
    @DisplayName("crearEntidad debe crear entidad correctamente")
    void testCrearEntidad() {
        // Arrange
        DTOEntidad dtoNuevaEntidad = new DTOEntidad(0, "Nueva info", "Nueva otra info");
        Entidad entidadParaGuardar = new Entidad(dtoNuevaEntidad);
        Entidad entidadGuardada = new Entidad("Nueva info", "Nueva otra info");
        entidadGuardada.setId(1);
        
        when(repositorioEntidad.save(any(Entidad.class))).thenReturn(entidadGuardada);

        // Act
        DTOEntidad resultado = servicioEntidad.crearEntidad(dtoNuevaEntidad);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.id());
        assertEquals("Nueva info", resultado.info());
        assertEquals("Nueva otra info", resultado.otraInfo());
        verify(repositorioEntidad).save(any(Entidad.class));
    }

    @Test
    @DisplayName("actualizarEntidad debe actualizar campos no nulos")
    void testActualizarEntidad() {
        // Arrange
        DTOEntidad dtoParcial = new DTOEntidad(1, "Info actualizada", null);
        when(repositorioEntidad.findById(1)).thenReturn(Optional.of(entidad1));
        when(repositorioEntidad.save(any(Entidad.class))).thenAnswer(invocation -> {
            Entidad entidadActualizada = invocation.getArgument(0);
            return entidadActualizada;
        });

        // Act
        DTOEntidad resultado = servicioEntidad.actualizarEntidad(1, dtoParcial);

        // Assert
        assertNotNull(resultado);
        assertEquals("Info actualizada", resultado.info());
        assertEquals(entidad1.getOtraInfo(), resultado.otraInfo()); // No debe cambiar
        verify(repositorioEntidad).findById(1);
        verify(repositorioEntidad).save(any(Entidad.class));
    }

    @Test
    @DisplayName("actualizarEntidad debe lanzar excepción si entidad no existe")
    void testActualizarEntidadNoExiste() {
        // Arrange
        DTOEntidad dtoParcial = new DTOEntidad(999, "Info actualizada", null);
        when(repositorioEntidad.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioEntidad.actualizarEntidad(999, dtoParcial);
        });
        verify(repositorioEntidad).findById(999);
        verify(repositorioEntidad, never()).save(any());
    }

    @Test
    @DisplayName("actualizarEntidad debe actualizar solo otraInfo cuando info es nulo")
    void testActualizarEntidadSoloOtraInfo() {
        // Arrange
        DTOEntidad dtoParcial = new DTOEntidad(1, null, "Otra info actualizada");
        when(repositorioEntidad.findById(1)).thenReturn(Optional.of(entidad1));
        when(repositorioEntidad.save(any(Entidad.class))).thenAnswer(invocation -> {
            Entidad entidadActualizada = invocation.getArgument(0);
            return entidadActualizada;
        });

        // Act
        DTOEntidad resultado = servicioEntidad.actualizarEntidad(1, dtoParcial);

        // Assert
        assertNotNull(resultado);
        assertEquals(entidad1.getInfo(), resultado.info()); // No debe cambiar
        assertEquals("Otra info actualizada", resultado.otraInfo());
        verify(repositorioEntidad).findById(1);
        verify(repositorioEntidad).save(any(Entidad.class));
    }

    @Test
    @DisplayName("borrarTodasLasEntidades debe borrar todas las entidades")
    void testBorrarTodasLasEntidades() {
        // Arrange
        List<Entidad> entidades = Arrays.asList(entidad1, entidad2, entidad3);
        when(repositorioEntidad.findAllOrderedById()).thenReturn(entidades);

        // Act
        List<DTOEntidad> resultado = servicioEntidad.borrarTodasLasEntidades();

        // Assert
        assertEquals(3, resultado.size());
        assertEquals(dtoEntidad1.id(), resultado.get(0).id());
        assertEquals(dtoEntidad2.id(), resultado.get(1).id());
        assertEquals(dtoEntidad3.id(), resultado.get(2).id());
        verify(repositorioEntidad).findAllOrderedById();
        verify(repositorioEntidad).deleteAll();
    }

    @Test
    @DisplayName("borrarTodasLasEntidades debe retornar lista vacía cuando no hay entidades")
    void testBorrarTodasLasEntidadesVacia() {
        // Arrange
        when(repositorioEntidad.findAllOrderedById()).thenReturn(Collections.emptyList());

        // Act
        List<DTOEntidad> resultado = servicioEntidad.borrarTodasLasEntidades();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repositorioEntidad).findAllOrderedById();
        verify(repositorioEntidad).deleteAll();
    }

    @Test
    @DisplayName("borrarEntidadPorId debe borrar entidad correctamente")
    void testBorrarEntidadPorId() {
        // Arrange
        when(repositorioEntidad.findById(1)).thenReturn(Optional.of(entidad1));

        // Act
        DTOEntidad resultado = servicioEntidad.borrarEntidadPorId(1);

        // Assert
        assertNotNull(resultado);
        assertEquals(dtoEntidad1.id(), resultado.id());
        assertEquals(dtoEntidad1.info(), resultado.info());
        assertEquals(dtoEntidad1.otraInfo(), resultado.otraInfo());
        verify(repositorioEntidad).findById(1);
        verify(repositorioEntidad).deleteById(1);
    }

    @Test
    @DisplayName("borrarEntidadPorId debe lanzar excepción si entidad no existe")
    void testBorrarEntidadPorIdNoExiste() {
        // Arrange
        when(repositorioEntidad.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadNoEncontradaException.class, () -> {
            servicioEntidad.borrarEntidadPorId(999);
        });
        verify(repositorioEntidad).findById(999);
        verify(repositorioEntidad, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("obtenerEntidadesPorInfo debe retornar lista vacía cuando no encuentra coincidencias")
    void testObtenerEntidadesPorInfoSinCoincidencias() {
        // Arrange
        when(repositorioEntidad.findByInfoContainingIgnoreCase("NoExiste")).thenReturn(Collections.emptyList());

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorInfo("NoExiste");

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repositorioEntidad).findByInfoContainingIgnoreCase("NoExiste");
    }

    @Test
    @DisplayName("obtenerEntidadesPorOtraInfo debe retornar lista vacía cuando no encuentra coincidencias")
    void testObtenerEntidadesPorOtraInfoSinCoincidencias() {
        // Arrange
        when(repositorioEntidad.findByOtraInfoContainingIgnoreCase("NoExiste")).thenReturn(Collections.emptyList());

        // Act
        List<DTOEntidad> resultado = servicioEntidad.obtenerEntidadesPorOtraInfo("NoExiste");

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repositorioEntidad).findByOtraInfoContainingIgnoreCase("NoExiste");
    }
}
