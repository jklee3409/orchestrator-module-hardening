package eureca.capstone.project.orchestrator.auth.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

import static eureca.capstone.project.orchestrator.auth.constant.TokenConstant.accessTokenValidity;
import static eureca.capstone.project.orchestrator.auth.constant.TokenConstant.refreshTokenValidity;


@Component
public class JwtUtil {
    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, Set<String> roles, Set<String> authorities, Long userId) {
        return generateToken(email, roles, authorities, userId, accessTokenValidity);
    }

    public String generateRefreshToken(String email, Set<String> roles, Set<String> authorities, Long userId) {
        return generateToken(email, roles, authorities, userId, refreshTokenValidity);
    }

    private String generateToken(String email, Set<String> roles, Set<String> authorities, Long userId, long validity) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .claim("authorities", authorities)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Set<String> extractRoles(String token) {
        return Set.copyOf(extractClaim(token, claims -> claims.get("roles", Set.class)));
    }

    public Set<String> extractAuthorities(String token) {
        return Set.copyOf(extractClaim(token, claims -> claims.get("authorities", Set.class)));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isValidToken(String token) throws JwtException {
        extractAllClaims(token);
        return true;
    }

    public long getRemainingValidity(String token) {
        Date expiration = extractExpiration(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}