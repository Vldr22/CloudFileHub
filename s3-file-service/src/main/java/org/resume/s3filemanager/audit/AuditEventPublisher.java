package org.resume.s3filemanager.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.MdcConstants;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.resume.s3filemanager.enums.CommonResponseStatus;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Сервис для публикации событий аудита.
 * <p>
 * Извлекает контекстную информацию из MDC и создает события аудита.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Публикует событие аудита с извлечением контекста из MDC.
     */
    public void publish(Object source,
                        AuditOperation operation,
                        ResourceType resourceType,
                        String resourceId,
                        CommonResponseStatus status,
                        String details) {
        try {
            String requestId = MDC.get(MdcConstants.REQUEST_ID);
            String username = MDC.get(MdcConstants.USERNAME);
            String ipAddress = MDC.get(MdcConstants.IP_ADDRESS);

            AuditEvent event = new AuditEvent(
                    source,
                    requestId,
                    username != null ? username : SecurityConstants.ANONYMOUS,
                    ipAddress,
                    operation,
                    resourceType,
                    resourceId,
                    status,
                    details
            );

            eventPublisher.publishEvent(event);
            log.debug("Audit event published: {} {} - {}", operation, resourceType, status);

        } catch (Exception e) {
            log.error("Failed to publish audit event", e);
        }
    }

    /**
     * Публикует событие о неудачной операции с информацией об исключении.
     */
    public void publishFailure(Object source,
                               AuditOperation operation,
                               ResourceType resourceType,
                               String resourceId,
                               Exception exception) {
        publish(source, operation, resourceType, resourceId,
                CommonResponseStatus.ERROR, exception.getMessage());
    }
}
