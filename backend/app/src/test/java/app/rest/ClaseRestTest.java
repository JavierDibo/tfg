package app.rest;

import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaClase;
import app.dtos.DTORespuestaPaginada;
import app.entidades.enums.EDificultad;
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
            "test-image.jpg", EDificultad.INTERMEDIO,
            Arrays.asList("alum1"), Arrays.asList("prof1"), 
            Arrays.asList("ej1"), Arrays.asList(), "CURSO"
        );
    }

    @Test
    void obtenerClases_ShouldCallServiceWithRoleBasedMethod() {
        // Given
        // Create a mock Page for testing
        org.springframework.data.domain.Page<DTOClase> mockPage = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(mockClase), 
            org.springframework.data.domain.PageRequest.of(0, 20), 
            1
        );
        DTORespuestaPaginada<DTOClase> expectedResponse = DTORespuestaPaginada.fromPage(mockPage, "id", "ASC");
        when(servicioClase.buscarClasesSegunRol(any(DTOParametrosBusquedaClase.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<DTORespuestaPaginada<DTOClase>> response = claseRest.obtenerClases(
            null, null, null, null, null, null, null, null, 0, 20, "id", "ASC"
        );

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        verify(servicioClase).buscarClasesSegunRol(any(DTOParametrosBusquedaClase.class));
    }

    @Test
    void obtenerClasePorId_ShouldCallService() {
        // Given
        when(servicioClase.obtenerClasePorId(1L)).thenReturn(mockClase);

        // When
        ResponseEntity<DTOClase> response = claseRest.obtenerClasePorId(1L);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockClase, response.getBody());
        verify(servicioClase).obtenerClasePorId(1L);
    }
}
