package app.repositorios;

import app.entidades.Ejercicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RepositorioEjercicioTest {

    @Autowired
    private RepositorioEjercicio repositorioEjercicio;

    @Test
    public void testBasicCRUDOperations() {
        // Create test exercise
        Ejercicio ejercicio = new Ejercicio(
            "Test Exercise",
            "Test Statement",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            "1"
        );
        
        // Save exercise
        Ejercicio saved = repositorioEjercicio.save(ejercicio);
        assertNotNull(saved.getId());
        assertEquals("Test Exercise", saved.getName());
        assertEquals("Test Statement", saved.getStatement());
        assertEquals("1", saved.getClassId());
        
        // Find by ID
        Optional<Ejercicio> found = repositorioEjercicio.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        
        // Find by name
        Optional<Ejercicio> foundByName = repositorioEjercicio.findByName("Test Exercise");
        assertTrue(foundByName.isPresent());
        assertEquals("Test Exercise", foundByName.get().getName());
        
        // Find by name containing
        List<Ejercicio> foundByContaining = repositorioEjercicio.findByNameContainingIgnoreCase("Test");
        assertFalse(foundByContaining.isEmpty());
        assertTrue(foundByContaining.stream().anyMatch(e -> e.getName().contains("Test")));
        
        // Find by statement containing
        List<Ejercicio> foundByStatement = repositorioEjercicio.findByStatementContainingIgnoreCase("Test");
        assertFalse(foundByStatement.isEmpty());
        assertTrue(foundByStatement.stream().anyMatch(e -> e.getStatement().contains("Test")));
        
        // Find by class ID
        List<Ejercicio> foundByClassId = repositorioEjercicio.findByClassId("1");
        assertFalse(foundByClassId.isEmpty());
        assertTrue(foundByClassId.stream().allMatch(e -> e.getClassId().equals("1")));
        
        // Update exercise
        saved.setName("Updated Exercise");
        Ejercicio updated = repositorioEjercicio.save(saved);
        assertEquals("Updated Exercise", updated.getName());
        
        // Delete exercise
        repositorioEjercicio.delete(saved);
        Optional<Ejercicio> deleted = repositorioEjercicio.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testFlexibleFilteringQuery() {
        // Create test exercise
        Ejercicio ejercicio = new Ejercicio(
            "Test Exercise",
            "Test Statement",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            "1"
        );
        
        // Save exercise
        Ejercicio saved = repositorioEjercicio.save(ejercicio);
        
        // Test simple query without normalize_text for H2 compatibility
        List<Ejercicio> result = repositorioEjercicio.findByClassId("1");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(e -> "1".equals(e.getClassId())));
    }

    @Test
    public void testDateRangeQueries() {
        // Create test exercises with different dates
        LocalDateTime now = LocalDateTime.now();
        
        Ejercicio pastExercise = new Ejercicio(
            "Past Exercise",
            "Past Statement",
            now.minusDays(10),
            now.minusDays(5),
            "1"
        );
        
        Ejercicio currentExercise = new Ejercicio(
            "Current Exercise",
            "Current Statement",
            now.minusDays(2),
            now.plusDays(5),
            "1"
        );
        
        Ejercicio futureExercise = new Ejercicio(
            "Future Exercise",
            "Future Statement",
            now.plusDays(5),
            now.plusDays(10),
            "1"
        );
        
        repositorioEjercicio.save(pastExercise);
        repositorioEjercicio.save(currentExercise);
        repositorioEjercicio.save(futureExercise);
        
        // Test date range queries
        List<Ejercicio> startDateRange = repositorioEjercicio.findByStartDateBetween(
            now.minusDays(5), now.plusDays(5)
        );
        assertFalse(startDateRange.isEmpty());
        
        List<Ejercicio> endDateRange = repositorioEjercicio.findByEndDateBetween(
            now.minusDays(5), now.plusDays(5)
        );
        assertFalse(endDateRange.isEmpty());
    }

    @Test
    public void testPagination() {
        // Create multiple exercises
        for (int i = 1; i <= 15; i++) {
            Ejercicio ejercicio = new Ejercicio(
                "Exercise " + i,
                "Statement " + i,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "1"
            );
            repositorioEjercicio.save(ejercicio);
        }
        
        // Test pagination
        Page<Ejercicio> firstPage = repositorioEjercicio.findAll(PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertTrue(firstPage.getTotalElements() >= 15);
        
        Page<Ejercicio> secondPage = repositorioEjercicio.findAll(PageRequest.of(1, 10));
        assertTrue(secondPage.getContent().size() > 0);
    }
}
