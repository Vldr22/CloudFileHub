package org.resume.s3filemanager.service.admin;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.dto.AuditLogFilterRequest;
import org.resume.s3filemanager.dto.AuditLogResponse;
import org.resume.s3filemanager.dto.UserDetailsResponse;
import org.resume.s3filemanager.entity.AuditLog;
import org.resume.s3filemanager.entity.QAuditLog;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserStatus;
import org.resume.s3filemanager.exception.UserNotFoundException;
import org.resume.s3filemanager.repository.AuditLogRepository;
import org.resume.s3filemanager.security.JwtWhitelistService;
import org.resume.s3filemanager.service.auth.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Сервис для административных операций.
 * <p>
 * Предоставляет функционал для работы с журналом аудита,
 * включая фильтрацию и пагинацию записей.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final JwtWhitelistService jwtWhitelistService;

    /**
     * Возвращает отфильтрованный список записей аудит-логов. Использует пагинацию.
     * <p>
     * Поддерживает динамическую фильтрацию через Querydsl.
     * Все параметры фильтра опциональны — если не указаны, возвращаются все записи.
     *
     * @param filter   параметры фильтрации (username, operation, from, to)
     * @param pageable параметры пагинации и сортировки
     * @return страница с записями аудита
     */
    public Page<AuditLogResponse> getAuditLogs(AuditLogFilterRequest filter, Pageable pageable) {
        QAuditLog qAuditLog = QAuditLog.auditLog;
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.username() != null) {
            builder.and(qAuditLog.username.eq(filter.username()));
        }
        if (filter.operation() != null) {
            builder.and(qAuditLog.operation.eq(filter.operation()));
        }
        if (filter.status() != null) {
            builder.and(qAuditLog.status.eq(filter.status()));
        }
        if (filter.from() != null) {
            builder.and(qAuditLog.timestamp.goe(filter.from()));
        }
        if (filter.to() != null) {
            builder.and(qAuditLog.timestamp.loe(filter.to()));
        }

        return auditLogRepository.findAll(builder, pageable)
                .map(this::toAuditLogResponse);
    }

    /**
     * Возвращает список всех пользователей с пагинацией.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница с информацией о пользователях
     */
    public Page<UserDetailsResponse> getUsers(Pageable pageable) {
        return userService.findAll(pageable)
                .map(this::toUserResponse);
    }

    /**
     * Изменяет статус пользователя.
     * <p>
     * При блокировке автоматически инвалидирует JWT токен,
     * что приводит к немедленному завершению сессии пользователя.
     *
     * @param userId ID пользователя
     * @param status новый статус (ACTIVE, BLOCKED)
     * @return обновлённая информация о пользователе
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDetailsResponse changeUserStatus(Long userId, UserStatus status) {
        User user = userService.findById(userId);
        userService.updateStatus(user, status);

        if (status == UserStatus.BLOCKED) {
           jwtWhitelistService.deleteToken(user.getUsername());
        }

        return toUserResponse(user);
    }

    /**
     * Изменяет статус загрузки файла (FileUploadStatus) пользователя.
     * <p>
     *
     * @param userId ID пользователя
     * @param uploadStatus новый статус (UNLIMITED, NOT_UPLOADED, FILE_UPLOADED,)
     * @return обновлённая информация о пользователе
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDetailsResponse changeUserFileUploadStatus(Long userId, FileUploadStatus uploadStatus) {
        User user = userService.findById(userId);
        userService.updateStatus(user, uploadStatus);
        return toUserResponse(user);
    }

    /**
     * Удаляет пользователя из системы.
     * <p>
     * JWT токен перед удалением пользователя также удаляется.
     *
     * @param userId ID пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public void deleteUser(Long userId) {
        User user = userService.findById(userId);
        jwtWhitelistService.deleteToken(user.getUsername());
        userService.delete(user);
    }

    private UserDetailsResponse toUserResponse(User user) {
        return UserDetailsResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .uploadStatus(user.getUploadStatus())
                .build();
    }

    private AuditLogResponse toAuditLogResponse(AuditLog entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getRequestId(),
                entity.getUsername(),
                entity.getIpAddress() != null ? entity.getIpAddress().getAddress() : null,
                entity.getOperation(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getStatus(),
                entity.getDetails(),
                entity.getTimestamp()
        );
    }

}
