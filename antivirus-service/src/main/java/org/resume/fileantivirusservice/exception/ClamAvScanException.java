package org.resume.fileantivirusservice.exception;

public class ClamAvScanException extends RetryableException {
    public ClamAvScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
