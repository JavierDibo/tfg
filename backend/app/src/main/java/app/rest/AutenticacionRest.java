package app.rest;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.servicios.ServicioAutenticacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutenticacionRest {
    
    private final ServicioAutenticacion servicioAutenticacion;
    
    @PostMapping("/login")
    public ResponseEntity<DTORespuestaLogin> login(@Valid @RequestBody DTOPeticionLogin request) {
        return ResponseEntity.ok(servicioAutenticacion.login(request));
    }
    
    @PostMapping("/registro")
    public ResponseEntity<DTORespuestaLogin> registro(@Valid @RequestBody DTOPeticionRegistro request) {
        return ResponseEntity.ok(servicioAutenticacion.registro(request));
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Autenticación funcionando correctamente");
    }
} 