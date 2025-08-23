package app.dtos;

/**
 * DTO para parámetros de búsqueda de profesores
 * Permite realizar búsquedas con múltiples criterios
 */
public record DTOParametrosBusquedaProfesor(
        String q,              // General search term across multiple fields
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
     * Constructor with default values for backward compatibility
     */
    public DTOParametrosBusquedaProfesor(String nombre, String apellidos, String email, String usuario, String dni, Boolean habilitado, String claseId, Boolean sinClases) {
        this(null, nombre, apellidos, email, usuario, dni, habilitado, claseId, sinClases);
    }
    
    /**
     * Constructor por defecto con todos los parámetros nulos
     */
    public DTOParametrosBusquedaProfesor() {
        this(null, null, null, null, null, null, null, null, null);
    }
    
    /**
     * Checks if general search should be used
     * @return true if general search term is provided
     */
    public boolean hasGeneralSearch() {
        return q != null && !q.trim().isEmpty();
    }
    
    /**
     * Checks if specific filters are provided
     * @return true if any specific filter is provided
     */
    public boolean hasSpecificFilters() {
        return nombre != null || apellidos != null || email != null || 
               usuario != null || dni != null || habilitado != null || 
               claseId != null || sinClases != null;
    }
    
    /**
     * Verifica si todos los parámetros están vacíos
     * @return true si no hay criterios de búsqueda
     */
    public boolean estaVacio() {
        return !hasGeneralSearch() && !hasSpecificFilters();
    }
    
    /**
     * Verifica si hay criterios de texto para búsqueda
     * @return true si hay al menos un criterio de texto
     */
    public boolean tieneCriteriosTexto() {
        return hasGeneralSearch() || nombre != null || apellidos != null || email != null || 
               usuario != null || dni != null;
    }
}
