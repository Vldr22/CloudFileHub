package org.resume.s3filemanager.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.pageable")
public class PageableProperties {

    @Min(value = 1, message = "Default page size must be at least 1")
    @Max(value = 100, message = "Default page size must not exceed 100")
    private final int defaultPageSize;

    @Min(value = 1, message = "Max page size must be at least 1")
    @Max(value = 100, message = "Max page size must not exceed 100")
    private final int maxPageSize;

}
