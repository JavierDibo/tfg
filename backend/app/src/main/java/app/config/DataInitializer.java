package app.config;

import app.entidades.Usuario;
import app.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuarios de prueba si no existen
        if (usuarioRepositorio.count() == 0) {
            Usuario admin = new Usuario(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "admin@academia.com",
                    "Administrador",
                    "Sistema"
            );
            admin.setRol(Usuario.Rol.ADMIN);
            usuarioRepositorio.save(admin);

            Usuario usuario = new Usuario(
                    "usuario",
                    passwordEncoder.encode("user123"),
                    "usuario@academia.com",
                    "Usuario",
                    "Prueba"
            );
            usuario.setRol(Usuario.Rol.USUARIO);
            usuarioRepositorio.save(usuario);

            System.out.println("Usuarios de prueba creados:");
            System.out.println("Admin - username: admin, password: admin123");
            System.out.println("Usuario - username: usuario, password: user123");
        }
    }
} 