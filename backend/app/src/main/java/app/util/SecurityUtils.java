package app.util;

import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private final RepositorioUsuario repositorioUsuario;
    
    /**
     * Gets the current authenticated user from the security context
     * @return The current user entity
     * @throws RuntimeException if no user is authenticated or user not found
     */
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No user is currently authenticated");
        }
        
        String username = authentication.getName();
        return repositorioUsuario.findByUsuario(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    /**
     * Gets the current user's ID
     * @return The current user's ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * Gets the current user's username
     * @return The current user's username
     */
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
    
    /**
     * Checks if the current user has a specific role
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Checks if the current user is an admin
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Checks if the current user is a professor
     * @return true if the user is a professor, false otherwise
     */
    public boolean isProfessor() {
        return hasRole("PROFESOR");
    }
    
    /**
     * Checks if the current user is a student
     * @return true if the user is a student, false otherwise
     */
    public boolean isStudent() {
        return hasRole("ALUMNO");
    }
}

