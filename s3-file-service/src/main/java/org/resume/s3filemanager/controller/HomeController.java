package org.resume.s3filemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.dto.FileResponse;
import org.resume.s3filemanager.service.file.FilePaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для публичного доступа к списку файлов.
 * <p>
 * Предоставляет постраничный список всех файлов в системе
 * без требования аутентификации.
 *
 * @see FilePaginationService
 */
@RestController
@RequestMapping("api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Домашняя страница. Публичный доступ к списку файлов")
public class HomeController {

    private final FilePaginationService filePaginationService;

    /**
     * Возвращает постраничный список файлов.
     * <p>
     * Доступен без аутентификации. Параметры пагинации
     * передаются через query параметры (page, size, sort).
     *
     * @param pageable параметры пагинации (по умолчанию: page=0, size из конфигурации)
     * @return страница с метаданными файлов
     */
    @Operation(summary = "Список файлов", description = "Постраничный список всех файлов без аутентификации")
    @GetMapping()
    public Page<FileResponse> getFiles(Pageable pageable) {
        return filePaginationService.paginate(pageable);
    }
}
