package app.servicios;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioJwt")
class ServicioJwtTest {

    @InjectMocks
    private ServicioJwt servicioJwt;

    private UserDetails userDetails;
    private String secretKey;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        // Configurar propiedades del servicio
        secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        jwtExpiration = 86400000L; // 24 horas en milisegundos

        ReflectionTestUtils.setField(servicioJwt, "secretKey", secretKey);
        ReflectionTestUtils.setField(servicioJwt, "jwtExpiration", jwtExpiration);

        // Crear UserDetails de prueba
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ALUMNO")
        );
        userDetails = new User("testuser", "password", authorities);
    }

    @Test
    @DisplayName("generateToken debe crear token válido")
    void testGenerateToken() {
        // Act
        String token = servicioJwt.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    @DisplayName("generateToken con claims extra debe incluir roles")
    void testGenerateTokenConClaimsExtra() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        // Act
        String token = servicioJwt.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);
        
        // Extraer claims y verificar que incluye los roles
        Claims claims = extractAllClaimsForTest(token);
        assertNotNull(claims.get("roles"));
        assertEquals("testuser", claims.getSubject());
        assertEquals("customValue", claims.get("customClaim"));
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ALUMNO"));
    }

    @Test
    @DisplayName("extractUsername debe extraer nombre de usuario correctamente")
    void testExtractUsername() {
        // Arrange
        String token = servicioJwt.generateToken(userDetails);

        // Act
        String username = servicioJwt.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("isTokenValid debe retornar true para token válido")
    void testIsTokenValidTokenValido() {
        // Arrange
        String token = servicioJwt.generateToken(userDetails);

        // Act
        boolean isValid = servicioJwt.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid debe retornar false para token con usuario diferente")
    void testIsTokenValidUsuarioDiferente() {
        // Arrange
        String token = servicioJwt.generateToken(userDetails);
        UserDetails otroUsuario = new User("otrousuario", "password", Arrays.asList());

        // Act
        boolean isValid = servicioJwt.isTokenValid(token, otroUsuario);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("extractClaim debe extraer claim específico")
    void testExtractClaim() {
        // Arrange
        String token = servicioJwt.generateToken(userDetails);

        // Act
        Date issuedAt = servicioJwt.extractClaim(token, Claims::getIssuedAt);
        Date expiration = servicioJwt.extractClaim(token, Claims::getExpiration);

        // Assert
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
    }

    @Test
    @DisplayName("token debe tener tiempo de expiración correcto")
    void testTokenExpiration() {
        // Arrange
        long tiempoAntes = System.currentTimeMillis();
        String token = servicioJwt.generateToken(userDetails);
        long tiempoDespues = System.currentTimeMillis();

        // Act
        Date expiration = servicioJwt.extractClaim(token, Claims::getExpiration);

        // Assert
        long tiempoExpiracionEsperado = tiempoAntes + jwtExpiration;
        long tiempoExpiracionEsperadoMax = tiempoDespues + jwtExpiration;
        
        assertTrue(expiration.getTime() >= tiempoExpiracionEsperado);
        assertTrue(expiration.getTime() <= tiempoExpiracionEsperadoMax);
    }

    @Test
    @DisplayName("extractUsername debe lanzar excepción para token malformado")
    void testExtractUsernameTokenMalformado() {
        // Arrange
        String tokenMalformado = "token.malformado.invalido";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            servicioJwt.extractUsername(tokenMalformado);
        });
    }

    @Test
    @DisplayName("isTokenValid debe lanzar excepción para token con firma inválida")
    void testIsTokenValidFirmaInvalida() {
        // Arrange
        String tokenConFirmaInvalida = createTokenWithInvalidSignature();

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            servicioJwt.isTokenValid(tokenConFirmaInvalida, userDetails);
        });
    }

    @Test
    @DisplayName("isTokenValid debe retornar false para token expirado")
    void testIsTokenValidTokenExpirado() {
        // Arrange
        // Crear un token con expiración muy corta para forzar que expire
        ReflectionTestUtils.setField(servicioJwt, "jwtExpiration", 1L); // 1 milisegundo
        String token = servicioJwt.generateToken(userDetails);
        
        // Esperar a que expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            servicioJwt.isTokenValid(token, userDetails);
        });
    }

    @Test
    @DisplayName("generateToken debe incluir fecha de emisión")
    void testGenerateTokenIncludeFechaEmision() {
        // Arrange
        long tiempoAntes = System.currentTimeMillis();
        
        // Act
        String token = servicioJwt.generateToken(userDetails);
        Date issuedAt = servicioJwt.extractClaim(token, Claims::getIssuedAt);
        
        long tiempoDespues = System.currentTimeMillis();

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() >= tiempoAntes);
        assertTrue(issuedAt.getTime() <= tiempoDespues);
    }

    @Test
    @DisplayName("generateToken debe crear tokens únicos")
    void testGenerateTokenUnicos() {
        // Act
        String token1 = servicioJwt.generateToken(userDetails);
        
        // Esperar un poco para asegurar diferentes timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = servicioJwt.generateToken(userDetails);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("generateToken debe manejar UserDetails sin authorities")
    void testGenerateTokenSinAuthorities() {
        // Arrange
        UserDetails userSinRoles = new User("usuario", "password", Collections.emptyList());

        // Act
        String token = servicioJwt.generateToken(userSinRoles);

        // Assert
        assertNotNull(token);
        String username = servicioJwt.extractUsername(token);
        assertEquals("usuario", username);
        
        Claims claims = extractAllClaimsForTest(token);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("extractClaim debe manejar diferentes tipos de claims")
    void testExtractClaimDiferentesTipos() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("stringClaim", "valor");
        extraClaims.put("numberClaim", 123);
        extraClaims.put("booleanClaim", true);
        
        String token = servicioJwt.generateToken(extraClaims, userDetails);

        // Act & Assert
        String stringValue = servicioJwt.extractClaim(token, claims -> claims.get("stringClaim", String.class));
        Integer numberValue = servicioJwt.extractClaim(token, claims -> claims.get("numberClaim", Integer.class));
        Boolean booleanValue = servicioJwt.extractClaim(token, claims -> claims.get("booleanClaim", Boolean.class));

        assertEquals("valor", stringValue);
        assertEquals(123, numberValue);
        assertTrue(booleanValue);
    }

    // Métodos auxiliares para testing

    private Claims extractAllClaimsForTest(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createTokenWithInvalidSignature() {
        // Crear un token con una clave diferente para que tenga firma inválida
        String differentKey = "differentSecretKey123456789012345678901234567890";
        byte[] keyBytes = Decoders.BASE64.decode(differentKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }
}

