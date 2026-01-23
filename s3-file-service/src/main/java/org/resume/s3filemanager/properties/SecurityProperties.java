package org.resume.s3filemanager.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private final boolean cookieSecure;
}
