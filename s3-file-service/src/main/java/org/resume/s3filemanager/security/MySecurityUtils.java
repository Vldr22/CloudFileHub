package org.resume.s3filemanager.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Утилитарный класс для работы с Spring Security контекстом.
 * <p>
 * Предоставляет удобные методы для получения информации о текущем
 * аутентифицированном пользователе и IP адресе клиента.
 */
@Slf4j
@UtilityClass
public class MySecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";

    /**
     * Получает объект аутентификации из SecurityContext.
     *
     * @return текущая аутентификация или null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Получает имя текущего аутентифицированного пользователя.
     *
     * @return имя пользователя или null для не аутентифицированных/анонимных пользователей
     */
    public static String getCurrentUsername() {
        Authentication auth = getCurrentAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (ANONYMOUS_USER.equals(principal)) {
            return null;
        }

        return principal instanceof String ? (String) principal : null;
    }

    /**
     * Извлекает IP адрес клиента из HTTP запроса.
     * <p>
     * Проверяет заголовки X-Forwarded-For и X-Real-IP перед использованием
     * RemoteAddr для корректного определения IP за прокси.
     *
     * @param request HTTP запрос
     * @return IP адрес клиента или "unknown"
     */
    public static String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        String remoteAddr = request.getRemoteAddr();

        // Localhost normalization для единообразия
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }

        return remoteAddr;
    }

    /**
     * Получает IP адрес клиента из текущего контекста запроса.
     * <p>
     * Проверяет заголовки X-Forwarded-For и X-Real-IP перед использованием
     * RemoteAddr для корректного определения IP за прокси.
     *
     * @return IP адрес клиента или "unknown"
     */
    public static String getClientIp() {
        return extractClientIp(getCurrentRequest());
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes != null ? attributes.getRequest() : null;
    }
}