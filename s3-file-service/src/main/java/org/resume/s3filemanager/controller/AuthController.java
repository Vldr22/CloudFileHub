package org.resume.s3filemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.dto.AuthRequest;
import org.resume.s3filemanager.dto.CommonResponse;
import org.resume.s3filemanager.dto.LoginResponse;
import org.resume.s3filemanager.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для аутентификации и авторизации пользователей.
 * <p>
 * Обрабатывает регистрацию, вход и выход пользователей из системы.
 * JWT токены передаются через HTTP-only cookie.
 *
 * @see AuthService
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Регистрация, вход и выход из системы")
public class AuthController {

    private final AuthService authService;

    /**
     * Авторизует пользователя в системе.
     * <p>
     * Проверяет учетные данные, генерирует JWT токен и устанавливает его в cookie.
     *
     * @param request данные для входа (username, password)
     * @param response HTTP ответ для установки cookie с токеном
     * @return информация о пользователе и токен
     */
    @Operation(summary = "Войти в систему", description = "Проверяет данные и устанавливает JWT Token в cookie")
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);
        return CommonResponse.success(loginResponse);
    }

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Создает пользователя с ролью USER и статусом загрузки NOT_UPLOADED.
     *
     * @param request данные для регистрации (username, password)
     * @return сообщение об успешной регистрации
     */
    @Operation(summary = "Регистрация", description = "Создаёт нового пользователя с ролью USER")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> register(@Valid @RequestBody AuthRequest request) {
        authService.register(request);
        return CommonResponse.success(SuccessMessages.REGISTRATION_SUCCESS);
    }

    /**
     * Выполняет выход пользователя из системы.
     * <p>
     * Удаляет JWT токен из whitelist и очищает cookie.
     *
     * @param response HTTP ответ для очистки cookie
     * @return сообщение об успешном выходе
     */
    @Operation(summary = "Выйти из системы", description = "Удаляет токен из whitelist и очищает cookie")
    @PostMapping("/logout")
    public CommonResponse<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return CommonResponse.success(SuccessMessages.LOGOUT_SUCCESS);
    }
}
