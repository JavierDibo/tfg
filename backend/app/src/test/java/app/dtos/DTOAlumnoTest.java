package app.dtos;

import app.entidades.Alumno;
import app.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOAlumno")
class DTOAlumnoTest {

    private Alumno alumno;
    private final Long ID = 1L;
    private final String USUARIO = "alumno123";
    private final String PASSWORD = "password123";
    private final String NOMBRE = "Juan";
    private final String APELLIDOS = "Pérez García";
    private final String DNI = "12345678Z";
    private final String EMAIL = "juan.perez@ejemplo.com";
    private final String TELEFONO = "123456789";
    private final LocalDateTime FECHA_INSCRIPCION = LocalDateTime.now();
    private final boolean MATRICULADO = true;
    private final boolean ENABLED = true;

    @BeforeEach
    void setUp() {
        alumno = new Alumno(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        alumno.setId(ID);
        alumno.setFechaInscripcion(FECHA_INSCRIPCION);
        alumno.setMatriculado(MATRICULADO);
        alumno.setEnabled(ENABLED);
    }

    @Test
    @DisplayName("Constructor desde entidad Alumno debe mapear correctamente")
    void testConstructorDesdeEntidad() {
        DTOAlumno dto = new DTOAlumno(alumno);

        assertEquals(ID, dto.id());
        assertEquals(USUARIO, dto.usuario());
        assertEquals(NOMBRE, dto.nombre());
        assertEquals(APELLIDOS, dto.apellidos());
        assertEquals(DNI, dto.dni());
        assertEquals(EMAIL, dto.email());
        assertEquals(TELEFONO, dto.numeroTelefono());
        assertEquals(FECHA_INSCRIPCION, dto.fechaInscripcion());
        assertEquals(MATRICULADO, dto.matriculado());
        assertEquals(ENABLED, dto.enabled());
    }

    @Test
    @DisplayName("Constructor debe manejar valores null correctamente")
    void testConstructorConValoresNull() {
        alumno.setNumeroTelefono(null);
        DTOAlumno dto = new DTOAlumno(alumno);

        assertNull(dto.numeroTelefono());
        assertNotNull(dto.id());
        assertNotNull(dto.usuario());
        assertNotNull(dto.nombre());
        assertNotNull(dto.apellidos());
        assertNotNull(dto.dni());
        assertNotNull(dto.email());
        assertNotNull(dto.fechaInscripcion());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testInmutabilidad() {
        DTOAlumno dto = new DTOAlumno(alumno);
        
        // Los records son inmutables por defecto, no se pueden modificar
        // Solo podemos verificar que los valores se mantienen
        assertEquals(ID, dto.id());
        assertEquals(USUARIO, dto.usuario());
        assertEquals(NOMBRE, dto.nombre());
    }

    @Test
    @DisplayName("Equals debe funcionar correctamente")
    void testEquals() {
        DTOAlumno dto1 = new DTOAlumno(alumno);
        
        // Crear otro alumno con los mismos datos
        Alumno alumno2 = new Alumno(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        alumno2.setId(ID);
        alumno2.setFechaInscripcion(FECHA_INSCRIPCION);
        alumno2.setMatriculado(MATRICULADO);
        alumno2.setEnabled(ENABLED);
        DTOAlumno dto2 = new DTOAlumno(alumno2);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOAlumno dto = new DTOAlumno(alumno);
        String toString = dto.toString();

        assertTrue(toString.contains(ID.toString()));
        assertTrue(toString.contains(USUARIO));
        assertTrue(toString.contains(NOMBRE));
        assertTrue(toString.contains(APELLIDOS));
        assertTrue(toString.contains(EMAIL));
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes estados de matrícula")
    void testDiferentesEstadosMatricula() {
        // Alumno matriculado
        alumno.setMatriculado(true);
        DTOAlumno dtoMatriculado = new DTOAlumno(alumno);
        assertTrue(dtoMatriculado.matriculado());

        // Alumno no matriculado
        alumno.setMatriculado(false);
        DTOAlumno dtoNoMatriculado = new DTOAlumno(alumno);
        assertFalse(dtoNoMatriculado.matriculado());
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes estados de habilitación")
    void testDiferentesEstadosHabilitacion() {
        // Alumno habilitado
        alumno.setEnabled(true);
        DTOAlumno dtoHabilitado = new DTOAlumno(alumno);
        assertTrue(dtoHabilitado.enabled());

        // Alumno deshabilitado
        alumno.setEnabled(false);
        DTOAlumno dtoDeshabilitado = new DTOAlumno(alumno);
        assertFalse(dtoDeshabilitado.enabled());
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes fechas de inscripción")
    void testDiferentesFechasInscripcion() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(30);
        alumno.setFechaInscripcion(fechaPasada);
        DTOAlumno dto = new DTOAlumno(alumno);
        assertEquals(fechaPasada, dto.fechaInscripcion());

        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(30);
        alumno.setFechaInscripcion(fechaFutura);
        DTOAlumno dto2 = new DTOAlumno(alumno);
        assertEquals(fechaFutura, dto2.fechaInscripcion());
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes tipos de teléfono")
    void testDiferentesTiposTelefono() {
        // Teléfono con prefijo
        alumno.setNumeroTelefono("+34 123 456 789");
        DTOAlumno dto = new DTOAlumno(alumno);
        assertEquals("+34 123 456 789", dto.numeroTelefono());

        // Teléfono sin prefijo
        alumno.setNumeroTelefono("123456789");
        DTOAlumno dto2 = new DTOAlumno(alumno);
        assertEquals("123456789", dto2.numeroTelefono());

        // Sin teléfono
        alumno.setNumeroTelefono(null);
        DTOAlumno dto3 = new DTOAlumno(alumno);
        assertNull(dto3.numeroTelefono());
    }
}
