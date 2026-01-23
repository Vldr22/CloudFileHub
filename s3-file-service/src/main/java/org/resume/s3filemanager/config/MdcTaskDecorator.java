package org.resume.s3filemanager.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Декоратор задач для Spring @Async.
 * <p>
 * Передаёт MDC (requestId, username, ip) в асинхронный поток.
 * <p>
 * После завершения задачи контекст ОЧИЩАЕТСЯ, чтобы избежать утечек.
 */
@Slf4j
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            setMdcContext(contextMap);
            try {
                runnable.run();
            } finally {
                clearMdcContext();
            }
        };
    }

    private void setMdcContext(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
            log.debug("MDC context set in async thread: {}", contextMap);
        } else {
            log.debug("No MDC context found for async thread");
        }
    }

    private void clearMdcContext() {
        MDC.clear();
        log.debug("MDC context cleared in async thread");
    }
}
