package org.resume.s3filemanager.dto;

import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.enums.CommonResponseStatus;

import java.time.Instant;

public record AuditLogFilterRequest(
        String username,
        AuditOperation operation,
        CommonResponseStatus status,
        Instant from,
        Instant to) {
}
