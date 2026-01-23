package org.resume.s3filemanager.exception;

import org.resume.s3filemanager.constant.ErrorMessages;

public class FileUploadLimitException extends RuntimeException {
    public FileUploadLimitException() {
        super(ErrorMessages.FILE_ALREADY_BEEN_UPLOADED);
    }
}
