package app.excepciones;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja validaciones de @RequestBody con @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Error de validación en @RequestBody: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido",
                    (existing, replacement) -> existing // En caso de duplicados, mantener el primero
                ));
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Errores de Validación");
        errorDetails.put("message", "Los datos enviados contienen errores");
        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Maneja validaciones de @PathVariable y @RequestParam
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("Error de validación en parámetros: {}", ex.getMessage());
        
        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> getPropertyName(violation),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing
                ));
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Parámetros Inválidos");
        errorDetails.put("message", "Los parámetros enviados son inválidos");
        errorDetails.put("violations", violations);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    private String getPropertyName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        // Extraer solo el nombre del parámetro (ej: "obtenerEntidadPorId.id" -> "id")
        return propertyPath.contains(".") ? 
               propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : 
               propertyPath;
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleEntidadNoEncontrada(
            EntidadNoEncontradaException ex, WebRequest request) {

        log.warn("Entidad no encontrada: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Entidad no encontrada",
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        log.warn("Intento de login fallido: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Autenticación fallida",
                "message", "Usuario o contraseña incorrectos",
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }



    @ExceptionHandler(AlumnoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleAlumnoNoEncontrado(
            AlumnoNoEncontradoException ex, WebRequest request) {

        log.warn("Alumno no encontrado: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Alumno no encontrado",
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Argumento inválido: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Datos inválidos",
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, WebRequest request) {
        
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error", "Error Interno del Servidor",
            "message", "Ha ocurrido un error inesperado",
            "path", request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 