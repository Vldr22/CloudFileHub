package org.resume.s3filemanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.enums.UserStatus;

@Builder
@Schema(description = "Информация о пользователе")
public record UserDetailsResponse(
        @Schema(description = "ID пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя", example = "Sara")
        String username,

        @Schema(description = "Роль пользователя", example = "ROLE_USER")
        UserRole role,

        @Schema(description = "Статус аккаунта", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "Статус загрузки файлов", example = "NOT_UPLOADED")
        FileUploadStatus uploadStatus) {
}
