package app.dtos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOPeticionRegistroAlumno")
class DTOPeticionRegistroAlumnoTest {

    @Test
    @DisplayName("Constructor debe crear DTO correctamente")
    void testConstructor() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        assertEquals("usuario123", dto.username());
        assertEquals("password123", dto.password());
        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez García", dto.lastName());
        assertEquals("12345678Z", dto.dni());
        assertEquals("juan@ejemplo.com", dto.email());
        assertEquals("123456789", dto.phoneNumber());
    }

    @Test
    @DisplayName("Constructor debe manejar valores null en teléfono")
    void testConstructorConTelefonoNull() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            null
        );

        assertNull(dto.phoneNumber());
        assertNotNull(dto.username());
        assertNotNull(dto.password());
        assertNotNull(dto.firstName());
        assertNotNull(dto.lastName());
        assertNotNull(dto.dni());
        assertNotNull(dto.email());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testInmutabilidad() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        // Los records son inmutables por defecto, no se pueden modificar
        // Solo podemos verificar que los valores se mantienen
        assertEquals("usuario123", dto.username());
        assertEquals("password123", dto.password());
        assertEquals("Juan", dto.firstName());
    }

    @Test
    @DisplayName("Equals debe funcionar correctamente")
    void testEquals() {
        DTOPeticionRegistroAlumno dto1 = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        DTOPeticionRegistroAlumno dto2 = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        String toString = dto.toString();

        assertTrue(toString.contains("usuario123"));
        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("Pérez García"));
        assertTrue(toString.contains("juan@ejemplo.com"));
        // Los records incluyen todos los campos en toString por defecto
        assertTrue(toString.contains("password123"));
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes tipos de teléfono")
    void testDiferentesTiposTelefono() {
        // Teléfono con prefijo
        DTOPeticionRegistroAlumno dto1 = new DTOPeticionRegistroAlumno(
            "usuario1", "password1", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "+34 123 456 789"
        );
        assertEquals("+34 123 456 789", dto1.phoneNumber());

        // Teléfono sin prefijo
        DTOPeticionRegistroAlumno dto2 = new DTOPeticionRegistroAlumno(
            "usuario2", "password2", "María", "García", "87654321Y", "maria@ejemplo.com", "123456789"
        );
        assertEquals("123456789", dto2.phoneNumber());

        // Sin teléfono
        DTOPeticionRegistroAlumno dto3 = new DTOPeticionRegistroAlumno(
            "usuario3", "password3", "Carlos", "López", "11223344X", "carlos@ejemplo.com", null
        );
        assertNull(dto3.phoneNumber());
    }

    @Test
    @DisplayName("Constructor debe manejar nombres con acentos")
    void testNombresConAcentos() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "José María",
            "García López",
            "12345678Z",
            "jose@ejemplo.com",
            "123456789"
        );

        assertEquals("José María", dto.firstName());
        assertEquals("García López", dto.lastName());
    }

    @Test
    @DisplayName("Constructor debe manejar usuarios con caracteres especiales")
    void testUsuarioConCaracteresEspeciales() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario_123",
            "password123",
            "Juan",
            "Pérez",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        assertEquals("usuario_123", dto.username());
    }

    @Test
    @DisplayName("Constructor debe manejar DNI con letra")
    void testDNIConLetra() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );

        assertEquals("12345678Z", dto.dni());
    }

    @Test
    @DisplayName("Constructor debe manejar email con dominio")
    void testEmailConDominio() {
        DTOPeticionRegistroAlumno dto = new DTOPeticionRegistroAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez",
            "12345678Z",
            "juan.perez@empresa.com",
            "123456789"
        );

        assertEquals("juan.perez@empresa.com", dto.email());
    }
}
