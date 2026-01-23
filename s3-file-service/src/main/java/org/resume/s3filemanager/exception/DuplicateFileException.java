package org.resume.s3filemanager.exception;

import org.resume.s3filemanager.constant.ErrorMessages;

public class DuplicateFileException extends RuntimeException {
    public DuplicateFileException() {
        super(ErrorMessages.DUPLICATE_FILE_ERROR);
    }
}
