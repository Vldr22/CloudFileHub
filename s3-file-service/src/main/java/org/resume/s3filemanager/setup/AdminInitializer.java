package org.resume.s3filemanager.setup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.properties.AdminProperties;
import org.resume.s3filemanager.service.auth.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Компонент для автоматической инициализации администратора при старте приложения.
 * <p>
 * Создает аккаунт администратора с учетными данными из конфигурации,
 * если пользователь с таким именем еще не существует.
 *
 * @see AdminProperties
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class AdminInitializer {

    private final AdminProperties adminProperties;
    private final UserService userService;

    /**
     * Создает администратора при инициализации приложения.
     * <p>
     * Вызывается автоматически после создания bean.
     * Проверяет существование пользователя перед созданием.
     */
    @PostConstruct
    public void createAdmin() {
        if (!userService.existsByUsername(adminProperties.getName())) {
            userService.createAdmin(adminProperties.getName(), adminProperties.getPassword());
        }
    }
}
