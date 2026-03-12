package org.resume.s3filemanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Статистика файлов по статусам антивирусной проверки")
public record FileStatsResponse(
        @Schema(description = "Файлы ожидающие проверки", example = "5")
        long pending,

        @Schema(description = "Чистые файлы", example = "100")
        long clean,

        @Schema(description = "Заражённые файлы", example = "2")
        long infected,

        @Schema(description = "Файлы с ошибкой проверки", example = "1")
        long error,

        @Schema(description = "Всего файлов", example = "108")
        long total
) {
}
