package org.resume.s3filemanager.service.file;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.resume.common.model.FileScanResult;
import org.resume.common.model.ScanStatus;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileScanResultService — обработка результатов антивирусного сканирования")
class FileScanResultServiceTest {

    private static final Faker FAKER = new Faker();

    @Mock
    private FileMetadataService fileMetadataService;

    @Mock
    private YandexStorageService yandexStorageService;

    @InjectMocks
    private FileScanResultService fileScanResultService;

    private FileScanResult scanResult;
    private String s3Key;

    @BeforeEach
    void setUp() {
        s3Key = FAKER.internet().uuid() + ".pdf";
        scanResult = new FileScanResult();
        scanResult.setS3Key(s3Key);
    }

    /**
     * Файл чистый — статус обновляется, S3 не трогаем.
     */
    @Test
    void shouldUpdateStatus_whenFileIsClean() {
        scanResult.setStatus(ScanStatus.CLEAN);

        fileScanResultService.processScanResult(scanResult);

        verify(fileMetadataService).updateScanStatus(s3Key, ScanStatus.CLEAN);
        verifyNoInteractions(yandexStorageService);
    }

    /**
     * Ошибка сканирования — статус обновляется, S3 не трогаем.
     */
    @Test
    void shouldUpdateStatus_whenScanResultIsError() {
        scanResult.setStatus(ScanStatus.ERROR);

        fileScanResultService.processScanResult(scanResult);

        verify(fileMetadataService).updateScanStatus(s3Key, ScanStatus.ERROR);
        verifyNoInteractions(yandexStorageService);
    }

    /**
     * Заражённый файл — удаляется из S3, статус обновляется на INFECTED.
     */
    @Test
    void shouldDeleteFromS3AndUpdateStatus_whenFileIsInfected() {
        scanResult.setStatus(ScanStatus.INFECTED);
        scanResult.setVirusName(FAKER.lorem().word());

        fileScanResultService.processScanResult(scanResult);

        verify(yandexStorageService).deleteFileYandexS3(s3Key);
        verify(fileMetadataService).updateScanStatus(s3Key, ScanStatus.INFECTED);
    }

    /**
     * Заражённый файл, S3 падает — статус всё равно обновляется на INFECTED.
     */
    @Test
    void shouldUpdateStatus_whenInfectedFileFailsToDeleteFromS3() {
        scanResult.setStatus(ScanStatus.INFECTED);
        scanResult.setVirusName(FAKER.lorem().word());

        doThrow(new RuntimeException()).when(yandexStorageService).deleteFileYandexS3(s3Key);

        fileScanResultService.processScanResult(scanResult);

        verify(fileMetadataService).updateScanStatus(s3Key, ScanStatus.INFECTED);
    }
}
