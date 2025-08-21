package app.util.datainit;

import app.dtos.DTOAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import app.entidades.Usuario;
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
        
        for (int i = 0; i < NUM_STUDENTS; i++) {
            String[] nombreCompleto = generateRandomNames();
            String email = generateRandomEmail(nombreCompleto[0], nombreCompleto[1], i);
            String dni = generateUniqueDNI(i);
            String telefono = generateRandomPhone();
            
            // Create student using the record constructor
            String username = removeAccents(nombreCompleto[0]) + i;
                
            DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
                username, // username without accents
                "password",
                nombreCompleto[0],
                nombreCompleto[1] + " " + nombreCompleto[2], // apellidos
                dni,
                email,
                telefono
            );
            
            try {
                DTOAlumno alumno = servicioAlumno.crearAlumno(dto);
                createdStudents.add(alumno);
            } catch (Exception e) {
                System.err.println("Error creating student: " + e.getMessage());
            }
        }
    }
    
    public List<DTOAlumno> getCreatedStudents() {
        return createdStudents;
    }
}
