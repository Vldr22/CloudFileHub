package org.resume.s3filemanager.exception;

import lombok.Getter;

@Getter
public class FileReadException extends RuntimeException {

    private final String originalFileName;

    public FileReadException(Throwable cause, String originalFileName) {
        super(cause);
        this.originalFileName = originalFileName;
    }
}
