package app.util.datainit;

import app.dtos.DTOCurso;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOProfesor;
import app.entidades.enums.EDificultad;
import app.entidades.enums.EPresencialidad;
import app.servicios.ServicioClase;
import app.servicios.ServicioMaterial;
import app.entidades.Material;
import app.repositorios.RepositorioMaterial;
import app.repositorios.RepositorioClase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void initialize() {
        // Set up security context for course creation (as a teacher)
        setupSecurityContext();
        
        ServicioClase servicioClase = context.getBean(ServicioClase.class);
        ServicioMaterial servicioMaterial = context.getBean(ServicioMaterial.class);
        RepositorioMaterial repositorioMaterial = context.getBean(RepositorioMaterial.class);
        RepositorioClase repositorioClase = context.getBean(RepositorioClase.class);
        ProfessorDataInitializer professorInit = context.getBean(ProfessorDataInitializer.class);
        
        // Get all available materials to associate with courses (get actual entities from repository)
        List<Material> availableMaterials = repositorioMaterial.findAll();
        
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
                
                // Select random materials for this course (minimum 2 materials per course, up to 4)
                // Use Material IDs instead of Material objects to avoid Hibernate session conflicts
                List<String> courseMaterialIds = selectRandomMaterialIds(availableMaterials, 2, 4);
                
                // Fetch Material objects from database using IDs to avoid session conflicts
                List<Material> courseMaterials = fetchMaterialsByIds(courseMaterialIds);
                
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
        
        // Log material distribution summary
        int totalMaterialsAssigned = createdCourses.stream()
            .mapToInt(course -> course.material().size())
            .sum();
        System.out.println("Courses created: " + createdCourses.size() + " (avg " + 
            String.format("%.1f", (double) totalMaterialsAssigned / createdCourses.size()) + " materials each)");
    }
    
    private void setupSecurityContext() {
        // Create a teacher authentication context for course creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("teacher-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
     * Selects a random number of material IDs from the available materials list
     * Ensures every course has at least 2 materials as per requirements
     * @param availableMaterials List of all available materials
     * @param minCount Minimum number of materials to select (should be 2 or more)
     * @param maxCount Maximum number of materials to select
     * @return List of randomly selected material IDs
     */
    private List<String> selectRandomMaterialIds(List<Material> availableMaterials, int minCount, int maxCount) {
        if (availableMaterials.isEmpty()) {
            System.err.println("Warning: No materials available for course assignment");
            return new ArrayList<>();
        }
        
        // Ensure we have enough materials to meet the minimum requirement
        if (availableMaterials.size() < minCount) {
            System.err.println("Warning: Only " + availableMaterials.size() + " materials available, but minimum required is " + minCount);
            return availableMaterials.stream().map(Material::getId).toList();
        }
        
        List<Material> shuffledMaterials = new ArrayList<>(availableMaterials);
        Collections.shuffle(shuffledMaterials);
        
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        count = Math.min(count, shuffledMaterials.size());
        
        return shuffledMaterials.subList(0, count).stream()
            .map(Material::getId)
            .toList();
    }
    
    /**
     * Fetches Material objects from database using IDs to avoid Hibernate session conflicts
     * @param materialIds List of material IDs to fetch
     * @return List of Material objects
     */
    private List<Material> fetchMaterialsByIds(List<String> materialIds) {
        List<Material> materials = new ArrayList<>();
        RepositorioMaterial repositorioMaterial = context.getBean(RepositorioMaterial.class);
        
        for (String materialId : materialIds) {
            try {
                // Use repository to get existing materials
                Material material = repositorioMaterial.findById(materialId).orElse(null);
                if (material != null) {
                    materials.add(material);
                } else {
                    System.err.println("Warning: Material with ID " + materialId + " not found in database");
                }
            } catch (Exception e) {
                System.err.println("Error fetching material with ID " + materialId + ": " + e.getMessage());
            }
        }
        return materials;
    }
    
    /**
     * Selects a random number of materials from the available materials list
     * Creates new Material references to avoid Hibernate session conflicts
     * Ensures every course has at least 2 materials as per requirements
     * @param availableMaterials List of all available materials
     * @param minCount Minimum number of materials to select (should be 2 or more)
     * @param maxCount Maximum number of materials to select
     * @return List of randomly selected materials with new references
     * @deprecated Use selectRandomMaterialIds instead to avoid Hibernate session conflicts
     */
    @Deprecated
    private List<Material> selectRandomMaterialsWithNewReferences(List<Material> availableMaterials, int minCount, int maxCount) {
        if (availableMaterials.isEmpty()) {
            System.err.println("Warning: No materials available for course assignment");
            return new ArrayList<>();
        }
        
        // Ensure we have enough materials to meet the minimum requirement
        if (availableMaterials.size() < minCount) {
            System.err.println("Warning: Only " + availableMaterials.size() + " materials available, but minimum required is " + minCount);
            return createNewMaterialReferences(availableMaterials);
        }
        
        List<Material> shuffledMaterials = new ArrayList<>(availableMaterials);
        Collections.shuffle(shuffledMaterials);
        
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        count = Math.min(count, shuffledMaterials.size());
        
        List<Material> selectedMaterials = shuffledMaterials.subList(0, count);
        
        // Create new Material references to avoid Hibernate session conflicts
        return createNewMaterialReferences(selectedMaterials);
    }
    
    /**
     * Creates new Material references from existing materials to avoid Hibernate session conflicts
     * @param materials List of materials to create new references for
     * @return List of new Material references
     * @deprecated Use fetchMaterialsByIds instead to avoid Hibernate session conflicts
     */
    @Deprecated
    private List<Material> createNewMaterialReferences(List<Material> materials) {
        List<Material> newReferences = new ArrayList<>();
        for (Material material : materials) {
            // Create a new Material object with the same data to avoid session conflicts
            Material newMaterial = new Material(
                material.getId(),
                material.getName(),
                material.getUrl()
            );
            newReferences.add(newMaterial);
        }
        return newReferences;
    }
}
