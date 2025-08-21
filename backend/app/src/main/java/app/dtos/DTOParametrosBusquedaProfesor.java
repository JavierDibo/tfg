package app.dtos;

/**
 * DTO para parámetros de búsqueda de profesores
 * Permite realizar búsquedas con múltiples criterios
 */
public record DTOParametrosBusquedaProfesor(
        String nombre,
        String apellidos,
        String email,
        String usuario,
        String dni,
        Boolean habilitado,
        String claseId,
        Boolean sinClases
) {
    
    /**
     * Constructor por defecto con todos los parámetros nulos
     */
    public DTOParametrosBusquedaProfesor() {
        this(null, null, null, null, null, null, null, null);
    }
    
    /**
     * Verifica si todos los parámetros están vacíos
     * @return true si no hay criterios de búsqueda
     */
    public boolean estaVacio() {
        return nombre == null && apellidos == null && email == null && 
               usuario == null && dni == null && habilitado == null && 
               claseId == null && sinClases == null;
    }
    
    /**
     * Verifica si hay criterios de texto para búsqueda
     * @return true si hay al menos un criterio de texto
     */
    public boolean tieneCriteriosTexto() {
        return nombre != null || apellidos != null || email != null || 
               usuario != null || dni != null;
    }
}
