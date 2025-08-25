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
            adminInitializer.initialize();
            System.out.println("Admin user created");
            
            professorInitializer.initialize();
            System.out.println("Professors created");
            
            studentInitializer.initialize();
            System.out.println("Students created");
            
            materialDataInitializer.initialize();
            System.out.println("Materials created");
            
            courseInitializer.initialize();
            
            studentEnrollmentInitializer.initialize();
            
            System.out.println("=== Data Initialization Completed ===");
            
        } catch (Exception e) {
            System.err.println("❌ Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
