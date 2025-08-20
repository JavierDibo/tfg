package app.validation;

import app.dtos.DTOActualizacionAlumno;
import app.dtos.DTOPeticionRegistroAlumno;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para validar las validaciones personalizadas
 */
public class ValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidDNI() {
        // DNI válido
        DTOActualizacionAlumno validDto = new DTOActualizacionAlumno(null, null, "12345678Z", null, null);
        Set<ConstraintViolation<DTOActualizacionAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // DNI inválido
        DTOActualizacionAlumno invalidDto = new DTOActualizacionAlumno(null, null, "12345678A", null, null);
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testValidEmail() {
        // Email válido
        DTOActualizacionAlumno validDto = new DTOActualizacionAlumno(null, null, null, "test@ejemplo.com", null);
        Set<ConstraintViolation<DTOActualizacionAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Email inválido
        DTOActualizacionAlumno invalidDto = new DTOActualizacionAlumno(null, null, null, "email-invalido", null);
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testValidPhone() {
        // Teléfono válido
        DTOActualizacionAlumno validDto = new DTOActualizacionAlumno(null, null, null, null, "123456789");
        Set<ConstraintViolation<DTOActualizacionAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Teléfono con prefijo válido
        validDto = new DTOActualizacionAlumno(null, null, null, null, "+34 123 456 789");
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Teléfono inválido (muy corto)
        DTOActualizacionAlumno invalidDto = new DTOActualizacionAlumno(null, null, null, null, "123");
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testNotEmptyIfPresent() {
        // Campo vacío (inválido)
        DTOActualizacionAlumno invalidDto = new DTOActualizacionAlumno("", null, null, null, null);
        Set<ConstraintViolation<DTOActualizacionAlumno>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
        
        // Campo null (válido para actualización parcial)
        DTOActualizacionAlumno validDto = new DTOActualizacionAlumno(null, null, null, null, null);
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Campo con contenido (válido)
        validDto = new DTOActualizacionAlumno("Juan", null, null, null, null);
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testCompleteRegistration() {
        // Registro válido completo
        DTOPeticionRegistroAlumno validRegistro = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );
        
        Set<ConstraintViolation<DTOPeticionRegistroAlumno>> violations = validator.validate(validRegistro);
        assertTrue(violations.isEmpty());
        
        // Registro con email inválido
        DTOPeticionRegistroAlumno invalidRegistro = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "email-invalido",
            "123456789"
        );
        
        violations = validator.validate(invalidRegistro);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}
