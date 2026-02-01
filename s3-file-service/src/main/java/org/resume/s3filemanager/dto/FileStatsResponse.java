package org.resume.s3filemanager.dto;

import lombok.Builder;

@Builder
public record FileStatsResponse(
        long pending,
        long clean,
        long infected,
        long error,
        long total
) {
}
