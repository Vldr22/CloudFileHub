package org.resume.s3filemanager.dto;

import lombok.Builder;

@Builder
public record FileResponse(
        String fileName,
        String uniqueName,
        String fileSize) {
}
