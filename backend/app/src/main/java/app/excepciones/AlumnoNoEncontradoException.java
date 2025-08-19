package app.excepciones;

/**
 * Excepción específica para cuando no se encuentra un alumno
 */
public class AlumnoNoEncontradoException extends RuntimeException {
    
    public AlumnoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    
    public AlumnoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
