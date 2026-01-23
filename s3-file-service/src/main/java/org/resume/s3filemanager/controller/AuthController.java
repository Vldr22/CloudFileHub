package org.resume.s3filemanager.controller;

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
    @PostMapping("/logout")
    public CommonResponse<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return CommonResponse.success(SuccessMessages.LOGOUT_SUCCESS);
    }
}
