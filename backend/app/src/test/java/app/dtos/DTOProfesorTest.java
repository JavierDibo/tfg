package app.dtos;

import app.entidades.Profesor;
import app.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOProfesor")
class DTOProfesorTest {

    private Profesor profesor;
    private DTOProfesor dtoProfesor;
    private final Long ID = 1L;
    private final String USUARIO = "profesor123";
    private final String NOMBRE = "María";
    private final String APELLIDOS = "García López";
    private final String DNI = "87654321A";
    private final String EMAIL = "maria.garcia@ejemplo.com";
    private final String TELEFONO = "987654321";
    private final Usuario.Role Role = Usuario.Role.PROFESOR;
    private final boolean ENABLED = true;
    private final LocalDateTime FECHA_CREACION = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        profesor = new Profesor(USUARIO, "password123", NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        profesor.setId(ID);
        profesor.setEnabled(ENABLED);
        
        dtoProfesor = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear DTO correctamente")
    void testConstructorConParametros() {
        assertNotNull(dtoProfesor);
        assertEquals(ID, dtoProfesor.id());
        assertEquals(USUARIO, dtoProfesor.username());
        assertEquals(NOMBRE, dtoProfesor.firstName());
        assertEquals(APELLIDOS, dtoProfesor.lastName());
        assertEquals(DNI, dtoProfesor.dni());
        assertEquals(EMAIL, dtoProfesor.email());
        assertEquals(TELEFONO, dtoProfesor.phoneNumber());
        assertEquals(Role, dtoProfesor.role());
        assertEquals(ENABLED, dtoProfesor.enabled());
        assertNotNull(dtoProfesor.classIds());
        assertEquals(FECHA_CREACION, dtoProfesor.createdAt());
    }

    @Test
    @DisplayName("Constructor desde entidad Profesor debe crear DTO correctamente")
    void testConstructorDesdeEntidad() {
        // Agregar algunas clases al profesor
        profesor.agregarClase("clase1");
        profesor.agregarClase("clase2");
        
        DTOProfesor dtoDesdeEntidad = new DTOProfesor(profesor);
        
        assertNotNull(dtoDesdeEntidad);
        assertEquals(profesor.getId(), dtoDesdeEntidad.id());
        assertEquals(profesor.getUsername(), dtoDesdeEntidad.username());
        assertEquals(profesor.getFirstName(), dtoDesdeEntidad.firstName());
        assertEquals(profesor.getLastName(), dtoDesdeEntidad.lastName());
        assertEquals(profesor.getDni(), dtoDesdeEntidad.dni());
        assertEquals(profesor.getEmail(), dtoDesdeEntidad.email());
        assertEquals(profesor.getPhoneNumber(), dtoDesdeEntidad.phoneNumber());
        assertEquals(profesor.getRole(), dtoDesdeEntidad.role());
        assertEquals(profesor.isEnabled(), dtoDesdeEntidad.enabled());
        assertEquals(profesor.getClasesId(), dtoDesdeEntidad.classIds());
        assertNotNull(dtoDesdeEntidad.createdAt());
    }

    @Test
    @DisplayName("Método from debe crear DTO desde entidad")
    void testMetodoFrom() {
        DTOProfesor dtoFrom = DTOProfesor.from(profesor);
        
        assertNotNull(dtoFrom);
        assertEquals(profesor.getId(), dtoFrom.id());
        assertEquals(profesor.getUsername(), dtoFrom.username());
        assertEquals(profesor.getFirstName(), dtoFrom.firstName());
        assertEquals(profesor.getLastName(), dtoFrom.lastName());
        assertEquals(profesor.getDni(), dtoFrom.dni());
        assertEquals(profesor.getEmail(), dtoFrom.email());
        assertEquals(profesor.getPhoneNumber(), dtoFrom.phoneNumber());
        assertEquals(profesor.getRole(), dtoFrom.role());
        assertEquals(profesor.isEnabled(), dtoFrom.enabled());
        assertEquals(profesor.getClasesId(), dtoFrom.classIds());
    }

    @Test
    @DisplayName("Método getNombreCompleto debe retornar nombre completo")
    void testGetNombreCompleto() {
        String nombreCompleto = dtoProfesor.getFullName();
        assertEquals(NOMBRE + " " + APELLIDOS, nombreCompleto);
    }

    @Test
    @DisplayName("Método getNombreCompleto con nombres con espacios")
    void testGetNombreCompletoConEspacios() {
        DTOProfesor dtoConEspacios = new DTOProfesor(ID, USUARIO, "María José", "García López", DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        String nombreCompleto = dtoConEspacios.getFullName();
        assertEquals("María José García López", nombreCompleto);
    }

    @Test
    @DisplayName("Método tieneClases debe retornar false cuando no hay clases")
    void testTieneClasesSinClases() {
        assertFalse(dtoProfesor.hasClasses());
    }

    @Test
    @DisplayName("Método tieneClases debe retornar true cuando hay clases")
    void testTieneClasesConClases() {
        List<String> clases = new ArrayList<>();
        clases.add("clase1");
        clases.add("clase2");
        
        DTOProfesor dtoConClases = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, clases, FECHA_CREACION);
        assertTrue(dtoConClases.hasClasses());
    }

    @Test
    @DisplayName("Método tieneClases debe retornar false cuando clasesId es null")
    void testTieneClasesConClasesIdNull() {
        DTOProfesor dtoConClasesNull = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, null, FECHA_CREACION);
        assertFalse(dtoConClasesNull.hasClasses());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar 0 cuando no hay clases")
    void testGetNumeroClasesSinClases() {
        assertEquals(0, dtoProfesor.getClassCount());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar número correcto de clases")
    void testGetNumeroClasesConClases() {
        List<String> clases = new ArrayList<>();
        clases.add("clase1");
        clases.add("clase2");
        clases.add("clase3");
        
        DTOProfesor dtoConClases = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, clases, FECHA_CREACION);
        assertEquals(3, dtoConClases.getClassCount());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar 0 cuando clasesId es null")
    void testGetNumeroClasesConClasesIdNull() {
        DTOProfesor dtoConClasesNull = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, null, FECHA_CREACION);
        assertEquals(0, dtoConClasesNull.getClassCount());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testRecordInmutable() {
        // Los records son inmutables por defecto, no se pueden modificar después de la creación
        assertNotNull(dtoProfesor);
        assertEquals(ID, dtoProfesor.id());
        assertEquals(USUARIO, dtoProfesor.username());
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        DTOProfesor dto1 = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        DTOProfesor dto2 = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Crear con diferentes valores
        DTOProfesor dto3 = new DTOProfesor(2L, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        String toString = dtoProfesor.toString();
        
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 10);
        assertNotNull(toString);
        // Verificar que contiene información del record
        assertTrue(toString.contains("DTOProfesor"));
    }

    @Test
    @DisplayName("Constructor desde entidad con clases")
    void testConstructorDesdeEntidadConClases() {
        // Agregar clases al profesor
        profesor.agregarClase("clase1");
        profesor.agregarClase("clase2");
        
        DTOProfesor dto = new DTOProfesor(profesor);
        
        assertTrue(dto.hasClasses());
        assertEquals(2, dto.getClassCount());
        assertTrue(dto.classIds().contains("clase1"));
        assertTrue(dto.classIds().contains("clase2"));
    }

    @Test
    @DisplayName("Constructor desde entidad con profesor deshabilitado")
    void testConstructorDesdeEntidadDeshabilitado() {
        profesor.setEnabled(false);
        
        DTOProfesor dto = new DTOProfesor(profesor);
        
        assertFalse(dto.enabled());
    }
}
