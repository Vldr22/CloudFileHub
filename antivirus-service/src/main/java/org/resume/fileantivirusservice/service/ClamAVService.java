package org.resume.fileantivirusservice.service;

import org.resume.common.model.FileScanResult;
import org.resume.common.model.ScanStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.fileantivirusservice.constant.ErrorMessages;
import org.resume.fileantivirusservice.exception.ClamAvScanException;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.io.InputStream;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClamAVService {

    private final ClamavClient clamavClient;

    public FileScanResult scanFile(String s3Key, InputStream inputStream) {
        try {
            ScanResult scanResult = clamavClient.scan(inputStream);

            if (scanResult instanceof ScanResult.OK) {
                return buildScanResult(s3Key, ScanStatus.CLEAN, null, null);

            } else if (scanResult instanceof ScanResult.VirusFound virusFound) {
                String virusName = virusFound.getFoundViruses().keySet().iterator().next();
                log.warn("Virus found: s3Key={}, virus={}", s3Key, virusName);
                return buildScanResult(s3Key, ScanStatus.INFECTED, virusName, null);

            }

            log.error("Unknown scan result for s3Key={}: {}", s3Key, scanResult.getClass());
            return buildScanResult(s3Key, ScanStatus.ERROR, null, ErrorMessages.CLAMAV_UNKNOWN_RESULT);

        } catch (Exception e) {
            log.error("ClamAV scan failed for s3Key={}", s3Key, e);
            throw new ClamAvScanException(ErrorMessages.CLAMAV_UNAVAILABLE, e);
        }
    }

    private FileScanResult buildScanResult(String s3Key, ScanStatus status,
                                           String virusName, String errorMessage) {
        return FileScanResult.builder()
                .s3Key(s3Key)
                .status(status)
                .virusName(virusName)
                .errorMessage(errorMessage)
                .scannedAt(Instant.now())
                .build();
    }

}
