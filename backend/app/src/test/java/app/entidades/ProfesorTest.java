package app.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la entidad Profesor")
class ProfesorTest {

    private Profesor profesor;
    private final String USUARIO = "profesor123";
    private final String PASSWORD = "password123";
    private final String NOMBRE = "María";
    private final String APELLIDOS = "García López";
    private final String DNI = "87654321A";
    private final String EMAIL = "maria.garcia@ejemplo.com";
    private final String TELEFONO = "987654321";

    @BeforeEach
    void setUp() {
        profesor = new Profesor(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear profesor correctamente")
    void testConstructorConParametros() {
        // Verificar que se crea correctamente
        assertNotNull(profesor);
        assertEquals(USUARIO, profesor.getUsuario());
        assertEquals(PASSWORD, profesor.getPassword());
        assertEquals(NOMBRE, profesor.getNombre());
        assertEquals(APELLIDOS, profesor.getApellidos());
        assertEquals(DNI, profesor.getDni());
        assertEquals(EMAIL, profesor.getEmail());
        assertEquals(TELEFONO, profesor.getNumeroTelefono());
        assertEquals(Usuario.Rol.PROFESOR, profesor.getRol());
        assertTrue(profesor.isEnabled());
        assertNotNull(profesor.getClasesId());
        assertTrue(profesor.getClasesId().isEmpty());
    }

    @Test
    @DisplayName("Constructor vacío debe crear profesor con valores por defecto")
    void testConstructorVacio() {
        Profesor profesorVacio = new Profesor();
        
        assertNotNull(profesorVacio);
        assertEquals(Usuario.Rol.PROFESOR, profesorVacio.getRol());
        assertTrue(profesorVacio.isEnabled());
        assertNotNull(profesorVacio.getClasesId());
        assertTrue(profesorVacio.getClasesId().isEmpty());
    }

    @Test
    @DisplayName("Getters y setters deben funcionar correctamente")
    void testGettersYSetters() {
        // Test clasesId
        List<String> nuevasClases = new ArrayList<>();
        nuevasClases.add("clase1");
        nuevasClases.add("clase2");
        profesor.setClasesId(nuevasClases);
        assertEquals(nuevasClases, profesor.getClasesId());

        // Test enabled (heredado de Usuario)
        profesor.setEnabled(false);
        assertFalse(profesor.isEnabled());
        
        profesor.setEnabled(true);
        assertTrue(profesor.isEnabled());
    }

    @Test
    @DisplayName("Método resetearpassword debe lanzar UnsupportedOperationException")
    void testResetearPassword() {
        assertThrows(UnsupportedOperationException.class, () -> {
            profesor.resetearpassword();
        });
    }

    @Test
    @DisplayName("Método agregarClase debe agregar clase correctamente")
    void testAgregarClase() {
        String claseId = "clase123";
        
        // Verificar que la lista está vacía inicialmente
        assertTrue(profesor.getClasesId().isEmpty());
        
        // Agregar clase
        profesor.agregarClase(claseId);
        
        // Verificar que se agregó
        assertTrue(profesor.getClasesId().contains(claseId));
        assertEquals(1, profesor.getClasesId().size());
        
        // Agregar la misma clase no debe duplicar
        profesor.agregarClase(claseId);
        assertEquals(1, profesor.getClasesId().size());
        
        // Agregar otra clase
        profesor.agregarClase("clase456");
        assertEquals(2, profesor.getClasesId().size());
        assertTrue(profesor.getClasesId().contains("clase123"));
        assertTrue(profesor.getClasesId().contains("clase456"));
    }

    @Test
    @DisplayName("Método removerClase debe remover clase correctamente")
    void testRemoverClase() {
        String claseId = "clase123";
        
        // Agregar clase
        profesor.agregarClase(claseId);
        assertTrue(profesor.getClasesId().contains(claseId));
        
        // Remover clase
        profesor.removerClase(claseId);
        assertFalse(profesor.getClasesId().contains(claseId));
        assertTrue(profesor.getClasesId().isEmpty());
        
        // Remover clase que no existe no debe causar error
        assertDoesNotThrow(() -> profesor.removerClase("claseInexistente"));
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        Profesor profesor1 = new Profesor(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        Profesor profesor2 = new Profesor(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        // Los objetos deben ser iguales si tienen los mismos valores
        assertEquals(profesor1, profesor2);
        assertEquals(profesor1.hashCode(), profesor2.hashCode());
        
        // Modificar un campo debe hacer que no sean iguales
        profesor2.agregarClase("clase123");
        assertNotEquals(profesor1, profesor2);
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        String toString = profesor.toString();
        
        // Verificar que toString no está vacío y tiene contenido
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 10);
        assertNotNull(toString);
    }

    @Test
    @DisplayName("Rol debe ser PROFESOR por defecto")
    void testRolPorDefecto() {
        assertEquals(Usuario.Rol.PROFESOR, profesor.getRol());
        
        // Verificar que se puede cambiar el rol (Lombok @Data permite esto)
        profesor.setRol(Usuario.Rol.ALUMNO);
        assertEquals(Usuario.Rol.ALUMNO, profesor.getRol());
    }

    @Test
    @DisplayName("ClasesId debe ser lista vacía por defecto")
    void testClasesIdPorDefecto() {
        Profesor nuevoProfesor = new Profesor();
        assertNotNull(nuevoProfesor.getClasesId());
        assertTrue(nuevoProfesor.getClasesId().isEmpty());
    }

    @Test
    @DisplayName("Enabled debe ser true por defecto")
    void testEnabledPorDefecto() {
        Profesor nuevoProfesor = new Profesor();
        assertTrue(nuevoProfesor.isEnabled());
    }

    @Test
    @DisplayName("Manejo de clasesId con valores nulos")
    void testClasesIdConValoresNulos() {
        // Establecer clasesId como null
        profesor.setClasesId(null);
        // El getter debe inicializar la lista si es null
        assertNotNull(profesor.getClasesId());
        assertTrue(profesor.getClasesId().isEmpty());
        
        // Agregar clase cuando clasesId era null debe funcionar
        profesor.agregarClase("clase123");
        assertNotNull(profesor.getClasesId());
        assertTrue(profesor.getClasesId().contains("clase123"));
    }

    @Test
    @DisplayName("Múltiples operaciones con clases")
    void testMultiplesOperacionesConClases() {
        // Agregar múltiples clases
        profesor.agregarClase("clase1");
        profesor.agregarClase("clase2");
        profesor.agregarClase("clase3");
        
        assertEquals(3, profesor.getClasesId().size());
        assertTrue(profesor.getClasesId().contains("clase1"));
        assertTrue(profesor.getClasesId().contains("clase2"));
        assertTrue(profesor.getClasesId().contains("clase3"));
        
        // Remover clase del medio
        profesor.removerClase("clase2");
        assertEquals(2, profesor.getClasesId().size());
        assertTrue(profesor.getClasesId().contains("clase1"));
        assertFalse(profesor.getClasesId().contains("clase2"));
        assertTrue(profesor.getClasesId().contains("clase3"));
        
        // Remover todas las clases
        profesor.removerClase("clase1");
        profesor.removerClase("clase3");
        assertTrue(profesor.getClasesId().isEmpty());
    }
}
