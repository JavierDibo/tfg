package app.rest;

import app.dtos.DTORespuestaPaginada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Base controller that provides common functionality for all REST controllers
 * Implements the standard pagination and validation pattern
 */
@Validated
public abstract class BaseRestController {

    /**
     * Validates and creates standard pagination parameters
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (ASC/DESC)
     * @return Configured Pageable
     */
    protected Pageable createPageable(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            @Size(max = 50) String sortBy,
            @Pattern(regexp = "ASC|DESC") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates a standard paginated response from a Spring Data Page
     * 
     * @param page Spring Data Page
     * @param sortBy Field by which it is sorted
     * @param sortDirection Sort direction
     * @return Standard DTORespuestaPaginada
     */
    protected <T> DTORespuestaPaginada<T> createPaginatedResponse(
            Page<T> page, 
            String sortBy, 
            String sortDirection) {
        
        return DTORespuestaPaginada.fromPage(page, sortBy, sortDirection);
    }

    /**
     * Validates that the page size is within allowed limits
     * 
     * @param size Requested page size
     * @return Validated page size
     */
    protected int validatePageSize(int size) {
        if (size < 1) {
            return 20; // Default value
        }
        if (size > 100) {
            return 100; // Maximum limit
        }
        return size;
    }

    /**
     * Validates that the page number is valid
     * 
     * @param page Requested page number
     * @return Validated page number
     */
    protected int validatePageNumber(int page) {
        return Math.max(0, page); // Cannot be negative
    }

    /**
     * Validates that the sort field is valid
     * 
     * @param sortBy Requested sort field
     * @param allowedFields Allowed fields for sorting
     * @return Validated sort field
     */
    protected String validateSortBy(String sortBy, String... allowedFields) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id"; // Default field
        }
        
        // If allowed fields are specified, validate against them
        if (allowedFields.length > 0) {
            for (String allowedField : allowedFields) {
                if (allowedField.equalsIgnoreCase(sortBy)) {
                    return allowedField;
                }
            }
            return "id"; // Default field if not allowed
        }
        
        return sortBy;
    }

    /**
     * Validates that the sort direction is valid
     * 
     * @param sortDirection Requested sort direction
     * @return Validated sort direction
     */
    protected String validateSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return "ASC";
        }
        
        String upperDirection = sortDirection.toUpperCase();
        if ("ASC".equals(upperDirection) || "DESC".equals(upperDirection)) {
            return upperDirection;
        }
        
        return "ASC"; // Default value if not valid
    }
} 