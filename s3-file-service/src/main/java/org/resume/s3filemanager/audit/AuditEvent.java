package org.resume.s3filemanager.audit;

import lombok.Getter;
import org.resume.s3filemanager.enums.CommonResponseStatus;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Событие аудита операции.
 */
@Getter
public class AuditEvent extends ApplicationEvent {

    private final String requestId;
    private final String username;
    private final String ipAddress;
    private final AuditOperation operation;
    private final ResourceType resourceType;
    private final String resourceId;
    private final CommonResponseStatus status;
    private final String details;
    private final Instant occurredAt;

    public AuditEvent(Object source,
                      String requestId,
                      String username,
                      String ipAddress,
                      AuditOperation operation,
                      ResourceType resourceType,
                      String resourceId,
                      CommonResponseStatus status,
                      String details) {
        super(source);
        this.requestId = requestId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.operation = operation;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.status = status;
        this.details = details;
        this.occurredAt = Instant.now();
    }
}
