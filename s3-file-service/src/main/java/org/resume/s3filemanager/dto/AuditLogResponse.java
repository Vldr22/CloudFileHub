package org.resume.s3filemanager.dto;

import lombok.Builder;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.audit.ResourceType;
import org.resume.s3filemanager.enums.CommonResponseStatus;

import java.time.Instant;

@Builder
public record AuditLogResponse(
        Long id,
        String requestId,
        String username,
        String ipAddress,
        AuditOperation operation,
        ResourceType resourceType,
        String resourceId,
        CommonResponseStatus status,
        String details,
        Instant timestamp) {
}
