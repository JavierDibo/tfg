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

        // Mock repository call for combined filter
        when(repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(
            eq(alumnoId), eq(ejercicioId), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        assertEquals(2, resultado.getTotalElements());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByAlumnoEntreganteIdAndEjercicioId(
            eq(alumnoId), eq(ejercicioId), any(Pageable.class));
        
        // Verify that no other repository methods were called
        verify(repositorioEntregaEjercicio, never()).findByAlumnoEntreganteId(anyString(), any(Pageable.class));
        verify(repositorioEntregaEjercicio, never()).findByEjercicioId(anyString(), any(Pageable.class));
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

        // Mock repository call for student-only filter
        when(repositorioEntregaEjercicio.findByAlumnoEntreganteId(
            eq(alumnoId), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByAlumnoEntreganteId(eq(alumnoId), any(Pageable.class));
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

        // Mock repository call for exercise-only filter
        when(repositorioEntregaEjercicio.findByEjercicioId(
            eq(ejercicioId), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByEjercicioId(eq(ejercicioId), any(Pageable.class));
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

        // Mock repository call for combined filters
        when(repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioIdAndEstadoAndNotaBetween(
            eq(alumnoId), eq(ejercicioId), eq(EEstadoEjercicio.CALIFICADO),
            eq(notaMin), eq(notaMax), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("12", resultado.getContent().get(0).alumnoEntreganteId());
        assertEquals("35", resultado.getContent().get(0).ejercicioId());
        assertEquals("CALIFICADO", resultado.getContent().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByAlumnoEntreganteIdAndEjercicioIdAndEstadoAndNotaBetween(
            alumnoId, ejercicioId, EEstadoEjercicio.CALIFICADO, notaMin, notaMax, pageable);
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

        // Mock repository call for student and status filter
        when(repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEstado(
            eq(alumnoId), eq(EEstadoEjercicio.ENTREGADO), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("12", resultado.getContent().get(0).alumnoEntreganteId());
        assertEquals("ENTREGADO", resultado.getContent().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByAlumnoEntreganteIdAndEstado(
            alumnoId, EEstadoEjercicio.ENTREGADO, pageable);
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

        // Mock repository call for exercise and status filter
        when(repositorioEntregaEjercicio.findByEjercicioIdAndEstado(
            eq(ejercicioId), eq(EEstadoEjercicio.CALIFICADO), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("35", resultado.getContent().get(0).ejercicioId());
        assertEquals("CALIFICADO", resultado.getContent().get(0).estado());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByEjercicioIdAndEstado(
            ejercicioId, EEstadoEjercicio.CALIFICADO, pageable);
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

        // Mock repository call for grade range filter
        when(repositorioEntregaEjercicio.findByNotaBetween(
            eq(notaMin), eq(notaMax), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("7.5", resultado.getContent().get(0).notaFormateada());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByNotaBetween(notaMin, notaMax, pageable);
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

        // Mock repository call for no filters
        when(repositorioEntregaEjercicio.findAll(any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        
        // Verify that findAll was called
        verify(repositorioEntregaEjercicio).findAll(pageable);
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

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEntregaEjercicio.obtenerEntregasPaginadas(
                alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called
        verify(repositorioEntregaEjercicio, never()).findAll(any(Pageable.class));
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

        // Mock repository call
        when(repositorioEntregaEjercicio.findByAlumnoEntreganteId(
            eq(alumnoId), any(Pageable.class))).thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getPage());
        assertEquals(5, resultado.getSize());
        assertEquals(25, resultado.getTotalElements());
        assertEquals(5, resultado.getTotalPages());
        assertEquals("fechaEntrega", resultado.getSortBy());
        assertEquals("DESC", resultado.getSortDirection());
        
        // Verify that the method was called with correct pagination
        verify(repositorioEntregaEjercicio).findByAlumnoEntreganteId(alumnoId, pageable);
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
        verify(repositorioEntregaEjercicio, never()).findAll(any(Pageable.class));
    }
}
