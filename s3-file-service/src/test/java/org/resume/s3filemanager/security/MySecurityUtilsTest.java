package org.resume.s3filemanager.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySecurityUtilsTest {

    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String IPV6_FULL = "0:0:0:0:0:0:0:1";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // getCurrentUsername

    /**
     * Нет аутентификации — возвращается null.
     */
    @Test
    void getCurrentUsername_returnsNull_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThat(MySecurityUtils.getCurrentUsername()).isNull();
    }

    /**
     * Анонимный пользователь — возвращается null.
     */
    @Test
    void getCurrentUsername_returnsNull_whenAnonymousUser() {
        var auth = new UsernamePasswordAuthenticationToken(ANONYMOUS_USER, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(MySecurityUtils.getCurrentUsername()).isNull();
    }

    /**
     * Аутентифицированный пользователь — возвращается имя.
     */
    @Test
    void getCurrentUsername_returnsUsername_whenAuthenticated() {
        var auth = new UsernamePasswordAuthenticationToken("john", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(MySecurityUtils.getCurrentUsername()).isEqualTo("john");
    }

    /**
     * Principal не является строкой — возвращается null.
     */
    @Test
    void getCurrentUsername_returnsNull_whenPrincipalIsNotString() {
        var auth = new UsernamePasswordAuthenticationToken(new Object(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(MySecurityUtils.getCurrentUsername()).isNull();
    }

    // extractClientIp

    /**
     * Request равен null — возвращается null.
     */
    @Test
    void extractClientIp_returnsNull_whenRequestIsNull() {
        assertThat(MySecurityUtils.extractClientIp(null)).isNull();
    }

    /**
     * Заголовок X-Forwarded-For содержит цепочку — возвращается первый IP.
     */
    @Test
    void extractClientIp_returnsFirstIp_fromXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");

        assertThat(MySecurityUtils.extractClientIp(request)).isEqualTo("203.0.113.5");
    }

    /**
     * Заголовок X-Real-IP присутствует, X-Forwarded-For отсутствует — возвращается X-Real-IP.
     */
    @Test
    void extractClientIp_returnsXRealIp_whenNoXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.10");

        assertThat(MySecurityUtils.extractClientIp(request)).isEqualTo("203.0.113.10");
    }

    /**
     * RemoteAddr — полный IPv6 localhost — нормализуется в 127.0.0.1.
     */
    @Test
    void extractClientIp_normalizesIPv6Localhost_toIPv4() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(IPV6_FULL);

        assertThat(MySecurityUtils.extractClientIp(request)).isEqualTo("127.0.0.1");
    }

    /**
     * RemoteAddr — сокращённый IPv6 localhost — нормализуется в 127.0.0.1.
     */
    @Test
    void extractClientIp_normalizesShortIPv6Localhost_toIPv4() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("::1");

        assertThat(MySecurityUtils.extractClientIp(request)).isEqualTo("127.0.0.1");
    }

    /**
     * Нет заголовков — возвращается RemoteAddr.
     */
    @Test
    void extractClientIp_returnsRemoteAddr_whenNoHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        assertThat(MySecurityUtils.extractClientIp(request)).isEqualTo("192.168.1.100");
    }
}