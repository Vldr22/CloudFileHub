package org.resume.fileantivirusservice.dto;

import lombok.Builder;
import lombok.Data;
import org.resume.fileantivirusservice.enums.ScanStatus;

import java.time.Instant;

@Data
@Builder
public class FileScanResult {
    private String s3Key;
    private ScanStatus status;
    private String virusName;
    private String errorMessage;
    private Instant scannedAt;
}
