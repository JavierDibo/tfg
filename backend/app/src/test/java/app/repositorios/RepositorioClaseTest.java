package app.repositorios;

import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Taller;
import app.entidades.Alumno;
import app.entidades.Profesor;
import app.entidades.Ejercicio;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests para RepositorioClase")
class RepositorioClaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepositorioClase repositorioClase;

    @Autowired
    private RepositorioAlumno repositorioAlumno;

    @Autowired
    private RepositorioProfesor repositorioProfesor;

    @Autowired
    private RepositorioEjercicio repositorioEjercicio;

    private Curso curso;
    private Taller taller;
    private Material material;
    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;
    private Profesor profesor1;
    private Profesor profesor2;
    private Ejercicio ejercicio1;
    private Ejercicio ejercicio2;
    
    // Utility method to generate valid DNIs using the same algorithm as the system
    private String generateValidDNI(int index) {
        StringBuilder dni = new StringBuilder();
        String indexStr = String.valueOf(index);
        
        // Fill with random digits to make it 8 digits  
        while (dni.length() + indexStr.length() < 8) {
            dni.append((int)(Math.random() * 10));
        }
        dni.append(indexStr);
        
        // Ensure it's exactly 8 digits
        if (dni.length() > 8) {
            dni.setLength(8);
        }
        
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        int numero = Integer.parseInt(dni.toString());
        dni.append(letras[numero % 23]);
        return dni.toString();
    }

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        repositorioClase.deleteAll();
        repositorioAlumno.deleteAll();
        repositorioProfesor.deleteAll();
        repositorioEjercicio.deleteAll();

        // Crear material
        material = new Material("mat1", "Apuntes de Java", "https://ejemplo.com/apuntes.pdf");
        material = entityManager.persistAndFlush(material);
        
        // Crear alumnos con DNIs válidos
        alumno1 = new Alumno("alumno1", "password1", "Juan", "Pérez", generateValidDNI(1), "juan@ejemplo.com", "123456789");
        alumno2 = new Alumno("alumno2", "password2", "María", "García", generateValidDNI(2), "maria@ejemplo.com", "987654321");
        alumno3 = new Alumno("alumno3", "password3", "Carlos", "López", generateValidDNI(3), "carlos@ejemplo.com", "555666777");
        
        // Crear profesores con DNIs válidos
        profesor1 = new Profesor("profesor1", "password1", "Ana", "Martínez", generateValidDNI(4), "ana@ejemplo.com", "111111111");
        profesor2 = new Profesor("profesor2", "password2", "Pedro", "Sánchez", generateValidDNI(5), "pedro@ejemplo.com", "222222222");
        
        // Guardar entidades relacionadas primero
        alumno1 = repositorioAlumno.save(alumno1);
        alumno2 = repositorioAlumno.save(alumno2);
        alumno3 = repositorioAlumno.save(alumno3);
        profesor1 = repositorioProfesor.save(profesor1);
        profesor2 = repositorioProfesor.save(profesor2);
        
        // Crear cursos primero sin relaciones
        curso = new Curso(
                "Curso de Java", "Aprende Java desde cero", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen1.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(30)
        );
        curso = repositorioClase.save(curso);

        taller = new Taller(
                "Taller de Spring", "Taller intensivo de Spring Boot", new BigDecimal("49.99"),
                EPresencialidad.PRESENCIAL, "imagen2.jpg", EDificultad.INTERMEDIO,
                4, LocalDate.now().plusDays(3), LocalTime.of(10, 0)
        );
        taller = repositorioClase.save(taller);
        
        // Crear ejercicios después de guardar las clases para referenciarlas
        ejercicio1 = new Ejercicio("Ejercicio 1", "Enunciado del ejercicio 1", 
                                  LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(7).atStartOfDay(), curso.getId().toString());
        ejercicio2 = new Ejercicio("Ejercicio 2", "Enunciado del ejercicio 2", 
                                  LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(7).atStartOfDay(), taller.getId().toString());
        ejercicio1 = repositorioEjercicio.save(ejercicio1);
        ejercicio2 = repositorioEjercicio.save(ejercicio2);
        
        // Ahora establecer las relaciones usando los métodos helper para mantener consistencia
        curso.agregarAlumno(alumno1);
        curso.agregarAlumno(alumno2);
        curso.agregarProfesor(profesor1);
        curso.agregarEjercicio(ejercicio1); // This properly sets up the bidirectional relationship
        curso.agregarMaterial(material);
        curso = repositorioClase.save(curso);

        taller.agregarAlumno(alumno3);
        taller.agregarProfesor(profesor2);
        taller.agregarEjercicio(ejercicio2); // This properly sets up the bidirectional relationship
        taller = repositorioClase.save(taller);
    }

    @Test
    @DisplayName("save debe guardar una clase correctamente")
    void testSave() {
        Curso cursoGuardado = repositorioClase.save(curso);

        assertNotNull(cursoGuardado);
        assertNotNull(cursoGuardado.getId());
        assertEquals("Curso de Java", cursoGuardado.getTitle());
        assertEquals(2, cursoGuardado.getStudents().size());
        assertEquals(1, cursoGuardado.getTeachers().size());
        assertEquals(1, cursoGuardado.getExercises().size());
        assertEquals(1, cursoGuardado.getMaterial().size());
    }

    @Test
    @DisplayName("findById debe encontrar una clase por ID")
    void testFindById() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        Optional<Clase> resultado = repositorioClase.findById(id);

        assertTrue(resultado.isPresent());
        assertEquals("Curso de Java", resultado.get().getTitle());
        assertEquals(EPresencialidad.ONLINE, resultado.get().getFormat());
        assertEquals(EDificultad.PRINCIPIANTE, resultado.get().getDifficulty());
    }

    @Test
    @DisplayName("findById debe retornar empty cuando no existe")
    void testFindByIdNoExiste() {
        Optional<Clase> resultado = repositorioClase.findById(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByTitle debe encontrar una clase por título")
    void testFindByTitle() {
        repositorioClase.save(curso);

        Optional<Clase> resultado = repositorioClase.findByTitle("Curso de Java");

        assertTrue(resultado.isPresent());
        assertEquals("Curso de Java", resultado.get().getTitle());
    }

    @Test
    @DisplayName("findByTitle debe retornar empty cuando no existe")
    void testFindByTitleNoExiste() {
        Optional<Clase> resultado = repositorioClase.findByTitle("Clase Inexistente");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase debe encontrar clases por título parcial")
    void testFindByTitleContainingIgnoreCase() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByTitleContainingIgnoreCase("Java");

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase debe ser case insensitive")
    void testFindByTitleContainingIgnoreCaseCaseInsensitive() {
        repositorioClase.save(curso);

        List<Clase> resultado = repositorioClase.findByTitleContainingIgnoreCase("java");

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByDescriptionContainingIgnoreCase debe encontrar clases por descripción")
    void testFindByDescriptionContainingIgnoreCase() {
        repositorioClase.save(curso);

        List<Clase> resultado = repositorioClase.findByDescriptionContainingIgnoreCase("Java");

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByFormat debe encontrar clases por presencialidad")
    void testFindByFormat() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByFormat(EPresencialidad.ONLINE);

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByDifficulty debe encontrar clases por nivel")
    void testFindByDifficulty() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByDifficulty(EDificultad.PRINCIPIANTE);

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByPriceBetween debe encontrar clases por rango de precio")
    void testFindByPriceBetween() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByPriceBetween(
                new BigDecimal("40.00"), new BigDecimal("60.00"));

        assertEquals(1, resultado.size());
        assertEquals("Taller de Spring", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByPriceLessThanEqual debe encontrar clases por precio máximo")
    void testFindByPriceLessThanEqual() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByPriceLessThanEqual(new BigDecimal("50.00"));

        assertEquals(1, resultado.size());
        assertEquals("Taller de Spring", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findAllOrderedById debe retornar clases ordenadas por ID")
    void testFindAllOrderedById() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findAllOrderedById();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getId() < resultado.get(1).getId());
    }

    @Test
    @DisplayName("findAllOrderedByPrecioAsc debe retornar clases ordenadas por precio ascendente")
    void testFindAllOrderedByPrecioAsc() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findAllOrderedByPrecioAsc();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getPrice().compareTo(resultado.get(1).getPrice()) <= 0);
    }

    @Test
    @DisplayName("findAllOrderedByTitulo debe retornar clases ordenadas por título")
    void testFindAllOrderedByTitulo() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findAllOrderedByTitulo();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getTitle().compareTo(resultado.get(1).getTitle()) <= 0);
    }

    @Test
    @DisplayName("findByAlumnoId debe encontrar clases por alumno")
    void testFindByAlumnoId() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByAlumnoId(alumno1.getId());

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByProfesorId debe encontrar clases por profesor")
    void testFindByProfesorId() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByProfesorId(profesor1.getId());

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findByEjercicioId debe encontrar clases por ejercicio")
    void testFindByEjercicioId() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findByEjercicioId(ejercicio1.getId());

        assertEquals(1, resultado.size());
        assertEquals("Curso de Java", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("countAlumnosByClaseId debe contar alumnos correctamente")
    void testCountAlumnosByClaseId() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        Integer resultado = repositorioClase.countAlumnosByClaseId(id);

        assertEquals(2, resultado);
    }

    @Test
    @DisplayName("countProfesoresByClaseId debe contar profesores correctamente")
    void testCountProfesoresByClaseId() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        Integer resultado = repositorioClase.countProfesoresByClaseId(id);

        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("findClasesSinAlumnos debe encontrar clases sin alumnos")
    void testFindClasesSinAlumnos() {
        Curso cursoSinAlumnos = new Curso(
                "Curso Vacío", "Curso sin alumnos", new BigDecimal("29.99"),
                EPresencialidad.ONLINE, "imagen3.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)
        );

        repositorioClase.save(curso);
        repositorioClase.save(cursoSinAlumnos);

        List<Clase> resultado = repositorioClase.findClasesSinAlumnos();

        assertEquals(1, resultado.size());
        assertEquals("Curso Vacío", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("findClasesSinProfesores debe encontrar clases sin profesores")
    void testFindClasesSinProfesores() {
        Curso cursoSinProfesores = new Curso(
                "Curso Sin Profesor", "Curso sin profesores", new BigDecimal("19.99"),
                EPresencialidad.ONLINE, "imagen4.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(15), LocalDate.now().plusDays(25)
        );

        repositorioClase.save(curso);
        repositorioClase.save(cursoSinProfesores);

        List<Clase> resultado = repositorioClase.findClasesSinProfesores();

        assertEquals(1, resultado.size());
        assertEquals("Curso Sin Profesor", resultado.get(0).getTitle());
    }

    @Test
    @DisplayName("delete debe eliminar una clase correctamente")
    void testDelete() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        repositorioClase.delete(cursoGuardado);

        Optional<Clase> resultado = repositorioClase.findById(id);
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("deleteById debe eliminar una clase por ID correctamente")
    void testDeleteById() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        repositorioClase.deleteById(id);

        Optional<Clase> resultado = repositorioClase.findById(id);
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("existsById debe retornar true cuando la clase existe")
    void testExistsById() {
        Curso cursoGuardado = repositorioClase.save(curso);
        Long id = cursoGuardado.getId();

        boolean resultado = repositorioClase.existsById(id);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("existsById debe retornar false cuando la clase no existe")
    void testExistsByIdNoExiste() {
        boolean resultado = repositorioClase.existsById(999L);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("saveAll debe guardar múltiples clases correctamente")
    void testSaveAll() {
        List<Clase> clases = Arrays.asList(curso, taller);

        List<Clase> resultado = repositorioClase.saveAll(clases);

        assertEquals(2, resultado.size());
        assertNotNull(resultado.get(0).getId());
        assertNotNull(resultado.get(1).getId());
    }

    @Test
    @DisplayName("findAll debe retornar todas las clases")
    void testFindAll() {
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        List<Clase> resultado = repositorioClase.findAll();

        assertEquals(2, resultado.size());
    }
}
