package org.resume.s3filemanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.audit.ResourceType;
import org.resume.s3filemanager.enums.CommonResponseStatus;

import java.time.Instant;

@Builder
@Schema(description = "Запись аудит-лога")
public record AuditLogResponse(
        @Schema(description = "ID записи", example = "1")
        Long id,

        @Schema(description = "ID запроса", example = "550e8400-e29b-41d4-a716-446655440000")
        String requestId,

        @Schema(description = "Имя пользователя", example = "Sara")
        String username,

        @Schema(description = "IP адрес", example = "127.0.0.1")
        String ipAddress,

        @Schema(description = "Тип операции", example = "FILE_UPLOAD")
        AuditOperation operation,

        @Schema(description = "Тип ресурса", example = "FILE")
        ResourceType resourceType,

        @Schema(description = "ID ресурса", example = "212d7ce9-8451-4c4d-881e-d198593aa518.png")
        String resourceId,

        @Schema(description = "Статус операции", example = "SUCCESS")
        CommonResponseStatus status,

        @Schema(description = "Детали операции при Error", example = "Duplicate file. File already exists")
        String details,

        @Schema(description = "Время операции", example = "2026-03-11T03:41:17.639342344")
        Instant timestamp) {
}
