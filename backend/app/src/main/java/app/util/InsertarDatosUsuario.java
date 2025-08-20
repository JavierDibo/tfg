package app.util;

import app.entidades.Alumno;
import app.entidades.Usuario;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class InsertarDatosUsuario implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioAlumno repositorioAlumno;
    private final PasswordEncoder pe;
    private final Random random = new Random();
    private ArrayList<Alumno> alumnos = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        datos();
    }

    private void datos() {
        // Crear usuarios de prueba si no existen
        if (repositorioUsuario.count() == 0) {
            crearEstudiantesAleatorios(1000);
            crearUsuariosPrueba();
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
        usuarios.add(admin);
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
} 