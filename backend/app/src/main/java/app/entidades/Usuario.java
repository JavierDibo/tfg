package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;
    
    @NotNull
    @Size(min = 6)
    private String password;
    
    @NotNull
    @Email
    @Column(unique = true)
    private String email;
    
    @NotNull
    @Size(max = 100)
    private String nombre;
    
    @NotNull
    @Size(max = 100)
    private String apellidos;
    
    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.USUARIO;
    
    private boolean enabled = true;
    
    public enum Rol {
        ADMIN, USUARIO
    }
    
    public Usuario() {}
    
    public Usuario(String username, String password, String email, String nombre, String apellidos) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
} 