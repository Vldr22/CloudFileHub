package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.resume.s3filemanager.exception.DuplicateFileException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для вычисления хеша файлов и обнаружения дубликатов.
 * <p>
 * Использует MD5 хеширование для идентификации дубликатов файлов
 * в рамках каждого пользователя отдельно.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileHashService {

    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Вычисляет MD5 хеш содержимого файла.
     *
     * @param fileBytes содержимое файла в виде массива байт
     * @return MD5 хеш в виде шестнадцатеричной строки
     */
    public String calculateMD5(byte[] fileBytes) {
        return DigestUtils.md5DigestAsHex(fileBytes);
    }

    /**
     * Проверяет наличие файла с заданным хешем у пользователя.
     *
     * @param fileHash MD5 хеш файла
     * @param userId идентификатор пользователя для проверки дубликатов
     * @throws DuplicateFileException если дубликат найден у данного пользователя
     */
    public void checkDuplicateInDatabase(String fileHash, Long userId) {
        if (fileMetadataRepository.existsByFileHashAndUserId(fileHash, userId)) {
            log.warn("Duplicate file detected with hash: {} for user: {}", fileHash, userId);
            throw new DuplicateFileException();
        }
    }
}
