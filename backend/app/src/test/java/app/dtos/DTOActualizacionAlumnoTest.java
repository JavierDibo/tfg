package app.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests para DTOActualizacionAlumno")
class DTOActualizacionAlumnoTest {

    @Test
    @DisplayName("Constructor debe crear DTO correctamente con todos los campos")
    void testConstructorCompleto() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            true,
            true
        );

        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez García", dto.lastName());
        assertEquals("12345678Z", dto.dni());
        assertEquals("juan@ejemplo.com", dto.email());
        assertEquals("123456789", dto.phoneNumber());
        assertTrue(dto.enrolled());
        assertTrue(dto.enabled());
    }

    @Test
    @DisplayName("Constructor debe crear DTO con campos null")
    void testConstructorConCamposNull() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertNull(dto.firstName());
        assertNull(dto.lastName());
        assertNull(dto.dni());
        assertNull(dto.email());
        assertNull(dto.phoneNumber());
        assertNull(dto.enrolled());
        assertNull(dto.enabled());
    }

    @Test
    @DisplayName("Constructor debe crear DTO con algunos campos null")
    void testConstructorConAlgunosCamposNull() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            null,
            "12345678Z",
            null,
            "123456789",
            null,
            null
        );

        assertEquals("Juan", dto.firstName());
        assertNull(dto.lastName());
        assertEquals("12345678Z", dto.dni());
        assertNull(dto.email());
        assertEquals("123456789", dto.phoneNumber());
        assertNull(dto.enrolled());
        assertNull(dto.enabled());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testInmutabilidad() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            true,
            true
        );

        // Los records son inmutables por defecto, no se pueden modificar
        // Solo podemos verificar que los valores se mantienen
        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez García", dto.lastName());
        assertEquals("12345678Z", dto.dni());
    }

    @Test
    @DisplayName("Equals debe funcionar correctamente")
    void testEquals() {
        DTOActualizacionAlumno dto1 = new DTOActualizacionAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            true,
            true
        );

        DTOActualizacionAlumno dto2 = new DTOActualizacionAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            true,
            true
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Equals debe funcionar con campos null")
    void testEqualsConCamposNull() {
        DTOActualizacionAlumno dto1 = new DTOActualizacionAlumno(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        DTOActualizacionAlumno dto2 = new DTOActualizacionAlumno(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            true,
            true
        );

        String toString = dto.toString();

        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("Pérez García"));
        assertTrue(toString.contains("juan@ejemplo.com"));
        assertTrue(toString.contains("123456789"));
    }

    @Test
    @DisplayName("ToString debe manejar campos null")
    void testToStringConCamposNull() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            null,
            "Pérez García",
            null,
            "juan@ejemplo.com",
            null,
            null,
            null
        );

        String toString = dto.toString();

        assertTrue(toString.contains("Pérez García"));
        assertTrue(toString.contains("juan@ejemplo.com"));
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes tipos de teléfono")
    void testDiferentesTiposTelefono() {
        // Teléfono con prefijo
        DTOActualizacionAlumno dto1 = new DTOActualizacionAlumno(
            "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "+34 123 456 789", null, null
        );
        assertEquals("+34 123 456 789", dto1.phoneNumber());

        // Teléfono sin prefijo
        DTOActualizacionAlumno dto2 = new DTOActualizacionAlumno(
            "María", "García", "87654321Y", "maria@ejemplo.com", "123456789", null, null
        );
        assertEquals("123456789", dto2.phoneNumber());

        // Sin teléfono
        DTOActualizacionAlumno dto3 = new DTOActualizacionAlumno(
            "Carlos", "López", "11223344X", "carlos@ejemplo.com", null, null, null
        );
        assertNull(dto3.phoneNumber());
    }

    @Test
    @DisplayName("Constructor debe manejar nombres con acentos")
    void testNombresConAcentos() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "José María",
            "García López",
            "12345678Z",
            "jose@ejemplo.com",
            "123456789",
            null,
            null
        );

        assertEquals("José María", dto.firstName());
        assertEquals("García López", dto.lastName());
    }

    @Test
    @DisplayName("Constructor debe manejar DNI con letra")
    void testDNIConLetra() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            "Pérez",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789",
            null,
            null
        );

        assertEquals("12345678Z", dto.dni());
    }

    @Test
    @DisplayName("Constructor debe manejar email con dominio")
    void testEmailConDominio() {
        DTOActualizacionAlumno dto = new DTOActualizacionAlumno(
            "Juan",
            "Pérez",
            "12345678Z",
            "juan.perez@empresa.com",
            "123456789",
            null,
            null
        );

        assertEquals("juan.perez@empresa.com", dto.email());
    }

    @Test
    @DisplayName("Constructor debe manejar actualización parcial")
    void testActualizacionParcial() {
        // Solo actualizar nombre
        DTOActualizacionAlumno dto1 = new DTOActualizacionAlumno(
            "Nuevo Nombre",
            null,
            null,
            null,
            null,
            null,
            null
        );
        assertEquals("Nuevo Nombre", dto1.firstName());
        assertNull(dto1.lastName());
        assertNull(dto1.dni());
        assertNull(dto1.email());
        assertNull(dto1.phoneNumber());
        assertNull(dto1.enrolled());
        assertNull(dto1.enabled());

        // Solo actualizar email
        DTOActualizacionAlumno dto2 = new DTOActualizacionAlumno(
            null,
            null,
            null,
            "nuevo@ejemplo.com",
            null,
            null,
            null
        );
        assertNull(dto2.firstName());
        assertNull(dto2.lastName());
        assertNull(dto2.dni());
        assertEquals("nuevo@ejemplo.com", dto2.email());
        assertNull(dto2.phoneNumber());
        assertNull(dto2.enrolled());
        assertNull(dto2.enabled());
    }
}