package org.resume.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadEvent {
    private Long fileId;
    private Long userId;
    private String s3Key;
    private String bucketName;
    private String originalFileName;
}
