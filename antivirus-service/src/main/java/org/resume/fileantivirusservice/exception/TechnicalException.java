package org.resume.fileantivirusservice.exception;

/**
 * Исключение для технических ошибок, которые требуют retry.
 * Например: ClamAV недоступен, S3 не отвечает и т д.
 */
public class TechnicalException extends RuntimeException {
    public TechnicalException(String message) {
        super(message);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
