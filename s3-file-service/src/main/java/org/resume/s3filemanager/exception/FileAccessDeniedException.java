package org.resume.s3filemanager.exception;

import org.resume.s3filemanager.constant.ErrorMessages;

public class FileAccessDeniedException extends RuntimeException {
    public FileAccessDeniedException() {
        super(ErrorMessages.ACCESS_DENIED_DELETE_FILE);
    }
}
