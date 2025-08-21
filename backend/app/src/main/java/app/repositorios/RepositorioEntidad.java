package app.repositorios;

import app.entidades.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioEntidad extends JpaRepository<Entidad, Integer> {
    
    @Query(value = "SELECT * FROM entidad e WHERE normalize_text(e.info) LIKE '%' || normalize_text(:info) || '%' ORDER BY e.id", nativeQuery = true)
    List<Entidad> findByInfoContainingIgnoreCase(@Param("info") String info);
    
    @Query(value = "SELECT * FROM entidad e WHERE normalize_text(e.otra_info) LIKE '%' || normalize_text(:otraInfo) || '%' ORDER BY e.id", nativeQuery = true)
    List<Entidad> findByOtraInfoContainingIgnoreCase(@Param("otraInfo") String otraInfo);
    
    @Query(value = "SELECT * FROM entidad e WHERE " +
           "(:info IS NULL OR normalize_text(e.info) LIKE '%' || normalize_text(:info) || '%') AND " +
           "(:otraInfo IS NULL OR normalize_text(e.otra_info) LIKE '%' || normalize_text(:otraInfo) || '%') " +
           "ORDER BY e.id", nativeQuery = true)
    List<Entidad> findByInfoAndOtraInfoContainingIgnoreCase(
        @Param("info") String info, 
        @Param("otraInfo") String otraInfo
    );
    
    boolean existsByInfo(String info);
    
    @Query("SELECT e FROM Entidad e ORDER BY e.id")
    List<Entidad> findAllOrderedById();
}
