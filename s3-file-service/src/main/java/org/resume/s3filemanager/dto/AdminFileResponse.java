package org.resume.s3filemanager.dto;

import lombok.Builder;
import org.resume.common.model.ScanStatus;

@Builder
public record AdminFileResponse(
        Long id,
        String fileName,
        String uniqueName,
        String fileSize,
        String type,
        ScanStatus scanStatus,
        String uploadedUserName
) {
}
