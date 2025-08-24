package app.repositorios;

import app.entidades.Profesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests para RepositorioProfesor")
class RepositorioProfesorTest {

    @Autowired
    private RepositorioProfesor repositorioProfesor;

    private Profesor profesor1;
    private Profesor profesor2;
    private Profesor profesor3;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        repositorioProfesor.deleteAll();

        // Crear profesores de prueba con DNIs válidos
        // 12345678 % 23 = 14, DNI_LETTERS[14] = 'Z' ✓
        // 87654321 % 23 = 10, DNI_LETTERS[10] = 'X' 
        // 11223344 % 23 = 11, DNI_LETTERS[11] = 'B'
        profesor1 = new Profesor("profesor1", "password1", "María", "García", "12345678Z", "maria@ejemplo.com", "123456789");
        profesor1.setEnabled(true);
        profesor1.agregarClase("clase1");
        profesor1.agregarClase("clase2");

        profesor2 = new Profesor("profesor2", "password2", "Juan", "Pérez", "87654321X", "juan@ejemplo.com", "987654321");
        profesor2.setEnabled(true);

        profesor3 = new Profesor("profesor3", "password3", "Ana", "López", "11223344B", "ana@ejemplo.com", "555666777");
        profesor3.setEnabled(false);
        profesor3.agregarClase("clase3");

        // Guardar profesores
        repositorioProfesor.save(profesor1);
        repositorioProfesor.save(profesor2);
        repositorioProfesor.save(profesor3);
    }

    @Test
    @DisplayName("findByUsuario debe encontrar profesor por usuario")
    void testFindByUsuario() {
        Optional<Profesor> resultado = repositorioProfesor.findByUsuario("profesor1");

        assertTrue(resultado.isPresent());
        assertEquals("profesor1", resultado.get().getUsername());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByUsuario debe retornar empty cuando no existe")
    void testFindByUsuarioNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findByUsuario("inexistente");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByEmail debe encontrar profesor por email")
    void testFindByEmail() {
        Optional<Profesor> resultado = repositorioProfesor.findByEmail("maria@ejemplo.com");

        assertTrue(resultado.isPresent());
        assertEquals("maria@ejemplo.com", resultado.get().getEmail());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByEmail debe retornar empty cuando no existe")
    void testFindByEmailNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findByEmail("inexistente@ejemplo.com");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByDni debe encontrar profesor por DNI")
    void testFindByDni() {
        Optional<Profesor> resultado = repositorioProfesor.findByDni("12345678Z");

        assertTrue(resultado.isPresent());
        assertEquals("12345678Z", resultado.get().getDni());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByDni debe retornar empty cuando no existe")
    void testFindByDniNoExiste() {
        // Usar un DNI válido que no existe en la base de datos
        Optional<Profesor> resultado = repositorioProfesor.findByDni("99999999R");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe encontrar profesores por nombre")
    void testFindByNombreContainingIgnoreCase() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("María");

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe ser case insensitive")
    void testFindByNombreContainingIgnoreCaseCaseInsensitive() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("maría");

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe retornar lista vacía cuando no encuentra")
    void testFindByNombreContainingIgnoreCaseNoEncuentra() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("findByApellidosContainingIgnoreCase debe encontrar profesores por apellidos")
    void testFindByApellidosContainingIgnoreCase() {
        List<Profesor> resultado = repositorioProfesor.findByApellidosContainingIgnoreCase("García");

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByApellidosContainingIgnoreCase debe ser case insensitive")
    void testFindByApellidosContainingIgnoreCaseCaseInsensitive() {
        List<Profesor> resultado = repositorioProfesor.findByApellidosContainingIgnoreCase("garcía");

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByEnabledTrue debe encontrar profesores habilitados")
    void testFindByEnabledTrue() {
        List<Profesor> resultado = repositorioProfesor.findByEnabledTrue();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Profesor::isEnabled));
    }

    @Test
    @DisplayName("findAllOrderedById debe retornar todos los profesores ordenados")
    void testFindAllOrderedById() {
        List<Profesor> resultado = repositorioProfesor.findAllOrderedById();

        assertEquals(3, resultado.size());
        // Verificar que están ordenados por ID
        assertTrue(resultado.get(0).getId() < resultado.get(1).getId());
        assertTrue(resultado.get(1).getId() < resultado.get(2).getId());
    }

    @Test
    @DisplayName("findByClaseId debe encontrar profesores por clase")
    void testFindByClaseId() {
        List<Profesor> resultado = repositorioProfesor.findByClaseId("clase1");

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getClasesId().contains("clase1"));
    }

    @Test
    @DisplayName("findByClaseId debe retornar lista vacía cuando no encuentra")
    void testFindByClaseIdNoEncuentra() {
        List<Profesor> resultado = repositorioProfesor.findByClaseId("claseInexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("countClasesByProfesorId debe contar clases correctamente")
    void testCountClasesByProfesorId() {
        Integer resultado = repositorioProfesor.countClasesByProfesorId(profesor1.getId());

        assertEquals(2, resultado);
    }

    @Test
    @DisplayName("countClasesByProfesorId debe retornar 0 para profesor sin clases")
    void testCountClasesByProfesorIdSinClases() {
        Integer resultado = repositorioProfesor.countClasesByProfesorId(profesor2.getId());

        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("findProfesoresSinClases debe encontrar profesores sin clases")
    void testFindProfesoresSinClases() {
        List<Profesor> resultado = repositorioProfesor.findProfesoresSinClases();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getClasesId().isEmpty());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por nombre")
    void testFindByFiltrosPorNombre() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                "María", null, null, null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por apellidos")
    void testFindByFiltrosPorApellidos() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, "García", null, null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por email")
    void testFindByFiltrosPorEmail() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, "maria@ejemplo.com", null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("maria@ejemplo.com", resultado.get(0).getEmail());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por usuario")
    void testFindByFiltrosPorUsuario() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, "profesor1", null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("profesor1", resultado.get(0).getUsername());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por DNI")
    void testFindByFiltrosPorDni() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, "12345678Z", null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("12345678Z", resultado.get(0).getDni());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por habilitado")
    void testFindByFiltrosPorHabilitado() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, true, null, null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Profesor::isEnabled));
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por clase")
    void testFindByFiltrosPorClase() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, "clase1", null);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getClasesId().contains("clase1"));
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por sin clases")
    void testFindByFiltrosPorSinClases() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, null, true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getClasesId().isEmpty());
    }

    @Test
    @DisplayName("findByFiltros debe combinar múltiples filtros")
    void testFindByFiltrosMultiples() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                "María", "García", null, null, null, true, null, null);

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
        assertEquals("García", resultado.get(0).getLastName());
        assertTrue(resultado.get(0).isEnabled());
    }

    @Test
    @DisplayName("findByFiltros debe retornar todos cuando no hay filtros")
    void testFindByFiltrosSinFiltros() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, null, null);

        assertEquals(3, resultado.size());
    }

    @Test
    @DisplayName("save debe guardar profesor correctamente")
    void testSave() {
        Profesor nuevoProfesor = new Profesor("nuevo", "password", "Nuevo", "Profesor", "99999999A", "nuevo@ejemplo.com", "111222333");
        nuevoProfesor.agregarClase("claseNueva");

        Profesor resultado = repositorioProfesor.save(nuevoProfesor);

        assertNotNull(resultado.getId());
        assertEquals("nuevo", resultado.getUsername());
        assertEquals("Nuevo", resultado.getFirstName());
        assertTrue(resultado.getClasesId().contains("claseNueva"));
    }

    @Test
    @DisplayName("findById debe encontrar profesor por ID")
    void testFindById() {
        Optional<Profesor> resultado = repositorioProfesor.findById(profesor1.getId());

        assertTrue(resultado.isPresent());
        assertEquals(profesor1.getId(), resultado.get().getId());
        assertEquals("profesor1", resultado.get().getUsername());
    }

    @Test
    @DisplayName("findById debe retornar empty cuando no existe")
    void testFindByIdNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findById(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("existsById debe retornar true cuando existe")
    void testExistsById() {
        boolean resultado = repositorioProfesor.existsById(profesor1.getId());

        assertTrue(resultado);
    }

    @Test
    @DisplayName("existsById debe retornar false cuando no existe")
    void testExistsByIdNoExiste() {
        boolean resultado = repositorioProfesor.existsById(999L);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("deleteById debe eliminar profesor correctamente")
    void testDeleteById() {
        Long idAEliminar = profesor1.getId();
        
        repositorioProfesor.deleteById(idAEliminar);
        
        assertFalse(repositorioProfesor.existsById(idAEliminar));
        assertEquals(2, repositorioProfesor.count());
    }

    @Test
    @DisplayName("count debe retornar número correcto de profesores")
    void testCount() {
        long resultado = repositorioProfesor.count();

        assertEquals(3, resultado);
    }

    @Test
    @DisplayName("deleteAll debe eliminar todos los profesores")
    void testDeleteAll() {
        repositorioProfesor.deleteAll();

        assertEquals(0, repositorioProfesor.count());
    }
}
