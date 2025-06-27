package app.servicios;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion {
    
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder pe;
    private final ServicioJwt servicioJwt;
    private final AuthenticationManager authenticationManager;
    
    public DTORespuestaLogin login(DTOPeticionLogin request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        
        var usuario = repositorioUsuario.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        var jwtToken = servicioJwt.generateToken(usuario);
        return DTORespuestaLogin.from(usuario, jwtToken);
    }
    
    public DTORespuestaLogin registro(DTOPeticionRegistro request) {
        if (repositorioUsuario.existsByUsername(request.username())) {
            throw new RuntimeException("El username ya existe");
        }
        
        if (repositorioUsuario.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya existe");
        }
        
        var usuario = new Usuario(
                request.username(),
                pe.encode(request.password()),
                request.email(),
                request.nombre(),
                request.apellidos()
        );
        
        repositorioUsuario.save(usuario);
        
        var jwtToken = servicioJwt.generateToken(usuario);
        return DTORespuestaLogin.from(usuario, jwtToken);
    }
} 