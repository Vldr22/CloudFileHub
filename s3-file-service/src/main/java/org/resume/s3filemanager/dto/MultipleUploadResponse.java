package org.resume.s3filemanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.resume.s3filemanager.enums.CommonResponseStatus;

@Schema(description = "Результат загрузки одного файла из batch-запроса")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MultipleUploadResponse(
        @Schema(description = "Статус загрузки", example = "SUCCESS")
        CommonResponseStatus status,

        @Schema(description = "Оригинальное имя файла", example = "photo.png")
        String originalFileName,

        @Schema(description = "Уникальное имя файла в хранилище", example = "212d7ce9-8451-4c4d-881e-d198593aa518.png")
        String uniqueName,

        @Schema(description = "Сообщение об ошибке", example = "File already been uploaded")
        String message) {
}