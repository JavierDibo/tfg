package app.servicios;

import app.dtos.LoginRequest;
import app.dtos.LoginResponse;
import app.dtos.RegistroRequest;
import app.entidades.Usuario;
import app.repositorios.UsuarioRepositorio;
import app.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        
        var usuario = usuarioRepositorio.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        var jwtToken = jwtService.generateToken(usuario);
        return LoginResponse.from(usuario, jwtToken);
    }
    
    public LoginResponse registro(RegistroRequest request) {
        if (usuarioRepositorio.existsByUsername(request.username())) {
            throw new RuntimeException("El username ya existe");
        }
        
        if (usuarioRepositorio.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya existe");
        }
        
        var usuario = new Usuario(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                request.nombre(),
                request.apellidos()
        );
        
        usuarioRepositorio.save(usuario);
        
        var jwtToken = jwtService.generateToken(usuario);
        return LoginResponse.from(usuario, jwtToken);
    }
} 