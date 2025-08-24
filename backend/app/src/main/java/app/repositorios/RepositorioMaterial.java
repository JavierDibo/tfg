package app.repositorios;

import app.entidades.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the Material entity
 * Provides data access operations for materials
 */
@Repository
public interface RepositorioMaterial extends JpaRepository<Material, String> {
    
    /**
     * Finds a material by its name
     * @param name Material name
     * @return Optional<Material>
     */
    Optional<Material> findByName(String name);
    
    /**
     * Finds materials by name (contains, case insensitive)
     * @param name Name to search
     * @return List of materials
     */
    List<Material> findByNameContainingIgnoreCase(String name);
    
    /**
     * Finds materials by name (contains, case insensitive) with pagination
     * @param name Name to search
     * @param pageable Pagination parameters
     * @return Page of materials
     */
    Page<Material> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Finds materials by URL (contains, case insensitive)
     * @param url URL to search
     * @return List of materials
     */
    List<Material> findByUrlContainingIgnoreCase(String url);
    
    /**
     * Finds materials by URL (contains, case insensitive) with pagination
     * @param url URL to search
     * @param pageable Pagination parameters
     * @return Page of materials
     */
    Page<Material> findByUrlContainingIgnoreCase(String url, Pageable pageable);
    
    /**
     * Finds materials by name or URL (contains, case insensitive) with pagination
     * @param name Name to search
     * @param url URL to search
     * @param pageable Pagination parameters
     * @return Page of materials
     */
    Page<Material> findByNameContainingIgnoreCaseOrUrlContainingIgnoreCase(String name, String url, Pageable pageable);
    
    /**
     * Finds materials by file extension
     * @param extension File extension to search
     * @return List of materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%.', :extension))")
    List<Material> findByFileExtension(@Param("extension") String extension);
    
    /**
     * Finds materials that are documents (PDF, DOC, etc.)
     * @return List of document materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(pdf|doc|docx|txt|rtf|md)$'")
    List<Material> findDocuments();
    
    /**
     * Finds materials that are documents (PDF, DOC, etc.) with pagination
     * @param pageable Pagination parameters
     * @return Page of document materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(pdf|doc|docx|txt|rtf|md)$'")
    Page<Material> findDocuments(Pageable pageable);
    
    /**
     * Finds materials that are images
     * @return List of image materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(jpg|jpeg|png|gif|bmp|svg)$'")
    List<Material> findImages();
    
    /**
     * Finds materials that are images with pagination
     * @param pageable Pagination parameters
     * @return Page of image materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(jpg|jpeg|png|gif|bmp|svg)$'")
    Page<Material> findImages(Pageable pageable);
    
    /**
     * Finds materials that are videos
     * @return List of video materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(mp4|avi|mov|wmv|flv|webm)$'")
    List<Material> findVideos();
    
    /**
     * Finds materials that are videos with pagination
     * @param pageable Pagination parameters
     * @return Page of video materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(mp4|avi|mov|wmv|flv|webm)$'")
    Page<Material> findVideos(Pageable pageable);
    
    /**
     * Counts materials by type
     * @return Number of materials
     */
    @Query("SELECT COUNT(m) FROM Material m")
    Long countAllMaterials();
    
    /**
     * Counts document materials
     * @return Number of document materials
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(pdf|doc|docx|txt|rtf|md)$'")
    Long countDocuments();
    
    /**
     * Counts image materials
     * @return Number of image materials
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(jpg|jpeg|png|gif|bmp|svg)$'")
    Long countImages();
    
    /**
     * Counts video materials
     * @return Number of video materials
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) REGEXP '.*\\.(mp4|avi|mov|wmv|flv|webm)$'")
    Long countVideos();
}
