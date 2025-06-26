package app.aop.exceptions;

/**
 * Excepción para parámetros inválidos en métodos
 */
public class ParametroInvalidoException extends ValidacionException {
    
    private final String parametro;
    private final Object valor;
    
    public ParametroInvalidoException(String parametro, Object valor, String mensaje) {
        super(String.format("Parámetro '%s' con valor '%s' es inválido: %s", parametro, valor, mensaje));
        this.parametro = parametro;
        this.valor = valor;
    }
    
    public String getParametro() {
        return parametro;
    }
    
    public Object getValor() {
        return valor;
    }
} 