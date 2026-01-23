package org.resume.s3filemanager.dto;

import lombok.Builder;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.enums.UserStatus;

@Builder
public record UserDetailsResponse(
        Long id,
        String username,
        UserRole role,
        UserStatus status,
        FileUploadStatus uploadStatus) {
}
