package app.rest;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.servicios.ServicioAutenticacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Autenticación", description = "API para autenticación y registro de usuarios")
public class AutenticacionRest {
    
    private final ServicioAutenticacion servicioAutenticacion;
    
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario con sus credenciales y devuelve un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaLogin.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
        )
    })
    public ResponseEntity<DTORespuestaLogin> login(
            @Parameter(description = "Credenciales de login", required = true)
            @Valid @RequestBody DTOPeticionLogin request) {
        return ResponseEntity.ok(servicioAutenticacion.login(request));
    }
    
    @PostMapping("/registro")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario y devuelve un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registro exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaLogin.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o usuario ya existe"
        )
    })
    public ResponseEntity<DTORespuestaLogin> registro(
            @Parameter(description = "Datos de registro", required = true)
            @Valid @RequestBody DTOPeticionRegistro request) {
        return ResponseEntity.ok(servicioAutenticacion.registro(request));
    }
    
    @GetMapping("/test")
    @Operation(
        summary = "Probar autenticación",
        description = "Endpoint de prueba para verificar que la autenticación funciona correctamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación funcionando correctamente",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        )
    })
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Autenticación funcionando correctamente");
    }
} 