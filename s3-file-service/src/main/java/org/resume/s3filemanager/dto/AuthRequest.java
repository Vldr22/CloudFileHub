package org.resume.s3filemanager.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.resume.s3filemanager.constant.ValidationMessages;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @Size(min = 3, max = 50, message = ValidationMessages.USERNAME_SIZE)
    private String username;

    @NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @Size(min = 3, max = 50, message = ValidationMessages.PASSWORD_SIZE)
    private String password;

}
