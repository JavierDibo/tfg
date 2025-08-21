package app.dtos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOParametrosBusquedaProfesor")
class DTOParametrosBusquedaProfesorTest {

    @Test
    @DisplayName("Constructor por defecto debe crear DTO con todos los parámetros nulos")
    void testConstructorPorDefecto() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor();
        
        assertNotNull(dto);
        assertNull(dto.nombre());
        assertNull(dto.apellidos());
        assertNull(dto.email());
        assertNull(dto.usuario());
        assertNull(dto.dni());
        assertNull(dto.habilitado());
        assertNull(dto.claseId());
        assertNull(dto.sinClases());
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear DTO correctamente")
    void testConstructorConParametros() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        assertNotNull(dto);
        assertEquals("María", dto.nombre());
        assertEquals("García", dto.apellidos());
        assertEquals("maria@test.com", dto.email());
        assertEquals("profesor123", dto.usuario());
        assertEquals("12345678A", dto.dni());
        assertTrue(dto.habilitado());
        assertEquals("clase1", dto.claseId());
        assertFalse(dto.sinClases());
    }

    @Test
    @DisplayName("Constructor con algunos parámetros nulos debe crear DTO correctamente")
    void testConstructorConParametrosNulos() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", null, "maria@test.com", null, "12345678A", null, "clase1", null);
        
        assertNotNull(dto);
        assertEquals("María", dto.nombre());
        assertNull(dto.apellidos());
        assertEquals("maria@test.com", dto.email());
        assertNull(dto.usuario());
        assertEquals("12345678A", dto.dni());
        assertNull(dto.habilitado());
        assertEquals("clase1", dto.claseId());
        assertNull(dto.sinClases());
    }

    @Test
    @DisplayName("Método estaVacio debe retornar true cuando todos los parámetros son nulos")
    void testEstaVacioConTodosNulos() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor();
        
        assertTrue(dto.estaVacio());
    }

    @Test
    @DisplayName("Método estaVacio debe retornar false cuando hay al menos un parámetro no nulo")
    void testEstaVacioConParametros() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", null, null, null, null, null, null, null);
        
        assertFalse(dto.estaVacio());
    }

    @Test
    @DisplayName("Método estaVacio debe retornar false cuando hay múltiples parámetros")
    void testEstaVacioConMultiplesParametros() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        assertFalse(dto.estaVacio());
    }

    @Test
    @DisplayName("Método tieneCriteriosTexto debe retornar true cuando hay criterios de texto")
    void testTieneCriteriosTextoConCriterios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", null, null, null, null, null, null, null);
        
        assertTrue(dto.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Método tieneCriteriosTexto debe retornar false cuando no hay criterios de texto")
    void testTieneCriteriosTextoSinCriterios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                null, null, null, null, null, true, "clase1", false);
        
        assertFalse(dto.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Método tieneCriteriosTexto debe retornar true cuando hay múltiples criterios de texto")
    void testTieneCriteriosTextoConMultiplesCriterios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        assertTrue(dto.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Método tieneCriteriosTexto debe retornar false cuando todos los criterios de texto son nulos")
    void testTieneCriteriosTextoConTodosNulos() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                null, null, null, null, null, true, "clase1", false);
        
        assertFalse(dto.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Criterios de texto individuales deben ser detectados correctamente")
    void testCriteriosTextoIndividuales() {
        // Test con nombre
        DTOParametrosBusquedaProfesor dtoNombre = new DTOParametrosBusquedaProfesor(
                "María", null, null, null, null, null, null, null);
        assertTrue(dtoNombre.tieneCriteriosTexto());
        
        // Test con apellidos
        DTOParametrosBusquedaProfesor dtoApellidos = new DTOParametrosBusquedaProfesor(
                null, "García", null, null, null, null, null, null);
        assertTrue(dtoApellidos.tieneCriteriosTexto());
        
        // Test con email
        DTOParametrosBusquedaProfesor dtoEmail = new DTOParametrosBusquedaProfesor(
                null, null, "maria@test.com", null, null, null, null, null);
        assertTrue(dtoEmail.tieneCriteriosTexto());
        
        // Test con usuario
        DTOParametrosBusquedaProfesor dtoUsuario = new DTOParametrosBusquedaProfesor(
                null, null, null, "profesor123", null, null, null, null);
        assertTrue(dtoUsuario.tieneCriteriosTexto());
        
        // Test con DNI
        DTOParametrosBusquedaProfesor dtoDni = new DTOParametrosBusquedaProfesor(
                null, null, null, null, "12345678A", null, null, null);
        assertTrue(dtoDni.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Criterios no textuales no deben afectar tieneCriteriosTexto")
    void testCriteriosNoTextuales() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                null, null, null, null, null, true, "clase1", false);
        
        assertFalse(dto.tieneCriteriosTexto());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testRecordInmutable() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        assertNotNull(dto);
        assertEquals("María", dto.nombre());
        assertEquals("García", dto.apellidos());
        assertEquals("maria@test.com", dto.email());
        assertEquals("profesor123", dto.usuario());
        assertEquals("12345678A", dto.dni());
        assertTrue(dto.habilitado());
        assertEquals("clase1", dto.claseId());
        assertFalse(dto.sinClases());
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        DTOParametrosBusquedaProfesor dto1 = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        DTOParametrosBusquedaProfesor dto2 = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Crear con diferentes valores
        DTOParametrosBusquedaProfesor dto3 = new DTOParametrosBusquedaProfesor(
                "Juan", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "María", "García", "maria@test.com", "profesor123", "12345678A", true, "clase1", false);
        
        String toString = dto.toString();
        
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 10);
        assertNotNull(toString);
        // Verificar que contiene información del record
        assertTrue(toString.contains("DTOParametrosBusquedaProfesor"));
    }

    @Test
    @DisplayName("Constructor con valores booleanos debe crear DTO correctamente")
    void testConstructorConValoresBooleanos() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                null, null, null, null, null, false, null, true);
        
        assertNotNull(dto);
        assertFalse(dto.habilitado());
        assertTrue(dto.sinClases());
    }

    @Test
    @DisplayName("Constructor con strings vacíos debe crear DTO correctamente")
    void testConstructorConStringsVacios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "", "", "", "", "", null, "", null);
        
        assertNotNull(dto);
        assertEquals("", dto.nombre());
        assertEquals("", dto.apellidos());
        assertEquals("", dto.email());
        assertEquals("", dto.usuario());
        assertEquals("", dto.dni());
        assertEquals("", dto.claseId());
    }

    @Test
    @DisplayName("Método estaVacio con strings vacíos debe retornar false")
    void testEstaVacioConStringsVacios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "", "", "", "", "", null, "", null);
        
        assertFalse(dto.estaVacio());
    }

    @Test
    @DisplayName("Método tieneCriteriosTexto con strings vacíos debe retornar true")
    void testTieneCriteriosTextoConStringsVacios() {
        DTOParametrosBusquedaProfesor dto = new DTOParametrosBusquedaProfesor(
                "", "", "", "", "", null, "", null);
        
        assertTrue(dto.tieneCriteriosTexto());
    }
}
