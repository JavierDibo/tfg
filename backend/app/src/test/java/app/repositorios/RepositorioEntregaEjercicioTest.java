package app.repositorios;

import app.entidades.EntregaEjercicio;
import app.entidades.Alumno;
import app.entidades.Ejercicio;
import app.entidades.enums.EEstadoEjercicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests para RepositorioEntregaEjercicio")
class RepositorioEntregaEjercicioTest {

    @Autowired
    private RepositorioEntregaEjercicio repositorioEntregaEjercicio;

    @Autowired
    private RepositorioAlumno repositorioAlumno;

    @Autowired
    private RepositorioEjercicio repositorioEjercicio;

    @Autowired
    private TestEntityManager entityManager;

    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;
    private Ejercicio ejercicio1;
    private Ejercicio ejercicio2;
    
    // Utility method to generate valid DNIs using the same algorithm as the system
    private String generateValidDNI(int index) {
        StringBuilder dni = new StringBuilder();
        String indexStr = String.valueOf(index);
        
        // Fill with random digits to make it 8 digits  
        while (dni.length() + indexStr.length() < 8) {
            dni.append((int)(Math.random() * 10));
        }
        dni.append(indexStr);
        
        // Ensure it's exactly 8 digits
        if (dni.length() > 8) {
            dni.setLength(8);
        }
        
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        int numero = Integer.parseInt(dni.toString());
        dni.append(letras[numero % 23]);
        return dni.toString();
    }

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        repositorioEntregaEjercicio.deleteAll();
        repositorioAlumno.deleteAll();
        repositorioEjercicio.deleteAll();

        // Crear alumnos con DNIs válidos
        alumno1 = new Alumno("alumno1", "password1", "Juan", "Pérez", generateValidDNI(1), "juan@ejemplo.com", "123456789");
        alumno2 = new Alumno("alumno2", "password2", "María", "García", generateValidDNI(2), "maria@ejemplo.com", "987654321");
        alumno3 = new Alumno("alumno3", "password3", "Carlos", "López", generateValidDNI(3), "carlos@ejemplo.com", "555666777");

        // Crear ejercicios
        ejercicio1 = new Ejercicio("Ejercicio 1", "Enunciado del ejercicio 1", 
                                  LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1");
        ejercicio2 = new Ejercicio("Ejercicio 2", "Enunciado del ejercicio 2", 
                                  LocalDateTime.now(), LocalDateTime.now().plusDays(7), "1");

        // Guardar entidades relacionadas
        alumno1 = repositorioAlumno.save(alumno1);
        alumno2 = repositorioAlumno.save(alumno2);
        alumno3 = repositorioAlumno.save(alumno3);
        ejercicio1 = repositorioEjercicio.save(ejercicio1);
        ejercicio2 = repositorioEjercicio.save(ejercicio2);
    }

    @Test
    @DisplayName("testBasicCRUDOperations debe realizar operaciones CRUD básicas")
    public void testBasicCRUDOperations() {
        // Create test delivery
        EntregaEjercicio entrega = new EntregaEjercicio(alumno1, ejercicio1);
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega.setNota(new BigDecimal("8.5"));
        entrega.setComentarios("Test delivery");
        
        // Save delivery
        EntregaEjercicio saved = repositorioEntregaEjercicio.save(entrega);
        assertNotNull(saved.getId());
        assertEquals(alumno1.getId(), saved.getAlumno().getId());
        assertEquals(ejercicio1.getId(), saved.getEjercicio().getId());
        assertEquals(EEstadoEjercicio.ENTREGADO, saved.getEstado());
        assertEquals(new BigDecimal("8.5"), saved.getNota());
        assertEquals("Test delivery", saved.getComentarios());
        
        // Find by ID
        Optional<EntregaEjercicio> found = repositorioEntregaEjercicio.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        
        // Find by alumno ID
        List<EntregaEjercicio> foundByAlumno = repositorioEntregaEjercicio.findByAlumnoEntreganteId(alumno1.getId());
        assertFalse(foundByAlumno.isEmpty());
        assertTrue(foundByAlumno.stream().allMatch(e -> e.getAlumno().getId().equals(alumno1.getId())));
        
        // Find by ejercicio ID
        List<EntregaEjercicio> foundByEjercicio = repositorioEntregaEjercicio.findByEjercicioId(ejercicio1.getId());
        assertFalse(foundByEjercicio.isEmpty());
        assertTrue(foundByEjercicio.stream().allMatch(e -> e.getEjercicio().getId().equals(ejercicio1.getId())));
        
        // Find by estado
        List<EntregaEjercicio> foundByEstado = repositorioEntregaEjercicio.findByEstado(EEstadoEjercicio.ENTREGADO);
        assertFalse(foundByEstado.isEmpty());
        assertTrue(foundByEstado.stream().allMatch(e -> e.getEstado() == EEstadoEjercicio.ENTREGADO));
        
        // Find by alumno and ejercicio
        Optional<EntregaEjercicio> foundByAlumnoAndEjercicio = repositorioEntregaEjercicio
            .findByAlumnoEntreganteIdAndEjercicioId(alumno1.getId(), ejercicio1.getId());
        assertTrue(foundByAlumnoAndEjercicio.isPresent());
        assertEquals(alumno1.getId(), foundByAlumnoAndEjercicio.get().getAlumno().getId());
        assertEquals(ejercicio1.getId(), foundByAlumnoAndEjercicio.get().getEjercicio().getId());
        
        // Update delivery
        saved.setNota(new BigDecimal("9.0"));
        saved.setEstado(EEstadoEjercicio.CALIFICADO);
        EntregaEjercicio updated = repositorioEntregaEjercicio.save(saved);
        assertEquals(new BigDecimal("9.0"), updated.getNota());
        assertEquals(EEstadoEjercicio.CALIFICADO, updated.getEstado());
        
        // Delete delivery
        repositorioEntregaEjercicio.delete(saved);
        Optional<EntregaEjercicio> deleted = repositorioEntregaEjercicio.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("testDateRangeQueries debe probar consultas por rango de fechas")
    public void testDateRangeQueries() {
        // Create test deliveries with different dates
        LocalDateTime now = LocalDateTime.now();
        
        EntregaEjercicio pastDelivery = new EntregaEjercicio(alumno1, ejercicio1);
        pastDelivery.setFechaEntrega(now.minusDays(10));
        pastDelivery.setEstado(EEstadoEjercicio.CALIFICADO);
        pastDelivery.setNota(new BigDecimal("7.0"));
        
        EntregaEjercicio currentDelivery = new EntregaEjercicio(alumno2, ejercicio1);
        currentDelivery.setFechaEntrega(now);
        currentDelivery.setEstado(EEstadoEjercicio.ENTREGADO);
        
        EntregaEjercicio futureDelivery = new EntregaEjercicio(alumno3, ejercicio1);
        futureDelivery.setFechaEntrega(now.plusDays(5));
        futureDelivery.setEstado(EEstadoEjercicio.PENDIENTE);
        
        repositorioEntregaEjercicio.save(pastDelivery);
        repositorioEntregaEjercicio.save(currentDelivery);
        repositorioEntregaEjercicio.save(futureDelivery);
        
        // Test date range queries
        List<EntregaEjercicio> dateRange = repositorioEntregaEjercicio.findByFechaEntregaBetween(
            now.minusDays(5), now.plusDays(5)
        );
        assertFalse(dateRange.isEmpty());
        assertTrue(dateRange.size() >= 1); // Should include at least current delivery
    }

    @Test
    @DisplayName("testGradeRangeQueries debe probar consultas por rango de notas")
    public void testGradeRangeQueries() {
        // Create test deliveries with different grades
        EntregaEjercicio lowGrade = new EntregaEjercicio(alumno1, ejercicio1);
        lowGrade.setFechaEntrega(LocalDateTime.now());
        lowGrade.setEstado(EEstadoEjercicio.CALIFICADO);
        lowGrade.setNota(new BigDecimal("5.0"));
        
        EntregaEjercicio mediumGrade = new EntregaEjercicio(alumno2, ejercicio1);
        mediumGrade.setFechaEntrega(LocalDateTime.now());
        mediumGrade.setEstado(EEstadoEjercicio.CALIFICADO);
        mediumGrade.setNota(new BigDecimal("7.5"));
        
        EntregaEjercicio highGrade = new EntregaEjercicio(alumno3, ejercicio1);
        highGrade.setFechaEntrega(LocalDateTime.now());
        highGrade.setEstado(EEstadoEjercicio.CALIFICADO);
        highGrade.setNota(new BigDecimal("9.5"));
        
        repositorioEntregaEjercicio.save(lowGrade);
        repositorioEntregaEjercicio.save(mediumGrade);
        repositorioEntregaEjercicio.save(highGrade);
        
        // Test grade range queries
        List<EntregaEjercicio> mediumGrades = repositorioEntregaEjercicio.findByNotaBetween(
            new BigDecimal("6.0"), new BigDecimal("8.0")
        );
        assertFalse(mediumGrades.isEmpty());
        assertTrue(mediumGrades.stream().allMatch(e -> 
            e.getNota().compareTo(new BigDecimal("6.0")) >= 0 && 
            e.getNota().compareTo(new BigDecimal("8.0")) <= 0
        ));
    }

    @Test
    @DisplayName("testMultipleDeliveries debe probar múltiples entregas")
    public void testMultipleDeliveries() {
        // Create multiple deliveries for the same exercise
        EntregaEjercicio entrega1 = new EntregaEjercicio(alumno1, ejercicio1);
        entrega1.setEstado(EEstadoEjercicio.ENTREGADO);
        
        EntregaEjercicio entrega2 = new EntregaEjercicio(alumno2, ejercicio1);
        entrega2.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega2.setNota(new BigDecimal("8.0"));
        
        EntregaEjercicio entrega3 = new EntregaEjercicio(alumno3, ejercicio2);
        entrega3.setEstado(EEstadoEjercicio.PENDIENTE);
        
        repositorioEntregaEjercicio.save(entrega1);
        repositorioEntregaEjercicio.save(entrega2);
        repositorioEntregaEjercicio.save(entrega3);
        
        // Test finding deliveries by exercise
        List<EntregaEjercicio> entregasEjercicio1 = repositorioEntregaEjercicio.findByEjercicioId(ejercicio1.getId());
        assertEquals(2, entregasEjercicio1.size());
        
        // Test finding deliveries by student
        List<EntregaEjercicio> entregasAlumno1 = repositorioEntregaEjercicio.findByAlumnoEntreganteId(alumno1.getId());
        assertEquals(1, entregasAlumno1.size());
        
        // Test finding deliveries by status
        List<EntregaEjercicio> entregasEntregadas = repositorioEntregaEjercicio.findByEstado(EEstadoEjercicio.ENTREGADO);
        assertEquals(1, entregasEntregadas.size());
        
        List<EntregaEjercicio> entregasCalificadas = repositorioEntregaEjercicio.findByEstado(EEstadoEjercicio.CALIFICADO);
        assertEquals(1, entregasCalificadas.size());
    }
}
