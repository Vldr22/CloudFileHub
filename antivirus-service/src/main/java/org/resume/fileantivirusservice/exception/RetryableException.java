package org.resume.fileantivirusservice.exception;

/**
 * Исключение для технических ошибок, которые требуют retry.
 * Например: ClamAV недоступен, S3 не отвечает, network timeout.
 */
public class RetryableException extends RuntimeException {
    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
