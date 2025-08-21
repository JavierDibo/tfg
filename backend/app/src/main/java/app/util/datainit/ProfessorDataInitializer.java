package app.util.datainit;

import app.dtos.DTOProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.servicios.ServicioProfesor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfessorDataInitializer extends BaseDataInitializer {
    private static final int NUM_PROFESSORS = 20;
    private final List<DTOProfesor> createdProfessors = new ArrayList<>();

    @Override
    public void initialize() {
        ServicioProfesor servicioProfesor = context.getBean(ServicioProfesor.class);
        
        System.out.println("Creating " + NUM_PROFESSORS + " professors...");
        
        for (int i = 0; i < NUM_PROFESSORS; i++) {
            String[] nombreCompleto = generateRandomNames();
            String email = "prof" + (i + 1) + "@academia.com";
            String dni = generateUniqueDNI(i);
            String telefono = generateRandomPhone();
            
            // Create professor using the record constructor
            String username = "prof" + removeAccents(nombreCompleto[0]) + i;
                
            DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                username, // username without accents
                "password",
                nombreCompleto[0],
                nombreCompleto[1] + " " + nombreCompleto[2], // apellidos
                dni,
                email,
                telefono,
                null // clasesId (null for now)
            );
            
            try {
                DTOProfesor profesor = servicioProfesor.crearProfesor(dto);
                createdProfessors.add(profesor);
            } catch (Exception e) {
                System.err.println("Error creating professor: " + e.getMessage());
            }
        }
    }
    
    public List<DTOProfesor> getCreatedProfessors() {
        return createdProfessors;
    }
}
