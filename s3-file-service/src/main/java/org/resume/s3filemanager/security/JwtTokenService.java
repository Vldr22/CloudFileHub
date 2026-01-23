package org.resume.s3filemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.Getter;
import org.resume.s3filemanager.constant.SecurityErrorMessages;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.properties.JwtTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Сервис для генерации и parsing'а JWT токенов.
 * <p>
 * Использует HMAC-SHA алгоритм с секретным ключом из конфигурации.
 * Токены содержат subject (имя пользователя) и роль в claims.
 *
 * @see JwtTokenProperties
 */
@Service
@EnableConfigurationProperties(JwtTokenProperties.class)
public class JwtTokenService {

    private final Key key;

    @Getter
    private final long expirationSeconds;

    public JwtTokenService(JwtTokenProperties jwtTokenConstants) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtTokenConstants.getSecretKey());
            if (keyBytes.length < 32) {
                throw new WeakKeyException(SecurityErrorMessages.WEAK_KEY_MESSAGE);
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
            this.expirationSeconds = jwtTokenConstants.getExpiration();
        } catch (Exception e) {
            throw new SecurityException(SecurityErrorMessages.INVALID_SECRET_KEY, e);
        }
    }

    /**
     * Генерирует JWT токен для пользователя с указанной ролью.
     * <p>
     * Токен содержит:
     * <ul>
     *   <li>Subject: имя пользователя</li>
     *   <li>Claim 'role': роль пользователя</li>
     *   <li>Expiration: время истечения токена</li>
     * </ul>
     *
     * @param subject имя пользователя (subject в токене)
     * @param userRole роль пользователя
     * @return подписанный JWT токен
     */
    public String generateToken(String subject, UserRole userRole) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .header()
                .type(SecurityConstants.TOKEN_TYPE)
                .and()
                .claim(SecurityConstants.CLAIM_ROLE, userRole.getAuthority())
                .subject(subject)
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Извлекает имя пользователя (subject) из JWT токена.
     *
     * @param token JWT токен для parsing'а
     * @return имя пользователя
     * @throws JwtException если токен невалидный или истек
     */
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Извлекает роль пользователя из claims токена.
     *
     * @param token JWT токен для parsing'а
     * @return роль пользователя в формате authority (например, "ROLE_USER")
     * @throws JwtException если токен невалидный или истек
     */
    public String extractRole(String token) {
        return extractClaims(token).get(SecurityConstants.CLAIM_ROLE, String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
