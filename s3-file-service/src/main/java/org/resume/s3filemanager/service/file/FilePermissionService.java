package org.resume.s3filemanager.service.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.exception.FileAccessDeniedException;
import org.resume.s3filemanager.exception.FileUploadLimitException;
import org.resume.s3filemanager.exception.UserNotFoundException;
import org.resume.s3filemanager.repository.UserRepository;
import org.resume.s3filemanager.security.MySecurityUtils;
import org.springframework.stereotype.Service;

/**
 * Сервис для проверки прав доступа к операциям с файлами.
 * <p>
 * Управляет разрешениями на загрузку и удаление файлов в зависимости от
 * роли пользователя и его статуса загрузки.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilePermissionService {

    private final UserRepository userRepository;

    /**
     * Проверяет права пользователя на загрузку файла.
     * <p>
     * Пользователи со статусом UNLIMITED могут загружать неограниченно.
     * Обычные пользователи могут загрузить только один файл.
     *
     * @return текущий пользователь с подтвержденными правами
     * @throws FileUploadLimitException если пользователь уже загрузил файл
     * @throws UserNotFoundException если текущий пользователь не найден
     */
    public User checkUploadPermission() {
        User user = getCurrentUser();

        if (user.getUploadStatus() == FileUploadStatus.UNLIMITED) {
            return user;
        }

        if (user.getUploadStatus() == FileUploadStatus.FILE_UPLOADED) {
            log.warn("Upload denied: user {} already uploaded file", user.getUsername());
            throw new FileUploadLimitException();
        }

        log.debug("Upload permission granted for user: {}", user.getUsername());
        return user;
    }

    /**
     * Проверяет права пользователя на удаление файла.
     * <p>
     * Обычные пользователи могут удалять только свои файлы.
     * Администраторы могут удалять любые файлы.
     *
     * @param currentUser текущий пользователь
     * @param file метаданные файла для проверки владения
     * @throws FileAccessDeniedException если пользователь пытается удалить чужой файл
     */
    public void checkDeletePermission(User currentUser, FileMetadata file) {
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = file.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new FileAccessDeniedException();
        }
    }


    /**
     * Помечает файл как загруженный для текущего пользователя.
     * <p>
     * Обновляет статус пользователя с NOT_UPLOADED на FILE_UPLOADED.
     * Пользователи со статусом UNLIMITED не изменяются.
     */
    @Transactional
    public void markFileUploaded() {
        User user = getCurrentUser();

        if (user.getUploadStatus() == FileUploadStatus.NOT_UPLOADED) {
            user.setUploadStatus(FileUploadStatus.FILE_UPLOADED);
            userRepository.save(user);
        }
    }

    /**
     * Получает текущего аутентифицированного пользователя из контекста безопасности.
     *
     * @return текущий пользователь
     * @throws UserNotFoundException если пользователь не найден в базе данных
     */
    public User getCurrentUser() {
        String username = MySecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Current user not found: {}", username);
                    return new UserNotFoundException(username);
                });
    }
}
