package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.common.model.ScanStatus;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.dto.*;
import org.resume.s3filemanager.enums.CommonResponseStatus;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserStatus;
import org.resume.s3filemanager.service.admin.AdminAuditService;
import org.resume.s3filemanager.service.admin.AdminFileService;
import org.resume.s3filemanager.service.admin.AdminUserService;
import org.resume.s3filemanager.service.kafka.RetryDLTService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST контроллер для административных операций.
 * <p>
 * Предоставляет API для управления пользователями и просмотра журнала аудита.
 * Доступен только администраторам.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminAuditService adminAuditService;
    private final AdminFileService adminFileService;
    private final RetryDLTService retryDLTService;

    /**
     * Возвращает журнал аудита с фильтрацией и пагинацией.
     *
     * @param username имя пользователя (опционально)
     * @param operation тип операции (опционально)
     * @param status статус операции (опционально)
     * @param from начало диапазона (опционально)
     * @param to конец диапазона (опционально)
     * @param pageable параметры пагинации
     * @return страница с записями аудита
     */
    @GetMapping("/audit-logs")
    public Page<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AuditOperation operation,
            @RequestParam(required = false) CommonResponseStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {

        AuditLogFilterRequest filter = new AuditLogFilterRequest(username, operation, status, from, to);
        return adminAuditService.getAuditLogs(filter, pageable);
    }

    /**
     * Возвращает список всех пользователей с пагинацией.
     *
     * @param pageable параметры пагинации (page, size, sort)
     * @return страница с информацией о пользователях
     */
    @GetMapping("/users")
    public Page<UserDetailsResponse> getUsers(Pageable pageable) {
        return adminUserService.getUsers(pageable);
    }

    /**
     * Изменяет статус пользователя.
     * При блокировке автоматически инвалидирует токен.
     *
     * @param userId ID пользователя
     * @param status новый статус (ACTIVE, BLOCKED)
     * @return обновлённая информация о пользователе
     */
    @PutMapping("/users/{userId}/status")
    public CommonResponse<UserDetailsResponse> changeUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        UserDetailsResponse result = adminUserService.changeStatus(userId, status);
        return CommonResponse.success(result);
    }

    /**
     * Изменяет статус загрузки файлов пользователя.
     *
     * @param userId ID пользователя
     * @param uploadStatus новый статус (UNLIMITED, NOT_UPLOADED, FILE_UPLOADED)
     * @return обновлённая информация о пользователе
     */
    @PutMapping("/users/{userId}/file-upload-status")
    public CommonResponse<UserDetailsResponse> changeUserFileUploadStatus(
            @PathVariable Long userId,
            @RequestParam FileUploadStatus uploadStatus) {
        UserDetailsResponse result = adminUserService.changeFileUploadStatus(userId, uploadStatus);
        return CommonResponse.success(result);
    }

    /**
     * Удаляет пользователя из системы.
     * Автоматически инвалидирует токен.
     *
     * @param userId ID пользователя
     */
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
    }

    /**
     * Возвращает список файлов по статусу сканирования.
     *
     * @param scanStatus статус сканирования (PENDING_SCAN, CLEAN, INFECTED, ERROR)
     * @param pageable параметры пагинации
     * @return страница с информацией о файлах
     */
    @GetMapping("/files/scan-status")
    public Page<AdminFileResponse> getFilesByScanStatus(
            @RequestParam ScanStatus scanStatus,
            Pageable pageable) {
        return adminFileService.findAllByScanStatus(scanStatus, pageable);
    }

    /**
     * Повторно обрабатывает все сообщения из Dead Letter Topic.
     * Retry выполняется только для файлов со статусом ERROR.
     *
     * @return количество переотправленных сообщений
     */
    @PostMapping("/files/retry-dlt")
    public CommonResponse<Integer> retryDlt() {
        Integer result = retryDLTService.retryFailedEvents();
        return CommonResponse.success(result);
    }

    /**
     * Повторно отправляет файл на сканирование.
     * Доступно только для файлов со статусом ERROR.
     *
     * @param fileId ID файла
     * @return обновлённая информация о файле
     */
    @PostMapping("/files/{fileId}/retry-scan")
    public CommonResponse<AdminFileResponse> retryScan(@PathVariable Long fileId) {
        AdminFileResponse result = adminFileService.retryScan(fileId);
        return CommonResponse.success(result);
    }

    /**
     * Возвращает статистику файлов по статусам сканирования.
     *
     * @return количество файлов по каждому статусу
     */
    @GetMapping("/files/stats")
    public CommonResponse<FileStatsResponse> getFileStats() {
        FileStatsResponse stats = adminFileService.getFileStats();
        return CommonResponse.success(stats);
    }

    /**
     * Удаляет все файлы с указанным статусом сканирования.
     *
     * @param scanStatus статус для удаления (INFECTED, ERROR)
     * @return количество удалённых записей
     */
    @DeleteMapping("/files/scan-status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<Integer> deleteFilesByScanStatus(@RequestParam ScanStatus scanStatus) {
        int deleted = adminFileService.deleteAllByScanStatus(scanStatus);
        return CommonResponse.success(deleted);
    }
}
