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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USUARIO")
public class Usuario implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String usuario;
    
    @NotNull
    @Size(min = 6)
    private String contraseña;
    
    @NotNull
    @Size(max = 100)
    private String nombre;
    
    @NotNull
    @Size(max = 100)
    private String apellidos;
    
    @NotNull
    @Size(max = 20)
    @Column(unique = true)
    private String dni;
    
    @NotNull
    @Email
    @Column(unique = true)
    private String email;
    
    @Size(max = 15)
    private String numeroTelefono;
    
    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.USUARIO;
    
    private boolean enabled = true;
    
    public enum Rol {
        ADMIN, PROFESOR, ALUMNO, USUARIO
    }
    
    public Usuario() {}
    
    public Usuario(String usuario, String contraseña, String nombre, String apellidos, String dni, String email, String numeroTelefono) {
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.email = email;
        this.numeroTelefono = numeroTelefono;
    }
    
    @Override
    public String getUsername() {
        return this.usuario;
    }
    
    @Override
    public String getPassword() {
        return this.contraseña;
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