package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.dto.CommonResponse;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.dto.MultipleUploadResponse;
import org.resume.s3filemanager.service.file.FileFacadeService;
import org.resume.s3filemanager.validation.ValidFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST контроллер для управления файлами.
 * <p>
 * Предоставляет API для загрузки, скачивания и удаления файлов.
 * Поддерживает одиночную загрузку (для всех пользователей) и
 * множественную загрузку (только для администраторов).
 *
 * @see FileFacadeService
 */
@Validated
@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileFacadeService fileFacadeService;

    /**
     * Загружает один файл (аутентифицированные пользователи).
     * <p>
     * Обычные пользователи могут загрузить только один файл (проверка статуса).
     * Администраторы могут загружать неограниченно.
     *
     * @param file загружаемый файл с валидацией типа
     * @return сообщение об успешной загрузке
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> upload (@RequestParam("file")
            @ValidFile MultipartFile file) {
        fileFacadeService.uploadFile(file);
        return CommonResponse.success(SuccessMessages.FILE_UPLOAD_SUCCESS);
    }

    /**
     * Загружает несколько файлов одновременно (только администраторы).
     * <p>
     * Реализует паттерн частичного успеха: валидные файлы загружаются,
     * невалидные отклоняются с указанием причины. Максимум 5 файлов за раз.
     *
     * @param files массив загружаемых файлов
     * @return список результатов для каждого файла (SUCCESS или ERROR)
     */
    @PostMapping("/multiple-upload")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<List<MultipleUploadResponse>> multipleUpload(@RequestParam("files")
                                        MultipartFile[] files) {
        List<MultipleUploadResponse> results = fileFacadeService.multipleUpload(files);
        return CommonResponse.success(results);
    }

    /**
     * Скачивает файл по уникальному имени.
     * <p>
     * Возвращает файл с оригинальным именем в заголовке Content-Disposition.
     *
     * @param uniqueName уникальное имя файла (UUID-based)
     * @return файл с корректными заголовками для скачивания
     */
    @GetMapping("/{uniqueName}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String uniqueName) {

        FileDownloadResponse response = fileFacadeService.downloadFile(uniqueName);
        ByteArrayResource resource = new ByteArrayResource(response.getContent());

        return ResponseEntity
                .ok()
                .contentLength(response.getSize())
                .header("Content-Type", response.getContentType())
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + response.getFileName())
                .body(resource);
    }

    /**
     * Удаляет файл по уникальному имени.
     * <p>
     * Пользователи могут удалять только свои файлы.
     * Администраторы могут удалять любые файлы.
     * При удалении сбрасывается статус загрузки владельца.
     *
     * @param uniqueName уникальное имя файла (UUID-based)
     */
    @DeleteMapping("/{uniqueName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uniqueName) {
        fileFacadeService.deleteFile(uniqueName);
    }
}
