package org.resume.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileScanResult {
    private Long fileId;
    private String s3Key;
    private ScanStatus status;
    private String virusName;
    private String errorMessage;
    private Instant scannedAt;
}
