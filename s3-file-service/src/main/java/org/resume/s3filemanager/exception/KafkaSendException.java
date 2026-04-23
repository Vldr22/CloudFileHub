package org.resume.s3filemanager.exception;

import lombok.Getter;

@Getter
public class KafkaSendException extends RuntimeException {

    private final Long fileId;

    public KafkaSendException(Throwable cause, Long fileId) {
        super(cause);
        this.fileId = fileId;
    }
}
