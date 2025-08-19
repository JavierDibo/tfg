package app.repositorios;

import app.entidades.Entidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioEntidad extends JpaRepository<Entidad, Integer> {
    
    @Query("SELECT e FROM Entidad e WHERE e.info LIKE %:info% ORDER BY e.id")
    List<Entidad> findByInfoContainingIgnoreCase(@Param("info") String info);
    
    @Query("SELECT e FROM Entidad e WHERE e.otraInfo LIKE %:otraInfo% ORDER BY e.id")
    List<Entidad> findByOtraInfoContainingIgnoreCase(@Param("otraInfo") String otraInfo);
    
    @Query("SELECT e FROM Entidad e WHERE " +
           "(:info IS NULL OR e.info LIKE %:info%) AND " +
           "(:otraInfo IS NULL OR e.otraInfo LIKE %:otraInfo%) " +
           "ORDER BY e.id")
    List<Entidad> findByInfoAndOtraInfoContainingIgnoreCase(
        @Param("info") String info, 
        @Param("otraInfo") String otraInfo
    );
    
    boolean existsByInfo(String info);
    
    @Query("SELECT e FROM Entidad e ORDER BY e.id")
    List<Entidad> findAllOrderedById();
}
