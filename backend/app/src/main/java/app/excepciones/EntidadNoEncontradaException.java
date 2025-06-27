package app.excepciones;

public class EntidadNoEncontradaException extends RuntimeException {
    public EntidadNoEncontradaException(String message) {
        super(message);
    }
}
