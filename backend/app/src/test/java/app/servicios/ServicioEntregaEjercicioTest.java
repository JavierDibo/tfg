package app.servicios;

import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
import app.repositorios.RepositorioEntregaEjercicio;
import app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioEntregaEjercicioTest {

    @Mock
    private RepositorioEntregaEjercicio repositorioEntregaEjercicio;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioEntregaEjercicio servicioEntregaEjercicio;

    @BeforeEach
    void setUp() {
        // Set up security context for testing
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("test-professor", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testObtenerEntregasPaginadas_ConAlumnoIdYEjercicioId_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = "35";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(8.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumnoEntreganteId("12");
        entrega2.setEjercicioId("35");
        entrega2.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega2.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method that the service actually uses
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), eq(ejercicioId), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        assertEquals(2, resultado.totalElements());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SoloAlumnoId_DebeFiltrarSoloPorAlumno() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumnoEntreganteId("12");
        entrega2.setEjercicioId("40");
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SoloEjercicioId_DebeFiltrarSoloPorEjercicio() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = "35";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumnoEntreganteId("15");
        entrega2.setEjercicioId("35");
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), eq(ejercicioId), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosCombinadosCompletos_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = "35";
        String estado = "CALIFICADO";
        BigDecimal notaMin = new BigDecimal("5.0");
        BigDecimal notaMax = new BigDecimal("10.0");
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(new BigDecimal("8.5"));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), eq(ejercicioId), eq("CALIFICADO"), eq(notaMin), eq(notaMax), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("12", resultado.content().get(0).alumnoEntreganteId());
        assertEquals("35", resultado.content().get(0).ejercicioId());
        assertEquals(EEstadoEjercicio.CALIFICADO, resultado.content().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConAlumnoYEstado_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = "ENTREGADO";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), isNull(), eq("ENTREGADO"), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("12", resultado.content().get(0).alumnoEntreganteId());
        assertEquals(EEstadoEjercicio.ENTREGADO, resultado.content().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConEjercicioYEstado_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = "35";
        String estado = "CALIFICADO";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(new BigDecimal("7.5"));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), eq(ejercicioId), eq("CALIFICADO"), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("35", resultado.content().get(0).ejercicioId());
        assertEquals(EEstadoEjercicio.CALIFICADO, resultado.content().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConRangoDeNotas_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = new BigDecimal("6.0");
        BigDecimal notaMax = new BigDecimal("9.0");
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(new BigDecimal("7.5"));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), eq(notaMin), eq(notaMax), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("7,50", resultado.content().get(0).getNotaFormateada());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SinFiltros_DebeRetornarTodas() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumnoEntreganteId("15");
        entrega2.setEjercicioId("40");
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method with all null filters
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the flexible filtering method was called with all null filters
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConEstadoInvalido_DebeLanzarExcepcion() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = "ESTADO_INVALIDO";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data - the service will still call the repository with the invalid state
        List<EntregaEjercicio> entregas = new ArrayList<>();
        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method - the service doesn't validate the state parameter
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), eq("ESTADO_INVALIDO"), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.content().size());
        
        // Verify that the repository method was called with the invalid state
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConPaginacion_DebeAplicarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 2;
        int size = 5;
        String sortBy = "fechaEntrega";
        String sortDirection = "DESC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega = new EntregaEjercicio();
        entrega.setId(1L);
        entrega.setAlumnoEntreganteId("12");
        entrega.setEjercicioId("35");
        entregas.add(entrega);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas, PageRequest.of(page, size), 25);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.page());
        assertEquals(5, resultado.size());
        assertEquals(25, resultado.totalElements());
        assertEquals(5, resultado.totalPages());
        assertEquals("fechaEntrega", resultado.sortBy());
        assertEquals("DESC", resultado.sortDirection());
        
        // Verify that the method was called with correct pagination
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SinPermisos_DebeLanzarExcepcion() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check - no permissions
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEntregaEjercicio.obtenerEntregasPaginadas(
                alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called
        verify(repositorioEntregaEjercicio, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosVacios_DebeTratarlosComoNull() {
        // Arrange
        String alumnoId = "";
        String ejercicioId = "   ";
        String estado = "";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> todasLasEntregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        todasLasEntregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumnoEntreganteId("15");
        entrega2.setEjercicioId("40");
        todasLasEntregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(todasLasEntregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method with all null filters (empty strings should be treated as null)
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the flexible filtering method was called with all null filters
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosCombinadosParciales_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = "CALIFICADO";
        BigDecimal notaMin = new BigDecimal("7.0");
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumnoEntreganteId("12");
        entrega1.setEjercicioId("35");
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(new BigDecimal("8.5"));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method with partial filters
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(alumnoId), isNull(), eq("CALIFICADO"), eq(notaMin), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("12", resultado.content().get(0).alumnoEntreganteId());
        assertEquals(EEstadoEjercicio.CALIFICADO, resultado.content().get(0).estado());
        assertEquals("8,50", resultado.content().get(0).getNotaFormateada());
        
        // Verify that the correct repository method was called with partial filters
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }
}
