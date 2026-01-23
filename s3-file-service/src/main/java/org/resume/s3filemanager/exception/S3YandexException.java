package org.resume.s3filemanager.exception;

import lombok.Getter;

@Getter
public class S3YandexException extends RuntimeException {
    private final String fileName;

    public S3YandexException(Throwable cause, String fileName) {
        super(cause);
        this.fileName = fileName;
    }
}
