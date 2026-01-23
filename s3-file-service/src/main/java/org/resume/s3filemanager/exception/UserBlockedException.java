package org.resume.s3filemanager.exception;

import lombok.Getter;
import org.resume.s3filemanager.constant.ErrorMessages;

@Getter
public class UserBlockedException extends RuntimeException {

    private final String username;

    public UserBlockedException(String username) {
        super(ErrorMessages.USER_BLOCKED);
        this.username = username;
    }
}
