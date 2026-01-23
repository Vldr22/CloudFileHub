package org.resume.s3filemanager.exception;

import org.resume.s3filemanager.constant.ErrorMessages;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super(ErrorMessages.USER_ALREADY_EXISTS);
    }
}
