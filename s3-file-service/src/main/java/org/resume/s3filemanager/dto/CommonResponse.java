package org.resume.s3filemanager.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.resume.s3filemanager.enums.CommonResponseStatus;
import org.springframework.http.ProblemDetail;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private T data;
    private CommonResponseStatus status;
    private ProblemDetail problemDetail;
    private LocalDateTime timestamp;

    public static <T> CommonResponse<T> success(T data) {
         return new CommonResponse<>(
                 data,
                 CommonResponseStatus.SUCCESS,
                 null,
                 LocalDateTime.now());
    }

    public static <T> CommonResponse<T> error(ProblemDetail problemDetail) {
        return new CommonResponse<>(
                null,
                CommonResponseStatus.ERROR,
                problemDetail,
                LocalDateTime.now());
    }

}
