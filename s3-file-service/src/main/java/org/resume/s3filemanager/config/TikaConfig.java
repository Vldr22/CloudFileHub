package org.resume.s3filemanager.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Apache Tika для определения типов файлов.
 * <p>
 * Предоставляет bean {@link Tika} для анализа файловых сигнатур
 * и защиты от подмены типа файла.
 *
 * @see Tika
 */
@Configuration
public class TikaConfig {

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
