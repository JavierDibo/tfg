package app.repositorios;

import app.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Administrador
 * Proporciona operaciones de acceso a datos para administradores
 */
@Repository
public interface RepositorioAdministrador extends JpaRepository<Administrador, Long> {
    
    /**
     * Busca un administrador por su nombre de usuario
     * @param usuario Nombre de usuario
     * @return Optional<Administrador>
     */
    Optional<Administrador> findByUsuario(String usuario);
    
    /**
     * Busca un administrador por su email
     * @param email Email del administrador
     * @return Optional<Administrador>
     */
    Optional<Administrador> findByEmail(String email);
    
    /**
     * Busca un administrador por su DNI
     * @param dni DNI del administrador
     * @return Optional<Administrador>
     */
    Optional<Administrador> findByDni(String dni);
    
    /**
     * Busca administradores por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de administradores
     */
    List<Administrador> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca administradores por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de administradores
     */
    List<Administrador> findByApellidosContainingIgnoreCase(String apellidos);
    
    /**
     * Busca administradores que están habilitados
     * @return Lista de administradores habilitados
     */
    List<Administrador> findByEnabledTrue();
    
    /**
     * Obtiene todos los administradores ordenados por ID
     * @return Lista de administradores ordenada
     */
    @Query("SELECT a FROM Administrador a ORDER BY a.id")
    List<Administrador> findAllOrderedById();
    
    /**
     * Obtiene todos los administradores ordenados por nombre
     * @return Lista de administradores ordenada por nombre
     */
    @Query("SELECT a FROM Administrador a ORDER BY a.firstName ASC, a.lastName ASC")
    List<Administrador> findAllOrderedByNombre();
    
    /**
     * Verifica si existe un administrador con el usuario dado
     * @param usuario Nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsuario(String usuario);
    
    /**
     * Verifica si existe un administrador con el email dado
     * @param email Email
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un administrador con el DNI dado
     * @param dni DNI
     * @return true si existe, false en caso contrario
     */
    boolean existsByDni(String dni);
}
