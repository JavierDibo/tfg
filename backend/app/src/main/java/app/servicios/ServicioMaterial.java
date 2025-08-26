package app.servicios;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Material;
import app.repositorios.RepositorioMaterial;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioMaterial {

    private final RepositorioMaterial repositorioMaterial;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DTOMaterial obtenerMaterialPorId(String id) {
        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return new DTOMaterial(material);
    }

    @Transactional(readOnly = true)
    public DTOMaterial obtenerMaterialPorNombre(String name) {
        Material material = repositorioMaterial.findByName(name).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "name", name);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return new DTOMaterial(material);
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerTodosLosMateriales() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findAll().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerMaterialesPorNombre(String name) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findByNameContainingIgnoreCase(name).stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerMaterialesPorUrl(String url) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findByUrlContainingIgnoreCase(url).stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerMaterialesPorExtension(String extension) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findByFileExtension(extension).stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerDocumentos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findDocuments().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerImagenes() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findImages().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerVideos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
        
        return repositorioMaterial.findVideos().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    public DTOMaterial crearMaterial(String name, String url) {
        // Security check: Only ADMIN and PROFESOR can create materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear materiales");
        }
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del material no puede estar vacío");
        }
        
        if (url == null || url.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("La URL del material no puede estar vacía");
        }

        // Check for duplicates
        if (repositorioMaterial.findByName(name).isPresent()) {
            ExceptionUtils.throwValidationError("Ya existe un material con el nombre: " + name);
        }

        // Create material with generated ID
        Material material = new Material(
            UUID.randomUUID().toString(),
            name.trim(),
            url.trim()
        );

        Material materialGuardado = repositorioMaterial.save(material);
        return new DTOMaterial(materialGuardado);
    }

    public DTOMaterial actualizarMaterial(String id, String name, String url) {
        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);

        // Security check: Only ADMIN and PROFESOR can update materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para actualizar materiales");
        }

        // Validate input
        if (name == null || name.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del material no puede estar vacío");
        }
        
        if (url == null || url.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("La URL del material no puede estar vacía");
        }

        // Check for duplicates (excluding current material)
        repositorioMaterial.findByName(name).ifPresent(existingMaterial -> {
            if (!existingMaterial.getId().equals(id)) {
                ExceptionUtils.throwValidationError("Ya existe un material con el nombre: " + name);
            }
        });

        // Update material
        material.setName(name.trim());
        material.setUrl(url.trim());

        Material materialActualizado = repositorioMaterial.save(material);
        return new DTOMaterial(materialActualizado);
    }

    public boolean borrarMaterialPorId(String id) {
        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);

        repositorioMaterial.delete(material);
        return true;
    }

    public boolean borrarMaterialPorNombre(String name) {
        Material material = repositorioMaterial.findByName(name).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "name", name);

        repositorioMaterial.delete(material);
        return true;
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOMaterial> obtenerMaterialesPaginados(
            String q,
            String name,
            String url,
            String type,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access materials
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }

        // Validate pagination parameters
        if (page < 0) {
            ExceptionUtils.throwValidationError("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            ExceptionUtils.throwValidationError("El tamaño de página debe estar entre 1 y 100");
        }

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build query based on filters using flexible approach
        Page<Material> materialPage;
        
        // Prepare filter parameters
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String urlFilter = (url != null && !url.trim().isEmpty()) ? url.trim() : null;
        String tipoFilter = (type != null && !type.trim().isEmpty()) ? type.toUpperCase() : null;
        
        // Use flexible query that handles all combinations
        materialPage = repositorioMaterial.findByFiltrosFlexibles(
            searchTerm, nombreFilter, urlFilter, tipoFilter, pageable);

        // Convert to DTOs
        List<DTOMaterial> materiales = materialPage.getContent().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<>(
            materiales,
            materialPage.getNumber(),
            materialPage.getSize(),
            materialPage.getTotalElements(),
            materialPage.getTotalPages(),
            materialPage.isFirst(),
            materialPage.isLast(),
            materialPage.hasContent(),
            sortBy,
            sortDirection
        );
    }

    @Transactional(readOnly = true)
    public long contarMateriales() {
        return repositorioMaterial.countAllMaterials();
    }

    @Transactional(readOnly = true)
    public long contarDocumentos() {
        return repositorioMaterial.countDocuments();
    }

    @Transactional(readOnly = true)
    public long contarImagenes() {
        return repositorioMaterial.countImages();
    }

    @Transactional(readOnly = true)
    public long contarVideos() {
        return repositorioMaterial.countVideos();
    }
}
