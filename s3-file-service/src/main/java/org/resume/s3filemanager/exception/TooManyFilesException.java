package org.resume.s3filemanager.exception;

import org.resume.s3filemanager.constant.ErrorMessages;

public class TooManyFilesException extends RuntimeException {
    public TooManyFilesException(int maxBatchSize) {
        super(String.format(ErrorMessages.MAX_FILES_EXCEEDED, maxBatchSize));
    }
}
