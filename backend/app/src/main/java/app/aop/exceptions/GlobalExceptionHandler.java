package app.aop.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleParametroInvalido(
            ParametroInvalidoException ex, WebRequest request) {
        
        log.warn("Error de validación de parámetro: {}", ex.getMessage());
        
        Map<String, Object> errorDetails = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Parámetro Inválido",
            "message", ex.getMessage(),
            "parametro", ex.getParametro(),
            "valor", ex.getValor() != null ? ex.getValor() : "null",
            "path", request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleEntidadNoEncontrada(
            EntidadNoEncontradaException ex, WebRequest request) {

        log.warn("Entidad no encontrada: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Entidad no encontrada",
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(
            ValidacionException ex, WebRequest request) {
        
        log.warn("Error de validación: {}", ex.getMessage());
        
        Map<String, Object> errorDetails = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Error de Validación",
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