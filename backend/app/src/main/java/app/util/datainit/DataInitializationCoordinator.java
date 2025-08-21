package app.util.datainit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializationCoordinator {
    
    @Autowired
    private StudentDataInitializer studentInitializer;
    
    @Autowired
    private ProfessorDataInitializer professorInitializer;
    
    @Autowired
    private CourseDataInitializer courseInitializer;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        System.out.println("Initializing sample data...");
        
        try {
            // Initialize in correct order due to dependencies
            professorInitializer.initialize();
            System.out.println("Professors initialized successfully");
            
            studentInitializer.initialize();
            System.out.println("Students initialized successfully");
            
            courseInitializer.initialize();
            System.out.println("Courses initialized successfully");
            
            System.out.println("Sample data initialization completed successfully");
            
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
