package app.dtos;

public record DTOParametrosBusquedaAlumno(
    String q,              // General search term across multiple fields
    String nombre,
    String apellidos,
    String dni,
    String email,
    Boolean matriculado
) {
    /**
     * Constructor with default values for backward compatibility
     */
    public DTOParametrosBusquedaAlumno(String nombre, String apellidos, String dni, String email, Boolean matriculado) {
        this(null, nombre, apellidos, dni, email, matriculado);
    }
    
    /**
     * Default constructor for backward compatibility
     */
    public DTOParametrosBusquedaAlumno() {
        this(null, null, null, null, null, null);
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
        return nombre != null || apellidos != null || dni != null || 
               email != null || matriculado != null;
    }
    
    /**
     * Checks if all parameters are empty (for backward compatibility)
     * @return true if no search criteria are provided
     */
    public boolean estaVacio() {
        return !hasGeneralSearch() && !hasSpecificFilters();
    }
}
