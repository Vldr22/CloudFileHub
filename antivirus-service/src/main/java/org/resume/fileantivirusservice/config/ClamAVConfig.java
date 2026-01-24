package org.resume.fileantivirusservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.fileantivirusservice.properties.ClamAVProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.capybara.clamav.ClamavClient;

/**
 * Конфигурация ClamAV клиента для антивирусного сканирования.
 * <p>
 * Создает {@link ClamavClient} с настройками подключения к ClamAV демону.
 * Используется для сканирования файлов на вирусы через TCP соединение.
 *
 * @see ClamAVProperties
 * @see ClamavClient
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClamAVConfig {

    private final ClamAVProperties clamAVProperties;

    @Bean
    public ClamavClient clamavClient() {
        log.info("Initializing ClamAV client: {}:{}",
                clamAVProperties.getHost(), clamAVProperties.getPort());

        return new ClamavClient(
                clamAVProperties.getHost(),
                clamAVProperties.getPort()
        );
    }

}
