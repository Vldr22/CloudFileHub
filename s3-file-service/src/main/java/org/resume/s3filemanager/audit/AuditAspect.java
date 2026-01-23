package org.resume.s3filemanager.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.resume.s3filemanager.enums.CommonResponseStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditEventPublisher auditEventPublisher;

    @Around("@annotation(org.resume.s3filemanager.audit.Auditable)")
    public Object auditOperation(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Auditable auditable = methodSignature.getMethod().getAnnotation(Auditable.class);

        AuditOperation operation = auditable.operation();
        ResourceType resourceType = auditable.resourceType();

        try {
            Object result = joinPoint.proceed();

            String resourceId = extractResourceId(joinPoint.getArgs());
            auditEventPublisher.publish(
                    this,
                    operation,
                    resourceType,
                    resourceId,
                    CommonResponseStatus.SUCCESS,
                    null
            );

            return result;

        } catch (Throwable e) {
            String resourceId = extractResourceId(joinPoint.getArgs());
            auditEventPublisher.publish(
                    this,
                    operation,
                    resourceType,
                    resourceId,
                    CommonResponseStatus.ERROR,
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Извлекает идентификатор ресурса из аргументов метода.
     * Для FILE операций: оригинальное имя файла или String параметр.
     * Для USER операций: username из AuthRequest или String параметр.
     */
    private String extractResourceId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof MultipartFile file) {
                String filename = file.getOriginalFilename();
                if (filename != null && !filename.isBlank()) {
                    return filename;
                }
            }

            if (arg instanceof String str && !str.isBlank()) {
                return str;
            }
        }

        return SecurityConstants.UNKNOWN;
    }
}
