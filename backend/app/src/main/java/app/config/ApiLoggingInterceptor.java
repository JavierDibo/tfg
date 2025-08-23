package app.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.servicios.ServicioJwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private final ServicioJwt servicioJwt;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Configurable properties for response body logging
    @Value("${app.logging.response-body.enabled:true}")
    private boolean responseBodyLoggingEnabled;
    
    @Value("${app.logging.response-body.max-length:2000}")
    private int maxResponseBodyLength;
    
    @Value("${app.logging.response-body.include-size:true}")
    private boolean includeResponseSize;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate unique request ID for correlation
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());

        // Extract user info from JWT if available
        String userInfo = extractUserInfo(request);
        
        // Log request details
        log.info("[API-LOG] {} | {} {} | User: {} | RequestID: {}", 
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                request.getMethod(), 
                request.getRequestURI(),
                userInfo,
                requestId);

        // Log additional user context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                log.info("[API-LOG] User Context: {} | Enabled: {} | Authorities: {}", 
                    userDetails.getUsername(),
                    userDetails.isEnabled(),
                    userDetails.getAuthorities());
            }
        }

        // Log request body for all operations that might have a body
        String requestBody = getRequestBody(request);
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            log.info("[API-LOG] Request Body: {}", maskSensitiveData(requestBody));
        }

        // Log query parameters if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.trim().isEmpty()) {
            log.info("[API-LOG] Query Parameters: {}", queryString);
        }

        // Log important headers (excluding sensitive ones)
        String contentType = request.getHeader("Content-Type");
        String userAgent = request.getHeader("User-Agent");
        if (contentType != null) {
            log.info("[API-LOG] Content-Type: {}", contentType);
        }
        if (userAgent != null) {
            log.info("[API-LOG] User-Agent: {}", userAgent);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestId = (String) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute("startTime");
        
        if (requestId != null && startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response details
            log.info("[API-LOG] {} | {} {} | User: {} | Duration: {}ms | Status: {} | RequestID: {}", 
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                    request.getMethod(), 
                    request.getRequestURI(),
                    extractUserInfo(request),
                    duration,
                    response.getStatus(),
                    requestId);

            // Enhanced response body logging
            if (responseBodyLoggingEnabled) {
                logResponseBody(response, requestId);
            }

            // Log response headers
            String responseContentType = response.getContentType();
            if (responseContentType != null) {
                log.info("[API-LOG] Response Content-Type: {}", responseContentType);
            }

            // Log exception if present
            if (ex != null) {
                log.error("[API-LOG] Exception occurred: {}", ex.getMessage(), ex);
            }
        }
    }

    private String extractUserInfo(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                
                // Get the principal object to extract more user details
                Object principal = authentication.getPrincipal();
                String userInfo = authentication.getName();
                
                // Try to extract additional user details
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    // Extract role information
                    String roles = userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .findFirst()
                        .orElse("UNKNOWN");
                    
                    userInfo = String.format("%s (Role: %s)", userInfo, roles);
                }
                
                return userInfo;
            }
            
            // Try to extract from JWT header if not in security context
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String username = servicioJwt.extractUsername(jwt);
                if (username != null && !username.isEmpty()) {
                    return username + " (JWT)";
                }
            }
        } catch (Exception e) {
            // Silently handle JWT parsing errors
        }
        return "anonymous";
    }

    /**
     * Retrieves the request body from the given HttpServletRequest if it is wrapped
     * in a ContentCachingRequestWrapper. This is useful for logging or inspecting
     * the request payload after it has been read by the servlet. If the request body
     * is available, it returns it as a UTF-8 encoded string; otherwise, returns null.
     * If an error occurs while reading the body, logs the error at debug level.
     *
     * @param request the HttpServletRequest to extract the body from
     * @return the request body as a String, or null if not available
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.debug("Could not read request body: {}", e.getMessage());
        }
        return null;
    }

    private String getResponseBody(HttpServletResponse response) {
        try {
            log.debug("[API-LOG] Checking response type: {}", response.getClass().getName());
            
            // Try to get response body from different wrapper types
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                log.debug("[API-LOG] Content wrapper found, content length: {}", content.length);
                
                if (content.length > 0) {
                    String responseBody = new String(content, StandardCharsets.UTF_8);
                    log.debug("[API-LOG] Response body extracted successfully, length: {}", responseBody.length());
                    return responseBody;
                } else {
                    log.debug("[API-LOG] Response content is empty (0 bytes)");
                }
            } else if (response.getClass().getName().contains("LoggingResponseWrapper")) {
                // Try to access our custom wrapper using reflection
                try {
                    java.lang.reflect.Method getContentMethod = response.getClass().getMethod("getContentAsByteArray");
                    byte[] content = (byte[]) getContentMethod.invoke(response);
                    if (content != null && content.length > 0) {
                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        log.debug("[API-LOG] Response body extracted from LoggingResponseWrapper, length: {}", responseBody.length());
                        return responseBody;
                    }
                } catch (Exception e) {
                    log.debug("[API-LOG] Could not access LoggingResponseWrapper content: {}", e.getMessage());
                }
            } else {
                log.debug("[API-LOG] Response is not a recognized wrapper: {}", response.getClass().getName());
                
                // For Spring Security wrapped responses, we can't easily access the body
                // This is a limitation of the current approach
                log.debug("[API-LOG] Spring Security response wrapper detected - response body logging not available");
            }
        } catch (Exception e) {
            log.debug("[API-LOG] Could not read response body: {}", e.getMessage(), e);
        }
        return null;
    }

    private String maskSensitiveData(String content) {
        // Enhanced masking for sensitive fields
        return content.replaceAll("(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"token\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"secret\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"authorization\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                     .replaceAll("(\"jwt\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
    }

    /**
     * Enhanced response body logging with better error handling and formatting
     */
    private void logResponseBody(HttpServletResponse response, String requestId) {
        try {
            log.debug("[API-LOG] Attempting to log response body for request: {}", requestId);
            log.debug("[API-LOG] Response class: {}", response.getClass().getName());
            log.debug("[API-LOG] Response content type: {}", response.getContentType());
            log.debug("[API-LOG] Response status: {}", response.getStatus());
            
            String responseBody = getResponseBody(response);
            log.debug("[API-LOG] Response body retrieved for request {}: length={}, isNull={}", 
                requestId, 
                responseBody != null ? responseBody.length() : 0, 
                responseBody == null);
            
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                // Check if response is JSON and try to format it
                String contentType = response.getContentType();
                log.debug("[API-LOG] Response content type for request {}: {}", requestId, contentType);
                
                if (contentType != null && contentType.contains("application/json")) {
                    try {
                        // Pretty print JSON for better readability
                        Object jsonObject = objectMapper.readValue(responseBody, Object.class);
                        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
                        
                        if (prettyJson.length() > maxResponseBodyLength) {
                            log.info("[API-LOG] Response Body (truncated, {} chars):\n{}...", 
                                prettyJson.length(), 
                                prettyJson.substring(0, maxResponseBodyLength));
                        } else {
                            log.info("[API-LOG] Response Body ({} chars):\n{}", 
                                prettyJson.length(), 
                                prettyJson);
                        }
                    } catch (Exception jsonException) {
                        log.debug("[API-LOG] JSON parsing failed for request {}, logging as text: {}", 
                            requestId, jsonException.getMessage());
                        // If JSON parsing fails, log as plain text
                        logResponseBodyAsText(responseBody, "JSON parsing failed, logging as text");
                    }
                } else {
                    // Non-JSON response
                    logResponseBodyAsText(responseBody, "Non-JSON response");
                }
                
                // Log response size if enabled
                if (includeResponseSize) {
                    log.info("[API-LOG] Response Size: {} characters", responseBody.length());
                }
            } else {
                log.debug("[API-LOG] Response body is empty or null for request: {}", requestId);
                log.debug("[API-LOG] This might indicate the response wasn't properly wrapped or the body is empty");
            }
        } catch (Exception e) {
            log.warn("[API-LOG] Could not log response body for request {}: {}", requestId, e.getMessage());
            log.debug("[API-LOG] Exception details for request {}: ", requestId, e);
        }
    }

    /**
     * Log response body as plain text with truncation
     */
    private void logResponseBodyAsText(String responseBody, String context) {
        if (responseBody.length() > maxResponseBodyLength) {
            log.info("[API-LOG] Response Body ({} - truncated): {}...", 
                context, 
                responseBody.substring(0, maxResponseBodyLength));
        } else {
            log.info("[API-LOG] Response Body ({}): {}", context, responseBody);
        }
    }
} 