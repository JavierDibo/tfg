package app.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test básico para verificar que los controladores refactorizados se pueden instanciar correctamente
 */
@SpringBootTest
@ActiveProfiles("test")
public class RefactoringTest {

    @Autowired(required = false)
    private ClaseRest claseRest;

    @Autowired(required = false)
    private EnrollmentRest enrollmentRest;

    @Autowired(required = false)
    private ClaseManagementRest claseManagementRest;

    @Autowired(required = false)
    private UserClaseRest userClaseRest;

    @Test
    public void testClaseRestInstantiation() {
        // Verificar que el controlador principal se puede instanciar
        assertNotNull(claseRest, "ClaseRest should be instantiated");
    }

    @Test
    public void testEnrollmentRestInstantiation() {
        // Verificar que el controlador de inscripciones se puede instanciar
        assertNotNull(enrollmentRest, "EnrollmentRest should be instantiated");
    }

    @Test
    public void testClaseManagementRestInstantiation() {
        // Verificar que el controlador de gestión se puede instanciar
        assertNotNull(claseManagementRest, "ClaseManagementRest should be instantiated");
    }

    @Test
    public void testUserClaseRestInstantiation() {
        // Verificar que el controlador de usuario se puede instanciar
        assertNotNull(userClaseRest, "UserClaseRest should be instantiated");
    }
}
