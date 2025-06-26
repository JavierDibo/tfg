package app.aop.exceptions;

/**
 * Excepción base para errores de validación
 */
public class ValidacionException extends RuntimeException {
    
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
    
    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
} 