package ru.istokmw.testotp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final Long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.token.secret-key:#{null}}") Optional<String> optSecretKey,
            @Value("${security.jwt.token.expiration-milliseconds:3600000}") Long validityInMilliseconds) throws NoSuchAlgorithmException {
        if (optSecretKey.isPresent()) {
            byte[] keyBytes = optSecretKey.get().getBytes();
            this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
            log.info("Use secret key in env");
        } else {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);
            this.secretKey = keyGenerator.generateKey();
            log.info("Generate secret key: {}", this.secretKey);
        }
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String generateToken(String username, String roles, String ipAddress) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .claim("roles", roles)
                .claim("ip", ipAddress)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public Mono<Boolean> validateToken(String token, String currentIp) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String tokenIp = claims.get("ip", String.class);
            if (tokenIp != null
                //&& tokenIp.equals(currentIp)
            ) {
                return Mono.just(true);
            } else {
                log.warn("IP address mismatch: token IP = {}, current IP = {}", tokenIp, currentIp);
                return Mono.just(false);
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    public String getUsernameFromToken(String authToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authToken)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token", e);
        }
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(role -> role instanceof String)
                    .map(role -> new SimpleGrantedAuthority((String) role))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid roles format in token claims");
        }
    }
}
