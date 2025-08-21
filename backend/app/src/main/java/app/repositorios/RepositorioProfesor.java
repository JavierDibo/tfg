package app.repositorios;

import app.entidades.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Profesor
 * Proporciona operaciones de acceso a datos para profesores
 */
@Repository
public interface RepositorioProfesor extends JpaRepository<Profesor, Long> {
    
    /**
     * Busca un profesor por su nombre de usuario
     * @param usuario Nombre de usuario
     * @return Optional<Profesor>
     */
    Optional<Profesor> findByUsuario(String usuario);
    
    /**
     * Busca un profesor por su email
     * @param email Email del profesor
     * @return Optional<Profesor>
     */
    Optional<Profesor> findByEmail(String email);
    
    /**
     * Busca un profesor por su DNI
     * @param dni DNI del profesor
     * @return Optional<Profesor>
     */
    Optional<Profesor> findByDni(String dni);
    
    /**
     * Busca profesores por nombre (contiene, ignorando mayúsculas y acentos)
     * @param nombre Nombre a buscar
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE UPPER(p.nombre) LIKE UPPER(CONCAT('%', :nombre, '%')) ORDER BY p.id")
    List<Profesor> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca profesores por apellidos (contiene, ignorando mayúsculas y acentos)
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE UPPER(p.apellidos) LIKE UPPER(CONCAT('%', :apellidos, '%')) ORDER BY p.id")
    List<Profesor> findByApellidosContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    /**
     * Busca profesores que están habilitados
     * @return Lista de profesores habilitados
     */
    List<Profesor> findByEnabledTrue();
    
    /**
     * Obtiene todos los profesores ordenados por ID
     * @return Lista de profesores ordenada
     */
    @Query("SELECT p FROM Profesor p ORDER BY p.id")
    List<Profesor> findAllOrderedById();
    
    /**
     * Busca profesores que tienen una clase específica asignada
     * @param claseId ID de la clase
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE :claseId MEMBER OF p.clasesId")
    List<Profesor> findByClaseId(@Param("claseId") String claseId);
    
    /**
     * Cuenta el número de clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Número de clases
     */
    @Query("SELECT SIZE(p.clasesId) FROM Profesor p WHERE p.id = :profesorId")
    Integer countClasesByProfesorId(@Param("profesorId") Long profesorId);
    
    /**
     * Busca profesores que no tienen clases asignadas
     * @return Lista de profesores sin clases
     */
    @Query("SELECT p FROM Profesor p WHERE SIZE(p.clasesId) = 0")
    List<Profesor> findProfesoresSinClases();
    
    /**
     * Busca profesores por múltiples filtros con soporte para texto insensible a acentos y mayúsculas
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param email Filtro por email
     * @param usuario Filtro por usuario
     * @param dni Filtro por DNI
     * @param habilitado Filtro por estado habilitado
     * @param claseId Filtro por clase asignada
     * @param sinClases Filtro para profesores sin clases
     * @return Lista de profesores que cumplen los criterios
     */
    @Query("SELECT DISTINCT p FROM Profesor p " +
           "WHERE " +
           "(:nombre IS NULL OR UPPER(p.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))) AND " +
           "(:apellidos IS NULL OR UPPER(p.apellidos) LIKE UPPER(CONCAT('%', :apellidos, '%'))) AND " +
           "(:email IS NULL OR UPPER(p.email) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
           "(:usuario IS NULL OR UPPER(p.usuario) LIKE UPPER(CONCAT('%', :usuario, '%'))) AND " +
           "(:dni IS NULL OR UPPER(p.dni) LIKE UPPER(CONCAT('%', :dni, '%'))) AND " +
           "(:habilitado IS NULL OR p.enabled = :habilitado) AND " +
           "(:claseId IS NULL OR :claseId MEMBER OF p.clasesId) AND " +
           "(:sinClases IS NULL OR (:sinClases = true AND SIZE(p.clasesId) = 0) OR (:sinClases = false)) " +
           "ORDER BY p.id")
    List<Profesor> findByFiltros(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("email") String email,
        @Param("usuario") String usuario,
        @Param("dni") String dni,
        @Param("habilitado") Boolean habilitado,
        @Param("claseId") String claseId,
        @Param("sinClases") Boolean sinClases
    );
}
