package app.util.datainit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializationCoordinator {
    
    @Autowired
    private AdminDataInitializer adminInitializer;
    
    @Autowired
    private StudentDataInitializer studentInitializer;
    
    @Autowired
    private ProfessorDataInitializer professorInitializer;
    
    @Autowired
    private CourseDataInitializer courseInitializer;
    
    @Autowired
    private StudentEnrollmentInitializer studentEnrollmentInitializer;
    
    @Autowired
    private MaterialDataInitializer materialDataInitializer;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        System.out.println("=== Starting Data Initialization ===");
        
        try {
            System.out.println("Creating admin user...");
            adminInitializer.initialize();
            System.out.println("Admin initialized successfully: (admin,admin)");
            
            System.out.println("Creating professors...");
            professorInitializer.initialize();
            
            System.out.println("Creating students...");
            studentInitializer.initialize();
            
            System.out.println("Creating courses...");
            courseInitializer.initialize();
            
            System.out.println("Enrolling students in courses...");
            studentEnrollmentInitializer.initialize();
            
            System.out.println("Creating materials...");
            materialDataInitializer.initialize();
            
            System.out.println("=== Data Initialization Completed Successfully ===");
            
        } catch (Exception e) {
            System.err.println("❌ Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
