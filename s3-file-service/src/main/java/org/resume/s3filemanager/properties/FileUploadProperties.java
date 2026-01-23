package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.multiple-upload")
public class FileUploadProperties {
    @Positive
    @Max(value = 10)
    private final int maxBatchSize;
}
