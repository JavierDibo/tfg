package app.util.datainit;

import app.dtos.DTOAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.servicios.ServicioAlumno;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StudentDataInitializer extends BaseDataInitializer {
    private static final int NUM_STUDENTS = 50;
    private final List<DTOAlumno> createdStudents = new ArrayList<>();

    @Override
    public void initialize() {
        ServicioAlumno servicioAlumno = context.getBean(ServicioAlumno.class);
        
        // Initialize password encoder
        initializePasswordEncoder();
        
        System.out.println("Creating hardcoded student and " + NUM_STUDENTS + " additional students...");
        
        // Create hardcoded student first
        DTOPeticionRegistroAlumno hardcodedStudent = new DTOPeticionRegistroAlumno(
            "estudiante", // username
            "password", // password
            "Estudiante",
            "Demo Apellido",
            generateUniqueDNI(NUM_STUDENTS),
            "estudiante@academia.com",
            "600000000"
        );
        
        try {
            DTOAlumno alumno = servicioAlumno.crearAlumno(hardcodedStudent);
            createdStudents.add(alumno);
            System.out.println("✓ Created hardcoded student: estudiante with password: password");
        } catch (Exception e) {
            System.err.println("✗ Error creating hardcoded student: " + e.getMessage());
        }
        
        for (int i = 0; i < NUM_STUDENTS; i++) {
            String[] nombreCompleto = generateRandomNames();
            String email = "user" + (i + 1) + "@academia.com";
            String dni = generateUniqueDNI(i);
            String telefono = generateRandomPhone();
            
            // Create student using the record constructor
            String username = removeAccents(nombreCompleto[0]) + i;
            
            // Note: The service will handle password encoding internally,
            // but we can also encode it here for consistency
            String rawPassword = "password";
            String encodedPassword = encodePassword(rawPassword);
                
            DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
                username, // username without accents
                rawPassword, // The service will encode this password
                nombreCompleto[0],
                nombreCompleto[1] + " " + nombreCompleto[2], // apellidos
                dni,
                email,
                telefono
            );
            
            try {
                DTOAlumno alumno = servicioAlumno.crearAlumno(dto);
                createdStudents.add(alumno);
                System.out.println("✓ Created student: " + username + " with encoded password");
            } catch (Exception e) {
                System.err.println("✗ Error creating student: " + e.getMessage());
            }
        }
        
        System.out.println("✓ Student creation completed. Total created: " + createdStudents.size());
    }
    
    public List<DTOAlumno> getCreatedStudents() {
        return createdStudents;
    }
}
