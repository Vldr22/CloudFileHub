package org.resume.s3filemanager.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Сервис для управления whitelist'ом JWT токенов в Redis.
 * <p>
 * Обеспечивает возможность валидации токенов при выходе пользователя,
 * храня активные токены в Redis с автоматическим истечением по TTL.
 *
 * @see RedissonClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtWhitelistService {

    private final RedissonClient redissonClient;

    /**
     * Сохраняет JWT токен в whitelist с указанным временем жизни.
     *
     * @param username имя пользователя (ключ в Redis)
     * @param token JWT токен для сохранения
     * @param ttlSeconds время жизни токена в секундах
     */
    public void saveToken(String username, String token, long ttlSeconds) {
        RBucket<String> bucket = getTokenBucket(username);
        bucket.set(token, Duration.ofSeconds(ttlSeconds));
        log.info("Token saved for user: {} with TTL: {} seconds", username, ttlSeconds);
    }

    /**
     * Проверяет валидность токена через whitelist.
     * <p>
     * Токен считается валидным, если он существует в Redis
     * и совпадает с сохраненным значением.
     *
     * @param username имя пользователя
     * @param token токен для проверки
     * @return true если токен валиден, false в противном случае
     */
    public boolean isValid(String username, String token) {
        String storedToken = getToken(username);
        boolean isValid = token != null && token.equals(storedToken);

        if (!isValid) {
            log.warn("Token validation failed for user: {}", username);
        }

        return isValid;
    }

    public void deleteToken(String username) {
        RBucket<String> bucket = getTokenBucket(username);
        bucket.delete();
        log.info("Token deleted for user: {}", username);
    }

    private String getToken(String username) {
        RBucket<String> bucket = getTokenBucket(username);
        return bucket.get();
    }

    private RBucket<String> getTokenBucket(String username) {
        String key = SecurityConstants.JWT_PREFIX + username;
        return redissonClient.getBucket(key);
    }
}
