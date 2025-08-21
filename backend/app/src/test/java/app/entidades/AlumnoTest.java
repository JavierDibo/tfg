package app.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la entidad Alumno")
class AlumnoTest {

    private Alumno alumno;
    private final String USUARIO = "alumno123";
    private final String PASSWORD = "password123";
    private final String NOMBRE = "Juan";
    private final String APELLIDOS = "Pérez García";
    private final String DNI = "12345678Z";
    private final String EMAIL = "juan.perez@ejemplo.com";
    private final String TELEFONO = "123456789";

    @BeforeEach
    void setUp() {
        alumno = new Alumno(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear alumno correctamente")
    void testConstructorConParametros() {
        // Verificar que se crea correctamente
        assertNotNull(alumno);
        assertEquals(USUARIO, alumno.getUsuario());
        assertEquals(PASSWORD, alumno.getPassword());
        assertEquals(NOMBRE, alumno.getNombre());
        assertEquals(APELLIDOS, alumno.getApellidos());
        assertEquals(DNI, alumno.getDni());
        assertEquals(EMAIL, alumno.getEmail());
        assertEquals(TELEFONO, alumno.getNumeroTelefono());
        assertEquals(Usuario.Rol.ALUMNO, alumno.getRol());
        assertTrue(alumno.isEnabled());
        assertFalse(alumno.isMatriculado());
        assertNotNull(alumno.getFechaInscripcion());
    }

    @Test
    @DisplayName("Constructor vacío debe crear alumno con valores por defecto")
    void testConstructorVacio() {
        Alumno alumnoVacio = new Alumno();
        
        assertNotNull(alumnoVacio);
        assertEquals(Usuario.Rol.ALUMNO, alumnoVacio.getRol());
        assertTrue(alumnoVacio.isEnabled());
        assertFalse(alumnoVacio.isMatriculado());
        assertNotNull(alumnoVacio.getFechaInscripcion());
    }

    @Test
    @DisplayName("Getters y setters deben funcionar correctamente")
    void testGettersYSetters() {
        // Test fechaInscripcion
        LocalDateTime nuevaFecha = LocalDateTime.now().minusDays(1);
        alumno.setFechaInscripcion(nuevaFecha);
        assertEquals(nuevaFecha, alumno.getFechaInscripcion());

        // Test matriculado
        alumno.setMatriculado(true);
        assertTrue(alumno.isMatriculado());
        
        alumno.setMatriculado(false);
        assertFalse(alumno.isMatriculado());

        // Test enabled (heredado de Usuario)
        alumno.setEnabled(false);
        assertFalse(alumno.isEnabled());
        
        alumno.setEnabled(true);
        assertTrue(alumno.isEnabled());
    }

    @Test
    @DisplayName("Método resetearpassword debe lanzar UnsupportedOperationException")
    void testResetearPassword() {
        assertThrows(UnsupportedOperationException.class, () -> {
            alumno.resetearpassword();
        });
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        Alumno alumno1 = new Alumno(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        Alumno alumno2 = new Alumno(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        
        // Los objetos deben ser iguales si tienen los mismos valores
        assertEquals(alumno1, alumno2);
        assertEquals(alumno1.hashCode(), alumno2.hashCode());
        
        // Modificar un campo debe hacer que no sean iguales
        alumno2.setMatriculado(true);
        assertNotEquals(alumno1, alumno2);
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        String toString = alumno.toString();
        
        // Verificar que toString no está vacío y tiene contenido
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 10);
        assertNotNull(toString);
    }

    @Test
    @DisplayName("Rol debe ser ALUMNO por defecto")
    void testRolPorDefecto() {
        assertEquals(Usuario.Rol.ALUMNO, alumno.getRol());
        
        // Verificar que se puede cambiar el rol (Lombok @Data permite esto)
        alumno.setRol(Usuario.Rol.PROFESOR);
        assertEquals(Usuario.Rol.PROFESOR, alumno.getRol());
    }

    @Test
    @DisplayName("Fecha de inscripción debe ser la fecha actual al crear")
    void testFechaInscripcionPorDefecto() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        Alumno nuevoAlumno = new Alumno();
        LocalDateTime despues = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(nuevoAlumno.getFechaInscripcion().isAfter(antes) || 
                  nuevoAlumno.getFechaInscripcion().isEqual(antes));
        assertTrue(nuevoAlumno.getFechaInscripcion().isBefore(despues) || 
                  nuevoAlumno.getFechaInscripcion().isEqual(despues));
    }

    @Test
    @DisplayName("Matriculado debe ser false por defecto")
    void testMatriculadoPorDefecto() {
        Alumno nuevoAlumno = new Alumno();
        assertFalse(nuevoAlumno.isMatriculado());
    }

    @Test
    @DisplayName("Enabled debe ser true por defecto")
    void testEnabledPorDefecto() {
        Alumno nuevoAlumno = new Alumno();
        assertTrue(nuevoAlumno.isEnabled());
    }
}
