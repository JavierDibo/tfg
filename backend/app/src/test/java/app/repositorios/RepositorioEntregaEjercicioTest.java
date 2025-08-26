package app.repositorios;

import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RepositorioEntregaEjercicioTest {

    @Autowired
    private RepositorioEntregaEjercicio repositorioEntregaEjercicio;

    @Test
    public void testBasicCRUDOperations() {
        // Create test delivery
        EntregaEjercicio entrega = new EntregaEjercicio();
        entrega.setAlumnoEntreganteId("1");
        entrega.setEjercicioId("1");
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega.setNota(new BigDecimal("8.5"));
        entrega.setComentarios("Test delivery");
        
        // Save delivery
        EntregaEjercicio saved = repositorioEntregaEjercicio.save(entrega);
        assertNotNull(saved.getId());
        assertEquals("1", saved.getAlumnoEntreganteId());
        assertEquals("1", saved.getEjercicioId());
        assertEquals(EEstadoEjercicio.ENTREGADO, saved.getEstado());
        assertEquals(new BigDecimal("8.5"), saved.getNota());
        assertEquals("Test delivery", saved.getComentarios());
        
        // Find by ID
        Optional<EntregaEjercicio> found = repositorioEntregaEjercicio.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        
        // Find by alumno ID
        List<EntregaEjercicio> foundByAlumno = repositorioEntregaEjercicio.findByAlumnoEntreganteId("1");
        assertFalse(foundByAlumno.isEmpty());
        assertTrue(foundByAlumno.stream().allMatch(e -> e.getAlumnoEntreganteId().equals("1")));
        
        // Find by ejercicio ID
        List<EntregaEjercicio> foundByEjercicio = repositorioEntregaEjercicio.findByEjercicioId("1");
        assertFalse(foundByEjercicio.isEmpty());
        assertTrue(foundByEjercicio.stream().allMatch(e -> e.getEjercicioId().equals("1")));
        
        // Find by estado
        List<EntregaEjercicio> foundByEstado = repositorioEntregaEjercicio.findByEstado(EEstadoEjercicio.ENTREGADO);
        assertFalse(foundByEstado.isEmpty());
        assertTrue(foundByEstado.stream().allMatch(e -> e.getEstado() == EEstadoEjercicio.ENTREGADO));
        
        // Find by alumno and ejercicio
        Optional<EntregaEjercicio> foundByAlumnoAndEjercicio = repositorioEntregaEjercicio
            .findByAlumnoEntreganteIdAndEjercicioId("1", "1");
        assertTrue(foundByAlumnoAndEjercicio.isPresent());
        assertEquals("1", foundByAlumnoAndEjercicio.get().getAlumnoEntreganteId());
        assertEquals("1", foundByAlumnoAndEjercicio.get().getEjercicioId());
        
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
    public void testDateRangeQueries() {
        // Create test deliveries with different dates
        LocalDateTime now = LocalDateTime.now();
        
        EntregaEjercicio pastDelivery = new EntregaEjercicio();
        pastDelivery.setAlumnoEntreganteId("1");
        pastDelivery.setEjercicioId("1");
        pastDelivery.setFechaEntrega(now.minusDays(10));
        pastDelivery.setEstado(EEstadoEjercicio.CALIFICADO);
        pastDelivery.setNota(new BigDecimal("7.0"));
        
        EntregaEjercicio currentDelivery = new EntregaEjercicio();
        currentDelivery.setAlumnoEntreganteId("2");
        currentDelivery.setEjercicioId("1");
        currentDelivery.setFechaEntrega(now);
        currentDelivery.setEstado(EEstadoEjercicio.ENTREGADO);
        
        EntregaEjercicio futureDelivery = new EntregaEjercicio();
        futureDelivery.setAlumnoEntreganteId("3");
        futureDelivery.setEjercicioId("1");
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
    public void testGradeRangeQueries() {
        // Create test deliveries with different grades
        EntregaEjercicio lowGrade = new EntregaEjercicio();
        lowGrade.setAlumnoEntreganteId("1");
        lowGrade.setEjercicioId("1");
        lowGrade.setFechaEntrega(LocalDateTime.now());
        lowGrade.setEstado(EEstadoEjercicio.CALIFICADO);
        lowGrade.setNota(new BigDecimal("5.0"));
        
        EntregaEjercicio mediumGrade = new EntregaEjercicio();
        mediumGrade.setAlumnoEntreganteId("2");
        mediumGrade.setEjercicioId("1");
        mediumGrade.setFechaEntrega(LocalDateTime.now());
        mediumGrade.setEstado(EEstadoEjercicio.CALIFICADO);
        mediumGrade.setNota(new BigDecimal("7.5"));
        
        EntregaEjercicio highGrade = new EntregaEjercicio();
        highGrade.setAlumnoEntreganteId("3");
        highGrade.setEjercicioId("1");
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
}
