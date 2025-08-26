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
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md'")
    List<Material> findDocuments();
    
    /**
     * Finds materials that are documents (PDF, DOC, etc.) with pagination
     * @param pageable Pagination parameters
     * @return Page of document materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md'")
    Page<Material> findDocuments(Pageable pageable);
    
    /**
     * Finds materials that are images
     * @return List of image materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg'")
    List<Material> findImages();
    
    /**
     * Finds materials that are images with pagination
     * @param pageable Pagination parameters
     * @return Page of image materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg'")
    Page<Material> findImages(Pageable pageable);
    
    /**
     * Finds materials that are videos
     * @return List of video materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm'")
    List<Material> findVideos();
    
    /**
     * Finds materials that are videos with pagination
     * @param pageable Pagination parameters
     * @return Page of video materials
     */
    @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm'")
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
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md'")
    Long countDocuments();
    
    /**
     * Counts image materials
     * @return Number of image materials
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg'")
    Long countImages();
    
    /**
     * Counts video materials
     * @return Number of video materials
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm'")
    Long countVideos();
    
    /**
     * Búsqueda flexible de materiales con múltiples filtros opcionales
     * Permite combinar filtros de nombre, URL y tipo
     */
    @Query(value = "SELECT * FROM materiales m WHERE " +
            "(:searchTerm IS NULL OR (" +
            "normalize_text(m.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
            "normalize_text(m.url) LIKE '%' || normalize_text(:searchTerm) || '%')) AND " +
            "(:nombre IS NULL OR normalize_text(m.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
            "(:url IS NULL OR normalize_text(m.url) LIKE '%' || normalize_text(:url) || '%') AND " +
            "(:tipo IS NULL OR (" +
            "  CASE WHEN :tipo = 'DOCUMENT' THEN (LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md') " +
            "       WHEN :tipo = 'IMAGE' THEN (LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg') " +
            "       WHEN :tipo = 'VIDEO' THEN (LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm') " +
            "       ELSE TRUE END))",
            countQuery = "SELECT COUNT(*) FROM materiales m WHERE " +
            "(:searchTerm IS NULL OR (" +
            "normalize_text(m.nombre) LIKE '%' || normalize_text(:searchTerm) || '%' OR " +
            "normalize_text(m.url) LIKE '%' || normalize_text(:searchTerm) || '%')) AND " +
            "(:nombre IS NULL OR normalize_text(m.nombre) LIKE '%' || normalize_text(:nombre) || '%') AND " +
            "(:url IS NULL OR normalize_text(m.url) LIKE '%' || normalize_text(:url) || '%') AND " +
            "(:tipo IS NULL OR (" +
            "  CASE WHEN :tipo = 'DOCUMENT' THEN (LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md') " +
            "       WHEN :tipo = 'IMAGE' THEN (LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg') " +
            "       WHEN :tipo = 'VIDEO' THEN (LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm') " +
            "       ELSE TRUE END))",
            nativeQuery = true)
    Page<Material> findByFiltrosFlexibles(
        @Param("searchTerm") String searchTerm,
        @Param("nombre") String nombre,
        @Param("url") String url,
        @Param("tipo") String tipo,
        Pageable pageable
    );
}
