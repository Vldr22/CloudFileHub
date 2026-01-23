package org.resume.s3filemanager.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.enums.UserStatus;
import org.resume.s3filemanager.exception.UserAlreadyExistsException;
import org.resume.s3filemanager.exception.UserNotFoundException;
import org.resume.s3filemanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления пользователями системы.
 * <p>
 * Обрабатывает создание пользователей с различными ролями,
 * поиск по имени пользователя и обновление статусов загрузки файлов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Создает нового пользователя с ролью USER.
     * <p>
     * Новый пользователь получает статус загрузки NOT_UPLOADED,
     * что позволяет загрузить один файл.
     *
     * @param username имя пользователя (уникальное)
     * @param password пароль в открытом виде (будет зашифрован)
     * @throws UserAlreadyExistsException если пользователь с таким именем уже существует
     */
    public void createUser(String username, String password) {
        createUserWithRole(username, password, UserRole.USER, FileUploadStatus.NOT_UPLOADED);
    }

    /**
     * Создает нового администратора с ролью ADMIN.
     * <p>
     * Администратор получает статус загрузки UNLIMITED,
     * что позволяет загружать неограниченное количество файлов.
     *
     * @param username имя администратора (уникальное)
     * @param password пароль в открытом виде (будет зашифрован)
     * @throws UserAlreadyExistsException если пользователь с таким именем уже существует
     */
    public void createAdmin(String username, String password) {
        createUserWithRole(username, password, UserRole.ADMIN, FileUploadStatus.UNLIMITED);
        log.info("Admin created in DB successfully: {}", username);
    }

    /**
     * Находит пользователя по имени.
     *
     * @param username имя пользователя для поиска
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UserNotFoundException(username);
                });
    }

    /**
     * Проверяет существование пользователя по имени.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Обновляет статус загрузки файлов для пользователя.
     * <p>
     * Используется для изменения статуса с NOT_UPLOADED на FILE_UPLOADED
     * после загрузки файла, или обратно после удаления.
     *
     * @param user пользователь для обновления
     * @param status новый статус загрузки
     */
    @Transactional
    public void updateUploadStatus(User user, FileUploadStatus status) {
        user.setUploadStatus(status);
        userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public void updateStatus(User user, UserStatus status) {
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void updateStatus(User user, FileUploadStatus uploadStatus) {
        user.setUploadStatus(uploadStatus);
        userRepository.save(user);
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    private void createUserWithRole(String username, String password,
                                    UserRole role, FileUploadStatus uploadStatus) {
        if (existsByUsername(username)) {
            throw new UserAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setUploadStatus(uploadStatus);

        userRepository.save(user);
    }
}
