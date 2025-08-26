package app.servicios;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Material;
import app.repositorios.RepositorioMaterial;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioMaterialTest {

    @Mock
    private RepositorioMaterial repositorioMaterial;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioMaterial servicioMaterial;

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
    void testObtenerMaterialesPaginados_ConFiltrosCombinados_DebeFiltrarCorrectamente() {
        // Arrange
        String q = "documento";
        String name = "apuntes";
        String url = "pdf";
        String type = "DOCUMENT";
        int page = 0;
        int size = 20;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material1 = new Material("1", "Apuntes de Java", "https://example.com/apuntes.pdf");
        Material material2 = new Material("2", "Documento de práctica", "https://example.com/practica.pdf");
        materiales.add(material1);
        materiales.add(material2);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            eq(q), eq(name), eq(url), eq(type), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        assertEquals(0, resultado.page());
        assertEquals(20, resultado.size());
        assertEquals(2, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(q, name, url, type, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_SoloConBusquedaGeneral_DebeFiltrarCorrectamente() {
        // Arrange
        String q = "java";
        String name = null;
        String url = null;
        String type = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Java Programming", "https://example.com/java.pdf");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            eq(q), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Java Programming", resultado.content().get(0).name());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(q, null, null, null, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_SoloConNombre_DebeFiltrarCorrectamente() {
        // Arrange
        String q = null;
        String name = "apuntes";
        String url = null;
        String type = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Apuntes de Matemáticas", "https://example.com/math.pdf");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), eq(name), isNull(), isNull(), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Apuntes de Matemáticas", resultado.content().get(0).name());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(null, name, null, null, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_SoloConTipo_DebeFiltrarCorrectamente() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = "VIDEO";
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Tutorial Video", "https://example.com/tutorial.mp4");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), eq(type), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Tutorial Video", resultado.content().get(0).name());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(null, null, null, type, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_SinFiltros_DebeRetornarTodos() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material1 = new Material("1", "Material 1", "https://example.com/1.pdf");
        Material material2 = new Material("2", "Material 2", "https://example.com/2.pdf");
        materiales.add(material1);
        materiales.add(material2);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the flexible filtering method was called with correct parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(null, null, null, null, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_ConFiltrosVacios_DebeTratarComoNull() {
        // Arrange
        String q = "";
        String name = "   ";
        String url = "";
        String type = "";
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Test Material", "https://example.com/test.pdf");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the flexible filtering method was called with null parameters
        verify(repositorioMaterial).findByFiltrosFlexibles(null, null, null, null, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_ConTipoEnMinusculas_DebeConvertirAMayusculas() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = "document"; // lowercase
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Document", "https://example.com/doc.pdf");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), eq("DOCUMENT"), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the type was converted to uppercase
        verify(repositorioMaterial).findByFiltrosFlexibles(null, null, null, "DOCUMENT", pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_ConPaginacion_DebeAplicarCorrectamente() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = null;
        int page = 2;
        int size = 5;
        String sortBy = "name";
        String sortDirection = "DESC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Material> materiales = new ArrayList<>();
        Material material = new Material("1", "Test Material", "https://example.com/test.pdf");
        materiales.add(material);

        Page<Material> materialPage = new PageImpl<>(materiales, PageRequest.of(page, size), 25);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for flexible filtering
        when(repositorioMaterial.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(materialPage);

        // Act
        DTORespuestaPaginada<DTOMaterial> resultado = servicioMaterial.obtenerMaterialesPaginados(
            q, name, url, type, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.page());
        assertEquals(5, resultado.size());
        assertEquals(25, resultado.totalElements());
        assertEquals(5, resultado.totalPages());
        assertEquals("name", resultado.sortBy());
        assertEquals("DESC", resultado.sortDirection());
        
        // Verify that the flexible filtering method was called with correct pagination
        verify(repositorioMaterial).findByFiltrosFlexibles(null, null, null, null, pageable);
    }

    @Test
    void testObtenerMaterialesPaginados_SinPermisos_DebeLanzarExcepcion() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check - no permissions
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.obtenerMaterialesPaginados(
                q, name, url, type, page, size, sortBy, sortDirection);
        });
        
        // Verify that the repository method was not called
        verify(repositorioMaterial, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerMaterialesPaginados_ConParametrosInvalidos_DebeLanzarExcepcion() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = null;
        int page = -1; // Invalid page
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.obtenerMaterialesPaginados(
                q, name, url, type, page, size, sortBy, sortDirection);
        });
        
        // Verify that the repository method was not called
        verify(repositorioMaterial, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerMaterialesPaginados_ConTamañoInvalido_DebeLanzarExcepcion() {
        // Arrange
        String q = null;
        String name = null;
        String url = null;
        String type = null;
        int page = 0;
        int size = 101; // Invalid size (max is 100)
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.obtenerMaterialesPaginados(
                q, name, url, type, page, size, sortBy, sortDirection);
        });
        
        // Verify that the repository method was not called
        verify(repositorioMaterial, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any());
    }
}
