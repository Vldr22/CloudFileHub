package org.resume.s3filemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфиг для асинхронного выполнения задач в приложении.
 * <p>
 * Создаёт ThreadPoolTaskExecutor с подключённым {@link MdcTaskDecorator} для безопасной передачи MDC
 * в асинхронные потоки.
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * Создаёт Executor для асинхронных задач с передачей MDC.
     *
     * @return настроенный ThreadPoolTaskExecutor
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");

        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        return executor;
    }

}
