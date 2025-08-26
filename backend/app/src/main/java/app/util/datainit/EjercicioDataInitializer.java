package app.util.datainit;

import app.dtos.DTOEjercicio;
import app.dtos.DTOPeticionCrearEjercicio;
import app.servicios.ServicioEjercicio;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class EjercicioDataInitializer extends BaseDataInitializer {
    private static final int EXERCISES_PER_COURSE = 3;
    private final List<DTOEjercicio> createdExercises = new ArrayList<>();
    private final Random random = new Random();
    
    @Override
    public void initialize() {
        // Set up security context for exercise creation (as a teacher)
        setupSecurityContext();
        
        ServicioEjercicio servicioEjercicio = context.getBean(ServicioEjercicio.class);
        CourseDataInitializer courseInit = context.getBean(CourseDataInitializer.class);
        
        // Get all created courses to assign exercises to them
        List<String> courseIds = courseInit.getCreatedCourses().stream()
            .map(course -> course.id().toString())
            .toList();
        
        if (courseIds.isEmpty()) {
            System.err.println("Warning: No courses available for exercise assignment");
            return;
        }
        
        for (String courseId : courseIds) {
            for (int i = 0; i < EXERCISES_PER_COURSE; i++) {
                String exerciseName = generateRandomExerciseName();
                String statement = generateRandomStatement(exerciseName);
                
                // Generate dates: start in the past, end in the future
                LocalDateTime startDate = generateRandomStartDate();
                LocalDateTime endDate = generateRandomEndDate(startDate);
                
                try {
                    DTOEjercicio dtoEjercicio = servicioEjercicio.crearEjercicio(
                        exerciseName,
                        statement,
                        startDate,
                        endDate,
                        courseId
                    );
                    
                    if (dtoEjercicio != null) {
                        createdExercises.add(dtoEjercicio);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error creating exercise: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Exercises created: " + createdExercises.size() + 
            " (" + EXERCISES_PER_COURSE + " per course)");
    }
    
    private void setupSecurityContext() {
        // Create a teacher authentication context for exercise creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("teacher-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private String generateRandomExerciseName() {
        String[] exerciseTypes = {
            "Tarea de Programación", "Ejercicio de Matemáticas", "Problema de Física",
            "Análisis de Texto", "Proyecto Final", "Examen Parcial", "Práctica de Laboratorio",
            "Investigación", "Presentación", "Ensayo", "Cuestionario", "Trabajo Grupal"
        };
        
        String[] subjects = {
            "Java", "Python", "C++", "JavaScript", "HTML/CSS", "SQL", "Algoritmos",
            "Cálculo", "Álgebra", "Estadística", "Mecánica", "Termodinámica",
            "Literatura", "Historia", "Filosofía", "Economía", "Psicología"
        };
        
        String type = exerciseTypes[random.nextInt(exerciseTypes.length)];
        String subject = subjects[random.nextInt(subjects.length)];
        
        return type + " - " + subject + " #" + (random.nextInt(100) + 1);
    }
    
    private String generateRandomStatement(String exerciseName) {
        String[] templates = {
            "Desarrolla una aplicación que implemente %s. El proyecto debe incluir documentación completa y casos de prueba.",
            "Resuelve los siguientes problemas de %s. Muestra todos los pasos de tu razonamiento.",
            "Analiza el texto proporcionado sobre %s y escribe un ensayo de 500-800 palabras.",
            "Crea una presentación sobre %s que incluya ejemplos prácticos y aplicaciones reales.",
            "Realiza un experimento de laboratorio sobre %s y documenta tus hallazgos.",
            "Investiga sobre %s y presenta un informe con al menos 3 fuentes bibliográficas.",
            "Desarrolla un proyecto en grupo sobre %s. Cada miembro debe contribuir significativamente.",
            "Completa el cuestionario sobre %s. Las respuestas deben estar justificadas.",
            "Implementa un algoritmo de %s y optimiza su rendimiento.",
            "Diseña una base de datos para %s siguiendo las mejores prácticas de normalización."
        };
        
        String template = templates[random.nextInt(templates.length)];
        String subject = exerciseName.split(" - ")[1].split(" #")[0];
        
        return String.format(template, subject);
    }
    
    private LocalDateTime generateRandomStartDate() {
        // Start dates: from 7 days ago to 14 days in the future (more realistic)
        int daysOffset = random.nextInt(21) - 7; // -7 to +13
        return LocalDateTime.now().plusDays(daysOffset);
    }
    
    private LocalDateTime generateRandomEndDate(LocalDateTime startDate) {
        // End dates: from 7 days to 30 days after start date
        int daysDuration = random.nextInt(24) + 7; // 7 to 30 days
        return startDate.plusDays(daysDuration);
    }
    
    public List<DTOEjercicio> getCreatedExercises() {
        return createdExercises;
    }
}
