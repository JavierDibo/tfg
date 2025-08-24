package app.dtos;

import app.entidades.Material;

/**
 * DTO for the Material entity
 * Contains material information without complex relationships
 */
public record DTOMaterial(
        String id,
        String name,
        String url
) {
    
    /**
     * Constructor that creates a DTO from a Material entity
     */
    public DTOMaterial(Material material) {
        this(
                material.getId(),
                material.getName(),
                material.getUrl()
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOMaterial from(Material material) {
        return new DTOMaterial(material);
    }
    
    /**
     * Verifies if the material has a valid URL
     */
    public boolean hasValidUrl() {
        return this.url != null && !this.url.trim().isEmpty();
    }
    
    /**
     * Verifies if the material has a valid name
     */
    public boolean hasValidName() {
        return this.name != null && !this.name.trim().isEmpty();
    }
    
    /**
     * Gets the file extension from the URL
     */
    public String getFileExtension() {
        if (!hasValidUrl()) {
            return "";
        }
        
        int lastDotIndex = this.url.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < this.url.length() - 1) {
            return this.url.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Verifies if the material is a document (PDF, DOC, etc.)
     */
    public boolean isDocument() {
        String extension = getFileExtension();
        return extension.matches("pdf|doc|docx|txt|rtf|md");
    }
    
    /**
     * Verifies if the material is an image
     */
    public boolean isImage() {
        String extension = getFileExtension();
        return extension.matches("jpg|jpeg|png|gif|bmp|svg");
    }
    
    /**
     * Verifies if the material is a video
     */
    public boolean isVideo() {
        String extension = getFileExtension();
        return extension.matches("mp4|avi|mov|wmv|flv|webm");
    }
    
    /**
     * Gets the material type based on file extension
     */
    public String getMaterialType() {
        if (isDocument()) {
            return "DOCUMENT";
        } else if (isImage()) {
            return "IMAGE";
        } else if (isVideo()) {
            return "VIDEO";
        } else {
            return "OTHER";
        }
    }
}
