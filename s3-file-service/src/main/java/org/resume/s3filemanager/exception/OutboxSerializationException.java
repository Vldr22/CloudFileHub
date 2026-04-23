package org.resume.s3filemanager.exception;

import lombok.Getter;

@Getter
public class OutboxSerializationException extends RuntimeException {

    private final Long fileId;

    public OutboxSerializationException(Throwable cause, Long fileId) {
        super(cause);
        this.fileId = fileId;
    }
}
