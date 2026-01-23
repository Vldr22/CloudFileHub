package org.resume.s3filemanager.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDownloadResponse {
    private byte[] content;
    private String fileName;
    private String contentType;
    private long size;
}
