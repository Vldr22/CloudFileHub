package org.resume.common.dto;

import org.resume.common.enums.ScanStatus;
import lombok.Builder;
import lombok.Data;

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
