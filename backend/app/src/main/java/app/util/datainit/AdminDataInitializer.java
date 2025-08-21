package app.util.datainit;

import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer extends BaseDataInitializer {
    
    @Override
    public void initialize() {
        RepositorioUsuario repositorioUsuario = context.getBean(RepositorioUsuario.class);
        
        // Initialize password encoder from base class
        initializePasswordEncoder();
        
        // Check if admin already exists
        if (repositorioUsuario.findByUsuario("admin").isEmpty()) {
            Usuario admin = new Usuario(
                "admin",
                encodePassword("admin"), // Use the base class method for consistency
                "Administrador",
                "Sistema",
                "12345678Z",
                "admin@academia.com",
                "600000000"
            );
            admin.setRol(Usuario.Rol.ADMIN);
            
            try {
                repositorioUsuario.save(admin);
                System.out.println("✓ Admin user created successfully with encoded password");
            } catch (Exception e) {
                System.err.println("✗ Error creating admin user: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ Admin user already exists");
        }
    }
}
