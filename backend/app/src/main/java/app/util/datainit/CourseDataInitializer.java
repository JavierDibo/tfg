package app.util.datainit;

import app.dtos.DTOCurso;
import app.dtos.DTOPeticionCrearClase;
import app.dtos.DTOProfesor;
import app.entidades.enums.ENivel;
import app.entidades.enums.EPresencialidad;
import app.servicios.ServicioClase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CourseDataInitializer extends BaseDataInitializer {
    private static final int COURSES_PER_PROFESSOR = 2;
    private final List<DTOCurso> createdCourses = new ArrayList<>();

    @Override
    public void initialize() {
        ServicioClase servicioClase = context.getBean(ServicioClase.class);
        ProfessorDataInitializer professorInit = context.getBean(ProfessorDataInitializer.class);
        
        System.out.println("Creating courses for professors...");
        
        for (DTOProfesor profesor : professorInit.getCreatedProfessors()) {
            for (int i = 0; i < COURSES_PER_PROFESSOR; i++) {
                String title = generateRandomCourseName();
                String description = "Curso de " + title + " impartido por " + profesor.nombre();
                BigDecimal price = BigDecimal.valueOf(generateRandomPrice());
                EPresencialidad presentiality = generateRandomPresentiality();
                ENivel level = generateRandomLevel();
                
                // Create dates for the course (start tomorrow, end in 3 months)
                LocalDate startDate = LocalDate.now().plusDays(1);
                LocalDate endDate = startDate.plusMonths(3);
                
                List<String> profesorIds = Collections.singletonList(profesor.id().toString());
                
                // Create course using the record constructor with simplified version
                DTOPeticionCrearClase dto = new DTOPeticionCrearClase(
                    title,
                    description,
                    price,
                    presentiality,
                    null, // imagen portada
                    level,
                    profesorIds,
                    null // material
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
        
        System.out.println("✓ Course creation completed. Total created: " + createdCourses.size());
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
    
    private ENivel generateRandomLevel() {
        ENivel[] levels = ENivel.values();
        return levels[random.nextInt(levels.length)];
    }
    
    private EPresencialidad generateRandomPresentiality() {
        EPresencialidad[] types = EPresencialidad.values();
        return types[random.nextInt(types.length)];
    }
    
    public List<DTOCurso> getCreatedCourses() {
        return createdCourses;
    }
}
