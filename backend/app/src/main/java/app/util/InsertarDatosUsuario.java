package app.util;

import app.entidades.*;
import app.entidades.enums.*;
import app.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class InsertarDatosUsuario implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioProfesor repositorioProfesor;
    private final RepositorioAdministrador repositorioAdministrador;
    private final RepositorioClase repositorioClase;
    private final RepositorioPago repositorioPago;
    private final RepositorioEjercicio repositorioEjercicio;
    private final RepositorioEntregaEjercicio repositorioEntregaEjercicio;
    private final PasswordEncoder pe;
    private final Random random = new Random();
    private ArrayList<Alumno> alumnos = new ArrayList<>();
    private ArrayList<Profesor> profesores = new ArrayList<>();
    private ArrayList<Administrador> administradores = new ArrayList<>();
    private ArrayList<Clase> clases = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        datos();
    }

    private void datos() {
        // Crear usuarios de prueba si no existen
        if (repositorioUsuario.count() == 0) {
            System.out.println("==== CREANDO DATOS DE PRUEBA ====");
            crearUsuariosPrueba();
            crearEstudiantesAleatorios(45);
            crearProfesoresAleatorios(45);
            crearAdministradoresAleatorios(5);
            crearClasesAleatorias(45);
            crearPagosAleatorios(45);
            crearEjerciciosAleatorios(45);
            crearEntregasAleatorias(45);
            System.out.println("==== DATOS DE PRUEBA CREADOS ====");
        }
    }

    private void crearUsuariosPrueba() {

        ArrayList<Usuario> usuarios = new ArrayList<>();

        Usuario admin = new Usuario(
                "admin",
                pe.encode("admin"),
                "Administrador",
                "Sistema",
                "12345678Z",
                "admin@academia.com",
                "600000000"
        );
        admin.setRol(Usuario.Rol.ADMIN);
        usuarios.add(admin);

        // Crear un usuario deshabilitado para pruebas
        Usuario usuarioDeshabilitado = new Usuario(
                "disabled_user",
                pe.encode("password123"),
                "Usuario",
                "Deshabilitado",
                "11111111H",
                "disabled@academia.com",
                "600000002"
        );
        usuarioDeshabilitado.setRol(Usuario.Rol.USUARIO);
        usuarioDeshabilitado.setEnabled(false); // Usuario deshabilitado
        usuarios.add(usuarioDeshabilitado);
        repositorioUsuario.saveAll(usuarios);

        System.out.println("Usuarios de prueba creados:");
        System.out.println("Admin - username: " + admin.getUsername() + ", password: admin");
        System.out.println("Usuario Deshabilitado - username: " + usuarioDeshabilitado.getUsername() + ", password: password123 (DESHABILITADO)");
    }

    private void crearEstudiantesAleatorios(int numeroEstudiantes) {
        List<String> nombres = List.of(
                "Carlos", "María", "José", "Ana", "Luis", "Carmen", "Antonio", "Isabel",
                "Manuel", "Pilar", "Francisco", "Dolores", "David", "Teresa", "Jesús",
                "Rosario", "Javier", "Antonia", "Daniel", "Mercedes", "Rafael", "Josefa",
                "Miguel", "Francisca", "Alejandro", "Cristina", "Fernando", "Rosa",
                "Sergio", "Lucía", "Pablo", "Esperanza", "Jorge", "Elena", "Alberto"
        );

        List<String> apellidos = List.of(
                "García", "Rodríguez", "González", "Fernández", "López", "Martínez",
                "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández",
                "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez",
                "Navarro", "Torres", "Domínguez", "Vázquez", "Ramos", "Gil", "Ramírez",
                "Serrano", "Blanco", "Suárez", "Molina", "Morales", "Ortega", "Delgado",
                "Castro", "Ortiz", "Rubio", "Marín", "Sanz", "Iglesias", "Medina"
        );

        System.out.println("\nCreando " + numeroEstudiantes + " estudiantes aleatorios:");

        for (int i = 1; i <= numeroEstudiantes; i++) {
            String nombre = nombres.get(random.nextInt(nombres.size()));
            String apellido1 = apellidos.get(random.nextInt(apellidos.size()));
            String apellido2 = apellidos.get(random.nextInt(apellidos.size()));
            String apellidosCompletos = apellido1 + " " + apellido2;

            String usuario = "estudiante" + String.format("%02d", i);
            String email = usuario + "@academia.com";
            String dni = generarDniAleatorio();
            String telefono = generarTelefonoAleatorio();

            // Algunos estudiantes estarán matriculados
            boolean matriculado = random.nextBoolean();

            Alumno alumno = new Alumno(
                    usuario,
                    pe.encode("password123"), // password por defecto
                    nombre,
                    apellidosCompletos,
                    dni,
                    email,
                    telefono
            );

            alumno.setMatriculado(matriculado);
            alumnos.add(alumno);

            // System.out.println(String.format("  %d. %s %s - usuario: %s, DNI: %s, matriculado: %s", i, nombre, apellidosCompletos, usuario, dni, matriculado ? "Sí" : "No"));
        }

        repositorioAlumno.saveAll(alumnos);
        System.out.println("Estudiantes aleatorios creados: " + alumnos.size());
    }

    private String generarDniAleatorio() {
        // Generar 8 dígitos aleatorios
        int numeroAleatorio = random.nextInt(90000000) + 10000000; // Entre 10000000 y 99999999

        // Calcular letra del DNI
        char[] letras = {'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D', 'X', 'B',
                'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'};
        char letra = letras[numeroAleatorio % 23];

        return numeroAleatorio + String.valueOf(letra);
    }

    private String generarTelefonoAleatorio() {
        // Generar teléfono español (6XXXXXXXX o 7XXXXXXXX)
        int prefijo = random.nextBoolean() ? 6 : 7;
        int numero = random.nextInt(90000000) + 10000000; // 8 dígitos más
        return prefijo + String.valueOf(numero).substring(1); // 9 dígitos totales
    }

    private void crearProfesoresAleatorios(int numeroProfesores) {
        List<String> nombres = List.of(
                "Carlos", "María", "José", "Ana", "Luis", "Carmen", "Antonio", "Isabel",
                "Manuel", "Pilar", "Francisco", "Dolores", "David", "Teresa", "Jesús",
                "Rosario", "Javier", "Antonia", "Daniel", "Mercedes", "Rafael", "Josefa",
                "Miguel", "Francisca", "Alejandro", "Cristina", "Fernando", "Rosa",
                "Sergio", "Lucía", "Pablo", "Esperanza", "Jorge", "Elena", "Alberto"
        );

        List<String> apellidos = List.of(
                "García", "Rodríguez", "González", "Fernández", "López", "Martínez",
                "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández",
                "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez"
        );

        System.out.println("\nCreando " + numeroProfesores + " profesores aleatorios:");

        for (int i = 1; i <= numeroProfesores; i++) {
            String nombre = nombres.get(random.nextInt(nombres.size()));
            String apellido1 = apellidos.get(random.nextInt(apellidos.size()));
            String apellido2 = apellidos.get(random.nextInt(apellidos.size()));
            String apellidosCompletos = apellido1 + " " + apellido2;

            String usuario = "profesor" + String.format("%02d", i);
            String email = usuario + "@academia.com";
            String dni = generarDniAleatorio();
            String telefono = generarTelefonoAleatorio();

            Profesor profesor = new Profesor(
                    usuario,
                    pe.encode("password123"),
                    nombre,
                    apellidosCompletos,
                    dni,
                    email,
                    telefono
            );

            profesores.add(profesor);
        }

        repositorioProfesor.saveAll(profesores);
        System.out.println("Profesores aleatorios creados: " + profesores.size());
    }

    private void crearAdministradoresAleatorios(int numeroAdministradores) {
        List<String> nombres = List.of("Juan", "María", "Pedro", "Carmen", "Luis");
        List<String> apellidos = List.of("García", "López", "Martínez", "González", "Rodríguez");

        System.out.println("\nCreando " + numeroAdministradores + " administradores:");

        for (int i = 1; i <= numeroAdministradores; i++) {
            String nombre = nombres.get(random.nextInt(nombres.size()));
            String apellido1 = apellidos.get(random.nextInt(apellidos.size()));
            String apellido2 = apellidos.get(random.nextInt(apellidos.size()));
            String apellidosCompletos = apellido1 + " " + apellido2;

            String usuario = "admin" + String.format("%02d", i);
            String email = usuario + "@academia.com";
            String dni = generarDniAleatorio();
            String telefono = generarTelefonoAleatorio();

            Administrador administrador = new Administrador(
                    usuario,
                    pe.encode("password123"),
                    nombre,
                    apellidosCompletos,
                    dni,
                    email,
                    telefono
            );

            administradores.add(administrador);
        }

        repositorioAdministrador.saveAll(administradores);
        System.out.println("Administradores creados: " + administradores.size());
    }

    private void crearClasesAleatorias(int numeroClases) {
        List<String> titulosTalleres = List.of(
                "Taller de Programación Web", "Taller de Diseño UX/UI", "Taller de Marketing Digital",
                "Taller de Fotografía", "Taller de Escritura Creativa", "Taller de Excel Avanzado",
                "Taller de Redes Sociales", "Taller de WordPress", "Taller de Python Básico",
                "Taller de Photoshop", "Taller de Video Editing", "Taller de SEO"
        );

        List<String> titulosCursos = List.of(
                "Curso Completo de Java", "Curso de React y Node.js", "Curso de Data Science",
                "Curso de Machine Learning", "Curso de Ciberseguridad", "Curso de DevOps",
                "Curso de Blockchain", "Curso de Inteligencia Artificial", "Curso de Cloud Computing",
                "Curso de Mobile Development", "Curso de Game Development", "Curso de Análisis de Datos"
        );

        System.out.println("\nCreando " + numeroClases + " clases aleatorias:");

        for (int i = 1; i <= numeroClases; i++) {
            boolean esTaller = random.nextBoolean();

            if (esTaller) {
                // Crear Taller
                String titulo = titulosTalleres.get(random.nextInt(titulosTalleres.size())) + " " + i;
                String descripcion = "Descripción detallada del " + titulo.toLowerCase();
                BigDecimal precio = BigDecimal.valueOf(50 + random.nextInt(200)); // 50-250€
                EPresencialidad presencialidad = random.nextBoolean() ? EPresencialidad.PRESENCIAL : EPresencialidad.ONLINE;
                ENivel nivel = ENivel.values()[random.nextInt(ENivel.values().length)];
                Integer duracionHoras = 2 + random.nextInt(6); // 2-8 horas
                LocalDate fechaRealizacion = LocalDate.now().plusDays(random.nextInt(60)); // próximos 60 días
                LocalTime horaComienzo = LocalTime.of(9 + random.nextInt(8), 0); // 9:00 - 17:00

                Taller taller = new Taller(titulo, descripcion, precio, presencialidad, 
                                         "imagen_" + i + ".jpg", nivel, duracionHoras, 
                                         fechaRealizacion, horaComienzo);

                // Asignar profesores aleatorios
                if (!profesores.isEmpty()) {
                    int numProfesores = 1 + random.nextInt(2); // 1-2 profesores
                    for (int j = 0; j < numProfesores && j < profesores.size(); j++) {
                        Profesor profesor = profesores.get(random.nextInt(profesores.size()));
                        taller.agregarProfesor(profesor.getId().toString());
                        profesor.agregarClase(taller.getId() != null ? taller.getId().toString() : "temp_" + i);
                    }
                }

                clases.add(taller);
            } else {
                // Crear Curso
                String titulo = titulosCursos.get(random.nextInt(titulosCursos.size())) + " " + i;
                String descripcion = "Descripción completa del " + titulo.toLowerCase();
                BigDecimal precio = BigDecimal.valueOf(200 + random.nextInt(800)); // 200-1000€
                EPresencialidad presencialidad = random.nextBoolean() ? EPresencialidad.PRESENCIAL : EPresencialidad.ONLINE;
                ENivel nivel = ENivel.values()[random.nextInt(ENivel.values().length)];
                LocalDate fechaInicio = LocalDate.now().plusDays(random.nextInt(30)); // próximos 30 días
                LocalDate fechaFin = fechaInicio.plusDays(30 + random.nextInt(60)); // 30-90 días duración

                Curso curso = new Curso(titulo, descripcion, precio, presencialidad,
                                      "imagen_" + i + ".jpg", nivel, fechaInicio, fechaFin);

                // Asignar profesores aleatorios
                if (!profesores.isEmpty()) {
                    int numProfesores = 1 + random.nextInt(3); // 1-3 profesores
                    for (int j = 0; j < numProfesores && j < profesores.size(); j++) {
                        Profesor profesor = profesores.get(random.nextInt(profesores.size()));
                        curso.agregarProfesor(profesor.getId().toString());
                        profesor.agregarClase(curso.getId() != null ? curso.getId().toString() : "temp_" + i);
                    }
                }

                clases.add(curso);
            }
        }

        repositorioClase.saveAll(clases);
        System.out.println("Clases aleatorias creadas: " + clases.size());
    }

    private void crearPagosAleatorios(int numeroPagos) {
        if (alumnos.isEmpty()) return;

        System.out.println("\nCreando " + numeroPagos + " pagos aleatorios:");

        for (int i = 1; i <= numeroPagos; i++) {
            Alumno alumno = alumnos.get(random.nextInt(alumnos.size()));
            BigDecimal importe = BigDecimal.valueOf(50 + random.nextInt(500)); // 50-550€
            EMetodoPago metodoPago = EMetodoPago.values()[random.nextInt(EMetodoPago.values().length)];
            EEstadoPago estado = random.nextInt(10) < 8 ? EEstadoPago.EXITO : 
                               (random.nextBoolean() ? EEstadoPago.ERROR : EEstadoPago.REEMBOLSADO);

            Pago pago = new Pago(importe, metodoPago, alumno.getId().toString());
            pago.setEstado(estado);
            pago.setFechaPago(LocalDateTime.now().minusDays(random.nextInt(365))); // último año

            // Agregar items al pago
            int numItems = 1 + random.nextInt(3); // 1-3 items
            for (int j = 1; j <= numItems; j++) {
                ItemPago item = new ItemPago(
                        "item_" + i + "_" + j,
                        "Concepto " + j + " del pago " + i,
                        BigDecimal.valueOf(10 + random.nextInt(100)),
                        1 + random.nextInt(3)
                );
                pago.agregarItem(item);
            }

            repositorioPago.save(pago);
        }

        System.out.println("Pagos aleatorios creados: " + numeroPagos);
    }

    private void crearEjerciciosAleatorios(int numeroEjercicios) {
        if (clases.isEmpty()) return;

        List<String> nombresEjercicios = List.of(
                "Ejercicio de Variables", "Práctica de Bucles", "Ejercicio de Funciones",
                "Proyecto Final", "Tarea de Investigación", "Laboratorio Práctico",
                "Ejercicio de Algoritmos", "Práctica de Base de Datos", "Proyecto Web",
                "Ejercicio de CSS", "Práctica de JavaScript", "Tarea de UX/UI"
        );

        System.out.println("\nCreando " + numeroEjercicios + " ejercicios aleatorios:");

        for (int i = 1; i <= numeroEjercicios; i++) {
            Clase clase = clases.get(random.nextInt(clases.size()));
            String nombre = nombresEjercicios.get(random.nextInt(nombresEjercicios.size())) + " " + i;
            String enunciado = "Enunciado detallado del ejercicio: " + nombre + ". " +
                             "Debe completar las siguientes tareas... [Descripción extensa del ejercicio]";

            LocalDateTime fechaInicio = LocalDateTime.now().plusDays(random.nextInt(30));
            LocalDateTime fechaFin = fechaInicio.plusDays(7 + random.nextInt(14)); // 1-3 semanas

            Ejercicio ejercicio = new Ejercicio(nombre, enunciado, fechaInicio, fechaFin, clase.getId().toString());
            repositorioEjercicio.save(ejercicio);

            // Agregar ejercicio a la clase
            clase.agregarEjercicio(ejercicio.getId().toString());
        }

        System.out.println("Ejercicios aleatorios creados: " + numeroEjercicios);
    }

    private void crearEntregasAleatorias(int numeroEntregas) {
        if (alumnos.isEmpty()) return;

        List<Ejercicio> ejercicios = repositorioEjercicio.findAll();
        if (ejercicios.isEmpty()) return;

        System.out.println("\nCreando " + numeroEntregas + " entregas aleatorias:");

        for (int i = 1; i <= numeroEntregas; i++) {
            Alumno alumno = alumnos.get(random.nextInt(alumnos.size()));
            Ejercicio ejercicio = ejercicios.get(random.nextInt(ejercicios.size()));

            // Verificar que no existe ya una entrega de este alumno para este ejercicio
            if (repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(
                    alumno.getId().toString(), ejercicio.getId().toString()).isPresent()) {
                continue; // Skip si ya existe
            }

            EntregaEjercicio entrega = new EntregaEjercicio(alumno.getId().toString(), ejercicio.getId().toString());
            entrega.setEjercicio(ejercicio);

            // Agregar archivos ficticios
            int numArchivos = 1 + random.nextInt(3);
            for (int j = 1; j <= numArchivos; j++) {
                entrega.agregarArchivo("archivo_" + i + "_" + j + ".pdf");
            }

            // Algunos ejercicios estarán calificados
            if (random.nextInt(10) < 7) { // 70% calificados
                BigDecimal nota = BigDecimal.valueOf(random.nextInt(101) / 10.0); // 0.0 - 10.0
                entrega.calificar(nota);
            }

            entrega.setFechaEntrega(LocalDateTime.now().minusDays(random.nextInt(30)));
            repositorioEntregaEjercicio.save(entrega);
        }

        System.out.println("Entregas aleatorias creadas: " + numeroEntregas);
    }
} 