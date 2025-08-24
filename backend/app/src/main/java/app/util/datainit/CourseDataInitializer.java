package app.util.datainit;

import app.dtos.DTOCurso;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOProfesor;
import app.entidades.enums.EDificultad;
import app.entidades.enums.EPresencialidad;
import app.servicios.ServicioClase;
import app.servicios.ServicioMaterial;
import app.entidades.Material;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class CourseDataInitializer extends BaseDataInitializer {
    private static final int COURSES_PER_PROFESSOR = 1;
    private final List<DTOCurso> createdCourses = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void initialize() {
        ServicioClase servicioClase = context.getBean(ServicioClase.class);
        ServicioMaterial servicioMaterial = context.getBean(ServicioMaterial.class);
        ProfessorDataInitializer professorInit = context.getBean(ProfessorDataInitializer.class);
        
        // Get all available materials to associate with courses
        List<Material> availableMaterials = servicioMaterial.obtenerTodosLosMateriales().stream()
            .map(dto -> new Material(dto.id(), dto.name(), dto.url()))
            .toList();
        
        for (DTOProfesor profesor : professorInit.getCreatedProfessors()) {
            for (int i = 0; i < COURSES_PER_PROFESSOR; i++) {
                String title = generateRandomCourseName();
                String description = "Curso de " + title + " impartido por " + profesor.firstName();
                BigDecimal price = BigDecimal.valueOf(generateRandomPrice());
                EPresencialidad presentiality = generateRandomPresentiality();
                EDificultad level = generateRandomLevel();
                
                // Create dates for the course (start tomorrow, end in 3 months)
                LocalDate startDate = LocalDate.now().plusDays(1);
                LocalDate endDate = startDate.plusMonths(3);
                
                List<String> profesorIds = Collections.singletonList(profesor.id().toString());
                
                // Select random materials for this course (1-3 materials per course)
                List<Material> courseMaterials = selectRandomMaterials(availableMaterials, 1, 3);
                
                // Create course using the record constructor with simplified version
                DTOPeticionCrearClase dto = new DTOPeticionCrearClase(
                    title,
                    description,
                    price,
                    presentiality,
                    null, // imagen portada
                    level,
                    profesorIds,
                    courseMaterials
                );
                
                try {
                    DTOCurso dtoCurso = servicioClase.crearCurso(dto, startDate, endDate);
                    if (dtoCurso != null) {
                        createdCourses.add(dtoCurso);
                    }
                } catch (Exception e) {
                    System.err.println("Error creating course: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Course creation completed. Total created: " + createdCourses.size());
    }
    
    private String generateRandomCourseName() {
        String[] subjects = {
            "Matemáticas Avanzadas", "Física Cuántica", "Química Orgánica",
            "Programación Java", "Diseño Web", "Bases de Datos",
            "Literatura Española", "Historia del Arte", "Inglés Empresarial"
        };
        return subjects[random.nextInt(subjects.length)];
    }
    
    private double generateRandomPrice() {
        return 20.0 + (random.nextDouble() * 180.0); // Prices between 20 and 200
    }
    
    private EDificultad generateRandomLevel() {
        EDificultad[] levels = EDificultad.values();
        return levels[random.nextInt(levels.length)];
    }
    
    private EPresencialidad generateRandomPresentiality() {
        EPresencialidad[] types = EPresencialidad.values();
        return types[random.nextInt(types.length)];
    }
    
    public List<DTOCurso> getCreatedCourses() {
        return createdCourses;
    }
    
    /**
     * Selects a random number of materials from the available materials list
     * @param availableMaterials List of all available materials
     * @param minCount Minimum number of materials to select
     * @param maxCount Maximum number of materials to select
     * @return List of randomly selected materials
     */
    private List<Material> selectRandomMaterials(List<Material> availableMaterials, int minCount, int maxCount) {
        if (availableMaterials.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Material> shuffledMaterials = new ArrayList<>(availableMaterials);
        Collections.shuffle(shuffledMaterials);
        
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        count = Math.min(count, shuffledMaterials.size());
        
        return shuffledMaterials.subList(0, count);
    }
}
