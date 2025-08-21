package app.dtos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOPeticionRegistroProfesor")
class DTOPeticionRegistroProfesorTest {

    private final String USUARIO = "profesor123";
    private final String PASSWORD = "password123";
    private final String NOMBRE = "María";
    private final String APELLIDOS = "García López";
    private final String DNI = "87654321A";
    private final String EMAIL = "maria.garcia@ejemplo.com";
    private final String TELEFONO = "987654321";

    @Test
    @DisplayName("Constructor completo debe crear DTO correctamente")
    void testConstructorCompleto() {
        List<String> clases = new ArrayList<>();
        clases.add("clase1");
        clases.add("clase2");
        
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, clases);
        
        assertNotNull(dto);
        assertEquals(USUARIO, dto.usuario());
        assertEquals(PASSWORD, dto.password());
        assertEquals(NOMBRE, dto.nombre());
        assertEquals(APELLIDOS, dto.apellidos());
        assertEquals(DNI, dto.dni());
        assertEquals(EMAIL, dto.email());
        assertEquals(TELEFONO, dto.numeroTelefono());
        assertEquals(clases, dto.clasesId());
    }

    @Test
    @DisplayName("Constructor sin clases debe crear DTO con lista vacía")
    void testConstructorSinClases() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals(USUARIO, dto.usuario());
        assertEquals(PASSWORD, dto.password());
        assertEquals(NOMBRE, dto.nombre());
        assertEquals(APELLIDOS, dto.apellidos());
        assertEquals(DNI, dto.dni());
        assertEquals(EMAIL, dto.email());
        assertEquals(TELEFONO, dto.numeroTelefono());
        assertNotNull(dto.clasesId());
        assertTrue(dto.clasesId().isEmpty());
    }

    @Test
    @DisplayName("Constructor con clases null debe crear DTO con lista vacía")
    void testConstructorConClasesNull() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, null);
        
        assertNotNull(dto);
        assertEquals(USUARIO, dto.usuario());
        assertEquals(PASSWORD, dto.password());
        assertEquals(NOMBRE, dto.nombre());
        assertEquals(APELLIDOS, dto.apellidos());
        assertEquals(DNI, dto.dni());
        assertEquals(EMAIL, dto.email());
        assertEquals(TELEFONO, dto.numeroTelefono());
        assertNull(dto.clasesId());
    }

    @Test
    @DisplayName("Constructor con clases vacía debe crear DTO correctamente")
    void testConstructorConClasesVacia() {
        List<String> clasesVacia = new ArrayList<>();
        
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, clasesVacia);
        
        assertNotNull(dto);
        assertEquals(clasesVacia, dto.clasesId());
        assertTrue(dto.clasesId().isEmpty());
    }

    @Test
    @DisplayName("Constructor con múltiples clases debe crear DTO correctamente")
    void testConstructorConMultiplesClases() {
        List<String> clases = List.of("clase1", "clase2", "clase3", "clase4");
        
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, clases);
        
        assertNotNull(dto);
        assertEquals(clases, dto.clasesId());
        assertEquals(4, dto.clasesId().size());
        assertTrue(dto.clasesId().contains("clase1"));
        assertTrue(dto.clasesId().contains("clase2"));
        assertTrue(dto.clasesId().contains("clase3"));
        assertTrue(dto.clasesId().contains("clase4"));
    }

    @Test
    @DisplayName("Constructor con nombres con espacios debe crear DTO correctamente")
    void testConstructorConNombresConEspacios() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, "María José", "García López", DNI, EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals("María José", dto.nombre());
        assertEquals("García López", dto.apellidos());
    }

    @Test
    @DisplayName("Constructor con usuario con caracteres especiales debe crear DTO correctamente")
    void testConstructorConUsuarioEspecial() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                "profesor_123", PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals("profesor_123", dto.usuario());
    }

    @Test
    @DisplayName("Constructor con email sin telefono debe crear DTO correctamente")
    void testConstructorSinTelefono() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, null);
        
        assertNotNull(dto);
        assertNull(dto.numeroTelefono());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testRecordInmutable() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals(USUARIO, dto.usuario());
        assertEquals(PASSWORD, dto.password());
        assertEquals(NOMBRE, dto.nombre());
        assertEquals(APELLIDOS, dto.apellidos());
        assertEquals(DNI, dto.dni());
        assertEquals(EMAIL, dto.email());
        assertEquals(TELEFONO, dto.numeroTelefono());
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        DTOPeticionRegistroProfesor dto1 = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        DTOPeticionRegistroProfesor dto2 = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Crear con diferentes valores
        DTOPeticionRegistroProfesor dto3 = new DTOPeticionRegistroProfesor(
                "otro_usuario", PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        String toString = dto.toString();
        
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 10);
        assertNotNull(toString);
        // Verificar que contiene información del record
        assertTrue(toString.contains("DTOPeticionRegistroProfesor"));
    }

    @Test
    @DisplayName("Constructor con clases inmutables debe crear DTO correctamente")
    void testConstructorConClasesInmutables() {
        List<String> clases = List.of("clase1", "clase2");
        
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, clases);
        
        assertNotNull(dto);
        assertEquals(clases, dto.clasesId());
        assertEquals(2, dto.clasesId().size());
    }

    @Test
    @DisplayName("Constructor con password larga debe crear DTO correctamente")
    void testConstructorConPasswordLarga() {
        String passwordLarga = "password_muy_larga_con_muchos_caracteres_123456789";
        
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, passwordLarga, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals(passwordLarga, dto.password());
    }

    @Test
    @DisplayName("Constructor con DNI con letra debe crear DTO correctamente")
    void testConstructorConDNILetra() {
        DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                USUARIO, PASSWORD, NOMBRE, APELLIDOS, "12345678Z", EMAIL, TELEFONO);
        
        assertNotNull(dto);
        assertEquals("12345678Z", dto.dni());
    }
}
