package org.resume.s3filemanager.exception;

import lombok.Getter;
import org.resume.s3filemanager.constant.ErrorMessages;

@Getter
public class UserNotFoundException extends RuntimeException {

    private final String identifier;

    public UserNotFoundException(String identifier) {
        super(ErrorMessages.USER_NOT_FOUND);
        this.identifier = identifier;
    }

    public UserNotFoundException(Long id) {
        this(String.valueOf(id));
    }
}
