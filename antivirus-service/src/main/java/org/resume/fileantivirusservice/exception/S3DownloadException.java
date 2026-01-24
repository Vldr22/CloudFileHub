package org.resume.fileantivirusservice.exception;

public class S3DownloadException extends RetryableException {
    public S3DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
