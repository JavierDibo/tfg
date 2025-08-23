package app.rest;

import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaClase;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaseRestTest {

    @Mock
    private ServicioClase servicioClase;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ClaseRest claseRest;

    private DTOClase mockClase;

    @BeforeEach
    void setUp() {
        mockClase = new DTOClase(
            1L, "Test Class", "Test Description", 
            java.math.BigDecimal.valueOf(100), 
            app.entidades.enums.EPresencialidad.PRESENCIAL,
            "test-image.jpg", app.entidades.enums.ENivel.INTERMEDIO,
            Arrays.asList("alum1"), Arrays.asList("prof1"), 
            Arrays.asList("ej1"), Arrays.asList(), "CURSO"
        );
    }

    @Test
    void obtenerClases_ShouldCallServiceWithRoleBasedMethod() {
        // Given
        List<DTOClase> expectedClases = Arrays.asList(mockClase);
        when(servicioClase.obtenerClasesSegunRol()).thenReturn(expectedClases);

        // When
        ResponseEntity<List<DTOClase>> response = claseRest.obtenerClases();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedClases, response.getBody());
        verify(servicioClase).obtenerClasesSegunRol();
    }

    @Test
    void buscarClasesPorTitulo_ShouldCallServiceWithRoleBasedMethod() {
        // Given
        String titulo = "Test";
        List<DTOClase> expectedClases = Arrays.asList(mockClase);
        when(servicioClase.buscarClasesPorTituloSegunRol(titulo)).thenReturn(expectedClases);

        // When
        ResponseEntity<List<DTOClase>> response = claseRest.buscarClasesPorTitulo(titulo);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedClases, response.getBody());
        verify(servicioClase).buscarClasesPorTituloSegunRol(titulo);
    }

    @Test
    void buscarClases_ShouldCallServiceWithRoleBasedMethod() {
        // Given
        DTOParametrosBusquedaClase parametros = new DTOParametrosBusquedaClase(
            "Test", null, null, null, null, null, null, null, 0, 10, "titulo", "ASC"
        );
        // Create a mock Page for testing
        org.springframework.data.domain.Page<DTOClase> mockPage = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(mockClase), 
            org.springframework.data.domain.PageRequest.of(0, 10), 
            1
        );
        DTORespuestaPaginada<DTOClase> expectedResponse = DTORespuestaPaginada.fromPage(mockPage, "titulo", "ASC");
        when(servicioClase.buscarClasesSegunRol(parametros)).thenReturn(expectedResponse);

        // When
        ResponseEntity<DTORespuestaPaginada<DTOClase>> response = claseRest.buscarClases(parametros);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        verify(servicioClase).buscarClasesSegunRol(parametros);
    }


}
