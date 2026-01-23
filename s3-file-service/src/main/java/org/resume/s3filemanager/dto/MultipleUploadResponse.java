package org.resume.s3filemanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.resume.s3filemanager.enums.CommonResponseStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MultipleUploadResponse(
        CommonResponseStatus status,
        String originalFileName,
        String uniqueName,
        String message) {
}