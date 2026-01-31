package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.common.model.FileScanResult;
import org.resume.common.model.ScanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для обработки результатов антивирусного сканирования.
 * <p>
 * Обрабатывает различные статусы сканирования:
 * <ul>
 *   <li>CLEAN — обновляет статус в БД</li>
 *   <li>INFECTED — удаляет файл из S3 и обновляет статус в БД</li>
 *   <li>ERROR — обновляет статус в БД</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileScanResultService {

    private final FileMetadataService fileMetadataService;
    private final YandexStorageService yandexStorageService;

    /**
     * Обрабатывает результат сканирования файла.
     *
     * @param result результат сканирования от antivirus-service
     */
    @Transactional
    public void processScanResult(FileScanResult result) {
        String s3Key = result.getS3Key();
        ScanStatus status = result.getStatus();

        if (status == ScanStatus.INFECTED) {
            handleInfectedFile(result);
        } else {
            fileMetadataService.updateScanStatus(s3Key, status);
        }
    }

    /**
     * Обрабатывает заражённый файл: удаляет из S3 и обновляет статус.
     */
    private void handleInfectedFile(FileScanResult result) {
        String s3Key = result.getS3Key();

        log.warn("Infected file detected: s3Key={}, virus={}",
                s3Key, result.getVirusName());

        try {
            yandexStorageService.deleteFileYandexS3(s3Key);
            log.debug("Infected file deleted from S3: s3Key={}", s3Key);

            fileMetadataService.updateScanStatus(s3Key, ScanStatus.INFECTED);

        } catch (Exception e) {
            log.error("Failed to handle infected file: s3Key={}, error={}",
                    s3Key, e.getMessage());
            fileMetadataService.updateScanStatus(s3Key, ScanStatus.INFECTED);
        }
    }
}
