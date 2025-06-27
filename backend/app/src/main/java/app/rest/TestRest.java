package app.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestRest {

    @GetMapping("/public")
    public ResponseEntity<String> publico() {
        return ResponseEntity.ok("Este endpoint es público - no requiere autenticación");
    }

    @GetMapping("/protected")
    public ResponseEntity<String> usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok("Endpoint protegido - Usuario autenticado: " + username);
    }

    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            return ResponseEntity.ok("Endpoint de admin - Usuario: " + username);
        } else {
            return ResponseEntity.status(403).body("Acceso denegado - Se requiere rol ADMIN");
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<String> obtenerInfoUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String authorities = auth.getAuthorities().toString();
        
        return ResponseEntity.ok(String.format(
                "Información del usuario:\nUsername: %s\nRoles: %s",
                username, authorities
        ));
    }
} 