package org.resume.fileantivirusservice.constant;

import lombok.experimental.UtilityClass;

/**
 * Константы для сообщений об ошибках.
 */
@UtilityClass
public class ErrorMessages {

    // === S3 Errors ===
    public static final String S3_SERVICE_UNAVAILABLE = "S3 service unavailable";
    public static final String S3_READ_FAILED = "Failed to read file from S3";
    public static final String S3_UNEXPECTED_ERROR = "Unexpected S3 error";

    // === ClamAV Errors ===
    public static final String CLAMAV_UNAVAILABLE = "ClamAV service unavailable";
    public static final String CLAMAV_UNKNOWN_RESULT = "Unknown scan result type";

    // === DLQ Errors ===
    public static final String DLQ_SCAN_FAILED = "File scan failed after multiple retry attempts";
}
