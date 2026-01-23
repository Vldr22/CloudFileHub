package org.resume.s3filemanager.exception;

import lombok.Getter;
import org.resume.s3filemanager.constant.ErrorMessages;
import org.resume.s3filemanager.dto.MultipleUploadResponse;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MultipleFileUploadException extends RuntimeException {

    private final List<MultipleUploadResponse> responses;

    public MultipleFileUploadException( List<MultipleUploadResponse> responses) {
        super("%s: %s".formatted(
                ErrorMessages.FILES_UPLOAD_ERROR,
                responses.stream()
                        .map(r -> "%s(%s)"
                                .formatted(r.originalFileName(), r.message()))
                        .collect(Collectors.joining(", "))));
        this.responses = responses;
    }
}