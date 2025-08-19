package app.util;

import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsertarDatosUsuario implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder pe;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuarios de prueba si no existen
        if (repositorioUsuario.count() == 0) {
            Usuario admin = new Usuario(
                    "admin",
                    pe.encode("admin"),
                    "admin@academia.com",
                    "Administrador",
                    "Sistema"
            );
            admin.setRol(Usuario.Rol.ADMIN);
            repositorioUsuario.save(admin);

            Usuario usuario = new Usuario(
                    "user",
                    pe.encode("user"),
                    "usuario@academia.com",
                    "Usuario",
                    "Prueba"
            );
            usuario.setRol(Usuario.Rol.USUARIO);
            repositorioUsuario.save(usuario);

            System.out.println("Usuarios de prueba creados:");
            System.out.println("Admin - username: " + admin.getUsername() + ", password: admin" );
            System.out.println("Usuario - username: " + usuario.getUsername() + ", password: user");
        }
    }
} 