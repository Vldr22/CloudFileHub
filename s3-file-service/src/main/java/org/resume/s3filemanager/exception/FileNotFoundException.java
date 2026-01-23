package org.resume.s3filemanager.exception;

import lombok.Getter;
import org.resume.s3filemanager.constant.ErrorMessages;

@Getter
public class FileNotFoundException extends RuntimeException {

    private final String fileName;

    public FileNotFoundException(String fileName) {
        super(ErrorMessages.FILE_NOT_FOUND);
        this.fileName = fileName;
    }
}
