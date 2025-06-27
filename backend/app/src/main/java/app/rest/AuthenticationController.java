package app.rest;

import app.dtos.LoginRequest;
import app.dtos.LoginResponse;
import app.dtos.RegistroRequest;
import app.servicios.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
    
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authenticationService.registro(request));
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Autenticación funcionando correctamente");
    }
} 